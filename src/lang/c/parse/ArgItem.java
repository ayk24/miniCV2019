package lang.c.parse;

import java.util.ArrayList;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CSymbolTable;
import lang.c.CSymbolTableEntry;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class ArgItem extends CParseRule {

	// argItem ::= INT [ MULT ] IDENT [ LBRA RBRA ]

	private ArrayList<CType> cTypeList;
	private CSymbolTableEntry cSymbolTableEntry;

	private String name = null;
	private CType cType;
	private int size = 0;
	private boolean constp = false;

	public ArgItem(CParseContext pcx, ArrayList<CType> list) {
		this.cTypeList = list;
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_INT;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);
		CSymbolTable cSymbolTable = pcx.getTable();

		cType = CType.getCType(CType.T_int);

		if (tk.getType() == CToken.TK_MULT) {
			cType = CType.getCType(CType.T_pint);
			tk = ct.getNextToken(pcx);
		}

		if (tk.getType() == CToken.TK_IDENT) {
			name = tk.getText();
			tk = ct.getNextToken(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "識別子がありません.");
		}

		if (tk.getType() == CToken.TK_LBRA) {
			tk = ct.getNextToken(pcx);

			if (tk.getType() == CToken.TK_RBRA) {
				if (cType == CType.getCType(CType.T_pint)) {
					cType = CType.getCType(CType.T_parray);
				} else if (cType == CType.getCType(CType.T_int)) {
					cType = CType.getCType(CType.T_iarray);
				}
				tk = ct.getNextToken(pcx);
			} else {
				pcx.fatalError(tk.toExplainString() + "']'がありません.");
			}
		}

		cTypeList.add(cType);
		cSymbolTableEntry = cSymbolTable.registerTable(name, cType, size, constp);

		if (cSymbolTableEntry == null) {
			pcx.fatalError(name + "は既に登録されています.");
		}

		cSymbolTable.setfd(name);
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
	}

}