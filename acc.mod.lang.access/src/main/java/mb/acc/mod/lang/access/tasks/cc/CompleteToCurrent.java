package mb.acc.mod.lang.access.tasks.cc;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.inject.Inject;

import mb.accmodlangaccess.AccModLangAccessScope;
import mb.common.codecompletion.CodeCompletionResult;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.statix.codecompletion.pie.CodeCompletionTaskDef;

@AccModLangAccessScope
public class CompleteToCurrent implements TaskDef<CompleteToCurrent.Args, CommandFeedback> {

	public static final class Args implements Serializable {

		private static final long serialVersionUID = 42L;
		
		private final Region selection;
		
		private final ResourceKey file;
		
		private final @Nullable ResourcePath rootDirectoryHint;
		
		public Args(Region selection, ResourceKey file, ResourcePath rootDirectoryHint) {
			this.selection = selection;
			this.file = file;
			this.rootDirectoryHint = rootDirectoryHint;			
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((selection == null) ? 0 : selection.hashCode());
			result = prime * result + ((file == null) ? 0 : file.hashCode());
			result = prime * result + ((rootDirectoryHint == null) ? 0 : rootDirectoryHint.hashCode());
			return result;
		}
		
		public CodeCompletionTaskDef.Input toCodeCompletionInput() {
			return new CodeCompletionTaskDef.Input(selection, file, rootDirectoryHint, true);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Args other = (Args) obj;
			return Objects.equals(selection, other.selection)
					&& Objects.equals(file, other.file)
					&& Objects.equals(rootDirectoryHint, other.rootDirectoryHint);
		}

		@Override
		public String toString() {
			return "Args [selection=" + selection + ", file=" + file + ", rootDirectoryHint=" + rootDirectoryHint + "]";
		}
		
	}
	
	private final CompleteTransformed complete;
	
	@Inject
	public CompleteToCurrent(
			CompleteTransformed complete
	) {
		this.complete = complete;
	}

	@Override
	public CommandFeedback exec(ExecContext context, Args args) throws Exception {
		ParseTransformed.offsetHolder.set(args.selection.getStartOffset());		
		Result<CodeCompletionResult, ?> result = context.require(complete, args.toCodeCompletionInput());
		context.logger().debug("cc-result: ", result);
		
		return CommandFeedback.of();		
	}

	@Override
	public String getId() {
		return CompleteToCurrent.class.getSimpleName();
	}

}
