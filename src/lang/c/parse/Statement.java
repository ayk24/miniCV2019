package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class Statement extends CParseRule{

	// statement ::= statementAssign | statementBranch | statementInput | StatementOutput | statementCall | statementReturn

	private CParseRule statement;
	private CToken idtk;

	public Statement(CParseContext pcx, CToken idtk) {
		this.idtk = idtk;
	}

	public static boolean isFirst(CToken tk) {
		return StatementAssign.isFirst(tk) || StatementBranch.isFirst(tk)
				|| StatementInput.isFirst(tk) || StatementOutput.isFirst(tk)
				|| StatementCall.isFirst(tk) || StatementReturn.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if (StatementAssign.isFirst(tk)) {
			statement = new StatementAssign(pcx);
		} else if (StatementBranch.isFirst(tk)) {
			statement = new StatementBranch(pcx, idtk);
		}else if(StatementInput.isFirst(tk)){
			statement = new StatementInput(pcx);
		}else if(StatementOutput.isFirst(tk)){
			statement = new StatementOutput(pcx);
		} else if (StatementCall.isFirst(tk)) {
			statement = new StatementCall(pcx);
		} else if (StatementReturn.isFirst(tk)) {
			statement = new StatementReturn(pcx, idtk);
		}

		statement.parse(pcx);
		tk = ct.getCurrentToken(pcx);

	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if(statement != null) {
			statement.semanticCheck(pcx);
			this.setCType(statement.getCType());
			this.setConstant(statement.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; statement starts");
		if (statement != null) { statement.codeGen(pcx); }
		o.println(";;; statement completes");
	}

	public boolean checkStatementReturn() {
		return statement instanceof StatementReturn;
	}

	public boolean checkStatementBranch() {
		return statement instanceof StatementBranch;
	}
}