package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class DebugSymbols {
	private List<CompilationUnit> compilationUnits;
	private CompilationUnit actualCompilationUnit;
	

	public DebugSymbols() {
		compilationUnits = new ArrayList<>();
	}

	public void setNextCompilationUnit(String filename) {
		actualCompilationUnit = new CompilationUnit(filename);
		compilationUnits.add(actualCompilationUnit);
	}

	public CompilationUnit getActualCompilationUnit() {
		return actualCompilationUnit;
	}

	public ByteBuffer saveLineNumberTable(ByteOrder byteOrder) {
		ByteBuffer buffer = ByteBuffer.allocate(0xFFFF);
		buffer.order(byteOrder);
		for (CompilationUnit cu : compilationUnits) {
			buffer.put(cu.SaveLineNumberTable(byteOrder));
		}
		buffer.flip();
		return buffer;
	}
}
