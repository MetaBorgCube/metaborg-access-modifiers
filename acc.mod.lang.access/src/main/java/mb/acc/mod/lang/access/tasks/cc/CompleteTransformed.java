package mb.acc.mod.lang.access.tasks.cc;

import javax.inject.Inject;

import mb.accmodlangaccess.AccModLangAccessClassLoaderResources;
import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessAnalyzeFile;
import mb.accmodlangaccess.task.AccModLangAccessCodeCompletionTaskDef;
import mb.accmodlangaccess.task.AccModLangAccessGetStrategoRuntimeProvider;
import mb.accmodlangaccess.task.AccModLangAccessStatixSpecTaskDef;
import mb.log.api.LoggerFactory;
import mb.nabl2.terms.stratego.StrategoTerms;
import mb.tego.strategies.runtime.TegoRuntime;

@AccModLangAccessScope
public class CompleteTransformed extends AccModLangAccessCodeCompletionTaskDef {

	@Inject
	public CompleteTransformed(
			ParseTransformed parseTask, 
			AccModLangAccessAnalyzeFile analyzeFileTask,
			AccModLangAccessGetStrategoRuntimeProvider getStrategoRuntimeProviderTask, 
			TegoRuntime tegoRuntime,
			AccModLangAccessStatixSpecTaskDef statixSpec, 
			StrategoTerms strategoTerms, 
			LoggerFactory loggerFactory,
			AccModLangAccessClassLoaderResources classLoaderResources
	) {
		super(parseTask, analyzeFileTask, getStrategoRuntimeProviderTask, tegoRuntime, statixSpec, strategoTerms, loggerFactory,
				classLoaderResources);
	}
		
	@Override
	public String getId() {
		return CompleteTransformed.class.getSimpleName();
	}

}
