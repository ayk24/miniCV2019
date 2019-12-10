package lang.c.parse;

import java.util.ArrayList;

import lang.FatalErrorException;
import lang.RecoverableErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CSymbolTable;
import lang.c.CSymbolTableEntry;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class VoidDecl extends CParseRule {

	// voidDecl ::= VOID IDENT LPAR [ typelist ] RPAR { COMMA IDENT LPAR [ typeList ] RPAR } SEMI

	private ArrayList<CParseRule> typeList;
	private CParseRule typelist;
	private CSymbolTableEntry cSymbolTableEntry;

	private String name = null;
	private CType type = CType.getCType(CType.T_void);
	private int size = 0;
	private boolean constp = true;

	public VoidDecl(CParseContext pcx) {
		typeList = new ArrayList<CParseRule>();
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_VOID;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);
		CSymbolTable cSymbolTable = pcx.getTable();

		try {
			if (tk.getType() == CToken.TK_IDENT) {
				name = tk.getText();
				tk = ct.getNextToken(pcx);
			} else {
				pcx.recoverableError(tk.toExplainString() + "識別子がありません.");
			}

			if (tk.getType() == CToken.TK_LPAR) {
				tk = ct.getNextToken(pcx);
			} else {
				pcx.warning(tk.toExplainString() + "'('がないので補いました.");
			}

			if (TypeList.isFirst(tk)) {
				typelist = new TypeList(pcx);
				typelist.parse(pcx);
				typeList.add(typelist);
			}

			tk = ct.getCurrentToken(pcx);

			if (tk.getType() == CToken.TK_RPAR) {
				tk = ct.getNextToken(pcx);
			} else {
				pcx.warning(tk.toExplainString() + "')'がないので補いました.");
			}
			cSymbolTableEntry = cSymbolTable.registerTable(name, type, size, constp);

			if (cSymbolTableEntry == null) {
				pcx.warning(name + "は既に定義されています.");
			}

			if (typelist != null) {
				cSymbolTableEntry.setList(((TypeList)typelist).getList());
				typelist = null;
			}
			while (tk.getType() == CToken.TK_COMMA) {
				tk = ct.getNextToken(pcx);

				if (tk.getType() == CToken.TK_IDENT) {
					name = tk.getText();
					tk = ct.getNextToken(pcx);
				} else {
					pcx.recoverableError(tk.toExplainString() + "識別子がありません.");
				}

				if (tk.getType() == CToken.TK_LPAR) {
					tk = ct.getNextToken(pcx);
				} else {
					pcx.warning(tk.toExplainString() + "'('がないので補いました.");
				}

				if (TypeList.isFirst(tk)) {
					typelist = new TypeList(pcx);
					typelist.parse(pcx);
					typeList.add(typelist);
				}

				tk = ct.getCurrentToken(pcx);

				if (tk.getType() == CToken.TK_RPAR) {
					tk = ct.getNextToken(pcx);
				} else {
					pcx.warning(tk.toExplainString() + "')'がないので補いました.");
				}

				cSymbolTableEntry = cSymbolTable.registerTable(name, type, size, constp);

				if (cSymbolTableEntry == null) {
					pcx.warning(name + "は既に定義されています.");
				}

				if (typelist != null) {
					cSymbolTableEntry.setList(((TypeList)typelist).getList());
					typelist = null;
				}
			}
			if (tk.getType() == CToken.TK_SEMI) {
				tk = ct.getNextToken(pcx);
			} else {
				pcx.warning(tk.toExplainString() + "';'がないので補いました.");
			}
		} catch (RecoverableErrorException e) {
			ct.skipTo(pcx, CToken.TK_SEMI, CToken.TK_RCUR);
			tk = ct.getNextToken(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {

	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
	}
}