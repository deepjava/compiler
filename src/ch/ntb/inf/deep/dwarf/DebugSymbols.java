package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.ssa.LineNrSSAInstrPair;

public class DebugSymbols {
	private final List<CompilationUnitDIE> compilationUnits;
	private final DieSerializer serializer;

	private CompilationUnitDIE actualCompilationUnit;

	public DebugSymbols(ByteOrder byteOrder) {
		compilationUnits = new ArrayList<>();

		Class refType = Class.initClasses;
		while (refType != null) {
			if (refType.methods != null) { // && refType.name.toString().equals("test/ObjFields")) {
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

		serializer = new DieSerializer(byteOrder);
		for (CompilationUnitDIE cu : compilationUnits) {
			cu.accept(serializer);
		}
	}

	public void setNextCompilationUnit(Class clazz) {
		actualCompilationUnit = new CompilationUnitDIE(clazz);
		compilationUnits.add(actualCompilationUnit);
	}

	public ByteBuffer getDebug_info() {
		return serializer.debug_info;
	}

	public ByteBuffer getDebug_abbrev() {
		return serializer.debug_abbrev;
	}

	public ByteBuffer getDebug_line() {
		return serializer.debug_line;
	}

}
