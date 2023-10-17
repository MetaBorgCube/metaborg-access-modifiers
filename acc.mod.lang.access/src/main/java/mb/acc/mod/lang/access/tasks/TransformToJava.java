package mb.acc.mod.lang.access.tasks;

import javax.inject.Inject;

import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessGetStrategoRuntimeProvider;

@AccModLangAccessScope
public class TransformToJava extends TransformWithAnalysis {

	@Inject
    public TransformToJava(AccModLangAccessGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, "transform-to-java");
    }
	
	@Override
	public String getId() {
		return TransformToJava.class.getCanonicalName();
	}

}
