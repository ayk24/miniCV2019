package lang.c.parse;

import java.util.ArrayList;

import lang.FatalErrorException;
import lang.RecoverableErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class IntDecl extends CParseRule {

	// intDecl ::= INT declItem { COMMA declItem } SEMI

	private ArrayList<DeclItem> declItemList;
	private DeclItem declItem;

	public IntDecl(CParseContext pcx) {
		declItemList = new ArrayList<DeclItem>();

	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_INT;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);
		try {
			if (tk.getType() == CToken.TK_INT) {
				tk = ct.getNextToken(pcx);

				if(DeclItem.isFirst(tk)) {
					declItem = new DeclItem(pcx);
					declItem.parse(pcx);
					declItemList.add(declItem);
				} else {
					pcx.recoverableError(tk.toExplainString() + "識別子がありません.");
				}

				tk = ct.getCurrentToken(pcx);

				while(tk.getType() == CToken.TK_COMMA) {
					tk = ct.getNextToken(pcx);
					if (DeclItem.isFirst(tk)) {
						declItem = new DeclItem(pcx);
						declItem.parse(pcx);
						declItemList.add(declItem);
					} else {
						pcx.recoverableError(tk.toExplainString() + "連続して宣言する識別子がありません.");
					}
					tk = ct.getCurrentToken(pcx);
				}

				if (tk.getType() == CToken.TK_SEMI) {
					tk = ct.getNextToken(pcx);
				} else {
					pcx.warning(tk.toExplainString() + "';'がなかったので補いました.");
				}
			}
		} catch (RecoverableErrorException e) {
			ct.skipTo(pcx, CToken.TK_SEMI, CToken.TK_RCUR);
			tk = ct.getNextToken(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		for (CParseRule declItem : declItemList) {
			declItem.codeGen(pcx);
		}
	}
}