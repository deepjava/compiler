package ch.ntb.inf.deep.strings;

import java.io.DataInput;
import java.io.IOException;
import java.io.PrintStream;


public abstract class HString implements IhStringConsts {
	static final boolean verbose = false, testAssertion = true, checkPre = true, checkPost = true;
	static PrintStream vrb = System.out;

//	private static DataInput clsIn;
	private static StringTable stab;

	// --- static fields
	private static byte[] byteBuffer;
	private static char[] charBuffer;
	static {
		byteBuffer = new byte[100];
		charBuffer = new char[100];
	}

	// --- instance fields
	HString next;
	char[] chars;
	int hash; // == this.toString().hashCode()
	char length; // string length (number of chars)
	char arrayLen; // int-array length (number of int values)
	int flags; // set {sattrIs7bitChars, sattrIs8bitChars, sattrIs16bitChars}

//	public static void setClassFileInput(DataInput classFileInput){
//		clsIn = classFileInput;
//	}

	public static void releaseBuffers(){
		if(byteBuffer.length > 100){
			byteBuffer = new byte[100];
			charBuffer = new char[100];			
		}
	}
	public static void setStringTable(StringTable stringTable){
		stab = stringTable;
	}

	public static int getBitSetUnion(char[] chars, int length) {
	   	if(checkPre && (length < 0 || length > chars.length) ) throw new StringIndexOutOfBoundsException(length);
		int charSet = 0;
		while (--length > 0) charSet = charSet | chars[length];
		return charSet;
	}

	public static int getHashCode(char[] chars, int length) {
    	if(checkPre) if( length < 0 || length > chars.length) throw new StringIndexOutOfBoundsException(length);
		int hash = 0;
		for(int index = 0; index < length; index++) hash = hash*31 + chars[index];
		return hash;
	}

	public static boolean isH8CharArray(char[] chars, int length) {
	   	if(checkPre && (length < 0 || length > chars.length) ) throw new StringIndexOutOfBoundsException(length);
		int charSet = 0;
		while (--length > 0) charSet = charSet | chars[length];
		return (charSet& -0x100) == 0;
	}


	/**
     * Creates a new array of 8 bit chars in compact form: 2 8-bit chars per java char.
     * The new array begins at the specified <code>startIndex</code> and
     * extends to the character at index <code>endIndex - 1</code>.
	 * The length of the new array is <code>( (endIndex-startIndex) + 1) / 2</code>.
	 * @param jchars  array containing 8 bit chars, one per array element (the lower 8 bits of each char are picked up to form an new char).
     * @param startIndex the beginning index, inclusive.
     * @param endIndex  the ending index, exclusive.
	 * @return
	 */
	static char[] getH8Array(char[] jchars, int startIndex, int endIndex) {
    	if(checkPre){
		   	if(endIndex > jchars.length) throw new StringIndexOutOfBoundsException(endIndex);
		   	if(startIndex < 0 || startIndex > endIndex) throw new StringIndexOutOfBoundsException(startIndex);
    	}
		int length = endIndex - startIndex;
//vrb.println(">getH8Array10: src"); printChars(chars, length);
		int arrLength = (length+1)>>1;
		char[] h8Array = new char[arrLength];
		int coupleLength = length>>1;
//vrb.println("getH8Array20: startIndex="+startIndex +", endIndex="+endIndex +", arrLength="+arrLength +", coupleLength="+coupleLength);
		int n = 0;
		while(n < coupleLength){
			h8Array[n] = (char)( (jchars[startIndex++]<<8) | (jchars[startIndex++]& 0xFF)); // truncate high byte
			n++;
		}
		if( (length&1) != 0)  h8Array[n] = (char)(jchars[startIndex]<<8);
//		vrb.println("<getH8Array: dst"); printChars(h8Array, h8Array.length);
		return h8Array;
	}

	/**
	 * Converts the given <code>string</code> to a new <code>hstring</code> (of type HString).
	 * <br>Post conditions: <code>string.equals( hstring.toString() )</code> = <code>true</code>.
	 * Creates a new HString which contains the same chars as the.
	 * @param string the source string.
	 * @return the new HString or <code>null</code>.
	 */
	public static HString getHString(String string) {
		if(string == null) return null;
		int length = string.length();
//vrb.println(">getHString: string="+string + ", length="+string);
		if (length > charBuffer.length)  charBuffer = new char[length];
		string.getChars(0, length, charBuffer, 0);
		HString hstring;
		if ( isH8CharArray(charBuffer, length) )
			hstring = new H8String(charBuffer, length);
		else
			hstring = new H16String(charBuffer, length);
//vrb.println("<getHString");
		return hstring;
	}

	public static int readUTF(DataInput classFileInput) throws IOException {// DataInputStream
		int utfLength = classFileInput.readUnsignedShort();
		if (utfLength > byteBuffer.length) {
			byteBuffer = new byte[utfLength];
			charBuffer = new char[utfLength];
		}
		classFileInput.readFully(byteBuffer, 0, utfLength);

		int hashCode = 0;
		int inByteNr = 0;
		int charSet = 0;
		int stringLength = 0;
		int bx = 0;
		while (inByteNr < utfLength) {
			int charValue = byteBuffer[bx++];
if (verbose) vrb.println("\ncp A: byteNr=" + inByteNr + ", bx=" + bx + ", stringLength=" + stringLength + ", ch=0x" + Integer.toHexString(charValue));

			if (charValue < 0) {// char consists of more then 1 byte
				charValue &= 0x1F;
				if ((charValue >> 5 & 0x7) - 6 == 0) {// 2 byte (1 follow byte) char
					charValue = (charValue << 6) | (byteBuffer[bx++] & 0x3F);
					inByteNr++;
				} else {// 3 byte (2 follow bytes) char
					charValue = (charValue << 6) | (byteBuffer[bx++] & 0x3F);
					charValue = (charValue << 6) | (byteBuffer[bx++] & 0x3F);
					inByteNr += 2;
				}
			}
			charBuffer[stringLength++] = (char)charValue;
			charSet |= charValue;
			hashCode = hashCode * 31 + charValue;
			inByteNr++;
		}
if (verbose) 	vrb.println("cp G: byteNr=" + inByteNr + ", bx=" + bx + ", stringLength=" + stringLength + ", ch=0x" + Integer.toHexString(0xFF) + ", utfLength=" + utfLength);

if (testAssertion) {
			assert inByteNr == utfLength;
//			String str = new String(charBuffer);
//			int hc = str.hashCode();
//			assert hc == hashCode;
}
			charBuffer[stringLength] = 0; // in case of odd string length of 8 bit strings (latin1, H8String)
		return stringLength;
	}

	public static HString readUTFandRegister(DataInput classFileInput) throws IOException {
		int lenght = readUTF(classFileInput);
		HString hstr = stab.insertCondAndGetEntry(charBuffer, lenght);
		return hstr;
	}

	public int length() {
		return length;
	}

    /**
     * Returns the <code>char</code> value at the specified index.
     * An index ranges from <code>0</code> to <code>length() - 1</code>. The first <code>char</code> value of the sequence
     * is at index <code>0</code>, the next at index <code>1</code>.
     * @param index
     * @return the char, specified by <code>index</code>, <code>a StringIndexOutOfBoundsException</code>
     *  if the <code>index</code> was out of range.
     */
	public abstract char charAt(int index);
	
	public abstract boolean equals(char[] chars, int length);
	
	public abstract int indexOf(int ch);
	public abstract int indexOf(int ch, int fromIndex);
	public abstract int lastIndexOf(int ch);
	
	public abstract HString substring(int start, int end);
	public abstract HString substring(int start);

	
	public abstract void getChars(int srcStartIndex, int srcEndIndex, char[] dstChars, int dstStartIndex);

	public boolean equals(HString cmpStr) {
//		assert hash != 0 && cmpStr.hash != 0;
		if (hash != cmpStr.hash || length != cmpStr.length) return false;
		int n = this.arrayLen - 1;
		char[] chars = this.chars;
		char[] cChars = cmpStr.chars;
		while (n >= 0 && chars[n] == cChars[n])  n--;
		return n < 0;
	}

	public abstract String toString();


	
	//--- debug primitives

	public static void printChars(char[] chars, int length){
		int len = Math.min(length, chars.length);
		for(int n = 0; n < len; n++)  vrb.printf(" ([%1$d]=0x%2$4x)", n, (int)chars[n]);
	}

	public static void printlnChars(char[] chars, int length){
		int len = Math.min(length, chars.length);
		for(int n = 0; n < len; n++)  vrb.printf("[%1$d]=0x%2$4x\n", n, (int)chars[n]);
	}
}
