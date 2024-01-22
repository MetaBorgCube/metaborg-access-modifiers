package mb.acc.mod.lang.access.tasks.torust;

import javax.inject.Inject;

import mb.acc.mod.lang.access.tasks.TransformWithAnalysis;
import mb.accmodlang.AMLClassLoaderResources;
import mb.accmodlang.AMLScope;
import mb.accmodlang.task.AMLGetStrategoRuntimeProvider;

@AMLScope
public class TransformToRust extends TransformWithAnalysis {

	@Inject
    public TransformToRust(
    		AMLGetStrategoRuntimeProvider getStrategoRuntimeProvider,
    		AMLClassLoaderResources classLoaderResources
    ) {
        super(getStrategoRuntimeProvider, "transform-to-rust", classLoaderResources);
    }
	
	@Override
	public String getId() {
		return TransformToRust.class.getCanonicalName();
	}

}
