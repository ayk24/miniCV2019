package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class StatementOutput extends CParseRule {

	// statementOutput ::= OUTPUT expression SEMI

	private CParseRule expression;

	public StatementOutput(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_OUTPUT;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);

		if (Expression.isFirst(tk)) {
			expression = new Expression(pcx);
			expression.parse(pcx);
			tk = ct.getCurrentToken(pcx);
			if (tk.getType() == CToken.TK_SEMI) {
				tk = ct.getNextToken(pcx);
			} else {
				pcx.fatalError("'expresssion'のあとに';'が来ます.");
			}
		} else {
			pcx.fatalError("'OUTPUT'のあとは'expression'が来ます.");
		}

	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (expression != null) {
			expression.semanticCheck(pcx);
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; statementOutput starts");
		if (expression != null) { expression.codeGen(pcx); }
		o.println("\tMOV\t-(R6), R0\t; statementOutput:");
		o.println("\tMOV\t#0xFFE0, R1\t; statementOutput:");
		o.println("\tMOV\tR0, (R1)\t; statementOutput:");
		o.println(";;; statementOutput completes");
	}
}
