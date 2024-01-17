package mb.acc.mod.lang.access.tasks.cc;

import javax.inject.Inject;

import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessGetStrategoRuntimeProvider;
import mb.stratego.pie.AstStrategoTransformTaskDef;

@AccModLangAccessScope
public class DesugarAll extends AstStrategoTransformTaskDef {

	@Inject
	public DesugarAll(AccModLangAccessGetStrategoRuntimeProvider getStrategoRuntimeProviders) {
		super(getStrategoRuntimeProviders, "desugar-all");
	}

	@Override
	public String getId() {
		return DesugarAll.class.getSimpleName();
	}
}
