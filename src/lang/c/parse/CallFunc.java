package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class CallFunc extends CParseRule {

	// callFunc ::= @ IDENT call

	private CParseRule ident, call;
	private CToken identToken;

	public CallFunc(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_AT;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);

		if (Ident.isFirst(tk)) {
			identToken = tk;
			ident = new Ident(pcx);
			ident.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "識別子がありません.");
		}

		tk = ct.getCurrentToken(pcx);

		if (Call.isFirst(tk)) {
			call = new Call(pcx, identToken);
			call.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "'()'がありません.");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (ident != null) {
			ident.semanticCheck(pcx);
			setCType(ident.getCType());
			setConstant(ident.isConstant());
		}
		if (call != null) {
			call.semanticCheck(pcx);
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; callFunc starts");
		if (call != null) { call.codeGen(pcx); }
		o.println("\tMOV\tR0, (R6)+\t; callFunc: 返り値をスタックに積む.");
		o.println(";;; callFunc completes");
	}
}