package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class MinusFactor extends CParseRule {

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
				pcx.fatalError("ポインタに'-'は付けられません.");
			}
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; minusfactor starts");
		if(unsignedfactor != null){ unsignedfactor.codeGen(pcx); }

		// スタックトップから値を取り出し,
		// その値を0から引いた結果をスタックに積む.

		o.println("\tMOV\t#0, R0\t; MinusFactor:");
		o.println("\tSUB\t-(R6), R0\t; MinusFactor:");
		o.println("\tMOV\tR0, (R6)+\t; MinusFactor:");
		o.println(";;; minusfactor completes");
	}
}