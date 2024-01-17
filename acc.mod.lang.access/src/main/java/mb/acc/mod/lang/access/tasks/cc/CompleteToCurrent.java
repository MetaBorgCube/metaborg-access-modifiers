package mb.acc.mod.lang.access.tasks.cc;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessAnalyzeFile;
import mb.accmodlangaccess.task.AccModLangAccessParse;
import mb.common.codecompletion.CodeCompletionItem;
import mb.common.codecompletion.CodeCompletionResult;
import mb.common.message.KeyedMessagesBuilder;
import mb.common.message.Severity;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.constraint.pie.ConstraintAnalyzeFile;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.spoofax.core.language.command.CommandFeedback;
import mb.statix.codecompletion.StrategoTermCodeCompletionItem;
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
	private final AccModLangAccessParse parse;
	private final PrependOffset prependOffset;
	private final DesugarAll desugarAll;
	private final AccModLangAccessAnalyzeFile analyze;
	private final AnalysisHasErrors analysisHasErrors;
	private final AccModAtOffset atOffset;
	private final mb.accmodlangaccess.AccModLangAccessClassLoaderResources classLoaderResources;

	
	@Inject
	public CompleteToCurrent(
			CompleteTransformed complete,
			AccModLangAccessParse parse,
			PrependOffset prependOffset,
			DesugarAll desugarAll,
			AccModLangAccessAnalyzeFile analyze,
			AnalysisHasErrors analysisHasErrors,
			AccModAtOffset atOffset,
			mb.accmodlangaccess.AccModLangAccessClassLoaderResources classLoaderResources
	) {
		this.complete = complete;
		this.parse = parse;
		this.prependOffset = prependOffset;
		this.desugarAll = desugarAll;
		this.analyze = analyze;
		this.analysisHasErrors = analysisHasErrors;
		this.atOffset = atOffset;
		this.classLoaderResources = classLoaderResources;
	}

	@Override
	public CommandFeedback exec(ExecContext context, Args args) throws Exception {
		context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        context.require(classLoaderResources.tryGetAsNativeResource(Args.class), ResourceStampers.hashFile());
        
        // Create local variables to prevent unnecessary capture 
        // (if doesn't work, create task that prepends offset)
        final int offset = args.selection.getStartOffset();
        
        // Fetch actual modifier
        final Supplier<Result<IStrategoTerm, ?>> actualModifierSupplier = atOffset.createSupplier(
        		prependOffset.createSupplier(
	        		parse.inputBuilder()
		        		.withFile(args.file)
		        		.rootDirectoryHintNullable(args.rootDirectoryHint)
		        		.buildRecoverableAstSupplier()
		        		.map((ctx, parseResult) -> parseResult.map(ast -> new PrependOffset.Args(offset, ast)))
        		));
        final Result<IStrategoTerm, ?> actualModifierResult = context.require(desugarAll, actualModifierSupplier);
        if (actualModifierResult.isErr()) return CommandFeedback.of(actualModifierResult.unwrapErr());
        final IStrategoAppl actualModifier = (IStrategoAppl) actualModifierResult.unwrapUnchecked();
        
        // Fetch analysis result
        final Supplier<Result<ConstraintAnalyzeFile.Output, ?>> analysisResultSupplier = analyze.createSupplier(new ConstraintAnalyzeFile.Input(args.rootDirectoryHint, args.file));
        final boolean analysisFailed = context.require(analysisHasErrors, analysisResultSupplier)
        		.mapOrElse(term -> true, err -> false);
        
		final Result<CodeCompletionResult, ?> ccResult = context.require(complete, args.toCodeCompletionInput());
        if (ccResult.isErr()) return CommandFeedback.of(ccResult.unwrapErr());
        final CodeCompletionResult codeCompletionResult = ccResult.unwrapUnchecked();
        
        
        if(!analysisFailed) {
            // Assume no errors in analysis
            for(CodeCompletionItem proposal: codeCompletionResult.getProposals()) {
            	final StrategoTermCodeCompletionItem termProposal = (StrategoTermCodeCompletionItem) proposal;
            	final IStrategoAppl term = (IStrategoAppl) termProposal.getStrategoTerm();
            	if(term.getConstructor().getName().equals(actualModifier.getConstructor().getName())) {
            		return CommandFeedback.of();
            	}
            }
            
    		return CommandFeedback.of(new KeyedMessagesBuilder()
    				.addMessage("No matching proposal found", Severity.Error, args.file, args.selection)
    				.build()
    		);
        } else if(actualModifier.getSubtermCount() == 0) {
            // Assume no errors in analysis
            for(CodeCompletionItem proposal: codeCompletionResult.getProposals()) {
            	final StrategoTermCodeCompletionItem termProposal = (StrategoTermCodeCompletionItem) proposal;
            	final IStrategoAppl term = (IStrategoAppl) termProposal.getStrategoTerm();
            	if(term.getConstructor().getName().equals(actualModifier.getConstructor().getName())) {                    
            		return CommandFeedback.of(new KeyedMessagesBuilder()
            				.addMessage("Failing proposal should not be given", Severity.Error, args.file, args.selection)
            				.build());
            	}
            }
    		return CommandFeedback.of();
        } else {
    		return CommandFeedback.of(new KeyedMessagesBuilder()
    				.addMessage("Argument comparison net yet implemented", Severity.Warning, args.file, args.selection)
    				.build());
        }
	}

	@Override
	public String getId() {
		return CompleteToCurrent.class.getSimpleName();
	}

}
