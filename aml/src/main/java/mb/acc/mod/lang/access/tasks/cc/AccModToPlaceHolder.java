package mb.acc.mod.lang.access.tasks.cc;

import javax.inject.Inject;

import mb.accmodlang.AMLScope;
import mb.accmodlang.task.AMLGetStrategoRuntimeProvider;
import mb.stratego.pie.AstStrategoTransformTaskDef;

@AMLScope
public class AccModToPlaceHolder extends AstStrategoTransformTaskDef {

	@Inject
	public AccModToPlaceHolder(AMLGetStrategoRuntimeProvider getStrategoRuntimeProviders) {
		super(getStrategoRuntimeProviders, "accmod-to-placeholder");
	}

	@Override
	public String getId() {
		return AccModToPlaceHolder.class.getSimpleName();
	}
	
	

}
