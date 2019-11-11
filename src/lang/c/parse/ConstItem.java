package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CSymbolTable;
import lang.c.CSymbolTableEntry;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class ConstItem extends CParseRule {

	// constItem ::= [ MULT ] IDENT ASSIGN [ AMP ] NUM

	private CSymbolTableEntry cSymbolTableEntry;

	private String name = null;
	private CType type;
	private int size = 1;
	private int addr = 0;
	private boolean constp = true;

	public ConstItem(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_MULT || tk.getType() == CToken.TK_IDENT;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		CSymbolTable cSymbolTable = pcx.getTable();

		if (tk.getType() == CToken.TK_MULT) {
			type = CType.getCType(CType.T_pint);
			tk = ct.getNextToken(pcx);
		} else {
			type = CType.getCType(CType.T_int);
		}

		if (tk.getType() == CToken.TK_IDENT) {
			name = tk.getText();
			tk = ct.getNextToken(pcx);

			if (tk.getType() == CToken.TK_ASSIGN) {
				tk = ct.getNextToken(pcx);

				if (tk.getType() == CToken.TK_AMP) {
					if (type != CType.getCType(CType.T_pint)) {
						pcx.fatalError(tk.toExplainString() + "左辺と右辺の型が異なります.");
					}
					tk = ct.getNextToken(pcx);
				} else {
					if (type != CType.getCType(CType.T_int)) {
						pcx.fatalError(tk.toExplainString() + "左辺と右辺の型が異なります.");
					}
				}

				if (tk.getType() == CToken.TK_NUM) {
					size = tk.getIntValue();
					tk = ct.getNextToken(pcx);
				} else {
					pcx.fatalError(tk.toExplainString() + "'='の後に定数がありません.");
				}
			} else {
				pcx.fatalError(tk.toExplainString() + "識別子の後に'='がありません.");
			}
		} else {
			pcx.fatalError(tk.toExplainString() + "'*'の後に識別子がありません.");
		}

		addr = cSymbolTable.getAddrsize();
		cSymbolTableEntry = cSymbolTable.registerTable(name, type, size, constp);

		if (cSymbolTableEntry == null) {
			pcx.fatalError(name + "は既に定義されています.");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; constItem starts");
		if (cSymbolTableEntry.isGlobal()) {
			o.println(name + ":\t.WORD\t" + size + "\t; ConstItem: 定数の領域確保");
		} else {
			o.println("\tMOV\tR4, R0\t; ConstItem: 局所定数の初期化");
			o.println("\tADD\t#" + addr +", R0\t; ConstItem:");
			o.println("\tMOV\t#" + size + ", (R0)\t; ConstItem:");
		}
		o.println(";;; constItem completes");
	}
}