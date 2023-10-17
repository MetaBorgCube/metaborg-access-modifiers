package mb.acc.mod.lang.access.tasks.tocsharp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.acc.mod.lang.access.tasks.TestCompat;
import mb.accmodlangaccess.task.AccModLangAccessAnalyze;
import mb.accmodlangaccess.task.AccModLangAccessParse;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;

public class TestCSharpCompat extends TestCompat {

	@Inject
	public TestCSharpCompat(AccModLangAccessParse parse, AccModLangAccessAnalyze analyze, TransformToCSharp transform,
			ResourceService resourceService) {
		super(parse, analyze, transform, resourceService, "csharp");
	}

	@Override
	public String getId() {
		return TestCSharpCompat.class.getCanonicalName();
	}

	@Override
	protected Result<KeyedMessages, IOException> compile(ExecContext context, Args args,
			IStrategoList csharpFilesTerm) {
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
				if (!srcDirFile.mkdirs()) {
					context.logger().warn("Cannot create directory {}.", srcDirFile);
				}
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

	private abstract class Command {

		private List<String> cmd = new ArrayList<String>();

		protected Command(String subCommand) {
			cmd.add("dotnet");
			cmd.add(subCommand);
		}

		protected void addArgument(String option) {
			cmd.add(option);
		}

		protected void addOption(String flag) {
			cmd.add(String.format("--%s", flag));
		}

		protected void addOption(String flag, String value) {
			cmd.add(String.format("--%s %s", flag, value));
		}

		protected void addParameter(String name, String value) {
			cmd.add(String.format("%s %s", name, value));
		}

		protected void run(ExecContext context, ResourcePath pwd) throws IOException {
			run(context, pwd, true);
		}

		protected KeyedMessages run(ExecContext context, ResourcePath pwd, boolean mustSucceed) throws IOException {
			final Runtime rt = Runtime.getRuntime();
			final String cmd = String.join(" ", this.cmd);
			final File pwdFile = resourceService.toLocalFile(pwd);

			context.logger().info("Running '{}' in {}", cmd, pwdFile);
			final Process proc = rt.exec(cmd, /*inherit env*/null, pwdFile);
			try {
				proc.waitFor();
			} catch (InterruptedException e) {
				throw new IOException(e);
			}

			final KeyedMessagesBuilder kmb = new KeyedMessagesBuilder();

			int exitValue = proc.exitValue();
			if (exitValue != 0) {
				final StringBuilder msg = new StringBuilder("Sub Process '" + cmd + "' terminated with non-zero error code");

				try {
					readAllLines(proc, line -> {
						context.logger().error(line);
						msg.append("\n").append("    ").append(line);
					});
				} catch (IOException e) {
					throw new IOException("Error reading process output. Partial result:" + msg.toString(), e);
				}
				if (mustSucceed) {
					throw new IOException(msg.toString());
				} else {
					kmb.addMessage(msg.toString(), Severity.Error);
				}
			} else {				
				readAllLines(proc.getErrorStream(), line -> { kmb.addMessage(line, Severity.Warning, pwd); });
				readAllLines(proc.getInputStream(), line -> { kmb.addMessage(line, Severity.Info, pwd); });
			}

			return kmb.build();

			// TODO: dependency on files
		}

	}
	
	private void readAllLines(Process proc, Consumer<String> consumer) throws IOException {			
		readAllLines(proc.getInputStream(), consumer);	
		readAllLines(proc.getErrorStream(), consumer);
	}
	
	private void readAllLines(InputStream inputStream, Consumer<String> consumer) throws IOException {		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			reader.lines().forEach(consumer);
		}
	}

	private final class NewSolutionCommand extends Command {

		public NewSolutionCommand(String name) {
			super("new sln");
			addOption("name", name);
			addOption("force");
		}

	}

	private final class NewProjectCommand extends Command {

		public NewProjectCommand(String name) {
			super("new classlib");
			addOption("output", name);
			addOption("force");
		}

	}

	private final class AddProjectCommand extends Command {

		public AddProjectCommand(String projectName, String solutionName) {
			super("sln");
			addArgument(solutionName + ".sln");
			addParameter("add", projectName);
		}

	}

	private final class AddDependencyCommand extends Command {

		public AddDependencyCommand(String projectName, String dependencyName) {
			super("add");
			addArgument(projectName);
			addParameter("reference", dependencyName);
		}

	}

	private final class BuildCommand extends Command {

		public BuildCommand() {
			super("build");
		}

	}

}
