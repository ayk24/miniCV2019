package lang.c.parse;

import java.io.PrintStream;
import java.util.ArrayList;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class StatementWhile extends CParseRule {

	// statementWhile ::= While LPAR condition RPAR LCUR { statement } RCUR

	private CParseRule condition, statement;
    private int seq;
    private ArrayList<CParseRule> statementList;

    public StatementWhile(CParseContext pcx) {
    }
    public static boolean isFirst(CToken tk) {
        return tk.getType() == CToken.TK_WHILE;
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        CTokenizer ct = pcx.getTokenizer();
        CToken tk = ct.getNextToken(pcx);
        statementList = new ArrayList<CParseRule>();

    	if (tk.getType() == CToken.TK_LPAR) {
			tk = ct.getNextToken(pcx);

			if (Condition.isFirst(tk)) {
				condition = new Condition(pcx);
				condition.parse(pcx);
				tk = ct.getCurrentToken(pcx);

				if (tk.getType() == CToken.TK_RPAR) {
					tk = ct.getNextToken(pcx);

					if (tk.getType() == CToken.TK_LCUR) {
						tk = ct.getNextToken(pcx);

						while (Statement.isFirst(tk)) {
							statement = new Statement(pcx);
							statement.parse(pcx);
							statementList.add(statement);
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
			pcx.fatalError("'WHILE'のあとは'('が来ます.");
		}
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if (condition != null) {
            condition.semanticCheck(pcx);
        }
        for ( CParseRule statement : statementList ) {
            if (statement != null) {
                statement.semanticCheck(pcx);
            }
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; statementWhile starts");
        seq = pcx.getSeqId();
        o.println("WHILE" + seq + ":\t\t\t\t; statementWhile:");
        if (condition != null) { condition.codeGen(pcx); }
        o.println("TRUE" + seq + ":\t\t\t\t; statementWhile");
        o.println("\tMOV\t-(R6), R0\t; statementWhile");
        o.println("\tCMP\t#0x0000, R0\t; statementWhile");
        o.println("\tBRZ\tFALSE" + seq + "\t\t; statementWhile");
        for (CParseRule statement : statementList) {
            statement.codeGen(pcx);
        }
        o.println("\tJMP\tWHILE" + seq + "\t\t; statementWhile:");
        o.println("FALSE" + seq + ":\t\t\t\t; statementWhile:");
        o.println(";;; statementWhile completes");
    }
}
