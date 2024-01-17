package mb.acc.mod.lang.access.tasks.torust;

import javax.inject.Inject;

import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessGetStrategoRuntimeProvider;
import mb.stratego.pie.AstStrategoTransformTaskDef;

@AccModLangAccessScope
public class InsertRustSettings extends AstStrategoTransformTaskDef {

	@Inject
	public InsertRustSettings(AccModLangAccessGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
		super(getStrategoRuntimeProvider, "insert-rust-settings");
	}

	@Override
	public String getId() {
		return InsertRustSettings.class.getCanonicalName();
	}

}
