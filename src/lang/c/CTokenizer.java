package lang.c;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import lang.Tokenizer;

public class CTokenizer extends Tokenizer<CToken, CParseContext> {
	@SuppressWarnings("unused")
	private CTokenRule	rule;
	private int			lineNo, colNo;
	private char		backCh;
	private boolean		backChExist = false;

	public CTokenizer(CTokenRule rule) {
		this.rule = rule;
		lineNo = 1; colNo = 1;
	}

	private InputStream in;
	private PrintStream err;

	private char readChar() {
		char ch;
		if (backChExist) {
			ch = backCh;
			backChExist = false;
		} else {
			try {
				ch = (char) in.read();
			} catch (IOException e) {
				e.printStackTrace(err);
				ch = (char) -1;
			}
		}
		++colNo;
		if (ch == '\n')  { colNo = 1; ++lineNo; }
//		System.out.print("'"+ch+"'("+(int)ch+")");
		return ch;
	}

	private void backChar(char c) {
		backCh = c;
		backChExist = true;
		--colNo;
		if (c == '\n') { --lineNo; }
	}

	// 現在読み込まれているトークンを返す
	private CToken currentTk = null;
	public CToken getCurrentToken(CParseContext pctx) {
		return currentTk;
	}

	// 次のトークンを読んで返す
	public CToken getNextToken(CParseContext pctx) {
		in = pctx.getIOContext().getInStream();
		err = pctx.getIOContext().getErrStream();
		currentTk = readToken();
//		System.out.println("Token='" + currentTk.toString());
		return currentTk;
	}


	// 課題1での疑問点
	// startCol = colNo-1; ･･･(*) については, readChar()が呼ばれた後に行っている.

	// 新たなひとかたまりを始める書く必要ある感
	// 例えば, 数なら 数字を読む前に(*)を行い, 終わったら, state=0に遷移し,
	// そこで新たな種類の文字を読む際に, readChar()後, 再び(*)を行う.

	// 今回の場合は '// コメント' と '/*' と 'コメント*/' という組み合わせでひとかたまりで見てる

	// ただ読まなかったことにする際にはbackChar()内でやってくれるので,
	// 書く必要はなし.

	private CToken readToken() {
		CToken tk = null;
		char ch;
		int  startCol = colNo;
		StringBuffer text = new StringBuffer();

		int state = 0;
		boolean accept = false;
		while (!accept) {
			switch (state) {
			case 0:					// 初期状態
				ch = readChar();
				if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {

				} else if (ch == (char) -1) {	// EOF
					startCol = colNo - 1;
					state = 1;
				} else if (ch >= '1' && ch <= '9') {
					startCol = colNo - 1;
					text.append(ch);
					state = 3;
				} else if (ch == '+') {
					startCol = colNo - 1;
					text.append(ch);
					state = 4;
				} else if (ch == '-') {
					startCol = colNo - 1;
					text.append(ch);
					state = 5;
				} else if (ch == '/') {
					startCol = colNo - 1;
					state = 6;
				} else if (ch == '&') {
					startCol = colNo - 1;
					text.append(ch);
					state = 10;
				} else if (ch == '0') {
					startCol = colNo - 1;
					text.append(ch);
					state = 12;
				} else {			// ヘンな文字を読んだ
					startCol = colNo - 1;
					text.append(ch);
					state = 2;
				}
				break;

			case 1:					// EOFを読んだ
				tk = new CToken(CToken.TK_EOF, lineNo, startCol, "end_of_file");
				accept = true;
				break;

			case 2:					// ヘンな文字を読んだ
				tk = new CToken(CToken.TK_ILL, lineNo, startCol, text.toString());
				accept = true;
				break;

			case 3:					// 数（10進数）の開始
				ch = readChar();
				if (Character.isDigit(ch)) {
					text.append(ch);
				} else {
					// 数の終わり
					backChar(ch);	// 数を表さない文字は戻す（読まなかったことにする）
					state = 11;
				}
				break;

			case 4:					// +を読んだ
				tk = new CToken(CToken.TK_PLUS, lineNo, startCol, "+");
				accept = true;
				break;

			case 5:					// -を読んだ
				tk = new CToken(CToken.TK_MINUS, lineNo, startCol, "-");
				accept = true;
				break;

			case 6:					// /を読んだ
				ch = readChar();
				if (ch == '/'){		// '//'のとき
					state = 7;
				} else if (ch == '*') {	// '/*'のとき
					// '/*'でひとかたまりとして見るため, 以下1文の処理が必要.
					startCol = colNo - 1;
					state = 8;
				} else if (ch == (char) -1) {
					backChar(ch);
					state = 0;
				}
				break;

			case 7:					// '//'の処理
				ch = readChar();
				if(ch == '\n' || ch == '\r'){
					state = 0;
				}else if(ch == (char) -1) { // EOF
					backChar(ch);
					state = 0;
				}
				break;

			case 8:					// '/*'の後の処理
				ch = readChar();
				if(ch == '*'){		// '/*'の後, *を読んだ
					state = 9;
				}else if(ch == (char) -1) { // EOF
					// コメント内部のEOFは不正とする
					backChar(ch);
					state = 2;
					System.err.println("ERROR : EOF in Comment Line.");
				}
				break;

			case 9:					// '/* .+ *' を読んだ
				ch = readChar();
				if(ch == '/'){		// '/* .+ */' を読んだ
					state = 0;
				}else if(ch == (char) -1) { // EOF
					// コメント内部のEOFは不正とする
					backChar(ch);
					state = 2;
					System.err.println("ERROR : EOF in Comment Line.");
				}else if(ch == '*') {
				}else{
					state = 8;
				}
				break;

			case 10:	// &
				tk = new CToken(CToken.TK_AMP, lineNo, startCol, "&");
				accept = true;
				break;

			case 11:	// '数の終わり'かどうかを確認する
				try {
					if(Integer.decode(text.toString()) <= 0xFFFF) {
						tk = new CToken(CToken.TK_NUM, lineNo, startCol, text.toString());
						accept = true;
					} else {
						System.err.println("ERROR : Bit size is too large.<case 11>");
						state = 2;
					}
				} catch(Exception e) {
					System.err.println("ERROR : Illegal character.<case 11>");
					state = 2;
				}
				break;

			case 12:					// '0'から始まる数字がどんな種類のものか分類する
				ch = readChar();
				if (ch >= '0' && ch <= '7') { 	// 8進数の処理状態へ遷移する
					text.append(ch);
					state = 13;
				} else if (ch == 'x') {			// '0x'まで読んだ
					text.append(ch);
					state = 14;
				} else {
					backChar(ch);				// 数を表さない文字は戻す（読まなかったことにする）
					state = 11;
				}
				break;

			case 13:					// 8進数の処理
				ch = readChar();
				if (ch >= '0' && ch <= '7') {
					text.append(ch);
				}
				else if(ch >= '8' && ch <= '9') {	// 8や9が来たら, 不正
					backChar(ch);
					state = 11;
				} else {
					backChar(ch);					// 数を表さない文字は戻す（読まなかったことにする）
					state = 11;
				}
				break;

			case 14:					// 16進数の処理状態へ遷移する
				ch = readChar();
				if (ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'f' || ch >= 'A' && ch <= 'F') {
					text.append(ch);
					state = 15;
				} else {
					System.err.println("ERROR : Illegal character.<case 14>");
					state = 2;
				}
				break;

			case 15:					// 16進数の処理
				ch = readChar();
				if (ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'f' || ch >= 'A' && ch <= 'F') {
					text.append(ch);
				} else {
					backChar(ch);		// 数を表さない文字は戻す（読まなかったことにする）
					state = 11;
				}
				break;

			}

		}
		return tk;
	}
}
