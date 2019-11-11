package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class Declaration extends CParseRule {

	// declaration ::= intDecl | constDecl | voidDecl

	private CParseRule cParseRule;

	public Declaration(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return IntDecl.isFirst(tk) || ConstDecl.isFirst(tk) || VoidDecl.isFirst(tk);
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if (IntDecl.isFirst(tk)) {
			cParseRule = new IntDecl(pcx);
			cParseRule.parse(pcx);
		} else if(ConstDecl.isFirst(tk)) {
			cParseRule = new ConstDecl(pcx);
			cParseRule.parse(pcx);
		} else if(VoidDecl.isFirst(tk)) {
			cParseRule = new VoidDecl(pcx);
			cParseRule.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (cParseRule != null) {
			cParseRule.semanticCheck(pcx);
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; declaration starts");
		if (cParseRule != null) { cParseRule.codeGen(pcx); }
		o.println(";;; declaration completes");
	}
}