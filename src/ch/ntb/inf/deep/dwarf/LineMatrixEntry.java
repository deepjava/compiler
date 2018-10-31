package ch.ntb.inf.deep.dwarf;

public class LineMatrixEntry {
	public final String filename;
	public final int line;
	public final int column;
	public final long address;

	public LineMatrixEntry(String filename, int line, int column, long address) {
		this.filename = filename;
		this.line = line;
		this.column = column;
		this.address = address;
	}
	
	@Override
	public String toString() {
		return String.format("Line:%d Address:0x%04X", line, address);
	}
}
