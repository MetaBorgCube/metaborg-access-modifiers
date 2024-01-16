package mb.acc.mod.lang.access.tasks.cc;

import javax.inject.Inject;

import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessGetStrategoRuntimeProvider;
import mb.stratego.pie.AstStrategoTransformTaskDef;

@AccModLangAccessScope
public class AccModToPlaceHolder extends AstStrategoTransformTaskDef {

	@Inject
	public AccModToPlaceHolder(AccModLangAccessGetStrategoRuntimeProvider getStrategoRuntimeProviders) {
		super(getStrategoRuntimeProviders, "accmod-to-placeholder");
	}

	@Override
	public String getId() {
		return AccModToPlaceHolder.class.getSimpleName();
	}
	
	

}
