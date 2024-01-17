package mb.acc.mod.lang.access.tasks.tocsharp;

import javax.inject.Inject;

import mb.acc.mod.lang.access.tasks.TransformWithAnalysis;
import mb.accmodlangaccess.AccModLangAccessClassLoaderResources;
import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessGetStrategoRuntimeProvider;

@AccModLangAccessScope
public class TransformToCSharp extends TransformWithAnalysis {

	@Inject
    public TransformToCSharp(AccModLangAccessGetStrategoRuntimeProvider getStrategoRuntimeProvider,
    		AccModLangAccessClassLoaderResources classLoaderResources) {
        super(getStrategoRuntimeProvider, "transform-to-csharp", classLoaderResources);
    }
	
	@Override
	public String getId() {
		return TransformToCSharp.class.getCanonicalName();
	}

}
