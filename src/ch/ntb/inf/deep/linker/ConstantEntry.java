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
import ch.ntb.inf.deep.classItems.ICdescAndTypeConsts;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.StdConstant;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.host.ErrorReporter;

/** 
 * For entries in the constant block which are constants (float, double).
 * This entry has the size of 4 or 8 bytes.
 */
public class ConstantEntry extends ConstBlkEntry implements ICdescAndTypeConsts {
	
	private static final ErrorReporter reporter = ErrorReporter.reporter;
	Item ref;
	
	/** 
	 * Create constant entry for a given item
	 * 
	 * @param ref Reference to item
	 */
	public ConstantEntry(Item ref) {
		this.ref = ref;
		this.name = ref.name;
	}
	
	protected int getItemSize() {
		return ((Type)ref.type).sizeInBits / 8;
	}
	
	/**
	 * Inserts this entry into a target segment represented by an integer array at a given byte offset.
	 * The value is inserted according to the endianess of the target.
	 * 
	 * @param a Integer array where this constant entry should be inserted
	 * @param offset Offset in bytes where to insert
	 * @return Number of bytes inserted
	 */
	protected int insertIntoArray(int[] a, int offset) {
		int size = this.getItemSize();
		int index = offset / 4;
		int written = 0;
		if(offset + size <= a.length * 4) {
			switch(size) {
			case 8: // double 
				if (Linker32.bigEndian) {
					a[index] = ((StdConstant)ref).valueH;
					a[index + 1] = ((StdConstant)ref).valueL;
				} else {
					a[index] = ((StdConstant)ref).valueL;
					a[index + 1] = ((StdConstant)ref).valueH;
				}
				written = size;
				break;
			case 4: // float 
				a[index] = ((StdConstant)ref).valueH;
				written = size;
				break;
			default: // error
				reporter.error(713, "use for float, double"); 
				break;
			}
		}
		return written;
	}
	
	/**
	 * Returns this entry as a byte array. Return in endianess order of the target.
	 * 
	 * @return Byte array
	 */
	public byte[] getBytes() {
		int size = getItemSize();
		byte[] bytes = null;
		
		switch(size) {
		case 8: // double
			if (Linker32.bigEndian) {
				ByteBuffer bb = ByteBuffer.allocate(8);
				bb.putInt(((StdConstant)ref).valueH);
				bb.putInt(((StdConstant)ref).valueL);
				bytes = bb.array();
			} else {
				ByteBuffer bb = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
				bb.putInt(((StdConstant)ref).valueL);
				bb.putInt(((StdConstant)ref).valueH);
				bytes = bb.array();
			}
			break;
		case 4: // float
			if (Linker32.bigEndian) {
				bytes = ByteBuffer.allocate(4).putInt(((StdConstant)ref).valueH).array();
			} else {
				bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(((StdConstant)ref).valueH).array();
			}
			break;
		default: // error
			reporter.error(713, "use for float, double"); 
			break;
		}
		return bytes;
	}
	
	public String toString() {
		int size = this.getItemSize();
		StringBuilder sb = new StringBuilder();
		switch(size) {
		case 8: // double
			if (Linker32.bigEndian) {
				sb.append(String.format("[%08X]", ((StdConstant)ref).valueH));
				sb.append(' ');
				sb.append(((StdConstant)ref));
				sb.append('\n');
				sb.append(String.format("[%08X]", ((StdConstant)ref).valueL));
			} else {
				sb.append(String.format("[%08X]", ((StdConstant)ref).valueL));
				sb.append(' ');
				sb.append(((StdConstant)ref));
				sb.append('\n');
				sb.append(String.format("[%08X]", ((StdConstant)ref).valueH));
			}
			break;
		case 4: // float 
			sb.append(String.format("[%08X]", ((StdConstant)ref).valueH));
			sb.append(' ');
			sb.append(((StdConstant)ref));
			break;
		default: // error
			reporter.error(713, "use for float, double"); 
			break;
		}
		return sb.toString();
	}
	
	public void setIndex(int index) {
		ref.index = index;
	}
	
	public void setOffset(int offset) {
		ref.offset = offset;
	}
	
	public void setAddress(int address) {
		ref.address = address;
	}
}
