package mb.acc.mod.lang.access.tasks.cc;

import java.util.Objects;

import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessGetStrategoRuntimeProvider;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.stratego.pie.StrategoTransformTaskDef;

@AccModLangAccessScope
public class PrependOffset extends StrategoTransformTaskDef<PrependOffset.Args> {

	public static class Args {
		
		private final int offset;
		
		private final IStrategoTerm ast;
		
		public Args(int offset, IStrategoTerm ast) {
			this.offset = offset;
			this.ast = ast;
		}

		@Override
		public int hashCode() {
			return Objects.hash(ast, offset);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Args other = (Args) obj;
			return Objects.equals(ast, other.ast) && offset == other.offset;
		}

		@Override
		public String toString() {
			return "Args [offset=" + offset + ", ast=" + ast + "]";
		}
		
	}
	
	@Inject
	public PrependOffset(AccModLangAccessGetStrategoRuntimeProvider getStrategoRuntimeProviders) {
		super(getStrategoRuntimeProviders, ListView.of());
	}

	@Override
	protected Result<IStrategoTerm, ?> getAst(ExecContext context, Args args) {
		final ITermFactory termFactory = getStrategoRuntime(context, args).getTermFactory();
		return Result.ofOk(termFactory.makeTuple(termFactory.makeInt(args.offset), args.ast));
	}

	@Override
	public String getId() {
		return PrependOffset.class.getSimpleName();
	}
}
