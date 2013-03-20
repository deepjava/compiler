/*
 * Copyright (c) 2011 NTB Interstate University of Applied Sciences of Technology Buchs.
 *
 * http://www.ntb.ch/inf
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Eclipse Public License for more details.
 * 
 * Contributors:
 *     NTB - initial implementation
 * 
 */

package ch.ntb.inf.deep.linker;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.strings.HString;

public class ConstantBlockEntry extends ConstBlkEntry {
	
	private static final int size = 4;
	
	Class clazz;
	
	public ConstantBlockEntry(Class clazz) {
		this.clazz = clazz;
		this.name = clazz.name;
	}
	
	public ConstantBlockEntry(String prefix, Class clazz) {
		this.clazz = clazz;
		this.name = HString.getRegisteredHString(prefix + clazz.name);
	}
	
	protected int getItemSize() {
		return size;
	}
	
	protected int insertIntoArray(int[] a, int offset) {
		int address;
		address = clazz.constSegment.getBaseAddress() + clazz.constOffset;
		int index = offset / 4;
		int written = 0;
		if(offset + size <= a.length * 4) {
			a[index] = address;
			written = size;
		}
		return written;
	}
	
	public byte[] getBytes() {
		byte[] bytes = new byte[size];
		for (int i = 0; i < size; ++i) {
		    int shift = i << 3; // i * 8
		    bytes[(size - 1) - i] = (byte)((this.getAddress() & (0xff << shift)) >>> shift);
		}
		return bytes;
	}
	
	public String toString() {
		return String.format("[%08X]", clazz.constSegment.getBaseAddress() + clazz.constOffset) + " (" + name + ")";
	}
	
	public int getAddress() {
		return clazz.constSegment.getBaseAddress() + clazz.constOffset;
	}

}
