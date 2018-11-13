package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.ssa.LineNrSSAInstrPair;

public class DebugSymbols {
	private final List<CompilationUnit> compilationUnits;
	private CompilationUnit actualCompilationUnit;
	private final ByteBuffer debug_string;
	private final ByteBuffer debug_info;
	private final ByteBuffer debug_abbrev;
	private final ByteBuffer debug_line;

	public DebugSymbols(ByteOrder byteOrder) {
		compilationUnits = new ArrayList<>();

		debug_string = ByteBuffer.allocate(0xFFFF);
		debug_string.order(byteOrder);
		debug_info = ByteBuffer.allocate(0xFFFF);
		debug_info.order(byteOrder);
		debug_abbrev = ByteBuffer.allocate(0xFFFF);
		debug_abbrev.order(byteOrder);
		debug_line = ByteBuffer.allocate(0xFFFF);
		debug_line.order(byteOrder);

		
		Class refType = Class.initClasses;
		while (refType != null) {
			if (refType.methods != null) {
				Class clazz = (Class) refType;
				setNextCompilationUnit(clazz);
				Method method = (Method) clazz.methods;
				while (method != null) {
					if (method.ssa != null) {
						for (LineNrSSAInstrPair line : method.ssa.getLineNrTable()) {
							int address = method.address + line.ssaInstr.machineCodeOffset * 4;
							actualCompilationUnit.addLineNumberEntry(line.lineNr, address);
						}
					}
					method = (Method) method.next;
				}
			}
			refType = refType.nextClass;
		}

		serialize();
	}

	public void setNextCompilationUnit(Class clazz) {
		actualCompilationUnit = new CompilationUnit(clazz);
		compilationUnits.add(actualCompilationUnit);
	}

	private void serialize() {
		compilationUnits.get(0).serializeAbbrev(debug_abbrev);
		for (CompilationUnit cu : compilationUnits) {
		//	cu.serializeAbbrev(debug_abbrev);
			cu.serialize(debug_info, debug_line.position());
			cu.serializeLine(debug_line);
		}

		debug_string.flip();
		debug_info.flip();
		debug_abbrev.flip();
		debug_line.flip();
	}

	public ByteBuffer getDebug_string() {
		if (debug_string == null)
			throw new RuntimeException(".debug_string is null. You should first call serialize()");
		return debug_string;
	}

	public ByteBuffer getDebug_info() {
		if (debug_info == null)
			throw new RuntimeException(".debug_info is null. You should first call serialize()");
		return debug_info;
	}

	public ByteBuffer getDebug_abbrev() {
		if (debug_abbrev == null)
			throw new RuntimeException(".debug_abbrev is null. You should first call serialize()");
		return debug_abbrev;
	}

	public ByteBuffer getDebug_line() {
		if (debug_line == null)
			throw new RuntimeException(".debug_line is null. You should first call serialize()");
		return debug_line;
	}

}
