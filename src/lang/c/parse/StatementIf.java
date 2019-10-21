package lang.c.parse;

import java.io.PrintStream;
import java.util.ArrayList;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class StatementIf extends CParseRule {

	// statementIf ::= IF LPAR condition RPAR LCUR { statement } RCUR { ELSEIF LPAR condition RPAR LCUR { statement } RCUR } [ ELSE LCUR statement RCUR ]

	private CParseRule condition, statement;
	private int i,seq;
	private ArrayList<CParseRule> ifConditionList, elseifConditionList, ifStateList, elseifStateList, elseStateList;

	public StatementIf(CParseContext pcx) {
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_IF;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);

		ifConditionList = new ArrayList<CParseRule>();
		elseifConditionList = new ArrayList<CParseRule>();
		ifStateList = new ArrayList<CParseRule>();
		elseifStateList = new ArrayList<CParseRule>();
		elseStateList = new ArrayList<CParseRule>();


		if (tk.getType() == CToken.TK_LPAR) {
			tk = ct.getNextToken(pcx);

			if (Condition.isFirst(tk)) {
				condition = new Condition(pcx);
				condition.parse(pcx);
				ifConditionList.add(condition);
				tk = ct.getCurrentToken(pcx);

				if (tk.getType() == CToken.TK_RPAR) {
					tk = ct.getNextToken(pcx);

					if (tk.getType() == CToken.TK_LCUR) {
						tk = ct.getNextToken(pcx);

						while (Statement.isFirst(tk)) {
							statement = new Statement(pcx);
							statement.parse(pcx);
							ifStateList.add(statement);
							tk = ct.getCurrentToken(pcx);
						}

						if (tk.getType() == CToken.TK_RCUR) {
							tk = ct.getNextToken(pcx);
						} else {
							pcx.fatalError("'statement'のあとは'}'が来ます.");
						}

					} else {
						pcx.fatalError("')'のあとは'{'が来ます.");
					}
				} else {
					pcx.fatalError("'condition'のあとは')'が来ます.");
				}
			} else {
				pcx.fatalError("'('のあとは'condition'が来ます.");
			}
		} else {
			pcx.fatalError("'if'のあとは'('が来ます.");
		}

		while (tk.getType() == CToken.TK_ELSEIF) {
			tk = ct.getNextToken(pcx);
			if (tk.getType() == CToken.TK_LPAR) {
				tk = ct.getNextToken(pcx);

				if (Condition.isFirst(tk)) {
					condition = new Condition(pcx);
					condition.parse(pcx);
					elseifConditionList.add(condition);
					tk = ct.getCurrentToken(pcx);

					if (tk.getType() == CToken.TK_RPAR) {
						tk = ct.getNextToken(pcx);

						if (tk.getType() == CToken.TK_LCUR) {
							tk = ct.getNextToken(pcx);

							while (Statement.isFirst(tk)) {
								statement = new Statement(pcx);
								statement.parse(pcx);
								elseifStateList.add(statement);
								tk = ct.getCurrentToken(pcx);
							}

							if (tk.getType() == CToken.TK_RCUR) {
								tk = ct.getNextToken(pcx);
							} else {
								pcx.fatalError("'statement'のあとは'}'が来ます.");
							}
						} else {
							pcx.fatalError("')'のあとは'{'が来ます.");
						}
					} else {
						pcx.fatalError("'condition'のあとは')'が来ます.");
					}
				} else {
					pcx.fatalError("'('のあとは'condition'が来ます.");
				}
			} else {
				pcx.fatalError("'elseif'のあとは'('が来ます.");
			}
		}

		if (tk.getType() == CToken.TK_ELSE) {
			tk = ct.getNextToken(pcx);

			if (tk.getType() == CToken.TK_LCUR) {
				tk = ct.getNextToken(pcx);

				while (Statement.isFirst(tk)) {
					statement = new Statement(pcx);
					statement.parse(pcx);
					elseStateList.add(statement);
					tk = ct.getCurrentToken(pcx);
				}

				if (tk.getType() == CToken.TK_RCUR) {
					tk = ct.getNextToken(pcx);
				} else {
					pcx.fatalError("'statement'のあとは'}'が来ます.");
				}

			} else {
				pcx.fatalError("'else'のあとは'{'が来ます.");
			}
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (condition != null) {
			condition.semanticCheck(pcx);
		}
		if (statement != null) {
			statement.semanticCheck(pcx);
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; statementIf starts");
		i = 0;
		seq = pcx.getSeqId();

		o.println("IF" + seq + ":\t\t\t\t; statementIf:");
		for (CParseRule condition : ifConditionList) {condition.codeGen(pcx);}
		o.println("\tMOV\t-(R6), R0\t; statementIf");
		o.println("\tCMP\t#0x0000, R0\t; statementIf");
		o.println("\tBRZ\tFALSE" + seq + "\t\t; statementIf");
		o.println("TRUE" + seq + ":\t\t\t\t; statementIf");
		for (CParseRule statement : ifStateList) { statement.codeGen(pcx); }
		o.println("\tJMP\tENDIF" + seq + "\t\t; statementIf:");


		for (CParseRule condition : elseifConditionList) {
			i++;
			o.println("IF" + (seq+i) + ":\t\t\t\t; statementIf:");
			condition.codeGen(pcx);
			o.println("\tMOV\t-(R6), R0\t; statementIf");
			o.println("\tCMP\t#0x0000, R0\t; statementIf");
			o.println("\tBRZ\tFALSE" + seq + "\t\t; statementIf");
			o.println("TRUE" + (seq+i) + ":\t\t\t\t; statementIf");
			elseifStateList.get(i-1).codeGen(pcx);
			o.println("\tJMP\tENDIF" + seq + "\t\t; statementIf:");
		}

		o.println("FALSE" + seq + ":\t\t\t\t; statementIf:");
		for (CParseRule statement :elseStateList) { statement.codeGen(pcx); }
		o.println("ENDIF" + seq + ":\t\t\t\t; statementIf:");
		o.println(";;; statementIf completes");
	}
}
