package mb.acc.mod.lang.access.tasks;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.common.result.Result;
import mb.common.util.ListView;
import mb.constraint.pie.ConstraintAnalyzeTaskDef.Output;
import mb.pie.api.ExecContext;
import mb.stratego.common.StrategoRuntime;
import mb.stratego.common.Strategy;
import mb.stratego.pie.GetStrategoRuntimeProvider;
import mb.stratego.pie.StrategoTransformTaskDef;

public abstract class TransformWithAnalysis extends StrategoTransformTaskDef<Output> {

	private final String strategyName;

	public TransformWithAnalysis(GetStrategoRuntimeProvider getStrategoRuntimeProvider, String strategyName) {
		super(getStrategoRuntimeProvider, ListView.of());
		this.strategyName = strategyName;
	}

	@Override
	protected StrategoRuntime getStrategoRuntime(ExecContext context, Output input) {
		return super.getStrategoRuntime(context, input).addContextObject(input.context);
	}

	@Override
	protected ListView<Strategy> getStrategies(ExecContext context, Output input) {
		return ListView.of(Strategy.strategy(strategyName, ListView.of(), ListView.of(input.result.analysis)));
	}

	@Override
	protected Result<IStrategoTerm, ?> getAst(ExecContext context, Output input) {
	    return Result.ofOk(input.result.analyzedAst);
	}

}