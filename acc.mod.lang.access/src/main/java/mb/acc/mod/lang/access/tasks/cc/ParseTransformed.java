package mb.acc.mod.lang.access.tasks.cc;

import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import mb.accmodlangaccess.AccModLangAccessClassLoaderResources;
import mb.accmodlangaccess.AccModLangAccessParser;
import mb.accmodlangaccess.AccModLangAccessScope;
import mb.accmodlangaccess.task.AccModLangAccessParse;
import mb.common.option.Option;
import mb.common.result.Result;
import mb.common.text.Text;
import mb.jsglr.common.JsglrParseException;
import mb.jsglr.common.JsglrParseOutput;
import mb.jsglr.pie.ImmutableJsglrParseTaskInput;
import mb.jsglr.pie.JsglrParseTaskInput;
import mb.pie.api.ExecContext;
import mb.pie.api.Supplier;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.common.StrategoRuntime;

@AccModLangAccessScope
public class ParseTransformed extends AccModLangAccessParse {
	
	/* public static final class Args implements JsglrParseTaskInput {

		private static final long serialVersionUID = 42L;
		
		private final JsglrParseTaskInput parentInput;
		private final int offset;
		
		public Args(ImmutableJsglrParseTaskInput parentInput, int offset) {
			this.parentInput = parentInput;
			this.offset = offset;
		}

		@Override
		public Optional<ResourcePath> rootDirectoryHint() {
			return parentInput.rootDirectoryHint();
		}

		@Override
		public Optional<String> startSymbol() {
			return parentInput.startSymbol();
		}

		@Override
		public Supplier<Text> textSupplier() {
			return parentInput.textSupplier();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + offset;
			result = prime * result + ((parentInput == null) ? 0 : parentInput.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Args other = (Args) obj;
			return Objects.equals(other.offset, offset) && Objects.equals(other.parentInput, parentInput);
		}

		@Override
		public String toString() {
			return "Args [offset=" + offset + ", parentInput=" + parentInput + "]";
		}
		
		
		
	} */

	private final Provider<StrategoRuntime> strategoRuntimeProvider;
	private final AccModLangAccessParse parse;
	private final AccModToPlaceHolder toPlaceHolder;

	@Inject
	public ParseTransformed(
			AccModLangAccessClassLoaderResources classLoaderResources,
			Provider<AccModLangAccessParser> parserProvider,
			Provider<StrategoRuntime> strategoRuntimeProvider, 
			AccModLangAccessParse parse, AccModToPlaceHolder toPlaceHolder
	) {
		super(classLoaderResources, parserProvider);
		this.strategoRuntimeProvider = strategoRuntimeProvider;
		this.parse = parse;
		this.toPlaceHolder = toPlaceHolder;
	}
	
	@Override
	public String getId() {
		return ParseTransformed.class.getSimpleName();
	}
	
	// Ugh: bypass inextensible code completion task
	static final ThreadLocal<Integer> offsetHolder = new ThreadLocal<Integer>();
		
	@Override
	public Result<JsglrParseOutput, JsglrParseException> exec(ExecContext context, JsglrParseTaskInput input)
			throws Exception {
		final int offset = offsetHolder.get();
		
		final Supplier<Result<JsglrParseOutput, JsglrParseException>> resultSupplier = parse.createSupplier(input)
			.map((ctx, parseResult) -> {
				return parseResult.flatMap(parseOutput -> {
					if(parseOutput.ast == null) {
						return Result.ofOk(parseOutput);
					}
					
					final Supplier<Result<IStrategoTerm, JsglrParseException>> astSupplier = parse.createAstSupplier(input)
							.map((c, astResult) -> {
								return astResult.map(ast -> {
									final ITermFactory termFactory = strategoRuntimeProvider.get().getTermFactory();
									return termFactory.makeTuple(termFactory.makeInt(offset), parseOutput.ast);	
								});						
							});
					
					final Result<JsglrParseOutput, JsglrParseException> replacedAst = ctx.require(toPlaceHolder, astSupplier)
							.map(newAst -> new JsglrParseOutput(newAst, parseOutput.tokens, parseOutput.messages, parseOutput.recovered, parseOutput.ambiguous, parseOutput.startSymbol, parseOutput.fileHint, parseOutput.rootDirectoryHint))
							.mapErr(e -> JsglrParseException.otherFail(e, Option.ofOptional(input.startSymbol()), input.fileHint(), Option.ofOptional(input.rootDirectoryHint())));
						
				
					ctx.logger().debug("replaced ast: ", replacedAst);
					
					return replacedAst;
				});
		});
		
		return context.require(resultSupplier);
	}
	


}
