package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.RecoverableErrorException;
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
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);
		try {
			if(Number.isFirst(tk)){
				number = new Number(pcx);
				number.parse(pcx);
			} else if (Primary.isFirst(tk)){
				primary = new Primary(pcx);
				primary.parse(pcx);
			} else {
				pcx.recoverableError(tk.toExplainString() + "'&'の後に数値がありません.");
			}
		} catch (RecoverableErrorException e) {
			ct.skipTo(pcx, CToken.TK_SEMI, CToken.TK_RCUR);
			tk = ct.getNextToken(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (number != null) {
			number.semanticCheck(pcx);
			setCType(CType.getCType(CType.T_pint));
			setConstant(number.isConstant());
		}

		if(primary != null) {
			if(((Primary) primary).checkObject() instanceof PrimaryMult) {
				pcx.warning("&の後ろに*が来てはいけません.");
			}
			primary.semanticCheck(pcx);
			if(primary.getCType().getType() == CType.T_pint || primary.getCType().getType() == CType.T_parray) {
				pcx.warning("ポインタへのポインタはこの言語では使用できません.");
			}
			setCType(CType.getCType(CType.T_pint));
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
