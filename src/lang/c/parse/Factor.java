package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class Factor extends CParseRule {

	// factor ::= factorAmp | number
	// factorAmp ::= AMP number

	private CParseRule number;
	private CParseRule amp;

	public Factor(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return Number.isFirst(tk) || FactorAmp.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if(Number.isFirst(tk)){
			number = new Number(pcx);
			number.parse(pcx);
		}else if(FactorAmp.isFirst(tk)){
			amp = new FactorAmp(pcx);
			amp.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (number != null) {
			number.semanticCheck(pcx);
			setCType(number.getCType());		// number の型をそのままコピー
			setConstant(number.isConstant());	// number は常に定数
		}

		if(amp != null) {
			amp.semanticCheck(pcx);
			setCType(amp.getCType());		// amp の型をそのままコピー
			setConstant(amp.isConstant());	// amp は常に定数
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; factor starts");
		if (number != null) { number.codeGen(pcx); }
		if (amp != null) { amp.codeGen(pcx); }
		o.println(";;; factor completes");
	}
}