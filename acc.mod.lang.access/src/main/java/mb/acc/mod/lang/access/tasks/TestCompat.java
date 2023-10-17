package mb.acc.mod.lang.access.tasks;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.accmodlangaccess.task.AccModLangAccessAnalyze;
import mb.accmodlangaccess.task.AccModLangAccessParse;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.constraint.pie.ConstraintAnalyzeTaskDef;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandFeedbackBuilder;
import mb.spoofax.core.language.command.ShowFeedback;

public abstract class TestCompat implements TaskDef<TestCompat.Args, CommandFeedback>{

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
	            ", rootDirectory" + rootDirectory +
	            '}';
	    }
	}

	protected static final String relativeTargetDirRoot = "build/test";
	
	protected final AccModLangAccessParse parse;
	protected final AccModLangAccessAnalyze analyze;
	protected final TransformWithAnalysis transform;
	protected final ResourceService resourceService;
	
	private final String langName;

	protected TestCompat(AccModLangAccessParse parse, AccModLangAccessAnalyze analyze, TransformWithAnalysis transform, ResourceService resourceService, String langDir) {
		this.parse = parse;
		this.analyze = analyze;
		this.transform = transform;
		this.resourceService = resourceService;
		this.langName = langDir;
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
		
		// 2. Compile
		final Result<KeyedMessages, IOException> compilerOutput = compile(context, args, TermUtils.toListAt(transformResultTerm, 1));
		if(compilerOutput.isErr()) {
			return CommandFeedback.ofTryExtractMessagesFrom(compilerOutput.getErr(), file);
		}
		final boolean javaCompilerOutputHasErrors = compilerOutput.get().containsErrorOrHigher();
		
		// 3. Compare Results
		final boolean sourceAnalysisHasErrors = analysisResult.result.messages.containsErrorOrHigher();
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
		cfb.withKeyedMessages(compilerOutput.get());
		return cfb.build();
	}

	protected abstract Result<KeyedMessages, IOException> compile(ExecContext context, Args args,
			IStrategoList transformResultTerm);


	protected HierarchicalResource normalizeRoot(ResourcePath rootDirectory) {
		if(rootDirectory.getQualifier().equals("spt")) {
			// SPT just gives the test case as project resource, so call `getRoot()`
			return resourceService.getHierarchicalResource(ResourceKeyString.of("eclipse-resource", rootDirectory.getIdAsString())).getRoot();
		}
		return resourceService.getHierarchicalResource(rootDirectory);
	}

	protected String normalizeFile(ResourceKey file, HierarchicalResource root) {
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

	protected HierarchicalResource buildRoot(HierarchicalResource project) {
		return project
			.appendAsRelativePath(relativeTargetDirRoot)
			.appendAsRelativePath(langName);
	}

}