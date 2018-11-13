/*
 * Copyright 2011 - 2013 NTB University of Applied Sciences in Technology
 * Buchs, Switzerland, http://www.ntb.ch/inf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package ch.ntb.inf.deep.linker;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import ch.ntb.inf.deep.strings.HString;

/** 
 * For entries in the constant block, the type descriptor, and the system table which have fixed values,
 * This entry has the size of 4 bytes.
 */
public class FixedValueEntry extends ConstBlkEntry {
	
	private static final int size = 4;
	
	int val;
		
	/** 
	 * Create fixed value entry with a given name for a given value.
	 * 
	 * @param name Name of the value
	 * @param val Value
	 */
	public FixedValueEntry(HString name, int val) {
		this.name = name;
		this.val = val;
	}
	
	/** 
	 * Create fixed value entry with a given name with no assigned value yet.
	 * 
	 * @param name Name of the value
	 */
	public FixedValueEntry(HString name) {
		this(name, -1);
	}
	
	/** 
	 * Create fixed value entry with a given name with no assigned value yet.
	 * 
	 * @param name Name of the value
	 */
	public FixedValueEntry(String name) {
		this(HString.getRegisteredHString(name), -1);
	}
	
	/** 
	 * Create fixed value entry with a given name for a given value.
	 * 
	 * @param name Name of the value
	 * @param val Value
	 */
	public FixedValueEntry(String name, int val) {
		this(HString.getRegisteredHString(name), val);
	}
	
	public void setValue(int val) {
		this.val = val;
	}
	
	protected int getItemSize() {
		return size;
	}
	
	/**
	 * Inserts this entry into a target segment represented by an integer array at a given byte offset.
	 * 
	 * @param a Integer array where this fixed value entry should be inserted
	 * @param offset Offset in bytes where to insert
	 * @return Number of bytes inserted
	 */
	protected int insertIntoArray(int[] a, int offset) {
		int index = offset / 4;
		if(offset + size <= a.length * 4) {
			a[index] = val;
			return size;
		}
		return 0;
	}
	
	/**
	 * Returns this entry as a byte array. Return in endianess order of the target.
	 * 
	 * @return Byte array
	 */
	public byte[] getBytes() {
		if (Linker32.bigEndian) {
			return ByteBuffer.allocate(4).putInt(val).array();
		} else {
			return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(val).array();
		}
	}
	
	public String toString() {
		return String.format("[%08X]", val) + " (" + name + ")";
	}
	
	public int getValue() {
		return val;
	}

}
