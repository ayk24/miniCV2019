package lang.c;

import lang.SimpleToken;

public class CToken extends SimpleToken {
	public static final int TK_PLUS			= 2;				// +
	public static final int TK_MINUS		= 3;				// -
	public static final int TK_AMP			= 4;				// &
	public static final int TK_MULT			= 5;				// *
	public static final int TK_DIV			= 6;				// /
	public static final int TK_LPAR			= 7;				// (
	public static final int TK_RPAR			= 8;				// )
	public static final int TK_LBRA			= 9;				// [
	public static final int TK_RBRA			= 10;				// ]
	public static final int TK_ASSIGN		= 11;				// =
	public static final int TK_SEMI			= 12;				// ;
	public static final int TK_INT			= 13;				// int
	public static final int TK_CONST		= 14;				// const
	public static final int TK_COMMA		= 15;				// ,

	// 文字のタイプ, 何行目, 何文字目, 綴り を保持
	public CToken(int type, int lineNo, int colNo, String s) {
		super(type, lineNo, colNo, s);
	}
}
