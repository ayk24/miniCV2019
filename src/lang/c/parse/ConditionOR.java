package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class ConditionOR extends CParseRule {

	// conditionOR ::= OR conditionTerm

	private CParseRule left, right;

	public ConditionOR(CParseContext pcx, CParseRule left) {
		this.left = left;
	}

	public static boolean isFirst(CToken tk) {
		return (tk.getType() == CToken.TK_OR);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if(tk.getType() == CToken.TK_OR){
			tk = ct.getNextToken(pcx);
			if(ConditionTerm.isFirst(tk)){
				right = new ConditionTerm(pcx);
				right.parse(pcx);
			} else {
				pcx.fatalError("'||'のあとは'ConditionTerm'が来ます.");
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
		o.println(";;; conditionOR starts");
		if (left != null && right != null) {
			left.codeGen(pcx);
			right.codeGen(pcx);
			o.println("\tMOV\t-(R6), R0\t; ConditionOR: 真理値を取り出す.");
			o.println("\tMOV\t-(R6), R1\t; ConditionOR:");
			o.println("\tOR\tR1, R0\t\t; ConditionOR: OR演算");
			o.println("\tMOV\tR0, (R6)+\t; ConditionOR: 演算結果を積む.");
		}
		o.println(";;; conditionOR completes");
	}
}
