package lang.c.parse;

import java.io.PrintStream;
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

public class Call extends CParseRule{

	// call ::= LPAR [ expression { COMMA expression } ] RPAR

	private int addrSize;
	private CParseRule expression;
	private CToken idtk;
	private CSymbolTableEntry cSymbolTableEntry;
	private ArrayList<CParseRule> expressionlist;


	public Call(CParseContext pcx, CToken idtk) {
		this.idtk = idtk;
		expressionlist = new ArrayList<CParseRule>();
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_LPAR;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);
		CSymbolTable cSymbolTable = pcx.getTable();

		cSymbolTableEntry = cSymbolTable.search(idtk.getText());

		try {
			if (Expression.isFirst(tk)) {
				expression = new Expression(pcx);
				expression.parse(pcx);
				expressionlist.add(expression);
				tk = ct.getCurrentToken(pcx);

				while (tk.getType() == CToken.TK_COMMA) {
					tk = ct.getNextToken(pcx);

					if (Expression.isFirst(tk)) {
						expression = new Expression(pcx);
						expression.parse(pcx);
						expressionlist.add(expression);
					} else {
						pcx.recoverableError(tk.toExplainString() + "引数がありません.");
					}

					tk = ct.getCurrentToken(pcx);
				}
			}

			if (tk.getType() == CToken.TK_RPAR) {
				ct.getNextToken(pcx);
			} else {
				pcx.warning(tk.toExplainString() + "')'がないので補いました.");
			}

			for (int i = 0; i < 100; i++) {
				if(cSymbolTable.search(idtk.getText() + "_a" + String.valueOf(i)) == null) {
					cSymbolTable.registerTable(idtk.getText() + "_a" + String.valueOf(i), CType.getCType(CType.T_int), 1, true);
					break;
				}
			}
			addrSize = cSymbolTable.getAddrsize();
		} catch (RecoverableErrorException e) {
			ct.skipTo(pcx, CToken.TK_SEMI, CToken.TK_RCUR);
			tk = ct.getNextToken(pcx);
		}

	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (expressionlist.size() != cSymbolTableEntry.getList().size()) {
			pcx.warning("引数の個数が違います.");
		}

		int i = 0;
		for (CParseRule expression : expressionlist) {
			expression.semanticCheck(pcx);
			if (expression.getCType() != cSymbolTableEntry.getList().get(i)) {
				pcx.warning("引数の型が違います.");
			}
			i++;
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; call starts");
		for (int i = expressionlist.size() - 1; i >= 0; i--) {
			expressionlist.get(i).codeGen(pcx);
		}

		o.println("\tMOV\t#" + addrSize +", R0\t\t; Call: フレームポインタからの相対値を求める.");
		o.println("\tADD\tR4, R0\t\t; Call: 相対アドレスを求める.");
		o.println("\tMOV\tR0, (R6)+\t; Call: 戻り番地をスタックに積む.");
		o.println("\tJSR "+ idtk.getText() +"\t\t\t; Call: 関数を呼び出す.");
		o.println("\tSUB\t#" + expressionlist.size() + ", R6\t\t; Call: 引数をスタックから降ろす.");
		o.println("\tMOV\t-(R6), R1\t; Call: アドレスを取り出す.");
		o.println("\tMOV\tR0, (R1)\t; Call: 返り値を入れる.");
		o.println("\tMOV\tR1, (R6)+\t; Call: 変数のアドレスを積む.");
		o.println(";;; call completes");
	}
}