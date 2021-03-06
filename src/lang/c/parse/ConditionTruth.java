package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;


public class ConditionTruth extends CParseRule{

	// conditionTruth ::=  expression ( conditionLT | conditionLE | conditionGT |
	//  conditionGE | conditionEQ | conditionNE ) | TRUE | FALSE | conditionBlock

	private CParseRule expression, condition;
	private boolean flag = true;

	public ConditionTruth(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return Expression.isFirst(tk) || ConditionBlock.isFirst(tk) ||
				tk.getType() == CToken.TK_TRUE || tk.getType() == CToken.TK_FALSE;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if (ConditionBlock.isFirst(tk)) {
			condition = new ConditionBlock(pcx);
			condition.parse(pcx);
		} else if (Expression.isFirst(tk)) {
			expression = new Expression(pcx);
			expression.parse(pcx);
			tk = ct.getCurrentToken(pcx);
			if (ConditionLT.isFirst(tk)) {
				condition = new ConditionLT(pcx, expression);
				condition.parse(pcx);
			} else if (ConditionLE.isFirst(tk)) {
				condition = new ConditionLE(pcx, expression);
				condition.parse(pcx);
			} else if (ConditionGT.isFirst(tk)) {
				condition = new ConditionGT(pcx, expression);
				condition.parse(pcx);
			} else if (ConditionGE.isFirst(tk)) {
				condition = new ConditionGE(pcx, expression);
				condition.parse(pcx);
			} else if (ConditionEQ.isFirst(tk)) {
				condition = new ConditionEQ(pcx, expression);
				condition.parse(pcx);
			} else if (ConditionNE.isFirst(tk)) {
				condition = new ConditionNE(pcx, expression);
				condition.parse(pcx);
			}
		} else if (tk.getType() == CToken.TK_TRUE) {
			flag = true;
			tk = ct.getNextToken(pcx);
		} else if (tk.getType() == CToken.TK_FALSE) {
			flag = false;
			tk = ct.getNextToken(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (condition != null) {
			condition.semanticCheck(pcx);
			setCType(condition.getCType());
			setConstant(condition.isConstant());
		} else {
			setCType(CType.getCType(CType.T_bool));
			setConstant(true);
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; conditionTruth starts");
		if (condition != null) {
			condition.codeGen(pcx);
		} else {
			if (flag == true) {
				o.println("\tMOV\t#0x0001, (R6)+\t; ConditionTruth: true(1)を積む");
			} else if (flag == false) {
				o.println("\tMOV\t#0x0000, (R6)+\t; ConditionTruth: false(0)を積む");
			}
		}
		o.println(";;; conditionTruth completes");
	}
}
