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

import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.StringLiteral;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.strings.HString;

public class StringItem extends BlockItem {
	
	private static final int tag = 0x55555555;
	private static final int constHeaderSize = 3 * 4; // byte
	
	Item ref;
	
	public StringItem(Item ref) {
		this.ref = ref;
		this.name = ref.name;
	}
	
	protected int getItemSize() {
		return getHeaderSize() + Linker32.roundUpToNextWord(getNumberOfChars() * 2);
	}
	
	protected int insertIntoArray(int[] a, int offset) {
		HString s = ((StringLiteral)ref).string;
		int size = getItemSize(); // byte
		int index = offset / 4;
		int objectSize = getHeaderSize() - constHeaderSize; // byte
		int word = 0, c = 0, written = 0;
		if(offset + size <= a.length * 4) {
			a[index++] = tag;
			a[index++] = getStringClassAddr();
			for(int i = 0; i < objectSize / 4; i ++) {
				a[index++] = 0; // TODO @Martin: insert object here...
			}
			a[index++] = getNumberOfChars();
			for(int j = 0; j < getNumberOfChars(); j++) {
				word = (word << 16) + s.charAt(j);
				c++;
				if(c > 1 || j == s.length() - 1) {
					if(j == s.length() - 1 && s.length() % 2 != 0) word = word << 16;
					a[index++] = word;
					c = 0;
					word = 0;
				}
			}
		}
		return written;
	}
	
	private int getNumberOfChars() {
		return ((StringLiteral)ref).string.length();
	}
	
	private static int getStringClassAddr() {
		if(Type.wktString != null)
			return Type.wktString.address;
		
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
		for(int i = 0; i < (getHeaderSize() - constHeaderSize) / 4; i++) {
			sb.append("[ Object ]\n");
		}
		sb.append(String.format("[%08X]", getNumberOfChars()));
		sb.append(" number of characters");
		for(int i = 0; i < getNumberOfChars() - 1; i += 2) {
			sb.append(String.format("\n[%08X]", (s.charAt(i) << 16) + s.charAt(i + 1)));
			sb.append(' ');
			sb.append(s.charAt(i));
			sb.append(s.charAt(i + 1));
		}
		if(getNumberOfChars() % 2 != 0) {
			int c = getNumberOfChars() - 1;
			sb.append(String.format("\n[%08X]", s.charAt(c) << 16));
			sb.append(' ');
			sb.append(s.charAt(c));
		}
				
		return sb.toString();
	}
	
	public void setIndex(int index) {
		ref.index = index;
	}
	
	public void setOffset(int offset) {
		ref.offset = offset;
	}
}
