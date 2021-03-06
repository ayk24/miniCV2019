package lang.c.parse;

import java.io.PrintStream;
import java.util.ArrayList;

import lang.FatalErrorException;
import lang.RecoverableErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class ConstDecl extends CParseRule {

	// constDecl ::= CONST INT constItem { COMMA constItem } SEMI

	private CParseRule constItem;
	private ArrayList<CParseRule> constList;

	public ConstDecl(CParseContext pcx){
		constList = new ArrayList<CParseRule>();
	}
	public static boolean isFirst(CToken tk){
		return tk.getType() == CToken.TK_CONST;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		try {
			if(tk.getType() == CToken.TK_CONST){
				tk = ct.getNextToken(pcx);

				if(tk.getType() == CToken.TK_INT){
					tk = ct.getNextToken(pcx);

					if(ConstItem.isFirst(tk)){
						constItem = new ConstItem(pcx);
						constItem.parse(pcx);
						constList.add(constItem);
					} else {
						pcx.recoverableError(tk.toExplainString() + "宣言する識別子がありません.");
					}

					tk = ct.getCurrentToken(pcx);

					while (tk.getType() == CToken.TK_COMMA){
						tk = ct.getNextToken(pcx);

						if(ConstItem.isFirst(tk)){
							constItem = new ConstItem(pcx);
							constItem.parse(pcx);
							constList.add(constItem);
						} else {
							pcx.recoverableError(tk.toExplainString() + "','の次には'constItem'が来ます.");
						}
						tk = ct.getCurrentToken(pcx);
					}
				}else{
					pcx.warning(tk.toExplainString() + "'const'の次の'int'が無かったので補いました.");
				}

				if(tk.getType() == CToken.TK_SEMI){
					tk = ct.getNextToken(pcx);
				} else {
					pcx.fatalError("';'が無かったので補いました.");
				}
			}
		} catch (RecoverableErrorException e) {
			ct.skipTo(pcx, CToken.TK_SEMI, CToken.TK_RCUR);
			tk = ct.getNextToken(pcx);
		}


	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		for(CParseRule constItem : constList){
			constItem.semanticCheck(pcx);
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; constDecl starts");
		if(constItem != null){
			for(CParseRule constItem : constList){
				constItem.codeGen(pcx);
			}
		}
		o.println(";;; constDecl completes");
	}
}
