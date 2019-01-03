package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.dwarf.die.CompilationUnitDIE;
import ch.ntb.inf.deep.dwarf.die.DWARF;

public class DebugSymbols {
	private final List<CompilationUnitDIE> compilationUnits;
	private final DWARF serializer;

	public DebugSymbols(ByteOrder byteOrder) {
		compilationUnits = new ArrayList<>();

		ClassIterator classIterator = new ClassIterator();
		Class c;
		while(classIterator.hasNext()) {
			c = classIterator.next();
			CompilationUnitDIE cu = new CompilationUnitDIE(c);
			compilationUnits.add(cu);
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

	private class ClassIterator implements Iterator<Class> {

		private Class actual;

		public ClassIterator() {
			actual = Class.initClasses;
		}

		@Override
		public boolean hasNext() {
			return actual != null;
		}

		@Override
		public Class next() {
			Class result = actual;
			if (actual == Class.initClassesTail) {
				actual = Class.nonInitClasses;
			} else {
				actual = actual.nextClass;
			}
			return result;
		}
	}
}
