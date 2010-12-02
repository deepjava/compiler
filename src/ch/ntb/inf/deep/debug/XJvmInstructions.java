package ch.ntb.inf.deep.debug;


//import java.io.PrintStream;

class XJvmInstructions implements XJvmInstructionMnemonics {
//	private static PrintStream out = System.out;
//	private static final boolean verbose = false;
//	private static final String bcAttributes = "Undef,New,Call,Switch,Return,UncondBranch,CondBranch,Branch";
//
//	private static String setToString(int set, int mask, String elemNames){
//		set = set & mask;
//		StringBuilder sb = new StringBuilder();
//		sb.append('{');
//		boolean comma = false;
//		int enLength = elemNames.length();
//		int namePos = 0;
//		int elemMask = 1;
//		while(mask != 0 && namePos >= 0){
//			//assert namePos points to the first char of the current element name
//			int endPos = elemNames.indexOf(',', namePos+1);
//			if (namePos < enLength) endPos = enLength;
//if(verbose) out.println("100: namePos="+namePos +", endPos="+endPos +", elemMask="+elemMask);
//			if ( (mask & elemMask & set) != 0){
//				if(comma) sb.append(',');
//				comma = true;
//if(verbose) out.println("110: namePos="+namePos +", endPos="+endPos +", elemMask="+elemMask);
//				if(endPos < 0) sb.append('?');
//				else{
//					while(namePos < enLength){
//						char ch = elemNames.charAt(namePos++);
//						if(ch == ',') break;
//						sb.append(ch);
//					}
//				}
//if(verbose) out.println("120: namePos="+namePos +", endPos="+endPos +", elemMask="+elemMask);
//			}else{// if ( (mask & elemMask & set) == 0)
//				endPos = elemNames.indexOf(',', namePos+1);
//				if(endPos > 0) namePos = endPos + 1;  else namePos = elemNames.length();
//			}
//			mask = mask & ~elemMask;
//			elemMask = elemMask << 1;
//		}
//		sb.append('}');
//
//		return sb.toString();
//	}
//
//	public static void main(String[] args){
//		int tabLength = bcAttrTab.length;
//		out.println("opcTable.length="+tabLength);
//
//		for(int n = 0; n < tabLength; n++){
//			int tabEntry = bcAttrTab[n];
//			int spChg = tabEntry>>28;
//			int flags = tabEntry>>20 & 0xFF;
//			int iLen = tabEntry>>8 & 0xF;
//			int opc = tabEntry & 0xFF;
//			String mnem = bcMnemonics[n];
//			int mnLen = mnem.length();
//			
//			if( (n & 0xF) == 0) out.println("\n[opc]\tmnemonic\tiLen\tspChg\tflags:");
//			out.printf("[%1$3d]\t%2$s", n, mnem);
//			if(mnLen < 8) out.print('\t');
//			
//			out.printf("\t%1$2d \t%2$3d \t0x%3$2x=%4$s", iLen, spChg, flags, setToString(flags, 0xFF, bcAttributes));
//			if (opc != n) {
//				out.print("\terror: n="+n + ", tab[n]&0xFF = "+(bcAttrTab[n] & 0xFF));
//				assert false;
//			}
//			out.println();
//		}
//
//		//---- test setToString( ... )
////		out.println("3, 0,   : "+setToString(3, 0, "")); // {}
////		out.println("1, 1,   : "+setToString(1, 1, "")); // {?}
////		out.println("0, 7, A,B,C: "+setToString(0, 7, "A,B,C")); // {}
////		out.println("1, 7, A,B,C: "+setToString(1, 7, "A,B,C")); // {}
////		out.println("2, 7, A,B,C: "+setToString(2, 7, "A,B,C")); // {}
////		out.println("3, 7, A,B,C: "+setToString(3, 7, "A,B,C")); // {}
////		out.println("4, 7, A,B,C: "+setToString(4, 7, "A,B,C")); // {}
////		out.println("5, 7, A,B,C: "+setToString(5, 7, "A,B,C")); // {}
////		out.println("6, 7, A,B,C: "+setToString(6, 7, "A,B,C")); // {}
////		out.println("7, 7, A,B,C: "+setToString(15, 7, "A,B,C")); // {}
//	}
}
/*Output:
*/