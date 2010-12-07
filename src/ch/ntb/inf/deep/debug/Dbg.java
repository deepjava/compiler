package ch.ntb.inf.deep.debug;

import java.io.PrintStream;

import ch.ntb.inf.deep.host.StdStreams;


public class Dbg implements  ICjvmInstructionOpcsAndMnemonics, ICclassFileConstsAndMnemonics {
	public static PrintStream vrb = StdStreams.vrb;
	public static final boolean verbose = false;

	public static final  char newLineChar = '\n';	

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
	
	private static String setToString(int set, String[] elemNames, int namesStartwithElemNr){
		assert elemNames != null: "pre2";
		assert namesStartwithElemNr >= 0 && namesStartwithElemNr < 32: "pre2";
		StringBuilder sb = new StringBuilder();

//		sb.append("0x");
//		sb.append(Integer.toHexString(set));

		sb.append('{');
		if(namesStartwithElemNr > 0) set = set >>> namesStartwithElemNr;
		int elemNr = 31;
		if( (set& -0x10000) == 0) {
			elemNr = 15;  set = set << 16;
		}
		//--- select firs element
		while(set > 0) {
			set = set << 1;
			elemNr--;
		}
		if(set != 0){
			if(elemNr >= elemNames.length){
				sb.append('?');
				int diff = elemNr - elemNames.length + 1;
				set = set << diff;
				elemNr -= diff;
			}

			if(set < 0) sb.append(elemNames[elemNr]);
			set = set << 1; elemNr--;
			while(set != 0){
				if(set < 0){
					sb.append(',');  sb.append(elemNames[elemNr]);
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
		vrb.println(setToString(bcAttrSet, bcAttributes, 0));
	}

	public static void printAccAndPropertyFlags(int flags){
		vrb.print(setToString(flags, apfIdents, 0));
	}

	public static void printJavaAccAndPropertyFlags(int flags){
		vrb.print(setToString(flags & apfSetJavaAccAndProperties, apfIdents, 0));
	}

	public static void printDeepAccAndPropertyFlags(int flags){
		vrb.print(setToString(flags & dpfSetProperties, apfIdents, 0));
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

	public static void main(String[] args){
		for(int elem = 0; elem < bcAttributes.length; elem++){
			int set = 1<<elem;
			vrb.println(setToString(set, bcAttributes, 0));
		}
		vrb.println(setToString(7, bcAttributes, 0));
	}
}
/*Output:
*/