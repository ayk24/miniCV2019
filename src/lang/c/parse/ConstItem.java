package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CSymbolTableEntry;
import lang.c.CToken;
import lang.c.CTokenizer;
import lang.c.CType;

public class ConstItem extends CParseRule {

    // constItem ::= [ MULT ] IDENT ASSIGN [ AMP ] NUM

	private CToken ident;
    private CToken number;
    boolean isPointer = false;

    public ConstItem(CParseContext pcx){
    }
    public static boolean isFirst(CToken tk){
    	return tk.getType() == CToken.TK_MULT || tk.getType() == CToken.TK_IDENT;
    }

    public void parse(CParseContext pcx) throws FatalErrorException {
        CTokenizer ct = pcx.getTokenizer();
        CToken tk = ct.getCurrentToken(pcx);

        if(tk.getType() == CToken.TK_MULT){
            isPointer = true;
            tk = ct.getNextToken(pcx);
        }

        if(tk.getType() == CToken.TK_IDENT){
            if(pcx.getTable().checkTable(tk.getText()) != null){
                pcx.fatalError(tk.toExplainString() + "既に登録されています");
            }
            ident = tk;
            tk = ct.getNextToken(pcx);

            if(tk.getType() == CToken.TK_ASSIGN){
                tk = ct.getNextToken(pcx);

                if(isPointer == true){
                    if(tk.getType() == CToken.TK_AMP){
                        tk = ct.getNextToken(pcx);
                    } else {
                        pcx.fatalError("左辺が'*'なので, 右辺は'&'でなければなりません.");
                    }
                } else {
                    if(tk.getType() == CToken.TK_AMP){
                        pcx.fatalError("左辺が'*'でないので, 右辺に'&'は必要ありません.");
                    }
                }

                if(tk.getType() == CToken.TK_NUM){
                    number = tk;
                    tk = ct.getNextToken(pcx);
                }else{
                    pcx.fatalError("右辺がありません.");
                }
            }else if(tk.getType() == CToken.TK_NUM){
            	pcx.fatalError("'='がありません.");
            }else{
                pcx.fatalError("初期値の定義がされていません.");
            }

            if(isPointer == true){
                this.setCType(CType.getCType(CType.T_pint));
                pcx.getTable().addToTable(ident.getText(), new CSymbolTableEntry(getCType(), 0, true, true, 0));
            } else if(isPointer == false){
                this.setCType(CType.getCType(CType.T_int));
                pcx.getTable().addToTable(ident.getText(), new CSymbolTableEntry(getCType(), 1, true, true, 0));
            }
        }
    }

    public void semanticCheck(CParseContext pcx) throws FatalErrorException {
    }

    public void codeGen(CParseContext pcx) throws FatalErrorException {
        PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; cnstItem starts");
        if(ident != null){
            o.println(ident.getText() + ":\t.WORD " + number.getIntValue() + " \t\t; constItem: 初期値<" + number.getIntValue() + ">を設定"); // .WORD 0
        }
        o.println(";;;  completes");
    }
}
