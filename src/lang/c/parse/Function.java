package lang.c.parse;

import java.io.PrintStream;
import java.util.ArrayList;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CSymbolTable;
import lang.c.CSymbolTableEntry;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class Function extends CParseRule{

	// 	function ::= FUNC ( INT [ MULT ] | VOID ) IDENT LPAR [ argList ] RPAR declblock

	private CParseRule argList, declblock;
	private CType funcType;
	private CToken ident;
	private CSymbolTableEntry cSymbolTableEntry;
	private ArrayList<CType> argTypeList;

	public Function(CParseContext pcx) {
		argTypeList = new ArrayList<CType>();
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_FUNC;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);
		CSymbolTable cst = pcx.getTable();

		cst.setupLocalSymbolTable();

		if (tk.getType() == CToken.TK_INT) {
			tk = ct.getNextToken(pcx);
			funcType = CType.getCType(CType.T_int);

			if (tk.getType() == CToken.TK_MULT) {
				tk = ct.getNextToken(pcx);
				funcType = CType.getCType(CType.T_pint);
			}

		} else if (tk.getType() == CToken.TK_VOID) {
			tk = ct.getNextToken(pcx);
			funcType = CType.getCType(CType.T_void);
		} else {
			pcx.fatalError(tk.toExplainString() + "型が指定されていません.");
		}

		if (tk.getType() == CToken.TK_IDENT) {
			ident = tk;
			tk = ct.getNextToken(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "識別子がありません.");
		}

		if (tk.getType() == CToken.TK_LPAR) {
			tk = ct.getNextToken(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "'('がありません.");
		}

		if (ArgList.isFirst(tk)) {
			argList = new ArgList(pcx, argTypeList);
			argList.parse(pcx);
		}

		tk = ct.getCurrentToken(pcx);

		if (tk.getType() == CToken.TK_RPAR) {
			tk = ct.getNextToken(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "')'がありません.");
		}

		cSymbolTableEntry = cst.searchFunc(ident.getText());

		if(cSymbolTableEntry == null) {
			pcx.fatalError(tk.toExplainString() + "この関数は定義されていません.");
		}

		if (Declblock.isFirst(tk)) {
			declblock = new Declblock(pcx,ident);
			declblock.parse(pcx);
//			cst.showTable();
		} else {
			pcx.fatalError(tk.toExplainString() + "関数の内部がありません");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (funcType != cSymbolTableEntry.getType()) {
			pcx.fatalError("プロトタイプ宣言の型と関数定義の型が異なります.");
		}

		if  ((argTypeList.size() != cSymbolTableEntry.getList().size())) {
			pcx.fatalError("プロトタイプ宣言の引数の個数と関数定義の引数の個数が異なります.");
		}

		int i = 0;
		for (CType argType : argTypeList) {
			if (argType != cSymbolTableEntry.getList().get(i)) {
				pcx.fatalError("プロトタイプ宣言の型と関数定義の型が異なります.");
			}
			i++;
		}

		if (declblock != null) {
			declblock.semanticCheck(pcx);
			if (funcType != declblock.getCType()) {
				pcx.fatalError("関数定義の型と返り値の型が異なります.");
			}
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; function starts");
		o.print(ident.getText() + ":  ");
		if (declblock != null) { declblock.codeGen(pcx);}
		o.println("R_" + ident.getText() + ":MOV R4, R6\t; Function: フレームポインタを戻す.");
		o.println("\tMOV\t-(R6), R4\t; Function:");
		o.println("\tRET\t\t\t\t; Function: 関数の終了.");
		o.println(";;; function completes");
	}
}
