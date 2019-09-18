package lang.c;

import lang.SimpleToken;

public class CToken extends SimpleToken {
	public static final int TK_PLUS			= 2;				// +
	public static final int TK_MINUS		= 3;				// -
	public static final int TK_AMP			= 4;				// &

	// 文字のタイプ, 何行目, 何文字目, 綴り を保持
	public CToken(int type, int lineNo, int colNo, String s) {
		super(type, lineNo, colNo, s);
	}
}
