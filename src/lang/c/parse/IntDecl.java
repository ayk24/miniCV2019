package lang.c.parse;

import java.io.PrintStream;
import java.util.ArrayList;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class IntDecl extends CParseRule {

	// intDecl ::= INT declItem { COMMA declItem } SEMI

	private CParseRule declItem;
	private ArrayList<CParseRule> intList;

	public IntDecl(CParseContext pcx) {
		intList = new ArrayList<CParseRule>();
	}
	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_INT;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if (tk.getType() == CToken.TK_INT) {
			tk = ct.getNextToken(pcx);

			if (DeclItem.isFirst(tk)) {
				declItem = new DeclItem(pcx);
				declItem.parse(pcx);
				intList.add(declItem);
				tk = ct.getCurrentToken(pcx);

				while (tk.getType() == CToken.TK_COMMA) {
					tk = ct.getNextToken(pcx);
					if (DeclItem.isFirst(tk)) {
						declItem = new DeclItem(pcx);
						declItem.parse(pcx);
						intList.add(declItem);
						tk = ct.getCurrentToken(pcx);
					} else {
						pcx.fatalError("','の後は'declItem'が来ます.");
					}
				}

				if (tk.getType() == CToken.TK_SEMI) {
					ct.getNextToken(pcx);
				} else {
					System.out.println(tk.toExplainString() + "文の終わりは';'です.");
				}
			}
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; intDecl starts");
		if (declItem != null) {
			for (CParseRule declItem : intList) {
				declItem.codeGen(pcx);
			}
		}
		o.println(";;; intDecl completes");
	}
}
