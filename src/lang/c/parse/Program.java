package lang.c.parse;

import java.io.PrintStream;
import java.util.ArrayList;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class Program extends CParseRule {

	// program ::= { declaraiton } { function } EOF

	private CParseRule decl, function;
	private ArrayList<CParseRule> declList, functionList;

	public Program(CParseContext pcx) {
		declList = new ArrayList<CParseRule>();
		functionList = new ArrayList<CParseRule>();
	}

	public static boolean isFirst(CToken tk) {
		return Declaration.isFirst(tk) || Function.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		while(Declaration.isFirst(tk)) {
			decl = new Declaration(pcx);
			decl.parse(pcx);
			declList.add(decl);
			tk = ct.getCurrentToken(pcx);
		}

		while(Function.isFirst(tk)) {
			function = new Function(pcx);
			function.parse(pcx);
			functionList.add(function);
			tk = ct.getCurrentToken(pcx);
		}

		if (tk.getType() != CToken.TK_EOF) {
			pcx.fatalError(tk.toExplainString() + "プログラムの最後にゴミがあります");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		try {
			for (CParseRule decl : declList) {
				if ( decl != null ) { decl.semanticCheck(pcx); }
			}
			for (CParseRule function : functionList) {
				if (function != null) { function.semanticCheck(pcx); }
			}
		} catch (NullPointerException e) {
			throw(e);
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();

		o.println(";;; program starts");
		o.println("\t. = 0x100");
		o.println("\tJMP\t__START\t\t; ProgramNode: 最初の実行文へ");
		o.println("__START:");
		o.println("\tMOV\t#0x1000, R6\t; ProgramNode: 計算用スタック初期化");

		for (CParseRule declation : declList) {
			declation.codeGen(pcx);
		}

		for (CParseRule func : functionList) {
			func.codeGen(pcx);
		}

		o.println("\tMOV\t-(R6), R0\t; ProgramNode: 計算結果確認用");
		o.println("\tHLT\t\t\t\t; ProgramNode:");
		o.println("\t.END\t\t\t; ProgramNode:");
		o.println(";;; program completes");
	}
}
