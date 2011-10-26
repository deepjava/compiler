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

package ch.ntb.inf.deep.strings;

public class H16String extends HString {

    /**
     * Constructs a new string of 8 bit (latin1) chars.
     * The new string begins at the specified <code>startIndex</code> and
     * extends to the character at index <code>endIndex - 1</code>.
     * Thus the length of the string is <code>endIndex-startIndex</code>.
     * 
     * @param chars8bit  array containing 8 bit chars, one per array element (the lower 8 bits of each char are picked up to form an new char).
     * @param startIndex the beginning index, inclusive.
     * @param endIndex  the ending index, exclusive.
     */
	H16String(char[] chars, int startIndex, int endIndex){
		if(endIndex > chars.length) throw new StringIndexOutOfBoundsException(endIndex);
		if(startIndex > endIndex) throw new StringIndexOutOfBoundsException(startIndex);
		int length = endIndex - startIndex;
		char[] dstChars = new char[length];
		System.arraycopy(chars, startIndex, dstChars, 0, length);
    	this.chars = dstChars;
    	this.length = (char)length;
		this.arrayLen = (char)length;
		hashCode();
	}

    /**
     * Constructs a new string of 8 bit (latin1) chars.
     * The new string begins with the first char in the given array and
     * extends to the character at index <code>length - 1</code>.
     * Thus the length of the string is <code>length</code>.
     * 
     * @param chars8bit  array containing 8 bit chars, one per array element.
     * @param      length   length of the new string.
     */
	H16String(char[] chars8bit, int length){
		this(chars8bit, 0, length);
	}


	public boolean equals(char[] jchars, int length) {
	   	if(length > Math.min(length, jchars.length)) throw new StringIndexOutOfBoundsException(length);
		if (length != this.length)  return false;

		char[] hchars = this.chars;
		length--;
		while (length >= 0 && hchars[length] == jchars[length] )  length--;
		return length < 0;
	}

	/**
     * Returns the java hash code for this string: <i>javaHashCode == this.toString().hashCode()</i> (see also java.lang.String).
     * @return  a hash code value for this object.
     */
    public int hashCode() {
    	if(hash != 0) return hash;
    	int haschCode = 0;
    	int end = length;
    	for(int n = 0; n < end; n++)  haschCode = haschCode*31 + chars[n];
    	hash = haschCode;
        return haschCode;
    }
    
    /**
     * Returns the <code>char</code> value at the specified index.
     * See base class for details.
     * @param index
     * @return the char, specified by <code>index</code>
     */
    public char charAt(int index){
    	if(index >= 0 && index < length)  return chars[index];  else throw new StringIndexOutOfBoundsException(index);
    }

	public int indexOf(int ch, int fromIndex){ 
    	if(fromIndex >= length) throw new StringIndexOutOfBoundsException(fromIndex);
		int end = length;
		int index = fromIndex;
		while(index < end && ch != chars[index]) index++;
		if(index < end) return index; else return -1;
	}

	public int indexOf(int ch){ 
		int end = length;
		int index = 0;
		while(index < end && ch != chars[index]) index++;
		if(index < end) return index; else return -1;
	}

	public int lastIndexOf(int ch){
		int index = length-1;
		while(index >= 0 && ch != chars[index]) index--;
		return index;
	}
	
	public void getChars(int srcStartIndex, int srcEndIndex, char[] dstChars, int dstStartIndex){
		if(checkPre){
		   	if(srcEndIndex > length) throw new StringIndexOutOfBoundsException(srcEndIndex);
		   	if( srcStartIndex < 0 || srcStartIndex > srcEndIndex ) throw new StringIndexOutOfBoundsException(srcStartIndex);
		   	if(dstStartIndex > dstChars.length) throw new StringIndexOutOfBoundsException(srcStartIndex);
		}
	   	int length = Math.min(this.length, srcEndIndex-srcStartIndex);
		length = Math.min(dstChars.length-dstStartIndex, length);
		System.arraycopy(chars, srcStartIndex, dstChars, dstStartIndex, length);
	}

	public HString substring(int start, int end){
		if(end > this.length) throw new StringIndexOutOfBoundsException(end);
		if(start > end) throw new StringIndexOutOfBoundsException(start);
		int length = end - start;
		char[] dstChars = new char[length];
		System.arraycopy(chars, start, dstChars, 0, length);
		return new H16String(dstChars, length);
	}

	public HString substring(int start){
		return substring(start, length);
	}

    public String toString(){
     	return new String(chars);
    }
    
    public int sizeInByte() {
    	return 2 * length;
    }
}
