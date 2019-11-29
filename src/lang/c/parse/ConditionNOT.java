package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class ConditionNOT extends CParseRule {

	// conditionNOT ::= NOT conditionTruth

	private CParseRule condition;

	public ConditionNOT(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return (tk.getType() == CToken.TK_NOT);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if(tk.getType() == CToken.TK_NOT){
			tk = ct.getNextToken(pcx);
			if(ConditionTruth.isFirst(tk)){
				condition = new ConditionTruth(pcx);
				condition.parse(pcx);
			} else {
				pcx.fatalError("'!'のあとは'ConditionTruth'が来ます.");
			}
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (condition != null) {
			condition.semanticCheck(pcx);
			this.setCType(CType.getCType(CType.T_bool));
			this.setConstant(true);
		}
	}


	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; conditionNOT starts");
		if (condition != null) {
			condition.codeGen(pcx);
			o.println("\tMOV\t-(R6), R0\t; ConditionNOT: スタックから値を取り出す.");
			o.println("\tXOR\t#0x0001, R0\t; ConditionNOT: NOT演算");
			o.println("\tMOV\tR0, (R6)+\t; ConditionNOT: 演算結果を積む.");
		}
		o.println(";;; conditionNOT completes");
	}
}
