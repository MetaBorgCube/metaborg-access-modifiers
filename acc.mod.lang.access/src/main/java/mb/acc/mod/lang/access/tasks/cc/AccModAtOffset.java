package mb.acc.mod.lang.access.tasks.cc;

import javax.inject.Inject;

import mb.accmodlang.AMLScope;
import mb.accmodlang.task.AMLGetStrategoRuntimeProvider;
import mb.stratego.pie.AstStrategoTransformTaskDef;

@AMLScope
public class AccModAtOffset extends AstStrategoTransformTaskDef {

	@Inject
	public AccModAtOffset(AMLGetStrategoRuntimeProvider getStrategoRuntimeProviders) {
		super(getStrategoRuntimeProviders, "accmod-at-offset", "strip-annos");
	}

	@Override
	public String getId() {
		return AccModAtOffset.class.getSimpleName();
	}
}
