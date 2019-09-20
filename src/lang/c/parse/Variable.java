package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class Variable extends CParseRule{

	// variable ::= ident [ array ]

	private CParseRule ident;
	private CParseRule array;

	private CParseRule list;
	private CParseRule variable;

	public Variable(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return Ident.isFirst(tk);
	}


	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if(tk.getType() == CToken.TK_IDENT) {
			ident = new Ident(pcx);
			ident.parse(pcx);
			tk = ct.getCurrentToken(pcx);

			if(tk.getType() == CToken.TK_LBRA){
				list = new Array(pcx);
				list.parse(pcx);
				array = list;
				tk = ct.getCurrentToken(pcx);
			}
		}
		variable = ident;
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (variable != null) {
			variable.semanticCheck(pcx);
			if (array != null) {
				array.semanticCheck(pcx);
				if (variable.getCType().getType() == CType.T_iarray) {
					setCType(CType.getCType(CType.T_int));
				} else if (variable.getCType().getType() == CType.T_parray) {
					setCType(CType.getCType(CType.T_pint));
				} else if (variable.getCType().getType() == CType.T_err) {
					pcx.fatalError("identの識別子はintでなければなりません.");
				} else if (variable.getCType().getType() == CType.T_int) {
					pcx.fatalError("int型の変数を配列型として扱っています.");
				} else if (variable.getCType().getType() == CType.T_pint) {
					pcx.fatalError("pint型の変数を配列型として扱っています.");
				}
			} else {
				if (variable.getCType().getType() == CType.T_iarray || variable.getCType().getType() == CType.T_parray) {
					pcx.fatalError("配列には添え字が必要です.");
				}
				setCType(variable.getCType());
			}
			setConstant(variable.isConstant());
		}
	}


	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; variable starts");
		if(ident != null) { ident.codeGen(pcx); }
		if(array != null) {
			array.codeGen(pcx);
			o.println("\tMOV\t-(R6), R1\t; Variable : 添え字の値を持ってくる.");
			o.println("\tADD\t-(R6), R1\t; Variable : 変数アドレスの番地に添え字の数を足し, スタックに積む.");
			o.println("\tMOV\t R1, (R6)+\t; Variable :");
		}
		o.println(";;; variable completes");
	}
}
