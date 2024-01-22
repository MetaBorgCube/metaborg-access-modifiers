package mb.acc.mod.lang.access.tasks.cc;

import javax.inject.Inject;

import org.checkerframework.checker.nullness.qual.Nullable;

import mb.accmodlang.AMLClassLoaderResources;
import mb.accmodlang.AMLScope;
import mb.accmodlang.task.AMLAnalyzeFile;
import mb.accmodlang.task.AMLGetStrategoRuntimeProvider;
import mb.accmodlang.task.AMLParse;
import mb.accmodlang.task.AMLStatixSpecTaskDef;
import mb.common.codecompletion.CodeCompletionResult;
import mb.common.result.Result;
import mb.log.api.LoggerFactory;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.pie.api.ExecContext;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.statix.codecompletion.pie.CodeCompletionTaskDef;
import mb.tego.strategies.runtime.TegoRuntime;

@AMLScope
public class CompleteTransformed extends CompleteTransformedBase {
	
	private final mb.accmodlang.AMLClassLoaderResources classLoaderResources;


	@Inject
	public CompleteTransformed(
			AMLParse parse, 
			AccModToPlaceHolder toPlaceHolder,
			AMLAnalyzeFile analyzeFileTask,
			AMLGetStrategoRuntimeProvider getStrategoRuntimeProviderTask,
			PrependOffset prependOffset, 
			TegoRuntime tegoRuntime,
			AMLStatixSpecTaskDef statixSpec, 
			StrategoTerms strategoTerms, 
			LoggerFactory loggerFactory,
			AMLClassLoaderResources classLoaderResources
	) {
		super(analyzeFileTask, 
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
    public @Nullable Result<CodeCompletionResult, ?> exec(ExecContext context, CompleteTransformedBase.Input input) throws Exception {
        context.require(classLoaderResources.tryGetAsNativeResource(getClass()), ResourceStampers.hashFile());
        context.require(classLoaderResources.tryGetAsNativeResource(CodeCompletionTaskDef.Input.class), ResourceStampers.hashFile());

        return super.exec(context, input);
    }

	@Override
	public String getId() {
		return CompleteTransformed.class.getSimpleName();
	}

}
