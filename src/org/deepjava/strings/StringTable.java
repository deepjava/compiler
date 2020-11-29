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

package org.deepjava.strings;

import java.io.PrintStream;

import org.deepjava.host.StdStreams;

public class StringTable {
	static final boolean verbose = false, testAssertion = true;
	static PrintStream vrb = StdStreams.vrb;
	
	private static StringTable strTab;

	public HString undefIdent;
	private HString[] tab;
	private int nofEntries;
	private final int hashCodeMask;

	public static void resetTable(){
		if (strTab != null){
			strTab.tab = new HString[strTab.tab.length];
			strTab.nofEntries = 0;
			if (strTab.undefIdent != null)  strTab.undefIdent = strTab.insertCondAndGetEntry(strTab.undefIdent );
		}
	}

	public static int hashCode(char[] val, int length) {
		int hashCode = 0;
		for (int off = 0; off < length; off++)  hashCode = 31 * hashCode + val[off];
		return hashCode;
	}

	public static void createSingleton(int initialTabLength, HString undefIdent) {
		assert strTab == null;
		if(verbose) vrb.println("createSingleton: undefIdent="+undefIdent);
		strTab = new StringTable(initialTabLength, undefIdent);
	}

	public static void createSingleton(int initialTabLength, String undefIdent) {
		HString uid = HString.getHString(undefIdent);
		createSingleton(initialTabLength, uid);
	}

	public static StringTable getInstance() {
		return strTab;
	}

	public int length(){
		return nofEntries;
	}

	private StringTable(int initialTabLength, HString undefIdent) {
		int mask = 4;
		while (mask < initialTabLength)  mask <<= 1;
		tab = new HString[mask];
		hashCodeMask = mask - 1;
		this.undefIdent = this.insertCondAndGetEntry(undefIdent);
	}

	public HString getEntry(HString hstring) {
		int hash = hstring.hash;
		int lvArrayLength = hstring.arrayLen;
		int lvLength = hstring.length;
		
		HString entry = tab[hash & hashCodeMask];
		while (entry != null && lvLength > entry.length)  entry = entry.next;	

		HString foundStr = null;
		while (entry != null && lvLength == entry.length) {
			if (lvArrayLength == entry.arrayLen && hash == entry.hash) {
				char[] eHchars = entry.chars;
				char[] hHchars = hstring.chars;
				lvArrayLength--;
				while (lvArrayLength >= 0 && eHchars[lvArrayLength] == hHchars[lvArrayLength]) lvArrayLength--;
				if (lvArrayLength < 0) {
					foundStr = hstring;
					break;
				}
			}
			entry = entry.next;
		}
		return foundStr;
	}

	public HString insertCondAndGetEntry(char[] jchars, int length) {
		int hashCode = hashCode(jchars, length);
		int index = hashCode  & hashCodeMask;
		HString entry = tab[index];
		HString pred = null;
		while (entry != null && length > entry.length)  { pred = entry;   entry = entry.next; }

		HString foundStr = null;
		while (entry != null && length == entry.length) {
			if (hashCode == entry.hash) {
				if (entry.equals(jchars, length)) {
					foundStr = entry;
					break;
				}
			}
			entry = entry.next;
		}

		if (foundStr == null) {
			nofEntries++;
			if( HString.isH8CharArray(jchars, length) )  foundStr = new H8String(jchars, length);
			else  foundStr = new H16String(jchars, length);

			if (pred == null) {
				foundStr.next = tab[index];   tab[index] = foundStr;
			}else{
				foundStr.next = pred.next;  pred.next = foundStr;
			}
		}
		return foundStr;
	}

	public HString insertCondAndGetEntry(HString newString) {
		if (verbose) {vrb.print(">insertCondAndGetString_HS: newString="); vrb.println(newString); }
		int length = newString.length();
		int hashCode = newString.hashCode();
		int tabIndex = hashCode & hashCodeMask;
		HString entry = tab[tabIndex];

		HString pred = null;
		while (entry != null && length > entry.length)  { pred = entry;   entry = entry.next; }

		HString foundStr = null;
		while (entry != null && length == entry.length) {
			if (hashCode == entry.hash) {
				if(verbose) {
					vrb.print(">insertCondAndGetString_HS 10: length="+length + ", entry.length="+(int)entry.length+ ", entry: "); vrb.println(entry);
				}
				if (newString.equals(entry)) {
					if(verbose) vrb.println("<str found>");
					foundStr = entry;
					break;
				}
			}
			entry = entry.next;
		}

		if (verbose) {
			vrb.print(">insertCondAndGetString_HS 20: length="+length);
			if(entry == null) vrb.print(" entry==null ");  else  vrb.println(entry);
			vrb.println();
		}

		if (foundStr == null) {	// insert new String
			nofEntries++;
			foundStr = newString;
			if (pred == null) {
				newString.next = tab[tabIndex];
				tab[tabIndex] = newString;
			} else {
				newString.next = pred.next;  pred.next = newString;
			}
		}
		if(verbose) { vrb.print("<insertCondAndGetString_HS: foundStr="); vrb.println(foundStr);}
		return foundStr;
	}


	public HString insertCondAndGetEntry(String jstring) {
		int jlength = jstring.length();
		char[] jchars = new char[jlength];
		jstring.getChars(0, jlength, jchars, 0);
		return insertCondAndGetEntry(jchars, jlength);
	}
		
	//--- debug primitives:
	
	public void printHeadLine() {
		vrb.println("\n  entry  hashCode  length arrLen  uFlags   string");		
	}

	public void print(String title) {
		vrb.print("\n\n"+title + ", length=" + nofEntries + ", hashTab.length="+ tab.length);
		vrb.printf(", loadFactor=%1$4.2f", (float)nofEntries/tab.length);
		int lineNr = 0;
		for (int tinx=0; tinx < tab.length; tinx++){
			HString entry = tab[tinx];
			if (entry != null) {
				if( (lineNr & (32-1)) == 0) printHeadLine();
				lineNr++;
				vrb.printf(" [%1$4d]", tinx);
				while (entry != null) {
					vrb.printf("\t0x%1$8x %2$6d %3$6d  0x%4$4x  \"%5$s\"\n" , entry.hash, (int)entry.length, (int)entry.arrayLen, entry.flags, entry);
					if(verbose){
						if(entry.arrayLen == 1) vrb.printf("\t[0]=0x%1$4x\n", (int)entry.chars[0]);
						else if(entry.arrayLen == 2) vrb.printf("\t[0]=0x%1$4x, [0]=0x%1$4x\n", (int)entry.chars[0], (int)entry.chars[1]);
						else if(entry.arrayLen > 2) vrb.printf("\t[0]=0x%1$4x, [1]=0x%2$4x, [len-1]=0x%3$4x\n", (int)entry.chars[0], (int)entry.chars[1], (int)entry.chars[entry.arrayLen-1]);
					}
					entry = entry.next;
				}
			}
		}
	}

//	public static void main(String[] args) {
//		char[] chars = new char[]  { 'a', 'b', 'c', 'd'};
//		
//		StringTable st = new StringTable(7);
//		st.print("String Table:"); Out.println();
//
//		st.insertCondAndGetString(chars, 2);
//		st.print("String Table 1:"); Out.println();
//
//		st.insertCondAndGetString(chars, 2);
//		st.print("String Table 2:"); Out.println();
//
//		st.insertCondAndGetString(chars, 1);
//		st.print("String Table 3:"); Out.println();
//
//		st.insertCondAndGetString(chars, 4);
//		st.print("String Table 4:"); Out.println();
//
//		st.insertCondAndGetString(chars, 3);
//		st.print("String Table 5:"); Out.println();
//
//		chars[0] = 'b';
//		st.insertCondAndGetString(chars, 3);
//		st.print("String Table 6:"); Out.println();
//		st.insertCondAndGetString(chars, 4);
//		st.print("String Table 7:"); Out.println();
//		
//		chars = new char[]  { ' ', ' ', ' ', ' ', ' ', ' ', ' '};
//		st.insertCondAndGetString(chars, 6);
//		st.print("String Table 11:"); Out.println();
//		st.insertCondAndGetString(chars, 7);
//		st.print("String Table 12:"); Out.println();
//	}
}
