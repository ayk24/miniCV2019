package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.RecoverableErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CSymbolTable;
import lang.c.CSymbolTableEntry;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class DeclItem extends CParseRule {

	// declItem ::= [ MULT ] IDENT [ LBRA NUMBER RBRA | LPAR [ typeList ] RPAR ]

	private CParseRule typelist;
	private CSymbolTableEntry cSymbolTableEntry;

	private String name = null;
	private CType type;
	private int size = 1;
	private boolean constp = false;

	public DeclItem(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_MULT || tk.getType() == CToken.TK_IDENT;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		CSymbolTable cst = pcx.getTable();

		if (tk.getType() == CToken.TK_MULT) {
			type = CType.getCType(CType.T_pint);
			tk = ct.getNextToken(pcx);
		} else {
			type = CType.getCType(CType.T_int);
		}

		try {
			if (tk.getType() == CToken.TK_IDENT) {
				name = tk.getText();
				tk = ct.getNextToken(pcx);
			} else {
				pcx.recoverableError(tk.toExplainString() + "識別子がありません.");
			}

			if (tk.getType() == CToken.TK_LBRA) {
				tk = ct.getNextToken(pcx);

				if (tk.getType() == CToken.TK_NUM) {
					size = tk.getIntValue();
					tk = ct.getNextToken(pcx);

					if (tk.getType() == CToken.TK_RBRA) {
						tk = ct.getNextToken(pcx);
					} else {
						pcx.warning(tk.toExplainString() + "要素数の後に']'がないので補いました.");
					}
				} else {
					pcx.recoverableError(tk.toExplainString() + "'['の後に要素数が指定されていません");
				}


				if (type == CType.getCType(CType.T_pint)) {
					type = CType.getCType(CType.T_parray);
				} else {
					type = CType.getCType(CType.T_iarray);
				}
			} else if (tk.getType() == CToken.TK_LPAR) {
				tk = ct.getNextToken(pcx);

				if (TypeList.isFirst(tk)) {
					typelist = new TypeList(pcx);
					typelist.parse(pcx);
				}

				tk = ct.getCurrentToken(pcx);

				if (tk.getType() == CToken.TK_RPAR) {
					size = 0;
					constp = true;
					tk = ct.getNextToken(pcx);
				} else {
					pcx.warning(tk.toExplainString() + "')'が無いので補いました.");
				}
			}
			cSymbolTableEntry = cst.registerTable(name, type, size, constp);

			if (cSymbolTableEntry == null) {
				pcx.warning(name + "は既に定義されています.");
			}

			if (typelist != null) {
				cSymbolTableEntry.setList(((TypeList)typelist).getList());
			}
		} catch (RecoverableErrorException e) {
			ct.skipTo(pcx, CToken.TK_SEMI, CToken.TK_RCUR);
			tk = ct.getNextToken(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; declItem starts");
		if (cSymbolTableEntry.isGlobal() && cSymbolTableEntry.getSize() != 0) {
			if (type == CType.getCType(CType.T_int) || type == CType.getCType(CType.T_pint)) {
				o.println(name + ":\t.WORD\t0\t	; DeclItem: 変数の領域を確保する.");
			} else {
				o.println(name + ":\t.BLKW\t" + size + "\t	; DeclItem: 変数の領域を確保する.");
			}
		}
		o.println(";;; declItem completes");
	}
}