package mb.acc.mod.lang.access.tasks;

import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessGetStrategoRuntimeProvider;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.constraint.pie.ConstraintAnalyzeTaskDef.Output;
import mb.pie.api.ExecContext;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.Strategy;
import mb.stratego.pie.StrategoTransformTaskDef;

@AccModLangAccessScope
public class TransformToJava extends StrategoTransformTaskDef<Output> {

	@Inject
    public TransformToJava(AccModLangAccessGetStrategoRuntimeProvider getStrategoRuntimeProvider) {
        super(getStrategoRuntimeProvider, ListView.of());
    }
	
	@Override
	public String getId() {
		return TransformToJava.class.getCanonicalName();
	}
	
	@Override
	protected StrategoRuntime getStrategoRuntime(ExecContext context, Output input) {
		return super.getStrategoRuntime(context, input).addContextObject(input.context);
	}
	
	@Override
	protected ListView<Strategy> getStrategies(ExecContext context, Output input) {
		return ListView.of(Strategy.strategy("transform-to-java", ListView.of(), ListView.of(input.result.analysis)));
	}

    @Override 
    protected Result<IStrategoTerm, ?> getAst(ExecContext context, Output input) {
        return Result.ofOk(input.result.analyzedAst);
    }

}
