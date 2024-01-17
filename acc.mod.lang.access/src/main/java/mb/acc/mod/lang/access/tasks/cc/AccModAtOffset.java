package mb.acc.mod.lang.access.tasks.cc;

import javax.inject.Inject;

import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessGetStrategoRuntimeProvider;
import mb.stratego.pie.AstStrategoTransformTaskDef;

@AccModLangAccessScope
public class AccModAtOffset extends AstStrategoTransformTaskDef {

	@Inject
	public AccModAtOffset(AccModLangAccessGetStrategoRuntimeProvider getStrategoRuntimeProviders) {
		super(getStrategoRuntimeProviders, "accmod-at-offset");
	}

	@Override
	public String getId() {
		return AccModAtOffset.class.getSimpleName();
	}
}
