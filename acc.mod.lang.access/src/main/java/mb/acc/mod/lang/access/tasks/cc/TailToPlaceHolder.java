package mb.acc.mod.lang.access.tasks.cc;

import javax.inject.Inject;

import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessGetStrategoRuntimeProvider;
import mb.stratego.pie.AstStrategoTransformTaskDef;

@AccModLangAccessScope
public class TailToPlaceHolder extends AstStrategoTransformTaskDef {

	@Inject
	public TailToPlaceHolder(AccModLangAccessGetStrategoRuntimeProvider getStrategoRuntimeProviders) {
		super(getStrategoRuntimeProviders, "tail-to-placeholder");
	}

	@Override
	public String getId() {
		return TailToPlaceHolder.class.getSimpleName();
	}

}
