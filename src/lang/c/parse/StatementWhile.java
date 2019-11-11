package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class StatementWhile extends CParseRule {

	// statementWhile ::= WHILE conditionBlock statementBlock

	private CParseRule conditionBlock, statementBlock;
	private CToken idtk;

	public StatementWhile(CParseContext pcx, CToken idtk) {
		this.idtk = idtk;
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_WHILE;
	}

	public void parse(CParseContext pcx) throws FatalErrorException {
		CTokenizer ct = pcx.getTokenizer();
		CToken tk = ct.getNextToken(pcx);

		if (ConditionBlock.isFirst(tk)) {
			conditionBlock = new ConditionBlock(pcx);
			conditionBlock.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "条件式がありません.");
		}

		tk = ct.getCurrentToken(pcx);

		if (StatementBlock.isFirst(tk)) {
			statementBlock = new StatementBlock(pcx, idtk);
			statementBlock.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "条件文内部に文がありません.");
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (conditionBlock != null && statementBlock != null) {
			conditionBlock.semanticCheck(pcx);
			statementBlock.semanticCheck(pcx);
			setCType(statementBlock.getCType());
			setConstant(statementBlock.isConstant());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
        o.println(";;; statementWhile starts");
		if (conditionBlock != null && statementBlock != null) {
			int seq = pcx.getSeqId();
			o.println("WHILE" + seq + ":\t\t\t\t; statementWhile:");
			conditionBlock.codeGen(pcx);
	        o.println("TRUE" + seq + ":\t\t\t\t; statementWhile:");
			o.println("\tMOV\t-(R6), R0\t; statementWhile:");
			o.println("\tBRZ\tFALSE" + seq + "\t\t\t; statementWhile:");
			statementBlock.codeGen(pcx);
			o.println("\tJMP\tWHILE" + seq + "\t\t\t; statementWhile:");
			o.println("FALSE" + seq + ":\t\t\t\t\t; statementWhile:");
		}
        o.println(";;; statementWhile completes");
	}
}
