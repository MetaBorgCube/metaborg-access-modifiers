package mb.acc.mod.lang.access.tasks.cc;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import mb.accmodlangaccess.AccModLangAccessClassLoaderResources;
import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessAnalyzeFile;
import mb.accmodlangaccess.task.AccModLangAccessCodeCompletionTaskDef;
import mb.accmodlangaccess.task.AccModLangAccessGetStrategoRuntimeProvider;
import mb.accmodlangaccess.task.AccModLangAccessParse;
import mb.accmodlangaccess.task.AccModLangAccessStatixSpecTaskDef;
import mb.common.codecompletion.CodeCompletionResult;
import mb.common.result.Result;
import mb.jsglr.common.JsglrParseException;
import mb.log.api.LoggerFactory;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.pie.api.ExecContext;
import mb.pie.api.Function;
import mb.pie.api.Supplier;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.statix.codecompletion.pie.CodeCompletionTaskDef;
import mb.stratego.common.StrategoRuntime;
import mb.tego.strategies.runtime.TegoRuntime;

@AccModLangAccessScope
public class CompleteTransformed extends CompleteTransformedBase {
	
	private final mb.accmodlangaccess.AccModLangAccessClassLoaderResources classLoaderResources;


	@Inject
	public CompleteTransformed(
			AccModLangAccessParse parse, 
			AccModToPlaceHolder toPlaceHolder,
			Provider<StrategoRuntime> strategoRuntimeProvider,
			AccModLangAccessAnalyzeFile analyzeFileTask,
			AccModLangAccessGetStrategoRuntimeProvider getStrategoRuntimeProviderTask, 
			TegoRuntime tegoRuntime,
			AccModLangAccessStatixSpecTaskDef statixSpec, 
			StrategoTerms strategoTerms, 
			LoggerFactory loggerFactory,
			AccModLangAccessClassLoaderResources classLoaderResources
	) {
		super(transformParseResult(parse, toPlaceHolder, strategoRuntimeProvider), 
				analyzeFileTask, 
				getStrategoRuntimeProviderTask, 
				tegoRuntime, 
				statixSpec, 
				strategoTerms, 
				loggerFactory, 
	            () -> null,

	            "pre-analyze",
	            "post-analyze",
	            "upgrade-placeholders",
	            "downgrade-placeholders",
	            "is-inj",
	            "pp-partial",

	            "main",
	            "programOk"
		);
		
		this.classLoaderResources = classLoaderResources;
	}


    @Override
    public @Nullable Result<CodeCompletionResult, ?> exec(ExecContext context, CodeCompletionTaskDef.Input input) throws Exception {
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        context.require(classLoaderResources.tryGetAsNativeResource(CodeCompletionTaskDef.Input.class), ResourceStampers.hashFile());

        return super.exec(context, input);
    }
		
	private static Function<CodeCompletionTaskDef.Input, Result<IStrategoTerm, ?>> transformParseResult(AccModLangAccessParse parse,
			AccModToPlaceHolder toPlaceHolder, Provider<StrategoRuntime> strategoRuntimeProvider) {
		return (ExecContext context, CodeCompletionTaskDef.Input input) -> {
			int offset = input.primarySelection.getStartOffset();
			final Supplier<Result<IStrategoTerm, JsglrParseException>> parseResultSupplier = parse.inputBuilder()
				.withFile(input.file)
	            .rootDirectoryHint(Optional.ofNullable(input.rootDirectoryHint))
	            .buildRecoverableAstSupplier();
			
			final Supplier<Result<IStrategoTerm, JsglrParseException>> transformInputSupplier = parseResultSupplier.map((ctx, parseResult) -> {
				return parseResult.map(ast -> {
					final ITermFactory termFactory = strategoRuntimeProvider.get().getTermFactory();
					final IStrategoTerm ast_offset = termFactory.makeTuple(termFactory.makeInt(offset), ast);	
					return ast_offset;
				});
			});
			
			return context.require(toPlaceHolder, transformInputSupplier);
		};
	}

	@Override
	public String getId() {
		return CompleteTransformed.class.getSimpleName();
	}

}
