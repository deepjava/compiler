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

package ch.ntb.inf.deep.cg.arm;

import ch.ntb.inf.deep.cg.InstructionDecoder;

public class InstructionDecoderARM extends InstructionDecoder implements InstructionOpcs {

	/**
	 * Encode the assembler mnemonic into the machine instruction. Does not check
	 * if the parameters are correct.
	 * 
	 * @param mnemonic
	 *            String
	 * @return machine instruction
	 */

	private static int constToImm(int const32) {	// A5.2.4 p.200
		int rot = 0;
		while ((const32 & 0xffffff00) != 0) {
			const32 = Integer.rotateLeft(const32, 2);
			rot++;
		}
		return (rot << 8) + (const32);
	}
	
	private static int encodeShiftType(String type) {	// A8.4.1 p.291
		int typeCode = 0;
		for(int i = 0; i < shiftType.length; i++) {	// get shift type
			if (type.equals(shiftType[i].toLowerCase()))	typeCode = i;
		}
		return typeCode;
	}
	
	private static int encodeRegisterlist(int indexFirstPart, String[] parts) {	// Encoding of lists of ARM core registers  p.214	->	p.295
		int registerList = 0;
		
		for (int i = indexFirstPart; i < parts.length; i++) {
			if (parts[i].startsWith("r")) {
				int register = 0;
				register =  Integer.parseInt( parts[i].replaceAll("[^0-9]", "") );
				registerList |= (0x1 << register);
			}

			if (parts[i].contains("sp")) 	registerList |= (0x1 << 13);
			if (parts[i].contains("lr")) 	registerList |= (0x1 << 14);
			if (parts[i].contains("pc")) 	registerList |= (0x1 << 15);
		}
		
		return registerList;
	}
	
	private static int[] encodeAmode(String part) {	// p.2001
		int[] r = new int[2];	// PU = {P, U}
		if (part.contains("da")) {	// Decrement After.
			r[0] = 0;
			r[1] = 0;
			return r;
		}
		if (part.contains("db")) {	// Decrement Before.
			r[0] = 1;
			r[1] = 0;
			return r;
		}
		if (part.contains("ia")) {	// Increment After.
			r[0] = 0;
			r[1] = 1;
			return r;
		}
		if (part.contains("ib")) {	// Increment Before.
			r[0] = 1;
			r[1] = 1;
			return r;
		}
		return r;
	}
	
	private static int encodeMode(String part) {	// p.2007 -> p.1139
		if (part.contains("usr"))	return 0x10;
		if (part.contains("fiq"))	return 0x11;
		if (part.contains("irq"))	return 0x12;
		if (part.contains("svc"))	return 0x13;
		if (part.contains("mon"))	return 0x16;
		if (part.contains("abt"))	return 0x17;
		if (part.contains("hyp"))	return 0x1a;
		if (part.contains("und"))	return 0x1b;
		if (part.contains("sys"))	return 0x1f;
		return 0x0;
	}
	
//	private static int bankedRegToSYSm(String bankedReg, int R) {	// p. 1975
//		int SYSm = 0;
//		if (R == 0) {
//			for(int i = 0; i < bankedRegR0.length; i++) {
//				if (bankedReg.contains(bankedRegR0[i])) SYSm = i;
//			}
//		}
//		if (R == 1) {
//			for(int i = 0; i < bankedRegR1.length; i++) {
//				if (bankedReg.contains(bankedRegR1[i])) SYSm = i;
//			}
//		}
//		return SYSm;
//	}
	
	
	
	public int getCode(String mnemonic) {		
		
		int code=0;
		
		mnemonic = mnemonic.toLowerCase();
		mnemonic = mnemonic.trim();
//		if (mnemonic.substring(mnemonic.length()-1) != ";")	return 0;	// Error: ';' is missing
		
		// Condition codes	(Mnemonic extensions get removed)
		// ///////////////
		int cond = 14;	// 14 = " always" or "";
		for(int i = 0; i < condString.length; i++) {
			if (mnemonic.contains(condString[i])) {
				cond = i;
				mnemonic = mnemonic.replace(condString[i], "");		// remove mnemonic extension for Condition
				break;
			}
		}
		code |= (cond<<28);
		
		
		
		String[] parts =  mnemonic.split(" ");	// Split mnemonic by spaces
//		System.out.print("Number of parts:   ");
//		System.out.println(parts.length);
//		System.out.println(parts[0]);
//		System.out.println(parts[1]);
		
		// Instructions
		// ///////////////
		
		//  Data-processing
		String[] dataProcessingMnemonics = {
			"adc",	// p.300
			"add",	// p.308
			"and",	// p.324
			"bic",	// p.340
			"eor",	// p.382
			"orr",	// p.516
			"rsb",	// p.574
			"rsc",	// p.580
			"sbc",	// p.592
			"sub"	// p.710
		};		
		
		for(int i = 0; i < dataProcessingMnemonics.length; i++) {
			if (parts[0].startsWith(dataProcessingMnemonics[i])) {
				
				if ((parts[0].length()>=4) ? parts[0].substring(3, 4).equals("s") : false)	code |= (1<<20);	// S


				int d = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// Rd
				code |= (d<<12);	// Rd
				
				int n = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rn
				code |= (n<<16);	// Rn
				
				if (parts[3].substring(0,1).equals("#")) {	// (immediate)
					code |= (1<<25);
					int const32;
					if (parts[3].substring(1,3).equals("0x"))
						const32 = Integer.parseInt( parts[3].substring(3), 16 );	// const
					else
						const32 = Integer.parseInt( parts[3].replaceAll("[^0-9]", "") );	// const				
					int imm12 = constToImm(const32);
//					System.out.println(Integer.toHexString(const32));
//					System.out.println(Integer.toHexString(imm12));
					code |= imm12;
				}

				if (parts.length >= 6 ? parts[5].startsWith("r") : false) {	// (register-shifted register)
					code |= (1<<4);
					int m = Integer.parseInt( parts[3].replaceAll("[^0-9]", "") );	// Rm
					code |= m;	// Rm
					int typeCode = encodeShiftType(parts[4]);	// shift type
					code |= (typeCode << 5);				// shift type				
					int s = Integer.parseInt( parts[5].replaceAll("[^0-9]", "") );	// Rs
					code |= (s<<8);	// Rs		
				}

				else if (	parts.length >= 4 ? parts[3].startsWith("r") : false) {	// (register)
					int m = Integer.parseInt( parts[3].replaceAll("[^0-9]", "") );	// Rm
					code |= m;	// Rm
					if (parts.length >= (6)) {	// <shift> is not omitted
						int typeCode = encodeShiftType(parts[4]);	// shift type
						code |= (typeCode << 5);				// shift type
						int imm5 = Integer.parseInt( parts[5].replaceAll("[^0-9]", "") );	// imm5
						code |= (imm5 << 7);	// imm5
					}
				}

				if (dataProcessingMnemonics[i].equals("adc"))	code |= (0x5<<21);
				if (dataProcessingMnemonics[i].equals("add"))	code |= (0x4<<21);
				if (dataProcessingMnemonics[i].equals("and"))	code |= (0x0<<21);
				if (dataProcessingMnemonics[i].equals("bic"))	code |= (0xe<<21);
				if (dataProcessingMnemonics[i].equals("eor"))	code |= (0x1<<21);
				if (dataProcessingMnemonics[i].equals("orr"))	code |= (0xc<<21);
				if (dataProcessingMnemonics[i].equals("rsb"))	code |= (0x3<<21);
				if (dataProcessingMnemonics[i].equals("rsc"))	code |= (0x7<<21);
				if (dataProcessingMnemonics[i].equals("sbc"))	code |= (0x6<<21);
				if (dataProcessingMnemonics[i].equals("sub"))	code |= (0x2<<21);
			}
		}
		
		
		// MVN p.504
		if (parts[0].startsWith("mvn")) {
			
			code |= (0xf<<21);
			
			if ((parts[0].length()>=4) ? parts[0].substring(3, 4).equals("s") : false)	code |= (1<<20);	// S
			
			int d = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// Rd
			code |= (d<<12);	// Rd

			if (parts[2].substring(0,1).equals("#")) {	// (immediate)	p.504
				code |= (1<<25);
				int const32 = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// const
				int imm12 = constToImm(const32);
				code |= imm12;
			}

			if (parts[4].substring(0,1).equals("#")) {	// (register)	p.506
				int m = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rm
				code |= m;	// Rm
				if (parts.length >= 5) {	// <shift> is not omitted
					int typeCode = encodeShiftType(parts[3]);	// shift type
					code |= (typeCode << 5);				// shift type
					int imm5 = Integer.parseInt( parts[4].replaceAll("[^0-9]", "") );	// imm5
					code |= (imm5 << 7);	// imm5
				}
			}

			if (parts[4].substring(0,1).equals("r")) {	// (register-shifted register)	p.508
				code |= (1<<4);
				int m = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rm
				code |= m;	// Rm
				int typeCode = encodeShiftType(parts[3]);	// shift type
				code |= (typeCode << 5);				// shift type				
				int s = Integer.parseInt( parts[4].replaceAll("[^0-9]", "") );	// Rs
				code |= (s<<8);	// Rs		
			}
			
		}

		
		//  Compare and Test
		String[] CTMnemonics = {
			"cmn",	// p.364
			"cmp",	// p.370
			"teq",	// p.738
			"tst"	// p.744
		};		
		
		for(int i = 0; i < CTMnemonics.length; i++) {
			if (parts[0].startsWith(CTMnemonics[i])) {
				
				int n = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// Rn
				code |= (n<<16);	// Rn

				if (parts[2].substring(0,1).equals("#")) {	// (immediate)
					code |= (1<<25);
					int const32 = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// const				
					int imm12 = constToImm(const32);
					code |= imm12;
				}

				if ((parts.length>=5) ? parts[4].substring(0,1).equals("#") : false) {	// (register)
					int m = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rm
					code |= m;	// Rm
					if (parts.length >= 5) {	// <shift> is not omitted
						int typeCode = encodeShiftType(parts[3]);	// shift type
						code |= (typeCode << 5);				// shift type
						int imm5 = Integer.parseInt( parts[4].replaceAll("[^0-9]", "") );	// imm5
						code |= (imm5 << 7);	// imm5
					}
				}

				if ((parts.length>=5) ? parts[4].substring(0,1).equals("r") : false) {	// (register-shifted register)
					code |= (1<<4);
					int m = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rm
					code |= m;	// Rm
					int typeCode = encodeShiftType(parts[3]);	// shift type
					code |= (typeCode << 5);				// shift type				
					int s = Integer.parseInt( parts[4].replaceAll("[^0-9]", "") );	// Rs
					code |= (s<<8);	// Rs		
				}

				if (CTMnemonics[i].equals("cmn"))	code |= (0x17 << 20);
				if (CTMnemonics[i].equals("cmp"))	code |= (0x15 << 20);
				if (CTMnemonics[i].equals("teq"))	code |= (0x13 << 20);
				if (CTMnemonics[i].equals("tst"))	code |= (0x11 << 20);
			}
		}

		
		//  Rotate and Shift
		String[] RSMnemonics = {
			"asr",	// p.330
			"lsl",	// p.468
			"lsr",	// p.472
			"ror",	// p.568
			"rrx"	// p.572
		};	
		
		for(int i = 0; i < RSMnemonics.length; i++) {
			if (parts[0].startsWith(RSMnemonics[i])) {
				
				code |= (0xd << 21);
				
				if ((parts[0].length()>=4) ? parts[0].substring(3, 4).equals("s") : false)	code |= (1<<20);	// S
				
				int iOffset = 0;
				if ( (parts[1].substring(0, 1).equals("r")) && (parts[2].substring(0, 1).equals("r")) ) {	// Rd is not omitted
					iOffset = 1;					
					int d = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// Rd
					code |= (d<<12);	// Rd
				}

				if ((parts.length>=(3+iOffset)) ? parts[2+iOffset].substring(0, 1).equals("#") : false) {	// (immediate)					
					int m = Integer.parseInt( parts[1+iOffset].replaceAll("[^0-9]", "") );	// Rm
					code |= m;	// Rm
					int imm5 = Integer.parseInt( parts[2+iOffset].replaceAll("[^0-9]", "") );	// imm5
					code |= (imm5 << 7);	// imm5
				}

				if ((parts.length>=(3+iOffset)) ? parts[2+iOffset].substring(0, 1).equals("r") : false) {	// (register)
					code |= (1 << 4);
					int n = Integer.parseInt( parts[1+iOffset].replaceAll("[^0-9]", "") );	// Rn
					code |= n;	// Rn
					int m = Integer.parseInt( parts[2+iOffset].replaceAll("[^0-9]", "") );	// Rm
					code |= (m << 8);	// Rm
				}

				if (parts[0].startsWith("rrx")) {	// RRX			
					int m = Integer.parseInt( parts[1+iOffset].replaceAll("[^0-9]", "") );	// Rm
					code |= m;	// Rm
				}

				if (RSMnemonics[i].equals("asr"))	code |= (0x2<<5);
				if (RSMnemonics[i].equals("lsl"))	code |= (0x0<<5);
				if (RSMnemonics[i].equals("lsr"))	code |= (0x1<<5);
				if (RSMnemonics[i].equals("ror"))	code |= (0x3<<5);
				if (RSMnemonics[i].equals("rrx"))	code |= (0x3<<5);
			}
		}
		
		
		// MOV / MOVW / MOVT	p.484 / p.484 / p.491
		if (parts[0].startsWith("mov")) {
			
			if ((parts[0].length()>=4) ? parts[0].substring(3, 4).equals("s") : false)	code |= (1<<20);	// MOVS
			
			int d = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// Rd
			code |= (d << 12);	// Rd
			
			if ((parts[0].length()>=4) ? parts[0].substring(3, 4).equals("w") : false) {	// MOVW	p.484	(Encoding A2)
				code |= (0xc << 22);
				int imm16 = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// imm16
				code |= (imm16 & 0x0fff);			// imm12
				code |= ((imm16 & 0xf000) << 4);	// imm4
			}
			
			else if ((parts[0].length()>=4) ? parts[0].substring(3, 4).equals("t") : false) {	// MOVT	p.491
				code |= (0xd << 22);
				int imm16 = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// imm16
				code |= (imm16 & 0x0fff);			// imm12
				code |= ((imm16 & 0xf000) << 4);	// imm4
			}
			
			else {	// MOV / MOVS	(Encoding A1)
				code |= (0xd << 21);
				if (parts[2].substring(0,1).equals("#")) {	// (immediate) A1 p.484
					code |= (1<<25);
					int const32 = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// const				
					int imm12 = constToImm(const32);
					code |= imm12;
				}
				else {	// (register, ARM) p.488
					int m = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rm
					code |= m;	// Rm
				}
			}			
		}
		
		
		// LDREX / LDREXB / LDREXD / LDREXH: Synchronization primitives 1/3 p.205
		if (parts[0].startsWith("ldrex")) {	// p.432 ... p.438

			code |= (0x19 << 20);
			code |= 0xf9f;

			int t = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// Rt
			code |= (t << 12);
			
			int n = 0;	// Rn
			
			if (parts[0].length() == 5) {	// LDREX	p.432
				n = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rn
				code |= (0x0 << 21);
			}
			else if (parts[0].substring(5, 6).equals("b")) {	// LDREXB	p.434
				n = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rn
				code |= (0x2 << 21);
			}
			else if (parts[0].substring(5, 6).equals("d")) {	// LDREXD	p.436
				n = Integer.parseInt( parts[3].replaceAll("[^0-9]", "") );	// Rn
//				int t2 = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rt2
				code |= (0x1 << 21);
			}
			else if (parts[0].substring(5, 6).equals("h")) {	// LDREXH	p.438
				n = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rn
				code |= (0x3 << 21);
			}
			
			code |= (n << 16);	// Rn
		}

		
		
		// STREX / STREXB / SREXH / SREXD: Synchronization primitives 2/3 p.205
		if (parts[0].startsWith("strex")) {	// p.690 ... p.696

			code |= (0x18 << 20);
			code |= 0xf90;

			int d = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// Rd
			code |= (d << 12);

			int t = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rt
			code |= t;
			
			int n = 0;	// Rn
			
			if (parts[0].length() == 5) {	// STREX
				n = Integer.parseInt( parts[3].replaceAll("[^0-9]", "") );	// Rn
				code |= (0x0 << 21);
			}
			else if (parts[0].substring(5, 6).equals("b")) {	// STREXB
				n = Integer.parseInt( parts[3].replaceAll("[^0-9]", "") );	// Rn
				code |= (0x2 << 21);
			}
			else if (parts[0].substring(5, 6).equals("d")) {	// STREXD
				n = Integer.parseInt( parts[4].replaceAll("[^0-9]", "") );	// Rn
//				int t2 = Integer.parseInt( parts[3].replaceAll("[^0-9]", "") );	// Rt2
				code |= (0x1 << 21);
			}
			else if (parts[0].substring(5, 6).equals("h")) {	// STREXH
				n = Integer.parseInt( parts[3].replaceAll("[^0-9]", "") );	// Rn
				code |= (0x3 << 21);
			}
			
			code |= (n << 16);	// Rn
		}
		
		
		//  SWP /SWPB: Synchronization primitives 3/3 p.205
		if (parts[0].startsWith("swp")) {	// p.722

			code |= (0x1 << 24);
			code |= 0x090;
			
			if ((parts[0].length()>=4) ? parts[0].substring(3, 4).equals("b") : false) {
				code |= (0x1 << 22);
			}

			int t = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// Rt
			code |= (t << 12);
			
			int t2 = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rt2
			code |= (t2 << 0);
			
			int n = Integer.parseInt( parts[3].replaceAll("[^0-9]", "") );	// Rn
			code |= (n << 16);
		}
		
		
		//  DBG	p.377
		if (parts[0].startsWith("dbg")) {

			code |= 0x0320f0f0;

			int option = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// option
			code |= option;
		}
		
		
//		//  MSR
//		if ((parts[0].length()>=3) ? parts[0].substring(0, 3).equals("msr") : false) {
//
//			code |= (0x320f << 12);
//
//			String specReg = parts[1];	// specReg
////			bankedRegToSYSm(String bankedReg, int R) 
//			
//			int const32 = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// const
//			code |= constToImm(const32);
//
//		}
		
		
		//  Hints
		String[] hintsMnemonics = {
			"nop",	// p.510
			"sev",	// p.606
			"wfe",	// p.1104
			"wfi",	// p.1106
			"yie"	// p.1108	YIELD
		};	
		
		for(int i = 0; i < hintsMnemonics.length; i++) {
			if (parts[0].startsWith(hintsMnemonics[i])) {
				
				code |= (0x320f << 12);
				
				if (parts[0].startsWith("nop"))	code |= 0x0;
				if (parts[0].startsWith("sev"))	code |= 0x4;
				if (parts[0].startsWith("wfe"))	code |= 0x2;
				if (parts[0].startsWith("wfi"))	code |= 0x3;
				if (parts[0].startsWith("yie"))	code |= 0x1;
			}
		}
		
		
		//  Saturation addition and subtraction
		String[] SAddSubMnemonics = {
			"qadd",	// p.540
			"qdad",	// p.548	QDADD
			"qdsu",	// p.550	QDSUB
			"qsub"	// p.554
		};	
		
		for(int i = 0; i < SAddSubMnemonics.length; i++) {
			if (parts[0].startsWith(SAddSubMnemonics[i])) {

				code |= (0x1 << 24);
				code |= (0x5 << 4);

				int iOffset = 0;
				if (parts.length == 4) {	// Rd is not omitted
					int d = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// Rd
					code |= (d << 12);
					iOffset = 1;
				}
				
				int m = Integer.parseInt( parts[1+iOffset].replaceAll("[^0-9]", "") );	// Rm
				code |= (m << 0);
				
				int n = Integer.parseInt( parts[2+iOffset].replaceAll("[^0-9]", "") );	// Rn
				code |= (n << 16);
				
				if (SAddSubMnemonics[i].equals("qadd"))	code |= (0x0 << 21);
				if (SAddSubMnemonics[i].equals("qdad"))	code |= (0x2 << 21);
				if (SAddSubMnemonics[i].equals("qdsu"))	code |= (0x3 << 21);
				if (SAddSubMnemonics[i].equals("qsub"))	code |= (0x1 << 21);
			}
		}
		
		
		//  Branch
		String[] BranchMnemonics = {
				"b",
				"bl",	// p.348	BL / BLX
				"bx"	// p.352	BX / BXJ
		};	
		
		for(int i = 0; i < BranchMnemonics.length; i++) {
			if (		((parts[0].length()>=2) ? parts[0].substring(0, 2).equals(BranchMnemonics[i]) : false) 
					||	((parts[0].length() == 1) && parts[0].substring(0, 1).equals("b") )) {

				if (parts[1].substring(0,1).equals("r")) {	// (register)
					code |= (0x12fff << 8);
					int m = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// Rm
					code |= (m << 0);
					if ((parts[0]+"   ").substring(0, 3).equals("blx"))	code |= (0x3 << 4);
					if ((parts[0]+"   ").substring(0, 3).equals("bx"))	code |= (0x1 << 4);
					if ((parts[0]+"   ").substring(0, 3).equals("bxj"))	code |= (0x2 << 4);
				}
				else {	// label / B
					code |= (0x5 << 25);
					int imm24 = 0;
					if ((parts[0].length()>=3) ? parts[0].substring(2, 3).equals("x") : false) {	// BLX (immediate) Encoding A2 p.348
						int H = 0;
						imm24 = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// label
						if ( parts[1].contains("-") )	imm24 = imm24 * -1;
						H = (imm24 >> 1) & 0x1;
						code |= (H << 24);
						imm24 |= imm24 >> 2;
						code |= (0xf << 28);
					}
					else {	// BL Encoding A1 / B
						imm24 = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// label
						if ( parts[1].contains("-") )	imm24 = imm24 * -1;
						imm24 |= imm24 >> 2;
						if (parts[0].length() == 2) {	// BL
							code |= (0x1 << 24);
						}
					}
					code |= (imm24 & 0xffffff);
				}
			}
		}
		
		
		// BKPT	p.346
		if (parts[0].startsWith("bkpt")) {
			int imm16 = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// #imm16
			code |= ((imm16 & 0xfff0) << 4);	// imm12
			code |= (imm16 & 0xf);	// imm4
			code |= (0x12 << 20);
			code |= (0x7 << 4);
		}
		
		
		// HVC	p.1984
		if (parts[0].startsWith("hvc")) {
			int imm16 = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// #imm16
			code |= ((imm16 & 0xfff0) << 4);	// imm12
			code |= (imm16 & 0xf);	// imm4
			code |= (0x14 << 20);
			code |= (0x7 << 4);
		}
		
		
		// CLZ	p.362
		if (parts[0].startsWith("clz")) {
			int d = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// Rd
			code |= (d << 12);	// Rd
			int m = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rm
			code |= m;	// Rm
			code |= 0x016f0f10;
		}
		
		
		// ERET	p.1982
		if (parts[0].startsWith("eret")) {
			code |= 0x160006e;
		}
		
		
		// SMC	p.2002
		if (parts[0].startsWith("smc")) {
			int imm4 = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// #imm4
			code |= imm4;	// imm4
			code |= 0x01600070;
		}
		
		
		//  Load/store word and unsigned byte	p.208
		String[] LSMnemonics = {
				"ldr",	// p.408...422 LDR including LDRB, LDRBT and LDRT
				"str"	// p.674...682 STR including STRB, STRBT and STRT
		};	
		
		for(int i = 0; i < LSMnemonics.length; i++) {
			if ( (parts[0].startsWith(LSMnemonics[i])) && (!parts[0].contains("rex")) ) {
				int P = 0;
				int U = 1;	// add = true
				int W = 0;
				
				// (register) / (literal) / (immediate)
				// /////////////////////////////////////
				if (parts.length > 3 && parts[3].contains("r")) {	// (register) / xxxT A2	/ xxxBT A2	
					int n = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rn
					code |= (n << 16);
					
					if (parts[3].contains("-"))	U = 0;			
					
					int m = Integer.parseInt( parts[3].replaceAll("[^0-9]", "") );	// Rm
					code |= (m << 0);
					
					if (parts.length > 4) {	// shift is not omitted
						int type = encodeShiftType(parts[4]);
						code |= (type << 5);
						int imm5 = Integer.parseInt( parts[5].replaceAll("[^0-9]", "") );	// imm5
						code |= (imm5 << 7);	// imm5
					}
					
					boolean index = false;	// !(P = 1)
					boolean wback = false;	// !(P=0||W=1)
					if (mnemonic.endsWith("]")) {	// Offset
						index = true;
						wback = false;								
					}
					else if (mnemonic.contains("!")) {	// Pre-indexed
						index = true;
						wback = true;
					}
					else {	// Post-indexed
						index = false;
						wback = true;
					}
					if (index)	P=1;
					 if (P==0 && wback)	W = 1;	// LDRT
					if (P==0 && !wback)	W = 0;
					if (P==1 && wback)	W = 1;
					if (P==1 && !wback)	W = 0;
					
					code |= (0x1 << 25);
				}
				
				else if (parts[2].contains("r")) {	// (immediate)				
					int n = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rn
					code |= (n << 16);
					
					if (parts.length >= 4) {	// imm12 is not omitted
						if (parts[3].contains("-"))	U = 0;
						int imm12 = Integer.parseInt( parts[3].replaceAll("[^0-9]", "") );	// imm12
						code |= (imm12 << 0);
					}
					else {	// imm12 is omitted
						U = 1;
						int imm12 = 0;	// imm12
						code |= (imm12 << 0);
					}
					
					boolean index = false;	// !(P = 1)
					boolean wback = false;	// !(P=0||W=1)
					if (mnemonic.endsWith("]")) {	// Offset
						index = true;
						wback = false;								
					}
					else if (mnemonic.contains("!")) {	// Pre-indexed
						index = true;
						wback = true;
					}
					else {	// Post-indexed
						index = false;
						wback = true;
					}
					if (index)	P=1;
					// if (P==0 && wback)	W = 1;	// see LDRT
					if (P==0 && !wback)	W = 0;
					if (P==1 && wback)	W = 1;
					if (P==1 && !wback)	W = 0;
				}
				
				else {	// (literal)
					int n = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rn
					code |= (n << 16);
					
					if (parts.length >= 4) {	// imm12 is not omitted
						if (parts[3].contains("-"))	U = 0;
						int imm12 = Integer.parseInt( parts[3].replaceAll("[^0-9]", "") );	// imm12
						code |= (imm12 << 0);
					}
					else {	// imm12 is omitted
						U = 1;
						int imm12 = 0;	// imm12
						code |= (imm12 << 0);
					}
					
					
					code |= (0xf << 16);
				}
				
				
				// xxx / xxxB  / xxxBT
				// //////////
				if (parts[0].length() <= 4) {	// LDR / STR / LDRB / STRB
					code |= (0x0 << 22);
				}
				
				else {	// LDRBT / STRBT
					code |= (0x1 << 22);
				}
				

				// STRx / LDRx
				// ///////////
				if (parts[0].startsWith("ldr")) {	// LDR / LDRB
					code |= (0x1 << 20);
				}
				
				else {	// STR / STRB
					code |= (0x0 << 20);
				}
				
				
				// All
				// ///
				int t = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// Rt
				code |= (t << 12);
				
				code |= (0x4 << 24);
				
				code |= (P << 24);
				code |= (U << 23);
				code |= (W << 21);
			}
		}
		
		
		// Branch, branch with link, and block data transfer
		String[] LMnemonics = {
				"ldm",	// p.398...404 LDM including all LDMxx
				"stm"	// p.664...670 STM including all STMxx
		};	
		
		for(int i = 0; i < LMnemonics.length; i++) {
			if (parts[0].startsWith(LMnemonics[i])) {
				
				code |= (0x1 << 27);
				
				int n = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// Rn
				code |= (n << 16);
				
				if (parts[1].contains("!"))	code |= (0x1 << 21);	// W
				
				// LDM / STM
				if (parts[0].startsWith("ldm")) {	// LDMxx
					code |= (0x1 << 20);
				}
				
				// LDMDA / LDMFA / STMDA / STMFA
				if ( (parts[0].contains("da")) || (parts[0].contains("fa")) ) {
					code |= (0x0 << 23);
				}
				// LDMDB / LDMEA / STMDB / STMEA
				else if ( (parts[0].contains("db")) || (parts[0].contains("ea")) ) {
					code |= (0x2 << 23);
				}
				// LDMIB / LDMED / STMIB / STMEE
				else if ( (parts[0].contains("ib")) || (parts[0].contains("ed")) ) {
					code |= (0x3 << 23);
				}
				// LDM / STM
				else {
					code |= (0x1 << 23);
				}
				
				// Register List
				code |= encodeRegisterlist(2, parts);
			}
		}
		
		
		// Pop and Push multiple registers
		String[] PPMnemonics = {
				"pop",	// p.536 POP
				"push"	// p.538 PUSH
		};	
		
		for(int i = 0; i < PPMnemonics.length; i++) {
			if (parts[0].startsWith(PPMnemonics[i])) {
				
				code |= (0xd << 16);
				
				// Register List
				code |= encodeRegisterlist(1, parts);

				if (parts[0].startsWith("pop"))		code |= (0x8b << 20);
				if (parts[0].startsWith("push"))	code |= (0x92 << 20);
			}
		}
		
		
		// RFE	p.2000
		if (parts[0].startsWith("rfe")) {
			int[] PU = new int[2];	// PU = {P, U}
			PU = encodeAmode(parts[0]);
			code |= (PU[0] << 24);	// P
			code |= (PU[1] << 23);	// U

			int n = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// Rn
			code |= (n << 16);
			
			if (parts[1].contains("!"))	code |= (0x1 << 21);	// W
			
			code |= 0xf8100a00;
		}
		
		
		// SRS	p.2006
		if (parts[0].startsWith("srs")) {
			int[] PU = new int[2];	// PU = {P, U}
			PU = encodeAmode(parts[0]);
			code |= (PU[0] << 24);	// P
			code |= (PU[1] << 23);	// U
			
			if (parts[1].contains("!"))	code |= (0x1 << 21);	// W

			int mode = encodeMode( parts[2]);
			code |= (mode << 0);

			code |= 0xf84d0500;
		}
		
		//  SETEND	p.604
		if (parts[0].startsWith("setend")) {
			code |= 0xf1010000;
			boolean big = parts[1].equals("be");
			if (big) code |= 1 << 9;
		}
		
		// MRC  p.492
		if (parts[0].startsWith("mrc")) {
			code |= 0xe100010;
			int val = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// cp
			code |= val<<8;	// cp
			val = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// opc1
			code |= val<<21;	// opc1
			val = Integer.parseInt( parts[3].replaceAll("[^0-9]", "") );	// Rt
			code |= val<<12;	// Rt
			val = Integer.parseInt( parts[4].replaceAll("[^0-9]", "") );	// CRn
			code |= val<<16;	// CRn
			val = Integer.parseInt( parts[5].replaceAll("[^0-9]", "") );	// CRm
			code |= val;	// CRm
			val = Integer.parseInt( parts[6].replaceAll("[^0-9]", "") );	// opc2
			code |= val<<5;	// opc2
		}

		// MCR  p.476
		if (parts[0].startsWith("mcr")) {
			code |= 0xe000010;
			int val = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// cp
			code |= val<<8;	// cp
			val = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// opc1
			code |= val<<21;	// opc1
			val = Integer.parseInt( parts[3].replaceAll("[^0-9]", "") );	// Rt
			code |= val<<12;	// Rt
			val = Integer.parseInt( parts[4].replaceAll("[^0-9]", "") );	// CRn
			code |= val<<16;	// CRn
			val = Integer.parseInt( parts[5].replaceAll("[^0-9]", "") );	// CRm
			code |= val;	// CRm
			val = Integer.parseInt( parts[6].replaceAll("[^0-9]", "") );	// opc2
			code |= val<<5;	// opc2
		}

		//  extension system register
		String[] extSysRegs = {
			"fpsid",
			"fpscr",
			"mvfr1",
			"mvfr0",
			"fpexc"	
		};	
		// vmrs  p.2014
		if (parts[0].startsWith("vmrs")) {
			code |= 0xef00a10;
			int val = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// Rt
			code |= val<<12;	// Rt
			if (parts[2].equals(extSysRegs[0]));	// spezReg
			else if (parts[2].equals(extSysRegs[1])) code |= 1<<16;	// spezReg
			else if (parts[2].equals(extSysRegs[2])) code |= 6<<16;	// spezReg
			else if (parts[2].equals(extSysRegs[3])) code |= 7<<16;	// spezReg
			else if (parts[2].equals(extSysRegs[4])) code |= 8<<16;	// spezReg
		}
		// vmrs  p.2016
		if (parts[0].startsWith("vmsr")) {
			code |= 0xee00a10;
			if (parts[1].replaceAll(",", "").equals(extSysRegs[0]));	// spezReg
			else if (parts[1].replaceAll(",", "").equals(extSysRegs[1])) code |= 1<<16;	// spezReg
			else if (parts[1].replaceAll(",", "").equals(extSysRegs[2])) code |= 6<<16;	// spezReg
			else if (parts[1].replaceAll(",", "").equals(extSysRegs[3])) code |= 7<<16;	// spezReg
			else if (parts[1].replaceAll(",", "").equals(extSysRegs[4])) code |= 8<<16;	// spezReg
			int val = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rt
			code |= val<<12;	// Rt
		}

		// vmov  p.948
		if (parts[0].startsWith("vmov")) {
			code |= 0xc400b10;
			if (parts[1].contains("d")) { // move to floating point register
				int val = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// Dm
				code |= (val&0xf) | ((val>>4)<<5);
				val = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rt	
				code |= val<<12;	// Rt
				val = Integer.parseInt( parts[3].replaceAll("[^0-9]", "") );	// Rt2	
				code |= val<<16;	// Rt2
			} else {	 // move from floating point register
				int val = Integer.parseInt( parts[1].replaceAll("[^0-9]", "") );	// Rt
				code |= val<<12;	// Rt
				val = Integer.parseInt( parts[2].replaceAll("[^0-9]", "") );	// Rt2	
				code |= val<<16;	// Rt2
				val = Integer.parseInt( parts[3].replaceAll("[^0-9]", "") );	// Dm	
				code |= (val&0xf) | ((val>>4)<<5);
			}
		}

		return code;
	}


	
	
	
	private static String decodeShift(int type, int imm5) {	// A8.4.1 p.291
		switch(type) {
		case 0: return imm5==0?"0":(shiftType[type] + " #" + imm5);
		case 1: return shiftType[type] + " #" + (imm5==0?32:imm5);
		case 2: return shiftType[type] + " #" + (imm5==0?32:imm5);
		case 3: return imm5==0?(shiftType[type+1]):(shiftType[type] + " #" + imm5);
		default: break;
		}
		return "Error: 'decodeShift()'";
	}

	private static String decodeBankedReg(int SYSm, int R) {	// B9.2.3 p1975
		switch(R) {
		case 0:	return bankedRegR0[SYSm];
		case 1: return bankedRegR1[SYSm];
		}
		return "Error: 'decodeBankedReg()'";
	}
	
	private static int immToConst(int imm12) {	// A5.2.4 p.200
		int imm8 = imm12 & 0xff;
		int rot = (imm12 >>> 8) & 0xf;
		return Integer.rotateRight(imm8, (rot*2));
	}
	
	private static String registerlist(int imm16) {	// Encoding of lists of ARM core registers  p.214	->	p.295
		String registerStr = "";
		int i = 0;
		while ( i <= 12 ) {	// Rx
			registerStr = registerStr + (((imm16>>>i)&1)==1?("R"+i+", "):(""));
			i++;
		}
		if (((imm16>>>13)&1)==1) registerStr = registerStr + "SP, ";
		if (((imm16>>>14)&1)==1) registerStr = registerStr + "LR, ";
		if (((imm16>>>15)&1)==1) registerStr = registerStr + "PC, ";
		
		if (imm16!=0) registerStr = registerStr.substring(0,registerStr.length()-2);	// delete last ", "	
		return registerStr;
	}
	
//	private static String list(int dList) {	// p.1080	WARNING: not correctly implemented
//		return "???" + Integer.toHexString(dList) + "???";
//	}
	
	private static String decodeDSBOption(int option) {	// p.380
		switch(option) { 
		case 0x2: return "OSHST";
		case 0x3: return "OSHS";
		case 0x6: return "NSHST";
		case 0x7: return "NSHS";
		case 0xa: return "ISHST";
		case 0xb: return "ISHS";
		case 0xe: return "ST";
		case 0xf: return "SY";
		default: return "IMPLEMENTATION DEFINED";
		}
	}
	
	private static String decodeAmode(int P, int U) {	// p.2001
		if (P==0 && U==0) return "da";	// Decrement After. The consecutive memory addresses end at the address in the base register. Encoded as P = 0, U = 0.
		if (P==1 && U==0) return "db";	// Decrement Before. The consecutive memory addresses end one word below the address in the base register. Encoded as P = 1, U = 0.
		if (P==0 && U==1) return "ia";	// Increment After. The consecutive memory addresses start at the address in the base register. This is the default. Encoded as P = 0, U = 1.
		if (P==1 && U==1) return "ib";	// Increment Before. ARM instructions only. The consecutive memory addresses start one word above the address in the base register. Encoded as P = 1, U = 1.
		return "Error: decodeAmode";
	}
	
	private static String decodeMode(int option5) {	// p.2007 -> p.1139
		if (option5 == 0x10) return "usr";
		if (option5 == 0x11) return "fiq";
		if (option5 == 0x12) return "irq";
		if (option5 == 0x13) return "svc";
		if (option5 == 0x16) return "mon";
		if (option5 == 0x17) return "abt";
		if (option5 == 0x1a) return "hyp";
		if (option5 == 0x1b) return "und";
		if (option5 == 0x1f) return "sys";
		return "Error: decodeMode";
	}
	
	
	
	public String getMnemonic(Integer instr) {

		int op28_4 = (instr >>> 28) & 0xf;
		int op25_3 = (instr >>> 25) & 0x7;
		int op24_1 = (instr >>> 24) & 0x1;
		int op23_2 = (instr >>> 23) & 0x3;
		int op23_1 = (instr >>> 23) & 0x1;
		int op22_1 = (instr >>> 22) & 0x1;
		int op21_3 = (instr >>> 21) & 0x7;
		int op21_2 = (instr >>> 21) & 0x3;
		int op21_1 = (instr >>> 21) & 0x1;
		int op20_8 = (instr >>> 20) & 0xff;
		int op20_7 = (instr >>> 20) & 0x7f;
		int op20_6 = (instr >>> 20) & 0x3f;
		int op20_5 = (instr >>> 20) & 0x1f;
		int op20_4 = (instr >>> 20) & 0xf;
		int op20_3 = (instr >>> 20) & 0x7;
		int op20_2 = (instr >>> 20) & 0x3;
		int op20_1 = (instr >>> 20) & 0x1;
//		int op18_2 = (instr >>> 18) & 0x3;
		int op18_1 = (instr >>> 18) & 0x1;
		int op16_5 = (instr >>> 16) & 0x1f;
		int op16_4 = (instr >>> 16) & 0xf;
//		int op16_3 = (instr >>> 16) & 0x7;
//		int op16_1 = (instr >>> 16) & 0x1;
		int op15_1 = (instr >>> 15) & 0x1;
		int op12_4 = (instr >>> 12) & 0xf;
		int op10_2 = (instr >>> 10) & 0x3;
		int op9_1 =  (instr >>> 9) & 0x1;
		int op8_4 =  (instr >>> 8) & 0xf;
		int op8_1 =  (instr >>> 8) & 0x1;
		int op7_5 =  (instr >>> 7) & 0x1f;
		int op7_1 =  (instr >>> 7) & 0x1;
//		int op6_2 = (instr >>> 6) & 0x3	;
		int op6_1 = (instr >>> 6) & 0x1;
		int op5_3 = (instr >>> 5) & 0x7;
		int op5_2 = (instr >>> 5) & 0x3;
		int op5_1 = (instr >>> 5) & 0x1;
		int op4_1 = (instr >>> 4) & 0x1;
		int op4_4 = (instr >>> 4) & 0xf;
		int op0_25 = instr & 0x1ffffff;
		int op0_24 = instr & 0xffffff;
		int op0_16 = instr & 0xffff;
		int op0_15 = instr & 0x7fff;
		int op0_12 = instr & 0xfff;
		int op0_8 = instr & 0xff;
		int op0_5 = instr & 0x1f;
		int op0_4 = instr & 0xf;
		
		int cond = op28_4;
		int op = op25_3;
		int op1 = op20_5;
		int n = op16_4;
		int d = op12_4;
		int op2 = op4_4;
	
		if (cond != 0xf)  {	// conditional instructions 
		switch (op) {	// p.194
			case 0: {	// Data-processing and miscellaneous instructions 1/2 p.196 (Bit 25)
				if ((op1 & 0x19) != 0x10) {	// !=(10XX0)
					if ((op2 & 0x1) == 0) {	// Data-processing (register) p.197
						int m = op0_4;
						int imm5 = op7_5;
						int type = op5_2;
						
						switch (op1) {	// p.197
						case 0:  return "and" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 1:  return "and" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 2:  return "eor" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m +", " +  decodeShift(type, imm5);
						case 3:  return "eor" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 4:  return "sub" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 5:  return "sub" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 6:  return "rsb" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 7:  return "rsb" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m +", " +  decodeShift(type, imm5);
						case 8:  return "add" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 9:	 return "add" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 10: return "adc" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 11: return "adc" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 12: return "sbc" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 13: return "sbc" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 14: return "rsc" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 15: return "rsc" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + decodeShift(type, imm5);

						// cases 16, 18, 20 and 22 are defined in an if statement below

						case 17: return "tst" + (cond!=condAlways?condString[cond]:"") + " R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 19: return "teq" + (cond!=condAlways?condString[cond]:"") + " R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 21: return "cmp" + (cond!=condAlways?condString[cond]:"") + " R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 23: return "cmn" + (cond!=condAlways?condString[cond]:"") + " R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 24: return "orr" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 25: return "orr" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 26:
							switch((instr >>> 5) & 0x3) {
							case 0:
								if (imm5 == 0) { 
										return "mov" + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + m;
									}
									else {
										return "lsl" + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + m +", #" + imm5;								
									}
							case 1: return "lsr" + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + m +", #" + imm5;
							case 2: return "asr" + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + m +", #" + imm5;
							case 3:
								if (imm5 == 0) { 
										return "rrx" + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + m;
									}
									else {
										return "ror" + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + m +", #" + imm5;								
									}
							default: break;
							}
						case 27: switch((instr >>> 5) & 0x3) {
							case 0:
								if (imm5 == 0) { 
										return "mov" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + m;
									}
									else {
										return "lsl" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + m +", #" + imm5;								
									}
							case 1: return "lsr" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + m +", #" + imm5;
							case 2: return "asr" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + m +", #" + imm5;
							case 3: 
								if (imm5 == 0) { 
										return "rrx" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + m;
									}
									else {
										return "ror" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + m +", #" + imm5;								
									}
							default: break;
							}
						case 28: return "bic" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 29: return "bic" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + decodeShift(type, imm5);
						case 30: return "mvn" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m + ", " + decodeShift(type, imm5);
						case 31: return "mvn" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m + ", " + decodeShift(type, imm5);
								
						default: break;
						}
					}
					
					if ((op2 & 0x9) == 0x1) {	// !=(0XX1) Data-processing (register-shifted register) p.198
						int s = (instr >>> 8) & 0xf;
						int m = instr & 0xf;
						int type = (instr >>> 5) & 0x3;
						switch (op1) {	// p.198
						case 0:  return "and" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 1:  return "and" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 2:  return "eor" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 3:  return "eor" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 4:  return "sub" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 5:  return "sub" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 6:  return "rsb" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 7:  return "rsb" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 8:  return "add" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 9:	 return "add" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 10: return "adc" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 11: return "adc" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 12: return "sbc" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 13: return "sbc" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 14: return "rsc" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 15: return "rsc" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;

						// cases 16, 18, 20 and 22 are defined in an if statement below:		if ((op1 & 0x19) == 0x10) {	// =(10XX0)

						case 17: return "tst" + (cond!=condAlways?condString[cond]:"") + " R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 19: return "teq" + (cond!=condAlways?condString[cond]:"") + " R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 21: return "cmp" + (cond!=condAlways?condString[cond]:"") + " R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 23: return "cmn" + (cond!=condAlways?condString[cond]:"") + " R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 24: return "orr" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 25: return "orr" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 26: 
							switch((instr >>> 5) & 0x3) {
							case 0: return "lsl" + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + op0_4 + ", R" + op8_4;	
							case 1: return "lsr" + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + op0_4 + ", R" + op8_4;
							case 2: return "asr" + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + op0_4 + ", R" + op8_4;
							case 3: return "ror" + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + op0_4 + ", R" + op8_4;
							default: break;
							}
						case 27: 
							switch((instr >>> 5) & 0x3) {
							case 0: return "lsl" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + op0_4 + ", R" + op8_4;	
							case 1: return "lsr" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + op0_4 + ", R" + op8_4;
							case 2: return "asr" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + op0_4 + ", R" + op8_4;
							case 3: return "ror" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d  + ", R" + op0_4 + ", R" + op8_4;
							default: break;
							}
						case 28: return "bic" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 29: return "bic" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m  + ", " + shiftType[type] + " R" + s;
						case 30: return "mvn" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m + ", " + shiftType[type] + " R" + s;
						case 31: return "mvn" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m + ", " + shiftType[type] + " R" + s;								
						default: break;
						}						
					}	
					
				}	// end if op1!=(10XX0)
				
				
				if ((op1 & 0x19) == 0x10) {	// =(10XX0)
					if ((op2 & 0x8) == 0x0) {	// =(0XXX) Miscellaneous instructions p.207
						int B = (instr >>> 9) & 0x1;
						int m = instr & 0xf;
						int n2 = m;
						switch ((op2 & 0x7)) {	// p.207
						case 0:
							switch (B) {
							case 1:
								int SYSm = ((((instr >>> 8) & 0x1))<<4) + op16_4;
								int R = ((instr >>> 22) & 0x1);
								if ((op21_2 & 0x1) == 0x0) { // Move from Banked or Special register p.1992
									return "mrs" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", " + decodeBankedReg(SYSm, R);
								}
								if ((op21_2 & 0x1) == 0x1) { // Move to Banked or Special register p.1994
									return "msr" + (cond!=condAlways?condString[cond]:"") + " " + decodeBankedReg(SYSm, R) + ", R" + n2;
								}
							case 0:
								switch (op21_2) {
								case 0:	case 2: // Move from Special register p.496 / p.1990
									return "mrs" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", " + spec_reg;
								case 1:
									if ((op16_4 & 0x3) == 0x0) {	// =(XX00) Move to Special register, Application level p.500
										return "msr" + (cond!=condAlways?condString[cond]:"") + " " + spec_reg + ", R" + n2;
									}
									if (((op16_4 & 0x3) == 0x1) || ((op16_4 & 0x2) == 0x2)) {	// =(XX01)||(XX1X) Move to Special register, System level p.1998
										return "msr" + (cond!=condAlways?condString[cond]:"") + " " + spec_reg + ", R" + n2;
									}
								case 3: // Move to Special register, System level p.1998
									return "msr" + (cond!=condAlways?condString[cond]:"") + " " + spec_reg + ", R" + n2;
								default: break;
								}
							default: break;
							}
						case 1:
							if (op21_2 == 1) {	// Branch and Exchange p.352
								return "bx" + (cond!=condAlways?condString[cond]:"") + " R" + m;
							}
							if (op21_2 == 3) {	// Count Leading Zeros p.362
								return "clz" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m;
							}
						case 2: return "bxj" + (cond!=condAlways?condString[cond]:"") + " R" + m;
						case 3:
							if (cond == 0xf) return "blx" + " R" + m;
							else return "bl" + (cond!=condAlways?condString[cond]:"") + " R" + m;
						case 4: return "undefined";
						case 5:	// Saturating addition and subtraction p.202
							switch(op21_2) {
							case 0: return "qadd" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m + ", R" + n;
							case 1: return "qsub" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m + ", R" + n;
							case 2: return "qdadd" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m + ", R" + n;
							case 3: return "qdsub" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m + ", R" + n;
							default: break;
							}
						case 6:
							if (op21_2 == 3) {	// Exception Return p.1982
								return "eret" + (cond!=condAlways?condString[cond]:"");
							}
						case 7:
							int imm16 = ((instr >>> 4) & 0xfff0) + m;
							switch(op21_2) {
							case 1: return "bkpt #" + imm16;	// Breakpoint p.346
							case 2: return "hvc #" + imm16;	// Hypervisor Call p.1984
							case 3: return "smc" + (cond!=condAlways?condString[cond]:"") + " #" + m;	//Secure Monitor Call p.2002
							default: break;
							}							
						default: break;
						}
					}	// End of p.207 op2=(0XXX)
					
					if ((op2 & 0x9) == 0x8) {	// =(1XX0) Halfword multiply and multiply accumulate p.203
						int dHi = op16_4;
						int a = op12_4;
						int dLo = op12_4;
						int m = op8_4;
						
						switch (op21_2) {
						case 0:	//Signed 16-bit x 32-bit multiply, 32-bit result p.620
							switch((instr>>>5) & 0x3) {
							case 0: return "smlabb" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + m + ", R" + a;
							case 1: return "smlatb" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + m + ", R" + a;
							case 2: return "smlabt" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + m + ", R" + a;
							case 3: return "smlatt" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + m + ", R" + a;
							default: break;
							}
						case 1:	
							switch(op5_1) {
							case 0:	//Signed 16-bit x 32-bit multiply, 32-bit accumulate p.630
								if (((instr >>> 6) & 0x1) == 0x0) return "smlawb" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + m + ", R" + a;
								if (((instr >>> 6) & 0x1) == 0x1) return "smlawt" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + m + ", R" + a;								
							case 1:	//Signed 16-bit x 32-bit multiply, 32-bit result p.648
								if (((instr >>> 6) & 0x1) == 0x0) return "smulwb" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + m;
								if (((instr >>> 6) & 0x1) == 0x1) return "smulwt" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + m;
							default: break;
							}
						case 2: // Signed 16-bit multiply, 64-bit accumulate p.626
							switch((instr>>>5) & 0x3) {
							case 0: return "smlalbb" + (cond!=condAlways?condString[cond]:"") + " R" + dLo + ", R" + dHi + ", R" + op0_4 + ", R" + m;
							case 1: return "smlaltb" + (cond!=condAlways?condString[cond]:"") + " R" + dLo + ", R" + dHi + ", R" + op0_4 + ", R" + m;
							case 2: return "smlalbt" + (cond!=condAlways?condString[cond]:"") + " R" + dLo + ", R" + dHi + ", R" + op0_4 + ", R" + m;
							case 3: return "smlaltt" + (cond!=condAlways?condString[cond]:"") + " R" + dLo + ", R" + dHi + ", R" + op0_4 + ", R" + m;
							default: break;
							}							
						case 3: // Signed 16-bit multiply, 32-bit result p.644
							switch((instr>>>5) & 0x3) {
							case 0: return "smulbb" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + m;
							case 1: return "smultb" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + m;
							case 2: return "smulbt" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + m;
							case 3: return "smultt" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + n + ", R" + m;
							default: break;
							}							
						default: break;
						}
					}	// End of: Halfword multiply and multiply accumulate p.203    op2=(1XX0)
					
				}	// End of: op1=(10XX0)
				
				
				if ((op1 & 0x10) == 0x00) {	// op1=(0XXXX)
					if (op2 == 0x9) {	// Multiply and multiply accumulate p.202
						int dHi = op16_4;
						int a = op12_4;
						int dLo = op12_4;
						int m = op8_4;
						
						switch(op20_4) {	// p.202
						case 0:  return "mul" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + m;
						case 1:  return "mul" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + m;
						case 2:  return "mla" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + m + ", R" + a;
						case 3:  return "mla" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + m + ", R" + a;
						case 4:  return "umaal" + (cond!=condAlways?condString[cond]:"") + " R" + dLo + ", R" + dHi + ", R" + op0_4 + ", R" + m;
						case 5:  return "undefined";
						case 6:  return "mls" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + m + ", R" + a;
						case 7:  return "undefined";
						case 8:  return "umull" + (cond!=condAlways?condString[cond]:"") + " R" + dLo + ", R" + dHi + ", R" + op0_4 + ", R" + m;
						case 9:  return "umull" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + dLo + ", R" + dHi + ", R" + op0_4 + ", R" + m;
						case 10: return "umlal" + (cond!=condAlways?condString[cond]:"") + " R" + dLo + ", R" + dHi + ", R" + op0_4 + ", R" + m;
						case 11: return "umlal" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + dLo + ", R" + dHi + ", R" + op0_4 + ", R" + m;
						case 12: return "smull" + (cond!=condAlways?condString[cond]:"") + " R" + dLo + ", R" + dHi + ", R" + op0_4 + ", R" + m;
						case 13: return "smull" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + dLo + ", R" + dHi + ", R" + op0_4 + ", R" + m;
						case 14: return "smlal" + (cond!=condAlways?condString[cond]:"") + " R" + dLo + ", R" + dHi + ", R" + op0_4 + ", R" + m;
						case 15: return "smlal" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + dLo + ", R" + dHi + ", R" + op0_4 + ", R" + m;							
						default: break;
						}						
					}	// End of: Multiply and multiply accumulate p.202
					
				}	// End of: op1=(0XXXX)
				
				
				if ((op1 & 0x10) == 0x10) {	// op1=(1XXXX)
					if (op2 == 0x9) {	// Synchronization primitives p.205
						int t = op12_4;
						int t2 = op0_4;
						if ((op20_4 & 0xb) == 0x0) {	// Swap Word, Swap Byte p.722
							return "swp" + (op22_1==1?"b":"") + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + t2 + ", [R" + n + "]";
						}
						switch(op20_4) {	// p.205
						case 4:  return "swpb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + t2 + ", [R" + n + "]";
						case 8:	 return "strex" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + op0_4 + ", [R" + n + "]";
						case 9:  return "ldrex" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "]";
						case 10: return "strexd" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + op0_4 + ", R" + (op0_4+1) + ", [R" + n + "]";
						case 11: return "ldrexd" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + (t+1) + ", [R" + n + "]";						
						case 12: return "strexb" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + op0_4 + ", [R" + n + "]";
						case 13: return "ldrexb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "]";
						case 14: return "strexh" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + op0_4 + ", [R" + n + "]";
						case 15: return "ldrexh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "]";							
						default: break;
						}
						
					}	// End of: Synchronization primitives p.205
					
				}	// End of: op1=(1XXXX)
				

				Boolean extraLoadStoreCond0 = (((op1 & 0x12) != 0x02) && (op2 == 0xb));
				Boolean extraLoadStoreCond1 = (((op1 & 0x12) != 0x02) && ((op2 & 0xd) == 0xd));
				Boolean extraLoadStoreCond2 = (((op1 & 0x13) == 0x02) && ((op2 & 0xd) == 0xd));
				if (extraLoadStoreCond0 | extraLoadStoreCond1 | extraLoadStoreCond2) {	// Extra load/store instructions p.203/4
					int t = op12_4;
					int m = op0_4;
					int W = ((instr >>> 21) & 0x1);
					int U = ((instr >>> 23) & 0x1);
					int P = ((instr >>> 24) & 0x1);
					int imm8 = (op8_4 << 4) + op0_4;
					Boolean wback = (P == 0x0 || W == 0x1);
					Boolean index = P == 0x1;
					Boolean add = U == 0x1;
					switch((instr>>>5) & 0x3) {
					case 01:	// op2
						if ((op1 & 0x5) == 0x0) {	// Store Halfword p.702
							if (index && wback) {	// Pre-indexed
								return "strh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", " + (add?"+":"-") + "R" + m + "]!";
							}
							if (!index && wback) {	// Post-indexed
								return "strh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], " + (add?"+":"-") + "R" + m;
							}
						}
						if ((op1 & 0x5) == 0x1) {	// Load Halfword p.446
							if (index && !wback) {	// Offset
								return "ldrh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", " + (add?"+":"-") + "R" + m + "]";
							}
							if (index && wback) {	// Pre-index
								return "ldrh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", " + (add?"+":"-") + "R" + m + "]!";
							}
							if (!index && wback) {	// Post-index
								return "ldrh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", " + (add?"+":"-") + "R" + m + "]";
							}
						}
						if ((op1 & 0x5) == 0x4) {	// Store Halfword p.700
							if (index && !wback) {	// Offset
								return "strh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm8 + "]";
							}
							if (index && wback) {	// Pre-index
								return "strh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm8 + "]!";
							}
							if (!index && wback) {	// Post-index
								return "strh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], #" + (add?"+":"-") + imm8;
							}
						}
						if (((op1 & 0x5) == 0x5) && (n != 0xf)) {	// Load Halfword p.442
							if (index && !wback) {	// Offset
								return "ldrh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm8 + "]";
							}
							if (index && wback) {	// Pre-index
								return "ldrh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm8 + "]!";
							}
							if (!index && wback) {	// Post-index
								return "ldrh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], #" + (add?"+":"-") + imm8;
							}							
						}
						if (((op1 & 0x5) == 0x5) && (n == 0xf)) {	// Load Halfword p.444
							return "ldrh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", " + (add?"+":"-") + imm8;
							//return "ldrh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [PC, #" + (add?"+":"-") + imm8 +"]";	// Alternative form
						}
					case 2:	// op2
						if ((op1 & 0x5) == 0x0) {	// Load Dual p.430
							if (index && !wback) {	// Offset
								return "ldrd" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + (t+1) + ", [R" + n + ", " + (add?"+":"-") + "R" + m + "]";
							}
							if (index && wback) {	// Pre-index
								return "ldrd" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + (t+1) + ", [R" + n + ", " + (add?"+":"-") + "R" + m + "]!";
							}
							if (!index && wback) {	// Post-index
								return "ldrd" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + (t+1) + ", [R" + n + "], " + (add?"+":"-") + "R" + m;
							}							
						}
						if ((op1 & 0x5) == 0x1){	// Load Signed Byte p.454
							if (index && !wback) {	// Offset
								return "ldrsb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", " + (add?"+":"-") + "R" + m + "]";
							}
							if (index && wback) {	// Pre-index
								return "ldrsb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", " + (add?"+":"-") + "R" + m + "]!";
							}
							if (!index && wback) {	// Post-index
								return "ldrsb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", " + (add?"+":"-") + "R" + m + "]";
							}							
						}
						if (((op1 & 0x5) == 0x4) && (n != 0xf)){	// Load Dual p.426 
							if (index && !wback) {	// Offset
								return "ldrd" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + (t+1) + ", [R" + n + ", #" + (add?"+":"-") + "R" + m + "]";
							}
							if (index && wback) {	// Pre-index
								return "ldrd" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + (t+1) + ", [R" + n + ", #" + (add?"+":"-") + "R" + m + "]!";
							}
							if (!index && wback) {	// Post-index
								return "ldrd" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + (t+1) + ", [R" + n + "], #" + (add?"+":"-") + "R" + m;
							}
						}
						if (((op1 & 0x5) == 0x4) && (n == 0xf)){	// Load Dual p.428 
							return "ldrd" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + (t+1) + ", " + (add?"+":"-") + imm8;
							//return "ldrd" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + (t+1) + ", [PC, #" + (add?"+":"-") + imm8 +"]";	// Alternative form
						}
						if (((op1 & 0x5) == 0x5) && (n != 0xf)){	// Load Signed Byte p.450 
							if (index && !wback) {	// Offset
								return "ldrsb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm8 + "]";
							}
							if (index && wback) {	// Pre-index
								return "ldrsb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm8 + "]!";
							}
							if (!index && wback) {	// Post-index
								return "ldrsb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], #" + (add?"+":"-") + imm8;
							}
						}
						if (((op1 & 0x5) == 0x5) && (n == 0xf)){	// Load Signed Byte p.452 
							return "ldrsb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", " + (add?"+":"-") + imm8;
							//return "ldrsb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [PC, #" + (add?"+":"-") + imm8 +"]";	// Alternative form
						}
					case 3:	// op2
						if ((op1 & 0x5) == 0x0) {	// Stor Dual p.688
							if (index && !wback) {	// Offset
								return "strd" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + (t+1) + ", [R" + n + ", " + (add?"+":"-") + "R" + m + "]";
							}
							if (index && wback) {	// Pre-index
								return "strd" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + (t+1) + ", [R" + n + ", " + (add?"+":"-") + "R" + m + "]!";
							}
							if (!index && wback) {	// Post-index
								return "strd" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + (t+1) + ", [R" + n + "], " + (add?"+":"-") + "R" + m;
							}
						}
						if ((op1 & 0x5) == 0x1){	// Load Signed Halfword p.462
							if (index && !wback) {	// Offset
								return "ldrsh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", " + (add?"+":"-") + "R" + m + "]";
							}
							if (index && wback) {	// Pre-index
								return "ldrsh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", " + (add?"+":"-") + "R" + m + "]!";
							}
							if (!index && wback) {	// Post-index
								return "ldrsh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], " + (add?"+":"-") + "R" + m;
							}
						}
						if ((op1 & 0x5) == 0x4){	// Store Dual p.686
							if (index && !wback) {	// Offset
								return "strd" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + (t+1) + ", [R" + n + ", #" + (add?"+":"-") + "R" + m + "]";
							}
							if (index && wback) {	// Pre-index
								return "strd" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + (t+1) + ", [R" + n + ", #" + (add?"+":"-") + "R" + m + "]!";
							}
							if (!index && wback) {	// Post-index
								return "strd" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + (t+1) + ", [R" + n + "], #" + (add?"+":"-") + "R" + m;
							}
						}
						if (((op1 & 0x5) == 0x5) && (n != 0xf)){	// Load Signed Halfword p.458
							if (index && !wback) {	// Offset
								return "ldrsh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm8 + "]";
							}
							if (index && wback) {	// Pre-index
								return "ldrsh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm8 + "]!";
							}
							if (!index && wback) {	// Post-index
								return "ldrsh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], #" + (add?"+":"-") + imm8;
							}
						}
						if (((op1 & 0x5) == 0x5) && (n == 0xf)){	// Load Signed Halfword p.460
							return "ldrsh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", " + (add?"+":"-") + imm8;
//							return "ldrsh" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [PC, #" + (add?"+":"-") + imm8 +"]";	// Alternative form
						}
					default: break;
					}
				}	// End of: Extra load/store instructions p.203/4

				
				extraLoadStoreCond0 = (((op1 & 0x12) == 0x2) && (op2 == 0xb));
				extraLoadStoreCond1 = (((op1 & 0x13) == 0x03) && ((op2 & 0xd) == 0xd));
				if (extraLoadStoreCond0 | extraLoadStoreCond1) {	// Extra load/store instructions, unprivileged p.204
					int t = op12_4;
					int m = op0_4;
					int U = ((instr >>> 23) & 0x1);
					int imm8 = (op8_4 << 4) + op0_4;
					Boolean add = U == 0x1;
					switch((instr>>>5) & 0x3) {
					case 01:
						if (((instr>>>20) & 0x1) == 0x0){	// Store Halfword unprivileged p.704
							if (((instr>>>22) & 0x1) == 0x1) {	// Encoding A1; register_form = FALSE;
								return "strht" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm8 + "]";
							}
							else {	// Encoding A2; register_form = TRUE;
								return "strht" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], " + (add?"+":"-") + "R" + m;								
							}
						}
						if (((instr>>>20) & 0x1) == 0x1){	// Load Halfword unprivileged p.448
							if (((instr>>>22) & 0x1) == 0x1) {	// Encoding A1; register_form = FALSE;
								return "ldrht" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm8 + "]";
							}
							else {	// Encoding A2; register_form = TRUE;
								return "ldrht" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], " + (add?"+":"-") + "R" + m;								
							}
						}
					case 02:	// Load Signed Byte Unprivileged p.456
						if (((instr>>>22) & 0x1) == 0x1) {	// Encoding A1; register_form = FALSE;
							return "ldrsbt" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm8 + "]";
						}
						else {	// Encoding A2; register_form = TRUE;
							return "ldrsbt" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], " + (add?"+":"-") + "R" + m;								
						}
					case 03:	// Load Signed Halfword unprivileged p.464
						if (((instr>>>22) & 0x1) == 0x1) {	// Encoding A1; register_form = FALSE;
							return "ldrsht" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm8 + "]";
						}
						else {	// Encoding A2; register_form = TRUE;
							return "ldrsht" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], " + (add?"+":"-") + "R" + m;								
						}
					default: break;
					}
				}	// End of: Extra load/store instructions, unprivileged p.204
				
				break; 
			}	// End of (op=0): Data-processing and miscellaneous instructions 1/2 p.196 (Bit 25)
			
			
			
			case 1: {	// Data-processing and miscellaneous instructions 2/2 p.196 (Bit 25)
				if ((op1 & 0x19) != 0x10) { // Data-processing (immediate) p.199
					int imm12 = instr & 0xfff;	
					int const32 = immToConst(imm12);
					switch (op1) {
					case 0: return "and" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
					case 1: return "and" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
					case 2: return "eor" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
					case 3: return "eor" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
					case 4:
						if (d != 0xf) {	// Subtract p.710
							return "sub" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
						}
						else {	//From PC-relative address p.322; ENCODING A2
							Boolean add = false;
							return "adr" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", " + (add?"+":"-") + const32;							
						}
					case 5:
						if (d != 0xf) {	// Subtract p.710
							return "sub" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
						}
						else {	//From PC-relative address p.322; ENCODING A2
							Boolean add = false;
							return "adr" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", " + (add?"+":"-") + const32;							
						}
					case 6: return "rsb" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
					case 7: return "rsb" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
					case 8:
						if (d != 0xf) {	// Add p.308
							return "add" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
						}
						else {	//From PC-relative address p.322; ENCODING A1
							Boolean add = true;
							return "adr" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", " + (add?"+":"-") + const32;							
						}
					case 9:
						if (d != 0xf) {	// Add p.308
							return "add" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
						}
						else {	//From PC-relative address p.322; ENCODING A1
							Boolean add = true;
							return "adr" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", " + (add?"+":"-") + const32;							
						}
					case 10: return "adc" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
					case 11: return "adc" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
					case 12: return "sbc" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
					case 13: return "sbc" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
					case 14: return "rsc" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
					case 15: return "rsc" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
					case 16: case 18: case 20: case 22: return "ERROR: Wrong turn";
					case 17: return "tst" + (cond!=condAlways?condString[cond]:"") + " R" + n + ", #" + const32;
					case 19: return "teq" + (cond!=condAlways?condString[cond]:"") + " R" + n + ", #" + const32;
					case 21: return "cmp" + (cond!=condAlways?condString[cond]:"") + " R" + n + ", #" + const32;
					case 23: return "cmn" + (cond!=condAlways?condString[cond]:"") + " R" + n + ", #" + const32;
					case 24: return "orr" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #0x" + Integer.toHexString(const32);
					case 25: return "orr" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
					case 26: return "mov" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", #" + const32;
					case 27: return "mov" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", #" + const32;
					case 28: return "bic" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
					case 29: return "bic" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + const32;
					case 30: return "mvn" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", #" + const32;
					case 31: return "mvn" + updateAPSR + (cond!=condAlways?condString[cond]:"") + " R" + d + ", #" + const32;					
					default: break;
					}					
				}	// End of ((op1 & 0x19) != 0x10): Data-processing (immediate) p.199
				
				if (op1 == 0x10) {	// 16-bit immediate load, MOV (immediate) p.484; ENCODING = A2
					int imm4 = (instr >>> 16) & 0xf;
					int imm12 = instr & 0xfff;
					int imm16 = (imm4<<12) + imm12;
					return "movw" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", #" + imm16;
				}	// End of (op1 == 0x10): 16-bit immediate load, MOV (immediate) p.484
				
				if (op1 == 0x14) {	// High halfword 16-bit immediate load, MOVTp.491
					int imm4 = (instr >>> 16) & 0xf;
					int imm12 = instr & 0xfff;
					int imm16 = (imm4<<12) + imm12;
					return "movt" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", #" + imm16;
				}	// End of (op1 == 0x14): High halfword 16-bit immediate load, MOVTp.491
				
				if ((op1 == 0x12) || (op1 == 0x16)) {	// MSR (immediate), and hints p.206
					
					if (op22_1 == 0) {
						if (op16_4 == 0) {
							switch (op0_8) {
							case 0: return "nop" + (cond!=condAlways?condString[cond]:"");	// p.510
							case 1: return "yield" + (cond!=condAlways?condString[cond]:"");	// p.1108
							case 2: return "wfe" + (cond!=condAlways?condString[cond]:"");	// p.1104
							case 3: return "wfi" + (cond!=condAlways?condString[cond]:"");	// p.1106
							case 4: return "sev" + (cond!=condAlways?condString[cond]:"");	// p.606
							default:
								if ((op0_8 & 0xf0) == 0xf0) {	// Debug hint p. 377
									return "dbg" + (cond!=condAlways?condString[cond]:"") + " #" + op0_4;
								}	// End of ((op0_8 & 0xf0) == 0xf0): Debug hint p. 377 							
							}	// End of switch (op0_8)
							
						}	// End of (op16_4 == 0)
						
						if ( (op16_4==4) || ((op16_4 & 0xb)==0x8) ) {	// Move to Special register, Application level p.498
							return "msr" + (cond!=condAlways?condString[cond]:"") + " " + spec_reg + ", #" + immToConst(op0_12);
						}	// End of ( (op16_4==4) || ((op16_4 & 0xb)==0x8) ): Move to Special register, Application level p.498

						
						if ( ((op16_4 & 0x3)==0x1) || ((op16_4 & 0x2)==0x2) ) {	// Move to Special register, System level p.1996
							return "msr" + (cond!=condAlways?condString[cond]:"") + " " + spec_reg + ", #" + immToConst(op0_12);
							
						}	// End of ( ((op16_4 & 0x3)==0x1) || ((op16_4 & 0x2)==0x2) ): Move to Special register, System level p.1996
						
					}	// End of (op22_1 == 0)
					else if (op22_1 == 1) {	// Move to special register, System level p.1996
						return "msr" + (cond!=condAlways?condString[cond]:"") + " " + spec_reg + ", #" + immToConst(op0_12);
					}	// End of (op22_1 == 1): Move to special register, System level p.1996					
					
				}	// End of ((op1 == 0x12) || (op1 == 0x16)):MSR (immediate), and hints p.206
			}	// End of (op=1): Data-processing and miscellaneous instructions 2/2 p.196 (Bit 25)				
				
			
			
			case 2: case 3: {
				if ((op==0x2) || ((op==0x3)&&(op4_1 == 0x0))) {	// Load/store word and unsigned byte p.208
					int A = ((instr >>> 25) & 0x1);
					int B = ((instr >>> 4) & 0x1);
					int type = (instr >>> 5) & 0x3;
					int imm5 = (instr >>> 7) & 0x1f;
					int imm12 = op0_12;
					int t = op12_4;
					int m = op0_4;
					int W = ((instr >>> 21) & 0x1);
					int U = ((instr >>> 23) & 0x1);
					int P = ((instr >>> 24) & 0x1);
					Boolean wback = (P == 0x0 || W == 0x1);
					Boolean index = P == 0x1;
					Boolean add = U == 0x1;
					
					
						if ((A==0)  &&  (((op1 & 0x5)==0x0) && ((op1 & 0x17)!=0x2))) {	// Store Register (immediate, ARM) p.674 
							if (index && !wback) {	// Offset
								return "str" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm12 + "]";
							}
							if (index && wback) {	// Pre-index
								return "str" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm12 + "]!";
							}
							if (!index && wback) {	// Post-index
								return "str" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], #" + (add?"+":"-") + imm12;
							}
						}	// End of: Store Register (immediate, ARM) p.674
						if ((A==1)  &&  (((op1 & 0x5)==0x0) && ((op1 & 0x17)!=0x2))  &&  (B==0)) {	// Store Register (register) p.676
							if (index && !wback) {	// Offset
								return "str" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", R" + m + ", " + decodeShift(type, imm5) + "]";
							}
							if (index && wback) {	// Pre-index
								return "str" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", R" + m + ", " + decodeShift(type, imm5) + "]!";
							}
							if (!index && wback) {	// Post-index
								return "str" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], R" + m + ", " + decodeShift(type, imm5);
							}
						}	// End of: Store Register (register) p.676
						boolean cond1 = ((A==0)  &&  ((op1 & 0x17)==0x2) );
						boolean cond2 = ((A==1)  &&  ((op1 & 0x17)==0x2) );
						if (cond1 || cond2) {	// Store Register Unprivileged p.706
							if (A==0) {	// Encoding A1
								return "strt" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + n + ", " + (add?"+":"-") + imm12;
							}
							if (A==1) {	// Encoding A2
								return "strt" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", R" + n + ", " + (add?"+":"-") + "R" + m + ", " + decodeShift(type, imm5);								
							}							
						}	// End of: Store Register Unprivileged p.706
						if ((A==0)  &&  (((op1 & 0x5)==0x1) && ((op1 & 0x17)!=0x3))  &&  (n!=0xf)) {	// Load Register (immediate) p.408
							if (index && !wback) {	// Offset
								return "ldr" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm12 + "]";
							}
							if (index && wback) {	// Pre-index
								return "ldr" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm12 + "]!";
							}
							if (!index && wback) {	// Post-index
								return "ldr" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], #" + (add?"+":"-") + imm12;
							}
						}	// End of:  Load Register (immediate) p.408
						if ((A==0)  &&  (((op1 & 0x5)==0x1) && ((op1 & 0x17)!=0x3))  &&  (n==0xf)) {	// Load Register (literal) p.410
							return "ldr" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", " + (add?"+":"-") + imm12;
//							return "ldr" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [PC, #" + (add?"+":"-") + imm12 +"]";	// Alternative form
						}	// End of: Load Register (literal) p.410
						if ((A==1)  &&  (((op1 & 0x5)==0x1) && ((op1 & 0x17)!=0x3))  &&  (B==0)) {	// Load Register p.414
							if (index && !wback) {	// Offset
								return "ldr" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", " + (add?"+":"-") + "R" + m + ", " + decodeShift(type, imm5) + "]";
							}
							if (index && wback) {	// Pre-index
								return "ldr" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", " + (add?"+":"-") + "R" + m + ", " + decodeShift(type, imm5) + "]!";
							}
							if (!index && wback) {	// Post-index
								return "ldr" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], " + (add?"+":"-") + "R" + m + ", " + decodeShift(type, imm5);
							}
						}	// End of: Load Register p.414
						cond1 = ((A==0)  &&  ((op1 & 0x17)==0x3));
						cond2 = ((A==1)  &&  ((op1 & 0x17)==0x3)  &&  (B==0));
						if (cond1 || cond2) {	// Load Register Unprivileged p.466
							if (((instr >>> 25) & 0x1) == 0) {	// Encoding A1
								return "ldrt" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], #" + (add?"+":"-") + imm12;
							}
							else if (((instr >>> 25) & 0x1) == 1) {	// Encoding A2
								return "ldrt" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], " + (add?"+":"-") + "R" + m + ", " + decodeShift(type, imm5);
							}
						}	// End of: Load Register Unprivileged p.466
						if ((A==0)  &&  (((op1 & 0x5)==0x4) && ((op1 & 0x17)!=0x6))) {	// Store Register Byte (immediate) p.680
							if (index && !wback) {	// Offset
								return "strb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm12 + "]";
							}
							if (index && wback) {	// Pre-index
								return "strb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm12 + "]!";
							}
							if (!index && wback) {	// Post-index
								return "strb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], #" + (add?"+":"-") + imm12;
							}
						}	// End of: Store Register Byte (immediate) p.680
						if ((A==1)  &&  (((op1 & 0x5)==0x4) && ((op1 & 0x17)!=0x6))  &&  (B==0)) {	// Store Register Byte (register) p.682
							if (index && !wback) {	// Offset
								return "strb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", " + "R" + m + ", " + decodeShift(type, imm5) + "]";
							}
							if (index && wback) {	// Pre-index
								return "strb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", " + "R" + m + ", " + decodeShift(type, imm5) + "]!";
							}
							if (!index && wback) {	// Post-index
								return "strb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], " + "R" + m + ", " + decodeShift(type, imm5);
							}
						}	// End of: Store Register Byte (register) p.682
						cond1 = ((A==0)  &&  ((op1 & 0x17)==0x6));
						cond2 = ((A==1)  &&  ((op1 & 0x17)==0x6)  &&  (B==0));
						if (cond1 || cond2) {	// Store Register Byte Unprivileged p.684
							if (((instr >>> 25) & 0x1) == 0) {	// Encoding A1
								return "strbt" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], #" + (add?"+":"-") + imm12;
							}
							else if (((instr >>> 25) & 0x1) == 1) {	// Encoding A2
								return "strbt" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], " + (add?"+":"-") + "R" + m + ", " + decodeShift(type, imm5);
							}
						}	// End of: Store Register Byte Unprivileged p.684
						if ((A==0)  &&  (((op1 & 0x5)==0x5) && ((op1 & 0x17)!=0x7))  &&  (n!=0xf)) {	// Load Register Byte (immediate) p.418
							if (index && !wback) {	// Offset
								return "ldrb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm12 + "]";
							}
							if (index && wback) {	// Pre-index
								return "ldrb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", #" + (add?"+":"-") + imm12 + "]!";
							}
							if (!index && wback) {	// Post-index
								return "ldrb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], #" + (add?"+":"-") + imm12;
							}
						}	// End of:  Load Register Byte (immediate) p.418
						if ((A==0)  &&  (((op1 & 0x5)==0x5) && ((op1 & 0x17)!=0x7))  &&  (n==0xf)) {	// Load Register Byte (literal) p.420
							return "ldrb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", " + (add?"+":"-") + imm12;
//							return "ldrb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [PC, #" + (add?"+":"-") + imm12 +"]";	// Alternative form
						}	// End of:  Load Register Byte (literal) p.420
						if ((A==1)  &&  (((op1 & 0x5)==0x5) && ((op1 & 0x17)!=0x7))) {	// Load Register Byte (register) p.422
							if (index && !wback) {	// Offset
								return "ldrb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", " + (add?"+":"-") + "R" + m + ", " + decodeShift(type, imm5) + "]";
							}
							if (index && wback) {	// Pre-index
								return "ldrb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + ", " + (add?"+":"-") + "R" + m + ", " + decodeShift(type, imm5) + "]!";
							}
							if (!index && wback) {	// Post-index
								return "ldrb" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], " + (add?"+":"-") + "R" + m + ", " + decodeShift(type, imm5);
							}
						}	// End of:  Load Register Byte (register) p.422
						cond1 = ((A==0)  &&  ((op1 & 0x17)==0x7));
						cond2 = ((A==1)  &&  ((op1 & 0x17)==0x7)  &&  (B==0));
						if (cond1 || cond2) {	// Load Register Byte Unprivileged p.424
							if (((instr >>> 25) & 0x1) == 0) {	// Encoding A1
								return "ldrbt" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], #" + (add?"+":"-") + imm12;
							}
							else if (((instr >>> 25) & 0x1) == 1) {	// Encoding A2
								return "ldrbt" + (cond!=condAlways?condString[cond]:"") + " R" + t + ", [R" + n + "], " + (add?"+":"-") + "R" + m + ", " + decodeShift(type, imm5);
							}
						}	// End of: Load Register Byte Unprivileged p.424
				}	// End of:  Load/store word and unsigned byte p.208
				
				
				if ((op==0x3)&&(op4_1 == 0x1)) {	// Media instructions p.209
					int imm5 = op7_5;
					int sh = op6_1;
					int M = op5_1;
					int R = op5_1;
					int m = op0_4;

					if ((op1 & 0x1c) == 0x00) {	// Parallel addition and subtraction, signed p.210
						if (op20_2 == 1) {
							switch(op5_3) {
							case 0: return "sadd16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.586
							case 1: return "sasx" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.590
							case 2: return "ssax" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.656
							case 3: return "ssub16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.658
							case 4: return "sadd8" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.588
							case 7: return "ssub8" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.660								
							default: break;
							}	// End of (op5_3)
						}	// End of (op20_2 == 1) 
						if (op20_2 == 2) {	// Saturation instructions
							switch(op5_3) {
							case 0: return "qadd16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.542
							case 1: return "qasx" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.546
							case 2: return "qsax" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.552
							case 3: return "qsub16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.556
							case 4: return "qadd8" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.544
							case 7: return "qsub8" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.558								
							default: break;
							}	// End of (op5_3)
						}	// End of (op20_2 == 2): Saturation instructions
						if (op20_2 == 3) {	// Halving instructions
							switch(op5_3) {
							case 0: return "shadd16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.608
							case 1: return "shasx" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.612
							case 2: return "shsax" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.614
							case 3: return "shsub16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.616
							case 4: return "shadd8" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.610
							case 7: return "shsub8" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.618								
							default: break;
							}	// End of (op5_3)
						}	// End of (op20_2 == 3): Halving instructions
					}	// End of ((op1 & 0x1c) == 0x00): Parallel addition and subtraction, signed p.210
					
					if ((op1 & 0x1c) == 0x04) {	// Parallel addition and subtraction, UNsigned p.211
						if (op20_2 == 1) {
							switch(op5_3) {
							case 0: return "uadd16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.750
							case 1: return "uasx" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.754
							case 2: return "usax" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.800
							case 3: return "usub16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.802
							case 4: return "uadd8" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.752
							case 7: return "usub8" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.804								
							default: break;
							}	// End of (op5_3)
						}	// End of (op20_2 == 1) 
						if (op20_2 == 2) {	// Saturation instructions
							switch(op5_3) {
							case 0: return "uqadd16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.780
							case 1: return "uqasx" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.784
							case 2: return "uqsax" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.786
							case 3: return "uqsub16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.788
							case 4: return "uqadd8" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.782
							case 7: return "uqsub8" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.790								
							default: break;
							}	// End of (op5_3)
						}	// End of (op20_2 == 2): Saturation instructions
						if (op20_2 == 3) {	// Halving instructions
							switch(op5_3) {
							case 0: return "uhadd16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.762
							case 1: return "uhasx" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.766
							case 2: return "uhsax" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.768
							case 3: return "uhsub16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.770
							case 4: return "uhadd8" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.764
							case 7: return "uhsub8" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;	// p.772								
							default: break;
							}	// End of (op5_3)
						}	// End of (op20_2 == 3): Halving instructions
					}	// End of ((op1 & 0x1c) == 0x04): Parallel addition and subtraction, UNsigned p.211
					
					if ((op1 & 0x18) == 0x08) {	// Packing, unpacking, saturation and reversal p.212
						if ((op20_3==0x0) && ((op5_3 & 0x1)==0x0)) {	// Pack Halfword p.522
							if (((instr >>> 6) & 0x1) == 0x0){	// Left Shift
								return "pkhbt" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[0] + " #" + imm5;
							} 
							else {	// Right Shift
								return "pkhtb" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", " + shiftType[2] + " #" + imm5;
							}
						}	// End of: Pack Halfword p.522
						if ((op20_3==0x0) && (op5_3 == 0x3) && (op16_4 != 0xf)) {	// Signed Extend and Add Byte 16-bit p.726
							return "sxtab16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", ROR #" + (8*op10_2);
						}	// End of: Signed Extend and Add Byte 16-bit p.726
						if ((op20_3==0x0) && (op5_3 == 0x3) && (op16_4 == 0xf)) {	// Signed Extend Byte 16-bit p.732
							return "sxtb16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m + ", ROR #" + (8*op10_2);
						}	// End of: Signed Extend Byte 16-bit p7.32
						if ((op20_3==0x0) && (op5_3 == 0x5)) {	// Select Bytes p.602
							return "sel" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;
						}	// End of: Select Bytes p.602
						
						if (((op20_3 & 0x6)==0x2) && ((op5_3 & 0x1)==0x0)) {	// Signed Saturate p.652
							return "ssat" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", #" + (op16_5+1) + ", R" + op0_4 + ", " + decodeShift(sh*2, imm5);
						}	// End of: Signed Saturate p.652
						
						if ((op20_3==0x2) && (op5_3 == 0x1)) {	// Signed Saturate, two 16-bit p.654
							return "ssat16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", #" + (op16_5+1) + ", R" + op0_4;
						}	// End of: Signed Saturate, two 16-bit p.654
						if ((op20_3==0x2) && (op5_3 == 0x3) && (op16_4 != 0xf)) {	// Signed Extend and Add Byte .p724
							return "sxtab" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", ROR #" + (8*op10_2);
						}	// End of: Signed Extend and Add Byte p.724
						if ((op20_3==0x2) && (op5_3 == 0x3) && (op16_4 == 0xf)) {	// Signed Extend Byte .p730
							return "sxtb" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m + ", ROR #" + (8*op10_2);
						}	// End of: Signed Extend Byte p.730

						if ((op20_3==0x3) && (op5_3 == 0x1)) {	// Byte-Reverse Word p.562
							return "rev" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m;
						}	// End of: Byte-Reverse Word p.562
						if ((op20_3==0x3) && (op5_3 == 0x3) && (op16_4 != 0xf)) {	// Signed Extend and Add Halfword p.728
							return "sxtah" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", ROR #" + (8*op10_2);
						}	// End of: Signed Extend and Add Halfword p.728
						if ((op20_3==0x3) && (op5_3 == 0x3) && (op16_4 == 0xf)) {	// Signed Extend Halfword p.734
							return "sxth" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m + ", ROR #" + (8*op10_2);
						}	// End of: Signed Extend Halfword p.734
						if ((op20_3==0x3) && (op5_3 == 0x5)) {	// Byte-Reverse Packed Halfword p.564
							return "rev16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m;
						}	// End of: Byte-Reverse Packed Halfword p.564

						if ((op20_3==0x4) && (op5_3 == 0x3) && (op16_4 != 0xf)) {	// Unsigned Extend and Add Byte 16-Bit p.808
							return "uxtab16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", ROR #" + (8*op10_2);
						}	// End of: Unsigned Extend and Add Byte 16-Bit p.808
						if ((op20_3==0x4) && (op5_3 == 0x3) && (op16_4 == 0xf)) {	// Unsigned Extend Byte 16-Bit p.814
							return "uxtb16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m + ", ROR #" + (8*op10_2);
						}	// End of: Unsigned Extend Byte 16-Bit p.814
						
						if (((op20_3 & 0x6)==0x6) && ((op5_3 & 0x1)==0x0)) {	// Unsigned Saturate p.796
							return "usat" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", #" + op16_5 + ", R" + op0_4 + ", " + decodeShift(sh*2, imm5);
						}	// End of: Unsigned Saturate p.796
						
						if ((op20_3==0x6) && (op5_3 == 0x1)) {	// Unsigned Saturate, two 16-bit p.798
							return "usat16" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", #" + op16_4 + ", R" + op0_4;
						}	// End of: Unsigned Saturate, two 16-bit p.798
						if ((op20_3==0x6) && (op5_3 == 0x3) && (op16_4 != 0xf)) {	// Unsigned Extend and Add Byte p.806
							return "uxtab" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", ROR #" + (8*op10_2);
						}	// End of: Unsigned Extend and Add Byte p.806
						if ((op20_3==0x6) && (op5_3 == 0x3) && (op16_4 == 0xf)) {	// Unsigned Extend Byte p.812
							return "uxtb" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m + ", ROR #" + (8*op10_2);
						}	// End of: Unsigned Extend Byte p.812

						if ((op20_3==0x3) && (op5_3 == 0x1)) {	// Reverse Bits p.560
							return "rbit" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m;
						}	// End of: Reverse Bits p.560
						if ((op20_3==0x7) && (op5_3 == 0x3) && (op16_4 != 0xf)) {	// Unsigned Extend and Add Halfword p.810
							return "uxtah" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m + ", ROR #" + (8*op10_2);
						}	// End of: Unsigned Extend and Add Halfword p.810
						if ((op20_3==0x7) && (op5_3 == 0x3) && (op16_4 == 0xf)) {	// Unsigned Extend Halfword p.816
							return "uxth" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m + ", ROR #" + (8*op10_2);
						}	// End of: Unsigned Extend Halfword p.816
						if ((op20_3==0x7) && (op5_3 == 0x5)) {	// Byte-Reverse Signed Halfword p.566
							return "revsh" + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + m;
						}	// End of: Byte-Reverse Signed Halfword p.566
					}	// End of ((op1 & 0x18) == 0x08): Packing, unpacking, saturation and reversal p.212
					
					if ((op1 & 0x18) == 0x10) {	// Signed multiply, signed and unsigned divide p.213
						if ((op20_3==0x0) && ((op5_3 & 0x6)==0x0) && (op12_4 != 0xf)) {	// Signed Multiply Accumulate Dual p.622
							return "smlad" + (M==0x1?"x":"") + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + op8_4 + ", R" + op12_4;
						}	// End of: Signed Multiply Accumulate Dual p.622
						if ((op20_3==0x0) && ((op5_3 & 0x6)==0x0) && (op12_4 == 0xf)) {	// Signed Dual Multiply Add p.642
							return "smuad" + (M==0x1?"x":"") + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + op8_4;
						}	// End of: Signed Dual Multiply Add p.642
						if ((op20_3==0x0) && ((op5_3 & 0x6)==0x2) && (op12_4 != 0xf)) {	// Signed Multiply Subtract Dual p.632
							return "smlsd" + (M==0x1?"x":"") + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + op8_4 + ", R" + op12_4;
						}	// End of: Signed Multiply Subtract Dual p.632
						if ((op20_3==0x0) && ((op5_3 & 0x6)==0x2) && (op12_4 == 0xf)) {	// Signed Dual Multiply Subtract p.650
							return "smusd" + (M==0x1?"x":"") + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + op8_4;
						}	// End of: Signed Dual Multiply Subtract p.650
						
						if ((op20_3==0x1) && (op5_3 == 0x0)) {	// Signed Divide p.600
							return "sdiv" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + op8_4;
						}	// End of: Signed Divide p.600
						
						if ((op20_3==0x3) && (op5_3 == 0x0)) {	// Unsigned Divide p.760
							return "udiv" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + op8_4;
						}	// End of: Unsigned Divide p.760
						
						if ((op20_3==0x4) && ((op5_3 & 0x6)==0x0)) {	// Signed Multiply Accumulate Long Dual p.628
							return "smlald" + (M==0x1?"x":"") + (cond!=condAlways?condString[cond]:"") + " R" + op12_4 + ", R" + op16_4 + ", R" + op0_4 + ", R" + op8_4;
						}	// Signed Multiply Accumulate Long Dual p.628					
						if ((op20_3==0x4) && ((op5_3 & 0x6)==0x2)) {	// Signed Multiply Subtract Long Dual p.634
							return "smlsld" + (M==0x1?"x":"") + (cond!=condAlways?condString[cond]:"") + " R" + op12_4 + ", R" + op16_4 + ", R" + op0_4 + ", R" + op8_4;
						}	// End of: Unsigned Multiply Subtract Long Dual p.634
						
						if ((op20_3==0x5) && ((op5_3 & 0x6)==0x0) && (op12_4 != 0xf)) {	// Signed Most Significant Word Multiply Accumulate p.636
							return "smmla" + (R==0x1?"r":"") + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + op8_4 + ", R" + op12_4;
						}	// End of: Signed Most Significant Word Multiply Accumulate p.636
						if ((op20_3==0x5) && ((op5_3 & 0x6)==0x0) && (op12_4 == 0xf)) {	// Signed Most Significant Word Multiply p.640
							return "smmul" + (R==0x1?"r":"") + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + op8_4;
						}	// End of: Signed Most Significant Word Multiply p.640
						if ((op20_3==0x5) && ((op5_3 & 0x6)==0x6)) {	// Signed Most Significant Word Multiply Subtract p.638
							return "smmls" + (R==0x1?"r":"") + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + op8_4 + ", R" + op12_4;
						}	// End of: Signed Most Significant Word Multiply Subtract p.638
					}	// End of ((op1 & 0x18) == 0x10): Signed multiply, signed and unsigned divide p.213
					
					if ((op1 == 0x18) && (op5_3 == 0) && (d == 0xf)) {	// Unsigned Sum of Absolute Differences p.792
						return "usad8" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + op8_4;
					}	// End of: Unsigned Sum of Absolute Differences p.792
					
					if ((op1 == 0x18) && (op5_3 == 0) && (d != 0xf)) {	// Unsigned Sum of Absolute Differences and Accumulate p.794
						return "usada8" + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + ", R" + op0_4 + ", R" + op8_4 + ", R" + op12_4;
					}	// End of: Unsigned Sum of Absolute Differences and Accumulate p.794
					
					if (((op1 & 0x1e) == 0x1a) && ((op5_3 & 0x3) == 0x2)) {	// Signed Bit Field Extract p.598
						return "sbfx" + (cond!=condAlways?condString[cond]:"") + " R" + op12_4 + ", R" + op0_4 + ", #" + op7_5 + ", #" + (op16_5+1);
					}	// End of: Signed Bit Field Extract p.598
					
					if (((op1 & 0x1e) == 0x1c) && ((op5_3 & 0x3) == 0x0) && (op0_4 == 0xf)) {	// Bit Field Clear p.336
						return "bfc" + (cond!=condAlways?condString[cond]:"") + " R" + op12_4 + ", #" + op7_5 + ", #" + (op16_5-op7_5+1);
					}	// End of: Bit Field Clear p.336
					
					if (((op1 & 0x1e) == 0x1c) && ((op5_3 & 0x3) == 0x0) && (op0_4 != 0xf)) {	// Bit Field Insert p.338
						return "bfi" + (cond!=condAlways?condString[cond]:"") + " R" + op12_4 + ", R" + op0_4 + ", #" + op7_5 + ", #" + (op16_5-op7_5+1);
					}	// End of: Bit Field Insert p.338
					
					if (((op1 & 0x1e) == 0x1e) && ((op5_3 & 0x3) == 0x2)) {	// Unsigned Bit Field Extract p.756
						return "ubfx" + (cond!=condAlways?condString[cond]:"") + " R" + op12_4 + ", R" + op0_4 + ", #" + op7_5 + ", #" + (op16_5+1);
					}	// End of: Unsigned Bit Field Extract p.756
					
					if (((op1 & 0x1f) == 0x1f) && ((op5_3 & 0x7) == 0x7) && (cond == 0xd)) {	// Permanently UNDEFINDED p.758
						int imm16 = ((instr >>> 4) & 0xfff0) + op0_4;
						return "udf" + (cond!=condAlways?condString[cond]:"") + " #" + imm16;
					}	// Permanently UNDEFINDED p.758
					
					if (((op1 & 0x1f) == 0x1f) && ((op5_3 & 0x7) == 0x7) && (cond != 0xd)) {	// -a
						return "undefined: see not '-a' on p.209";
					}	// -a
					
				}	// End of: Media instructions p.209
				break;
			}// End of (op=2/3):
				
				

			case 4: case 5:{	// Branch, branch with link, and block data transfer p.214
				if ((op20_6 & 0x3d) == 0x00)  {	// Store Multiple Decrement After p.666
					return "stmda" + (cond!=condAlways?condString[cond]:"") + " R" + n + (op21_1==1?"!":"") + ", " + registerlist(op0_16);
				}
				if ((op20_6 & 0x3d) == 0x01)  {	// Load Multiple Decrement After p.400
					return "ldmda" + (cond!=condAlways?condString[cond]:"") + " R" + n + (op21_1==1?"!":"") + ", " + registerlist(op0_16);
				}
				if ((op20_6 & 0x3d) == 0x08)  {	// Store Multiple Increment After p.664
					return "stm" + (cond!=condAlways?condString[cond]:"") + " R" + n + (op21_1==1?"!":"") + ", " + registerlist(op0_16);
				}
				if ((op20_6==0x09) || ((op20_6==0x0b) && (n!=0xd)))  {	// Load Multiple Increment After p.398
					return "ldm" + (cond!=condAlways?condString[cond]:"") + " R" + n + (op21_1==1?"!":"") + ", " + registerlist(op0_16);
				}
				if ((op20_6==0x0b) && (n==0xd))  {	// Pop multiple registers p.536
					return "pop" + (cond!=condAlways?condString[cond]:"") + " " + registerlist(op0_16);
				}
				if ((op20_6==0x10) || ((op20_6==0x12) && (n!=0xd)))  {	// Store Multiple Decrement Before p.668
					return "stmdb" + (cond!=condAlways?condString[cond]:"") + " R" + n + (op21_1==1?"!":"") + ", " + registerlist(op0_16);
				}
				if ((op20_6==0x12) && (n==0xd)) {	// Push multiple registers p.538
					return "push" + (cond!=condAlways?condString[cond]:"") + " " + registerlist(op0_16);
				}
				if ((op20_6 & 0x3d) == 0x11) {	// Load Multiple Decrement Before p.402
					return "ldmdb" + (cond!=condAlways?condString[cond]:"") + " R" + n + (op21_1==1?"!":"") + ", " + registerlist(op0_16);
				}
				if ((op20_6 & 0x3d) == 0x18) {	// Store Multiple Increment Before p.670
					return "stmib" + (cond!=condAlways?condString[cond]:"") + " R" + n + (op21_1==1?"!":"") + ", " + registerlist(op0_16);
				}
				if ((op20_6 & 0x3d) == 0x19) {	// Load Multiple Increment Before p.404
					return "ldmib" + (cond!=condAlways?condString[cond]:"") + " R" + n + (op21_1==1?"!":"") + ", " + registerlist(op0_16);
				}
				if ((op20_6 & 0x25) == 0x04) {	// Store Multiple (user registers) p.2008
					return "stm" + amode[op23_2] + (cond!=condAlways?condString[cond]:"") + " R" + n + ", " + registerlist(op0_16) + "^";
				}
				if (((op20_6 & 0x25) == 0x05) && (op15_1==0)) {	// Load Multiple (user registers) p.1988
					return "ldm	" + amode[op23_2] + (cond!=condAlways?condString[cond]:"") + " R" + n + ", " + registerlist(op0_15) + "^";	// WITHOUT PC
				}
				if (((op20_6 & 0x25) == 0x05) && (op15_1==1)) {	// Load Multiple (exception return) p.1986
					return "ldm	" + amode[op23_2] + (cond!=condAlways?condString[cond]:"") + " R" + n + (op21_1==1?"!":"") + ", " + registerlist(op0_15) + "^";	// WITH PC
				}
				if ((op20_6 & 0x30) == 0x20) {	// Branch p.334
					return "b" + (cond!=condAlways?condString[cond]:"") + " " + (((op0_24<<8)>>6)+8);	// SignExtend(imm24:'00', 32); Multiplied by 4, correct for pipeline stage
				}
				if ((op20_6 & 0x30) == 0x30) {	// Branch with Link p.348
					// Encoxding A1 (cond != 0xf)
					return "bl" + (cond!=condAlways?condString[cond]:"") + " " + (((op0_24<<8)>>6)+8);	// SignExtend(imm24:'00', 32); Multiplied by 4, correct for pipeline stage
				}	
				break;
			}// End of (op=4/5): Branch, branch with link, and block data transfer p.214

			
			
			case 6: case 7:{	// Coprocessor instructions, and Supervisor Call p.215
				Boolean add = (op23_1 == 0x1);

				if ((op20_6 & 0x3e) == 0x00)  {	// UNDEFINED
					return "undefined p.215";
				}
				if ((op20_6 & 0x30) == 0x30)  {	// Supervisor Call p.720
					return "svc" + (cond!=condAlways?condString[cond]:"") + " #" + op0_24;
				}
				if ((op8_4 & 0xe) != 0xa) {
					if ( ((op20_6 & 0x21)==0x00) && ((op20_6 & 0x3b)!=0x00) ) {	// Store Coprocessor p.662
						if ((op24_1 == 1) && (op21_1 == 0)) {	// Offset. P = 1, W = 0.
							return "stc" + (op22_1==0x1?"l":"") + (cond!=condAlways?condString[cond]:"") + " P" + op8_4 + ", CR" + d + ", [R" + n + ", #" + (add?"+":"-") + op0_8 +"]";
						}
						if ((op24_1 == 1) && (op21_1 == 1)) {	// Pre-indexed. P = 1, W = 1.
							return "stc" + (op22_1==0x1?"l":"") + (cond!=condAlways?condString[cond]:"") + " P" + op8_4 + ", CR" + d + ", [R" + n + ", #" + (add?"+":"-") + op0_8 +"]!";
						}
						if ((op24_1 == 0) && (op21_1 == 1)) {	// Post-indexed. P = 0, W = 1.
							return "stc" + (op22_1==0x1?"l":"") + (cond!=condAlways?condString[cond]:"") + " P" + op8_4 + ", CR" + d + ", [R" + n + "], #" + (add?"+":"-") + op0_8;
						}
						if ((op24_1 == 0) && (op21_1 == 0) && (op23_1 == 1)) {	// Unindexed. P = 0, W = 0, U = 1.
							return "stc" + (op22_1==0x1?"l":"") + (cond!=condAlways?condString[cond]:"") + " P" + op8_4 + ", CR" + d + ", [R" + n + "], {" + op0_8 +"}";
						}
					}
					if ( ((op20_6 & 0x21)==0x01) && ((op20_6 & 0x3b)!=0x01) ) {
						if (n != 0xf) {	// Load Coprocessor (immediate) p.392
							if ((op24_1 == 1) && (op21_1 == 0)) {	// Offset. P = 1, W = 0.
								return "ldc" + (op22_1==0x1?"l":"") + (cond!=condAlways?condString[cond]:"") + " P" + op8_4 + ", CR" + d + ", [R" + n + ", #" + (add?"+":"-") + op0_8 +"]";
							}
							if ((op24_1 == 1) && (op21_1 == 1)) {	// Pre-indexed. P = 1, W = 1.
								return "ldc" + (op22_1==0x1?"l":"") + (cond!=condAlways?condString[cond]:"") + " P" + op8_4 + ", CR" + d + ", [R" + n + ", #" + (add?"+":"-") + op0_8 +"]!";
							}
							if ((op24_1 == 0) && (op21_1 == 1)) {	// Post-indexed. P = 0, W = 1.
								return "ldc" + (op22_1==0x1?"l":"") + (cond!=condAlways?condString[cond]:"") + " P" + op8_4 + ", CR" + d + ", [R" + n + "], #" + (add?"+":"-") + op0_8;
							}
							if ((op24_1 == 0) && (op21_1 == 0) && (op23_1 == 1)) {	// Unindexed. P = 0, W = 0, U = 1.
								return "ldc" + (op22_1==0x1?"l":"") + (cond!=condAlways?condString[cond]:"") + " P" + op8_4 + ", CR" + d + ", [R" + n + "], {" + op0_8 +"}";
							}
						}
						else if (n != 0xf) {	// Load Coprocessor (literal) p.394
							if ((op24_1 == 1) && (op21_1 == 0)) {	// P = 1, W = 0.
								return "ldc" + (op22_1==0x1?"l":"") + (cond!=condAlways?condString[cond]:"") + ", CR" + d + (add?"+":"-") + ((op0_8<<24)>>22);
							}
							if ((op24_1 == 0) && (op23_1 == 1) && (op21_1 == 0)) {	// Unindexed. P = 0, U = 1, W = 0.
								return "ldc" + (op22_1==0x1?"l":"") + (cond!=condAlways?condString[cond]:"") + " P" + op8_4 + ", CR" + d + ", [R" + n + "], {" + op0_8 + "}";
							}

						}
					}
					if (op20_6 == 0x04) {	// Move to Coprocessor from two ARM core registers p.478
						return "mcrr" + (cond!=condAlways?condString[cond]:"") + " P" + op8_4 + ", #" + op4_4 + ", R" + op12_4 + ", R" + op16_4 + ", CR" + op0_4;
					}
					if (op20_6 == 0x05) {	// Move to two ARM core registers from Coprocessor p.494
						return "mrrc" + (cond!=condAlways?condString[cond]:"") + " P" + op8_4 + ", #" + op4_4 + ", R" + op12_4 + ", R" + op16_4 + ", CR" + op0_4;
					}
					if ( ((op20_6 & 0x30)==0x20) && (op4_1==0x0) ) {	// Coprocessor data operations p.358
						return "cdp" + (cond!=condAlways?condString[cond]:"") + " p" + op8_4 + ", #" + op20_4 + ", CR" + op12_4 + ", CR" + op16_4 + ", CR" + op0_4 + ", #" + op5_3;
					}
					if ( ((op20_6 & 0x31)==0x20) && (op4_1==0x1) ) {	// Move to Coprocessor from ARM core register p.476
						return "mcr" + (cond!=condAlways?condString[cond]:"") + " p" + op8_4 + ", R" + op21_3 + ", R" + op12_4 + ", CR" + op16_4 + ", CR" + op0_4 + ", #" + op5_3;
					}
					if ( ((op20_6 & 0x31)==0x21) && (op4_1==0x1) ) {	// Move to ARM core register from Coprocessor p.492
						return "mrc" + (cond!=condAlways?condString[cond]:"") + " p" + op8_4 + ", R" + op21_3 + ", R" + op12_4 + ", CR" + op16_4 + ", CR" + op0_4 + ", #" + op5_3;
					}
				}	// End of: ((op8_4 & 0xe) != 0xa)
				
				if ((op8_4 & 0xe) == 0xa) {
				
					// SIMD not implemented
					String mode = "";
					if ((op24_1==0) && (op23_1==1)) mode = "ia";	// Increment After. Encoded as P = 0, U = 1.
					if ((op24_1==1) && (op23_1==0)) mode = "db";	// Decrement Before. Encoded as P = 1; U = 0.
					int VmM = (op0_4<<1) + op5_1;
					int MVm = (op5_1<<4) + op0_4;
					int VdD = (op12_4<<1) + op22_1;
					int DVd = (op22_1<<4) + op12_4;

					if ( ((op20_6 & 0x20)==0x00) && ((op20_6 & 0x3a)!=0x00) ) {	// Advanced SIMD, Floating-point p.274	

						// WARNING: <list> not implemented;
						
						if ((op20_5 & 0x1e)==0x04) {	// 64-bit transfers between ARM core and extension registers p.279
							if ( (op8_1==0x0) && ((op4_4 & 0xd)==0x1) ) {	// VMOV (between two ARM core registers and two single-precision registers) p.946
								if (op20_1 == 0x0) {	// Encoded as op = 0
									return "vmov" + (cond!=condAlways?condString[cond]:"") + " S" + VmM + ", S" + (VmM) + ", R" + op12_4 + ", R" + op16_4;
								}
								else if (op20_1 == 0x1) {	// Encoded as op = 1
									return "vmov" + (cond!=condAlways?condString[cond]:"") + " R" + op12_4 + ", R" + op16_4 + ", S" + VmM + ", S" + (VmM+1);
								}
							}
							if ( (op8_1==0x1) && ((op4_4 & 0xd)==0x1) ) {	// VMOV (between two ARM core registers and a doubleword extension register) p.948
								if (op20_1 == 0x0) {	// Encoded as op = 0
									return "vmov" + (cond!=condAlways?condString[cond]:"") + " D" + MVm + ", R" + op12_4 + ", R" + op16_4;
								}
								else if (op20_1 == 0x1) {	// Encoded as op = 1
									return "vmov" + (cond!=condAlways?condString[cond]:"") + " R" + op12_4 + ", R" + op16_4 + ", D" + MVm;
								}
							}
						}
//						if ( ((op20_5 & 0x19)==0x08) || (((op20_5 & 0x1b)==0x12)&&(n!=0xd)) ){	// Vector Store Multiple p.1080
//							if (op8_1 == 0x1) {	// Encoding A1
//								return "vstm" + mode + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + (op21_1==0x1?"!":"") + list(DVd);
//							}
//							else if (op8_1 == 0x0) {	// Encoding A2
//								return "vstm" + mode + (cond!=condAlways?condString[cond]:"") + " R" + op16_4 + (op21_1==0x1?"!":"") + list(VdD);
//							}
//						}
//						if ((op20_5 & 0x13)==0x10) {	// Vector Store Register p.1082
//							if (op8_1 == 0x1) {	// Encoding A1
//								return "vstr" + (cond!=condAlways?condString[cond]:"") + ".64 D" + DVd + ", [R" + n + ", #" + (add?"+":"-") + (op0_8<<2);
//							}
//							else if (op8_1 == 0x0) {	// Encoding A2
//								return "vstr" + (cond!=condAlways?condString[cond]:"") + ".32 S" + VdD + ", [R" + n + ", #" + (add?"+":"-") + (op0_8<<2);
//							}
//						}
//						if (((op20_5 & 0x1b)==0x12)&&(n==0xd)) {	// Vector Push Registers p.992
//							if (op8_1 == 0x1) {	// Encoding A1
//								return "vpush" + (cond!=condAlways?condString[cond]:"") + list(DVd);
//							}
//							else if (op8_1 == 0x0) {	// Encoding A2
//								return "vpush" + (cond!=condAlways?condString[cond]:"") + list(VdD);
//							}
//						}
//						if ( ((op20_5 & 0x1b)==0x09) || (((op20_5 & 0x1b)==0x0b)&&(n!=0xd)) || ((op20_5 & 0x1b)==0x13) ){	// Vector Load Multiple p.922
//							if (op8_1 == 0x1) {	// Encoding A1
//								return "vldm" + mode + (cond!=condAlways?condString[cond]:"") + " R" + n + (op21_1==0x1?"!":"") + ", " + list(DVd);
//							}
//							else if (op8_1 == 0x0) {	// Encoding A2
//								return "vldm" + mode + (cond!=condAlways?condString[cond]:"") + " R" + n + (op21_1==0x1?"!":"") + ", " + list(VdD);
//							}							
//						}
//						if (((op20_5 & 0x1b)==0x0b)&&(n==0xd)) {	// Vector Pop Registers p.990
//							if (op8_1 == 0x1) {	// Encoding A1
//								return "vpop" + (cond!=condAlways?condString[cond]:"") + list(DVd);
//							}
//							else if (op8_1 == 0x0) {	// Encoding A2
//								return "vpop" + (cond!=condAlways?condString[cond]:"") + list(DVd);
//							}							
//						}
//						if ((op20_5 & 0x13)==0x11) {	// Vector Load Register p.924
//							if (op8_1 == 0x1) {	// Encoding A1
//								return "vldr" + (cond!=condAlways?condString[cond]:"") + ".64 D" + DVd + ", [R" + n + ", #" + (add?"+":"-") + (op0_8<<2);
//							}
//							else if (op8_1 == 0x0) {	// Encoding A2
//								return "vldr" + (cond!=condAlways?condString[cond]:"") + ".32 S" + VdD + ", [R" + n + ", #" + (add?"+":"-") + (op0_8<<2);
//							}								
//						}
					}	// End of: Advanced SIMD, Floating-point p.274

					if ((op20_6 & 0x3e)==0x04) {	// Advanced SIMD, Floating-point p.279
						if ( (op8_1==0x0) && ((op4_4 & 0xb)==0x1) ) {	// VMOV (between two ARM core registers and two single-precision registers) p.946
							if (op20_1 == 0x0) {	// Encoded as op = 0
								return "vmov" + (cond!=condAlways?condString[cond]:"") + " S" + VmM + ", S" + (VmM) + ", R" + op12_4 + ", R" + op16_4;
							}
							else if (op20_1 == 0x1) {	// Encoded as op = 1
								return "vmov" + (cond!=condAlways?condString[cond]:"") + " R" + op12_4 + ", R" + op16_4 + ", S" + VmM + ", S" + (VmM+1);
							}
						}
						if ( (op8_1==0x1) && ((op4_4 & 0xb)==0x1) ) {	// VMOV (between two ARM core registers and a doubleword extension register) p.948
							if (op20_1 == 0x0) {	// Encoded as op = 0
								return "vmov" + (cond!=condAlways?condString[cond]:"") + " D" + MVm + ", R" + op12_4 + ", R" + op16_4;
							}
							else if (op20_1 == 0x1) {	// Encoded as op = 1
								return "vmov" + (cond!=condAlways?condString[cond]:"") + " R" + op12_4 + ", R" + op16_4 + ", D" + MVm;
							}							
						}						
					}	// End of: Advanced SIMD, Floating-point p.279

//					if ( ((op20_6 & 0x30)==0x20) && (op4_1==0x0) ) {	// Floating-point data processing p.272
//						if ((op20_4 & 0xb)==0x0) {	// Vector Multiply Accumulate or Subtract p.932	Encoding = A2
//							if (op8_1==0x1) {	// sz=1
//								return "v" + (op6_1==0?"mla":"mls") + (cond!=condAlways?condString[cond]:"") + ".f64 D" + DVd + ", D" + n + ", D" + op0_4;
//							}
//							else if (op8_1==0x0) {	// sz=0
//								return "v" + (op6_1==0?"mla":"mls") + (cond!=condAlways?condString[cond]:"") + ".f32 S" + VdD + ", S" + n + ", S" + op0_4;
//							}
//						}
//						if ((op20_4 & 0xb) == 0x1) {	// Vector Negate Multiply Accumulate or Subtract p.970	Encoding = A1
//							if (op8_1==0x1) {	// sz=1
//								return "vn" + (op6_1==0?"mla":"mls") + (cond!=condAlways?condString[cond]:"") + ".f64 D" + DVd + ", D" + n + ", D" + op0_4;
//							}
//							else if (op8_1==0x0) {	// sz=0
//								return "vn" + (op6_1==0?"mla":"mls") + (cond!=condAlways?condString[cond]:"") + ".f32 S" + VdD + ", S" + n + ", S" + op0_4;
//							}
//						}
//						if (((op20_4 & 0xb)==0x2) && (op6_1==0x1)) {	// Vector Negate Multiply Accumulate or Subtract p.970	Encoding = A2
//							if (op8_1==0x1) {	// sz=1
//								return "vnmul" + (cond!=condAlways?condString[cond]:"") + ".f64 D" + DVd + ", D" + n + ", D" + op0_4;
//							}
//							else if (op8_1==0x0) {	// sz=0
//								return "vnmul" + (cond!=condAlways?condString[cond]:"") + ".f32 S" + VdD + ", S" + n + ", S" + op0_4;
//							}
//						}
//						if ( ((op20_4 & 0xb)==0x2) && (op6_1==0x0) ) {	// Vector Multiply p.960	Encoding = A2
//							if (op8_1==0x1) {	// sz=1
//								return "vmul" + (cond!=condAlways?condString[cond]:"") + ".f64 D" + DVd + ", D" + n + ", D" + op0_4;
//							}
//							else if (op8_1==0x0) {	// sz=0
//								return "vmul" + (cond!=condAlways?condString[cond]:"") + ".f32 S" + VdD + ", S" + n + ", S" + op0_4;
//							}
//						}
//						if ( ((op20_4 & 0xb)==0x3) && (op6_1==0x0) ) {	// Vector Add p.830	Encoding = A2
//							if (op8_1==0x1) {	// sz=1
//								return "vadd" + (cond!=condAlways?condString[cond]:"") + ".f64 D" + DVd + ", D" + n + ", D" + op0_4;
//							}
//							else if (op8_1==0x0) {	// sz=0
//								return "vadd" + (cond!=condAlways?condString[cond]:"") + ".f32 S" + VdD + ", S" + n + ", S" + op0_4;
//							}
//						}
//						if ( ((op20_4 & 0xb)==0x3) && (op6_1==0x1) ) {	// Vector Subtract p.1086	Encoding = A2
//							if (op8_1==0x1) {	// sz=1
//								return "vsub" + (cond!=condAlways?condString[cond]:"") + ".f64 D" + DVd + ", D" + n + ", D" + op0_4;
//							}
//							else if (op8_1==0x0) {	// sz=0
//								return "vsub" + (cond!=condAlways?condString[cond]:"") + ".f32 S" + VdD + ", S" + n + ", S" + op0_4;
//							}
//						}
//						if ( ((op20_4 & 0xb)==0x8) && (op6_1==0x0) ) {	// Vector Divide p.882
//							if (op8_1==0x1) {	// sz=1
//								return "vdiv" + (cond!=condAlways?condString[cond]:"") + ".f64 D" + DVd + ", D" + n + ", D" + op0_4;
//							}
//							else if (op8_1==0x0) {	// sz=0
//								return "vdiv" + (cond!=condAlways?condString[cond]:"") + ".f32 S" + VdD + ", S" + n + ", S" + op0_4;
//							}
//						}
//						if ((op20_4 & 0xb)==0x9) {	// Vector Fused Negate Multiply Accumulate or Subtract p.894
//							if (op8_1==0x1) {	// sz=1
//								return "vfnm" + (op6_1==1?"a":"s") + (cond!=condAlways?condString[cond]:"") + ".f64 D" + DVd + ", D" + n + ", D" + op0_4;
//							}
//							else if (op8_1==0x0) {	// sz=0
//								return "vfnm" + (op6_1==1?"a":"s") + (cond!=condAlways?condString[cond]:"") + ".f32 S" + VdD + ", S" + n + ", S" + op0_4;
//							}
//						}
//						if ((op20_4 & 0xb)==0xa) {	// Vector Fused Multiply Accumulate or Subtract p.892	Encoding = A2
//							if (op8_1==0x1) {	// sz=1
//								return "vfm" + (op6_1==1?"a":"s") + (cond!=condAlways?condString[cond]:"") + ".f64 D" + DVd + ", D" + n + ", D" + op0_4;
//							}
//							else if (op8_1==0x0) {	// sz=0
//								return "vfm" + (op6_1==1?"a":"s") + (cond!=condAlways?condString[cond]:"") + ".f32 S" + VdD + ", S" + n + ", S" + op0_4;
//							}
//						}
//
//						if ((op20_4 & 0xb)==0xb) {	// Other Floating-point data-processing instructions A7-17 p.272/273
//							int imm8 = (op16_4 << 4) + op0_4;
//
//							if (op6_1 == 0x0) {	// Vector Move (immediate) p.936	Encoding = A2
//								if (op8_1==0x1) {	// sz=1
//									return "vmov" + (cond!=condAlways?condString[cond]:"") + ".f64 D" + DVd + ", #" + imm8;
//								}
//								else if (op8_1==0x0) {	// sz=0
//									return "vmov" + (cond!=condAlways?condString[cond]:"") + ".f32 S" + VdD + ", #" + imm8;
//								}
//							}
//							if ( (op16_4==0x0) && (op6_2==0x1)) {	// Vector Move (register) p.938	Encoding = A2
//								if (op8_1==0x1) {	// sz=1
//									return "vmov" + (cond!=condAlways?condString[cond]:"") + ".f64 D" + DVd + ", D" + MVm;
//								}
//								else if (op8_1==0x0) {	// sz=0
//									return "vmov" + (cond!=condAlways?condString[cond]:"") + ".f32 S" + VdD + ", S" + VmM;
//								}
//							}
//							if ( (op16_4==0x0) && (op6_2==0x3)) {	// Vector Absolute p.824	Encoding = A2
//								if (op8_1==0x1) {	// sz=1
//									return "vabs" + (cond!=condAlways?condString[cond]:"") + ".f64 D" + DVd + ", D" + MVm;
//								}
//								else if (op8_1==0x0) {	// sz=0
//									return "vabs" + (cond!=condAlways?condString[cond]:"") + ".f32 S" + VdD + ", S" + VmM;
//								}
//							}
//							if ( (op16_4==0x1) && (op6_2==0x1)) {	// Vector Negate p.968	Encoding = A2
//								if (op8_1==0x1) {	// sz=1
//									return "vneg" + (cond!=condAlways?condString[cond]:"") + ".f64 D" + DVd + ", D" + MVm;
//								}
//								else if (op8_1==0x0) {	// sz=0
//									return "vneg" + (cond!=condAlways?condString[cond]:"") + ".f32 S" + VdD + ", S" + VmM;
//								}
//							}
//							if ( (op16_4==0x1) && (op6_2==0x3)) {	// Vector Square Root p.1058
//								if (op8_1==0x1) {	// sz=1
//									return "vsqrt" + (cond!=condAlways?condString[cond]:"") + ".f64 D" + DVd + ", D" + MVm;
//								}
//								else if (op8_1==0x0) {	// sz=0
//									return "vsqrt" + (cond!=condAlways?condString[cond]:"") + ".f32 S" + VdD + ", S" + VmM;
//								}
//							}
//							if ( ((op16_4 & 0xe)==0x2) && (op6_1==0x1)) {	// Vector Convert p.880
//								return "vcvt" + (op7_1==0?"b":"t") + (cond!=condAlways?condString[cond]:"") + (op16_1==0?".f32.f16":".f16.f32") +" S" + VdD + ", S" + VmM;
//							}
//							if ( ((op16_4 & 0xe)==0x4) && (op6_1==0x1)) {	// Vector Compare p.864
//								if (op16_1 == 0x0) {	// Encoding = A1
//									if (op8_1==0x1) {	// sz=1
//										return "vcmp" + (op7_1==1?"e":"") + (cond!=condAlways?condString[cond]:"") + ".f64 D" + DVd + ", D" + MVm;
//									}
//									else if (op8_1==0x0) {	// sz=0
//										return "vcmp" + (op7_1==1?"e":"") + (cond!=condAlways?condString[cond]:"") + ".f32 S" + VdD + ", S" + VmM;
//									}
//								}
//								else if (op16_1 == 0x1) {	// Encoding = A2
//									if (op8_1==0x1) {	// sz=1
//										return "vcmp" + (op7_1==1?"e":"") + (cond!=condAlways?condString[cond]:"") + ".f64 D" + DVd + ", #0.0";
//									}
//									else if (op8_1==0x0) {	// sz=0
//										return "vcmp" + (op7_1==1?"e":"") + (cond!=condAlways?condString[cond]:"") + ".f32 S" + VdD + ", #0.0";
//									}
//								}
//							}
//							if ( (op16_4==0x7) && (op6_2==0x3)) {	// Vector Convert p.876
//								if (op8_1==0x1) {	// sz=1
//									return "vcvt" + (cond!=condAlways?condString[cond]:"") + ".f32.f64 S" + VdD + ", D" + MVm;
//								}
//								else if (op8_1==0x0) {	// sz=0
//									return "vcvt" + (cond!=condAlways?condString[cond]:"") + ".f64.f32 D" + VdD + ", S" + MVm;
//								}
//							}
//							if ( ((op16_4 == 0x8) && (op6_1==0x1)) || (((op16_4 & 0xe)==0xc) && (op6_1==0x1)) ) {	// Vector Convert p.870
//								if ((op16_3 == 0x5) && (op8_1 == 0x1)) {	// Encoded as opc2 = 0b101, sz = 1
//									return "vcvt" + (op7_1==0?"r":"") + (cond!=condAlways?condString[cond]:"") + ".s32.f64 S" + VdD + ", D" + MVm;
//								}
//								if ((op16_3 == 0x5) && (op8_1 == 0x0)) {	// Encoded as opc2 = 0b101, sz = 0
//									return "vcvt" + (op7_1==0?"r":"") + (cond!=condAlways?condString[cond]:"") + ".s32.f32 S" + VdD + ", S" + VmM;
//								}
//								if ((op16_3 == 0x4) && (op8_1 == 0x1)) {	// Encoded as opc2 = 0b100, sz = 1
//									return "vcvt" + (op7_1==0?"r":"") + (cond!=condAlways?condString[cond]:"") + ".u32.f64 S" + VdD + ", D" + MVm;
//								}
//								if ((op16_3 == 0x4) && (op8_1 == 0x0)) {	// Encoded as opc2 = 0b100, sz = 0
//									return "vcvt" + (op7_1==0?"r":"") + (cond!=condAlways?condString[cond]:"") + ".u32.f32 S" + VdD + ", S" + VmM;
//								}
//								if ((op16_3 == 0x0) && (op8_1 == 0x1)) {	// Encoded as opc2 = 0b000, sz = 1
//									return "vcvt" + (cond!=condAlways?condString[cond]:"") + ".f64." + (op7_1==1?"s32":"u32") + " D" + DVd + ", S" + VmM;
//								}
//								if ((op16_3 == 0x0) && (op8_1 == 0x0)) {	// Encoded as opc2 = 0b000, sz = 0
//									return "vcvt" + (cond!=condAlways?condString[cond]:"") + ".f32." + (op7_1==1?"s32":"u32") + " S" + VdD + ", S" + VmM;
//								}
//							}
//							if ( (((op16_4 & 0xe)==0xa) && (op6_1==0x1)) || (((op16_4 & 0xe)==0xe) && (op6_1==0x1)) ) {	// Vector Convert p.874
//								String Td = (op16_1==0x1?"u":"s") + (op7_1==0x1?"32":"16");
//								if ((op18_1 == 0x1) && (op8_1 == 0x1)) {	// Encoded as op = 1, sf = 1
//									return "vcvt" + (cond!=condAlways?condString[cond]:"") + "." + Td + ".f64 D" + DVd + ", D" + DVd + ", #" + "XXXX";
//								}
//								if ((op18_1 == 0x1) && (op8_1 == 0x0)) {	// Encoded as op = 1, sf = 0
//									return "vcvt" + (cond!=condAlways?condString[cond]:"") + "." + Td + ".f32 S" + VdD + ", S" + VdD + ", #" + "XXXX";
//								}
//								if ((op18_1 == 0x0) && (op8_1 == 0x1)) {	// Encoded as op = 0, sf = 1
//									return "vcvt" + (cond!=condAlways?condString[cond]:"") + ".f64." + Td + " D" + DVd + ", D" + DVd + ", #" + "XXXX";
//								}
//								if ((op18_1 == 0x0) && (op8_1 == 0x0)) {	// Encoded as op = 0, sf = 0
//									return "vcvt" + (cond!=condAlways?condString[cond]:"") + ".f32." + Td + " S" + VdD + ", S" + VdD + ", #" + "XXXX";
//								}
//							}						
//						}	// End of: Other Floating-point data-processing instructions A7-17 p.272/273						
//					}	// End of: Floating-point data processing p.272

					if ( ((op20_6 & 0x30)==0x20) && (op4_1==0x1) ) {	// Advanced SIMD, Floating-point p.278
						int VnN = (op16_4<<1) + op7_1;
						if ( (op20_1==0x0) && (op8_1==0x0) && (op21_3==0x0) ) {	// Vector Move p.944
							if (op20_1==0x0) {	// op=0
								return "vmov" + (cond!=condAlways?condString[cond]:"") + " S" + VnN + ", R" + op12_4;
							}
							if (op20_1==0x1) {	// op=1
								return "vmov" + (cond!=condAlways?condString[cond]:"") + " R" + op12_4 + ", S" + VnN;
							}
						}
						if ( (op20_1==0x0) && (op8_1==0x0) && (op21_3==0x7) ) {	// Move to Floating-point Special register from ARM core register p.2016 (p.956)
							String specReg = "";
							switch (op16_4) {
							case 0: specReg = "FPSID";	break;
							case 1: specReg = "FPSCR";	break;
							case 6: specReg = "MVFR1";	break;
							case 7: specReg = "MVFR0";	break;
							case 8: specReg = "FPEXC";	break;
							default: break;
							}
							return "vmsr" + (cond!=condAlways?condString[cond]:"") + " " + specReg + ", R" + op12_4;
						}
						if ( (op20_1==0x0) && (op8_1==0x1) && ((op21_3 & 0x4)==0x0) ) {	// Vector Move p.940
							int DVdx = 0;
							int size = 32;
							if (op22_1==1) {
								size = 8;
								DVdx = (((op7_1<<4) + op16_4)<<3) + (op21_1<<2) + op5_2;
							}
							if (op22_1==0 && op5_1==1) {
								size = 16;
								DVdx = (((op7_1<<4) + op16_4)<<2) + (op21_1<<1) + op6_1;
							}
							if (op22_1==0 && op5_2==0) {
								size = 32;
								DVdx = (((op7_1<<4) + op16_4)<<1) + op21_1;
							}
							return "vmov" + (cond!=condAlways?condString[cond]:"") + "." + size + " D" + DVdx + ", R" + op12_4;
						}
						if ( (op20_1==0x0) && (op8_1==0x0) && ((op21_3 & 0x4)==0x4) && (op6_1==0x0) ) {	// Vector Duplicate p.886
							int DVd2 = (op7_1<<4) + op16_4;
							int size = 32;
							if (op22_1==0 && op6_1==0) {
								size = 32;
							}
							if (op22_1==0 && op6_1==1) {
								size = 16;
							}
							if (op22_1==1 && op6_1==0) {
								size = 8;
							}
							if (op21_1==1) {	// Encoded as Q=1
								return "vdup" + (cond!=condAlways?condString[cond]:"") + "." + size + " Q" + DVd2 + ", R" + op12_4;
							}
							if (op21_1==0) {	// Encoded as Q=0
								return "vdup" + (cond!=condAlways?condString[cond]:"") + "." + size + " D" + DVd2 + ", R" + op12_4;
							}
						}
						if ( (op20_1==0x1) && (op8_1==0x0) && (op21_3==0x0) ) {	// Vector Move p.944
							if (op20_1==0x0) {	// op=0
								return "vmov" + (cond!=condAlways?condString[cond]:"") + " S" + VnN + ", R" + op12_4;
							}
							if (op20_1==0x1) {	// op=1
								return "vmov" + (cond!=condAlways?condString[cond]:"") + " R" + op12_4 + ", S" + VnN;
							}
						}
						if ( (op20_1==0x1) && (op8_1==0x0) && (op21_3==0x7) ) {	// Move to ARM core register from Floating-point Special register p.954
							String specReg = "";
							switch (op16_4) {
							case 0: specReg = "FPSID";	break;
							case 1: specReg = "FPSCR";	break;
							case 6: specReg = "MVFR1";	break;
							case 7: specReg = "MVFR0";	break;
							case 8: specReg = "FPEXC";	break;
							default: break;
							}
							return "vmrs" + (cond!=condAlways?condString[cond]:"") + " R" + op12_4 + ", " + specReg;
						}
//						if ( (op20_1==0x1) && (op8_1==0x1) ) {	// Vector Move p.942
//							int NVnx;
//							if ((op23_1 == 0x0) && (op22_1 == 0x1)) {	// Encoded as U = 0, opc1<1> = 1
//								NVnx = (op7_1<<7) + (op16_4<<3) + (op21_1<<2) + op5_2;
//								return "vmov" + (cond!=condAlways?condString[cond]:"") + ".s8 R" + op12_4 + ", D" + NVnx;
//							}
//							if ((op18_1 == 0x0) && (op8_1 == 0x0) && (op5_1 == 0x1)) {	// Encoded as U = 0, opc1<1> = 0, opc2<0> = 1
//								NVnx = (op7_1<<6) + (op16_4<<2) + (op21_1<<1) + op6_1;
//								return "vmov" + (cond!=condAlways?condString[cond]:"") + ".s8 R" + op12_4 + ", D" + NVnx;
//							}
//							if ((op18_1 == 0x1) && (op8_1 == 0x1)) {	// Encoded as U = 1, opc1<1> = 1
//								NVnx = (op7_1<<7) + (op16_4<<3) + (op21_1<<2) + op5_2;
//								return "vmov" + (cond!=condAlways?condString[cond]:"") + ".s8 R" + op12_4 + ", D" + NVnx;
//							}
//							if ((op18_1 == 0x1) && (op8_1 == 0x0) && (op5_1 == 0x1)) {	// Encoded as U = 1, opc1<1> = 0, opc2<0> = 1
//								NVnx = (op7_1<<6) + (op16_4<<2) + (op21_1<<1) + op6_1;
//								return "vmov" + (cond!=condAlways?condString[cond]:"") + ".s8 R" + op12_4 + ", D" + NVnx;
//							}
//							if ((op18_1 == 0x0) && (op8_1 == 0x0) && (op5_2 == 0x0)) {	// Encoded as U = 0, opc1<1> = 0, opc2 = 0b00
//								NVnx = (op7_1<<5) + (op16_4<<1) + op21_1;
//								return "vmov" + (cond!=condAlways?condString[cond]:"") + ".s8 R" + op12_4 + ", D" + NVnx;
//							}
//						}
					}	// End of: Advanced SIMD, Floating-point p.278
				}	// End of: ((op8_4 & 0xe)==0xa)
			}// End of (op=6/7): Coprocessor instructions, and Supervisor Call p.215
			default: break;
			}	// End of: switch (op)	(p.194)
		}	// End of: (cond != 0xf)	(p.194)
		else if (cond == 0xf) {	// unconditional instructions p.216
			Boolean add = (op23_1 == 0x1);
			if ((op20_8 & 0x80)==0x00) {	//	Memory hints, Advanced SIMD instructions, and miscellaneous instructions p.217/218
				if ( (op20_7==0x10) && ((op4_4 & 0x2)==0x0) && ((op16_4 & 0x1)==0x0) ) {	// Change Processor State p.1980
					String effect = "";
					if ((op7_1==0x1) || (op6_1==0x1)) effect = "IE";
					else effect = "ID";
					return "cps" + effect + (op8_1==1?"a":"") + (op7_1==1?"i":"") + (op6_1==1?"f":"") + ", #" + decodeMode(op0_5);
				}	// End of: Change Processor State p.1980
				
				if ( (op20_7==0x10) && (op4_4==0x0) && ((op16_4 & 0x1)==0x1) ) {	// Set Endianness p.604
					return "setend " + (op9_1==1?"BE":"LE");
				}	// End of: Set Endianness p.604

				if ( (op20_7==0x12) && (op4_4==0x7) ) {	// UNPREDICTABLE
					return "unpredictable";
				}	// End of: UNPREDICTABLE
				

				if ((op20_7 & 0x60)==0x20) {	// Advanced SIMD data-processing instructions p.261
					return "SIMD not implemented";
				}	// End of: Advanced SIMD data-processing instructions p.261
				
				if ((op20_7 & 0x71)==0x40) {	// Advanced SIMD element or structure load/store instructions p.275
					return "SIMD not implemented";
				}	// End of: Advanced SIMD element or structure load/store instructions p.275
				

				if ((op20_7 & 0x77)==0x41) {	// Unallocated memory hint (treat as NOP) p.510
					return "nop";
				}	// End of: Unallocated memory hint (treat as NOP) p.510
				
				if ((op20_7 & 0x77)==0x45) {	// Preload Instruction p.530
					return "pli" + " [R" + op16_4 + ", #" + (add?"+":"-") + op0_12 +"]";
				}	// End of: Preload Instruction p.530

				if ((op20_7 & 0x73)==0x43) {	// UNPREDICTABLE
					return "unpredictable";
				}	// End of: UNPREDICTABLE
				
				if ( ((op20_7 & 0x77)==0x51) && (op16_4!=0xf) ) {	// Preload Data with intent to Write p.524
					return "pld" + (op22_1==0?"w":"") + " [R" + op16_4 + ", #" + (add?"+":"-") + op0_12 +"]";
				}	// End of: Preload Data with intent to Write p.524

				if ( ((op20_7 & 0x77)==0x51) && (op16_4==0xf) ) {	// UNPREDICTABLE
					return "unpredictable";
				}	// End of: UNPREDICTABLE

				if ( ((op20_7 & 0x77)==0x55) && (op16_4!=0xf) ) {	// Preload Data (immediate) p.524
					return "pld" + (op22_1==0?"w":"") + " [R" + op16_4 + ", #" + (add?"+":"-") + op0_12 +"]";
				}	// End of: Preload Data (immediate) p.524

				if ( ((op20_7 & 0x77)==0x55) && (op16_4==0xf) ) {	// Preload Data (literal) p.526
					return "pld" + (op22_1==0?"w":"") + (add?"+":"-") + op0_12;
				}	// End of: Preload Data (literal) p.526

				if (op20_7==0x53) {	// UNPREDICTABLE
					return "unpredictable";
				}	// End of: UNPREDICTABLE

				if ( (op20_7==0x57) && (op4_4==0x0) ) {	// UNPREDICTABLE
					return "unpredictable";
				}	// End of: UNPREDICTABLE

				if ( (op20_7==0x57) && (op4_4==0x1) ) {	// Clear-Exclusive p.360
					return "clrex";
				}	// End of: Clear-Exclusive p.360

				if ( (op20_7==0x57) && ((op16_4 & 0xe)==0x2) ) {	// UNPREDICTABLE
					return "unpredictable";
				}	// End of: UNPREDICTABLE

				if ( (op20_7==0x57) && (op4_4==0x4) ) {	// Data Synchronization Barrier p.380
					return "dsb" + " " + decodeDSBOption(op0_4);
				}	// End of: Data Synchronization Barrier p.380

				if ( (op20_7==0x57) && (op4_4==0x5) ) {	// Data Memory Barrier p.378
					return "dmb" + " " + decodeDSBOption(op0_4);
				}	// End of: Data Memory Barrier p.378

				if ( (op20_7==0x57) && (op4_4==0x6) ) {	// Instruction Synchronization Barrier p.389
					return "isb" + " sy";
				}	// End of: Instruction Synchronization Barrier p.389

				if ( (op20_7==0x57) && (op4_4==0x7) ) {	// UNPREDICTABLE
					return "unpredictable";
				}	// End of: UNPREDICTABLE

				if ( (op20_7==0x57) && ((op16_4 & 0x8)==0x8) ) {	// UNPREDICTABLE
					return "unpredictable";
				}	// End of: UNPREDICTABLE

				if ((op20_7 & 0x7b)==0x5b) {	// UNPREDICTABLE
					return "unpredictable";
				}	// End of: UNPREDICTABLE

				if ( ((op20_7 & 0x77)==0x61) && ((op4_4 & 0x1)==0x0) ) {	// Unallocated memory hint (treat as NOP) p.510
					return "nop";
				}	// End of: Unallocated memory hint (treat as NOP) p.510

				if ( ((op20_7 & 0x77)==0x65) && ((op4_4 & 0x1)==0x0) ) {	// Preload Instruction p.532
					return "pli" + " [R" + op16_4 + ", " + (add?"+":"-") + "R" + op0_4 + ", " + decodeShift(op5_2, op7_5) + "]";
				}	// End of: Preload Instruction p.532

				if ( ((op20_7 & 0x77)==0x71) && ((op4_4 & 0x1)==0x0) ) {	// Preload Data with intent to Write p.528
					return "pld" + (op22_1==0?"w":"") + " [R" + op16_4 + ", " + (add?"+":"-") + "R" + op0_4 + ", " + decodeShift(op5_2, op7_5) + "]";					
				}	// End of: Preload Data with intent to Write p.528

				if ( ((op20_7 & 0x77)==0x75) && ((op4_4 & 0x1)==0x0) ) {	// Preload Data p.528
					return "pld" + (op22_1==0?"w":"") + " [R" + op16_4 + ", " + (add?"+":"-") + "R" + op0_4 + ", " + decodeShift(op5_2, op7_5) + "]";		
				}	// End of: Preload Data p.528

				if ( ((op20_7 & 0x63)==0x63) && ((op4_4 & 0x1)==0x0) ) {	// UNPREDICTABLE
					return "unpredictable";
				}	// End of: UNPREDICTABLE

				if ( ((op20_7 & 0x7f)==0x7f) && ((op4_4 & 0xf)==0xf) ) {	// Permanently UNDEFINED
					return "undefined";
				}	// End of: Permanently UNDEFINED
			}	// End of ((op20_8 & 0x80)==0x00): Memory hints, Advanced SIMD instructions, and miscellaneous instructions p.217/218
			
			
		
			
			if ((op20_8 & 0xe5)==0x84) {	// Store Return State p.2006
				return "srs" + decodeAmode(op24_1, op23_1) + " SP" + (op21_1==1?"!":"") + ", #" + decodeMode(op0_5);
			}	// End of: Store Return State p.2006
			
			if ((op20_8 & 0xe5)==0x81) {	// Return From Exception p.2000
				return "rfe" + decodeAmode(op24_1, op23_1) + " R" + op16_4 + (op21_1==1?"!":"");
			}	// End of: Return From Exception p.2000
			
			if ((op20_8 & 0xe0)==0xa0) {	// Branch with Link and Exchange p.348
				// Encoding A2 (cond = 0xf)
				return "blx" + " " + ((op0_25<<7)>>6);	// SignExtend(imm24:H:'0', 32); Even numbers in the range -33554432 to 33554430.
			}	// End of: Branch with Link and Exchange p.348
			
			if ( ((op20_8 & 0xe1)==0xc0) && ((op20_8 & 0xfb)!=0xc0) ){	// Store Coprocessor p.662
				// Encoding A2 (cond = 0xf)
				if ((op24_1 == 1) && (op21_1 == 0)) {	// Offset. P = 1, W = 0.
					return "stc2" + (op22_1==0x1?"l":"") + " P" + op8_4 + ", CR" + d + ", [R" + n + ", #" + (add?"+":"-") + op0_8 +"]";
				}
				if ((op24_1 == 1) && (op21_1 == 1)) {	// Pre-indexed. P = 1, W = 1.
					return "stc2" + (op22_1==0x1?"l":"") + " P" + op8_4 + ", CR" + d + ", [R" + n + ", #" + (add?"+":"-") + op0_8 +"]!";
				}
				if ((op24_1 == 0) && (op21_1 == 1)) {	// Post-indexed. P = 0, W = 1.
					return "stc2" + (op22_1==0x1?"l":"") + " P" + op8_4 + ", CR" + d + ", [R" + n + "], #" + (add?"+":"-") + op0_8;
				}
				if ((op24_1 == 0) && (op21_1 == 0) && (op23_1 == 1)) {	// Unindexed. P = 0, W = 0, U = 1.
					return "stc2" + (op22_1==0x1?"l":"") + " P" + op8_4 + ", CR" + d + ", [R" + n + "], {" + op0_8 +"}";
				}				
			}	// End of: Store Coprocessor p.662
			
			if ( ((op20_8 & 0xe1)==0xc1) && ((op20_8 & 0xfb)!=0xc1) && (op16_4!=0xf) ) {	// Load Coprocessor (immediate) p.392
				// Encoding A2 (cond = 0xf)
				if ((op24_1 == 1) && (op21_1 == 0)) {	// Offset. P = 1, W = 0.
					return "ldc2" + (op22_1==0x1?"l":"") + (cond!=condAlways?condString[cond]:"") + " P" + op8_4 + ", CR" + d + ", [R" + n + ", #" + (add?"+":"-") + op0_8 +"]";
				}
				if ((op24_1 == 1) && (op21_1 == 1)) {	// Pre-indexed. P = 1, W = 1.
					return "ldc2" + (op22_1==0x1?"l":"") + (cond!=condAlways?condString[cond]:"") + " P" + op8_4 + ", CR" + d + ", [R" + n + ", #" + (add?"+":"-") + op0_8 +"]!";
				}
				if ((op24_1 == 0) && (op21_1 == 1)) {	// Post-indexed. P = 0, W = 1.
					return "ldc2" + (op22_1==0x1?"l":"") + (cond!=condAlways?condString[cond]:"") + " P" + op8_4 + ", CR" + d + ", [R" + n + "], #" + (add?"+":"-") + op0_8;
				}
				if ((op24_1 == 0) && (op21_1 == 0) && (op23_1 == 1)) {	// Unindexed. P = 0, W = 0, U = 1.
					return "ldc2" + (op22_1==0x1?"l":"") + (cond!=condAlways?condString[cond]:"") + " P" + op8_4 + ", CR" + d + ", [R" + n + "], {" + op0_8 +"}";
				}				
			}	// End of: Load Coprocessor (immediate) p.392
			
			if ( ((op20_8 & 0xe1)==0xc1) && ((op20_8 & 0xfb)!=0xc1) && (op16_4==0xf) ) {	// Load Coprocessor (literal) p.394
				// Encoding A2 (cond = 0xf)
				if ((op24_1 == 1) && (op21_1 == 0)) {	// P = 1, W = 0.
					return "ldc2" + (op22_1==0x1?"l":"") + ", CR" + d + (add?"+":"-") + ((op0_8<<24)>>22);
				}
				if ((op24_1 == 0) && (op23_1 == 1) && (op21_1 == 0)) {	// Unindexed. P = 0, U = 1, W = 0.
					return "ldc2" + (op22_1==0x1?"l":"") + " P" + op8_4 + ", CR" + d + ", [R" + n + "], {" + op0_8 + "}";
				}				
			}	// End of: Load Coprocessor (literal) p.394
			
			if (op20_8==0xc8) {	// Move to Coprocessor from two ARM core registers p.478
				// Encoding A2 (cond = 0xf)
				return "mcrr2" + " P" + op8_4 + ", #" + op4_4 + ", R" + op12_4 + ", R" + op16_4 + ", CR" + op0_4;
			}	// End of: Move to Coprocessor from two ARM core registers p.478
			
			if (op20_8==0xc9) {	// Move to two ARM core registers from Coprocessor p.494
				// Encoding A2 (cond = 0xf)
				return "mrrc2" + " P" + op8_4 + ", #" + op4_4 + ", R" + op12_4 + ", R" + op16_4 + ", CR" + op0_4;
			}	// End of: Move to two ARM core registers from Coprocessor p.494
			
			if ( ((op20_8 & 0xf0)==0xe0) && (op4_1==0) ) {	// Coprocessor data operations p.358
				// Encoding A2 (cond = 0xf)
				return "cdp2" + " p" + op8_4 + ", #" + op20_4 + ", CR" + op12_4 + ", CR" + op16_4 + ", CR" + op0_4 + ", #" + op5_3;
			}	// End of: Coprocessor data operations p.358
			
			if ( ((op20_8 & 0xf1)==0xe0) && (op4_1==1) ) {	// Move to Coprocessor from ARM core register p.476
				// Encoding A2 (cond = 0xf)
				return "mcr2" + " p" + op8_4 + ", R" + op21_3 + ", R" + op12_4 + ", CR" + op16_4 + ", CR" + op0_4 + ", #" + op5_3;
			}	// End of: Move to Coprocessor from ARM core register p.476
			
			if ( ((op20_8 & 0xf1)==0xe1) && (op4_1==1) ) {	// Move to ARM core register from Coprocessor p.492
				// Encoding A2 (cond = 0xf)
				return "mrc2" + " p" + op8_4 + ", R" + op21_3 + ", R" + op12_4 + ", CR" + op16_4 + ", CR" + op0_4 + ", #" + op5_3;
			}	// End of: Move to ARM core register from Coprocessor p.492
		}	// End of: unconditional instructions p.216
		return "undefined";
	}	// End of: public String getMnemonic(Integer instr)

//	static {
//	int code = InstructionDecoderARM.getCode("rlwinm  r3, r29, 2, 0, 29");
//	//		System.out.println(InstructionDecoder.getMnemonic(code);
//	}
}
