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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.deepjava.classItems.Item;
import org.deepjava.strings.HString;

/** 
 * For entries in the type descriptor which are offsets. This are instance fields which are references.
 * This entry has the size of 4 bytes.
 */
public class OffsetEntry extends ConstBlkEntry {
	
	private static final int size = 4;
	
	Item itemRef;
	
	/** 
	 * Create offset entry for a given item
	 * 
	 * @param ref Reference to item
	 */
	public OffsetEntry(Item ref) {
		this.itemRef = ref;
		if(ref.name != null) this.name = ref.name;
		else name = UNDEF;
	}
	
	/** 
	 * Create offset entry for a given item. 
	 * The name of the entry can be specified with a prefix. 
	 * The final name will be prefix + item.name
	 * 
	 * @param prefix Prefix
	 * @param ref Reference to item
	 */
	public OffsetEntry(String prefix, Item ref) {
		this.itemRef = ref;
		if(ref.name != null) this.name = HString.getRegisteredHString(prefix + ref.name);
		else name = HString.getRegisteredHString(prefix + "???");
	}
	
	protected int getItemSize() {
		return size;
	}
	
	/**
	 * Inserts this entry into a target segment represented by an integer array at a given byte offset.
	 * 
	 * @param a Integer array where this offset entry should be inserted
	 * @param offset Offset in bytes where to insert
	 * @return Number of bytes inserted
	 */
	protected int insertIntoArray(int[] a, int offset) {
		int value = itemRef.offset;
		int index = offset / 4;
		if(offset + size <= a.length * 4) {
			a[index] = value;
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
			return ByteBuffer.allocate(4).putInt(itemRef.offset).array();
		} else {
			return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(itemRef.offset).array();
		}
	}
	
	public String toString() {
		return String.format("[%08X]", itemRef.offset) + " (" + name + ")";
	}
	
	public int getOffset() {
		return itemRef.offset;
	}

}
