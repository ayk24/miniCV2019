package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.RecoverableErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class ConditionGE extends CParseRule {

	// conditionGE ::= GE expression ( >= )

	private CParseRule left, right;
	private int seq;

	public ConditionGE(CParseContext pcx, CParseRule left) {
		this.left = left;
	}

	public static boolean isFirst(CToken tk) {
		return (tk.getType() == CToken.TK_GE);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		if(tk.getType() == CToken.TK_GE){
			tk = ct.getNextToken(pcx);
			try {
				if(Expression.isFirst(tk)){
					right = new Expression(pcx);
					right.parse(pcx);
				} else {
					pcx.recoverableError(tk.toExplainString() + "'>='のあとは'expression'が来ます.");
				}
			} catch (RecoverableErrorException e) {
				ct.skipTo(pcx, CToken.TK_SEMI, CToken.TK_RCUR);
				tk = ct.getNextToken(pcx);
			}
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (left != null && right != null) {
			left.semanticCheck(pcx);
			right.semanticCheck(pcx);
			if (!left.getCType().equals(right.getCType())) {
				pcx.warning("左辺の型[" + left.getCType().toString() + "] と右辺の型["
						+ right.getCType().toString() + "] が一致しないので比較できません.");
			} else {
				this.setCType(CType.getCType(CType.T_bool));
				this.setConstant(true);
			}
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; condition >= (compare) starts");
		if (left != null && right != null) {
			left.codeGen(pcx);
			right.codeGen(pcx);
			seq = pcx.getSeqId();
			o.println("\tMOV\t-(R6), R0\t; ConditionGE: 2数を取り出して, 比べる.");
			o.println("\tMOV\t-(R6), R1\t; ConditionGE:");
			o.println("\tMOV\t#0x0001, R2\t; ConditionGE: set true");
			o.println("\tCMP\tR1, R0\t\t; ConditionGE: R1>=R0 = R0-R1<=0");
			o.println("\tBRN\tGE" + seq + "\t\t\t; ConditionGE:");
			o.println("\tBRZ\tGE" + seq + "\t\t\t; ConditionGE:");
			o.println("\tCLR\tR2\t\t\t; ConditionGE: set false");
			o.println("GE" + seq + ":MOV\tR2, (R6)+\t; ConditionGE:");
		}
		o.println(";;; condition >= (compare) completes");
	}
}
