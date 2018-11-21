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

public interface InstructionOpcs {
	
	// Condition codes
	final int condEQ = 0;
	final int condNOTEQ = 1;
	final int condCS = 2;
	final int condCC = 3;
	final int condMI = 4;
	final int condPL = 5;
	final int condVS = 6;
	final int condVC = 7;
	final int condHI = 8;
	final int condLS = 9;
	final int condGE = 10;
	final int condLT = 11;
	final int condGT = 12;
	final int condLE = 13;
	final int condAlways = 14;
	
	// Conditions mnemonics
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
	};
	
	// Constant shift codes
	final int noShift = 0;
	final int LSL = 0;
	final int LSR = 1;
	final int ASR = 1;
	final int ROR = 3;
	final int RRX = 3;

	// Constant shift mnemonics
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
	
	// opcodes
	final int 	
	// createDataProcImm / createDataProcReg / createDataProcRegShiftedReg
	armAdc = (0x5 << 21),
	armAdd = (0x4 << 21),
	armAnd = (0x0 << 21),
	armBic = (0xe << 21),
	armEor = (0x1 << 21),
	armMov = (0xd << 21),
	armMvn = (0xf << 21),	
	armOrr = (0xc << 21),
	armRsb = (0x3 << 21),
	armRsc = (0x7 << 21),
	armSbc = (0x6 << 21),
	armSub = (0x2 << 21),
	armAdcs = armAdc | (0x1 << 20),
	armAdds = armAdd | (0x1 << 20),
	armAnds = armAnd | (0x1 << 20),
	armBics = armBic | (0x1 << 20),
	armEors = armEor | (0x1 << 20),
	armMovs = armMov | (1 << 20),
	armMvns = armMvn | (0x1 << 20),
	armOrrs = armOrr | (0x1 << 20),
	armRsbs = armRsb | (0x1 << 20),
	armRscs = armRsc | (0x1 << 20),
	armSbcs = armSbc | (0x1 << 20),
	armSubs = armSub | (0x1 << 20),	

	armCmn = (0x17 << 20),
	armCmp = (0x15 << 20),
	armTeq = (0x13 << 20),
	armTst = (0x11 << 20),
	
	armAsr = (0xd << 21) | (0x2 << 5),
	armLsl = (0xd << 21) | (0x0 << 5),
	armLsr = (0xd << 21) | (0x1 << 5),
	armRor = (0xd << 21) | (0x3 << 5),
	armRrx = armRor,
	armAsrs = armAsr | (1 << 20),
	armLsls = armLsl | (1 << 20),
	armLsrs = armLsr | (1 << 20),
	armRors = armRor | (1 << 20),
	armRrxs = armRors,
	
	// 16 bit moves
	armMovw = (0x10 << 20),
	armMovt = (0x14 << 20),
	
	// multiply
	armMul = (0 << 21),
	armMla = (1 << 21),
	armMls = (3 << 21),
	armSmull = (6 << 21),
	armUmull = (4 << 21),
	armMuls = armMul | (1 << 20),
	armMlas = armMla | (1 << 20),
	armSmulls = armSmull | (1 << 20),
	armUmulls = armUmull | (1 << 20),

	// branch (immediate)
	armB = (0xa << 24),
	armBl = (0xb << 24),
	
	// branch (register)
	armBlxReg	= 0x012fff30,
	armBx		= 0x012fff10,
	armBxj		= 0x012fff20,

	// extension register load / store
	armVldr = (0xd1 << 20) | (0xa << 8),
	armVstr = (0xd0 << 20) | (0xa << 8),
	armVpop = (0xcbd << 16) | (0xb << 8),
	armVpush = (0xd2d << 16) | (0xb << 8),

	// floating point data processing
	armVadd = (0xe3 << 20) | (0xa0 << 4),
	armVsub = (0xe3 << 20) | (0xa4 << 4),
	armVmul = (0xe2 << 20) | (0xa0 << 4),
	armVdiv = (0xe8 << 20) | (0xa0 << 4),
	armVneg = (0xeb << 20) | (1 << 16) | (0xa4 << 4),
	armVmov = (0xeb << 20) | (0xa4 << 4),
	armVcvt = (0xeb << 20) | (0xac << 4),

	// floating point move between registers
	armVmovDouble = (0xc4 << 20) | (0xb1 << 4),
	armVmovSingle = (0xe0 << 20) | (0xa1 << 4),
	
	// coprocessor
	armMrc = (0xe1 << 20) | (0x1 << 4),
	armMcr = (0xe0 << 20) | (0x1 << 4),

	// Load/store word and unsigned byte
	armLdr   = (0x41 << 20),
	armStr   = (0x40 << 20),
	armLdrb  = (0x45 << 20),
	armStrb  = (0x44 << 20),
	// extra load/store halfword, double word and signed byte
	armLdrsb = (0x01 << 20) | (0xd << 4),
	armLdrh  = (0x01 << 20) | (0xb << 4),
	armLdrsh = (0x01 << 20) | (0xf << 4),
	armStrh  = (0xb << 4),

	// block data transfer
	armLdm   = (0x89 << 20),
	armLdmda = (0x81 << 20),
	armLdmdb = (0x91 << 20),
	armLdmib = (0x99 << 20),
	armStm   = (0x88 << 20),
	armStmda = (0x80 << 20),
	armStmdb = (0x90 << 20),
	armStmib = (0x98 << 20),
	armPop   = (0x8bd << 16),
	armPush  = (0x92d << 16),

			// various
	armSvc = 0x0f000000,
	armClz = (0x16f << 16) | (0xf1 << 4),

	// verified till here
	
	// miscellaneous data processing
	armSbfx = (0x3d << 21) | (0x5 << 4), 
	armUbfx = (0x3f << 21) | (0x5 << 4), 

	//	// divide
	//	armSdiv = (0x71 << 20) | (0xf << 12) | (1 << 4),
	//	
	// createSynchPrimLoad / createSynchPrimStore
	armLdrex  = (0x0 << 21) | (3 << 23) | (1 << 20) | (0xf9f << 0),
	armLdrexb = (0x2 << 21) | (3 << 23) | (1 << 20) | (0xf9f << 0),
	armLdrexd = (0x1 << 21) | (3 << 23) | (1 << 20) | (0xf9f << 0),
	armLdrexh = (0x3 << 21) | (3 << 23) | (1 << 20) | (0xf9f << 0),
	armStrex  = (0x0 << 21) | (3 << 23) | (0xf90 << 0),
	armStrexb = (0x2 << 21) | (3 << 23) | (0xf90 << 0),
	armStrexd = (0x1 << 21) | (3 << 23) | (0xf90 << 0),
	armStrexh = (0x3 << 21) | (3 << 23) | (0xf90 << 0),

	
	// SWP / SWPB
	armSwp  = (0x14 << 30) | (0x9 << 4),
	armSwpb = (0x10 << 30) | (0x9 << 4),
	
	
	// DBG
	armDbg = 0x0320f0f0,
	
	
	// Hints (NOP / SEV / WFE / WFI / YIELD)
	armNop   = 0x0320f000 | 0x0,
	armSev   = 0x0320f000 | 0x4,
	armWfe   = 0x0320f000 | 0x2,
	armWfi   = 0x0320f000 | 0x3,
	armYield = 0x0320f000 | 0x1,
	
	// BKPT / HVC
	armBkpt = (0x12 << 20) | (0x7 << 4),
	armHvc  = (0x14 << 20) | (0x7 << 4),
	

	// ERET
	armEret = 0x0160006e,
	
	// SMC
	armSmc = 0x01600070,
	
	// packing, unpacking, saturation, reversal
	armSxtb = (0x2 << 20) | (0xf << 16) | (0x3 << 5),
	armSxth = (0x3 << 20) | (0xf << 16) | (0x3 << 5), 
	
	// Unconditional instructions
	armRfe = 0x08100a00,
	armSrs = 0x084d0500;
	
}