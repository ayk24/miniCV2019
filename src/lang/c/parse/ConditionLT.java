package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class ConditionLT extends CParseRule {

	// conditionLT ::= LT expression ( < )

	private CParseRule left, right;
	private int seq;

	public ConditionLT(CParseContext pcx, CParseRule left) {
		this.left = left;
	}

	public static boolean isFirst(CToken tk) {
		return (tk.getType() == CToken.TK_LT);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		if(tk.getType() == CToken.TK_LT){
			tk = ct.getNextToken(pcx);
			if(Expression.isFirst(tk)){
				right = new Expression(pcx);
				right.parse(pcx);
			} else {
				pcx.fatalError("'<'のあとは'expression'が来ます.");
			}
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (left != null && right != null) {
			left.semanticCheck(pcx);
			right.semanticCheck(pcx);
			if (!left.getCType().equals(right.getCType())) {
				pcx.fatalError("左辺の型[" + left.getCType().toString() + "] と右辺の型["
						+ right.getCType().toString() + "] が一致しないので比較できません.");
			} else {
				this.setCType(CType.getCType(CType.T_bool));
				this.setConstant(true);
			}
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; condition < (compare) starts");
		if (left != null && right != null) {
			left.codeGen(pcx);
			right.codeGen(pcx);
			seq = pcx.getSeqId();
			o.println("\tMOV\t-(R6), R0\t; ConditionLT: 2数を取り出して, 比べる.");
			o.println("\tMOV\t-(R6), R1\t; ConditionLT:");
			o.println("\tMOV\t#0x0001, R2\t; ConditionLT: set true");
			o.println("\tCMP\tR0, R1\t\t; ConditionLT: R1<R0 = R1-R0<0");
			o.println("\tBRN\tLT" + seq + "\t\t\t; ConditionLT:");
			o.println("\tCLR\tR2\t\t\t; ConditionLT: set false");
			o.println("LT" + seq + ":MOV\tR2, (R6)+\t; ConditionLT:");
		}
		o.println(";;; condition < (compare) completes");
	}
}
