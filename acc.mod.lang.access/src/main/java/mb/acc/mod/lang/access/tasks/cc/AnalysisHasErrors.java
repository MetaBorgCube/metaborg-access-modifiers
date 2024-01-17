package mb.acc.mod.lang.access.tasks.cc;

import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessGetStrategoRuntimeProvider;
import mb.common.result.Result;
import mb.constraint.pie.ConstraintAnalyzeFile;
import mb.constraint.pie.ConstraintAnalyzeFile.Output;
import mb.pie.api.ExecContext;
import mb.stratego.pie.StrategoTransformTaskDef;

@AccModLangAccessScope
public class AnalysisHasErrors extends StrategoTransformTaskDef<ConstraintAnalyzeFile.Output> {

	@Inject
	public AnalysisHasErrors(AccModLangAccessGetStrategoRuntimeProvider getStrategoRuntimeProviders) {
		super(getStrategoRuntimeProviders, "analysis-has-errors");
	}

	@Override
	public String getId() {
		return AnalysisHasErrors.class.getSimpleName();
	}

	@Override
	protected Result<IStrategoTerm, ?> getAst(ExecContext context, Output analysis) {
		return Result.ofOk(analysis.analysis);
	}
}
