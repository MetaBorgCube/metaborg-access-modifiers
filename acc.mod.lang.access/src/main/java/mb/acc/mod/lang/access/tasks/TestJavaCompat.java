package mb.acc.mod.lang.access.tasks;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessAnalyze;
import mb.accmodlangaccess.task.AccModLangAccessParse;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.constraint.pie.ConstraintAnalyzeTaskDef;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.task.java.CompileJava;
import mb.pie.task.java.jdk.JdkJavaCompiler;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceService;
import mb.resource.WritableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandFeedbackBuilder;
import mb.spoofax.core.language.command.ShowFeedback;


@AccModLangAccessScope
public class TestJavaCompat implements TaskDef<TestJavaCompat.Args, CommandFeedback> {
	
    public static class Args implements Serializable {
    	
		private static final long serialVersionUID = 42L;
		
		public final ResourceKey file;
		public final ResourcePath rootDirectory;

        public Args(ResourceKey file, ResourcePath rootDirectory) {
            this.file = file;
			this.rootDirectory = rootDirectory;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Args args = (Args)o;
            return file.equals(args.file) && rootDirectory.equals(args.rootDirectory);
        }

        @Override public int hashCode() {
            return Objects.hash(file, rootDirectory);
        }

        @Override public String toString() {
            return "Args{" +
                "file=" + file +
                "rootDirectory" + rootDirectory +
                '}';
        }
    }
	
    
    private static final String relativeTargetDirRoot = "build/test/java";
    
    private final AccModLangAccessParse parse;
	private final AccModLangAccessAnalyze analyze;
	private final TransformToJava transform;
	private final ResourceService resourceService;
		
	
	@Inject
	public TestJavaCompat(AccModLangAccessParse parse, AccModLangAccessAnalyze analyze, TransformToJava transform, ResourceService resourceService) {
		this.parse = parse;
		this.analyze = analyze;
		this.transform = transform;
		this.resourceService = resourceService;
	}

	@Override
	public CommandFeedback exec(ExecContext context, Args args) throws Exception {
		final ResourceKey file = args.file;
		
		// 0. Extract analysis result
		final Supplier<Result<ConstraintAnalyzeTaskDef.Output, ?>> analysisSupplier = analyze
				.createSupplier(new ConstraintAnalyzeTaskDef.Input(file, parse
						.inputBuilder()
						.withFile(file)
						.buildAstSupplier()));
		final Result<ConstraintAnalyzeTaskDef.Output, ?> analysisTaskResult = context.require(analysisSupplier);
		if(analysisTaskResult.isErr()) {
			return CommandFeedback.ofTryExtractMessagesFrom(analysisTaskResult.getErr(), file);
		}
		final ConstraintAnalyzeTaskDef.Output analysisResult = analysisTaskResult.get();
		final boolean sourceAnalysisHasErrors = analysisResult.result.messages.containsErrorOrHigher();
		
		// 1. Transform to Java
		final Result<IStrategoTerm, ? extends Exception> transformResult = context.require(transform, analysisSupplier);
		
		// Exception in transformation
		if(transformResult.isErr()) {
			return CommandFeedback.ofTryExtractMessagesFrom(transformResult.getErr(), file);
		}
		final IStrategoTerm transformResultTerm = transformResult.get();
		
		// current program is incompatible with transformation
		if(TermUtils.isAppl(transformResultTerm, "TransformFailure", 1)) {
			// Transformation failed
			final KeyedMessagesBuilder kmb = new KeyedMessagesBuilder();
			for(IStrategoTerm message : transformResultTerm.getSubterm(0)) {
				kmb.addMessage(TermUtils.toJavaString(message), Severity.Error, args.file);
			}
			return CommandFeedback.of(kmb.build());
		}
		
		// 2. Write to Disk
		final IStrategoList javaFilesTerm = TermUtils.toListAt(transformResultTerm, 1);
		final HierarchicalResource normalizedProject = normalizeRoot(args.rootDirectory);
		final String normalizedFile = normalizeFile(args.file, normalizedProject);
		final Result<Collection<ResourcePath>, IOException> writeResult = writeJavaFiles(context, javaFilesTerm, normalizedFile, normalizedProject);
		
		if(writeResult.isErr()) {
			return CommandFeedback.ofTryExtractMessagesFrom(writeResult.getErr(), file);
		}
		
		final Collection<ResourcePath> javaResources = writeResult.get();
		
		// 3. Compile Java
		final JdkJavaCompiler javaCompiler = new JdkJavaCompiler();
		final KeyedMessages compilerOutput = javaCompiler.compile(
				context,
				ListView.of(CompileJava.Sources.builder()
					.addAllSourceFiles(javaResources)
					.build()), 
				ListView.of(), 
				ListView.of(), 
				"11", 
				resourceService.getHierarchicalResource(args.rootDirectory)
					.appendAsRelativePath(relativeTargetDirRoot)
					.appendAsRelativePath("src-gen")
					.appendAsRelativePath(normalizedFile)
					.getPath(), 
				resourceService.getHierarchicalResource(args.rootDirectory)
					.appendAsRelativePath(relativeTargetDirRoot)
					.appendAsRelativePath("build")
					.appendAsRelativePath(normalizedFile)
					.getPath(),
				false, 
				false, 
				ListView.of()
		);
		final boolean javaCompilerOutputHasErrors = compilerOutput.containsErrorOrHigher();
		
		// 4. Compare Results
		if(sourceAnalysisHasErrors) {
			if(javaCompilerOutputHasErrors) {
				return CommandFeedback.of(ShowFeedback.showText("Both source analysis and target compilation returned errors", "Compatible"));
			}
			
			final CommandFeedbackBuilder cfb = new CommandFeedbackBuilder();
			cfb.addShowFeedback(ShowFeedback.showText("Error: source language analysis reported no errors, but Java compiler did.", "Java Compatibility check Failed"));
			cfb.withKeyedMessages(analysisResult.result.messages.toKeyed(args.file));
			return cfb.build();
		} 
		// source analysis has no errors
		if(!javaCompilerOutputHasErrors) {
			return CommandFeedback.of(ShowFeedback.showText("Both source analysis and target compilation returned no errors", "Compatible"));
		}
		// but java compiler did
		final CommandFeedbackBuilder cfb = new CommandFeedbackBuilder();
		cfb.addShowFeedback(ShowFeedback.showText("Error: source language analysis reported errors, but Java compiler did not.", "Java Compatibility check Failed"));
		cfb.withKeyedMessages(compilerOutput);
		return cfb.build();
	}

	private Result<Collection<ResourcePath>, IOException> writeJavaFiles(ExecContext ctx, IStrategoList transformedPPedFiles, String file, HierarchicalResource project) {
		final HierarchicalResource targetRoot = project
				.appendAsRelativePath(relativeTargetDirRoot)
				.appendAsRelativePath("src")
				.appendAsRelativePath(file);
		final ArrayList<ResourcePath> javaFiles = new ArrayList<>();
		
		for(IStrategoTerm classFile : transformedPPedFiles) {
			final String ppClass = TermUtils.toJavaStringAt(classFile, 1);
			final String javaPath = TermUtils.toJavaStringAt(classFile, 0);
			final ResourcePath classFilePath = targetRoot.appendRelativePath(javaPath).getKey();
			final WritableResource classFileResource = resourceService.getWritableResource(classFilePath);
			final Path localFile = resourceService.toLocalFile(classFileResource).toPath();

			try {
				if(!Files.exists(localFile)) {
					Files.createDirectories(localFile.getParent());
					Files.createFile(localFile);
				}
			} catch (IOException e) {
				return Result.ofErr(e);
			}
			
			try(BufferedOutputStream write = classFileResource.openWriteBuffered()) {
				write.write(ppClass.getBytes());
				write.flush();

				ctx.provide(classFileResource);
				javaFiles.add(classFilePath);
			} catch (IOException e) {
				return Result.ofErr(e);
			}
		}
		
		return Result.ofOk(Collections.unmodifiableList(javaFiles));
	}

	
	private HierarchicalResource normalizeRoot(ResourcePath rootDirectory) {
		if(rootDirectory.getQualifier().equals("spt")) {
			// SPT just gives the test case as project resource, so call `getRoot()`
			return resourceService.getHierarchicalResource(ResourceKeyString.of("eclipse-resource", rootDirectory.getIdAsString())).getRoot();
		}
		return resourceService.getHierarchicalResource(rootDirectory);
	}

	private String normalizeFile(ResourceKey file, HierarchicalResource root) {
		if(file.getQualifier().equals("spt")) {
			final String[] components = file.getIdAsString().split("!!");
			final ResourceKeyString baseResourcePath = ResourceKeyString.of(root.getPath().getQualifier(), components[0]);
			final ResourcePath baseResource = resourceService.getResourcePath(baseResourcePath);
			if(components.length == 1) {
				// no specific test case
				return root.getPath().relativize(baseResource);
			} else {
				return root.getPath().relativize(baseResource.appendAsRelativePath(components[1]));
			}
		}
		final ResourceKeyString baseResourcePath = ResourceKeyString.of(root.getPath().getQualifier(), file.getIdAsString());
		final ResourcePath baseResource = resourceService.getResourcePath(baseResourcePath);
		return root.getPath().relativize(baseResource);
	}

	@Override
	public String getId() {
		return TestJavaCompat.class.getCanonicalName();
	}

}
