package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CSymbolTable;
import lang.c.CSymbolTableEntry;
import lang.c.CToken;
import lang.c.CTokenizer;

public class Ident extends CParseRule {

	// ident ::= IDENT

	private CToken ident;
	private CSymbolTableEntry cSymbolTableEntry;

	public Ident(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_IDENT;
	}
	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		CSymbolTable cSymbolTable = pcx.getTable();

		ident = tk;
		cSymbolTableEntry = cSymbolTable.search(ident.getText());

		if(cSymbolTableEntry == null) {
			pcx.warning(tk.toExplainString() + "この識別子は宣言されていません.");
		}

		tk = ct.getNextToken(pcx);
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		this.setCType(cSymbolTableEntry.getType());
		this.setConstant(cSymbolTableEntry.isConstp());
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; ident starts");
		if (ident != null) {
			if (cSymbolTableEntry.isGlobal()) {
				o.println("\tMOV\t#" + ident.getText() + ", (R6)+\t; Ident: 大域変数アドレスを積む.<"
						+ ident.toExplainString() + ">");
			} else {
				o.println("\tMOV\t#" + cSymbolTableEntry.getAddress() + ", R0\t\t; Ident: フレームポインタからの相対値を求める.");
				o.println("\tADD\tR4, R0\t\t; Ident: 相対アドレスを求める.");
				o.println("\tMOV\tR0, (R6)+\t; Ident: 局所変数アドレスを積む.<"
						+ ident.toExplainString() + ">");
			}
		}
		o.println(";;; ident completes");
	}
}