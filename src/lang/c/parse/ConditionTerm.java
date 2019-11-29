package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class ConditionTerm extends CParseRule {

	// conditionTerm ::= conditionFactor { conditionAND }

	private CParseRule conditionTerm;

	public ConditionTerm(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return ConditionFactor.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CParseRule conditionFactor = null, list = null;

		conditionFactor = new ConditionFactor(pcx);
		conditionFactor.parse(pcx);

		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		while (ConditionAND.isFirst(tk)) {
			list = new ConditionAND(pcx, conditionFactor);
			list.parse(pcx);
			conditionFactor = list;
			tk = ct.getCurrentToken(pcx);
		}
		conditionTerm = conditionFactor;
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (conditionTerm != null) {
			conditionTerm.semanticCheck(pcx);
			this.setCType(conditionTerm.getCType());
			this.setConstant(conditionTerm.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; conditionTerm starts");
		if (conditionTerm != null) conditionTerm.codeGen(pcx);
		o.println(";;; conditionTerm completes");
	}
}