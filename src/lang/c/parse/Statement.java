package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class Statement extends CParseRule {

    // statement ::= statementAssign

	private CParseRule statementAssign;

    public Statement(CParseContext pcx) {

    }
    public static boolean isFirst(CToken tk) {
        return StatementAssign.isFirst(tk);
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        CTokenizer ct = pcx.getTokenizer();
        CToken tk = ct.getCurrentToken(pcx);

        if(StatementAssign.isFirst(tk)) {
            statementAssign = new StatementAssign(pcx);
            statementAssign.parse(pcx);
        }
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
        if(statementAssign != null) {
            statementAssign.semanticCheck(pcx);
            this.setCType(statementAssign.getCType());
            this.setConstant(statementAssign.isConstant());
        }
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; statement starts");
        if (statementAssign != null) { statementAssign.codeGen(pcx); }
        o.println(";;; statement completes");
    }
}
