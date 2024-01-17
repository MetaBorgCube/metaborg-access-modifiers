package mb.acc.mod.lang.access.tasks.torust;

import javax.inject.Inject;

import mb.acc.mod.lang.access.tasks.TransformWithAnalysis;
import mb.accmodlangaccess.AccModLangAccessClassLoaderResources;
import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessGetStrategoRuntimeProvider;

@AccModLangAccessScope
public class TransformToRust extends TransformWithAnalysis {

	@Inject
    public TransformToRust(
    		AccModLangAccessGetStrategoRuntimeProvider getStrategoRuntimeProvider,
    		AccModLangAccessClassLoaderResources classLoaderResources
    ) {
        super(getStrategoRuntimeProvider, "transform-to-rust", classLoaderResources);
    }
	
	@Override
	public String getId() {
		return TransformToRust.class.getCanonicalName();
	}

}
