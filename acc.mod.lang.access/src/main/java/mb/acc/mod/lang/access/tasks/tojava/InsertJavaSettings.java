package mb.acc.mod.lang.access.tasks.tojava;

import javax.inject.Inject;

import mb.accmodlang.AMLScope;
import mb.accmodlang.task.AMLGetStrategoRuntimeProvider;
import mb.stratego.pie.AstStrategoTransformTaskDef;

@AMLScope
public class InsertJavaSettings extends AstStrategoTransformTaskDef {

	@Inject
	public InsertJavaSettings(AMLGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
		super(getStrategoRuntimeProvider, "insert-java-settings");
	}

	@Override
	public String getId() {
		return InsertJavaSettings.class.getCanonicalName();
	}

}
