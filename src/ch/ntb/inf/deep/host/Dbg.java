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

package ch.ntb.inf.deep.host;

import java.io.PrintStream;



public class Dbg implements  ICjvmInstructionMnemonics, ICclassFileConstsAndMnemonics {
	public static PrintStream vrb = StdStreams.vrb;
	public static final boolean verbose = false;

	public static final  char newLineChar = '\n';	
	public static final  char categorySeparator = '|';

	public void SetPrintStream(PrintStream verboseStream){
		vrb = verboseStream;
	}

	public static void indent(int indentLevel){
		indentLevel = indentLevel*3;
		while(indentLevel-- > 0) vrb.print(' ');
	}

	public static void println(){
		vrb.println();
	}

	public static void printSpace(){
		vrb.print(' ');
	}

	public static void printTab(){
		vrb.print('\t');
	}

	public static void print(char ch){
		vrb.print(ch);
	}

	private static String getElementName( String elemName, char category){
		if( elemName.charAt(0) == categorySeparator ){
			int pos = 0, endPos = 0;
			char cat = 0;
			do {
				pos = endPos + 1;
				cat = elemName.charAt(pos);
				endPos = elemName.indexOf(categorySeparator, pos);
			} while (cat != category && pos > 0);
			if (endPos < 0 ) endPos = elemName.length();
			if (cat == category) elemName = elemName.substring(pos+1, endPos);
		}
		return elemName;
	}
	
	private static String setToString(int set, String[] elemNames, char itemCategory){
		// itemCategory = {'C', 'F', 'M'} (for {Class, Field, Method})
		StringBuilder sb = new StringBuilder();

		sb.append('{');
//		if(namesStartwithElemNr > 0) set = set >>> namesStartwithElemNr;
		int elemNr = 31;
		if ((set& -0x10000) == 0) {
			elemNr = 15;  set = set << 16;
		}
		//--- select first element
		while (set > 0) {
			set = set << 1;
			elemNr--;
		}
		if (set != 0) {
			if (elemNr >= elemNames.length){
				sb.append('?');
				int diff = elemNr - elemNames.length + 1;
				set = set << diff;
				elemNr -= diff;
			}

			if (set < 0) sb.append(getElementName(elemNames[elemNr], itemCategory));
			set = set << 1; elemNr--;
			while (set != 0) {
				if (set < 0) {
					sb.append(',');  sb.append( getElementName(elemNames[elemNr], itemCategory));
				}
				set = set << 1; elemNr--;
			}
		}
		sb.append('}');
		return sb.toString();
	}
/*
 * set	elemNr
 */
	public static void printJvmInstr(int instrAddr, int opc){
		vrb.printf("  @%1$4d,%2$3d \t%3$s ", instrAddr, opc, bcMnemonics[opc]);
		int bcAttrSet =( bcAttrTab[opc] >> bcapBase) & 0xFFF;
		vrb.println(setToString(bcAttrSet, bcAttributes, '?'));
	}

	public static void printFlags(int flags, char category){
		vrb.print(setToString(flags, apfIdents, category));
	}
	
	public static void printAccAndPropertyFlags(int flags, char category){
		printFlags(flags, category);
	}
	public static void printAccAndPropertyFlags(int flags){
		printFlags(flags, '?');
	}

	public static void printJavaAccAndPropertyFlags(int flags, char category){
		printFlags(flags & apfSetJavaAccAndProperties, category);
	}
	public static void printJavaAccAndPropertyFlags(int flags){
		printFlags(flags & apfSetJavaAccAndProperties, '?');
	}

	public static void printDeepAccAndPropertyFlags(int flags, char category){
		printFlags(flags & dpfSetProperties, category);
	}
	public static void printDeepAccAndPropertyFlags(int flags){
		printFlags(flags & dpfSetProperties, '?');
	}

	public static void printCpTagIdent(int cptNumber, int fieldWidth){
		int strLength = cptIdents[cptNumber].length();
		int nofSpaceChars = fieldWidth - strLength;
		vrb.print(cptIdents[cptNumber]); while(nofSpaceChars-- > 0) vrb.print(' ');
	}

	static char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
	static void printHex(int value){
		int digitVal = value & 0xF;
		if( (value & -0x10) != 0) printHex8(value >>> 4);
		vrb.print(hexDigits[digitVal]);
	}

	static char[] printDigits = new char[8];
	static void printHex8(int value){
		int index = printDigits.length-1;
		do{
			printDigits[index--] = hexDigits[value & 0xF];
			value = value >>> 4;
		}while(value != 0);
		while(index >= 0) printDigits[index--] = '0';
		vrb.print( new String(printDigits) );
	}

//	public static void main(String[] args){
//		for(int elem = 0; elem < bcAttributes.length; elem++){
//			int set = 1<<elem;
//			vrb.println(setToString(set, bcAttributes, '\0'));
//		}
//		vrb.println(setToString(7, bcAttributes, '\0') );
//	}
}
/*Output:
*/
