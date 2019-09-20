package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class StatementAssign extends CParseRule {

	// statementAssign ::= primary ASSIGN expression SEMI

	private CParseRule primary;
	private CParseRule expression;

	public StatementAssign(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return Primary.isFirst(tk);
	}

	public void parse(CParseContext pcx) throws FatalErrorException {

		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getCurrentToken(pcx);

		primary = new Primary(pcx);
		primary.parse(pcx);
		tk = ct.getCurrentToken(pcx);

		if(tk.getType() != CToken.TK_ASSIGN) {
			pcx.fatalError("'='が来ます.");
		}

		tk = ct.getNextToken(pcx);

		if(Expression.isFirst(tk)) {
			expression = new Expression(pcx);
			expression.parse(pcx);
		}else{
			pcx.fatalError("右辺がありません.");
		}

		tk = ct.getCurrentToken(pcx);

		if(tk.getType() != CToken.TK_SEMI) {
			pcx.fatalError("';'が来ます.");
		}

		tk = ct.getNextToken(pcx);
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if(primary != null){
			primary.semanticCheck(pcx);
			if(primary.isConstant()){
				pcx.fatalError("定数には代入できません.");
			}
		}

		if(expression != null){
			expression.semanticCheck(pcx);
			this.setCType(expression.getCType());
			this.setConstant(expression.isConstant());
		}

		// debug
		// System.out.println(primary.getCType());
		// System.out.println(expression.getCType());

		if(primary.getCType() != expression.getCType()){
			pcx.fatalError("左辺と右辺の型が一致していません");
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; statementAssign starts");
		if ( primary != null ) { primary.codeGen(pcx); }
		if ( expression != null ) { expression.codeGen(pcx); }
		o.println("\tMOV\t-(R6), R0\t; StatementAssign: 右辺の値を取り出す");
		o.println("\tMOV\t-(R6), R1\t; StatementAssign: 左辺の値を取り出す");
		o.println("\tMOV\tR0, (R1)\t; StatementAssign: 右辺を左辺のアドレスに書き込む");
		o.println(";;; statementAssign completes");
	}
}
