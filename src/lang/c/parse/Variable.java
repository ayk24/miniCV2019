package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class Variable extends CParseRule{

	// variable ::= ident [ array | call ]

	private CParseRule ident, array, call;
	private CToken idtk;

	public Variable(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return Ident.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		if (Ident.isFirst(tk)) {
			idtk = tk;
			ident = new Ident(pcx);
			ident.parse(pcx);
		}

		tk = ct.getCurrentToken(pcx);

		if (Array.isFirst(tk)) {
			array = new Array(pcx);
			array.parse(pcx);
		}

		if (Call.isFirst(tk)) {
			call = new Call(pcx, idtk);
			call.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (ident != null) {
			ident.semanticCheck(pcx);
			setCType(ident.getCType());
			if (array != null) {
				array.semanticCheck(pcx);
				if (ident.getCType() == CType.getCType(CType.T_iarray)) {
					setCType(CType.getCType(CType.T_int));
				} else if (ident.getCType() == CType.getCType(CType.T_parray)) {
					setCType(CType.getCType(CType.T_pint));
				} else if (ident.getCType().getType() == CType.T_err) {
					pcx.fatalError("identの識別子はintでなければなりません.");
				} else if (ident.getCType().getType() == CType.T_int) {
					pcx.fatalError("int型の変数を配列型として扱っています.");
				} else if (ident.getCType().getType() == CType.T_pint) {
					pcx.fatalError("pint型の変数を配列型として扱っています.");
				}
			}
			if (call != null) {
				if (call.isConstant()) {
					pcx.fatalError("定数には代入できません.");
				}
			}
			setConstant(ident.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; variable starts");
		if (ident != null) { ident.codeGen(pcx);}
		if (array != null) { array.codeGen(pcx);}
		if (call != null) { call.codeGen(pcx);}
		o.println(";;; variable completes");
	}
}