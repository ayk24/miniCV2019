package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class UnsignedFactor extends CParseRule {

	// unsignedFactor ::= factorAmp | number | LPAR expression RPAR | addressToValue

	private CParseRule factoramp;
	private CParseRule number;
	private CParseRule expression;
	private CParseRule addressToValue;

	public UnsignedFactor(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return Number.isFirst(tk) || FactorAmp.isFirst(tk) ||
				tk.getType() == CToken.TK_LPAR || AddressToValue.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if(FactorAmp.isFirst(tk)){
			factoramp = new FactorAmp(pcx);
			factoramp.parse(pcx);
		} else if(Number.isFirst(tk)) {
			number = new Number(pcx);
			number.parse(pcx);
		} else if(tk.getType() == CToken.TK_LPAR) {
			tk = ct.getNextToken(pcx);
			expression = new Expression(pcx);
			expression.parse(pcx);
			tk = ct.getCurrentToken(pcx);

			if(tk.getType() != CToken.TK_RPAR){
				pcx.fatalError("')'がありません.");
			}

			tk = ct.getNextToken(pcx);

		} else if (AddressToValue.isFirst(tk)) {
            addressToValue = new AddressToValue(pcx);
            addressToValue.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (number != null) {
			number.semanticCheck(pcx);
			setCType(number.getCType());			// number の型をそのままコピー
			setConstant(number.isConstant());		// number は常に定数
		}

		if (factoramp != null){
			factoramp.semanticCheck(pcx);
			setCType(factoramp.getCType());				// factoramp の型をそのままコピー
			setConstant(factoramp.isConstant());		// factoramp は常に定数
		}

		if (expression != null){
			expression.semanticCheck(pcx);
			setCType(expression.getCType());			// expresssion の型をそのままコピー
			setConstant(expression.isConstant());		// expresssion は常に定数
		}

		if (addressToValue != null){
			addressToValue.semanticCheck(pcx);
			setCType(addressToValue.getCType());		// addressToValue の型をそのままコピー
			setConstant(addressToValue.isConstant());	// addressToValue は常に定数
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; unsignedfactor starts");
		if (number != null) { number.codeGen(pcx); }
		if (factoramp != null) { factoramp.codeGen(pcx); }
		if (expression != null) { expression.codeGen(pcx); }
		if (addressToValue != null) { addressToValue.codeGen(pcx); }
		o.println(";;; unsignedfactor completes");
	}
}