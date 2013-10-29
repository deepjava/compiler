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

import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.strings.HString;

public class OffsetEntry extends ConstBlkEntry {
	
	private static final int size = 4;
	
	Item itemRef;
	
	public OffsetEntry(Item ref) {
		this.itemRef = ref;
		if(ref.name != null) this.name = ref.name;
		else name = UNDEF;
	}
	
	public OffsetEntry(String prefix, Item ref) {
		this.itemRef = ref;
		if(ref.name != null) this.name = HString.getRegisteredHString(prefix + ref.name);
		else name = HString.getRegisteredHString(prefix + "???");
	}
	
	protected int getItemSize() {
		return size;
	}
	
	protected int insertIntoArray(int[] a, int offset) {
		int value = itemRef.offset;
		int index = offset / 4;
		int written = 0;
		if(offset + size <= a.length * 4) {
			a[index] = value;
			written = size;
		}
		return written;
	}
	
	public byte[] getBytes() {
		byte[] bytes = new byte[size];
		for (int i = 0; i < size; ++i) {
		    int shift = i << 3; // i * 8
		    bytes[(size - 1) - i] = (byte)((itemRef.offset & (0xff << shift)) >>> shift);
		}
		return bytes;
	}
	
	public String toString() {
		return String.format("[%08X]", itemRef.offset) + " (" + name + ")";
	}
	
	public int getOffset() {
		return itemRef.offset;
	}

}
