package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.dwarf.die.CompilationUnitDIE;
import ch.ntb.inf.deep.dwarf.die.DWARF;

public class DebugSymbols{
	private final List<CompilationUnitDIE> compilationUnits;
	private final DWARF serializer;

	public DebugSymbols(ByteOrder byteOrder) {
		compilationUnits = new ArrayList<>();

		Class refType = Class.initClasses;
		while (refType != null) {
			if (refType.methods != null) {
//			if (refType.methods != null && refType.name.toString().contains("ObjFields")) {
				Class clazz = (Class) refType;
				CompilationUnitDIE cu = new CompilationUnitDIE(clazz);
				compilationUnits.add(cu);
			}
			refType = refType.nextClass;
		}

		serializer = new DWARF(byteOrder, compilationUnits);
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
	
	public ByteBuffer getDebug_loc() {
		return serializer.debug_loc;
	}
}
