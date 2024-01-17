package mb.acc.mod.lang.access.tasks.tojava;

import javax.inject.Inject;

import mb.acc.mod.lang.access.tasks.TransformWithAnalysis;
import mb.accmodlangaccess.AccModLangAccessClassLoaderResources;
import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessGetStrategoRuntimeProvider;

@AccModLangAccessScope
public class TransformToJava extends TransformWithAnalysis {

	@Inject
    public TransformToJava(
    		AccModLangAccessGetStrategoRuntimeProvider getStrategoRuntimeProvider,
    		AccModLangAccessClassLoaderResources classLoaderResources
    ) {
        super(getStrategoRuntimeProvider, "transform-to-java", classLoaderResources);
    }
	
	@Override
	public String getId() {
		return TransformToJava.class.getCanonicalName();
	}

}
