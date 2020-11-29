package org.deepjava.dwarf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deepjava.classItems.Class;
import org.deepjava.dwarf.die.CompilationUnitDIE;
import org.deepjava.dwarf.die.DWARF;

public class DebugSymbols {
	private final DWARF serializer;

	public DebugSymbols(ByteOrder byteOrder) {
		List<CompilationUnitDIE> compilationUnits = new ArrayList<>();
		ClassIterator classIterator = new ClassIterator();
		
		// First Iteration for Types which are used in Second Iteration
		while (classIterator.hasNext()) {
			Class clazz = classIterator.next();
			compilationUnits.add(new CompilationUnitDIE(clazz));
		}
		
		compilationUnits.forEach(cu -> cu.insertChildInformation());
		
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
