package mb.acc.mod.lang.access.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.accmodlang.AMLClassLoaderResources;
import mb.accmodlang.task.AMLAnalyze;
import mb.accmodlang.task.AMLParse;
import mb.common.message.KeyedMessages;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.result.Result;
import mb.constraint.pie.ConstraintAnalyzeTaskDef;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.spoofax.core.language.command.CommandFeedbackBuilder;
import mb.spoofax.core.language.command.ShowFeedback;
import mb.stratego.pie.AstStrategoTransformTaskDef;

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

	protected final AMLClassLoaderResources classLoaderResources;
	protected final AMLParse parse;
	protected final AstStrategoTransformTaskDef insertSettings;
	protected final AMLAnalyze analyze;
	protected final TransformWithAnalysis transform;
	protected final ResourceService resourceService;

	private final String langName;

	protected TestCompat(
			AMLClassLoaderResources classLoaderResources,
			AMLParse parse,
			AstStrategoTransformTaskDef insertSettings,
			AMLAnalyze analyze,
			TransformWithAnalysis transform,
			ResourceService resourceService,
			String langDir
	) {
		this.classLoaderResources = classLoaderResources;
		this.parse = parse;
		this.insertSettings = insertSettings;
		this.analyze = analyze;
		this.transform = transform;
		this.resourceService = resourceService;
		this.langName = langDir;
	}

	@Override
	public CommandFeedback exec(ExecContext context, Args args) throws Exception {
		context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        context.require(classLoaderResources.tryGetAsNativeResource(Args.class), ResourceStampers.hashFile());

        context.cancelToken().throwIfCanceled();
        final ResourceKey file = args.file;

		// 0. Extract analysis result
		final Supplier<Result<ConstraintAnalyzeTaskDef.Output, ?>> analysisSupplier = analyze
				.createSupplier(new ConstraintAnalyzeTaskDef.Input(file,
						insertSettings.createSupplier(parse
								.inputBuilder()
								.withFile(file)
								.buildAstSupplier())));
		final Result<ConstraintAnalyzeTaskDef.Output, ?> analysisTaskResult = context.require(analysisSupplier);
		if(analysisTaskResult.isErr()) {
			return CommandFeedback.ofTryExtractMessagesFrom(analysisTaskResult.getErr(), file);
		}
		final ConstraintAnalyzeTaskDef.Output analysisResult = analysisTaskResult.get();

		// 1. Transform to Java
		final Result<IStrategoTerm, ? extends Exception> transformResult = context.require(transform, analysisSupplier);
		context.cancelToken().throwIfCanceled();

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
				kmb.addMessage(TermUtils.toJavaString(message), Severity.Error, file);
			}
			return CommandFeedback.of(kmb.build());
		}

		// 2. Compile
		final Result<KeyedMessages, IOException> compilerOutput = compile(context, args, transformResultTerm.getSubterm(1));
		context.cancelToken().throwIfCanceled();
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
			cfb.addShowFeedback(ShowFeedback.showText("Error: AML analysis reported errors, but " + langName + " compiler did not.", "Compatibility check Failed"));
			cfb.withKeyedMessages(analysisResult.result.messages.toKeyed(file));
			return cfb.build();
		}
		// source analysis has no errors
		if(!javaCompilerOutputHasErrors) {
			return CommandFeedback.of(ShowFeedback.showText("Both source analysis and target compilation returned no errors", "Compatible"));
		}
		// but java compiler did
		final CommandFeedbackBuilder cfb = new CommandFeedbackBuilder();
		cfb.addShowFeedback(ShowFeedback.showText("Error: AML analysis reported no errors, but " + langName + " compiler did.", "Compatibility check Failed"));
		cfb.withKeyedMessages(compilerOutput.get());
		return cfb.build();
	}
	

	protected abstract Result<KeyedMessages, IOException> compile(ExecContext context, Args args,
			IStrategoTerm transformResultTerm);


	protected HierarchicalResource normalizeRoot(ResourcePath rootDirectory) {
		if(rootDirectory.getQualifier().equals("spt")) {
			// SPT just gives the test case as project resource, so call `getRoot()`
			return resourceService.getHierarchicalResource(ResourceKeyString.of("eclipse-resource", rootDirectory.getIdAsString())).getRoot();
		}
		return resourceService.getHierarchicalResource(rootDirectory);
	}

	protected String normalizeFile(ResourceKey file, HierarchicalResource root) {
		final ResourcePath baseResource;
		if(file.getQualifier().equals("spt")) {
			final String[] components = file.getIdAsString().split("!!");
			final ResourceKeyString baseResourcePath = ResourceKeyString.of(root.getPath().getQualifier(), components[0]);
			if(components.length == 1) {
				// no specific test case
				baseResource = resourceService.getResourcePath(baseResourcePath);
			} else {
				baseResource = resourceService.getResourcePath(baseResourcePath).appendAsRelativePath(components[1]);
			}
		} else {
			final ResourceKeyString baseResourcePath = ResourceKeyString.of(root.getPath().getQualifier(), file.getIdAsString());
			baseResource = resourceService.getResourcePath(baseResourcePath);
		}
		final String normalizedFile;
		if(baseResource.isAbsolute()) {
			normalizedFile = root.getPath().relativize(baseResource);
		} else {
			normalizedFile = root.getPath().appendRelativePath(baseResource).asString();
		}
		return normalizedFile
				.replace("|", "_")
				.replace(":", "_")
				.replace(";", "_")
				.replace("/", "_")
				.replace("\\", "_")
				.replace(" ", "_");
	}

	protected HierarchicalResource buildRoot(HierarchicalResource project) {
		return project
			.appendAsRelativePath(relativeTargetDirRoot)
			.appendAsRelativePath(langName);
	}


	protected abstract class Command {

		private List<String> cmd = new ArrayList<String>();

		private List<String> requires = new ArrayList<String>();
		private List<String> provides = new ArrayList<String>();

		protected Command(String command) {
			cmd.add(command);
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

		protected void require(String file) {
			requires.add(file);
		}

		protected void provide(String file) {
			provides.add(file);
		}

		public void run(ExecContext context, ResourcePath workingDirectory) throws IOException {
			run(context, workingDirectory, true);
		}

		public KeyedMessages run(ExecContext context, ResourcePath workingDirectory, boolean mustSucceed) throws IOException {
			final Runtime rt = Runtime.getRuntime();
//			final String cmd = String.join(" ", this.cmd);

			context.require(workingDirectory);
			for(String file: requires) {
				context.require(workingDirectory.appendAsRelativePath(file));
			}
			final File pwdFile = resourceService.toLocalFile(workingDirectory);

			final Map<String, String> env = new HashMap<>(System.getenv());
			env.compute("PATH", (v, path) -> { return  path + ":/usr/local/share/dotnet/"; });
			context.logger().error("Running '{}' in {}. PATH={}", cmd, pwdFile, System.getenv("PATH"));
			final Process proc = rt.exec(
					String.join(" ", cmd), 
					/*inherit env: null*/
					env.entrySet().stream().map(e -> String.format("%s=%s", e.getKey(), e.getValue())).toArray(String[]::new),
					pwdFile
			);
			try {
				proc.waitFor(10, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				context.logger().error("command time-out", e);
				throw new IOException(e);
			}

			// Executing a process can take a while, so check for cancellation.
			context.cancelToken().throwIfCanceled();

			final KeyedMessagesBuilder kmb = new KeyedMessagesBuilder();

			int exitValue = proc.exitValue();
			if (exitValue != 0) {
				final StringBuilder msg = new StringBuilder("Sub Process '" + cmd + "' terminated with non-zero error code");

				try {
					readAllLines(proc, line -> {
						if(mustSucceed) {
							context.logger().error(line);
						} else {
							context.logger().debug(line);
						}
						msg.append("\n").append("    ").append(line);
					});
				} catch (IOException e) {
					context.logger().error("Error reading process output", e);
					throw new IOException("Error reading process output. Partial result: " + msg.toString(), e);
				}
				if (mustSucceed) {
					throw new IOException(msg.toString());
				} else {
					kmb.addMessage(msg.toString(), Severity.Error);
				}
			} else {
				readAllLines(proc.getErrorStream(), line -> { kmb.addMessage(line, Severity.Warning, workingDirectory); });
				readAllLines(proc.getInputStream(), line -> { kmb.addMessage(line, Severity.Info, workingDirectory); });
			}

			for(String file: provides) {
				context.provide(resourceService.getReadableResource(workingDirectory.appendAsRelativePath(file)));
			}

			return kmb.build();
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

	}

}
