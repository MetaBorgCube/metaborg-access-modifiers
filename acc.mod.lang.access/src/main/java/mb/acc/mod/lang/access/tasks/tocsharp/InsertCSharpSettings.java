package mb.acc.mod.lang.access.tasks.tocsharp;

import javax.inject.Inject;

import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessGetStrategoRuntimeProvider;
import mb.stratego.pie.AstStrategoTransformTaskDef;

@AccModLangAccessScope
public class InsertCSharpSettings extends AstStrategoTransformTaskDef {

	@Inject
	public InsertCSharpSettings(AccModLangAccessGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
		super(getStrategoRuntimeProvider, "insert-csharp-settings");
	}

	@Override
	public String getId() {
		return InsertCSharpSettings.class.getCanonicalName();
	}

}
