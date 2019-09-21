package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class ConditionLE extends CParseRule {

	// conditionLE ::= LE expression ( <= )

	private CParseRule left, right;
	private int seq;

	public ConditionLE(CParseContext pcx, CParseRule left) {
		this.left = left;
	}

	public static boolean isFirst(CToken tk) {
		return (tk.getType() == CToken.TK_LE);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		if(tk.getType() == CToken.TK_LE){
			tk = ct.getNextToken(pcx);
			if(Expression.isFirst(tk)){
				right = new Expression(pcx);
				right.parse(pcx);
			} else {
				pcx.fatalError("'<='のあとは'expression'が来ます.");
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
		o.println(";;; condition <= (compare) starts");
		if (left != null && right != null) {
			left.codeGen(pcx);
			right.codeGen(pcx);
			seq = pcx.getSeqId();
			o.println("\tMOV\t-(R6), R0\t; ConditionLE: 2数を取り出して,比べる.");
			o.println("\tMOV\t-(R6), R1\t; ConditionLE:");
			o.println("\tMOV\t#0x0001, R2\t; ConditionLE: set true");
			o.println("\tCMP\tR0, R1\t\t; ConditionLE: R1<=R0 = R1-R0<=0");
			o.println("\tBRN\tLE" + seq + "\t\t\t; ConditionLE:");
			o.println("\tBRZ\tLE" + seq + "\t\t\t; ConditionLE:");
			o.println("\tCLR\tR2\t\t\t; ConditionLE: set false");
			o.println("LE" + seq + ":MOV\tR2, (R6)+\t; ConditionLE:");
		}
		o.println(";;; condition <= (compare) completes");
	}
}

