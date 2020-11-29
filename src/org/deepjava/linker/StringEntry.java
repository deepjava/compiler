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
import org.deepjava.classItems.StringLiteral;
import org.deepjava.classItems.Type;
import org.deepjava.host.ErrorReporter;
import org.deepjava.strings.HString;

/** 
 * For entries in the constant block, the type descriptor, and the system table which are strings,
 * This entry has the size of multiples of 4 bytes.
 */
public class StringEntry extends ConstBlkEntry {
	
	private static final int tag = 0x80000000; // set mark bit, clear array bit and primitive array bit
	private static final int constHeaderSize = 3 * 4; // byte
	Item ref;
	
	/** 
	 * Create string entry for a given item
	 * 
	 * @param ref Reference to item
	 */
	public StringEntry(Item ref) {
		this.ref = ref;
		this.name = ref.name;
	}
	
	protected int getItemSize() {
		return getHeaderSize() + Linker32.roundUpToNextWord(getNumberOfChars() * 2);
	}
	
	/**
	 * Inserts this entry into a target segment represented by an integer array at a given byte offset.
	 * 
	 * @param a Integer array where this string entry should be inserted
	 * @param offset Offset in bytes where to insert
	 * @return Number of bytes inserted
	 */
	protected int insertIntoArray(int[] a, int offset) {
		HString s = ((StringLiteral)ref).string;
		int size = getItemSize(); // byte
		int index = offset / 4;
		int objectSize = getHeaderSize() - constHeaderSize; // byte
		int word = 0, c = 0, written = 0;
		if (offset + size <= a.length * 4) {
			a[index++] = tag;
			a[index++] = getStringClassAddr();
			written += 8;
			for (int i = 0; i < objectSize / 4; i ++) {
				a[index++] = 0; // TODO @Urs: insert fields of object here...
				written += 4;
			}
			a[index++] = getNumberOfChars();
			written += 4;
			for (int i = 0; i < getNumberOfChars(); i++) {
				word = (word << 16) + s.charAt(i);
				c++;
				if (c > 1 || i == s.length() - 1) {
					if (i == s.length() - 1 && s.length() % 2 != 0) word = word << 16;
					if (!Linker32.bigEndian) word = Integer.rotateLeft(word, 16); 
					a[index++] = word; 
					written += 4;
					c = 0;
					word = 0;
				}
			}
		}
		return written;
	}
	
	protected void insertBytes(byte[] bytes, int offset, int val, int size) {
		if (Linker32.bigEndian) {
			ByteBuffer bb = ByteBuffer.allocate(size);
			byte[] b;
			if (size == 4) b = bb.putInt(val).array();
			else b = bb.putChar((char)val).array();
			System.arraycopy(b, 0, bytes, offset, size);
		} else {
			ByteBuffer bb = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
			byte[] b;
			if (size == 4) b = bb.putInt(val).array();
			else b = bb.putChar((char)val).array();
			System.arraycopy(b, 0, bytes, offset, size);
		}
	}
	
	public byte[] getBytes() {
		HString s = ((StringLiteral)ref).string;
		int size = getItemSize();
		byte[] bytes = new byte[size];
		int offset = 0; int word = 0; int c = 0;
		if (Linker32.bigEndian) {
			insertBytes(bytes, offset, tag, 4); offset += 4;
			insertBytes(bytes, offset, getStringClassAddr(), 4); offset += 4;
			for(int i = getHeaderSize() - constHeaderSize; i > 0; i--) {
				bytes[offset] = 0;
				offset++;
			}
			insertBytes(bytes, offset, getNumberOfChars(), 4); offset += 4;
			for(int j = 0; j < getNumberOfChars(); j++) {
				word = (word << 16) + s.charAt(j);
				c++;
				if(c > 1 || j == s.length() - 1) {
					if(j == s.length() - 1 && s.length() % 2 != 0) word = word << 16;
					insertBytes(bytes, offset, word, 4); offset += 4;
					c = 0;
					word = 0;
				}
			}
		} else {
			insertBytes(bytes, offset, tag, 4); offset += 4;
			insertBytes(bytes, offset, getStringClassAddr(), 4); offset += 4;
			for(int i = getHeaderSize() - constHeaderSize; i > 0; i--) {
				bytes[offset] = 0;
				offset++;
			}
			insertBytes(bytes, offset, getNumberOfChars(), 4); offset += 4;
			for (int i = 0; i < getNumberOfChars(); i++) {
				insertBytes(bytes, offset, s.charAt(i), 2);
				offset += 2;
			}
			if (getNumberOfChars() % 2 != 0) insertBytes(bytes, offset, 0, 2);
		}
		return bytes;
	}
	
	private int getNumberOfChars() {
		return ((StringLiteral)ref).string.length();
	}
	
	private static int getStringClassAddr() {
		if (Type.wktString != null) return Type.wktString.address;	
		ErrorReporter.reporter.error(701, "String (Type.wktString == null)");
		return -1;
	}

	private static int getHeaderSize() {
		return constHeaderSize + Linker32.roundUpToNextWord(Type.wktObject.objectSize);
	}
	
	public String toString() {
		HString s = ((StringLiteral)ref).string;
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("[%08X]", tag));
		sb.append(" tag\n");
		sb.append(String.format("[%08X]", getStringClassAddr()));
		sb.append(" string class address\n");
		for (int i = 0; i < (getHeaderSize() - constHeaderSize) / 4; i++) {
			sb.append("[ Object ]\n");
		}
		sb.append(String.format("[%08X]", getNumberOfChars()));
		sb.append(" number of characters");
		for (int i = 0; i < getNumberOfChars(); i++) {
			sb.append(String.format("\n[%04X]", (int)s.charAt(i)));
			sb.append(' ');
			sb.append(s.charAt(i));
		}
		if (getNumberOfChars() % 2 != 0) sb.append("\n[0000]");	// entries must be multiples of 2 -> 4 bytes
		return sb.toString();
	}
	
	public void setIndex(int index) {
		ref.index = index;
	}
	
	public void setOffset(int offset) {
		ref.offset = offset;
	}
}
