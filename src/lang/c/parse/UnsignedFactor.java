 package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class UnsignedFactor extends CParseRule {

	// unsignedFactor ::= factorAmp | number | LPAR expression RPAR | addressToValue

	private CParseRule cParseRule;

	public UnsignedFactor(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return FactorAmp.isFirst(tk) || Number.isFirst(tk)
				|| tk.getType() == CToken.TK_LPAR || AddressToValue.isFirst(tk)
				|| CallFunc.isFirst(tk);	// ほんと?
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if(FactorAmp.isFirst(tk)) {
			cParseRule = new FactorAmp(pcx);
			cParseRule.parse(pcx);
		} else if (Number.isFirst(tk)) {
			cParseRule = new Number(pcx);
			cParseRule.parse(pcx);
		} else if (tk.getType() == CToken.TK_LPAR) {
			tk = ct.getNextToken(pcx);
			if (Expression.isFirst(tk)) {
				cParseRule = new Expression(pcx);
				cParseRule.parse(pcx);
				tk = ct.getCurrentToken(pcx);
				if (tk.getType() == CToken.TK_RPAR) {
					ct.getNextToken(pcx);
				} else {
					cParseRule = null;
				}
			}
		} else if (AddressToValue.isFirst(tk)) {
			cParseRule = new AddressToValue(pcx);
			cParseRule.parse(pcx);
		} else if (CallFunc.isFirst(tk)) {
			cParseRule = new CallFunc(pcx);
			cParseRule.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (cParseRule != null) {
			cParseRule.semanticCheck(pcx);
			setCType(cParseRule.getCType());
			setConstant(cParseRule.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; unsignedFactor starts");
		if (cParseRule != null) { cParseRule.codeGen(pcx);}
		o.println(";;; unsignedFactor completes");
	}
}