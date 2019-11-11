package lang.c.parse;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class StatementElse extends CParseRule {

	// statementElse ::= ELSE ( statementIf | statementBlock )

	private CParseRule cParseRule;
	private CToken idtk;

	public StatementElse(CParseContext pcx, CToken idtk) {
		this.idtk = idtk;
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_ELSE;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);

		if (StatementIf.isFirst(tk)) {
			cParseRule = new StatementIf(pcx, idtk);
			cParseRule.parse(pcx);
		} else if (StatementBlock.isFirst(tk)) {
			cParseRule = new StatementBlock(pcx, idtk);
			cParseRule.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "条件文の内部に文がありません.");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (cParseRule != null) {
			cParseRule.semanticCheck(pcx);
			setCType(cParseRule.getCType());
			setConstant(cParseRule.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		if (cParseRule != null) { cParseRule.codeGen(pcx); }
	}
}
