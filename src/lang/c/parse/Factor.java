package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class Factor extends CParseRule {

	// factor ::= plusFactor | minusFactor | unsignedFactor

	private CParseRule plusfactor;
	private CParseRule minusfactor;
	private CParseRule unsignedfactor;

	public Factor(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return PlusFactor.isFirst(tk) || MinusFactor.isFirst(tk) || UnsignedFactor.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if(PlusFactor.isFirst(tk)){
			plusfactor = new PlusFactor(pcx);
			plusfactor.parse(pcx);
		} else if(MinusFactor.isFirst(tk)) {
			minusfactor = new MinusFactor(pcx);
			minusfactor.parse(pcx);
		} else if(UnsignedFactor.isFirst(tk)){
			unsignedfactor = new UnsignedFactor(pcx);
			unsignedfactor.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (plusfactor != null) {
			plusfactor.semanticCheck(pcx);
			setCType(plusfactor.getCType());			// plusfactor の型をそのままコピー
			setConstant(plusfactor.isConstant());		// plusfactor は常に定数
		}
		if (minusfactor != null){
			minusfactor.semanticCheck(pcx);
			setCType(minusfactor.getCType());			// minusfactor の型をそのままコピー
			setConstant(minusfactor.isConstant());		// minusfactor は常に定数
		}
		if (unsignedfactor != null){
			unsignedfactor.semanticCheck(pcx);
			setCType(unsignedfactor.getCType());		// unsignedfactor の型をそのままコピー
			setConstant(unsignedfactor.isConstant());	// unsignedfactor は常に定数
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; factor starts");
		if (plusfactor != null) { plusfactor.codeGen(pcx); }
		if (minusfactor != null) {minusfactor.codeGen(pcx); }
		if (unsignedfactor != null) {unsignedfactor.codeGen(pcx); }
		o.println(";;; factor completes");
	}
}