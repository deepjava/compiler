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
 * For entries in the type descriptor which are interface methods,
 * This entry has the size of 4 bytes.
 */
public class InterfaceEntry extends ConstBlkEntry {
	
	private static final int size = 4;
	short ifaceID;
	short bmo;
		
	/** 
	 * Create interface method entry. 
	 * 
	 * @param ifaceName Name of the interface
	 * @param ifaceID Interface id
	 * @param bmo 
	 */
	public InterfaceEntry(HString ifaceName, short ifaceID, short bmo) {
		this.name = ifaceName;
		this.ifaceID = ifaceID;
		this.bmo = bmo;
	}
	
	public int getOffset() {
		return this.bmo;
	}
	
	public int getID() {
		return this.ifaceID;
	}
	
	protected int getItemSize() {
		return size;
	}
	
	public void setBmo(int offset) {
		bmo = (short)offset;
	}

	/**
	 * Inserts this entry into a target segment represented by an integer array at a given byte offset.
	 * 
	 * @param a Integer array where this interface entry should be inserted
	 * @param offset Offset in bytes where to insert
	 * @return Number of bytes inserted
	 */
	protected int insertIntoArray(int[] a, int offset) {
		int index = offset / 4;
		if(offset + size <= a.length * 4) {
			a[index] = (int)this.ifaceID << 16 | ((int)this.bmo & 0xFFFF);
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
			return ByteBuffer.allocate(4).putInt((int)this.ifaceID << 16 | ((int)this.bmo & 0xFFFF)).array();
		} else {
			return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt((int)this.ifaceID << 16 | ((int)this.bmo & 0xFFFF)).array();
		}
	}
	
	public String toString() {
		return String.format("[%08X]", (int)this.ifaceID << 16 | ((int)this.bmo & 0xFFFF)) + " (" + name + ")";
	}

}
