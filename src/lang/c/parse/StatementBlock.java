package lang.c.parse;

import java.util.ArrayList;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class StatementBlock extends CParseRule {

	// statementBlock ::= LCUR [ statement ] RCUR

	CParseRule statement;
	private ArrayList<CParseRule> statementList;
	private CToken idtk;

	public StatementBlock(CParseContext pcx, CToken idtk) {
		this.idtk = idtk;
		statementList = new ArrayList<CParseRule>();
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_LCUR;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);

		while(Statement.isFirst(tk)) {
			statement = new Statement(pcx, idtk);
			statement.parse(pcx);
			statementList.add(statement);
			tk = ct.getCurrentToken(pcx);
		}

		if (tk.getType() == CToken.TK_RCUR) {
			ct.getNextToken(pcx);
		} else {
			pcx.warning(tk.toExplainString() + "'}'が無いので補いました.");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		setCType(null);
		for (CParseRule statement : statementList) {
			if (statement != null) {
				statement.semanticCheck(pcx);
				if (((Statement)statement).checkStatementReturn()) {
					setCType(statement.getCType());
				}
			}
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		for (CParseRule statement : statementList) { statement.codeGen(pcx); }
	}
}
