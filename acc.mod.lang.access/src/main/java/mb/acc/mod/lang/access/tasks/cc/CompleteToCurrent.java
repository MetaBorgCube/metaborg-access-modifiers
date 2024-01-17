package mb.acc.mod.lang.access.tasks.cc;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoAppl;

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
import mb.jsglr.common.JsglrParseException;
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
	
	public static final boolean EXPAND_DETERMINISTIC = true;

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
	private final AccModToPlaceHolder accModToPlaceHolder;
	private final TailToPlaceHolder tailToPlaceHolder;
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
			AccModToPlaceHolder accModToPlaceHolder,
			TailToPlaceHolder tailToPlaceHolder,
			AccModLangAccessAnalyzeFile analyze,
			AnalysisHasErrors analysisHasErrors,
			AccModAtOffset atOffset,
			mb.accmodlangaccess.AccModLangAccessClassLoaderResources classLoaderResources
	) {
		this.complete = complete;
		this.parse = parse;
		this.prependOffset = prependOffset;
		this.desugarAll = desugarAll;
		this.accModToPlaceHolder = accModToPlaceHolder;
		this.tailToPlaceHolder = tailToPlaceHolder;
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
        final Region region = args.selection;
        
        // Create some reusable suppliers
        final Supplier<Result<IStrategoTerm, JsglrParseException>> astSupplier = parse.inputBuilder()
        		.withFile(args.file)
        		.rootDirectoryHintNullable(args.rootDirectoryHint)
        		.buildRecoverableAstSupplier();
        
        final Supplier<Result<IStrategoTerm, ?>> astOffsetSupplier = prependOffset.createSupplier(
        		astSupplier.map((ctx, parseResult) -> parseResult.map(ast -> new PrependOffset.Args(region, ast))));    
        
        final Supplier<Result<IStrategoTerm, ?>> actualModifierSupplier = atOffset.createSupplier(astOffsetSupplier);
        final Supplier<Result<IStrategoTerm, ?>> accModPlaceHolderSupplier = accModToPlaceHolder.createSupplier(astOffsetSupplier);
        final Supplier<Result<IStrategoTerm, ?>> tailPlaceHolderSupplier = tailToPlaceHolder.createSupplier(astOffsetSupplier);
        

        // Fetch actual modifier
        final Result<IStrategoTerm, ?> actualModifierResult = context.require(desugarAll, actualModifierSupplier);
        if (actualModifierResult.isErr()) return CommandFeedback.of(actualModifierResult.unwrapErr());
        final IStrategoAppl currentModifier = (IStrategoAppl) actualModifierResult.unwrapUnchecked();
        
        // Fetch analysis result
        final Supplier<Result<ConstraintAnalyzeFile.Output, ?>> analysisResultSupplier = analyze.createSupplier(new ConstraintAnalyzeFile.Input(args.rootDirectoryHint, args.file));
        final boolean analysisSucceeded = context.require(analysisHasErrors, analysisResultSupplier).isErr();
        
        // Access Modifier Completion result
        final CompleteTransformedBase.Input amccInput = 
				new CompleteTransformedBase.Input(accModPlaceHolderSupplier, args.file, args.selection, args.rootDirectoryHint, EXPAND_DETERMINISTIC);
		final Result<CodeCompletionResult, ?> amccResult = context.require(complete, amccInput);
        if (amccResult.isErr()) return CommandFeedback.of(amccResult.unwrapErr());
        final CodeCompletionResult accModCCResult = amccResult.unwrapUnchecked();
        
        if(currentModifier.getSubtermCount() == 0) {        	
    		if(analysisSucceeded) {
        		return checkProposeCurrent(args, accModCCResult, currentModifier);
        	} else {
        		// Analysis failed: completion should not propose current modifier
        		return checkNotProposeCurrent(args, accModCCResult, currentModifier);
        	}
        } else {
        	final boolean containsCurrentModifierTemplate = 
        			containsCurrentModifierTemplate(accModCCResult, currentModifier);
        	
        	if(!containsCurrentModifierTemplate && !analysisSucceeded) {
        		// Not even suggesting current modifier, so we are fine
        		return success();
        	} else if (!containsCurrentModifierTemplate && analysisSucceeded) {
        		// Analysis succeeded, but current modifier was not even suggested
        		return error("Analysis succeeded, but " + currentModifier.getName() + " was not suggested", args);
        	}

            // Tail completion result
            final CompleteTransformedBase.Input tailccInput = 
    				new CompleteTransformedBase.Input(tailPlaceHolderSupplier, args.file, args.selection, args.rootDirectoryHint, EXPAND_DETERMINISTIC);
    		final Result<CodeCompletionResult, ?> tailccResult = context.require(complete, tailccInput);
            if (tailccResult.isErr()) return CommandFeedback.of(tailccResult.unwrapErr());
            final CodeCompletionResult tailCompletionResult = tailccResult.unwrapUnchecked();

            final boolean containsNil = containsNil(tailCompletionResult);
            
            if(containsNil && !analysisSucceeded) {
            	return error("Despite failing analysis, current modifier was suggested", args);
            } else if(containsNil && !analysisSucceeded) {
            	return error("Despite succeeding analysis, current modifier was not suggested", args);
            }
            
            return success();            
        }
	}
	
	private boolean containsCurrentModifierTemplate(CodeCompletionResult accModCCResult,
			IStrategoAppl currentModifier) {
		return accModCCResult.getProposals().stream().anyMatch(proposal -> {
        	final StrategoTermCodeCompletionItem termProposal = (StrategoTermCodeCompletionItem) proposal;
        	final IStrategoAppl term = (IStrategoAppl) termProposal.getStrategoTerm();
			return term.getConstructor().getName().equals(currentModifier.getConstructor().getName());
		});
	}
	
	private boolean containsNil(CodeCompletionResult accModCCResult) {
		return accModCCResult.getProposals().stream().anyMatch(proposal -> {
        	final StrategoTermCodeCompletionItem termProposal = (StrategoTermCodeCompletionItem) proposal;
        	final IStrategoAppl term = (IStrategoAppl) termProposal.getStrategoTerm();
			return term.getConstructor().getName().equals("MRLNil");
		});
	}

	private CommandFeedback checkProposeCurrent(
		Args args,
		CodeCompletionResult ccResult,
		IStrategoAppl currentModifier		
	) {
        for(CodeCompletionItem proposal: ccResult.getProposals()) {
        	final StrategoTermCodeCompletionItem termProposal = (StrategoTermCodeCompletionItem) proposal;
        	final IStrategoAppl term = (IStrategoAppl) termProposal.getStrategoTerm();
        	if(stripAnnos(term).equals(currentModifier)) {
        		return success();
        	}
        }
        
        return error("No matching proposal found", args);
	}
	

	private CommandFeedback checkNotProposeCurrent(
		Args args,
		CodeCompletionResult ccResult,
		IStrategoAppl currentModifier		
	) {        
		for(CodeCompletionItem proposal: ccResult.getProposals()) {
        	final StrategoTermCodeCompletionItem termProposal = (StrategoTermCodeCompletionItem) proposal;
        	final IStrategoAppl term = (IStrategoAppl) termProposal.getStrategoTerm();
        	if(stripAnnos(term).equals(currentModifier)) {
                return error("Failing proposal should not be given", args);
        	}
        }
		
		return success();
	}
	
	private IStrategoAppl stripAnnos(IStrategoAppl term) {
    	// Term factory only caches constructors, therefore discarding the annotations without term factory is safe.
    	return new StrategoAppl(term.getConstructor(), term.getAllSubterms(), null);
	}
	
	private CommandFeedback success() {
		return CommandFeedback.of();
	}
 	
	private CommandFeedback error(String message, Args args) {
		return CommandFeedback.of(new KeyedMessagesBuilder()
				.addMessage(message, Severity.Error, args.file, args.selection)
				.build());
	}

	@Override
	public String getId() {
		return CompleteToCurrent.class.getSimpleName();
	}

}
