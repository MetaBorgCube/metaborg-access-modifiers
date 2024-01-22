package mb.acc.mod.lang.access.tasks.tocsharp;

import javax.inject.Inject;

import mb.accmodlang.AMLScope;
import mb.accmodlang.task.AMLGetStrategoRuntimeProvider;
import mb.stratego.pie.AstStrategoTransformTaskDef;

@AMLScope
public class InsertCSharpSettings extends AstStrategoTransformTaskDef {

	@Inject
	public InsertCSharpSettings(AMLGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
		super(getStrategoRuntimeProvider, "insert-csharp-settings");
	}

	@Override
	public String getId() {
		return InsertCSharpSettings.class.getCanonicalName();
	}

}
