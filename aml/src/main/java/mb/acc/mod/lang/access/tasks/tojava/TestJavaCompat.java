package mb.acc.mod.lang.access.tasks.tojava;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.acc.mod.lang.access.tasks.TestCompat;
import mb.accmodlang.AMLClassLoaderResources;
import mb.accmodlang.AMLScope;
import mb.accmodlang.task.AMLAnalyze;
import mb.accmodlang.task.AMLParse;
import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.pie.task.java.CompileJava;
import mb.pie.task.java.jdk.JdkJavaCompiler;
import mb.resource.ResourceService;
import mb.resource.WritableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;


@AMLScope
public class TestJavaCompat extends TestCompat {
	
    @Inject
	public TestJavaCompat(
			AMLClassLoaderResources classLoaderResources, 
			AMLParse parse,
			InsertJavaSettings insertSettings,
			AMLAnalyze analyze, 
			TransformToJava transform, 
			ResourceService resourceService
	) {
    	super(classLoaderResources, parse, insertSettings, analyze, transform, resourceService, "java");
	}
	
	@Override
	protected Result<KeyedMessages, IOException> compile(ExecContext context, Args args, IStrategoTerm pp) {
		final IStrategoList javaFilesTerm = TermUtils.toList(pp);
		final HierarchicalResource normalizedProject = normalizeRoot(args.rootDirectory);
		final String normalizedFile = normalizeFile(args.file, normalizedProject);
		final Result<Collection<ResourcePath>, IOException> writeResult = writeJavaFiles(context, javaFilesTerm, normalizedFile, normalizedProject);
		
		if(writeResult.isErr()) {
			return Result.ofErr(writeResult.getErr());
		}
		
		final Collection<ResourcePath> javaResources = writeResult.get();
		
		// 3. Compile Java
		final JdkJavaCompiler javaCompiler = new JdkJavaCompiler();
		KeyedMessages compilerOutput;
		try {
			compilerOutput = javaCompiler.compile(
					context,
					ListView.of(CompileJava.Sources.builder()
						.addAllSourceFiles(javaResources)
						.build()), 
					ListView.of(), 
					ListView.of(), 
					"11", 
					buildRoot(normalizedProject)
						.appendAsRelativePath("src-gen")
						.appendAsRelativePath(normalizedFile)
						.getPath(), 
					buildRoot(normalizedProject)
						.appendAsRelativePath("build")
						.appendAsRelativePath(normalizedFile)
						.getPath(),
					false, 
					false, 
					ListView.of()
			);
		} catch (IOException e) {
			return Result.ofErr(e);
		}
		
		return Result.ofOk(compilerOutput);
	}

	private Result<Collection<ResourcePath>, IOException> writeJavaFiles(ExecContext ctx, IStrategoList transformedPPedFiles, String file, HierarchicalResource project) {
		final HierarchicalResource targetRoot = buildRoot(project)
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

	@Override
	public String getId() {
		return TestJavaCompat.class.getCanonicalName();
	}

}
