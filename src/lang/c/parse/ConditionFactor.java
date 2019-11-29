package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class ConditionFactor extends CParseRule {

	// conditionFactor ::= conditionNOT | conditionTruth

	private CParseRule condition;

	public ConditionFactor(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return ConditionNOT.isFirst(tk) || ConditionTruth.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if (ConditionNOT.isFirst(tk)) {
			condition = new ConditionNOT(pcx);
			condition.parse(pcx);
		} else if (ConditionTruth.isFirst(tk)) {
			condition = new ConditionTruth(pcx);
			condition.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (condition != null) {
			condition.semanticCheck(pcx);
			setCType(condition.getCType());
			setConstant(condition.isConstant());
		}
	}
	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; conditionFactor starts");
		if (condition != null) { condition.codeGen(pcx); }
		o.println(";;; conditionFactor completes");
	}
}