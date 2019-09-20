package lang.c;

import lang.IOContext;
import lang.ParseContext;

public class CParseContext extends ParseContext {
	public CParseContext(IOContext ioCtx,  CTokenizer tknz) {
		super(ioCtx, tknz);
	}

	private CSymbolTable table = new CSymbolTable();
	public CSymbolTable getTable() { return table; }

	@Override
	public CTokenizer getTokenizer()		{ return (CTokenizer) super.getTokenizer(); }

	private int seqNo = 0;
	public int getSeqId() { return ++seqNo; }
}
