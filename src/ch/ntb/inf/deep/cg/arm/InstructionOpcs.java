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

interface InstructionOpcs {
	
	
	final int TOifequal = 0x04;
	final int TOifless = 0x10;
	final int TOifgreater = 0x08;
	final int TOifgeU = 0x05;
	final int TOifnequal = 0x18;
	final int TOalways = 0x1f;
	final int CRF0 = 0;
	final int CRF1 = 1;
	final int CRF2 = 2;
	final int CRF3 = 3;
	final int CRF4 = 4;
	final int CRF5 = 5;
	final int CRF6 = 6;
	final int CRF7 = 7;
	final int LT = 0;
	final int GT = 1;
	final int EQ = 2;
	final int XER = 1;
	final int LR = 8;
	final int CTR = 9;
	final int SRR0 = 26;
	final int SRR1 = 27;
	final int EIE = 80;
	final int EID = 81;
	final int NRI = 82;
	
	final int CRF0SO = 3;
	final int CRF0EQ = 2;
	final int CRF0GT = 1;
	final int CRF0LT = 0;
	final int CRF1SO = 7;
	final int CRF1EQ = 6;
	final int CRF1GT = 5;
	final int CRF1LT = 4;
	final int CRF2SO = 11;
	final int CRF2EQ = 10;
	final int CRF2GT = 9;
	final int CRF2LT = 8;
	


	
	
	
	
	
	
// ARM
// /////////////////////////////////////
	final int condEQ = 0;
	final int condNOTEQ = 1;
	final int condCS = 2;
	final int condAlways = 14;
	
	
	
	
	
	
	// Mnemonic extensions
	public static String[] condString = {
		" if equal",			// 0
		" if not equal",		// 1
		" if carry set",		// 2
		" if carry clear",		// 3
		" if negative",			// 4
		" if positive",			// 5
		" if overflow",			// 6
		" if no overflow",		// 7
		" if unsigned higher",	// 8
		" if unsigned lower",	// 9
		" if greater or equal",	// 10 0xa
		" if less",				// 11 0xb
		" if greater",			// 12 0xc
		" if less or equal",	// 13 0xd
		" always",				// 14 0xe
		" reserved"				// 15 0xf
		
		// 
//		"EQ",
//		"NE",
//		"CS",
//		"CC",
//		"MI",
//		"PL",
//		"VS",
//		"VC",
//		"HI",
//		"LS",
//		"GE",
//		"LT",
//		"GT",
//		"LE",
//		"",
//		"reserved"
	};
	
	// Constant shifts A8.4.1 p.291
	public static String[] shiftType = {
		"LSL",	// type==0
		"LSR",	// type==1
		"ASR",	// type==2
		"ROR",	// type==3
		"RRX"	// type==4
	};
	
	public static String[] bankedRegR0 = {
		"R8_usr",	// SYSm<4:3> = 0b00
		"R9_usr",	//		SYSm<2:0> = 0b001
		"R10_usr",
		"R11_usr",
		"R12_usr",
		"SP_usr",
		"LR_usr",
		"UNPREDICTABLE",
		"R8_fiq",	// SYSm<4:3> = 0b01
		"R9_fiq",
		"R10_fiq",
		"R11_fiq",
		"R12_fiq",
		"SP_fiq",
		"LR_fiq",
		"UNPREDICTABLE",
		"LR_irq",	// SYSm<4:3> = 0b10
		"SP_irq",
		"LR_svc",
		"SP_svc",
		"LR_abt",
		"SP_abt",
		"LR_und",
		"SP_und",
		"UNPREDICTABLE",	// SYSm<4:3> = 0b11
		"UNPREDICTABLE",
		"UNPREDICTABLE",
		"UNPREDICTABLE",
		"LR_mon",
		"SP_mon",
		"ELR_hyp",
		"SP_hyp"
	};
	
	public static String[] bankedRegR1 = {
	"UNPREDICTABLE",	// SYSm<4:3> = 0b00
	"UNPREDICTABLE",	//		SYSm<2:0> = 0b001
	"UNPREDICTABLE",
	"UNPREDICTABLE",
	"UNPREDICTABLE",
	"UNPREDICTABLE",
	"UNPREDICTABLE",
	"UNPREDICTABLE",
	"UNPREDICTABLE",	// SYSm<4:3> = 0b01
	"UNPREDICTABLE",
	"UNPREDICTABLE",
	"UNPREDICTABLE",
	"UNPREDICTABLE",
	"UNPREDICTABLE",
	"SPSR_fiq",
	"UNPREDICTABLE",
	"SPSR_irq",			// SYSm<4:3> = 0b10
	"UNPREDICTABLE",
	"SPSR_svc",
	"UNPREDICTABLE",
	"SPSR_abt",
	"UNPREDICTABLE",
	"SPSR_und",
	"UNPREDICTABLE",
	"UNPREDICTABLE",	// SYSm<4:3> = 0b11
	"UNPREDICTABLE",
	"UNPREDICTABLE",
	"UNPREDICTABLE",
	"SPSR_mon",
	"UNPREDICTABLE",
	"SPSR_hyp",
	"UNPREDICTABLE"		
	};
	
	public static String updateAPSR = "s";
	
	public static String spec_reg = "APSR";
	
	public static String[] amode = {
		"DA",
		"IA",
		"DB",
		"IB"
	};
	
	
	final int noShift = 0;
	final int LSL = 0;
	final int LRS = 1;
	final int ASR = 1;
	final int ROR = 3;
	final int RRX = 3;

	final int // ARM Instructions
////		armAdc = (0x0 << 26) | (0x5 << 21),
////		armAnd = (0x0 << 26) | (0x0 << 21),
		armAdd = (0x8 << 20),
		armAdds = (0x9 << 20),
////		armB = (0x12 << 26),
////		armBl = (0x12 << 26) | 1,
////		armBc = (0x10 << 26),
		armRsb = (0x26 << 20),
		armRsbs = (0x27 << 20),
		armSub = (0x24 << 20),
		armSubs = (0x25 << 20);
}