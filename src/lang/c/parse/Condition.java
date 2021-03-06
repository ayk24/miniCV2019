package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class Condition extends CParseRule {

	// condition ::= conditionTerm { conditionOR }

	private CParseRule condition;

	public Condition(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return ConditionTerm.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CParseRule conditionTerm = null, list = null;

		conditionTerm = new ConditionTerm(pcx);
		conditionTerm.parse(pcx);

		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		while (ConditionOR.isFirst(tk)) {
			list = new ConditionOR(pcx, conditionTerm);
			list.parse(pcx);
			conditionTerm = list;
			tk = ct.getCurrentToken(pcx);
		}
		condition = conditionTerm;
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (condition != null) {
			condition.semanticCheck(pcx);
			this.setCType(condition.getCType());
			this.setConstant(condition.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; condition starts");
		if (condition != null) condition.codeGen(pcx);
		o.println(";;; condition completes");
	}
}