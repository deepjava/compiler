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

import ch.ntb.inf.deep.classItems.ICdescAndTypeConsts;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.StdConstant;
import ch.ntb.inf.deep.classItems.Type;

public class ConstantEntry extends ConstBlkEntry implements ICdescAndTypeConsts {
	
	Item ref;
	
	public ConstantEntry(Item ref) {
		this.ref = ref;
		this.name = ref.name;
	}
	
	protected int getItemSize() {
		return ((Type)ref.type).sizeInBits / 8;
	}
	
	protected int insertIntoArray(int[] a, int offset) {
		int size = this.getItemSize();
		int index = offset / 4;
		int written = 0;
		if(offset + size <= a.length * 4) {
			switch(size) {
			case 8: // long/double 
				if (Linker32.bigEndian) {
					a[index] = ((StdConstant)ref).valueH;
					a[index + 1] = ((StdConstant)ref).valueL;
				} else {
					a[index] = ((StdConstant)ref).valueL;
					a[index + 1] = ((StdConstant)ref).valueH;
				}
				written = size;
				break;
			case 4: // int/float 
				a[index] = ((StdConstant)ref).valueH;
				written = size;
				break;
			case 2: // short/char
				break;
			case 1:	// byte/boolean 
				break;
			default: // error
				break;
			}
		}
		return written;
	}
	
	public byte[] getBytes() {
		int size = getItemSize();
		byte[] bytes = new byte[size];
		
		switch(size) {
		case 8: // long/double
			if (Linker32.bigEndian) {
				for (int i = 0; i < 4; ++i) {
				    int shift = i << 3; // i * 8
				    bytes[7 - i] = (byte)((((StdConstant)ref).valueL & (0xff << shift)) >>> shift);
				}
				for (int i = 0; i < 4; ++i) {
				    int shift = i << 3; // i * 8
				    bytes[3 - i] = (byte)((((StdConstant)ref).valueH & (0xff << shift)) >>> shift);
				}
			} else {
				for (int i = 0; i < 4; ++i) {
				    int shift = i << 3; // i * 8
				    bytes[7 - i] = (byte)((((StdConstant)ref).valueH & (0xff << shift)) >>> shift);
				}
				for (int i = 0; i < 4; ++i) {
				    int shift = i << 3; // i * 8
				    bytes[3 - i] = (byte)((((StdConstant)ref).valueL & (0xff << shift)) >>> shift);
				}
			}
			break;
		case 4: // int/float
			for (int i = 0; i < size; ++i) {
			    int shift = i << 3; // i * 8
			    bytes[3 - i] = (byte)((((StdConstant)ref).valueH & (0xff << shift)) >>> shift);
			}
			break;
		case 2: // short/char
			break;
		case 1:	// byte/boolean 
			break;
		default: // error
			break;
		}
		return bytes;
	}
	
	public String toString() {
		int size = this.getItemSize();
		StringBuilder sb = new StringBuilder();
		switch(size) {
		case 8: // long/double
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
		case 4: // int/float 
			sb.append(String.format("[%08X]", ((StdConstant)ref).valueH));
			sb.append(' ');
			sb.append(((StdConstant)ref));
			break;
		case 2: // short/char
			break;
		case 1:	// byte/boolean 
			break;
		default: // error
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
