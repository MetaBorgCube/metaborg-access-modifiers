package mb.acc.mod.lang.access.tasks.cc;

import javax.inject.Inject;

import mb.accmodlang.AMLScope;
import mb.accmodlang.task.AMLGetStrategoRuntimeProvider;
import mb.stratego.pie.AstStrategoTransformTaskDef;

@AMLScope
public class TailToPlaceHolder extends AstStrategoTransformTaskDef {

	@Inject
	public TailToPlaceHolder(AMLGetStrategoRuntimeProvider getStrategoRuntimeProviders) {
		super(getStrategoRuntimeProviders, "tail-to-placeholder");
	}

	@Override
	public String getId() {
		return TailToPlaceHolder.class.getSimpleName();
	}

}
