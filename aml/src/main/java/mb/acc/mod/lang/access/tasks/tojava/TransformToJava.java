package mb.acc.mod.lang.access.tasks.tojava;

import javax.inject.Inject;

import mb.acc.mod.lang.access.tasks.TransformWithAnalysis;
import mb.accmodlang.AMLClassLoaderResources;
import mb.accmodlang.AMLScope;
import mb.accmodlang.task.AMLGetStrategoRuntimeProvider;

@AMLScope
public class TransformToJava extends TransformWithAnalysis {

	@Inject
    public TransformToJava(
    		AMLGetStrategoRuntimeProvider getStrategoRuntimeProvider,
    		AMLClassLoaderResources classLoaderResources
    ) {
        super(getStrategoRuntimeProvider, "transform-to-java", classLoaderResources);
    }
	
	@Override
	public String getId() {
		return TransformToJava.class.getCanonicalName();
	}

}
