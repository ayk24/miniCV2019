package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class Factor extends CParseRule {

	// factor ::= plusFactor | minusFactor | unsignedFactor

	private CParseRule factor;

	public Factor(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return PlusFactor.isFirst(tk) || MinusFactor.isFirst(tk) || UnsignedFactor.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if (PlusFactor.isFirst(tk)) {
			factor = new PlusFactor(pcx);
			factor.parse(pcx);
		} else if (MinusFactor.isFirst(tk)) {
			factor = new MinusFactor(pcx);
			factor.parse(pcx);
		} else if (UnsignedFactor.isFirst(tk)) {
			factor = new UnsignedFactor(pcx);
			factor.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if(factor != null) {
			factor.semanticCheck(pcx);
			setCType(factor.getCType());
			setConstant(factor.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; factor starts");
		if (factor != null) { factor.codeGen(pcx); }
		o.println(";;; factor completes");
	}
}

class PlusFactor extends CParseRule {

	// plusFactor ::= PLUS unsignedFactor

	private CParseRule unsignedfactor;

	public PlusFactor(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_PLUS;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);

		if(UnsignedFactor.isFirst(tk)){
			unsignedfactor = new UnsignedFactor(pcx);
			unsignedfactor.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "'+'の後に'unsignedfactor'がありません.");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (unsignedfactor != null) {
			unsignedfactor.semanticCheck(pcx);
			setCType(unsignedfactor.getCType());		// unsignedfactor の型をそのままコピー
			setConstant(unsignedfactor.isConstant());	// unsignedfactor は常に定数
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; plusFactor starts");
		if(unsignedfactor != null){ unsignedfactor.codeGen(pcx); }
		o.println(";;; plusFactor completes");
	}
}

class MinusFactor extends CParseRule {

	// minusFactor ::= MINUS unsignedFactor

	private CParseRule unsignedfactor;

	public MinusFactor(CParseContext pcx) {
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_MINUS;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);

		if(UnsignedFactor.isFirst(tk)){
			unsignedfactor = new UnsignedFactor(pcx);
			unsignedfactor.parse(pcx);
		} else {
			  pcx.fatalError(tk.toExplainString() + "'-'の後に'unsignedfactor'がありません.");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (unsignedfactor != null) {
			unsignedfactor.semanticCheck(pcx);
			setCType(unsignedfactor.getCType());
			setConstant(unsignedfactor.isConstant());

			if(unsignedfactor.getCType().getType() == CType.T_pint){
				pcx.fatalError("右辺の型[int*]に'-'は付けられません.");
			}
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; minusFactor starts");
		if(unsignedfactor != null){ unsignedfactor.codeGen(pcx); }

		// 符号の変換
		// スタックトップから値を取り出し,
		// その値を0から引いた結果をスタックに積む.

		o.println("\tMOV\t#0, R0\t; MinusFactor:");
		o.println("\tSUB\t-(R6), R0\t; MinusFactor:");
		o.println("\tMOV\tR0, (R6)+\t; MinusFactor:");
		o.println(";;; minusFactor completes");
	}
}