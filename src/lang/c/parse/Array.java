package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class Array extends CParseRule {

	// array ::= LBRA expression RBRA

	private CParseRule expression;

	public Array(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_LBRA;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている

		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);

		if(Expression.isFirst(tk)) {
			expression = new Expression(pcx);
			expression.parse(pcx);
			tk = ct.getCurrentToken(pcx);

			if(tk.getType() != CToken.TK_RBRA) {
				pcx.fatalError("']'がありません.");
			}
			tk = ct.getNextToken(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (expression != null) {
			expression.semanticCheck(pcx);

			if(expression.getCType() != CType.getCType(CType.T_int)){
				pcx.fatalError("[]内は整数型ででなければいけません.");
			}

			this.setCType(expression.getCType());
			this.setConstant(expression.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; array starts");
		if (expression != null) { expression.codeGen(pcx); }
		o.println("\tMOV\t-(R6), R0\t; Array: 配列が示す番地を計算し, 格納します.");
		o.println("\tADD\t-(R6), R0\t; Array:");
		o.println("\tMOV\tR0, (R6)+\t; Array:");
		o.println(";;; array completes");

	}
}
