package lang.c.parse;

import java.util.ArrayList;

import lang.FatalErrorException;
import lang.RecoverableErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class ArgList extends CParseRule {

	// arglist ::= argItem { COMMA argItem }

	private CParseRule argItem;
	private ArrayList<CType> cTypeList;
	private ArrayList<CParseRule> argItemList;

	public ArgList(CParseContext pcx, ArrayList<CType> list) {
		cTypeList = list;
		argItemList = new ArrayList<CParseRule>();
	}

	public static boolean isFirst(CToken tk) {
		return ArgItem.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if (ArgItem.isFirst(tk)) {
			argItem = new ArgItem(pcx, cTypeList);
			argItem.parse(pcx);
			argItemList.add(argItem);
		}

		tk = ct.getCurrentToken(pcx);

		while (tk.getType() == CToken.TK_COMMA) {
			try {
				tk = ct.getNextToken(pcx);

				if (ArgItem.isFirst(tk)) {
					argItem = new ArgItem(pcx, cTypeList);
					argItem.parse(pcx);
					argItemList.add(argItem);
				} else {
					pcx.recoverableError(tk.toExplainString() + "引数がありません.");
				}
			} catch (RecoverableErrorException e) {
				ct.skipTo(pcx, CToken.TK_SEMI, CToken.TK_RCUR);
				tk = ct.getNextToken(pcx);
			}

			tk = ct.getCurrentToken(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
	}
}