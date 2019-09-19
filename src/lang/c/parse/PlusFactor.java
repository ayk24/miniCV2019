package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class PlusFactor extends CParseRule {

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
		o.println(";;; plusfactor starts");
		if(unsignedfactor != null){ unsignedfactor.codeGen(pcx); }
		o.println(";;; plusfactor completes");
	}
}