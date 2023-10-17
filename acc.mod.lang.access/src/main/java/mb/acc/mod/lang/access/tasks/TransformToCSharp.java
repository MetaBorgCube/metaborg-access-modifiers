package mb.acc.mod.lang.access.tasks;

import javax.inject.Inject;

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
