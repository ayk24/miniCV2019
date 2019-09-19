package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class FactorAmp extends CParseRule {

	// factorAmp ::= AMP ( number | primary )

	private CParseRule number;
	private CParseRule primary;

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_AMP;
	}

	public FactorAmp(CParseContext pcx) {
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);

		if(Number.isFirst(tk)){
			number = new Number(pcx);
			number.parse(pcx);
		} else if (Primary.isFirst(tk)){
			primary = new Primary(pcx);
			primary.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "'&'の後に数値がありません.");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (number != null) {
			number.semanticCheck(pcx);
			setCType(CType.getCType(CType.T_pint));		// factorAmpの型をそのままコピー
			setConstant(number.isConstant());			// number は常に定数
		}

		if(primary != null) {
			if(((Primary) primary).checkObject() instanceof PrimaryMult) {
				pcx.fatalError("&*varといった表現は禁止されています.");
			}
			primary.semanticCheck(pcx);
			setCType(primary.getCType());
			setConstant(primary.isConstant());
		}

	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; factorAmp starts");
		if (number != null) { number.codeGen(pcx); }
		if (primary != null) { primary.codeGen(pcx); }
		o.println(";;; factorAmp completes");
	}

}
