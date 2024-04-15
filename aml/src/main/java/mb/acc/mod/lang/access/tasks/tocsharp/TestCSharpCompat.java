package mb.acc.mod.lang.access.tasks.tocsharp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.acc.mod.lang.access.tasks.TestCompat;
import mb.accmodlang.AMLClassLoaderResources;
import mb.accmodlang.task.AMLAnalyze;
import mb.accmodlang.task.AMLParse;
import mb.common.message.KeyedMessages;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;

public class TestCSharpCompat extends TestCompat {

	@Inject
	public TestCSharpCompat(
			AMLClassLoaderResources classLoaderResources, 
			AMLParse parse,
			InsertCSharpSettings insertSettings,
			AMLAnalyze analyze, 
			TransformToCSharp transform,
			ResourceService resourceService
	) {
		super(classLoaderResources, parse, insertSettings, analyze, transform, resourceService, "csharp");
	}

	@Override
	public String getId() {
		return TestCSharpCompat.class.getCanonicalName();
	}

	@Override
	protected Result<KeyedMessages, IOException> compile(ExecContext context, Args args, IStrategoTerm pp) {
		final IStrategoList csharpFilesTerm = TermUtils.toList(pp);
		try {
			final HierarchicalResource root = normalizeRoot(args.rootDirectory);
			final HierarchicalResource rootDir = buildRoot(root);
			final String normalizedFile = normalizeFile(args.file, root);
			final ResourcePath srcDir = rootDir
//				.appendAsRelativePath("src")
					.appendAsRelativePath(normalizedFile).getPath();

			// Create Solution
			final File srcDirFile = resourceService.toLocalFile(srcDir);
			if (!srcDirFile.exists()) {
				srcDirFile.mkdirs();
			}
			new NewSolutionCommand("Test").run(context, srcDir);

			// Create projects
			for (IStrategoTerm ppFile : csharpFilesTerm) {
				final String name = TermUtils.toJavaStringAt(ppFile, 0);
				final String contents = TermUtils.toJavaStringAt(ppFile, 2);

				// Create project
				new NewProjectCommand(name).run(context, srcDir);

				final File classFile = resourceService
						.toLocalFile(srcDir.appendAsRelativePath(name).appendAsRelativePath("Class1.cs"));

				// Add project to Solution
				new AddProjectCommand(name, "Test").run(context, srcDir);

				// Write content to file
				Files.write(classFile.toPath(), Collections.singleton(contents), StandardOpenOption.TRUNCATE_EXISTING);

			}

			// Add dependencies
			for (IStrategoTerm ppFile : csharpFilesTerm) {
				final String name = TermUtils.toJavaStringAt(ppFile, 0);
				final IStrategoList imports = TermUtils.toListAt(ppFile, 1);
				for (IStrategoTerm imp : imports) {
					new AddDependencyCommand(name, TermUtils.toJavaString(imp)).run(context, srcDir);
				}
			}

			// Build
			return Result.ofOk(new BuildCommand().run(context, srcDir, false));
		} catch (IOException e) {
			return Result.ofErr(e);
		}
	}

	private abstract class CSCommand extends Command {
		
		protected CSCommand(String subCommand) {
			super("/usr/local/share/dotnet/dotnet");
			addArgument(subCommand);
		}
		
		protected String solutionFile(String solutionName) {
			return solutionName + ".sln";
		}

		protected String projectFile(String projectName) {
			return String.format("%s/%s.csproj", projectName, projectName);
		}
		
	}
	

	private final class NewSolutionCommand extends CSCommand {

		public NewSolutionCommand(String name) {
			super("new sln");
			addOption("name", name);
			addOption("force");
			provide(solutionFile(name));
		}

	}

	private final class NewProjectCommand extends CSCommand {

		public NewProjectCommand(String name) {
			super("new classlib");
			addOption("output", name);
			addOption("force");
			provide(projectFile(name));
		}

	}

	private final class AddProjectCommand extends CSCommand {

		public AddProjectCommand(String projectName, String solutionName) {
			super("sln");
			
			final String solutionFile = solutionFile(solutionName);
			addArgument(solutionFile);
			require(solutionFile);
			
			addParameter("add", projectName);
			provide(projectFile(projectName));
			
			provide(projectName + "/Class1.cs");
		}

	}

	private final class AddDependencyCommand extends CSCommand {

		public AddDependencyCommand(String projectName, String dependencyName) {
			super("add");
			
			addArgument(projectName);
			require(projectFile(projectName));
			
			addParameter("reference", dependencyName);
			require(projectFile(dependencyName));
		}

	}

	private final class BuildCommand extends CSCommand {

		public BuildCommand() {
			super("build");
		}

	}

}
