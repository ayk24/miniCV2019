package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.RecoverableErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class StatementInput extends CParseRule {

	// statementInput ::= INPUT primary SEMI

	private CParseRule primary;

	public StatementInput(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_INPUT;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);
		try {
			if (Primary.isFirst(tk)) {
				primary = new Primary(pcx);
				primary.parse(pcx);
				tk = ct.getCurrentToken(pcx);
				if (tk.getType() == CToken.TK_SEMI) {
					ct.getNextToken(pcx);
				}else {
					pcx.warning(tk.toExplainString() + "'primary'のあとの';'を補いました");
				}
			}else {
				pcx.recoverableError(tk.toExplainString() + "'primary'がありません.");
			}
		} catch (RecoverableErrorException e) {
			ct.skipTo(pcx, CToken.TK_SEMI, CToken.TK_RCUR);
			tk = ct.getNextToken(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (primary != null) {
			primary.semanticCheck(pcx);
			if (primary.isConstant() == true) {
				pcx.warning("定数にinputは出来ません.");
			}
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; statementInput starts");
		if (primary != null) { primary.codeGen(pcx); }
		o.println("\tMOV\t-(R6), R0\t; statementInput:");
		o.println("\tMOV\t#0xFFE0, R1\t; statementInput:");
		o.println("\tMOV\t(R1), (R0)\t; statementInput:");
		o.println(";;; statementInput completes");
	}
}
