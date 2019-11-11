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

public class StatementCall extends CParseRule {

	// call ::= LPAR [ expression { COMMA expression } ] RPAR

	private int addrSize;
	private CParseRule ident, expression;
	private CToken idtk;
	private CSymbolTableEntry cSymbolTableEntry;
	private ArrayList<CParseRule> expressionList;

	public StatementCall(CParseContext pcx) {
		expressionList = new ArrayList<CParseRule>();
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_CALL;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);
		CSymbolTable cSymbolTable = pcx.getTable();

		if (Ident.isFirst(tk)) {
			idtk = tk;
			ident = new Ident(pcx);
			ident.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "識別子がありません.");
		}

		tk = ct.getCurrentToken(pcx);

		if (tk.getType() == CToken.TK_LPAR) {
			tk = ct.getNextToken(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "'('がありません.");
		}

		cSymbolTableEntry = cSymbolTable.searchFunc(idtk.getText());

		if (Expression.isFirst(tk)) {
			expression = new Expression(pcx);
			expression.parse(pcx);
			expressionList.add(expression);
			tk = ct.getCurrentToken(pcx);

			while (tk.getType() == CToken.TK_COMMA) {
				tk = ct.getNextToken(pcx);
				if (Expression.isFirst(tk)) {
					expression = new Expression(pcx);
					expression.parse(pcx);
					expressionList.add(expression);
				} else {
					pcx.fatalError(tk.toExplainString() + "引数がありません.");
				}
				tk = ct.getCurrentToken(pcx);
			}
		}

		if (tk.getType() == CToken.TK_RPAR) {
			tk = ct.getNextToken(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "')'がありません.");
		}

		if (tk.getType() == CToken.TK_SEMI) {
			tk = ct.getNextToken(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "';'がありません.");
		}
		addrSize = cSymbolTable.getAddrsize();
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (ident != null) {
			ident.semanticCheck(pcx);
		}

		if (expressionList.size() != cSymbolTableEntry.getList().size()) {
			pcx.fatalError("プロトタイプ宣言の引数の個数と関数呼び出し時の引数の個数が異なります.");
		}

		int i = 0;

		for (CParseRule expression : expressionList) {
			expression.semanticCheck(pcx);
			if (expression.getCType() != cSymbolTableEntry.getList().get(i)) {
				pcx.fatalError("プロトタイプ宣言の引数の型と関数呼び出し時の引数の型が異なります.");
			}
			i++;
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; statementCall starts");
		for (int i = expressionList.size() - 1; i >= 0; i--) {
			expressionList.get(i).codeGen(pcx);
		}
		o.println("\tMOV\t#" + addrSize +", R0\t\t; StatementCall: フレームポインタからの相対値を求める.");
		o.println("\tADD\tR4, R0\t\t; StatementCall: 相対アドレスを求める.");
		o.println("\tMOV\tR0, (R6)+\t; StatementCall: 戻り番地をスタックに積む.");
		o.println("\tJSR " + idtk.getText() + "\t\t\t; StatementCall: 関数を呼び出す.");
		o.println("\tSUB\t#" + expressionList.size() + ", R6\t\t; StatementCall: 引数をスタックから降ろす.");
		o.println("\tMOV\t-(R6), R1\t; StatementCall: アドレスを取り出す.");
		o.println("\tMOV\tR0, (R1)\t; StatementCall: 返り値を入れる.");
		o.println("\tMOV\tR1, (R6)+\t; StatementCall: 変数のアドレスを積む.");

		o.println(";;; statementCall completes");
	}
}