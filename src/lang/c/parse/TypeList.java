package lang.c.parse;

import java.util.ArrayList;

import lang.FatalErrorException;
import lang.RecoverableErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class TypeList extends CParseRule {

	// typeList ::= typeItem { COMMA typeItem }

	private ArrayList<CType> cTypeList;
	private ArrayList<CParseRule> typeItemList;
	private CParseRule typeitem;

	public TypeList(CParseContext pcx) {
		cTypeList = new ArrayList<CType>();
		typeItemList = new ArrayList<CParseRule>();
	}

	public static boolean isFirst(CToken tk) {
		return TypeItem.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		if (TypeItem.isFirst(tk)) {
			typeitem = new TypeItem(pcx, cTypeList);
			typeitem.parse(pcx);
			typeItemList.add(typeitem);
		}

		tk = ct.getCurrentToken(pcx);

		while (tk.getType() == CToken.TK_COMMA) {
			tk = ct.getNextToken(pcx);
			try {
				if (TypeItem.isFirst(tk)) {
					typeitem = new TypeItem(pcx, cTypeList);
					typeitem.parse(pcx);
					typeItemList.add(typeitem);
				} else {
					pcx.recoverableError(tk.toExplainString() + "引数の型が指定されていません.");
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

	public ArrayList<CType> getList() {
		return cTypeList;
	}
}