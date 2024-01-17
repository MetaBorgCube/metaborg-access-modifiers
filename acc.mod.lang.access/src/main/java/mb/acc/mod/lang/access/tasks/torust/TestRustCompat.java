package mb.acc.mod.lang.access.tasks.torust;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.acc.mod.lang.access.tasks.TestCompat;
import mb.accmodlangaccess.AccModLangAccessClassLoaderResources;
import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessAnalyze;
import mb.accmodlangaccess.task.AccModLangAccessParse;
import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.resource.ResourceService;
import mb.resource.WritableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;


@AccModLangAccessScope
public class TestRustCompat extends TestCompat {
	
    private static final String PROGRAM_FILE_NAME = "program.rs";

	@Inject
	public TestRustCompat(
			AccModLangAccessClassLoaderResources classLoaderResources, 
			AccModLangAccessParse parse,
			InsertRustSettings insertSettings,
			AccModLangAccessAnalyze analyze, 
			TransformToRust transform, 
			ResourceService resourceService
	) {
    	super(classLoaderResources, parse, insertSettings, analyze, transform, resourceService, "rust");
	}
	
	@Override
	protected Result<KeyedMessages, IOException> compile(ExecContext context, Args args, IStrategoTerm rustAst) {
		final HierarchicalResource normalizedProject = normalizeRoot(args.rootDirectory);
		final String normalizedFile = normalizeFile(args.file, normalizedProject);
		final Result<ResourcePath, IOException> writeResult = writeRustFile(context, rustAst, normalizedFile, normalizedProject);
		
		if(writeResult.isErr()) {
			return Result.ofErr(writeResult.getErr());
		}
		
		final ResourcePath rustFilePath = writeResult.get();
		
		// 3. Compile Rust
		final CompileRustCommand compileCommand = new CompileRustCommand(PROGRAM_FILE_NAME);
		KeyedMessages compilerOutput;
		try {
			compilerOutput = compileCommand.run(context, rustFilePath.getParent(), false);
		} catch (IOException e) {
			return Result.ofErr(e);
		}
		
		return Result.ofOk(compilerOutput);
	}

	private Result<ResourcePath, IOException> writeRustFile(ExecContext ctx, IStrategoTerm rustAst, String file, HierarchicalResource project) {
		final HierarchicalResource targetRoot = buildRoot(project)
				.appendAsRelativePath("src")
				.appendAsRelativePath(file);		

		final ResourcePath programFilePath = targetRoot.appendRelativePath(PROGRAM_FILE_NAME).getKey();
		final WritableResource classFileResource = resourceService.getWritableResource(programFilePath);
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
			write.write(TermUtils.toJavaString(rustAst).getBytes());
			write.flush();

			ctx.provide(classFileResource);
		} catch (IOException e) {
			return Result.ofErr(e);
		}
		
		return Result.ofOk(programFilePath);
	}

	@Override
	public String getId() {
		return TestRustCompat.class.getCanonicalName();
	}
	
	private class CompileRustCommand extends Command {

		protected CompileRustCommand(String filePath) {
			super("rustc");
			
			addArgument(filePath);
		}
		
	}

}
