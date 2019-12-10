package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class Primary extends CParseRule {

	// primary ::= primaryMult | variable

	private CParseRule variable;
	private CParseRule primaryMult;

	public Primary(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return PrimaryMult.isFirst(tk) | Variable.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if(Variable.isFirst(tk)) {
			variable = new Variable(pcx);
			variable.parse(pcx);
		}else if(PrimaryMult.isFirst(tk)){
			primaryMult = new PrimaryMult(pcx);
			primaryMult.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if(primaryMult != null) {
			primaryMult.semanticCheck(pcx);
			setCType(primaryMult.getCType());
			setConstant(primaryMult.isConstant());
		} else if(variable != null) {
			variable.semanticCheck(pcx);
			setCType(variable.getCType());
			setConstant(variable.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; primary starts");
		if (primaryMult != null) { primaryMult.codeGen(pcx); }
		if (variable != null) {variable.codeGen(pcx); }
		o.println(";;; primary completes");
	}

	public CParseRule checkObject(){
		if(primaryMult != null) {
			return primaryMult;
		} else if(variable != null) {
			return variable;
		} else{
			return null;
		}
	}
}

class PrimaryMult extends CParseRule {

	// primaryMult ::= MULT variable

	private CParseRule variable;
	private CToken op;

	public PrimaryMult(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_MULT;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		op = tk;

		tk = ct.getNextToken(pcx);
		if(Variable.isFirst(tk)) {
			variable = new Variable(pcx);
			variable.parse(pcx);
		}else{
			pcx.warning(tk.toExplainString() + "変数でないものをポインタ参照しようとしています.");
		}

	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if(variable != null) {
			variable.semanticCheck(pcx);
			setCType(variable.getCType());
			setConstant(variable.isConstant());
		}
		if(variable.getCType().getType() == CType.T_int || variable.getCType().getType() == CType.T_iarray) {
			pcx.warning("ポインタ参照の変数はint型やint型の配列ではいけません.");
		}else if(variable.getCType().getType() == CType.T_pint){
            this.setCType(CType.getCType(CType.T_int));
        }
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; primaryMult starts");
		if(variable != null) {
			variable.codeGen(pcx);
			o.println("\tMOV\t-(R6), R0\t; PrimaryMult: アドレスを取り出して, 内容を参照して, 積む<" + op.toExplainString() +">");
			o.println("\tMOV\t(R0), (R6)+\t; PrimaryMult:");
		}
		o.println(";;; primaryMult completes");
	}
}