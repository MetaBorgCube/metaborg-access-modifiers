package mb.acc.mod.lang.access.tasks.tocsharp;

import javax.inject.Inject;

import mb.acc.mod.lang.access.tasks.TransformWithAnalysis;
import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessGetStrategoRuntimeProvider;

@AccModLangAccessScope
public class TransformToCSharp extends TransformWithAnalysis {

	@Inject
    public TransformToCSharp(AccModLangAccessGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "transform-to-csharp");
    }
	
	@Override
	public String getId() {
		return TransformToCSharp.class.getCanonicalName();
	}

}
