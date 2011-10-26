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

public class H8String extends HString {

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
	H8String(char[] chars8bit, int startIndex, int endIndex){
//vrb.println(">H8String: startIndex="+startIndex +", endIndex="+endIndex);
    	this.chars = getH8Array(chars8bit, startIndex, endIndex);
    	this.length = (char)(endIndex - startIndex);
		this.arrayLen = (char)((length+1)>>1);
		hashCode();
//vrb.println(">H8String: hash="+hash +", arrayLen="+(int)arrayLen +", length="+(int)length);
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
	H8String(char[] chars8bit, int length){
		this(chars8bit, 0, length);
	}

	public boolean equals(char[] jchars, int length) {
	   	if(length > Math.min(length, jchars.length)) throw new StringIndexOutOfBoundsException(length);
	   	int arrLength = (length+1)>>1;
		if (length != this.length || (length != this.arrayLen && arrLength != this.arrayLen))  return false;

//vrb.println(">equals10: lenght="+length +", this.length="+(int)this.length +", jchar.length="+jchars.length); HString.printChars(jchars, length);

		assert length == this.length;
		char[] h8chars = this.chars;
		int jIndex = length-1;
		int hIndex  = jIndex>>1;
		if( (length&1) != 0){
			if( (h8chars[hIndex--]>>8) != jchars[jIndex--]) return false;
		}
//vrb.println(">equals20: hIndex="+hIndex +", jIndex="+jIndex +", jchar.length="+jchars.length); HString.printChars(h8chars, h8chars.length);
		while (hIndex >= 0){
			int hval = h8chars[hIndex--];
			if ( (hval & 0xFF) != jchars[jIndex--] || (hval>>8) != jchars[jIndex--]) break;
		}
//vrb.println(">equals30: hIndex="+hIndex +", jIndex="+jIndex);
		return jIndex < 0;
	}

	/**
     * Returns the java hash code for this string: <i>javaHashCode == this.toString().hashCode()</i> (see also java.lang.String).
     * @return  a hash code value for this object.
     */
    public int hashCode() {
    	if(hash != 0) return hash;
    	int haschCode = 0;
    	int end = length>>1;
    	for(int n = 0; n < end; n++) haschCode = (haschCode*31 + (chars[n]>>>8) )*31 + (chars[n] & 0xFF);
    	if( (length&1) != 0) haschCode = haschCode*31 + (chars[end]>>>8);
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
    	if(index < 0 || index >= length) throw new StringIndexOutOfBoundsException(index);
    	int value = chars[index>>1];
		if( (index&1) == 0 ) value = value>>>8;  else value = value& 0xFF;
		return (char)value;
    }


	public int indexOf(int ch, int fromIndex){ 
    	if(fromIndex >= length) throw new StringIndexOutOfBoundsException(fromIndex);
		int end = length>>1;
		if( (fromIndex&1) != 0 && (chars[fromIndex>>1]&0xFF) == ch )  return fromIndex;

		int index = (fromIndex+1)>>1;
		int hch = ch<<8;
		int m = -1;
		while(index < end){
			int value = chars[index];
			if( (value&0xFF00) == hch) { m = 0; break; }
			if( (value&0xFF) == ch)  { m = 1; break; }
			index++;
		}
		if(m >= 0)  return index*2 + m;  //found
		else if (index == end){
			if( (length&1) != 0){
				if( (chars[end]&0xFF00) == hch) return end*2;
			}
		}
		return -1;
	}

	public int indexOf(int ch){
		return indexOf(ch, 0);
	}
	
	public int lastIndexOf(int ch){
		if( (ch& -0x100) != 0) return -1;
		int n = length>>1;
//vrb.println("LI0: length="+(int)length +", n="+n);
		int hch = ch<<8;
		if( (length&1) != 0){
			if( (chars[n]&0xFF00) == hch) return n*2;
		}
		n--;
//vrb.println("LI1: length="+(int)length +", n="+n);
		int m = -1;
		while(n >= 0){
			int value = chars[n];
			if( (value&0xFF00) == hch) { m = 0; break; }
			if( (value&0xFF) == ch)  { m = 1; break; }
			n--;
		}
		if(n >= 0) return n*2 + m;
		return -1;
	}

	public void getChars(int srcStartIndex, int srcEndIndex, char[] dstChars, int dstStartIndex){
		if(checkPre){
		   	if(srcEndIndex > length) throw new StringIndexOutOfBoundsException(srcEndIndex);
		   	if( srcStartIndex < 0 || srcStartIndex > srcEndIndex ) throw new StringIndexOutOfBoundsException(srcStartIndex);
		   	if(dstStartIndex > dstChars.length) throw new StringIndexOutOfBoundsException(srcStartIndex);
		}
	   	int length = Math.min(this.length, srcEndIndex-srcStartIndex);
		length = Math.min(dstChars.length-dstStartIndex, length);
		for(int n = 0; n < length; n++) dstChars[dstStartIndex++] = charAt(srcStartIndex++);
	}

	public HString substring(int startIndex, int endIndex){
		if(endIndex > length) throw new StringIndexOutOfBoundsException(endIndex);
		if(startIndex > endIndex) throw new StringIndexOutOfBoundsException(startIndex);
		int length = endIndex - startIndex;
		char[] dstChars = new char[length];
		getChars(startIndex, endIndex, dstChars, 0);
		return new H8String(dstChars, length);
	}

	public HString substring(int start){
		return substring(start, length);
	}
	
    public String toString(){
//vrb.println(">toString");
//    	printlnChars(chars, chars.length);
    	char[] strChars = new char[length];
    	int index = length-1;
    	if((length&1) == 1) {
    		strChars[index] = (char)(chars[index>>1]>>>8);
    		index--;
    	}
    	for(; index > 0; index -= 2){
    		int couple = chars[index>>1];
       		strChars[index] = (char)(couple & 0xFF);
       		strChars[index-1] = (char)(couple>>8);
       	}
//vrb.println("<toString");
//    	printlnChars(strChars, strChars.length);
    	return new String(strChars);
    }
    
    public int sizeInByte() {
    	return length;
    }
}
