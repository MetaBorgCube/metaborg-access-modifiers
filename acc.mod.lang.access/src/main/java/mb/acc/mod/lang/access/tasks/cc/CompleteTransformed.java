package mb.acc.mod.lang.access.tasks.cc;

import javax.inject.Inject;

import org.checkerframework.checker.nullness.qual.Nullable;

import mb.accmodlangaccess.AccModLangAccessClassLoaderResources;
import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessAnalyzeFile;
import mb.accmodlangaccess.task.AccModLangAccessGetStrategoRuntimeProvider;
import mb.accmodlangaccess.task.AccModLangAccessParse;
import mb.accmodlangaccess.task.AccModLangAccessStatixSpecTaskDef;
import mb.common.codecompletion.CodeCompletionResult;
import mb.common.result.Result;
import mb.log.api.LoggerFactory;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.pie.api.ExecContext;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.statix.codecompletion.pie.CodeCompletionTaskDef;
import mb.tego.strategies.runtime.TegoRuntime;

@AccModLangAccessScope
public class CompleteTransformed extends CompleteTransformedBase {
	
	private final mb.accmodlangaccess.AccModLangAccessClassLoaderResources classLoaderResources;


	@Inject
	public CompleteTransformed(
			AccModLangAccessParse parse, 
			AccModToPlaceHolder toPlaceHolder,
			AccModLangAccessAnalyzeFile analyzeFileTask,
			AccModLangAccessGetStrategoRuntimeProvider getStrategoRuntimeProviderTask,
			PrependOffset prependOffset, 
			TegoRuntime tegoRuntime,
			AccModLangAccessStatixSpecTaskDef statixSpec, 
			StrategoTerms strategoTerms, 
			LoggerFactory loggerFactory,
			AccModLangAccessClassLoaderResources classLoaderResources
	) {
		super(CodeCompletionUtils.transformParseResult(parse, toPlaceHolder, prependOffset), 
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

	@Override
	public String getId() {
		return CompleteTransformed.class.getSimpleName();
	}

}
