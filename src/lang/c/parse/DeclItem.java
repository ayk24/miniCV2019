package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CSymbolTableEntry;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class DeclItem extends CParseRule
{
    // declItem ::= [ MULT ] IDENT [ LBRA NUM RBRA]

	private CToken ident;
    private CToken number;
    boolean isPointer = false;
    boolean isArray = false;

    public DeclItem(CParseContext pcx) {
    }
    public static boolean isFirst(CToken tk) {
        return tk.getType() == CToken.TK_MULT || tk.getType() == CToken.TK_IDENT;
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        CTokenizer ct = pcx.getTokenizer();
        CToken tk = ct.getCurrentToken(pcx);

        if (tk.getType() == CToken.TK_MULT) {
            isPointer = true;
            tk = ct.getNextToken(pcx);
        }

        if (tk.getType() == CToken.TK_IDENT) {
            if (pcx.getTable().checkTable(tk.getText()) != null) {
                pcx.fatalError(tk.toExplainString() + "既に登録されています.");
            }
            ident = tk;
            tk = ct.getNextToken(pcx);
        } else {
            pcx.fatalError("'IDENT'が来ます.");
        }

        if (tk.getType() == CToken.TK_LBRA) {
            isArray = true;
            tk = ct.getNextToken(pcx);
            if (tk.getType() == CToken.TK_NUM) {
                number = tk;
                tk = ct.getNextToken(pcx);
            }
            if (tk.getType() != CToken.TK_RBRA) {
                pcx.fatalError("']'が来ます.");
            }
            tk = ct.getNextToken(pcx);
        }

        if (isPointer == true && isArray == true) {
            this.setCType(CType.getCType(CType.T_parray));
            pcx.getTable().addToTable(ident.getText(), new CSymbolTableEntry(getCType(), number.getIntValue(), false, true, 0));
        } else if (isPointer == true && isArray == false) {
            this.setCType(CType.getCType(CType.T_pint));
            pcx.getTable().addToTable(ident.getText(), new CSymbolTableEntry(getCType(), 0, false, true, 0));
        } else if (isPointer == false && isArray == true) {
            this.setCType(CType.getCType(CType.T_iarray));
            pcx.getTable().addToTable(ident.getText(), new CSymbolTableEntry(getCType(), number.getIntValue(), false, true, 0));
        } else if (isPointer == false && isArray == false) {
            this.setCType(CType.getCType(CType.T_int));
            pcx.getTable().addToTable(ident.getText(), new CSymbolTableEntry(getCType(), 1, false, true, 0));
        }
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; declItem starts");
        if(ident != null && isArray == false) {
            o.println(ident.getText() + ":\t .WORD 0 \t\t; declItem: 変数用の領域を確保");
        }
        if (ident != null && isArray == true) {
            o.println(ident.getText() + ":\t .BLKW " + number.getIntValue() + "\t\t; declItem: 変数用の領域を確保");
        }
        o.println(";;; declItem completes");
    }
}
