package mb.acc.mod.lang.access.tasks.torust;

import javax.inject.Inject;

import mb.accmodlang.AMLScope;
import mb.accmodlang.task.AMLGetStrategoRuntimeProvider;
import mb.stratego.pie.AstStrategoTransformTaskDef;

@AMLScope
public class InsertRustSettings extends AstStrategoTransformTaskDef {

	@Inject
	public InsertRustSettings(AMLGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
		super(getStrategoRuntimeProvider, "insert-rust-settings");
	}

	@Override
	public String getId() {
		return InsertRustSettings.class.getCanonicalName();
	}

}
