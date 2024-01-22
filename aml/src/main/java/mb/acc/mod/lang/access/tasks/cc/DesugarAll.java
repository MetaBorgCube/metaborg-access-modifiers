package mb.acc.mod.lang.access.tasks.cc;

import javax.inject.Inject;

import mb.accmodlang.AMLScope;
import mb.accmodlang.task.AMLGetStrategoRuntimeProvider;
import mb.stratego.pie.AstStrategoTransformTaskDef;

@AMLScope
public class DesugarAll extends AstStrategoTransformTaskDef {

	@Inject
	public DesugarAll(AMLGetStrategoRuntimeProvider getStrategoRuntimeProviders) {
		super(getStrategoRuntimeProviders, "desugar-all");
	}

	@Override
	public String getId() {
		return DesugarAll.class.getSimpleName();
	}
}
