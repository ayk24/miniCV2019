package lang.c;

import lang.SymbolTableEntry;

public class CSymbolTableEntry extends SymbolTableEntry {
    private CType     type;      // この識別子に対して宣言された型
    private int       size;      // メモリ上に確保すべきワード数
    private boolean   constp;    // 定数宣言か?
    private boolean   isGlobal;  // 大域変数か?
    private int       address;   // 割り当て番地

    public CSymbolTableEntry(CType type, int size, boolean constp, boolean isGlobal, int addr) {
        this.type = type;
        this.size = size;
        this.constp = constp;
        this.isGlobal = isGlobal;
        this.address = addr;
    }

    // このエントリに関する情報を作り出す. 記号表全体を出力するときに使う.
    public String toExplainString() {
    	return type.toString() + ", " + size + (constp ? "定数" : "変数");
    }

    public CType getType() { return type; }
    public int getSize() { return size; }
    public boolean isConstp() { return constp; }
    public boolean isGlobal() { return isGlobal; }
    public int getAddress() { return address; }
    public void setType(CType type) { this.type = type; }
    public void setSize(int size) { this.size = size; }
    public void setConstp(boolean constp) { this.constp = constp; }
    public void setGlobal(boolean isGlobal) { this.isGlobal = isGlobal; }
    public void setAddress(int address) { this.address = address; }
}
