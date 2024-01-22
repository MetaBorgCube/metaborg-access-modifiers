package mb.acc.mod.lang.access.tasks.tocsharp;

import javax.inject.Inject;

import mb.acc.mod.lang.access.tasks.TransformWithAnalysis;
import mb.accmodlang.AMLClassLoaderResources;
import mb.accmodlang.AMLScope;
import mb.accmodlang.task.AMLGetStrategoRuntimeProvider;

@AMLScope
public class TransformToCSharp extends TransformWithAnalysis {

	@Inject
    public TransformToCSharp(AMLGetStrategoRuntimeProvider getStrategoRuntimeProvider,
    		AMLClassLoaderResources classLoaderResources) {
        super(getStrategoRuntimeProvider, "transform-to-csharp", classLoaderResources);
    }
	
	@Override
	public String getId() {
		return TransformToCSharp.class.getCanonicalName();
	}

}
