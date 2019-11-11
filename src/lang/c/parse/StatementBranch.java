package lang.c.parse;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class StatementBranch extends CParseRule {

	// statementBranch ::= statementIf | statementWhile

	private CParseRule branch;
	private CToken idtk;

	public StatementBranch(CParseContext pcx, CToken idtk) {
		this.idtk = idtk;
	}

	public static boolean isFirst(CToken tk) {
		return StatementIf.isFirst(tk) || StatementWhile.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		if (StatementIf.isFirst(tk)) {
			branch = new StatementIf(pcx, idtk);
			branch.parse(pcx);
		} else if (StatementWhile.isFirst(tk)) {
			branch = new StatementWhile(pcx, idtk);
			branch.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (branch != null) {
			branch.semanticCheck(pcx);
			setCType(branch.getCType());
			setConstant(branch.isConstant());
		}
	}
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		if (branch != null) { branch.codeGen(pcx); }
	}
}