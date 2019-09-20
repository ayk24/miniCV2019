package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class Declaration extends CParseRule {

    // declaraiton ::= intDecl | constDecl

	private CParseRule intDecl;
    private CParseRule constDecl;

    public Declaration(CParseContext pcx) {
    }

    public static boolean isFirst(CToken tk) {
        return IntDecl.isFirst(tk) || ConstDecl.isFirst(tk);
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        CTokenizer ct = pcx.getTokenizer();
        CToken tk = ct.getCurrentToken(pcx);

        if (IntDecl.isFirst(tk)) {
            intDecl = new IntDecl(pcx);
            intDecl.parse(pcx);
        } else if (ConstDecl.isFirst(tk)) {
            constDecl = new ConstDecl(pcx);
            constDecl.parse(pcx);
        }
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; declaration starts");
        if (intDecl != null) {intDecl.codeGen(pcx);}
        if (constDecl != null) {constDecl.codeGen(pcx);}
        o.println(";;; declaration completes");
    }
}
