package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class ConditionAND extends CParseRule {

	// conditionAND ::= AND conditionFactor

	private CParseRule left, right;

	public ConditionAND(CParseContext pcx, CParseRule left) {
		this.left = left;
	}

	public static boolean isFirst(CToken tk) {
		return (tk.getType() == CToken.TK_AND);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if(tk.getType() == CToken.TK_AND){
			tk = ct.getNextToken(pcx);
			if(ConditionFactor.isFirst(tk)){
				right = new ConditionFactor(pcx);
				right.parse(pcx);
			} else {
				pcx.fatalError(tk.toExplainString() +"'&&'のあとは'ConditionFactor'が来ます.");
			}
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (left != null && right != null) {
			left.semanticCheck(pcx);
			right.semanticCheck(pcx);
			this.setCType(CType.getCType(CType.T_bool));
			this.setConstant(true);
		}

	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; conditionAND starts");
		if (left != null && right != null) {
			left.codeGen(pcx);
			right.codeGen(pcx);
			o.println("\tMOV\t-(R6), R0\t; ConditionAND: 真理値を取り出す.");
			o.println("\tMOV\t-(R6), R1\t; ConditionAND:");
			o.println("\tAND\tR1, R0\t\t; ConditionAND: AND演算");
			o.println("\tMOV\tR0, (R6)+\t; ConditionAND: 演算結果を積む.");
		}
		o.println(";;; conditionAND completes");
	}
}
