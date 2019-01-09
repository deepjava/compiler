package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import ch.ntb.inf.deep.dwarf.die.CompilationUnitDIE;
import ch.ntb.inf.deep.dwarf.die.DWARF;

public class DebugSymbols {
	//private final List<CompilationUnitDIE> compilationUnits;
	private final CompilationUnitDIE compilationUnit;
	private final DWARF serializer;

	public DebugSymbols(ByteOrder byteOrder) {
		//compilationUnits = new ArrayList<>();
		compilationUnit = new CompilationUnitDIE();
		serializer = new DWARF(byteOrder, compilationUnit);
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
