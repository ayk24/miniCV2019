package lang.c.parse;

import java.io.PrintStream;
import java.util.ArrayList;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class Program extends CParseRule {

	// program ::= { declaraiton } { statement } EOF

	private CParseRule statement;
	private ArrayList<CParseRule> statementList;

	private CParseRule decl;
	private ArrayList<CParseRule> declList;


	public Program(CParseContext pcx) {
		statementList = new ArrayList<CParseRule>();
		declList = new ArrayList<CParseRule>();
	}

	public static boolean isFirst(CToken tk) {
		return Declaration.isFirst(tk) ||
				Statement.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		// ここにやってくるときは、必ずisFirst()が満たされている
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		while(Declaration.isFirst(tk)) {
			decl = new Declaration(pcx);
			decl.parse(pcx);
			declList.add(decl);
			tk = ct.getCurrentToken(pcx);
		}

		while(Statement.isFirst(tk)) {
			statement = new Statement(pcx);
			statement.parse(pcx);
			statementList.add(statement);
			tk = ct.getCurrentToken(pcx);
		}

		if (tk.getType() != CToken.TK_EOF) {
			pcx.fatalError(tk.toExplainString() + "プログラムの最後にゴミがあります");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		for (CParseRule decl : declList) {
			if ( decl != null ) { decl.semanticCheck(pcx); }
		}
		for (CParseRule statement : statementList) {
			if (statement != null) { statement.semanticCheck(pcx); }
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; program starts");
		o.println("\t. = 0x100");
		o.println("\tJMP\t__START\t\t; ProgramNode: 最初の実行文へ");

		// ここには将来、宣言に対するコード生成が必要
		if (decl != null) {
			o.println(";;; 変数宣言");
			for (CParseRule declaration : declList) {
				declaration.codeGen(pcx);
			}
			o.println(";;; 変数宣言終了");
		}

		if (statement != null) {
			o.println("__START:");
			o.println("\tMOV\t#0x1000, R6\t; ProgramNode: 計算用スタック初期化");
			for (CParseRule program : statementList) {
				program.codeGen(pcx);
			}
			o.println("\tMOV\t-(R6), R0\t; ProgramNode: 計算結果確認用");
		}

		o.println("\tHLT\t\t\t\t; ProgramNode:");
		o.println("\t.END\t\t\t; ProgramNode:");
		o.println(";;; program completes");
	}
}
