package mb.acc.mod.lang.access.tasks.cc;

import java.io.Serializable;
import java.util.Objects;

import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import mb.accmodlang.AMLScope;
import mb.accmodlang.task.AMLGetStrategoRuntimeProvider;
import mb.common.region.Region;
import mb.common.result.Result;
import mb.common.util.ListView;
import mb.pie.api.ExecContext;
import mb.stratego.pie.StrategoTransformTaskDef;

@AMLScope
public class PrependOffset extends StrategoTransformTaskDef<PrependOffset.Args> {

	public static class Args implements Serializable {
		
		private static final long serialVersionUID = 42L;

		private final Region region;
		
		private final IStrategoTerm ast;
		
		public Args(Region region, IStrategoTerm ast) {
			this.region = region;
			this.ast = ast;
		}

		@Override
		public int hashCode() {
			return Objects.hash(ast, region);
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
			return Objects.equals(ast, other.ast) && region == other.region;
		}

		@Override
		public String toString() {
			return "Args [region=" + region + ", ast=" + ast + "]";
		}
		
	}
	
	@Inject
	public PrependOffset(AMLGetStrategoRuntimeProvider getStrategoRuntimeProviders) {
		super(getStrategoRuntimeProviders, ListView.of());
	}

	@Override
	protected Result<IStrategoTerm, ?> getAst(ExecContext context, Args args) {
		final ITermFactory termFactory = getStrategoRuntime(context, args).getTermFactory();
		return Result.ofOk(termFactory.makeTuple(
				termFactory.makeInt(args.region.getStartOffset()),
				termFactory.makeInt(args.region.getEndOffset()),
				args.ast
		));
	}

	@Override
	public String getId() {
		return PrependOffset.class.getSimpleName();
	}
}
