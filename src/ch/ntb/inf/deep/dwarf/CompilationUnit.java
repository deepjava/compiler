package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class CompilationUnit {
	private String filename;
	private List<LineMatrixEntry> lineNumberTableMatrix;

	public CompilationUnit(String filename) {
		this.filename = filename;
		this.lineNumberTableMatrix = new ArrayList<>();
	}

	public void addLineNumberEntry(int srcLineNumber, int machineCodeAddress) {
		lineNumberTableMatrix.add(new LineMatrixEntry(filename, srcLineNumber, 0, machineCodeAddress));
	}

	public ByteBuffer SaveLineNumberTable(ByteOrder byteOrder) {
		DebugLineStateMaschine stateMachine = new DebugLineStateMaschine(lineNumberTableMatrix);		
		return stateMachine.SerializeProgram(byteOrder);
	}
}
