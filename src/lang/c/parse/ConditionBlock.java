package lang.c.parse;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class ConditionBlock extends CParseRule {

	// conditionBlock ::= LPAR condition RPAR

	private CParseRule condition;
	public ConditionBlock(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_LPAR;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);

		if (Condition.isFirst(tk)) {
			condition = new Condition(pcx);
			condition.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "条件式がありません.");
		}

		tk = ct.getCurrentToken(pcx);

		if (tk.getType() == CToken.TK_RPAR) {
			ct.getNextToken(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "')'がありません.");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (condition != null) {
			condition.semanticCheck(pcx);
			setCType(condition.getCType());
			setConstant(condition.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		if (condition != null) { condition.codeGen(pcx); }
	}
}
