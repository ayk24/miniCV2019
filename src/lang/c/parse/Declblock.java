package lang.c.parse;

import java.io.PrintStream;
import java.util.ArrayList;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CSymbolTable;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class Declblock extends CParseRule {

	// declblock ::= LCUR { declaration } { statement } RCUR

	private int addrSize;
	private CParseRule rule;
	private CType returnType = null;
	private CToken ident;
	private ArrayList<CParseRule> declarationList;
	private ArrayList<CParseRule> statementList;

	public Declblock(CParseContext pcx, CToken ct) {
		ident = ct;
		declarationList = new ArrayList<CParseRule>();
		statementList = new ArrayList<CParseRule>();
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_LCUR;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);
		CSymbolTable cst = pcx.getTable();

		while(Declaration.isFirst(tk)) {
			rule = new Declaration(pcx);
			rule.parse(pcx);
			declarationList.add(rule);
			tk = ct.getCurrentToken(pcx);
		}

		while(Statement.isFirst(tk)) {
			rule = new Statement(pcx, ident);
			rule.parse(pcx);
			statementList.add(rule);
			tk = ct.getCurrentToken(pcx);
		}

		if (tk.getType() == CToken.TK_RCUR) {
			ct.getNextToken(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "'}'がありません.");
		}

		addrSize = cst.getAddrsize();
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		for (CParseRule decl : declarationList) {
			decl.semanticCheck(pcx);
		}

		for (CParseRule statement : statementList) {
			statement.semanticCheck(pcx);
			if (((Statement)statement).checkStatementReturn()) {
				if (returnType != null) {
					if (statement.getCType() != returnType) {
						pcx.fatalError("返り値の型が異なっているものがあります.");
					}
				} else {
					returnType = statement.getCType();
					setCType(returnType);
				}
			} else if (((Statement)statement).checkStatementBranch()) {
				if (returnType != null) {
					if ((statement.getCType() != null) && statement.getCType() != returnType) {
						pcx.fatalError("返り値の型が異なっているものがあります.");
					}
				} else {
					returnType = statement.getCType();
					setCType(returnType);
				}
			}
		}
		if (returnType == null ) {
			setCType(CType.getCType(CType.T_void));
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();

//		o.println(";;; declblock starts");
		o.println("MOV R4, (R6)+\t; Declblock: フレームポインタをスタックに積む.");
		o.println("\tMOV\tR6, R4\t\t; Declblock: フレームポインタを保存する.");

		for (CParseRule decl : declarationList) {
			decl.codeGen(pcx);
		}
		o.println("\tADD\t#" + addrSize + ", R6\t\t; Declblock: スタックポインタを移動し, 局所変数用の領域を確保.");

		for (CParseRule statement : statementList) {
			statement.codeGen(pcx);
		}
//		o.println(";;; declblock completes");
	}
}