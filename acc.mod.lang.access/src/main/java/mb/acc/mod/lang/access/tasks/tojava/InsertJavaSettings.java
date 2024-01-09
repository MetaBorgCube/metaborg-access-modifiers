package mb.acc.mod.lang.access.tasks.tojava;

import javax.inject.Inject;

import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessGetStrategoRuntimeProvider;
import mb.stratego.pie.AstStrategoTransformTaskDef;

@AccModLangAccessScope
public class InsertJavaSettings extends AstStrategoTransformTaskDef {

	@Inject
	public InsertJavaSettings(AccModLangAccessGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
		super(getStrategoRuntimeProvider, "insert-java-settings");
	}

	@Override
	public String getId() {
		return InsertJavaSettings.class.getCanonicalName();
	}

}
