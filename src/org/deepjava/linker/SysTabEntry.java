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

package org.deepjava.linker;

import org.deepjava.classItems.Class;
import org.deepjava.strings.HString;

/** 
 * For entries in the system table which are classes.
 * This entry has the size of 4 bytes.
 */
public class SysTabEntry extends ConstBlkEntry {
	
	private static final int size = 4;
	
	Class cls;
	
	/** 
	 * Create system table entry for a given item
	 * 
	 * @param clazz Reference to a class
	 */
	public SysTabEntry(Class clazz) {
		this.cls = clazz;
		this.name = clazz.name;
	}
	
	/** 
	 * Create system table entry for a given class. 
	 * The name of the class can be specified with a prefix. 
	 * The final name will be prefix + class.name
	 * 
	 * @param prefix Prefix
	 * @param clazz Reference to class
	 */
	public SysTabEntry(String prefix, Class clazz) {
		this.cls = clazz;
		this.name = HString.getRegisteredHString(prefix + clazz.name);
	}
	
	protected int getItemSize() {
		return size;
	}
	
	/**
	 * Inserts this entry into a target segment represented by an integer array at a given byte offset.
	 * 
	 * @param a Integer array where this system table entry should be inserted
	 * @param offset Offset in bytes where to insert
	 * @return Number of bytes inserted
	 */
	protected int insertIntoArray(int[] a, int offset) {
		int address;
		assert cls.constSegment != null;
		address = cls.constSegment.address + cls.constOffset;
		int index = offset / 4;
		if (offset + size <= a.length * 4) {
			a[index] = address;
			return size;
		}
		return 0;
	}
	
	public String toString() {
		if (cls.constSegment == null)
			return String.format("[%08X]", -1) + " (" + name + ")";
		else 
			return String.format("[%08X]", cls.constSegment.address + cls.constOffset) + " (" + name + ")";	
	}
	
	public int getAddress() {
		return cls.constSegment.address + cls.constOffset;
	}

}
