package lang.c.parse;

import java.io.PrintStream;

import lang.FatalErrorException;
import lang.c.CParseContext;
import lang.c.CParseRule;
import lang.c.CToken;
import lang.c.CTokenizer;

public class StatementIf extends CParseRule {

	// statementIf ::= IF conditionBlock statementBlock statementElse

	private CParseRule conditionBlock, statementBlock, statementElse;
	private CToken ident;

	public StatementIf(CParseContext pcx, CToken ident) {
		this.ident = ident;
	}

	public static boolean isFirst(CToken tk) {
		return tk.getType() == CToken.TK_IF;
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
			statementBlock = new StatementBlock(pcx, ident);
			statementBlock.parse(pcx);
		} else {
			pcx.fatalError(tk.toExplainString() + "条件文の中に文がありません");
		}

		tk = ct.getCurrentToken(pcx);

		if (StatementElse.isFirst(tk)) {
			statementElse = new StatementElse(pcx, ident);
			statementElse.parse(pcx);
		}
	}

	public void semanticCheck(CParseContext pcx) throws FatalErrorException {
		if (conditionBlock != null && statementBlock != null) {
			conditionBlock.semanticCheck(pcx);
			statementBlock.semanticCheck(pcx);
		}

		if (statementElse != null) {
			statementElse.semanticCheck(pcx);
			if (statementBlock.getCType() == null) {
				setCType(statementElse.getCType());
			} else if (statementElse.getCType() == null) {
				setCType(statementBlock.getCType());
			} else if (statementBlock.getCType() == statementElse.getCType()) {
				setCType(statementBlock.getCType());
			} else {
				pcx.fatalError("IF文とELSE文で返り値の型が異なっています.");
			}
		} else {
			setCType(statementBlock.getCType());
		}
	}

	public void codeGen(CParseContext pcx) throws FatalErrorException {
		PrintStream o = pcx.getIOContext().getOutStream();
		o.println(";;; statementIf starts");
		if (conditionBlock != null && statementBlock != null) {
			int seq = pcx.getSeqId();
			o.println("IF" + seq + ":\t\t\t\t; statementIf:");
			conditionBlock.codeGen(pcx);
			o.println("\tMOV\t-(R6), R0\t; statementIf:");
			o.println("\tBRZ\tFALSE" + seq + "\t\t; statementIf:");
			o.println("TRUE" + seq + ":\t\t\t\t; statementIf");
			statementBlock.codeGen(pcx);
			o.println("\tJMP\tENDIF" + seq + "\t\t\t; statementIf:");
			o.println("FALSE" + seq + ":\t\t\t\t\t; statementIf:");
			if (statementElse != null) {
				statementElse.codeGen(pcx);
			}
			o.println("ENDIF" + seq + ":\t\t\t\t; statementIf:");
		}
		o.println(";;; statementIf completes");
	}
}
