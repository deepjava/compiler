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

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.cg.Code32;
import ch.ntb.inf.deep.cg.CodeGen;
import ch.ntb.inf.deep.cg.RegAllocator;
import ch.ntb.inf.deep.cg.InstructionDecoder;
import ch.ntb.inf.deep.classItems.*;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.Linker32;
import ch.ntb.inf.deep.ssa.*;
import ch.ntb.inf.deep.ssa.instruction.*;
import ch.ntb.inf.deep.strings.HString;

public class CodeGenARM extends CodeGen implements InstructionOpcs, Registers {

	// maximum nof registers used by this method, used to calculate stack size and for debugging output
	public static int nofNonVolEXTRD, nofNonVolEXTRS, nofVolEXTRD, nofVolEXTRS;

	private static final int arrayLenOffset = 8;	

	public static int idHALT, idENABLE_FLOATS;
	public static int idGETGPR, idGETEXTRD, idGETEXTRS, idGETCPR;
	public static int idPUTGPR, idPUTEXTRD, idPUTEXTRS, idPUTCPR;

	private static int paramOffset;	// // stack offset (in bytes) to parameters stored on the stack
	private static int intfMethStorageOffset;	// // stack offset (in bytes) to auxiliary registers for invokeinterface stored on the stack
	static boolean enFloatsInExc;
	static boolean intfMethStorage;	// used for methods which call "invokeinterface", allocates temporary storage on stack for auxiliary registers

	private static boolean newString;

	public CodeGenARM() {}

	public void init() { 
		Class cls = Configuration.getOS().usClass;
		if (cls == null) {ErrorReporter.reporter.error(630); return;}
		Method m = Configuration.getOS().getSystemMethodByName(cls, "GETGPR"); 
		if (m != null) idGETGPR = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "GETEXTRD"); 
		if (m != null) idGETEXTRD = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "GETEXTRS"); 
		if (m != null) idGETEXTRS = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "GETCPR"); 
		if (m != null) idGETCPR = m.id; else {ErrorReporter.reporter.error(631); return;}
		
		m = Configuration.getOS().getSystemMethodByName(cls, "PUTGPR"); 
		if (m != null) idPUTGPR = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "PUTEXTRD"); 
		if (m != null) idPUTEXTRD = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "PUTEXTRS"); 
		if (m != null) idPUTEXTRS = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "PUTCPR"); 
		if (m != null) idPUTCPR = m.id; else {ErrorReporter.reporter.error(631); return;}
		
		super.init();
	}

	public void translateMethod(Method method) {
		init(method);
		nofNonVolEXTRD = 0; nofNonVolEXTRS = 0;
		nofVolEXTRD = 0; nofVolEXTRS = 0;
		SSA ssa = method.ssa;
		Code32 code = method.machineCode;
		
		if (dbg) StdStreams.vrb.println("build intervals");

		enFloatsInExc = false;
		intfMethStorage = false;
		RegAllocatorARM.regsGPR = regsGPRinitial;
		RegAllocatorARM.regsEXTRD = regsEXTRDinitial;
		RegAllocatorARM.regsEXTRS = regsEXTRSinitial;
		RegAllocatorARM.nofNonVolEXTRD = 0;
		RegAllocatorARM.nofNonVolEXTRS = 0;
		RegAllocatorARM.nofVolEXTRD = 0;
		RegAllocatorARM.nofVolEXTRS = 0;

		RegAllocator.buildIntervals(ssa);
		
		// determine, which parameters go into which register
		if (dbg) StdStreams.vrb.println("assign registers to parameters");
		SSANode b = (SSANode) ssa.cfg.rootNode;
		while (b.next != null) b = (SSANode) b.next;
		SSAValue[] lastExitSet = b.exitSet;
		parseExitSet(lastExitSet, method.maxStackSlots);
		if (dbg) {
			StdStreams.vrb.print("parameter go into register: ");
			for (int n = 0; paramRegNr[n] != -1; n++) StdStreams.vrb.print(paramRegNr[n] + "  "); 
			StdStreams.vrb.println();
		}
		
		if (dbg) StdStreams.vrb.println("allocate registers");
		RegAllocatorARM.assignRegisters();
		if (!RegAllocator.fullRegSetGPR || !RegAllocator.fullRegSetFPR) {	// repeat with a reduced register set
			if (!RegAllocator.fullRegSetGPR) {
				if (dbg) StdStreams.vrb.println("GPR register allocation for method " + method.owner.name + "." + method.name + " was not successful, run again and use stack slots");
				if (RegAllocator.useLongs) RegAllocatorARM.regsGPR = regsGPRinitial & ~(0xf << nonVolStartGPR) & ~((1<<volEndGPR) | (1<<(volEndGPR-1)) | (1<<(volEndGPR-2)));
				else RegAllocatorARM.regsGPR = regsGPRinitial & ~(0xf << nonVolStartGPR);
				if (dbg) StdStreams.vrb.println("regsGPRinitial = 0x" + Integer.toHexString(RegAllocatorARM.regsGPR));
			}
			if (!RegAllocator.fullRegSetFPR) {
				if (dbg) StdStreams.vrb.println("FPR register allocation for method " + method.owner.name + "." + method.name + " was not successful, run again and use stack slots");
				RegAllocatorARM.regsEXTRD = regsEXTRDinitial & ~(0x7 << nonVolStartEXTR);
				RegAllocatorARM.regsEXTRS = regsEXTRSinitial & ~(0x3f << (nonVolStartEXTR*2));
				if (dbg) StdStreams.vrb.println("regsEXTRDinitial = 0x" + Integer.toHexString(RegAllocatorARM.regsEXTRD) + ", regsEXTRSinitial = 0x" + Integer.toHexString(RegAllocatorARM.regsEXTRS));
			}
			RegAllocator.stackSlotSpilledRegs = -1;
			// empty local variable information
			LocalVar[] lvTable = ssa.localVarTab;
			if (lvTable != null) {
				for (int i = 0; i < lvTable.length; i++) {
					LocalVar lv = lvTable[i];
					while (lv != null) {	// locals occupying the same slot are linked by field "next"
						lv.range = null;
						lv.ssaInstrStart = null;
						lv = (LocalVar) lv.next;
					}
				}
			}
			parseExitSet(lastExitSet, method.maxStackSlots);
			if (dbg) {
				StdStreams.vrb.print("parameter go into register: ");
				for (int n = 0; paramRegNr[n] != -1; n++) StdStreams.vrb.print(paramRegNr[n] + "  "); 
				StdStreams.vrb.println();
			}
			RegAllocatorARM.resetRegisters();
			RegAllocatorARM.assignRegisters();
		}

		if (dbg) {
			StdStreams.vrb.println(RegAllocatorARM.joinsToString());
		}
		if (dbg) {
			StdStreams.vrb.print("register usage in method: nofNonVolGPR = " + nofNonVolGPR + ", nofVolGPR = " + nofVolGPR);
			StdStreams.vrb.println(", nofNonVolEXTRD = " + nofNonVolEXTRD + ", nofNonVolEXTRS = " + nofNonVolEXTRS + ", nofVolEXTRD = " + nofVolEXTRD + ", nofVolEXTRS = " + nofVolEXTRS);
			StdStreams.vrb.print("register usage for parameters: nofParamGPR = " + nofParamGPR + ", nofParamFPR = " + nofParamFPR);
			StdStreams.vrb.println(", receive parameters slots on stack = " + recParamSlotsOnStack);
			StdStreams.vrb.println("max. parameter slots for any call in this method = " + callParamSlotsOnStack);
			StdStreams.vrb.print("parameter end at instr no: ");
			for (int n = 0; n < nofParam; n++) 
				if (paramRegEnd[n] != -1) StdStreams.vrb.print(paramRegEnd[n] + "  "); 
			StdStreams.vrb.println();
		}
		int stackSize = 0;
		if ((method.accAndPropFlags & (1 << dpfExcHnd)) != 0) {	// exception
			if (method.name == HString.getRegisteredHString("reset")) {	// reset has no prolog
			} else if (method.name == HString.getRegisteredHString("vectorTable")) {	// vector table has no prolog
			} else if (method.name == HString.getRegisteredHString("superVisorCall")) {	// special treatment for exception handling
				code.iCount = 0;
//				createIrSrAsimm(ppcStwu, stackPtr, stackPtr, -24);
//				createIrSspr(ppcMtspr, EID, 0);	// must be set for further debugger exceptions
//				createIrSrAd(ppcStmw, 28, stackPtr, 4);
//				createIrArSrB(ppcOr, 31, paramStartGPR, paramStartGPR);	// copy exception into nonvolatile
				createBlockDataTransfer(code, armPush, condAlways, 3 << 11);	// store nonvolatiles R11 and R12 which are used within this method
				createDataProcMovReg(code, armMov, condAlways, topGPR, paramStartGPR, noShift, 0);	// copy exception into nonvolatile
//				createBranchImm(code, armB, condAlways, -2);
			} else {
				stackSize = calcStackSizeException(code);
				insertPrologException(code, stackSize);
			}
		} else {
			stackSize = calcStackSize(code);
			insertProlog(code, stackSize);	// builds stack frame and copies parameters
		}
		
		SSANode node = (SSANode)ssa.cfg.rootNode;
		while (node != null) {
			node.codeStartIndex = code.iCount;
			translateSSA(node, method);
			node.codeEndIndex = code.iCount-1;
			node = (SSANode) node.next;
		}
		node = (SSANode)ssa.cfg.rootNode;
		if (dbg) StdStreams.vrb.println("\nresolve local branch targets");
		while (node != null) {	// resolve local branch targets
			if (node.nofInstr > 0) {
				if ((node.instructions[node.nofInstr-1].ssaOpcode == sCbranch) || (node.instructions[node.nofInstr-1].ssaOpcode == sCswitch)) {
					int instr = code.instructions[node.codeEndIndex];
					if (dbg) StdStreams.vrb.println("branch at instruction: " + node.codeEndIndex);
					CFGNode[] successors = node.successors;
					if ((instr & 0x0f000000) == armB) {		
						if (dbg) StdStreams.vrb.println("local branch at instruction: " + node.codeEndIndex);
						if ((instr & 0xffff) != 0) {	// switch
							int nofCases = instr & 0xffff;
							int offset = node.codeEndIndex - 1;
							int val = 1;
							for (int k = nofCases-1; k >= 0; k--) {
								int branchOffset = ((SSANode)successors[k]).codeStartIndex - node.codeEndIndex + val;
								int diff = code.instructions[offset] & 0xffffff;
								code.instructions[offset] = (code.instructions[offset] & 0xff000000) | ((branchOffset - 2) & 0xffffff);
								offset -= diff;
								val += diff;
							}
							int branchOffset = ((SSANode)successors[nofCases]).codeStartIndex - node.codeEndIndex;
							if ((branchOffset >= 0x1000000) || (branchOffset <= 0xff000000)) {ErrorReporter.reporter.error(650); return;}
							code.instructions[node.codeEndIndex] &= 0xff000000;
							code.instructions[node.codeEndIndex] |= (branchOffset - 2) & 0xffffff;
						} else {
							int branchOffset;
							if ((instr & (condAlways << 28)) == condAlways << 28) {	// no choice, take first successor, unless specified by mark bit
								if ((instr & 0x800000) != 0) {	// mark bit
									branchOffset = ((SSANode)successors[1]).codeStartIndex - node.codeEndIndex;
									if (dbg) StdStreams.vrb.println("unconditional branch, mark bit set, choose successor = 1, branchOffset: " + branchOffset);	
								} else branchOffset = ((SSANode)successors[0]).codeStartIndex - node.codeEndIndex;
							} else {	// conditionally branch to second choice
								branchOffset = ((SSANode)successors[1]).codeStartIndex - node.codeEndIndex;
								if (dbg) StdStreams.vrb.println("branchOffset: " + branchOffset);
							}
							if ((branchOffset >= 0x1000000) || (branchOffset <= 0xff000000)) {ErrorReporter.reporter.error(650); return;}
							code.instructions[node.codeEndIndex] &= 0xff000000;
							code.instructions[node.codeEndIndex] |= (branchOffset - 2) & 0xffffff;	// account for pipeline
						}
					}
				} else if (node.instructions[node.nofInstr-1].ssaOpcode == sCreturn) {
					if (node.next != null) {
						int branchOffset = code.iCount - node.codeEndIndex;
						if ((branchOffset >= 0x1000000) || (branchOffset <= 0xff000000)) {ErrorReporter.reporter.error(650); return;}
						code.instructions[node.codeEndIndex] |= (branchOffset - 2) & 0xffffff;	// account for pipeline
					}
				}
			}
			node = (SSANode) node.next;
		}
		if ((method.accAndPropFlags & (1 << dpfExcHnd)) != 0) {	// exception
			if (method.name == HString.getRegisteredHString("reset")) {	// reset needs no epilog
			} else if (method.name == HString.getRegisteredHString("vectorTable")) {	// vector table needs no epilog
			} else if (method.name == HString.getRegisteredHString("superVisorCall")) {	// special treatment for exception handling
				Method m = Method.getCompSpecSubroutine("handleException");
				assert m != null;
//				loadConstantAndFixup(code, 31, m);
//				createIrSspr(ppcMtspr, LR, 31);
//				createIrDrAd(ppcLmw, 28, stackPtr, 4);
//				createIrDrAsimm(ppcAddi, stackPtr, stackPtr, 24);
//				createIBOBILK(ppcBclr, BOalways, 0, false);
				createBlockDataTransfer(code, armPop, condAlways, 3 << 11);	// restore nonvolatiles R11 and R12 which were used within this method
				loadConstantAndFixup(code, scratchReg, m);
				createDataProcMovReg(code, armMov, condAlways, PC, scratchReg, noShift, 0);	
			} else {
				insertEpilogException(code, stackSize);
			}
		} else {
			insertEpilog(code, stackSize);
		}
		if (dbg) {StdStreams.vrb.print(ssa.toString()); StdStreams.vrb.print(code.toString());}
	}

	/**
	 * Go through exit set of last node to check which parameter is used and what type of register 
	 * must be allocated for it.
	 * 
	 * @param exitSet
	 * @param maxStackSlots
	 */
	private static void parseExitSet(SSAValue[] exitSet, int maxStackSlots) {
		nofParamGPR = 0; nofParamFPR = 0;
		nofMoveGPR = 0; nofMoveFPR = 0;
		if (dbg) StdStreams.vrb.print("[");
		for (int i = 0; i < nofParam; i++) {
			int type = paramType[i];
			if (dbg) StdStreams.vrb.print("(" + svNames[type] + ")");
			if (type == tLong) {
				if (exitSet[i+maxStackSlots] != null) {	// if null -> parameter is never used
					RegAllocator.useLongs = true;
					if (dbg) StdStreams.vrb.print("R");
					if (paramHasNonVolReg[i]) {
						int reg = RegAllocatorARM.reserveReg(gpr, true, false);
						int regLong = RegAllocatorARM.reserveReg(gpr, true, false);
						moveGPRsrc[nofMoveGPR] = nofParamGPR;
						moveGPRsrc[nofMoveGPR+1] = nofParamGPR+1;
						moveGPRdst[nofMoveGPR++] = reg;
						moveGPRdst[nofMoveGPR++] = regLong;
						paramRegNr[i] = reg;
						paramRegNr[i+1] = regLong;
						if (dbg) StdStreams.vrb.print(reg + ",R" + regLong);
					} else {
						int reg = paramStartGPR + nofParamGPR;
						if (reg <= paramEndGPR) RegAllocatorARM.reserveReg(gpr, reg, false);
						else {
							reg = RegAllocatorARM.reserveReg(gpr, false, false);
							moveGPRsrc[nofMoveGPR] = nofParamGPR;
							moveGPRdst[nofMoveGPR++] = reg;
						}
						int regLong = paramStartGPR + nofParamGPR + 1;
						if (regLong <= paramEndGPR) RegAllocatorARM.reserveReg(gpr, regLong, false);
						else {
							regLong = RegAllocatorARM.reserveReg(gpr, false, false);
							moveGPRsrc[nofMoveGPR] = nofParamGPR + 1;
							moveGPRdst[nofMoveGPR++] = regLong;
						}
						paramRegNr[i] = reg;
						paramRegNr[i+1] = regLong;
						if (dbg) StdStreams.vrb.print(reg + ",R" + regLong);
					}
				}
				i++;
				nofParamGPR += 2;	// see comment below for else type 
			} else if (type == tFloat) {
				if (exitSet[i+maxStackSlots] != null) {	// if null -> parameter is never used
					if (dbg) StdStreams.vrb.print("S");
					if (paramHasNonVolReg[i]) {
						int reg = RegAllocatorARM.reserveReg(extr, true, true);
						moveFPRsrc[nofMoveFPR] = nofParamFPR;
						moveFPRtype[nofMoveFPR] = true;
						moveFPRdst[nofMoveFPR++] = reg;
						paramRegNr[i] = reg;
						if (dbg) StdStreams.vrb.print(reg);
					} else {
						int reg = (paramStartEXTR + nofParamFPR) * 2;
						if (reg <= paramEndEXTR * 2 + 1) RegAllocatorARM.reserveReg(extr, reg, true);
						else {
							reg = RegAllocatorARM.reserveReg(extr, false, true);
							moveFPRsrc[nofMoveFPR] = nofParamFPR;
							moveFPRtype[nofMoveFPR] = true;
							moveFPRdst[nofMoveFPR++] = reg;
						}
						paramRegNr[i] = reg;
						if (dbg) StdStreams.vrb.print(reg);
					}
				}
				nofParamFPR++;	// see comment below for else type 
			} else if (type == tDouble) {
				if (exitSet[i+maxStackSlots] != null) {	// if null -> parameter is never used
					if (dbg) StdStreams.vrb.print("D");
					if (paramHasNonVolReg[i]) {	// must copy from parameter register to nonvolatile or stack slot
						int reg = RegAllocatorARM.reserveReg(extr, true, false);	// nonvolatile or stack slot
						moveFPRsrc[nofMoveFPR] = nofParamFPR;
						moveFPRtype[nofMoveFPR] = false;
						moveFPRdst[nofMoveFPR++] = reg;
						paramRegNr[i] = reg;
						if (dbg) StdStreams.vrb.print(reg);
					} else {
						int reg = paramStartEXTR + nofParamFPR;
						if (reg <= paramEndEXTR) RegAllocatorARM.reserveReg(extr, reg, false);
						else {
							reg = RegAllocatorARM.reserveReg(extr, false, false);
							moveFPRsrc[nofMoveFPR] = nofParamFPR;
							moveFPRtype[nofMoveFPR] = false;
							moveFPRdst[nofMoveFPR++] = reg;
						}
						paramRegNr[i] = reg;
						if (dbg) StdStreams.vrb.print(reg);
					}
				}
				i++;
				nofParamFPR++;	// see comment below for else type 
				paramRegNr[i] = paramRegNr[i-1];
			} else {
				if (exitSet[i+maxStackSlots] != null) {	// if null -> parameter is never used
					if (dbg) StdStreams.vrb.print("R");
					if (paramHasNonVolReg[i]) {	// must copy from parameter register to nonvolatile or stack slot
						int reg = RegAllocatorARM.reserveReg(gpr, true, false);	// nonvolatile or stack slot
						moveGPRsrc[nofMoveGPR] = nofParamGPR;
						moveGPRdst[nofMoveGPR++] = reg;
						paramRegNr[i] = reg;
						if (dbg) StdStreams.vrb.print(reg);
					} else {
						int reg = paramStartGPR + nofParamGPR;
						if (reg <= paramEndGPR) RegAllocatorARM.reserveReg(gpr, reg, false); // mark as reserved
						else {
							reg = RegAllocatorARM.reserveReg(gpr, false, false);	// volatile, nonvolatile or stack slot
							moveGPRsrc[nofMoveGPR] = nofParamGPR;
							moveGPRdst[nofMoveGPR++] = reg;
						}
						paramRegNr[i] = reg;
						if (dbg) StdStreams.vrb.print(reg);
					}
				}
				nofParamGPR++;	// even if the parameter is not used, the calling method assigns a register and we have to account for this here
			}
			if (i < nofParam - 1) if(dbg) StdStreams.vrb.print(", ");
		}
		int nof = nofParamGPR - (paramEndGPR - paramStartGPR + 1);
		if (nof > 0) recParamSlotsOnStack = nof;
		nof = nofParamFPR - (paramEndEXTR - paramStartEXTR + 1);
		if (nof > 0) recParamSlotsOnStack += nof*2;
		
		if(dbg) StdStreams.vrb.println("]");
	}

	/**
	 * Calculates stack size in bytes. Includes saved nonvolatiles, locals on the stack and parameters passed on the stack.
	 * @return Nof bytes on the stack
	 */
	private static int calcStackSize(Code32 code) {
		int size = 8 + nofNonVolGPR * 4 + nofNonVolEXTRD * 8 + nofNonVolEXTRS * 4;	// includes LR and SP for back trace
		size += callParamSlotsOnStack * 4 + RegAllocator.maxLocVarStackSlots * 4 + (intfMethStorage? 12: 0);	
		assert(nofNonVolEXTRD < 16);
		if (nofNonVolEXTRD >= 16) ErrorReporter.reporter.error(1000);
//		if (enFloatsInExc) size += nonVolStartEXTR * 8 + 8;	// save volatile FPR's and FPSCR
//		int size = 4 + callParamSlotsOnStack * 4 + RegAllocator.maxLocVarStackSlots * 4;
//		int padding = (16 - (size % 16)) % 16;
//		size = size + padding;
		paramOffset = 4;
		code.localVarOffset = paramOffset + callParamSlotsOnStack * 4;
		intfMethStorageOffset = paramOffset + callParamSlotsOnStack * 4 + RegAllocator.maxLocVarStackSlots * 4;
		return size;
	}

	private static int calcStackSizeException(Code32 code) {
//		int size = 28 + nofGPR * 4 + RegAllocator.maxLocVarStackSlots * 4;
//		if (enFloatsInExc) {
//			size += nofNonVolFPR * 8;	// save used nonvolatile EXTR's
//			size += nonVolStartEXTR * 8 + 8;	// save all volatile EXTR's and FPSCR
//		}
		int size = RegAllocator.maxLocVarStackSlots * 4;
//		int padding = (16 - (size % 16)) % 16;
//		size = size + padding;
//		LRoffset = size - 4;
//		XERoffset = LRoffset - 4;
//		CRoffset = XERoffset - 4;
//		CTRoffset = CRoffset - 4;
//		SRR1offset = CTRoffset - 4;
//		SRR0offset = SRR1offset - 4;
//		GPRoffset = SRR0offset - nofGPR * 4;
//		FPRoffset = GPRoffset - nofNonVolFPR * 8;
//		if (enFloatsInExc) FPRoffset -= nonVolStartEXTR * 8 + 8;
		paramOffset = 4;
		code.localVarOffset = paramOffset;
		return size;
	}

	private void translateSSA (SSANode node, Method meth) {
		int stringReg = 0;
		Item stringCharRef = null;
		int dReg = 0, dRegLong = 0, src1Reg = 0, src1RegLong = 0, src2Reg = 0, src2RegLong = 0, src3Reg = 0, src3RegLong = 0;
		Code32 code = meth.machineCode;

		for (int i = 0; i < node.nofInstr; i++) {
			SSAInstruction instr = node.instructions[i];
			SSAValue res = instr.result;
			instr.machineCodeOffset = code.iCount;
			if (node.isCatch && i == 0 && node.loadLocalExc > -1) {	
				if (dbg) StdStreams.vrb.println("enter move register intruction for local 'exception' in catch clause: from R" + paramStartGPR + " to R" + node.instructions[node.loadLocalExc].result.reg);
//				createIrArSrB(ppcOr, node.instructions[node.loadLocalExc].result.reg, paramStartGPR, paramStartGPR);
			}
			
			if (dbg) StdStreams.vrb.println("handle instruction " + instr.toString());
			if (instr.ssaOpcode == sCloadLocal) continue;	
			SSAValue[] opds = instr.getOperands();
			if (instr.ssaOpcode == sCstoreToArray) {	// ref(int), index(must be int), val
				src3Reg = opds[2].reg; 
				src3RegLong = opds[2].regLong;
				if (src3RegLong >= 0x100) {
					if (dbg) StdStreams.vrb.println("opd3 regLong on stack slot for instr: " + instr.toString());
					int slot = src3RegLong & 0xff;
					src3RegLong = volEndGPR;
					createLSWordImm(code, armLdr, condAlways, src3RegLong, stackPtr, code.localVarOffset + 4 * slot, 1, 1, 0);	
				}
				if (src3Reg >= 0x100) {
					if (dbg) StdStreams.vrb.println("opd3 reg on stack slot for instr: " + instr.toString());
					int slot = src3Reg & 0xff;
					if ((opds[2].type == tFloat) || (opds[2].type == tDouble)) {
						src3Reg = nonVolStartEXTR * ((opds[2].type == tFloat)?2:1) + 0;
						createLSExtReg(code, armVldr, condAlways, src3Reg, stackPtr, code.localVarOffset + 4 * slot, (opds[2].type == tFloat));
					} else {
						src3Reg = nonVolStartGPR + 0;
						createLSWordImm(code, armLdr, condAlways, src3Reg, stackPtr, code.localVarOffset + 4 * slot, 1, 1, 0);	
					}
				}			
			}
			int slot1L = 0, slot1 = 0, slot2L = 0, slot2 = 0;
			if (opds != null && opds.length != 0) {
				if (opds.length >= 2) {
					src2Reg = opds[1].reg; 
					src2RegLong = opds[1].regLong;
					if (src2RegLong >= 0x100) {	
						if (dbg) StdStreams.vrb.println("opd2 regLong on stack slot for instr: " + instr.toString());
						slot2L = src2RegLong & 0xff;
						src2RegLong = volEndGPR - 2;
						createLSWordImm(code, armLdr, condAlways, src2RegLong, stackPtr, code.localVarOffset + 4 * slot2L, 1, 1, 0);	
					}
					if (src2Reg >= 0x100) {
						if (dbg) StdStreams.vrb.println("opd2 reg on stack slot for instr: " + instr.toString());
						slot2 = src2Reg & 0xff;
						if ((opds[1].type == tFloat) || (opds[1].type == tDouble)) {
							src2Reg = nonVolStartEXTR * ((opds[1].type == tFloat)?2:1) + 2;
							createLSExtReg(code, armVldr, condAlways, src2Reg, stackPtr, code.localVarOffset + 4 * slot2, (opds[1].type == tFloat));
						} else {
							src2Reg = nonVolStartGPR + 2;
							createLSWordImm(code, armLdr, condAlways, src2Reg, stackPtr, code.localVarOffset + 4 * slot2, 1, 1, 0);	
						}
					}			
				}
				src1Reg = opds[0].reg; 
				src1RegLong = opds[0].regLong;
				if (src1RegLong >= 0x100) {
					if (dbg) StdStreams.vrb.println("opd1 regLong on stack slot for instr: " + instr.toString());
					slot1L = src1RegLong & 0xff;
					src1RegLong = volEndGPR - 1;
					createLSWordImm(code, armLdr, condAlways, src1RegLong, stackPtr, code.localVarOffset + 4 * slot1L, 1, 1, 0);	
				}
				if (src1Reg >= 0x100) {
					if (dbg) StdStreams.vrb.println("opd1 reg on stack slot for instr: " + instr.toString());
					slot1 = src1Reg & 0xff;
					if ((opds[0].type == tFloat) || (opds[0].type == tDouble)) {
						src1Reg = nonVolStartEXTR * ((opds[0].type == tFloat)?2:1) + 1;
						createLSExtReg(code, armVldr, condAlways, src1Reg, stackPtr, code.localVarOffset + 4 * slot1, (opds[0].type == tFloat));
					} else {
						src1Reg = nonVolStartGPR + 1;
						createLSWordImm(code, armLdr, condAlways, src1Reg, stackPtr, code.localVarOffset + 4 * slot1, 1, 1, 0);	
					}
				}
			}
			dRegLong = res.regLong;
			int dRegLongSlot = -1;
			if (dRegLong >= 0x100) {
				if (dbg) StdStreams.vrb.println("result regLong on stack slot for instr: " + instr.toString());
				dRegLongSlot = dRegLong & 0xff;
				dRegLong = volEndGPR;
			}
			dReg = res.reg;
			int dRegSlot = -1;
			if (dReg >= 0x100) {
				if (dbg) StdStreams.vrb.println("result reg on stack slot " + (dReg & 0xff) + " for instr: " + instr.toString());
				dRegSlot = dReg & 0xff;
				if ((res.type == tFloat) || (res.type == tDouble)) dReg = nonVolStartEXTR * ((res.type == tFloat)?2:1) + 0;
				else dReg = nonVolStartGPR + 0;
			}

			int gAux1 = res.regGPR1;
			if (gAux1 >= 0x100) {
				gAux1 = nonVolStartGPR + 3;
			}
			int gAux2 = res.regGPR2;
			if (gAux2 >= 0x100) {
				gAux2 = nonVolStartGPR + 4;
				assert false;
			}
			
			switch (instr.ssaOpcode) { 
			case sCloadConst: {
				if (dReg >= 0) {	// else immediate opd, does not have to be loaded into a register
					switch (res.type & ~(1<<ssaTaFitIntoInt)) {
					case tByte: case tShort: case tInteger:
						int immVal = ((StdConstant)res.constant).valueH;
						loadConstant(code, dReg, immVal);
						break;
					case tRef: case tAbyte: case tAshort: case tAchar: case tAinteger:
					case tAlong: case tAfloat: case tAdouble: case tAboolean: case tAref:
						if (res.constant == null) {// object = null
							loadConstant(code, dReg, 0);
						} else if ((code.ssa.cfg.method.owner.accAndPropFlags & (1<<apfEnum)) != 0 && code.ssa.cfg.method.name.equals(HString.getHString("valueOf"))) {	// special case 
							loadConstantAndFixup(code, dReg, res.constant); // load address of static field "ENUM$VALUES"
							createLSWordImm(code, armLdr, condAlways, dReg, dReg, 0, 1, 1, 0);	// load reference to object on heap
						} else {	// ref to constant string
							loadConstantAndFixup(code, dReg, res.constant);
						} 
						break;
					case tLong:	
						StdConstant constant = (StdConstant)res.constant;
						long immValLong = ((long)(constant.valueH)<<32) | (constant.valueL&0xFFFFFFFFL);
						loadConstant(code, dRegLong, (int)(immValLong >> 32));
						loadConstant(code, dReg, (int)immValLong);
						break;	
					case tFloat: 
						constant = (StdConstant)res.constant;
						if (constant.valueH == 0) {	// 0.0 must be loaded directly as it's not in the cp
							loadConstant(code, scratchReg, 0);
							createFPregMove(code, armVmovSingle, condAlways, dReg, scratchReg, 0, false, true);
						} else if (constant.valueH == 0x3f800000) {	// 1.0
							loadConstant(code, scratchReg, 0x3f800000);
							createFPregMove(code, armVmovSingle, condAlways, dReg, scratchReg, 0, false, true);
						} else if (constant.valueH == 0x40000000) {	// 2.0
							loadConstant(code, scratchReg, 0x40000000);
							createFPregMove(code, armVmovSingle, condAlways, dReg, scratchReg, 0, false, true);
						} else {
							loadConstantAndFixup(code, scratchReg, constant);	// address of constant (in the const area) is loaded
							createLSExtReg(code, armVldr, condAlways, dReg, scratchReg, 0, true);
						}
						break;
					case tDouble:
						constant = (StdConstant)res.constant;
						if (constant.valueH == 0) {	// 0.0 must be loaded directly as it's not in the cp
							loadConstant(code, LR, 0);
							loadConstant(code, scratchReg, 0);
							createFPregMove(code, armVmovDouble, condAlways, dReg, scratchReg, LR, false, false);
						} else if (constant.valueH == 0x3ff00000 && constant.valueL == 0) {	// 1.0
							loadConstant(code, LR, 0x3ff00000);
							loadConstant(code, scratchReg, 0);
							createFPregMove(code, armVmovDouble, condAlways, dReg, scratchReg, LR, false, false);
						} else {
							loadConstantAndFixup(code, scratchReg, constant);	// address of constant (in the const area) is loaded
							createLSExtReg(code, armVldr, condAlways, dReg, scratchReg, 0, false);
						}
						break;
					default:
						ErrorReporter.reporter.error(610);
						assert false : "result of SSA instruction has wrong type";
						return;
					}
				} 
				break;}	// sCloadConst
			case sCloadLocal:
				break;	// sCloadLocal
			case sCloadFromField: {
				int refReg, offset = 0;			
				if (opds == null) {	// getstatic
					refReg = LR;
					Item field = ((NoOpndRef)instr).field;
					loadConstantAndFixup(code, refReg, field);
				} else {	// getfield
					refReg = src1Reg;
					if ((meth.ssa.cfg.method.owner == Type.wktString) &&	// string access needs special treatment
							((MonadicRef)instr).item.name.equals(HString.getRegisteredHString("value"))) {
						createDataProcMovReg(code, armMov, condAlways, dReg, refReg, noShift, 0);	// result contains ref to string
						stringCharRef = ((MonadicRef)instr).item;	// ref to "value"
						break;	
					} else {
						offset = ((MonadicRef)instr).item.offset;
						createDataProcCmpImm(code, armCmp, condAlways, refReg, 0);
						createSvc(code, armSvc, condEQ, 7);
					}
				}
				
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tBoolean: case tByte:
					createLSWordImm(code, armLdrsb, condAlways, dReg, refReg, offset, 1, 1, 0);
					break;
				case tShort: 
					createLSWordImm(code, armLdrsh, condAlways, dReg, refReg, offset, 1, 1, 0);
					break;
				case tChar: 
					createLSWordImm(code, armLdrh, condAlways, dReg, refReg, offset, 1, 1, 0);
					break;
				case tInteger: case tRef: case tAref: case tAboolean:
				case tAchar: case tAfloat: case tAdouble: case tAbyte: 
				case tAshort: case tAinteger: case tAlong:
					createLSWordImm(code, armLdr, condAlways, dReg, refReg, offset, 1, 1, 0);
					break;
				case tLong: 
					createLSWordImm(code, armLdr, condAlways, dReg, refReg, offset, 1, 1, 0);
					createLSWordImm(code, armLdr, condAlways, dRegLong, refReg, offset + 4, 1, 1, 0);
					break;
				case tFloat: 
					createLSExtReg(code, armVldr, condAlways, dReg, refReg, offset, true);
					break;
				case tDouble:
					createLSExtReg(code, armVldr, condAlways, dReg, refReg, offset, false);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCloadFromField
			case sCloadFromArray: {
				int refReg = src1Reg;	// ref to array;
				int indexReg = src2Reg;	// index into array;
				if (meth.ssa.cfg.method.owner == Type.wktString && opds[0].owner instanceof MonadicRef && ((MonadicRef)opds[0].owner).item == stringCharRef) {	// string access needs special treatment
					createDataProcMovReg(code, armLsl, condAlways, scratchReg, refReg, noShift, objectSize);	// read field "count", must be first field
					createDataProcCmpReg(code, armCmp, condAlways, indexReg, scratchReg, noShift, 0);
					createSvc(code, armSvc, condCS, 20);
					switch (res.type & 0x7fffffff) {	// type to read
					case tByte:
						createDataProcMovReg(code, armLsl, condAlways, LR, indexReg, noShift, 1);
						createDataProcImm(code, armAdd, condAlways, dReg, refReg, stringSize - 4);	// add index of field "value" to index
						createLSWordReg(code, armLdrsb, condAlways, dReg, LR, dReg, noShift, 0, 1, 1, 0);
						break;
					case tChar:
						createDataProcMovReg(code, armLsl, condAlways, LR, indexReg, noShift, 1);
						createDataProcImm(code, armAdd, condAlways, dReg, refReg, stringSize - 4);	// add index of field "value" to index
						createLSWordReg(code, armLdrh, condAlways, dReg, LR, dReg, noShift, 0, 1, 1, 0);
						break;
					default:
						ErrorReporter.reporter.error(610);
						assert false : "result of SSA instruction has wrong type";
						return;
					}
				} else {
					createDataProcCmpImm(code, armCmp, condAlways, refReg, 0);
					createSvc(code, armSvc, condEQ, 7);
					createLSWordImm(code, armLdrh, condAlways, scratchReg, refReg, arrayLenOffset, 1, 0, 0);
					createDataProcCmpReg(code, armCmp, condAlways, indexReg, scratchReg, noShift, 0);
					createSvc(code, armSvc, condCS, 20);
					switch (res.type & ~(1<<ssaTaFitIntoInt)) {	// type to read
					case tByte: case tBoolean:
						createDataProcImm(code, armAdd, condAlways, LR, refReg, objectSize);
						createLSWordReg(code, armLdrsb, condAlways, dReg, LR, indexReg, noShift, 0, 1, 1, 0);
						break;
					case tShort: 
						createDataProcImm(code, armAdd, condAlways, LR, refReg, objectSize);
						createDataProcReg(code, armAdd, condAlways, LR, LR, indexReg, LSL, 1);
						createLSWordImm(code, armLdrsh, condAlways, dReg, LR, 0, 1, 1, 0);
						break;
					case tChar: 
						createDataProcImm(code, armAdd, condAlways, LR, refReg, objectSize);
						createDataProcReg(code, armAdd, condAlways, LR, LR, indexReg, LSL, 1);
						createLSWordImm(code, armLdrh, condAlways, dReg, LR, 0, 1, 1, 0);
						break;
					case tInteger: case tRef: case tAref: case tAchar: case tAfloat: 
					case tAdouble: case tAbyte: case tAshort: case tAinteger: case tAlong:
						createDataProcImm(code, armAdd, condAlways, LR, refReg, objectSize);
						createLSWordReg(code, armLdr, condAlways, dReg, LR, indexReg, LSL, 2, 1, 1, 0);
						break;
					case tLong: 
						createDataProcImm(code, armAdd, condAlways, LR, refReg, objectSize);
						createLSWordReg(code, armLdr, condAlways, dReg, LR, indexReg, LSL, 3, 1, 1, 1);
						createLSWordImm(code, armLdr, condAlways, dRegLong, LR, 4, 1, 1, 0);
						break;
					case tFloat: 
						createDataProcReg(code, armAdd, condAlways, LR, refReg, indexReg, LSL, 2);
						createLSExtReg(code, armVldr, condAlways, dReg, LR, objectSize, true);
						break;
					case tDouble:
						createDataProcReg(code, armAdd, condAlways, LR, refReg, indexReg, LSL, 3);
						createLSExtReg(code, armVldr, condAlways, dReg, LR, objectSize, false);
						break;
					default:
						ErrorReporter.reporter.error(610);
						assert false : "result of SSA instruction has wrong type";
						return;
					}
				}
				break;}	// sCloadFromArray
			case sCstoreToField: {
				int valReg, valRegLong, refReg, offset, type = 0;
				if (opds.length == 1) {	// putstatic
					valReg = src1Reg;
					valRegLong = src1RegLong;
					refReg = LR;
					Item item = ((MonadicRef)instr).item;
					if(((Type)item.type).category == 'P')
						type = Type.getPrimitiveTypeIndex(item.type.name.charAt(0));
					else type = tRef; //is a Array or a Object 
					offset = 0;
					loadConstantAndFixup(code, refReg, item);
				} else {	// putfield
					refReg = src1Reg;
					valReg = src2Reg;
					valRegLong = src2RegLong;
					if(((Type)((DyadicRef)instr).field.type).category == 'P')
						type = Type.getPrimitiveTypeIndex(((DyadicRef)instr).field.type.name.charAt(0));
					else type = tRef;//is a Array or a Object 
					offset = ((DyadicRef)instr).field.offset;
					createDataProcCmpImm(code, armCmp, condAlways, refReg, 0);
					createSvc(code, armSvc, condEQ, 7);
				}
				switch (type) {
				case tBoolean: case tByte: 
					createLSWordImm(code, armStrb, condAlways, valReg, refReg, offset, 1, 1, 0);
					break;
				case tShort: case tChar:
					createLSWordImm(code, armStrh, condAlways, valReg, refReg, offset, 1, 1, 0);
					break;
				case tInteger: case tRef: case tAref: case tAboolean:
				case tAchar: case tAfloat: case tAdouble: case tAbyte: 
				case tAshort: case tAinteger: case tAlong:
					createLSWordImm(code, armStr, condAlways, valReg, refReg, offset, 1, 1, 0);
					break;
				case tLong: 
					createLSWordImm(code, armStr, condAlways, valReg, refReg, offset, 1, 1, 0);
					createLSWordImm(code, armStr, condAlways, valRegLong, refReg, offset + 4, 1, 1, 0);
					break;
				case tFloat: 
					createLSExtReg(code, armVstr, condAlways, valReg, refReg, offset, true);
					break;
				case tDouble:
					createLSExtReg(code, armVstr, condAlways, valReg, refReg, offset, false);
					break;
				default:
					ErrorReporter.reporter.error(611);
					assert false : "operand of SSA instruction has wrong type";
					return;
				}
				break;}	// sCstoreToField
			case sCstoreToArray: {
				int refReg = src1Reg;	// ref to array
				int indexReg = src2Reg;	// index into array
				int valReg = src3Reg;	// value to store
				if (meth.ssa.cfg.method.owner == Type.wktString && opds[0].owner instanceof MonadicRef && ((MonadicRef)opds[0].owner).item == stringCharRef) {	// string access needs special treatment
					createDataProcImm(code, armAdd, condAlways, LR, refReg, stringSize - 4);	// add index of field "value" to index
					createDataProcReg(code, armAdd, condAlways, LR, LR, indexReg, LSL, 1);
					createLSWordImm(code, armStrh, condAlways, valReg, LR, 0, 1, 1, 0);
				} else {
					createDataProcCmpImm(code, armCmp, condAlways, refReg, 0);
					createSvc(code, armSvc, condEQ, 7);
					createLSWordImm(code, armLdrh, condAlways, scratchReg, refReg, arrayLenOffset, 1, 0, 0);
					createDataProcCmpReg(code, armCmp, condAlways, indexReg, scratchReg, noShift, 0);
					createSvc(code, armSvc, condCS, 20);
					switch (opds[0].type & ~(1<<ssaTaFitIntoInt)) {
					case tAbyte: case tAboolean:
						createDataProcImm(code, armAdd, condAlways, LR, refReg, objectSize);
						createLSWordReg(code, armStrb, condAlways, valReg, LR, indexReg, noShift, 0, 1, 1, 0);
						break;
					case tAshort: case tAchar: 
						createDataProcImm(code, armAdd, condAlways, LR, refReg, objectSize);
						createDataProcReg(code, armAdd, condAlways, LR, LR, indexReg, LSL, 1);
						createLSWordImm(code, armStrh, condAlways, valReg, LR, 0, 1, 1, 0);
						break;
					case tAref: case tRef: case tAinteger:
						createDataProcImm(code, armAdd, condAlways, LR, refReg, objectSize);
						createLSWordReg(code, armStr, condAlways, valReg, LR, indexReg, LSL, 2, 1, 1, 0);
						break;
					case tAlong: 
						createDataProcImm(code, armAdd, condAlways, LR, refReg, objectSize);
						createLSWordReg(code, armStr, condAlways, valReg, LR, indexReg, LSL, 3, 1, 1, 1);
						createLSWordImm(code, armStr, condAlways, src3RegLong, LR, 4, 1, 1, 0);
						break;
					case tAfloat: 
						createDataProcReg(code, armAdd, condAlways, LR, refReg, indexReg, LSL, 2);
						createLSExtReg(code, armVstr, condAlways, valReg, LR, objectSize, true);
						break;
					case tAdouble:
						createDataProcReg(code, armAdd, condAlways, LR, refReg, indexReg, LSL, 3);
						createLSExtReg(code, armVstr, condAlways, valReg, LR, objectSize, false);
						break;
					default:
						ErrorReporter.reporter.error(611);
						assert false : "operand of SSA instruction has wrong type";
						return;
					}
				}
				break;}	// sCstoreToArray
			case sCadd: { 
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tInteger:
					if (src1Reg < 0) {	// imm + reg
						int immVal = ((StdConstant)opds[0].constant).valueH;
						if (immVal >= 0) {
							createDataProcImm(code, armAdd, condAlways, dReg, src2Reg, packImmediate(immVal));
						} else {
							createDataProcImm(code, armSub, condAlways, dReg, src2Reg, packImmediate(-immVal));
						}
					} else if (src2Reg < 0) {	// reg + imm
						int immVal = ((StdConstant)opds[1].constant).valueH;
						if (immVal >= 0) {
							createDataProcImm(code, armAdd, condAlways, dReg, src1Reg, packImmediate(immVal));
						} else {
							createDataProcImm(code, armSub, condAlways, dReg, src1Reg, packImmediate(-immVal));
						}
					} else {	// reg + reg
						createDataProcReg(code, armAdd, condAlways, dReg, src1Reg, src2Reg, noShift, 0);
					}
					break;
				case tLong: 
					createDataProcReg(code, armAdds, condAlways, dReg, src1Reg, src2Reg, noShift, 0);
					createDataProcReg(code, armAdc, condAlways, dRegLong, src1RegLong, src2RegLong, noShift, 0);
					break;
				case tFloat:
					createFPdataProc(code, armVadd, condAlways, dReg, src1Reg, src2Reg, true);
					break;
				case tDouble:
					createFPdataProc(code, armVadd, condAlways, dReg, src1Reg, src2Reg, false);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	//sCadd
			case sCsub: {
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tInteger:
					if (src1Reg < 0) {	// imm - reg
						int immVal = ((StdConstant)opds[0].constant).valueH;
						if (immVal >= 0)
							createDataProcImm(code, armRsb, condAlways, dReg, src2Reg, packImmediate(immVal));
						else {
							createDataProcImm(code, armAdd, condAlways, dReg, src2Reg, packImmediate(-immVal));
							createDataProcImm(code, armRsb, condAlways, dReg, dReg, 0);
						}
					} else if (src2Reg < 0) {	// reg - imm
						int immVal = ((StdConstant)opds[1].constant).valueH;
						if (immVal >= 0)
							createDataProcImm(code, armSub, condAlways, dReg, src1Reg, packImmediate(immVal));
						else
							createDataProcImm(code, armAdd, condAlways, dReg, src1Reg, packImmediate(-immVal));
					} else {	// reg - reg
						createDataProcReg(code, armSub, condAlways, dReg, src1Reg, src2Reg, noShift, 0);
					}
					break;
				case tLong: 
					createDataProcReg(code, armSubs, condAlways, dReg, src1Reg, src2Reg, noShift, 0);
					createDataProcReg(code, armSbc, condAlways, dRegLong, src1RegLong, src2RegLong, noShift, 0);
					break;
				case tFloat: 
					createFPdataProc(code, armVsub, condAlways, dReg, src1Reg, src2Reg, true);
					break;
				case tDouble:
					createFPdataProc(code, armVsub, condAlways, dReg, src1Reg, src2Reg, false);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCsub
			case sCmul: {
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tInteger:
					if (src2Reg < 0) {	// is power of 2
						int immVal = ((StdConstant)opds[1].constant).valueH;
						int shift = 0;
						while (immVal > 1) {shift++; immVal >>= 1;}
						if (shift == 0) createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
						else createDataProcMovReg(code, armLsl, condAlways, dReg, src1Reg, noShift, shift);
					} else {
						createMul(code, armMul, condAlways, dReg, src1Reg, src2Reg);
					}
					break;
				case tLong: 
					if (src2Reg < 0) {	// is power of 2
						long immValLong = ((long)(((StdConstant)opds[1].constant).valueH)<<32) | (((StdConstant)opds[1].constant).valueL&0xFFFFFFFFL);
						int shift = 0;
						while (immValLong > 1) {shift++; immValLong >>= 1;}
						if (shift == 0) {
							createDataProcMovReg(code, armMov, condAlways, dRegLong, src1RegLong, noShift, 0);
							createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
						} else {
							if (shift < 32) {
								createDataProcShiftImm(code, armLsr, condAlways, scratchReg, src1Reg, 32 - shift);
								createDataProcReg(code, armOrr, condAlways, dRegLong, scratchReg, src1RegLong, LSL, shift);
								createDataProcShiftImm(code, armLsl, condAlways, dReg, src1Reg, shift);
							} else if (shift == 32) {
								createDataProcMovReg(code, armMov, condAlways, dRegLong, src1Reg, noShift, 0);
								createMovw(code, armMovw, condAlways, dReg, 0);							
							} else {
								createDataProcShiftImm(code, armLsl, condAlways, dRegLong, src1Reg, shift - 32);
								createMovw(code, armMovw, condAlways, dReg, 0);
							}
						}
					} else {
						createMul(code, armMul, condAlways, scratchReg, src1RegLong, src2Reg);
						createMul(code, armMul, condAlways, LR, src1Reg, src2RegLong);
						createDataProcReg(code, armAdd, condAlways, scratchReg, scratchReg, LR, noShift, 0);
						createMulLong(code, armUmull, condAlways, LR, dReg, src1Reg, src2Reg);
						createDataProcReg(code, armAdd, condAlways, dRegLong, scratchReg, LR, noShift, 0);
					}
					break;
				case tFloat: 
					createFPdataProc(code, armVmul, condAlways, dReg, src1Reg, src2Reg, true);
					break;
				case tDouble:
					createFPdataProc(code, armVmul, condAlways, dReg, src1Reg, src2Reg, false);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	//sCmul
			case sCdiv: {
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tByte: case tShort: case tInteger:
					if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;	
						if (immVal == 0) createSvc(code, armSvc, condAlways, 7);
						else if (RegAllocatorARM.isPowerOf2(Math.abs(immVal))) {	// is power of 2
							int shift = Integer.numberOfTrailingZeros(Math.abs(immVal));
							if (shift == 0) {
								if (immVal < 0) createDataProcImm(code, armRsb, condAlways, dReg, src1Reg, 0);
								else createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
							} else {
								// works for positive and negative dividend
								createDataProcMovReg(code, armAsr, condAlways, LR, src1Reg, noShift, shift - 1);
								createDataProcMovReg(code, armLsr, condAlways, LR, LR, noShift, 32 - shift);
								createDataProcReg(code, armAdd, condAlways, LR, src1Reg, LR, noShift, 0);
								createDataProcMovReg(code, armAsr, condAlways, dReg, LR, noShift, shift);
								if (immVal < 0) createDataProcImm(code, armRsb, condAlways, dReg, dReg, 0);	// negate for negative divisor
							}
						} else {	// A = 2^(32+n) / immVal			// only positive values && not power of 2
							int imm = Math.abs(immVal);
							createDataProcCmpImm(code, armCmp, condAlways, src1Reg, 0);	// is dividend negative?
							createDataProcImm(code, armRsb, condLT, src1Reg, src1Reg, 0);	// negate 
							int val = Integer.highestOneBit(imm);
							loadConstant(code, LR, (int) (0x100000001L * val / imm));
							createMulLong(code, armUmull, condAlways, dReg, LR, src1Reg, LR);
							createDataProcMovReg(code, armAsr, condAlways, dReg, dReg, noShift, Integer.numberOfTrailingZeros(val));
							createDataProcImm(code, armRsb, condLT, dReg, dReg, 0);	// if dividend was negative, negate result
							createDataProcImm(code, armRsb, condLT, src1Reg, src1Reg, 0);	// restore dividend 
							if (immVal < 0) createDataProcImm(code, armRsb, condAlways, dReg, dReg, 0);
						}
					} else {
						createDataProcMovReg(code, armMovs, condAlways, gAux1, src2Reg, noShift, 0);
						createSvc(code, armSvc, condEQ, 7);	// check for divide by zero
						createDataProcImm(code, armRsb, condLT, gAux1, gAux1, 0);	// negate divisor
						createDataProcMovReg(code, armMovs, condAlways, scratchReg, src1Reg, noShift, 0);
						createDataProcImm(code, armRsb, condLT, scratchReg, scratchReg, 0);	// negate dividend
						// shift divisor until it is bigger than dividend
						createClz(code, armClz, condAlways, LR, scratchReg);
						createClz(code, armClz, condAlways, dReg, gAux1);
						createDataProcReg(code, armSubs, condAlways, dReg, dReg, LR, noShift, 0);
						createDataProcShiftReg(code, armLsl, condGT, gAux1, gAux1, dReg);
						loadConstant(code, LR, 1); // set bit 0 in aux register, which will be shifted left then right
						createDataProcShiftReg(code, armLsl, condGT, LR, LR, dReg);
						loadConstant(code, dReg, 0);	// clear dReg to accumulate result
						// loop
						createDataProcCmpReg(code, armCmp, condAlways, scratchReg, gAux1, noShift, 0);
						createDataProcReg(code, armSub, condCS, scratchReg, scratchReg, gAux1, noShift, 0);
						createDataProcReg(code, armAdd, condCS, dReg, dReg, LR, noShift, 0);
						createDataProcMovReg(code, armMovs, condAlways, LR, LR, LSR, 1);
						createDataProcMovReg(code, armMov, condCC, gAux1, gAux1, LSR, 1);
						createBranchImm(code, armB, condCC, -7);
						createDataProcReg(code, armEors, condAlways, scratchReg, src1Reg, src2Reg, noShift, 0);
						createDataProcImm(code, armRsb, condLT, dReg, dReg, 0);	// negate if dividend and divisor have opposite sign
					}
					break;
				case tLong: 
					if (src2Reg < 0) {	// is power of 2
						long immVal = ((long)(((StdConstant)opds[1].constant).valueH)<<32) | (((StdConstant)opds[1].constant).valueL&0xFFFFFFFFL);
						int shift = Long.numberOfTrailingZeros(immVal);
						if (shift == 0) {
							createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
							createDataProcMovReg(code, armMov, condAlways, dRegLong, src1RegLong, noShift, 0);
						} else if (shift < 32) {
							int sh1 = shift - 1;	// shift right arithmetic immediate by shift-1
							if (sh1 == 0) {			// sh1 can be 0
								createDataProcMovReg(code, armMov, condAlways, scratchReg, src1Reg, noShift, 0);
								createDataProcMovReg(code, armMov, condAlways, LR, src1RegLong, noShift, 0);
							} else {
								createDataProcShiftImm(code, armLsl, condAlways, scratchReg, src1RegLong, 32 - sh1);
								createDataProcReg(code, armOrr, condAlways, scratchReg, scratchReg, src1Reg, ASR, sh1);
								createDataProcShiftImm(code, armAsr, condAlways, LR, src1RegLong, sh1);								
							}
							createDataProcShiftImm(code, armLsr, condAlways, scratchReg, LR, 32 - shift);		// shift right immediate by 64-shift
							createMovw(code, armMovw, condAlways, LR, 0);
							createDataProcReg(code, armAdds, condAlways, scratchReg, src1Reg, scratchReg, noShift, 0);	// add
							createDataProcReg(code, armAdc, condAlways, LR, src1RegLong, LR, noShift, 0);
							createDataProcShiftImm(code, armLsl, condAlways, dReg, LR, 32 - shift);	// shift right arithmetic immediate by shift
							createDataProcReg(code, armOrr, condAlways, dReg, dReg, scratchReg, ASR, shift);
							createDataProcShiftImm(code, armAsr, condAlways, dRegLong, LR, shift);								
						} else if (shift == 32) {
							createDataProcShiftImm(code, armLsl, condAlways, scratchReg, src1RegLong, 1);	// shift right arithmetic immediate by shift-1
							createDataProcReg(code, armOrr, condAlways, scratchReg, scratchReg, src1Reg, ASR, 31);
							createDataProcShiftImm(code, armAsr, condAlways, LR, src1RegLong, 31);								
							createDataProcMovReg(code, armMov, condAlways, scratchReg, LR, noShift, 0);		// shift right immediate by 64-shift
							createMovw(code, armMovw, condAlways, LR, 0);
							createDataProcReg(code, armAdds, condAlways, scratchReg, src1Reg, scratchReg, noShift, 0);	// add
							createDataProcReg(code, armAdc, condAlways, LR, src1RegLong, LR, noShift, 0);
							createDataProcMovReg(code, armMov, condAlways, dReg, LR, noShift, 0);	// shift right arithmetic immediate by shift
							createMedia(code, armSbfx, condAlways, dRegLong, dReg, 31, 1);								
						} else {
							createDataProcShiftImm(code, armAsr, condAlways, scratchReg, src1RegLong, shift - 32 - 1);	// shift right arithmetic immediate by shift-1
							createMedia(code, armSbfx, condAlways, LR, scratchReg, 31, 1);							
							createDataProcShiftImm(code, armLsl, condAlways, dReg, LR, 32 - (64 - shift));	// shift right immediate by 64-shift
							createDataProcReg(code, armOrr, condAlways, scratchReg, dReg, scratchReg, LSR, 64 - shift);
							createDataProcShiftImm(code, armLsr, condAlways, LR, LR, 64 - shift);
							createDataProcReg(code, armAdds, condAlways, scratchReg, src1Reg, scratchReg, noShift, 0);	// add
							createDataProcReg(code, armAdc, condAlways, LR, src1RegLong, LR, noShift, 0);				
							createDataProcShiftImm(code, armAsr, condAlways, dReg, LR, shift - 32);	// shift right arithmetic immediate by shift
							createMedia(code, armSbfx, condAlways, dRegLong, dReg, 31, 1);							
						}
					} else { // not a power of 2
						// copy src1 and src2 into registers reserved for getting locals from the stack, if not already fetched from the stack
						if (src1RegLong != volEndGPR - 1) createDataProcMovReg(code, armMov, condAlways, volEndGPR - 1, src1RegLong, noShift, 0);
						if (src1Reg != nonVolStartGPR + 1) createDataProcMovReg(code, armMov, condAlways, nonVolStartGPR + 1, src1Reg, noShift, 0);
						if (src2RegLong != volEndGPR - 2) createDataProcMovReg(code, armMov, condAlways, volEndGPR - 2, src2RegLong, noShift, 0);
						if (src2Reg != nonVolStartGPR + 2) createDataProcMovReg(code, armMov, condAlways, nonVolStartGPR + 2, src2Reg, noShift, 0);
						int src1RegLongCopy = volEndGPR - 1, src1RegCopy = nonVolStartGPR + 1, src2RegLongCopy = volEndGPR - 2, src2RegCopy = nonVolStartGPR + 2;
						int gAux1Copy = nonVolStartGPR + 3;
	
						// check for divide by zero
						createDataProcCmpImm(code, armCmp, condAlways, src2RegLongCopy, 0);
						createDataProcCmpImm(code, armCmp, condEQ, src2RegCopy, 0);
						createSvc(code, armSvc, condEQ, 7);	
						createDataProcReg(code, armEors, condAlways, gAux1Copy, src1RegLongCopy, src2RegLongCopy, noShift, 0);	// determine sign of result
						// negate divisor
						createDataProcCmpImm(code, armCmp, condAlways, src2RegLongCopy, 0);
						createBranchImm(code, armB, condGE, 1);	// omit next 2 instructions if divisor < 0
						createDataProcImm(code, armRsbs, condAlways, src2RegCopy, src2RegCopy, 0);	
						createDataProcImm(code, armRsc, condAlways, src2RegLongCopy, src2RegLongCopy, 0);	
						// negate dividend
						createDataProcCmpImm(code, armCmp, condAlways, src1RegLongCopy, 0);
						createBranchImm(code, armB, condGE, 1);	// omit next 2 instructions if dividend < 0
						createDataProcImm(code, armRsbs, condAlways, src1RegCopy, src1RegCopy, 0);	
						createDataProcImm(code, armRsc, condAlways, src1RegLongCopy, src1RegLongCopy, 0);	
						// shift divisor until it is bigger than dividend
						createClz(code, armClz, condAlways, LR, src1RegLongCopy);
						createDataProcCmpImm(code, armCmp, condAlways, LR, 32);
						createClz(code, armClz, condEQ, scratchReg, src1RegCopy);
						createDataProcReg(code, armAdd, condEQ, LR, scratchReg, LR, noShift, 0);
						createClz(code, armClz, condAlways, dReg, src2RegLongCopy);	// use dReg as temp reg
						createDataProcCmpImm(code, armCmp, condAlways, dReg, 32);
						createClz(code, armClz, condEQ, scratchReg, src2RegCopy);
						createDataProcReg(code, armAdd, condEQ, dReg, scratchReg, dReg, noShift, 0);
						createDataProcReg(code, armSubs, condAlways, dReg, dReg, LR, noShift, 0);	// dReg contains the shift amount, could be negative
						createBranchImm(code, armB, condLT, 7);	// do not shift if negative shift amount				
						createDataProcImm(code, armRsbs, condAlways, LR, dReg, 32);
						createDataProcShiftReg(code, armLsr, condGE, LR, src2RegCopy, LR);
						createDataProcShiftReg(code, armLsl, condGE, scratchReg, src2RegLongCopy, dReg);
						createDataProcReg(code, armOrr, condGE, src2RegLongCopy, LR, scratchReg, noShift, 0);
						createDataProcShiftReg(code, armLsl, condGE, src2RegCopy, src2RegCopy, dReg);
						createDataProcImm(code, armSub, condLT, scratchReg, dReg, 32);
						createDataProcShiftReg(code, armLsl, condLT, src2RegLongCopy, src2RegCopy, scratchReg);
						createDataProcMovImm(code, armMov, condLT, src2RegCopy, 0);
						
						loadConstant(code, LR, 1); // set bit 0 in aux register (LR/scratch), which will be shifted left then right
						loadConstant(code, scratchReg, 0);
						createDataProcCmpImm(code, armCmp, condAlways, dReg, 0); // do not shift if negative shift amount
						createBranchImm(code, armB, condLT, 3);	 				
						createDataProcShiftReg(code, armLsl, condAlways, LR, LR, dReg);
						createDataProcImm(code, armSubs, condAlways, dReg, dReg, 32);
						createDataProcImm(code, armAdd, condGE, scratchReg, scratchReg, 1);
						createDataProcShiftReg(code, armLsl, condGE, scratchReg, scratchReg, dReg);
						loadConstant(code, dRegLong, 0);	// clear dReg to accumulate result
						loadConstant(code, dReg, 0);	
	
						// loop
						createDataProcCmpReg(code, armCmp, condAlways, src1RegLongCopy, src2RegLongCopy, noShift, 0);	// compare dividend and divisor
						createDataProcCmpReg(code, armCmp, condEQ, src1RegCopy, src2RegCopy, noShift, 0);
						createBranchImm(code, armB, condCC, 3);	// omit next 4 instructions if dividend < divisor 				
						createDataProcReg(code, armSubs, condAlways, src1RegCopy, src1RegCopy, src2RegCopy, noShift, 0);
						createDataProcReg(code, armSbc, condAlways, src1RegLongCopy, src1RegLongCopy, src2RegLongCopy, noShift, 0);
						createDataProcReg(code, armAdds, condAlways, dReg, dReg, LR, noShift, 0);
						createDataProcReg(code, armAdc, condAlways, dRegLong, dRegLong, scratchReg, noShift, 0);
						createDataProcMovReg(code, armMovs, condAlways, scratchReg, scratchReg, LSR, 1);	// shift aux register right
						createDataProcRRX(code, armRrxs, condAlways, LR, LR);
						createBranchImm(code, armB, condCS, 2);	// omit next 3 instructions if done 
						createDataProcMovReg(code, armMovs, condAlways, src2RegLongCopy, src2RegLongCopy, LSR, 1);
						createDataProcRRX(code, armRrx, condAlways, src2RegCopy, src2RegCopy);
						createBranchImm(code, armB, condAlways, -14);
						// negate if dividend and divisor have opposite sign
						createDataProcCmpImm(code, armCmp, condAlways, gAux1Copy, 0);
						createBranchImm(code, armB, condGE, 1);	// omit next 2 instructions if same sign
						createDataProcImm(code, armRsbs, condAlways, dReg, dReg, 0);	
						createDataProcImm(code, armRsc, condAlways, dRegLong, dRegLong, 0);	
					}
					break;
				case tFloat:
					createFPdataProc(code, armVdiv, condAlways, dReg, src1Reg, src2Reg, true);
					break;
				case tDouble:
					createFPdataProc(code, armVdiv, condAlways, dReg, src1Reg, src2Reg, false);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCdiv
			case sCrem: {
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tByte: case tShort: case tInteger:
					if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;	
						if (immVal == 0) createSvc(code, armSvc, condEQ, 7);
						else if (RegAllocatorARM.isPowerOf2(Math.abs(immVal))) {	// is power of 2
							int shift = Integer.numberOfTrailingZeros(Math.abs(immVal));
							if (shift == 0) {
								loadConstant(code, dReg, 0);
							} else {
								// works for positive and negative dividend
								createDataProcMovReg(code, armAsr, condAlways, LR, src1Reg, noShift, shift - 1);
								createDataProcMovReg(code, armLsr, condAlways, LR, LR, noShift, 32 - shift);
								createDataProcReg(code, armAdd, condAlways, LR, src1Reg, LR, noShift, 0);
								createDataProcMovReg(code, armAsr, condAlways, scratchReg, LR, noShift, shift);
								if (immVal < 0) createDataProcImm(code, armRsb, condAlways, scratchReg, scratchReg, 0);	// negate for negative divisor
								loadConstant(code, LR, immVal);
								createMul(code, armMul, condAlways, LR, LR, scratchReg);
								createDataProcReg(code, armSub, condAlways, dReg, src1Reg, LR, noShift, 0);								
							}
						} else {	// A = 2^(32+n) / immVal			// only positive values && not power of 2
							int imm = Math.abs(immVal);
							createDataProcCmpImm(code, armCmp, condAlways, src1Reg, 0);	// is dividend negative?
							createDataProcImm(code, armRsb, condLT, src1Reg, src1Reg, 0);	// negate 
							int val = Integer.highestOneBit(imm);
							loadConstant(code, LR, (int) (0x100000001L * val / imm));
							createMulLong(code, armUmull, condAlways, scratchReg, LR, src1Reg, LR);
							createDataProcMovReg(code, armAsr, condAlways, scratchReg, scratchReg, noShift, Integer.numberOfTrailingZeros(val));
							createDataProcImm(code, armRsb, condLT, scratchReg, scratchReg, 0);	// if dividend was negative, negate result
							createDataProcImm(code, armRsb, condLT, src1Reg, src1Reg, 0);	// restore dividend 
							if (immVal < 0) createDataProcImm(code, armRsb, condAlways, scratchReg, scratchReg, 0);
							loadConstant(code, LR, immVal);
							createMul(code, armMul, condAlways, LR, LR, scratchReg);
							createDataProcReg(code, armSub, condAlways, dReg, src1Reg, LR, noShift, 0);
						}
					} else {
						// copy src1 and src2 into registers reserved for getting locals from the stack, if not already fetched from the stack
						int src1RegCopy = nonVolStartGPR + 1, src2RegCopy = nonVolStartGPR + 2, dRegCopy = nonVolStartGPR;
						if (src1Reg != src1RegCopy) createDataProcMovReg(code, armMov, condAlways, src1RegCopy, src1Reg, noShift, 0);
						if (src2Reg != src2RegCopy) createDataProcMovReg(code, armMov, condAlways, src2RegCopy, src2Reg, noShift, 0);

						createDataProcReg(code, armEor, condAlways, scratchReg, src1RegCopy, src2RegCopy, noShift, 0);
						createDataProcCmpImm(code, armCmp, condAlways, src2RegCopy, 0);
						createSvc(code, armSvc, condEQ, 7);	// check for divide by zero
						createDataProcImm(code, armRsb, condLT, src2RegCopy, src2RegCopy, 0);	// negate divisor
						createDataProcCmpImm(code, armCmp, condAlways, src1RegCopy, 0);
						createDataProcImm(code, armRsb, condLT, src1RegCopy, src1RegCopy, 0);	// negate dividend
						// shift divisor until it is bigger than dividend
						createClz(code, armClz, condAlways, LR, src1RegCopy);
						createClz(code, armClz, condAlways, dRegCopy, src2RegCopy);
						createDataProcReg(code, armSubs, condAlways, dRegCopy, dRegCopy, LR, noShift, 0);
						createDataProcShiftReg(code, armLsl, condGT, src2RegCopy, src2RegCopy, dRegCopy);
						loadConstant(code, LR, 1); // set bit 0 in aux register, which will be shifted left then right
						createDataProcShiftReg(code, armLsl, condGT, LR, LR, dRegCopy);
						loadConstant(code, dRegCopy, 0);	// clear dReg to accumulate result
						// loop
						createDataProcCmpReg(code, armCmp, condAlways, src1RegCopy, src2RegCopy, noShift, 0);
						createDataProcReg(code, armSub, condCS, src1RegCopy, src1RegCopy, src2RegCopy, noShift, 0);
						createDataProcReg(code, armAdd, condCS, dRegCopy, dRegCopy, LR, noShift, 0);
						createDataProcMovReg(code, armMovs, condAlways, LR, LR, LSR, 1);
						createDataProcMovReg(code, armMov, condCC, src2RegCopy, src2RegCopy, LSR, 1);
						createBranchImm(code, armB, condCC, -7);
						createDataProcCmpImm(code, armCmp, condAlways, scratchReg, 0);
						createDataProcImm(code, armRsb, condLT, dRegCopy, dRegCopy, 0);	// negate if dividend and divisor have opposite sign
						
						// fetch divisor and dividend again from register or stack
						if (src1Reg != src1RegCopy) createDataProcMovReg(code, armMov, condAlways, src1RegCopy, src1Reg, noShift, 0);
						else createLSWordImm(code, armLdr, condAlways, src1RegCopy, stackPtr, code.localVarOffset + 4 * slot1, 1, 1, 0);
						if (src2Reg != src2RegCopy) createDataProcMovReg(code, armMov, condAlways, src2RegCopy, src2Reg, noShift, 0);
						else createLSWordImm(code, armLdr, condAlways, src2RegCopy, stackPtr, code.localVarOffset + 4 * slot2, 1, 1, 0);
						createMul(code, armMul, condAlways, scratchReg, src2RegCopy, dRegCopy);
						createDataProcReg(code, armSub, condAlways, dReg, src1RegCopy, scratchReg, noShift, 0);
					}
					break;
				case tLong: 
					if (src2Reg < 0) {	// is power of 2
						long immVal = ((long)(((StdConstant)opds[1].constant).valueH)<<32) | (((StdConstant)opds[1].constant).valueL&0xFFFFFFFFL);
						int shift = Long.numberOfTrailingZeros(immVal);
						if (shift == 0) {
							createMovw(code, armMovw, condAlways, dReg, 0);
							createMovw(code, armMovw, condAlways, dRegLong, 0);
						} else if (shift < 32) {
							int sh1 = shift - 1;	// shift right arithmetic immediate by shift-1
							if (sh1 == 0) {			// sh1 can be 0
								createDataProcMovReg(code, armMov, condAlways, scratchReg, src1Reg, noShift, 0);
								createDataProcMovReg(code, armMov, condAlways, LR, src1RegLong, noShift, 0);
							} else {
								createDataProcShiftImm(code, armLsl, condAlways, scratchReg, src1RegLong, 32 - sh1);
								createDataProcReg(code, armOrr, condAlways, scratchReg, scratchReg, src1Reg, ASR, sh1);
								createDataProcShiftImm(code, armAsr, condAlways, LR, src1RegLong, sh1);								
							}
							createDataProcShiftImm(code, armLsr, condAlways, scratchReg, LR, 32 - shift);				// shift right immediate by 64-shift
							createMovw(code, armMovw, condAlways, LR, 0);
							createDataProcReg(code, armAdds, condAlways, scratchReg, src1Reg, scratchReg, noShift, 0);	// add
							createDataProcReg(code, armAdc, condAlways, LR, src1RegLong, LR, noShift, 0);
							createDataProcShiftImm(code, armLsl, condAlways, gAux1, LR, 32 - shift);					// shift right arithmetic immediate by shift
							createDataProcReg(code, armOrr, condAlways, scratchReg, gAux1, scratchReg, ASR, shift); 
							createDataProcShiftImm(code, armAsr, condAlways, LR, LR, shift);							
							createDataProcShiftImm(code, armLsr, condAlways, gAux1, scratchReg, 32 - shift);			// multiply
							createDataProcReg(code, armOrr, condAlways, LR, gAux1, LR, LSL, shift);
							createDataProcShiftImm(code, armLsl, condAlways, scratchReg, scratchReg, shift);
							createDataProcReg(code, armSubs, condAlways, dReg, src1Reg, scratchReg, noShift, 0);		// subtract
							createDataProcReg(code, armSbc, condAlways, dRegLong, src1RegLong, LR, noShift, 0);
						} else if (shift == 32) {
							createDataProcShiftImm(code, armLsl, condAlways, scratchReg, src1RegLong, 1);				// shift right arithmetic immediate by shift-1
							createDataProcReg(code, armOrr, condAlways, scratchReg, scratchReg, src1Reg, ASR, 31);
							createDataProcShiftImm(code, armAsr, condAlways, LR, src1RegLong, 31);								
							createDataProcMovReg(code, armMov, condAlways, scratchReg, LR, noShift, 0);					// shift right immediate by 64-shift
							createMovw(code, armMovw, condAlways, LR, 0);
							createDataProcReg(code, armAdds, condAlways, scratchReg, src1Reg, scratchReg, noShift, 0);	// add
							createDataProcReg(code, armAdc, condAlways, LR, src1RegLong, LR, noShift, 0);
							createDataProcMovReg(code, armMov, condAlways, scratchReg, LR, noShift, 0);					// shift right arithmetic immediate by shift
							createMedia(code, armSbfx, condAlways, LR, scratchReg, 31, 1);								
							createDataProcMovReg(code, armMov, condAlways, LR, scratchReg, noShift, 0);					// multiply
							createMovw(code, armMovw, condAlways, scratchReg, 0);							
							createDataProcReg(code, armSubs, condAlways, dReg, src1Reg, scratchReg, noShift, 0);		// subtract
							createDataProcReg(code, armSbc, condAlways, dRegLong, src1RegLong, LR, noShift, 0);
						} else {
							createDataProcShiftImm(code, armAsr, condAlways, scratchReg, src1RegLong, shift - 32 - 1);	// shift right arithmetic immediate by shift-1
							createMedia(code, armSbfx, condAlways, LR, scratchReg, 31, 1);							
							createDataProcShiftImm(code, armLsl, condAlways, gAux1, LR, 32 - (64 - shift));				// shift right immediate by 64-shift
							createDataProcReg(code, armOrr, condAlways, scratchReg, gAux1, scratchReg, LSR, 64 - shift);
							createDataProcShiftImm(code, armLsr, condAlways, LR, LR, 64 - shift);
							createDataProcReg(code, armAdds, condAlways, scratchReg, src1Reg, scratchReg, noShift, 0);	// add
							createDataProcReg(code, armAdc, condAlways, LR, src1RegLong, LR, noShift, 0);				
							createDataProcShiftImm(code, armAsr, condAlways, scratchReg, LR, shift - 32);				// shift right arithmetic immediate by shift
							createMedia(code, armSbfx, condAlways, LR, scratchReg, 31, 1);							
							createDataProcShiftImm(code, armLsl, condAlways, LR, scratchReg, shift - 32);				// multiply
							createMovw(code, armMovw, condAlways, scratchReg, 0);
							createDataProcReg(code, armSubs, condAlways, dReg, src1Reg, scratchReg, noShift, 0);		// subtract
							createDataProcReg(code, armSbc, condAlways, dRegLong, src1RegLong, LR, noShift, 0);
						}
					} else { // not a power of 2
						// copy src1 and src2 into registers reserved for getting locals from the stack, if not already fetched from the stack
						if (src1RegLong != volEndGPR - 1) createDataProcMovReg(code, armMov, condAlways, volEndGPR - 1, src1RegLong, noShift, 0);
						if (src1Reg != nonVolStartGPR + 1) createDataProcMovReg(code, armMov, condAlways, nonVolStartGPR + 1, src1Reg, noShift, 0);
						if (src2RegLong != volEndGPR - 2) createDataProcMovReg(code, armMov, condAlways, volEndGPR - 2, src2RegLong, noShift, 0);
						if (src2Reg != nonVolStartGPR + 2) createDataProcMovReg(code, armMov, condAlways, nonVolStartGPR + 2, src2Reg, noShift, 0);
						int src1RegLongCopy = volEndGPR - 1, src1RegCopy = nonVolStartGPR + 1, src2RegLongCopy = volEndGPR - 2, src2RegCopy = nonVolStartGPR + 2;
						int gAux1Copy = nonVolStartGPR + 3;
						int dRegLongCopy = volEndGPR, dRegCopy = nonVolStartGPR;
	
						// check for divide by zero
						createDataProcCmpImm(code, armCmp, condAlways, src2RegLongCopy, 0);
						createDataProcCmpImm(code, armCmp, condEQ, src2RegCopy, 0);
						createSvc(code, armSvc, condEQ, 7);	
						createDataProcReg(code, armEors, condAlways, gAux1Copy, src1RegLongCopy, src2RegLongCopy, noShift, 0);	// determine sign of result
						// negate divisor
						createDataProcCmpImm(code, armCmp, condAlways, src2RegLongCopy, 0);
						createBranchImm(code, armB, condGE, 1);	// omit next 2 instructions if divisor < 0
						createDataProcImm(code, armRsbs, condAlways, src2RegCopy, src2RegCopy, 0);	
						createDataProcImm(code, armRsc, condAlways, src2RegLongCopy, src2RegLongCopy, 0);	
						// negate dividend
						createDataProcCmpImm(code, armCmp, condAlways, src1RegLongCopy, 0);
						createBranchImm(code, armB, condGE, 1);	// omit next 2 instructions if dividend < 0
						createDataProcImm(code, armRsbs, condAlways, src1RegCopy, src1RegCopy, 0);	
						createDataProcImm(code, armRsc, condAlways, src1RegLongCopy, src1RegLongCopy, 0);	
						// shift divisor until it is bigger than dividend
						createClz(code, armClz, condAlways, LR, src1RegLongCopy);
						createDataProcCmpImm(code, armCmp, condAlways, LR, 32);
						createClz(code, armClz, condEQ, scratchReg, src1RegCopy);
						createDataProcReg(code, armAdd, condEQ, LR, scratchReg, LR, noShift, 0);
						createClz(code, armClz, condAlways, dRegCopy, src2RegLongCopy);	// use dReg as temp reg
						createDataProcCmpImm(code, armCmp, condAlways, dRegCopy, 32);
						createClz(code, armClz, condEQ, scratchReg, src2RegCopy);
						createDataProcReg(code, armAdd, condEQ, dRegCopy, scratchReg, dRegCopy, noShift, 0);
						createDataProcReg(code, armSubs, condAlways, dRegCopy, dRegCopy, LR, noShift, 0);	// dReg contains the shift amount, could be negative
						createBranchImm(code, armB, condLT, 7);	// do not shift if negative shift amount				
						createDataProcImm(code, armRsbs, condAlways, LR, dRegCopy, 32);
						createDataProcShiftReg(code, armLsr, condGE, LR, src2RegCopy, LR);
						createDataProcShiftReg(code, armLsl, condGE, scratchReg, src2RegLongCopy, dRegCopy);
						createDataProcReg(code, armOrr, condGE, src2RegLongCopy, LR, scratchReg, noShift, 0);
						createDataProcShiftReg(code, armLsl, condGE, src2RegCopy, src2RegCopy, dRegCopy);
						createDataProcImm(code, armSub, condLT, scratchReg, dRegCopy, 32);
						createDataProcShiftReg(code, armLsl, condLT, src2RegLongCopy, src2RegCopy, scratchReg);
						createDataProcMovImm(code, armMov, condLT, src2RegCopy, 0);
						
						loadConstant(code, LR, 1); // set bit 0 in aux register (LR/scratch), which will be shifted left then right
						loadConstant(code, scratchReg, 0);
						createDataProcCmpImm(code, armCmp, condAlways, dRegCopy, 0); // do not shift if negative shift amount
						createBranchImm(code, armB, condLT, 3);	 				
						createDataProcShiftReg(code, armLsl, condAlways, LR, LR, dRegCopy);
						createDataProcImm(code, armSubs, condAlways, dRegCopy, dRegCopy, 32);
						createDataProcImm(code, armAdd, condGE, scratchReg, scratchReg, 1);
						createDataProcShiftReg(code, armLsl, condGE, scratchReg, scratchReg, dRegCopy);
						loadConstant(code, dRegLongCopy, 0);	// clear dReg to accumulate result
						loadConstant(code, dRegCopy, 0);	
	
						// loop
						createDataProcCmpReg(code, armCmp, condAlways, src1RegLongCopy, src2RegLongCopy, noShift, 0);	// compare dividend and divisor
						createDataProcCmpReg(code, armCmp, condEQ, src1RegCopy, src2RegCopy, noShift, 0);
						createBranchImm(code, armB, condCC, 3);	// omit next 4 instructions if dividend < divisor 				
						createDataProcReg(code, armSubs, condAlways, src1RegCopy, src1RegCopy, src2RegCopy, noShift, 0);
						createDataProcReg(code, armSbc, condAlways, src1RegLongCopy, src1RegLongCopy, src2RegLongCopy, noShift, 0);
						createDataProcReg(code, armAdds, condAlways, dRegCopy, dRegCopy, LR, noShift, 0);
						createDataProcReg(code, armAdc, condAlways, dRegLongCopy, dRegLongCopy, scratchReg, noShift, 0);
						createDataProcMovReg(code, armMovs, condAlways, scratchReg, scratchReg, LSR, 1);	// shift aux register right
						createDataProcRRX(code, armRrxs, condAlways, LR, LR);
						createBranchImm(code, armB, condCS, 2);	// omit next 3 instructions if done 
						createDataProcMovReg(code, armMovs, condAlways, src2RegLongCopy, src2RegLongCopy, LSR, 1);
						createDataProcRRX(code, armRrx, condAlways, src2RegCopy, src2RegCopy);
						createBranchImm(code, armB, condAlways, -14);
						// negate if dividend and divisor have opposite sign
						createDataProcCmpImm(code, armCmp, condAlways, gAux1Copy, 0);
						createBranchImm(code, armB, condGE, 1);	// omit next 2 instructions if same sign
						createDataProcImm(code, armRsbs, condAlways, dRegCopy, dRegCopy, 0);	
						createDataProcImm(code, armRsc, condAlways, dRegLongCopy, dRegLongCopy, 0);	
	
						// fetch divisor and dividend again from register or stack
						if (src1RegLong != src1RegLongCopy) createDataProcMovReg(code, armMov, condAlways, src1RegLongCopy, src1RegLong, noShift, 0);
						else createLSWordImm(code, armLdr, condAlways, src1RegLongCopy, stackPtr, code.localVarOffset + 4 * slot1L, 1, 1, 0);
						if (src1Reg != src1RegCopy) createDataProcMovReg(code, armMov, condAlways, src1RegCopy, src1Reg, noShift, 0);
						else createLSWordImm(code, armLdr, condAlways, src1RegCopy, stackPtr, code.localVarOffset + 4 * slot1, 1, 1, 0);
						if (src2RegLong != src2RegLongCopy) createDataProcMovReg(code, armMov, condAlways, src2RegLongCopy, src2RegLong, noShift, 0);
						else createLSWordImm(code, armLdr, condAlways, src2RegLongCopy, stackPtr, code.localVarOffset + 4 * slot2L, 1, 1, 0);
						if (src2Reg != src2RegCopy) createDataProcMovReg(code, armMov, condAlways, src2RegCopy, src2Reg, noShift, 0);
						else createLSWordImm(code, armLdr, condAlways, src2RegCopy, stackPtr, code.localVarOffset + 4 * slot2, 1, 1, 0);
						
						createMul(code, armMul, condAlways, scratchReg, src2RegLongCopy, dRegCopy);
						createMul(code, armMul, condAlways, LR, src2RegCopy, dRegLongCopy);
						createDataProcReg(code, armAdd, condAlways, scratchReg, scratchReg, LR, noShift, 0);
						createMulLong(code, armUmull, condAlways, LR, dRegCopy, src2RegCopy, dRegCopy);
						createDataProcReg(code, armAdd, condAlways, dRegLongCopy, scratchReg, LR, noShift, 0);
	
						createDataProcReg(code, armSubs, condAlways, dReg, src1RegCopy, dRegCopy, noShift, 0);
						createDataProcReg(code, armSbc, condAlways, dRegLong, src1RegLongCopy, dRegLongCopy, noShift, 0);
						break;
					}
				case tFloat: 
					createFPdataProc(code, armVdiv, condAlways, scratchRegEXTR, src1Reg, src2Reg, true);
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, scratchRegEXTR, 5, 1, true);
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, scratchRegEXTR, 0, 1, true);
					createFPdataProc(code, armVmul, condAlways, scratchRegEXTR, scratchRegEXTR, src2Reg, true);
					createFPdataProc(code, armVsub, condAlways, dReg, src1Reg, scratchRegEXTR, true);
					break;
				case tDouble:
					createFPdataProc(code, armVdiv, condAlways, scratchRegEXTR, src1Reg, src2Reg, false);
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, scratchRegEXTR, 5, 1, false);
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, scratchRegEXTR, 0, 1, false);
					createFPdataProc(code, armVmul, condAlways, scratchRegEXTR, scratchRegEXTR, src2Reg, false);
					createFPdataProc(code, armVsub, condAlways, dReg, src1Reg, scratchRegEXTR, false);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCrem
			case sCneg: {
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tInteger:
					createDataProcImm(code, armRsb, condAlways, dReg, src1Reg, 0);
					break;
				case tLong:
					createDataProcImm(code, armRsbs, condAlways, dReg, src1Reg, 0);
					createDataProcImm(code, armRsc, condAlways, dRegLong, src1RegLong, 0);
					break;
				case tFloat:
					createFPdataProc(code, armVneg, condAlways, dReg, src1Reg, true);
					break;
				case tDouble:
					createFPdataProc(code, armVneg, condAlways, dReg, src1Reg, false);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCneg
			case sCshl: {
				int type = res.type & ~(1<<ssaTaFitIntoInt);
				if (type == tInteger) {
					if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH % 32;
						createDataProcShiftImm(code, armLsl, condAlways, dReg, src1Reg, immVal);
					} else {
						createDataProcImm(code, armAnd, condAlways, scratchReg, src2Reg, 0x1f);	// arm takes the lowest 8 bit, whereas java allows only 5 bits
						createDataProcShiftReg(code, armLsl, condAlways, dReg, src1Reg, scratchReg);
					}
				} else if (type == tLong) {
					if (src2Reg < 0) {	
						int immVal = ((StdConstant)opds[1].constant).valueH % 64;
						if (immVal == 0) {
							createDataProcMovReg(code, armMov, condAlways, dRegLong, src1RegLong, noShift, 0);
							createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
						} else if (immVal < 32) {
							createDataProcShiftImm(code, armLsr, condAlways, scratchReg, src1Reg, 32 - immVal);
							createDataProcReg(code, armOrr, condAlways, dRegLong, scratchReg, src1RegLong, LSL, immVal);
							createDataProcShiftImm(code, armLsl, condAlways, dReg, src1Reg, immVal);
						} else if (immVal == 32) {
							createDataProcMovReg(code, armMov, condAlways, dRegLong, src1Reg, noShift, 0);
							createMovw(code, armMovw, condAlways, dReg, 0);							
						} else {
							createDataProcShiftImm(code, armLsl, condAlways, dRegLong, src1Reg, immVal - 32);
							createMovw(code, armMovw, condAlways, dReg, 0);
						}
					} else {  // implemented so as not to use aux register
						createDataProcImm(code, armAnd, condAlways, scratchReg, src2Reg, 0x3f);
						createDataProcImm(code, armRsbs, condAlways, LR, scratchReg, 32);
						createDataProcShiftReg(code, armLsr, condGE, LR, src1Reg, LR);
						createDataProcShiftReg(code, armLsl, condGE, scratchReg, src1RegLong, scratchReg);
						createDataProcReg(code, armOrr, condGE, dRegLong, LR, scratchReg, noShift, 0);
						createDataProcImm(code, armAnd, condAlways, scratchReg, src2Reg, 0x3f);
						createDataProcShiftReg(code, armLsl, condGE, dReg, src1Reg, scratchReg);
						createDataProcImm(code, armSub, condLT, scratchReg, scratchReg, 32);
						createDataProcShiftReg(code, armLsl, condLT, dRegLong, src1Reg, scratchReg);
						createDataProcMovImm(code, armMov, condLT, dReg, 0);
					}
				} else {
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCshl
			case sCshr: {
				int type = res.type & ~(1<<ssaTaFitIntoInt);
				if (type == tInteger) {
					if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH % 32;
						if (immVal == 0) createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
						else createDataProcMovReg(code, armAsr, condAlways, dReg, src1Reg, noShift, immVal);
					} else {
						createDataProcImm(code, armAnd, condAlways, scratchReg, src2Reg, 0x1f);	// arm takes the lowest 8 bit, whereas java allows only 5 bits
						createDataProcShiftReg(code, armAsr, condAlways, dReg, src1Reg, scratchReg);
					}
				} else if (type == tLong) {
					if (src2Reg < 0) {	
						int immVal = ((StdConstant)opds[1].constant).valueH % 64;
						if (immVal == 0) {
							createDataProcMovReg(code, armMov, condAlways, dRegLong, src1RegLong, noShift, 0);
							createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
						} else if (immVal < 32) {
							createDataProcShiftImm(code, armLsl, condAlways, scratchReg, src1RegLong, 32 - immVal);
							createDataProcReg(code, armOrr, condAlways, dReg, scratchReg, src1Reg, ASR, immVal);
							createDataProcShiftImm(code, armAsr, condAlways, dRegLong, src1RegLong, immVal);
						} else if (immVal == 32) {
							createDataProcMovReg(code, armMov, condAlways, dReg, src1RegLong, noShift, 0);
							createMedia(code, armSbfx, condAlways, dRegLong, dReg, 31, 1);							
						} else {
							createDataProcShiftImm(code, armAsr, condAlways, dReg, src1RegLong, immVal - 32);
							createMedia(code, armSbfx, condAlways, dRegLong, dReg, 31, 1);							
						}
					} else {  // implemented so as not to use aux register
						createDataProcImm(code, armAnd, condAlways, scratchReg, src2Reg, 0x3f);
						createDataProcImm(code, armRsbs, condAlways, LR, scratchReg, 32);
						createDataProcShiftReg(code, armLsl, condGE, LR, src1RegLong, LR);
						createDataProcShiftReg(code, armLsr, condGE, scratchReg, src1Reg, scratchReg);
						createDataProcReg(code, armOrr, condGE, dReg, LR, scratchReg, noShift, 0);
						createDataProcImm(code, armAnd, condAlways, scratchReg, src2Reg, 0x3f);
						createDataProcShiftReg(code, armAsr, condGE, dRegLong, src1RegLong, scratchReg);
						createDataProcImm(code, armSub, condLT, scratchReg, scratchReg, 32);
						createDataProcShiftReg(code, armAsr, condLT, dReg, src1RegLong, scratchReg);
						createMedia(code, armSbfx, condLT, dRegLong, dReg, 31, 1);
					}
				} else {
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCshr
			case sCushr: {
				int type = res.type & ~(1<<ssaTaFitIntoInt);
				if (type == tInteger) {
					if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH % 32;
						if (immVal == 0) createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
						else createDataProcMovReg(code, armLsr, condAlways, dReg, src1Reg, noShift, immVal);
					} else {
						createDataProcImm(code, armAnd, condAlways, scratchReg, src2Reg, 0x1f);	// arm takes the lowest 8 bit, whereas java allows only 5 bits
						createDataProcShiftReg(code, armLsr, condAlways, dReg, src1Reg, scratchReg);
					}
				} else if (type == tLong) {
					if (src2Reg < 0) {	
						int immVal = ((StdConstant)opds[1].constant).valueH % 64;
						if (immVal == 0) {
							createDataProcMovReg(code, armMov, condAlways, dRegLong, src1RegLong, noShift, 0);
							createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
						} else if (immVal < 32) {
							createDataProcShiftImm(code, armLsl, condAlways, scratchReg, src1RegLong, 32 - immVal);
							createDataProcReg(code, armOrr, condAlways, dReg, scratchReg, src1Reg, LSR, immVal);
							createDataProcShiftImm(code, armLsr, condAlways, dRegLong, src1RegLong, immVal);
						} else if (immVal == 32) {
							createDataProcMovReg(code, armMov, condAlways, dReg, src1RegLong, noShift, 0);
							createMovw(code, armMovw, condAlways, dRegLong, 0);							
						} else {
							createDataProcShiftImm(code, armLsr, condAlways, dReg, src1RegLong, immVal - 32);
							createMovw(code, armMovw, condAlways, dRegLong, 0);
						}
					} else { // implemented so as not to use aux register
						createDataProcImm(code, armAnd, condAlways, scratchReg, src2Reg, 0x3f);
						createDataProcImm(code, armRsbs, condAlways, LR, scratchReg, 32);
						createDataProcShiftReg(code, armLsl, condGE, LR, src1RegLong, LR);
						createDataProcShiftReg(code, armLsr, condGE, scratchReg, src1Reg, scratchReg);
						createDataProcReg(code, armOrr, condGE, dReg, LR, scratchReg, noShift, 0);
						createDataProcImm(code, armAnd, condAlways, scratchReg, src2Reg, 0x3f);	
						createDataProcShiftReg(code, armLsr, condGE, dRegLong, src1RegLong, scratchReg);
						createDataProcImm(code, armSub, condLT, LR, scratchReg, 32);
						createDataProcShiftReg(code, armLsr, condLT, dReg, src1RegLong, LR);
						createDataProcMovImm(code, armMov, condLT, dRegLong, 0);
					}
				} else {
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCushr
			case sCand: {
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tByte: case tShort: case tInteger:
					if (src1Reg < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						createDataProcImm(code, armAnd, condAlways, dReg, src2Reg, immVal);
					} else if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						createDataProcImm(code, armAnd, condAlways, dReg, src1Reg, immVal);
					} else
						createDataProcReg(code, armAnd, condAlways, dReg, src1Reg, src2Reg, noShift, 0);
					break;
				case tLong:
					if (src1Reg < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueL;
						createDataProcImm(code, armAnd, condAlways, dRegLong, src2RegLong, 0);
						createDataProcImm(code, armAnd, condAlways, dReg, src2Reg, immVal);
					} else if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueL;
						createDataProcImm(code, armAnd, condAlways, dRegLong, src1RegLong, 0);
						createDataProcImm(code, armAnd, condAlways, dReg, src1Reg, immVal);
					} else {
						createDataProcReg(code, armAnd, condAlways, dRegLong, src1RegLong, src2RegLong, noShift, 0);
						createDataProcReg(code, armAnd, condAlways, dReg, src1Reg, src2Reg, noShift, 0);
					}
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCand
			case sCor: {
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tByte: case tShort: case tInteger:
					if (src1Reg < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						createDataProcImm(code, armOrr, condAlways, dReg, src2Reg, immVal);
					} else if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						createDataProcImm(code, armOrr, condAlways, dReg, src1Reg, immVal);
					} else
						createDataProcReg(code, armOrr, condAlways, dReg, src1Reg, src2Reg, noShift, 0);
					break;
				case tLong:
					if (src1Reg < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueL;
						createDataProcImm(code, armOrr, condAlways, dRegLong, src2RegLong, 0);
						createDataProcImm(code, armOrr, condAlways, dReg, src2Reg, immVal);
					} else if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueL;
						createDataProcImm(code, armOrr, condAlways, dRegLong, src1RegLong, 0);
						createDataProcImm(code, armOrr, condAlways, dReg, src1Reg, immVal);
					} else {
						createDataProcReg(code, armOrr, condAlways, dRegLong, src1RegLong, src2RegLong, noShift, 0);
						createDataProcReg(code, armOrr, condAlways, dReg, src1Reg, src2Reg, noShift, 0);
					}
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	//sCor
			case sCxor: {
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tByte: case tShort: case tInteger:
					if (src1Reg < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						createDataProcImm(code, armEor, condAlways, dReg, src2Reg, immVal);
					} else if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						createDataProcImm(code, armEor, condAlways, dReg, src1Reg, immVal);
					} else
						createDataProcReg(code, armEor, condAlways, dReg, src1Reg, src2Reg, noShift, 0);
					break;
				case tLong:
					if (src1Reg < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueL;
						createDataProcImm(code, armEor, condAlways, dRegLong, src2RegLong, 0);
						createDataProcImm(code, armEor, condAlways, dReg, src2Reg, immVal);
					} else if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueL;
						createDataProcImm(code, armEor, condAlways, dRegLong, src1RegLong, 0);
						createDataProcImm(code, armEor, condAlways, dReg, src1Reg, immVal);
					} else {
						createDataProcReg(code, armEor, condAlways, dRegLong, src1RegLong, src2RegLong, noShift, 0);
						createDataProcReg(code, armEor, condAlways, dReg, src1Reg, src2Reg, noShift, 0);
					}
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCxor
			case sCconvInt:	{// int -> other type
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tByte:
					createPacking(code, armSxtb, condAlways, dReg, src1Reg);
					break;
				case tChar: 
					createDataProcReg(code, armMov, condAlways, dReg, 0, src1Reg, LSL, 16);
					createDataProcReg(code, armMov, condAlways, dReg, 0, dReg, LSR, 16);
					break;
				case tShort: 
					createPacking(code, armSxth, condAlways, dReg, src1Reg);
					break;
				case tLong:
					createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
					createMedia(code, armSbfx, condAlways, dRegLong, src1Reg, 31, 1);
					break;
				case tFloat: 
					createFPregMove(code, armVmovSingle, condAlways, scratchRegEXTR, src1Reg, 0, false, true);
					createFPdataConv(code, armVcvt, condAlways, dReg, scratchRegEXTR, 0, 1, true);
					break;
				case tDouble:
					createFPregMove(code, armVmovSingle, condAlways, scratchRegEXTR, src1Reg, 0, false, true);
					createFPdataConv(code, armVcvt, condAlways, dReg, scratchRegEXTR, 0, 1, false);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCconvInt
			case sCconvLong: {	// long -> other type
				switch (res.type & ~(1<<ssaTaFitIntoInt)){
				case tByte:
					createPacking(code, armSxtb, condAlways, dReg, src1Reg);
					break;
				case tChar: 
					createDataProcReg(code, armMov, condAlways, dReg, 0, src1Reg, LSL, 16);
					createDataProcReg(code, armMov, condAlways, dReg, 0, dReg, LSR, 16);
					break;
				case tShort: 
					createPacking(code, armSxth, condAlways, dReg, src1Reg);
					break;
				case tInteger:
					createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
					break;
				case tFloat:
					// cannot use vmla as in long -> double, not enough scratch registers
					createFPregMove(code, armVmovSingle, condAlways, scratchRegEXTR, src1RegLong, 0, false, true);
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, scratchRegEXTR, 0, 1, false);
					Item item = int2floatConst2;	// ref to 2^32;
					loadConstantAndFixup(code, scratchReg, item);
					createLSExtReg(code, armVldr, condAlways, scratchRegEXTR1, scratchReg, 0, false);
					createFPdataProc(code, armVmul, condAlways, scratchRegEXTR1, scratchRegEXTR1, scratchRegEXTR, false);
					createFPregMove(code, armVmovSingle, condAlways, scratchRegEXTR, src1Reg, 0, false, true);
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, scratchRegEXTR, 0, 0, false);
					createFPdataProc(code, armVadd, condAlways, scratchRegEXTR, scratchRegEXTR1, scratchRegEXTR, false);
					createFPdataConvPrec(code, armVcvtp, condAlways, dReg, scratchRegEXTR, true);
					break;
				case tDouble:
					createFPregMove(code, armVmovSingle, condAlways, scratchRegEXTR, src1Reg, 0, false, true);
					createFPdataConv(code, armVcvt, condAlways, dReg, scratchRegEXTR, 0, 0, false);
					createFPregMove(code, armVmovSingle, condAlways, scratchRegEXTR, src1RegLong, 0, false, true);
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, scratchRegEXTR, 0, 1, false);
					item = int2floatConst2;	// ref to 2^32;
					loadConstantAndFixup(code, scratchReg, item);
					createLSExtReg(code, armVldr, condAlways, scratchRegEXTR1, scratchReg, 0, false);
					createFPdataProc(code, armVmla, condAlways, dReg, scratchRegEXTR, scratchRegEXTR1, false);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}
			case sCconvFloat: {	// float -> other type
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tByte:
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, src1Reg, 5, 1, true);
					createFPregMove(code, armVmovSingle, condAlways, scratchRegEXTR, dReg, 0, true, true);
					createPacking(code, armSxtb, condAlways, dReg, dReg);
					break;
				case tChar: 
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, src1Reg, 5, 1, true);
					createFPregMove(code, armVmovSingle, condAlways, scratchRegEXTR, dReg, 0, true, true);
					createDataProcReg(code, armMov, condAlways, dReg, 0, dReg, LSL, 16);
					createDataProcReg(code, armMov, condAlways, dReg, 0, dReg, LSR, 16);
					break;
				case tShort: 
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, src1Reg, 5, 1, true);
					createFPregMove(code, armVmovSingle, condAlways, scratchRegEXTR, dReg, 0, true, true);
					createPacking(code, armSxth, condAlways, dReg, src1Reg);
					break;
				case tInteger:
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, src1Reg, 5, 1, true);
					createFPregMove(code, armVmovSingle, condAlways, scratchRegEXTR, dReg, 0, true, true);
					break;
				case tLong:	
					createFPdataProc(code, armVcmpe, condAlways, src1Reg, 0, true);
					createCoProcFPU(code, armVmrs, condAlways, 15);	// get flags only
					createFPdataProc(code, armVneg, condLT, scratchRegEXTR1, src1Reg, true);	// make positive
					createFPdataProc(code, armVmov, condGE, scratchRegEXTR1, 0, src1Reg, true);	// copy if already positive		
					createFPdataConvPrec(code, armVcvtp, condAlways, scratchRegEXTR1, scratchRegEXTR1, false);	// convert to double
					
					Item item = int2floatConst2;	// ref to 2^32;
					loadConstantAndFixup(code, scratchReg, item);	// address of constant (in the const area) is loaded
					createLSExtReg(code, armVldr, condAlways, scratchRegEXTR, scratchReg, 0, false);
					createFPdataProc(code, armVdiv, condAlways, scratchRegEXTR1, scratchRegEXTR1, scratchRegEXTR, false);
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, scratchRegEXTR1, 5, 1, false);	// F64 -> S32
					createFPregMove(code, armVmovSingle, condAlways, scratchRegEXTR, dRegLong, 0, true, true);
				
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR1, scratchRegEXTR, 0, 1, false);	// S32 -> F64
					createLSExtReg(code, armVldr, condAlways, scratchRegEXTR, scratchReg, 0, false);	// load 2^32 again
					createFPdataProc(code, armVmul, condAlways, scratchRegEXTR1, scratchRegEXTR1, scratchRegEXTR, false);
					createFPdataProc(code, armVneg, condLT, scratchRegEXTR, src1Reg, true);
					createFPdataProc(code, armVmov, condGE, scratchRegEXTR, 0, src1Reg, true);
					createFPdataConvPrec(code, armVcvtp, condAlways, scratchRegEXTR, scratchRegEXTR, false);	// convert to double

					createFPdataProc(code, armVsub, condAlways, scratchRegEXTR1, scratchRegEXTR, scratchRegEXTR1, false);
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, scratchRegEXTR1, 4, 1, false);	// F64 -> U32
					createFPregMove(code, armVmovSingle, condAlways, scratchRegEXTR, dReg, 0, true, true);

					createBranchImm(code, armB, condGE, 1);	// omit next 2 instructions if source was positive
					createDataProcImm(code, armRsbs, condAlways, dReg, dReg, 0);	// negate
					createDataProcImm(code, armRsc, condAlways, dRegLong, dRegLong, 0);
					// largest negative number is smaller than -number by 1
					createDataProcCmpImm(code, armCmp, condAlways, dRegLong, packImmediate(1 << 31));
					createDataProcCmpImm(code, armCmp, condEQ, dReg, packImmediate(1));
					createDataProcImm(code, armSub, condEQ, dReg, dReg, 1);
					break;
				case tDouble:
					createFPdataConvPrec(code, armVcvtp, condAlways, dReg, src1Reg, false);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}
			case sCconvDouble: {	// double -> other type
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tByte:
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, src1Reg, 5, 1, true);
					createFPregMove(code, armVmovSingle, condAlways, scratchRegEXTR, dReg, 0, true, true);
					createPacking(code, armSxtb, condAlways, dReg, dReg);
					break;
				case tChar: 
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, src1Reg, 5, 1, true);
					createFPregMove(code, armVmovSingle, condAlways, scratchRegEXTR, dReg, 0, true, true);
					createDataProcReg(code, armMov, condAlways, dReg, 0, dReg, LSL, 16);
					createDataProcReg(code, armMov, condAlways, dReg, 0, dReg, LSR, 16);
					break;
				case tShort: 
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, src1Reg, 5, 1, true);
					createFPregMove(code, armVmovSingle, condAlways, scratchRegEXTR, dReg, 0, true, true);
					createPacking(code, armSxth, condAlways, dReg, src1Reg);
					break;
				case tInteger:
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, src1Reg, 5, 1, false);
					createFPregMove(code, armVmovSingle, condAlways, scratchRegEXTR, dReg, 0, true, true);
					break;
				case tLong:					
					createFPdataProc(code, armVcmpe, condAlways, src1Reg, 0, false);
					createCoProcFPU(code, armVmrs, condAlways, 15);	// get flags only
					createFPdataProc(code, armVneg, condLT, scratchRegEXTR1, src1Reg, false);	// make positive
					createFPdataProc(code, armVmov, condGE, scratchRegEXTR1, 0, src1Reg, false);	// copy if already positive			
					Item item = int2floatConst2;	// ref to 2^32;
					loadConstantAndFixup(code, scratchReg, item);	// address of constant (in the const area) is loaded
					createLSExtReg(code, armVldr, condAlways, scratchRegEXTR, scratchReg, 0, false);
					createFPdataProc(code, armVdiv, condAlways, scratchRegEXTR1, scratchRegEXTR1, scratchRegEXTR, false);
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, scratchRegEXTR1, 5, 1, false);	// F64 -> S32
					createFPregMove(code, armVmovSingle, condAlways, scratchRegEXTR, dRegLong, 0, true, true);
				
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR1, scratchRegEXTR, 0, 1, false);	// S32 -> F64
					createLSExtReg(code, armVldr, condAlways, scratchRegEXTR, scratchReg, 0, false);	// load 2^32 again
					createFPdataProc(code, armVmul, condAlways, scratchRegEXTR1, scratchRegEXTR1, scratchRegEXTR, false);
					createFPdataProc(code, armVneg, condLT, scratchRegEXTR, src1Reg, false);
					createFPdataProc(code, armVmov, condGE, scratchRegEXTR, 0, src1Reg, false);	
					createFPdataProc(code, armVsub, condAlways, scratchRegEXTR1, scratchRegEXTR, scratchRegEXTR1, false);
					createFPdataConv(code, armVcvt, condAlways, scratchRegEXTR, scratchRegEXTR1, 4, 1, false);	// F64 -> U32
					createFPregMove(code, armVmovSingle, condAlways, scratchRegEXTR, dReg, 0, true, true);

					createBranchImm(code, armB, condGE, 1);	// omit next 2 instructions if source was positive
					createDataProcImm(code, armRsbs, condAlways, dReg, dReg, 0);	// negate
					createDataProcImm(code, armRsc, condAlways, dRegLong, dRegLong, 0);
					// largest negative number is smaller than -number by 1
					createDataProcCmpImm(code, armCmp, condAlways, dRegLong, packImmediate(1 << 31));
					createDataProcCmpImm(code, armCmp, condEQ, dReg, packImmediate(1));
					createDataProcImm(code, armSub, condEQ, dReg, dReg, 1);
					break;
				case tFloat:
					createFPdataConvPrec(code, armVcvtp, condAlways, dReg, src1Reg, true);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}
			case sCcmpl: case sCcmpg: {
				int type = opds[0].type & ~(1<<ssaTaFitIntoInt);
				if (type == tLong) {
					SSAInstruction next = node.instructions[i+1];
					if (next.ssaOpcode == sCregMove) {i++; next = node.instructions[i+1]; assert false;}
					assert next.ssaOpcode == sCbranch : "sCcompl or sCcompg is not followed by branch instruction";
					int bci = meth.cfg.code[node.lastBCA] & 0xff;
					if (bci == bCifeq) {
						createDataProcCmpReg(code, armCmp, condAlways, src1RegLong, src2RegLong, noShift, 0);
						createDataProcCmpReg(code, armCmp, condEQ, src1Reg, src2Reg, noShift, 0);
						createBranchImm(code, armB, condEQ, 0);
					} else if (bci == bCifne) {
						createDataProcCmpReg(code, armCmp, condAlways, src1RegLong, src2RegLong, noShift, 0);
						createDataProcCmpReg(code, armCmp, condEQ, src1Reg, src2Reg, noShift, 0);
						createBranchImm(code, armB, condNOTEQ, 0);
					} else if (bci == bCiflt) {
						createDataProcCmpReg(code, armCmp, condAlways, src1Reg, src2Reg, noShift, 0);
						createDataProcReg(code, armSbcs, condAlways, scratchReg, src1RegLong, src2RegLong, noShift, 0);
						createBranchImm(code, armB, condLT, 0);
					} else if (bci == bCifge) {
						createDataProcCmpReg(code, armCmp, condAlways, src1Reg, src2Reg, noShift, 0);
						createDataProcReg(code, armSbcs, condAlways, scratchReg, src1RegLong, src2RegLong, noShift, 0);
						createBranchImm(code, armB, condGE, 0);
					} else if (bci == bCifgt) {
						createDataProcCmpReg(code, armCmp, condAlways, src1RegLong, src2RegLong, noShift, 0);
						createBranchImm(code, armB, condLT, 3);
						createBranchImm(code, armB, condGT, 1);
						createDataProcCmpReg(code, armCmp, condAlways, src1Reg, src2Reg, noShift, 0);
						createBranchImm(code, armB, condLS, 0);
						createBranchImm(code, armB, condAlways, 0x800000);
					} else if (bci == bCifle) {
						createDataProcCmpReg(code, armCmp, condAlways, src1RegLong, src2RegLong, noShift, 0);
						createBranchImm(code, armB, condGT, 3);
						createBranchImm(code, armB, condLT, 1);
						createDataProcCmpReg(code, armCmp, condAlways, src1Reg, src2Reg, noShift, 0);
						createBranchImm(code, armB, condHI, 0);
						createBranchImm(code, armB, condAlways, 0x800000);	// mark unconditional branch to be treated like conditional branch at end of node
					} else {
						ErrorReporter.reporter.error(623);
						assert false : "sCcompl or sCcompg is not followed by branch instruction";
						return;
					}
				} else if (type == tFloat  || type == tDouble) {
					createFPdataProc(code, armVcmp, condAlways, src1Reg, src2Reg, type == tFloat);
					createCoProcFPU(code, armVmrs, condAlways, 15);	// get flags only
					SSAInstruction next = node.instructions[i+1];
					assert next.ssaOpcode == sCbranch : "sCcompl or sCcompg is not followed by branch instruction";
					int bci = meth.cfg.code[node.lastBCA] & 0xff;
					if (bci == bCifeq) 
						createBranchImm(code, armB, condEQ, 0);
					else if (bci == bCifne)
						createBranchImm(code, armB, condNOTEQ, 0);
					else if (bci == bCiflt)
						createBranchImm(code, armB, condLT, 0);
					else if (bci == bCifge)
						createBranchImm(code, armB, condGE, 0);
					else if (bci == bCifgt)
						createBranchImm(code, armB, condGT, 0);
					else if (bci == bCifle)
						createBranchImm(code, armB, condLE, 0);
					else {
						ErrorReporter.reporter.error(623);
						assert false : "sCcompl or sCcompg is not followed by branch instruction";
						return;
					}
				} else {
					ErrorReporter.reporter.error(611);
					assert false : "operand of SSA instruction has wrong type";
					return;
				}
				i++;	// following branch instruction is already handled
				break;}
			case sCinstanceof: {
				MonadicRef ref = (MonadicRef)instr;
				Type t = (Type)ref.item;
				if (t.category == tcRef) {	// object (to test for) is regular class or interface
					if ((t.accAndPropFlags & (1<<apfInterface)) != 0) {	// object is interface
						createDataProcCmpImm(code, armCmp, condAlways, src1Reg, 0);	// is null?
						createMovw(code, armMovw, condEQ, dReg, 0);
						createBranchImm(code, armB, condEQ, 12);	// jump to end
						createLSWordImm(code, armLdrb, condAlways, scratchReg, src1Reg, 6, 1, 0, 0);	// get array bit
						createDataProcCmpImm(code, armCmp, condAlways, scratchReg, 0);	// is array?
						createMovw(code, armMovw, condNOTEQ, dReg, 0);
						createBranchImm(code, armB, condNOTEQ, 8);	// jump to end					
						createLSWordImm(code, armLdr, condAlways, scratchReg, src1Reg, 4, 1, 0, 0);	// get tag
						createLSWordImm(code, armLdr, condAlways, LR, scratchReg, Linker32.tdIntfTypeChkTableOffset, 1, 1, 0);
						createDataProcReg(code, armAdd, condAlways, scratchReg, LR, scratchReg, noShift, 0);
						// label 1
						createLSWordImm(code, armLdrh, condAlways, LR, scratchReg, 0, 1, 1, 0);
						createDataProcCmpImm(code, armCmp, condAlways, LR, ((Class)t).chkId);	// is interface chkId?
						createDataProcImm(code, armAdd, condGT, scratchReg, scratchReg, 2);
						createBranchImm(code, armB, condGT, -5);	// jump to label 1
						createMovw(code, armMovw, condLT, dReg, 0);
						createMovw(code, armMovw, condGE, dReg, 1);
					} else {	// regular class
						int offset = ((Class)t).extensionLevel;
						if (t.name.equals(HString.getHString("java/lang/Object"))) {
							createDataProcCmpImm(code, armCmp, condAlways, src1Reg, 0);	// is null?
							createMovw(code, armMovw, condEQ, dReg, 0);
							createMovw(code, armMovw, condNOTEQ, dReg, 1);
						} else { // regular class but not java/lang/Object
							createDataProcCmpImm(code, armCmp, condAlways, src1Reg, 0);	// is null?
							createMovw(code, armMovw, condEQ, dReg, 0);
							createBranchImm(code, armB, condEQ, 10);	// jump to end
							createLSWordImm(code, armLdrb, condAlways, scratchReg, src1Reg, 6, 1, 0, 0);	// get array bit
							createDataProcCmpImm(code, armCmp, condAlways, scratchReg, 0);	// is array?
							createMovw(code, armMovw, condNOTEQ, dReg, 0);
							createBranchImm(code, armB, condNOTEQ, 6);	// jump to end
							createLSWordImm(code, armLdr, condAlways, scratchReg, src1Reg, 4, 1, 0, 0);	// get tag
							createLSWordImm(code, armLdr, condAlways, scratchReg, scratchReg, Linker32.tdBaseClass0Offset + offset * 4, 1, 1, 0);
							loadConstantAndFixup(code, LR, t);	// addr of type
							createDataProcCmpReg(code, armCmp, condAlways, LR, scratchReg, noShift, 0);
							createMovw(code, armMovw, condEQ, dReg, 1);
							createMovw(code, armMovw, condNOTEQ, dReg, 0);
						}
					}
				} else {	// object is an array
					if (((Array)t).componentType.category == tcPrimitive) {  // array of base type
						createDataProcCmpImm(code, armCmp, condAlways, src1Reg, 0);	// is null?
						createMovw(code, armMovw, condEQ, dReg, 0);
						createBranchImm(code, armB, condEQ, 10);	// jump to end		
						createLSWordImm(code, armLdrb, condAlways, scratchReg, src1Reg, 6, 1, 0, 0);	// get array bit
						createDataProcCmpImm(code, armCmp, condAlways, scratchReg, 0);	// is not array?
						createMovw(code, armMovw, condEQ, dReg, 0);
						createBranchImm(code, armB, condEQ, 6);	// jump to end
						createLSWordImm(code, armLdr, condAlways, scratchReg, src1Reg, 4, 1, 0, 0);	// get tag
						loadConstantAndFixup(code, LR, t);	// addr of type
						createDataProcCmpReg(code, armCmp, condAlways, LR, scratchReg, noShift, 0);
						createMovw(code, armMovw, condEQ, dReg, 1);
						createMovw(code, armMovw, condNOTEQ, dReg, 0);
					} else {	// array of regular classes or interfaces
						int nofDim = ((Array)t).dimension;
						Item compType = RefType.refTypeList.getItemByName(((Array)t).componentType.name.toString());
						int offset = ((Class)(((Array)t).componentType)).extensionLevel;
						if (((Array)t).componentType.name.equals(HString.getHString("java/lang/Object"))) {
							createDataProcCmpImm(code, armCmp, condAlways, src1Reg, 0);	// is null?
							createMovw(code, armMovw, condEQ, dReg, 0);
							createBranchImm(code, armB, condEQ, 16);	// jump to end		
							createLSWordImm(code, armLdrb, condAlways, scratchReg, src1Reg, 6, 1, 0, 0);	// get array bit
							createDataProcCmpImm(code, armCmp, condAlways, scratchReg, 0);	// is not array?
							createMovw(code, armMovw, condEQ, dReg, 0);
							createBranchImm(code, armB, condEQ, 12);	// jump to end
							createLSWordImm(code, armLdr, condAlways, scratchReg, src1Reg, 4, 1, 0, 0);	// get tag
							createLSWordImm(code, armLdr, condAlways, LR, scratchReg, 0, 1, 0, 0);	// get first entry of array type descriptor
							createDataProcShiftImm(code, armLsl, condAlways, scratchReg, LR, 1);	// cut off P bit
							createDataProcMovReg(code, armLsr, condAlways, scratchReg, scratchReg, noShift, 17);	// get dim
							createDataProcCmpImm(code, armCmp, condAlways, LR, 0);	// check if array of primitive type
							createBranchImm(code, armB, condLT, 3);	// jump to label 1
							// array of regular classes
							createDataProcCmpImm(code, armCmp, condAlways, scratchReg, nofDim);	// actual dimension must be greater or equal than the dimension of type to test against
							createMovw(code, armMovw, condGE, dReg, 1);
							createMovw(code, armMovw, condLT, dReg, 0);
							createBranchImm(code, armB, condAlways, 2);	// jump to end
							// label 1, is array of primitive type
							createDataProcCmpImm(code, armCmp, condAlways, scratchReg, nofDim);	// actual dimension must be greater than the dimension of type to test against
							createMovw(code, armMovw, condGT, dReg, 1);
							createMovw(code, armMovw, condLE, dReg, 0);
						} else {	// array of regular classes or interfaces but not java/lang/Object
							if ((compType.accAndPropFlags & (1<<apfInterface)) != 0) {	// array of interfaces
								createDataProcCmpImm(code, armCmp, condAlways, src1Reg, 0);	// is null?
								createMovw(code, armMovw, condEQ, dReg, 0);
								createBranchImm(code, armB, condEQ, 22);	// jump to end		
								createLSWordImm(code, armLdrb, condAlways, scratchReg, src1Reg, 6, 1, 0, 0);	// get array bit
								createDataProcCmpImm(code, armCmp, condAlways, scratchReg, 0);	// is not array?
								createMovw(code, armMovw, condEQ, dReg, 0);
								createBranchImm(code, armB, condEQ, 18);	// jump to end
								createLSWordImm(code, armLdr, condAlways, scratchReg, src1Reg, 4, 1, 0, 0);	// get tag
								createLSWordImm(code, armLdr, condAlways, LR, scratchReg, 0, 1, 0, 0);	// get first entry of array type descriptor
								createDataProcShiftImm(code, armLsl, condAlways, LR, LR, 1);	// cut off P bit
								createDataProcMovReg(code, armLsr, condAlways, LR, LR, noShift, 17);	// get dim
								createDataProcCmpImm(code, armCmp, condAlways, LR, nofDim);	// actual dimension must be equal to dimension of type to test against
								createMovw(code, armMovw, condNOTEQ, dReg, 0);
								createBranchImm(code, armB, condNOTEQ, 11);	// jump to end		
								createLSWordImm(code, armLdr, condAlways, scratchReg, scratchReg, 8 + nofDim * 4, 1, 1, 0);	// get component type
								createDataProcCmpImm(code, armCmp, condAlways, scratchReg, 0);	// is 0?
								createMovw(code, armMovw, condEQ, dReg, 0);
								createBranchImm(code, armB, condEQ, 7);	// jump to end		
								createLSWordImm(code, armLdr, condAlways, LR, scratchReg, Linker32.tdIntfTypeChkTableOffset, 1, 1, 0);	// get base class type descriptor
								createDataProcReg(code, armAdd, condAlways, scratchReg, LR, scratchReg, noShift, 0);
								// label 1
								createLSWordImm(code, armLdrh, condAlways, LR, scratchReg, 0, 1, 1, 0);
								createDataProcCmpImm(code, armCmp, condAlways, LR, ((Class)compType).chkId);	// is interface chkId?
								createDataProcImm(code, armAdd, condGT, scratchReg, scratchReg, 2);
								createBranchImm(code, armB, condGT, -5);	// jump to label 1
								createMovw(code, armMovw, condLT, dReg, 0);
								createMovw(code, armMovw, condGE, dReg, 1);
							} else {	// array of regular classes
								createDataProcCmpImm(code, armCmp, condAlways, src1Reg, 0);	// is null?
								createMovw(code, armMovw, condEQ, dReg, 0);
								createBranchImm(code, armB, condEQ, 20);	// jump to end		
								createLSWordImm(code, armLdrb, condAlways, scratchReg, src1Reg, 6, 1, 0, 0);	// get array bit
								createDataProcCmpImm(code, armCmp, condAlways, scratchReg, 0);	// is not array?
								createMovw(code, armMovw, condEQ, dReg, 0);
								createBranchImm(code, armB, condEQ, 16);	// jump to end
								createLSWordImm(code, armLdr, condAlways, scratchReg, src1Reg, 4, 1, 0, 0);	// get tag
								createLSWordImm(code, armLdr, condAlways, LR, scratchReg, 0, 1, 0, 0);	// get first entry of array type descriptor
								createDataProcShiftImm(code, armLsl, condAlways, LR, LR, 1);	// cut off P bit
								createDataProcMovReg(code, armLsr, condAlways, LR, LR, noShift, 17);	// get dim
								createDataProcCmpImm(code, armCmp, condAlways, LR, nofDim);	// actual dimension must be equal to dimension of type to test against
								createMovw(code, armMovw, condNOTEQ, dReg, 0);
								createBranchImm(code, armB, condNOTEQ, 9);	// jump to end		
								createLSWordImm(code, armLdr, condAlways, scratchReg, scratchReg, 8 + nofDim * 4, 1, 1, 0);	// get component type
								createDataProcCmpImm(code, armCmp, condAlways, scratchReg, 0);	// is 0?
								createMovw(code, armMovw, condEQ, dReg, 0);
								createBranchImm(code, armB, condEQ, 5);	// jump to end		
								createLSWordImm(code, armLdr, condAlways, scratchReg, scratchReg, Linker32.tdBaseClass0Offset + offset * 4, 1, 1, 0);	// get base class type descriptor
								loadConstantAndFixup(code, LR, compType);	// addr of component type
								createDataProcCmpReg(code, armCmp, condAlways, LR, scratchReg, noShift, 0);	// is equal?
								createMovw(code, armMovw, condEQ, dReg, 1);
								createMovw(code, armMovw, condNOTEQ, dReg, 0);
							}
						}
					}
				}
				break;}
			case sCcheckcast: {
				MonadicRef ref = (MonadicRef)instr;
				Type t = (Type)ref.item;
				if (t.category == tcRef) {	// object (to test for) is regular class or interface
					if ((t.accAndPropFlags & (1<<apfInterface)) != 0) {	// object is interface
						createDataProcCmpImm(code, armCmp, condAlways, src1Reg, 0);	// is null?
						createBranchImm(code, armB, condEQ, 10);	// jump to end
						createLSWordImm(code, armLdrb, condAlways, scratchReg, src1Reg, 6, 1, 0, 0);	// get array bit
						createDataProcCmpImm(code, armCmp, condAlways, scratchReg, 0);	// is array?
						createSvc(code, armSvc, condNOTEQ, 8);
						createLSWordImm(code, armLdr, condAlways, scratchReg, src1Reg, 4, 1, 0, 0);	// get tag
						createLSWordImm(code, armLdr, condAlways, LR, scratchReg, Linker32.tdIntfTypeChkTableOffset, 1, 1, 0);
						createDataProcReg(code, armAdd, condAlways, scratchReg, LR, scratchReg, noShift, 0);
						// label 1
						createLSWordImm(code, armLdrh, condAlways, LR, scratchReg, 0, 1, 1, 0);
						createDataProcCmpImm(code, armCmp, condAlways, LR, ((Class)t).chkId);	// is interface chkId?
						createDataProcImm(code, armAdd, condGT, scratchReg, scratchReg, 2);
						createBranchImm(code, armB, condGT, -5);	// jump to label 1
						createSvc(code, armSvc, condNOTEQ, 8);	// chkId is not equal
					} else {	// regular class
						int offset = ((Class)t).extensionLevel;
						createDataProcCmpImm(code, armCmp, condAlways, src1Reg, 0);	// is null?
						createBranchImm(code, armB, condEQ, 8);	// jump to end
						createLSWordImm(code, armLdrb, condAlways, scratchReg, src1Reg, 6, 1, 0, 0);	// get array bit
						createDataProcCmpImm(code, armCmp, condAlways, scratchReg, 0);	// is array?
						createSvc(code, armSvc, condNOTEQ, 8);
						createLSWordImm(code, armLdr, condAlways, scratchReg, src1Reg, 4, 1, 0, 0);	// get tag
						createLSWordImm(code, armLdr, condAlways, scratchReg, scratchReg, Linker32.tdBaseClass0Offset + offset * 4, 1, 1, 0);
						loadConstantAndFixup(code, LR, t);	// addr of type
						createDataProcCmpReg(code, armCmp, condAlways, LR, scratchReg, noShift, 0);
						createSvc(code, armSvc, condNOTEQ, 8);
					}
				} else {	// object is an array
					if (((Array)t).componentType.category == tcPrimitive) {  // array of base type
						createDataProcCmpImm(code, armCmp, condAlways, src1Reg, 0);	// is null?
						createBranchImm(code, armB, condEQ, 7);	// jump to end
						createLSWordImm(code, armLdrb, condAlways, scratchReg, src1Reg, 6, 1, 0, 0);	// get array bit
						createDataProcCmpImm(code, armCmp, condAlways, scratchReg, 0);	// is not array?
						createSvc(code, armSvc, condEQ, 8);
						createLSWordImm(code, armLdr, condAlways, scratchReg, src1Reg, 4, 1, 0, 0);	// get tag
						loadConstantAndFixup(code, LR, t);	// addr of type
						createDataProcCmpReg(code, armCmp, condAlways, LR, scratchReg, noShift, 0);
						createSvc(code, armSvc, condNOTEQ, 8);
					} else {	// array of regular classes or interfaces
						int nofDim = ((Array)t).dimension;
						Item compType = RefType.refTypeList.getItemByName(((Array)t).componentType.name.toString());
						int offset = ((Class)(((Array)t).componentType)).extensionLevel;
						if (((Array)t).componentType.name.equals(HString.getHString("java/lang/Object"))) {
							createDataProcCmpImm(code, armCmp, condAlways, src1Reg, 0);	// is null?
							createBranchImm(code, armB, condEQ, 13);	// jump to end		
							createLSWordImm(code, armLdrb, condAlways, scratchReg, src1Reg, 6, 1, 0, 0);	// get array bit
							createDataProcCmpImm(code, armCmp, condAlways, scratchReg, 0);	// is not array?
							createSvc(code, armSvc, condEQ, 8);
							createLSWordImm(code, armLdr, condAlways, scratchReg, src1Reg, 4, 1, 0, 0);	// get tag
							createLSWordImm(code, armLdr, condAlways, LR, scratchReg, 0, 1, 0, 0);	// get first entry of array type descriptor
							createDataProcShiftImm(code, armLsl, condAlways, scratchReg, LR, 1);	// cut off P bit
							createDataProcMovReg(code, armLsr, condAlways, scratchReg, scratchReg, noShift, 17);	// get dim
							createDataProcCmpImm(code, armCmp, condAlways, LR, 0);	// check if array of primitive type
							createBranchImm(code, armB, condLT, 2);	// jump to label 1
							// array of regular classes
							createDataProcCmpImm(code, armCmp, condAlways, scratchReg, nofDim);	// actual dimension must be greater or equal than the dimension of type to test against
							createSvc(code, armSvc, condLT, 8);
							createBranchImm(code, armB, condAlways, 1);	// jump to end
							// label 1, is array of primitive type
							createDataProcCmpImm(code, armCmp, condAlways, scratchReg, nofDim);	// actual dimension must be greater than the dimension of type to test against
							createSvc(code, armSvc, condLE, 8);
						} else {	// array of regular classes or interfaces but not java/lang/Object
							if ((compType.accAndPropFlags & (1<<apfInterface)) != 0) {	// array of interfaces
								createDataProcCmpImm(code, armCmp, condAlways, src1Reg, 0);	// is null?
								createBranchImm(code, armB, condEQ, 18);	// jump to end		
								createLSWordImm(code, armLdrb, condAlways, scratchReg, src1Reg, 6, 1, 0, 0);	// get array bit
								createDataProcCmpImm(code, armCmp, condAlways, scratchReg, 0);	// is not array?
								createSvc(code, armSvc, condEQ, 8);
								createLSWordImm(code, armLdr, condAlways, scratchReg, src1Reg, 4, 1, 0, 0);	// get tag
								createLSWordImm(code, armLdr, condAlways, LR, scratchReg, 0, 1, 0, 0);	// get first entry of array type descriptor
								createDataProcShiftImm(code, armLsl, condAlways, LR, LR, 1);	// cut off P bit
								createDataProcMovReg(code, armLsr, condAlways, LR, LR, noShift, 17);	// get dim
								createDataProcCmpImm(code, armCmp, condAlways, LR, nofDim);	// actual dimension must be equal to dimension of type to test against
								createSvc(code, armSvc, condNOTEQ, 8);		
								createLSWordImm(code, armLdr, condAlways, scratchReg, scratchReg, 8 + nofDim * 4, 1, 1, 0);	// get component type
								createDataProcCmpImm(code, armCmp, condAlways, scratchReg, 0);	// is 0?
								createSvc(code, armSvc, condEQ, 8);
								createLSWordImm(code, armLdr, condAlways, LR, scratchReg, Linker32.tdIntfTypeChkTableOffset, 1, 1, 0);	// get base class type descriptor
								createDataProcReg(code, armAdd, condAlways, scratchReg, LR, scratchReg, noShift, 0);
								// label 1
								createLSWordImm(code, armLdrh, condAlways, LR, scratchReg, 0, 1, 1, 0);
								createDataProcCmpImm(code, armCmp, condAlways, LR, ((Class)compType).chkId);	// is interface chkId?
								createDataProcImm(code, armAdd, condGT, scratchReg, scratchReg, 2);
								createBranchImm(code, armB, condGT, -5);	// jump to label 1
								createSvc(code, armSvc, condLT, 8);	
							} else {	// array of regular classes
								createDataProcCmpImm(code, armCmp, condAlways, src1Reg, 0);	// is null?
								createBranchImm(code, armB, condEQ, 16);	// jump to end		
								createLSWordImm(code, armLdrb, condAlways, scratchReg, src1Reg, 6, 1, 0, 0);	// get array bit
								createDataProcCmpImm(code, armCmp, condAlways, scratchReg, 0);	// is not array?
								createSvc(code, armSvc, condEQ, 8);
								createLSWordImm(code, armLdr, condAlways, scratchReg, src1Reg, 4, 1, 0, 0);	// get tag
								createLSWordImm(code, armLdr, condAlways, LR, scratchReg, 0, 1, 0, 0);	// get first entry of array type descriptor
								createDataProcShiftImm(code, armLsl, condAlways, LR, LR, 1);	// cut off P bit
								createDataProcMovReg(code, armLsr, condAlways, LR, LR, noShift, 17);	// get dim
								createDataProcCmpImm(code, armCmp, condAlways, LR, nofDim);	// actual dimension must be equal to dimension of type to test against
								createSvc(code, armSvc, condNOTEQ, 8);
								createLSWordImm(code, armLdr, condAlways, scratchReg, scratchReg, 8 + nofDim * 4, 1, 1, 0);	// get component type
								createDataProcCmpImm(code, armCmp, condAlways, scratchReg, 0);	// is 0?
								createSvc(code, armSvc, condEQ, 8);
								createLSWordImm(code, armLdr, condAlways, scratchReg, scratchReg, Linker32.tdBaseClass0Offset + offset * 4, 1, 1, 0);	// get base class type descriptor
								loadConstantAndFixup(code, LR, compType);	// addr of component type
								createDataProcCmpReg(code, armCmp, condAlways, LR, scratchReg, noShift, 0);	// is equal?
								createSvc(code, armSvc, condNOTEQ, 8);
							}
						}
					}
				}
				break;}
			case sCthrow: {
//				assert false;
				break;}
			case sCalength: {
				int refReg = src1Reg;
				createDataProcCmpImm(code, armCmp, condAlways, refReg, 0);
				createSvc(code, armSvc, condEQ, 7);
				createLSWordImm(code, armLdrsh, condAlways, dReg, refReg, arrayLenOffset, 1, 0, 0);
				break;}
			case sCcall: {
				Call call = (Call)instr;
				Method m = (Method)call.item;
				if (dbg) StdStreams.vrb.println("is a call");
				if ((m.accAndPropFlags & (1 << dpfSynthetic)) != 0) {
					if (dbg) StdStreams.vrb.println("and synthetic");
					if (m.id == idGET1) {	// GET1
						createLSWordImm(code, armLdrsb, condAlways, dReg, src1Reg, 0, 0, 0, 0);
					} else if (m.id == idGET2) { // GET2
						createLSWordImm(code, armLdrsh, condAlways, dReg, src1Reg, 0, 0, 0, 0);
					} else if (m.id == idGET4) { // GET4
						createLSWordImm(code, armLdr, condAlways, dReg, src1Reg, 0, 0, 0, 0);
					} else if (m.id == idGET8) { // GET8
						createLSWordImm(code, armLdr, condAlways, dReg, src1Reg, 0, 0, 0, 0);
						createLSWordImm(code, armLdr, condAlways, dRegLong, src1Reg, 4, 1, 1, 0);
					} else if (m.id == idPUT1) { // PUT1
						createLSWordImm(code, armStrb, condAlways, src2Reg, src1Reg, 0, 0, 0, 0);
					} else if (m.id == idPUT2) { // PUT2
						createLSWordImm(code, armStrh, condAlways, src2Reg, src1Reg, 0, 0, 0, 0);
					} else if (m.id == idPUT4) { // PUT4
						createLSWordImm(code, armStr, condAlways, src2Reg, src1Reg, 0, 0, 0, 0);
					} else if (m.id == idPUT8) { // PUT8
						createLSWordImm(code, armStr, condAlways, src2Reg, src1Reg, 0, 0, 0, 0);
						createLSWordImm(code, armStr, condAlways, src2RegLong, src1Reg, 4, 1, 1, 0);
					} else if (m.id == idBIT) { // BIT
						createLSWordImm(code, armLdr, condAlways, dReg, src1Reg, 0, 0, 0, 0);
						if (src2Reg < 0) {
							int immVal = ((StdConstant)opds[1].constant).valueH;
							if (immVal > 0) createDataProcMovReg(code, armLsr, condAlways, dReg, dReg, noShift, immVal);	
						} else {
							createDataProcShiftReg(code, armLsr, condAlways, dReg, dReg, src2Reg);
						}
						createDataProcImm(code, armAnd, condAlways, dReg, dReg, 1);
					} else if (m.id == idGETGPR) { // GETGPR
						int gpr = ((StdConstant)opds[0].constant).valueH;
						createDataProcMovReg(code, armMov, condAlways, dReg, gpr, noShift, 0);
					} else if (m.id == idGETEXTRD) { // GETEXTRD
						int fpr = ((StdConstant)opds[0].constant).valueH;
						createFPdataProc(code, armVmov, condAlways, dReg, 0, fpr, false);
					} else if (m.id == idGETEXTRS) { // GETEXTRS
						int fpr = ((StdConstant)opds[0].constant).valueH;
						createFPdataProc(code, armVmov, condAlways, dReg, 0, fpr, true);
					} else if (m.id == idGETCPR) { // GETCPR
						int coproc = ((StdConstant)opds[0].constant).valueH;
						int CRn = ((StdConstant)opds[1].constant).valueH;
						int opc1 = ((StdConstant)opds[2].constant).valueH;
						int CRm = ((StdConstant)opds[3].constant).valueH;
						int opc2 = ((StdConstant)opds[4].constant).valueH;
						createCoProc(code, armMrc, condAlways, coproc, opc1, dReg, CRn, CRm, opc2);
					} else if (m.id == idPUTGPR) { // PUTGPR
						int gpr = ((StdConstant)opds[0].constant).valueH;
						createDataProcMovReg(code, armMov, condAlways, gpr, src2Reg, noShift, 0);
					} else if (m.id == idPUTEXTRD) { // PUTEXTRD
						int fpr = ((StdConstant)opds[0].constant).valueH;
						createFPdataProc(code, armVmov, condAlways, fpr, 0, src2Reg, false);
					} else if (m.id == idPUTEXTRS) { // PUTEXTRS
						int fpr = ((StdConstant)opds[0].constant).valueH;
						createFPdataProc(code, armVmov, condAlways, fpr, 0, src2Reg, true);
					} else if (m.id == idPUTCPR) { // PUTCPR
						int coproc = ((StdConstant)opds[0].constant).valueH;
						int CRn = ((StdConstant)opds[1].constant).valueH;
						int opc1 = ((StdConstant)opds[2].constant).valueH;
						int CRm = ((StdConstant)opds[3].constant).valueH;
						int opc2 = ((StdConstant)opds[4].constant).valueH;
						int Rt = ((StdConstant)opds[4].constant).valueH;
						createCoProc(code, armMcr, condAlways, coproc, opc1, Rt, CRn, CRm, opc2);
//					} else if (m.id == idHALT) { // HALT	// TODO
//						createItrap(ppcTw, TOalways, 0, 0);
					} else if (m.id == idASM) { // ASM
						code.instructions[code.iCount] = InstructionDecoder.dec.getCode(((StringLiteral)opds[0].constant).string.toString());
						code.iCount++;
						int len = code.instructions.length;
						if (code.iCount == len) {
							int[] newInstructions = new int[2 * len];
							for (int k = 0; k < len; k++)
								newInstructions[k] = code.instructions[k];
							code.instructions = newInstructions;
						}
					} else if (m.id == idADR_OF_METHOD) { // ADR_OF_METHOD
						HString name = ((StringLiteral)opds[0].constant).string;
						int last = name.lastIndexOf('/');
						HString className = name.substring(0, last);
						HString methName = name.substring(last + 1);
						Class clazz = (Class)(RefType.refTypeList.getItemByName(className.toString()));
						if(clazz == null){
							ErrorReporter.reporter.error(634, className.toString());
							assert false : "class not found" + className.toString();
						} else {
							Item method = clazz.methods.getItemByName(methName.toString());
							loadConstantAndFixup(code, res.reg, method);	// addr of method
						}
					} else if (m.id == idREF) { // REF
						createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
					} else if (m.id == idDoubleToBits) { // DoubleToBits
						createFPregMove(code, armVmovDouble, condAlways, src1Reg, dReg, dRegLong, true, false);
					} else if (m.id == idBitsToDouble) { // BitsToDouble
						createFPregMove(code, armVmovDouble, condAlways, dReg, src1Reg, src1RegLong, false, false);
					} else if (m.id == idFloatToBits) { // FloatToBits
						createFPregMove(code, armVmovSingle, condAlways, src1Reg, dReg, 0, true, true);
					} else if (m.id == idBitsToFloat) { // BitsToFloat
						createFPregMove(code, armVmovSingle, condAlways, dReg, src1Reg, 0, false, true);
					} else {
						ErrorReporter.reporter.error(626, m.name.toString());
						assert false : "not implemented " + m.name.toString();
					}
				} else {	// real method (not synthetic)
					if ((m.accAndPropFlags & (1<<apfStatic)) != 0 ||
							m.name.equals(HString.getHString("newPrimTypeArray")) ||
							m.name.equals(HString.getHString("newRefArray"))
							) {	// invokestatic
						if (m == stringNewstringMethod) {	// replace newstring stub with Heap.newstring
							m = heapNewstringMethod;
						}
						if (dbg) StdStreams.vrb.println("call to " + m.name + ": copy parameters");
						copyParameters(code, opds);
						insertBLAndFixup(code, m);	// addr of method
					} else if ((m.accAndPropFlags & (1<<dpfInterfCall)) != 0) {	// invokeinterface
						copyParameters(code, opds);
						int refReg = paramStartGPR;
						int offset = (Class.maxExtensionLevelStdClasses + 1) * Linker32.slotSize + Linker32.tdBaseClass0Offset;
						createDataProcCmpImm(code, armCmp, condAlways, refReg, 0);
						createSvc(code, armSvc, condEQ, 7);
						createLSWordImm(code, armLdr, condAlways, LR, refReg, 4, 1, 0, 0);
						createLSWordImm(code, armLdr, condAlways, LR, LR, offset, 1, 1, 0);	// , offset is positive, delegate method
						loadConstant(code, scratchReg, m.owner.index << 16 | m.index * 4);	// interface id and method offset	
						createBranchReg(code, armBlxReg, condAlways, LR);
					} else if (call.invokespecial) {	// invokespecial
						if (newString) {	// special treatment for strings
							if (m == strInitC) m = strAllocC;
							else if (m == strInitCII) m = strAllocCII;	// addr of corresponding allocate method
							else if (m == strInitCII) m = strAllocCII;
							if (dbg) StdStreams.vrb.println("call to " + m.name + ": copy parameters");
							copyParameters(code, opds);
							insertBLAndFixup(code, m);
						} else {
							if (dbg) StdStreams.vrb.println("call to " + m.name + ": copy parameters");
							copyParameters(code, opds);
							int refReg = paramStartGPR;
							createDataProcCmpImm(code, armCmp, condAlways, refReg, 0);
							createSvc(code, armSvc, condEQ, 7);
							insertBLAndFixup(code, m);
						}
					} else {	// invokevirtual 
						if (dbg) StdStreams.vrb.println("call to " + m.name + ": copy parameters");
						copyParameters(code, opds);
						int refReg = paramStartGPR;
						int offset = Linker32.tdMethTabOffset;
						offset -= m.index * Linker32.slotSize; 
						createDataProcCmpImm(code, armCmp, condAlways, refReg, 0);
						createSvc(code, armSvc, condEQ, 7);
						createLSWordImm(code, armLdr, condAlways, LR, refReg, 4, 1, 0, 0);
						createLSWordImm(code, armLdr, condAlways, LR, LR, -offset, 1, 0, 0);	// offset is negative
						createBranchReg(code, armBlxReg, condAlways, LR);
					}

					if (newString) {
						int sizeOfObject = Type.wktObject.objectSize;
						createDataProcImm(code, armMov, condAlways, paramStartGPR + opds.length, 0, sizeOfObject); // reg after last parameter
					}

					// get result, must be copied from return register to result register
					int type = res.type & ~(1<<ssaTaFitIntoInt);
					if (type == tLong) {	// call must return in correct registers
						if (dRegLong == returnGPR2) {
							if (dReg == returnGPR1) {	// returnGPR2 -> scratchReg, returnGPR1 -> returnGPR2, scratchReg -> returnGPR1
								createDataProcMovReg(code, armMov, condAlways, scratchReg, returnGPR2, noShift, 0);
								createDataProcMovReg(code, armMov, condAlways, dRegLong, returnGPR1, noShift, 0);
								createDataProcMovReg(code, armMov, condAlways, dReg, scratchReg, noShift, 0);
							} else {	// returnGPR2 -> reg, returnGPR1 -> r3
								createDataProcMovReg(code, armMov, condAlways, dReg, returnGPR2, noShift, 0);
								createDataProcMovReg(code, armMov, condAlways, dRegLong, returnGPR1, noShift, 0);
							}
						} else { // returnGPR1 -> regLong, returnGPR2 -> reg
							createDataProcMovReg(code, armMov, condAlways, dRegLong, returnGPR1, noShift, 0);
							createDataProcMovReg(code, armMov, condAlways, dReg, returnGPR2, noShift, 0);
						}
					} else if (type == tFloat || type == tDouble) {
						createFPdataProc(code, armVmov, condAlways, dReg, 0, returnEXTR, (type == tFloat));
					} else if (type == tVoid) {
						if (newString) {
							newString = false;
							createDataProcMovReg(code, armMov, condAlways, stringReg, returnGPR1, noShift, 0); // stringReg was set by preceding sCnew
						}
					} else
						createDataProcMovReg(code, armMov, condAlways, dReg, returnGPR1, noShift, 0);
				}
				break;}	//sCcall
			case sCnew: {
				Item item = ((Call)instr).item;	// item = ref
				Item method;
				if (opds == null) {	// bCnew
					if (item == Type.wktString) {
						newString = true;	// allocation of strings is postponed
						stringReg = dReg;
						loadConstantAndFixup(code, dReg, item);	// ref to string
					} else {
						method = CFR.getNewMemoryMethod(bCnew);
						loadConstantAndFixup(code, paramStartGPR, item);	// ref
						insertBLAndFixup(code, method);	// addr of new
						createDataProcMovReg(code, armMov, condAlways, dReg, returnGPR1, noShift, 0);
					}
				} else if (opds.length == 1) {
					switch (res.type  & ~(1<<ssaTaFitIntoInt)) {
					case tAboolean: case tAchar: case tAfloat: case tAdouble:
					case tAbyte: case tAshort: case tAinteger: case tAlong:	// bCnewarray
						method = CFR.getNewMemoryMethod(bCnewarray);
						createDataProcMovReg(code, armMov, condAlways, paramStartGPR, src1Reg, noShift, 0);	// nof elems
						createDataProcImm(code, armMov, condAlways, paramStartGPR + 1, 0, (instr.result.type & 0x7fffffff) - 10);	// type
						loadConstantAndFixup(code, paramStartGPR + 2, item);	// ref to type descriptor
						insertBLAndFixup(code, method);	// addr of newarray
						createDataProcMovReg(code, armMov, condAlways, dReg, returnGPR1, noShift, 0);
						break;
					case tAref:	// bCanewarray
						method = CFR.getNewMemoryMethod(bCanewarray);
						createDataProcMovReg(code, armMov, condAlways, paramStartGPR, src1Reg, noShift, 0);	// nof elems
						loadConstantAndFixup(code, paramStartGPR + 1, item);	// ref to type descriptor
						insertBLAndFixup(code, method);	// addr of anewarray
						createDataProcMovReg(code, armMov, condAlways, dReg, returnGPR1, noShift, 0);
						break;
					default:
						ErrorReporter.reporter.error(612);
						assert false : "operand of new instruction has wrong type";
						return;
					}
				} else { // bCmultianewarray:
					method = CFR.getNewMemoryMethod(bCmultianewarray);
					loadConstantAndFixup(code, LR, method);	// addr of multianewarray
					copyParametersNewArray(code, opds);
					loadConstantAndFixup(code, paramStartGPR, item);	// ref to type descriptor
					createMovw(code, armMovw, condAlways, paramStartGPR + 1, opds.length);	// nofDimensions
					createBranchReg(code, armBlxReg, condAlways, LR);
					createDataProcMovReg(code, armMov, condAlways, dReg, returnGPR1, noShift, 0);
				}
				break;}
			case sCreturn: {
				int bci = meth.ssa.cfg.code[node.lastBCA] & 0xff;
				switch (bci) {
				case bCreturn:
					break;
				case bCireturn:
				case bCareturn:
					createDataProcMovReg(code, armMov, condAlways, returnGPR1, src1Reg, noShift, 0);
					break;
				case bClreturn:
					createDataProcMovReg(code, armMov, condAlways, returnGPR1, src1RegLong, noShift, 0);
					createDataProcMovReg(code, armMov, condAlways, returnGPR2, src1Reg, noShift, 0);
					break;
				case bCfreturn: 
					createFPdataProc(code, armVmov, condAlways, returnEXTR, 0, src1Reg, true);
					break;
				case bCdreturn:
					createFPdataProc(code, armVmov, condAlways, returnEXTR, 0, src1Reg, false);
					break;
				default:
					ErrorReporter.reporter.error(620);
					assert false : "return instruction not implemented";
					return;
				}
				if (node.next != null)	// last node needs no branch, other nodes branch to epilogue 
					createBranchImm(code, armB, condAlways, 0);
				break;}
			case sCbranch:
			case sCswitch: {
				int bci = meth.cfg.code[node.lastBCA] & 0xff;
				switch (bci) {
				case bCgoto:
					createBranchImm(code, armB, condAlways, 0);
					break;
				case bCif_acmpeq:
				case bCif_acmpne:
					createDataProcReg(code, armCmp, condAlways, 0, src2Reg, src1Reg, noShift, 0);
					if (bci == bCif_acmpeq)
						createBranchImm(code, armB, condEQ, 0);
					else
						createBranchImm(code, armB, condNOTEQ, 0);
					break;
				case bCif_icmpeq:
				case bCif_icmpne:
				case bCif_icmplt:
				case bCif_icmpge:
				case bCif_icmpgt:
				case bCif_icmple:
					boolean inverted = false;
					if (src1Reg < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						createDataProcCmpImm(code, armCmp, condAlways, src2Reg, immVal);
					} else if (src2Reg < 0) {
						inverted = true;
						int immVal = ((StdConstant)opds[1].constant).valueH;
						createDataProcCmpImm(code, armCmp, condAlways, src1Reg, immVal);
					} else {
						createDataProcReg(code, armCmp, condAlways, 0, src2Reg, src1Reg, noShift, 0);
					}
					if (!inverted) {
						if (bci == bCif_icmpeq) 
							createBranchImm(code, armB, condEQ, 0);
						else if (bci == bCif_icmpne)
							createBranchImm(code, armB, condNOTEQ, 0);
						else if (bci == bCif_icmplt)
							createBranchImm(code, armB, condLT, 0);
						else if (bci == bCif_icmpge)
							createBranchImm(code, armB, condGE, 0);
						else if (bci == bCif_icmpgt)
							createBranchImm(code, armB, condGT, 0);
						else if (bci == bCif_icmple)
							createBranchImm(code, armB, condLE, 0);
					} else {
						if (bci == bCif_icmpeq) 
							createBranchImm(code, armB, condEQ, 0);
						else if (bci == bCif_icmpne)
							createBranchImm(code, armB, condNOTEQ, 0);
						else if (bci == bCif_icmplt)
							createBranchImm(code, armB, condGE, 0);
						else if (bci == bCif_icmpge)
							createBranchImm(code, armB, condLT, 0);
						else if (bci == bCif_icmpgt)
							createBranchImm(code, armB, condLE, 0);
						else if (bci == bCif_icmple)
							createBranchImm(code, armB, condGT, 0);
					}
					break; 
				case bCifeq:
				case bCifne:
				case bCiflt:
				case bCifge:
				case bCifgt:
				case bCifle: 
					createDataProcCmpImm(code, armCmp, condAlways, src1Reg, 0);
					if (bci == bCifeq) 
						createBranchImm(code, armB, condEQ, 0);
					else if (bci == bCifne)
						createBranchImm(code, armB, condNOTEQ, 0);
					else if (bci == bCiflt)
						createBranchImm(code, armB, condLT, 0);
					else if (bci == bCifge)
						createBranchImm(code, armB, condGE, 0);
					else if (bci == bCifgt)
						createBranchImm(code, armB, condGT, 0);
					else if (bci == bCifle)
						createBranchImm(code, armB, condLE, 0);
					break;
				case bCifnonnull:
				case bCifnull: 
					createDataProcCmpImm(code, armCmp, condAlways, src1Reg, 0);
					if (bci == bCifnonnull)
						createBranchImm(code, armB, condNOTEQ, 0);
					else
						createBranchImm(code, armB, condEQ, 0);
					break;
				case bCtableswitch:
					int addr = node.lastBCA + 1;
					addr = (addr + 3) & -4; // round to the next multiple of 4
					addr += 4; // skip default offset
					int low = getInt(meth.ssa.cfg.code, addr);
					int high = getInt(meth.ssa.cfg.code, addr + 4);
					int nofCases = high - low + 1;
					for (int k = 0; k < nofCases; k++) {
						int val = low + k;
						if (isPackable(Math.abs(val))) {
							if (val > 0) createDataProcCmpImm(code, armCmp, condAlways, src1Reg, packImmediate(val));
							else createDataProcCmpImm(code, armCmn, condAlways, src1Reg, packImmediate(-val));
							createBranchImm(code, armB, condEQ, 2);
						} else {
							loadConstant(code, scratchReg, val);
							createDataProcReg(code, armCmp, condAlways, 0, src1Reg, scratchReg, noShift, 0);
							if (((val >> 16) & 0xffff) == 0) createBranchImm(code, armB, condEQ, 3);
							else createBranchImm(code, armB, condEQ, 4);
						}
					}
					createBranchImm(code, armB, condAlways, nofCases);
					break;
				case bClookupswitch:
					addr = node.lastBCA + 1;
					addr = (addr + 3) & -4; // round to the next multiple of 4
					addr += 4; // skip default offset
					int nofPairs = getInt(meth.ssa.cfg.code, addr);
					for (int k = 0; k < nofPairs; k++) {
						int key = getInt(meth.ssa.cfg.code, addr + 4 + k * 8);
						if (isPackable(Math.abs(key))) {
							if (key > 0) createDataProcCmpImm(code, armCmp, condAlways, src1Reg, packImmediate(key));
							else createDataProcCmpImm(code, armCmn, condAlways, src1Reg, packImmediate(-key));
							createBranchImm(code, armB, condEQ, 2);
						} else {
							loadConstant(code, scratchReg, key);
							createDataProcReg(code, armCmp, condAlways, 0, src1Reg, scratchReg, noShift, 0);	
							if (((key >> 16) & 0xffff) == 0) createBranchImm(code, armB, condEQ, 3);
							else createBranchImm(code, armB, condEQ, 4);
						}
					}
					createBranchImm(code, armB, condAlways, nofPairs);
					break;
				default:
					ErrorReporter.reporter.error(621);
					assert false : "branch instruction not implemented";
					return;
				}
				break;}
			case sCregMove: {
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tInteger: case tChar: case tShort: case tByte: 
				case tBoolean: case tRef: case tAref: case tAboolean:
				case tAchar: case tAfloat: case tAdouble: case tAbyte: 
				case tAshort: case tAinteger: case tAlong:
					createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
					break;
				case tLong: 
					createDataProcMovReg(code, armMov, condAlways, dRegLong, src1RegLong, noShift, 0);
					createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
					break;
				case tFloat: 
					createFPdataProc(code, armVmov, condAlways, dReg, 0, src1Reg, true);
					break;
				case tDouble:
					createFPdataProc(code, armVmov, condAlways, dReg, 0, src1Reg, false);
					break;
				default:
					if (dbg) StdStreams.vrb.println("type = " + (res.type & 0x7fffffff));
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}
			default:
				ErrorReporter.reporter.error(625);
				assert false : "SSA instruction not implemented: " + SSAInstructionMnemonics.scMnemonics[instr.ssaOpcode] + " function";
				return;
			}
			if (instr.ssaOpcode != sCcmpl && instr.ssaOpcode != sCcmpg) {	// result must not be handled for this case because these instructions
				// produce a result which does not to have to be stored onto the stack
				if (dRegLongSlot >= 0) createLSWordImm(code, armStr, condAlways, dRegLong, stackPtr, code.localVarOffset + 4 * dRegLongSlot, 1, 1, 0);
				if (dRegSlot >= 0) {
					if ((res.type == tFloat) || (res.type == tDouble)) createLSExtReg(code, armVstr, condAlways, dReg, stackPtr, code.localVarOffset + 4 * dRegSlot, (res.type == tFloat));
					else createLSWordImm(code, armStr, condAlways, dReg, stackPtr, code.localVarOffset + 4 * dRegSlot, 1, 1, 0);
				}
			}
		}
	}

	private boolean isPackable(int val) {
		int lead = Integer.numberOfLeadingZeros(val);
		lead -= lead % 2;	// make even, immediate operands can be shifted by an even number only
		return lead + Integer.numberOfTrailingZeros(val) >= 24;		
	}
	
	private int packImmediate(int immVal) {
		int val = Integer.numberOfLeadingZeros(immVal);
		val -= val % 2;	// make even
		int shift = (24 - val);
		if (shift > 0) {
			int baseVal = Integer.rotateRight(immVal, shift);
			immVal = baseVal | ((16 - shift / 2) << 8);
		}
		return immVal;
	}

	// copy parameters for methods into parameter registers or onto stack, uses scratch register but not LR
	private void copyParameters(Code32 code, SSAValue[] opds) {
//		boolean dbg = true;
		// information about the src registers for parameters of a call to a method 
		int[] srcGPR = new int[nofGPR];	
		int[] srcGPRcount = new int[nofGPR];
		int[] srcEXTR = new int[nofEXTR];
		boolean[] srcEXTRtype = new boolean[nofEXTR];
		int[] srcEXTRcount = new int[nofEXTR];

		int offset = 0;
		for (int k = 0; k < nofGPR; k++) {srcGPR[k] = -1; srcGPRcount[k] = 0;}
		for (int k = 0; k < nofEXTR; k++) {srcEXTR[k] = -1; srcEXTRtype[k] = false; srcEXTRcount[k] = 0;}

		// get info about in which register parameters are located
		// parameters which go onto the stack are treated equally
		for (int k = 0, kGPR = 0, kEXTR = 0; k < opds.length; k++) {
			int type = opds[k].type & ~(1<<ssaTaFitIntoInt);
			if (type == tLong) {
				srcGPR[kGPR + paramStartGPR] = opds[k].regLong;
				srcGPR[kGPR + 1 + paramStartGPR] = opds[k].reg;
				kGPR += 2;
			} else if (type == tFloat || type == tDouble) {
				srcEXTR[kEXTR + paramStartEXTR] = opds[k].reg;
				srcEXTRtype[kEXTR + paramStartEXTR] = type == tFloat;
				kEXTR++;
			} else {
				srcGPR[kGPR + paramStartGPR] = opds[k].reg;
				kGPR++;
			}
		}

		if (dbg) {
			StdStreams.vrb.print("\tsrcGPR = ");
			for (int k = paramStartGPR; srcGPR[k] != -1; k++) StdStreams.vrb.print(srcGPR[k] + ","); 
			StdStreams.vrb.print("\tsrcGPRcount = ");
			for (int k = 0; k < nofGPR; k++) StdStreams.vrb.print(srcGPRcount[k] + ","); 
			StdStreams.vrb.println();
			StdStreams.vrb.print("\tsrcEXTR = ");
			for (int k = paramStartEXTR; srcEXTR[k] != -1; k++) StdStreams.vrb.print(srcEXTR[k] + ","); 
			StdStreams.vrb.print("\tsrcEXTRtype = ");
			for (int k = paramStartEXTR; srcEXTR[k] != -1; k++) StdStreams.vrb.print(srcEXTRtype[k] + ","); 
			StdStreams.vrb.print("\tsrcEXTRDcount = ");
			for (int k = 0; k < nofEXTR; k++) StdStreams.vrb.print(srcEXTRcount[k] + ","); 
			StdStreams.vrb.println();
		}

		// count register usage
		int i = paramStartGPR;
		while (srcGPR[i] != -1) {
			if (srcGPR[i] <= topGPR) srcGPRcount[srcGPR[i]]++;
			i++;
		}
		i = paramStartEXTR;
		while (srcEXTR[i] != -1) {
			if (srcEXTR[i] <= topEXTR) {
				if (srcEXTRtype[i]) srcEXTRcount[srcEXTR[i]/2]++;
				else srcEXTRcount[srcEXTR[i]]++;
			}
			i++;
		}
		if (dbg) {
			StdStreams.vrb.print("\tsrcGPR = ");
			for (int k = paramStartGPR; srcGPR[k] != -1; k++) StdStreams.vrb.print(srcGPR[k] + ","); 
			StdStreams.vrb.print("\tsrcGPRcount = ");
			for (int k = 0; k < nofGPR; k++) StdStreams.vrb.print(srcGPRcount[k] + ","); 
			StdStreams.vrb.println();
			StdStreams.vrb.print("\tsrcEXTR = ");
			for (int k = paramStartEXTR; srcEXTR[k] != -1; k++) StdStreams.vrb.print(srcEXTR[k] + ","); 
			StdStreams.vrb.print("\tsrcEXTRtype = ");
			for (int k = paramStartEXTR; srcEXTR[k] != -1; k++) StdStreams.vrb.print(srcEXTRtype[k] + ","); 
			StdStreams.vrb.print("\tsrcEXTRDcount = ");
			for (int k = 0; k < nofEXTR; k++) StdStreams.vrb.print(srcEXTRcount[k] + ","); 
			StdStreams.vrb.println();
		}
		
		// handle move to itself
		i = paramStartGPR;
		while (srcGPR[i] != -1) {
			if (srcGPR[i] == i) srcGPRcount[i]--;
			i++;
		}
		i = paramStartEXTR;
		while (srcEXTR[i] != -1) {
			if (srcEXTRtype[i]) {
				if (srcEXTR[i] / 2 == i) srcEXTRcount[i]--;
			} else {
				if (srcEXTR[i] == i) srcEXTRcount[i]--;
			}
			i++;
		}
		if (dbg) {
			StdStreams.vrb.print("\tsrcGPR = ");
			for (int k = paramStartGPR; srcGPR[k] != -1; k++) StdStreams.vrb.print(srcGPR[k] + ","); 
			StdStreams.vrb.print("\tsrcGPRcount = ");
			for (int n = 0; n < nofGPR; n++) StdStreams.vrb.print(srcGPRcount[n] + ","); 
			StdStreams.vrb.println();
			StdStreams.vrb.print("\tsrcEXTR = ");
			for (int k = paramStartEXTR; srcEXTR[k] != -1; k++) StdStreams.vrb.print(srcEXTR[k] + ","); 
			StdStreams.vrb.print("\tsrcEXTRtype = ");
			for (int k = paramStartEXTR; srcEXTR[k] != -1; k++) StdStreams.vrb.print(srcEXTRtype[k] + ","); 
			StdStreams.vrb.print("\tsrcEXTRDcount = ");
			for (int k = 0; k < nofEXTR; k++) StdStreams.vrb.print(srcEXTRcount[k] + ","); 
			StdStreams.vrb.println();
		}

		// move registers 
		boolean done = false;
		while (!done) {
			i = paramStartGPR; done = true;
			while (srcGPR[i] != -1) {
				if (i > paramEndGPR) {	// copy to stack
					if (srcGPRcount[i] >= 0) { // check if not done yet
						if (dbg) StdStreams.vrb.println("\tGPR: parameter " + (i-paramStartGPR) + " from register R" + srcGPR[i] + " to stack slot");
						if (srcGPR[i] >= 0x100) {	// copy from stack slot to stack (into parameter area)
							createLSWordImm(code, armLdr, condAlways, scratchReg, stackPtr, code.localVarOffset + 4 * (srcGPR[i] - 0x100), 1, 1, 0);
							createLSWordImm(code, armStr, condAlways, scratchReg, stackPtr, paramOffset + offset, 1, 1, 0);
						} else {
							createLSWordImm(code, armStr, condAlways, srcGPR[i], stackPtr, paramOffset + offset, 1, 1, 0);
							srcGPRcount[srcGPR[i]]--; 
						}
						offset += 4;
						srcGPRcount[i]--; 
						done = false;
					}
				} else {	// copy to register
					if (srcGPRcount[i] == 0) { // check if register no longer used for parameter
						if (dbg) StdStreams.vrb.println("\tGPR: parameter " + (i-paramStartGPR) + " from register R" + srcGPR[i] + " to R" + i);
						if (srcGPR[i] >= 0x100) {	// copy from stack
							createLSWordImm(code, armLdr, condAlways, i, stackPtr, code.localVarOffset + 4 * (srcGPR[i] - 0x100), 1, 1, 0);
						} else {
							createDataProcMovReg(code, armMov, condAlways, i,  srcGPR[i], noShift, 0);
							srcGPRcount[srcGPR[i]]--; 
						}
						srcGPRcount[i]--; 
						done = false;
					}
				}
				i++; 
			}
		}
		done = false;
		while (!done) {
			if (dbg) {
				StdStreams.vrb.print("\tround");
				StdStreams.vrb.print("\tsrcEXTRDcount = ");
				for (int k = 0; k < nofEXTR; k++) StdStreams.vrb.print(srcEXTRcount[k] + ","); 
				StdStreams.vrb.println();
			}
			i = paramStartEXTR; done = true;
			while (srcEXTR[i] != -1) {
				if (i > paramEndEXTR) {	// copy to stack
					if (srcEXTRtype[i]) {	// floats
						if (srcEXTRcount[i] >= 0) { // check if not done yet
							if (dbg) StdStreams.vrb.println("\tEXTR: parameter " + (i-paramStartEXTR) + " from register S" + srcEXTR[i] + " to stack slot");
							if (srcEXTR[i] >= 0x100) {	// copy from stack slot to stack (into parameter area)
								createLSExtReg(code, armVldr, condAlways, scratchRegEXTR, stackPtr, code.localVarOffset + 4 * (srcEXTR[i] - 0x100), true);
								createLSExtReg(code, armVstr, condAlways, scratchRegEXTR, stackPtr, paramOffset + offset, true);
							} else {
								createLSExtReg(code, armVstr, condAlways, srcEXTR[i], stackPtr, paramOffset + offset, true);
								srcEXTRcount[srcEXTR[i]/2]--;
							}
							offset += 8;
							srcEXTRcount[i]--;  
							done = false;
						}
					} else { // doubles
						if (srcEXTRcount[i] >= 0) { // check if not done yet
							if (dbg) StdStreams.vrb.println("\tEXTR: parameter " + (i-paramStartEXTR) + " from register D" + srcEXTR[i] + " to stack slot");
							if (srcEXTR[i] >= 0x100) {	// copy from stack slot to stack (into parameter area)
								createLSExtReg(code, armVldr, condAlways, scratchRegEXTR, stackPtr, code.localVarOffset + 4 * (srcEXTR[i] - 0x100), false);
								createLSExtReg(code, armVstr, condAlways, scratchRegEXTR, stackPtr, paramOffset + offset, false);
							} else {
								createLSExtReg(code, armVstr, condAlways, srcEXTR[i], stackPtr, paramOffset + offset, false);
								srcEXTRcount[srcEXTR[i]]--;
							}
							offset += 8;
							srcEXTRcount[i]--;  
							done = false;
						}
					}
				} else {	// copy to register
					if (srcEXTRtype[i]) {	// floats
						if (srcEXTRcount[i] == 0) { // check if register no longer used for parameter
							if (dbg) StdStreams.vrb.println("\tEXTR: parameter " + (i-paramStartEXTR) + " from register S" + srcEXTR[i] + " to S" + (i*2));
							if (srcEXTR[i] >= 0x100) {	// copy from stack
								createLSExtReg(code, armVldr, condAlways, i, stackPtr, code.localVarOffset + 4 * (srcEXTR[i] - 0x100), true);
							} else {
								createFPdataProc(code, armVmov, condAlways, i*2, 0, srcEXTR[i], true);
								srcEXTRcount[srcEXTR[i]/2]--;
							}
							srcEXTRcount[i]--;  
							done = false;
						}
					} else { // doubles
						if (srcEXTRcount[i] == 0) { // check if register no longer used for parameter
							if (dbg) StdStreams.vrb.println("\tEXTR: parameter " + (i-paramStartEXTR) + " from register D" + srcEXTR[i] + " to D" + i);
							if (srcEXTR[i] >= 0x100) {	// copy from stack
								createLSExtReg(code, armVldr, condAlways, i, stackPtr, code.localVarOffset + 4 * (srcEXTR[i] - 0x100), false);
							} else {
								createFPdataProc(code, armVmov, condAlways, i, 0, srcEXTR[i], false);
								srcEXTRcount[srcEXTR[i]]--;
							}
							srcEXTRcount[i]--;  
							done = false;
						}
					}
				}
				i++; 
			}
		}
		if (dbg) {
			StdStreams.vrb.print("\tsrcGPR = ");
			for (int k = paramStartGPR; srcGPR[k] != -1; k++) StdStreams.vrb.print(srcGPR[k] + ","); 
			StdStreams.vrb.print("\tsrcGPRcount = ");
			for (int n = 0; n < nofGPR; n++) StdStreams.vrb.print(srcGPRcount[n] + ","); 
			StdStreams.vrb.println();
			StdStreams.vrb.print("\tsrcEXTR = ");
			for (int k = paramStartEXTR; srcEXTR[k] != -1; k++) StdStreams.vrb.print(srcEXTR[k] + ","); 
			StdStreams.vrb.print("\tsrcEXTRtype = ");
			for (int k = paramStartEXTR; srcEXTR[k] != -1; k++) StdStreams.vrb.print(srcEXTRtype[k] + ","); 
			StdStreams.vrb.print("\tsrcEXTRDcount = ");
			for (int k = 0; k < nofEXTR; k++) StdStreams.vrb.print(srcEXTRcount[k] + ","); 
			StdStreams.vrb.println();
		}

		// resolve cycles
		if (dbg) StdStreams.vrb.println("\tresolve cycles");
		done = false;
		while (!done) {
			i = paramStartGPR; done = true;
			while (srcGPR[i] != -1) {
				int src = -1;
				if (srcGPRcount[i] == 1) {
					src = i;
					createDataProcMovReg(code, armMov, condAlways, scratchReg,  srcGPR[i], noShift, 0);
					if (dbg) StdStreams.vrb.println("\tGPR: from register " + srcGPR[i] + " to " + scratchReg);
					srcGPRcount[srcGPR[i]]--;
					done = false;
				}
				boolean done1 = false;
				while (!done1) {
					int k = paramStartGPR; done1 = true;
					while (srcGPR[k] != -1) {
						if (srcGPRcount[k] == 0 && k != src) {
							createDataProcMovReg(code, armMov, condAlways, k,  srcGPR[k], noShift, 0);
							if (dbg) StdStreams.vrb.println("\tGPR: from register " + srcGPR[k] + " to " + k);
							srcGPRcount[k]--; srcGPRcount[srcGPR[k]]--; 
							done1 = false;
						}
						k++; 
					}
				}
				if (src != -1) {
					createDataProcMovReg(code, armMov, condAlways, src,  scratchReg, noShift, 0);
					if (dbg) StdStreams.vrb.println("\tGPR: from register " + scratchReg + " to " + src);
					srcGPRcount[src]--;
				}
				i++;
			}
		}
		done = false;
		while (!done) {
			i = paramStartEXTR; done = true;
			while (srcEXTR[i] >= 0) {
				int src = -1;
				if (srcEXTRcount[i] == 1) {
					src = i;
					if (dbg) {
						StdStreams.vrb.println("prepare");
						if (srcEXTRtype[i]) StdStreams.vrb.println("\tEXTR: from register S" + srcEXTR[i] + " to S" + scratchRegEXTR);
						else StdStreams.vrb.println("\tEXTR: from register D" + srcEXTR[i] + " to D" + scratchRegEXTR);
					}
					createFPdataProc(code, armVmov, condAlways, scratchRegEXTR, 0, srcEXTR[i], srcEXTRtype[i]);
					if (srcEXTRtype[i]) srcEXTRcount[srcEXTR[i]/2]--;
					else srcEXTRcount[srcEXTR[i]]--;
					done = false;
				}
				boolean done1 = false;
				while (!done1) {
					int k = paramStartEXTR; done1 = true;
					while (srcEXTR[k] >= 0) {
						if (srcEXTRcount[k] == 0 && k != src) {
							if (dbg) {
								StdStreams.vrb.println("k="+k);
								if (srcEXTRtype[k]) StdStreams.vrb.println("\tEXTR: from register S" + srcEXTR[k] + " to S" + k*2);
								else StdStreams.vrb.println("\tEXTR: from register D" + srcEXTR[k] + " to D" + k);
							}
							if (srcEXTRtype[k]) createFPdataProc(code, armVmov, condAlways, k*2, 0, srcEXTR[k], srcEXTRtype[k]);
							else createFPdataProc(code, armVmov, condAlways, k, 0, srcEXTR[k], srcEXTRtype[k]);
							srcEXTRcount[k]--; 
							if (srcEXTRtype[k]) srcEXTRcount[srcEXTR[k]/2]--;
							else srcEXTRcount[srcEXTR[k]]--;
							done1 = false;
						}
						k++; 
					}
				}
				if (src >= 0) {
					if (dbg) {
						StdStreams.vrb.println("cleanup");
						if (srcEXTRtype[i]) StdStreams.vrb.println("\tEXTR: from register S" + scratchRegEXTR + " to S" + src*2);
						else StdStreams.vrb.println("\tEXTR: from register D" + scratchRegEXTR + " to D" + src);
					}
					if (srcEXTRtype[i]) createFPdataProc(code, armVmov, condAlways, src*2, 0, 0, srcEXTRtype[i]);
					else createFPdataProc(code, armVmov, condAlways, src, 0, 0, srcEXTRtype[i]);
					srcEXTRcount[src]--;
				}
				i++;
			}
		}
		if (dbg) StdStreams.vrb.println("\tdone");
	}

	// copy parameters for method "bCmultianewarray" into parameter registers or onto stack
	private void copyParametersNewArray(Code32 code, SSAValue[] opds) {
//		boolean dbg = true;
		// information about the src registers for parameters of a call to a method 
		int[] srcGPR = new int[nofGPR];	
		int[] srcGPRcount = new int[nofGPR];

		int offset = 0;
		for (int k = 0; k < nofGPR; k++) {srcGPR[k] = -1; srcGPRcount[k] = 0;}

		// get info about in which register parameters are located
		// parameters which go onto the stack are treated equally
		for (int k = 0, kGPR = 0; k < opds.length; k++) {
			int type = opds[k].type & ~(1<<ssaTaFitIntoInt);
			if (type == tLong) {
				srcGPR[kGPR + paramStartGPR + 2] = opds[k].regLong;
				srcGPR[kGPR + 1 + paramStartGPR + 2] = opds[k].reg;
				kGPR += 2;
			} else {
				srcGPR[kGPR + paramStartGPR + 2] = opds[k].reg;
				kGPR++;
			}
		}

		if (dbg) {
			StdStreams.vrb.print("\tsrcGPR = ");
			for (int k = paramStartGPR; srcGPR[k] != -1; k++) StdStreams.vrb.print(srcGPR[k] + ","); 
			StdStreams.vrb.print("\tsrcGPRcount = ");
			for (int k = 0; k < nofGPR; k++) StdStreams.vrb.print(srcGPRcount[k] + ","); 
			StdStreams.vrb.println();
		}

		// count register usage
		int i = paramStartGPR + 2;
		while (srcGPR[i] != -1) {
			if (srcGPR[i] <= topGPR) srcGPRcount[srcGPR[i]]++;
			i++;
		}
		if (dbg) {
			StdStreams.vrb.print("\tsrcGPR = ");
			for (int k = paramStartGPR; srcGPR[k] != -1; k++) StdStreams.vrb.print(srcGPR[k] + ","); 
			StdStreams.vrb.print("\tsrcGPRcount = ");
			for (int k = 0; k < nofGPR; k++) StdStreams.vrb.print(srcGPRcount[k] + ","); 
			StdStreams.vrb.println();
		}
		
		// handle move to itself
		i = paramStartGPR + 2;
		while (srcGPR[i] != -1) {
			if (srcGPR[i] == i) srcGPRcount[i]--;
			i++;
		}
		if (dbg) {
			StdStreams.vrb.print("\tsrcGPR = ");
			for (int k = paramStartGPR; srcGPR[k] != -1; k++) StdStreams.vrb.print(srcGPR[k] + ","); 
			StdStreams.vrb.print("\tsrcGPRcount = ");
			for (int n = 0; n < nofGPR; n++) StdStreams.vrb.print(srcGPRcount[n] + ","); 
			StdStreams.vrb.println();
		}

		// move registers 
		boolean done = false;
		while (!done) {
			i = paramStartGPR + 2; done = true;
			while (srcGPR[i] != -1) {
				if (i > paramEndGPR) {	// copy to stack
					if (srcGPRcount[i] >= 0) { // check if not done yet
						if (dbg) StdStreams.vrb.println("\tGPR: parameter " + (i-paramStartGPR) + " from register R" + srcGPR[i] + " to stack slot");
						if (srcGPR[i] >= 0x100) {	// copy from stack slot to stack (into parameter area)
							createLSWordImm(code, armLdr, condAlways, scratchReg, stackPtr, code.localVarOffset + 4 * (srcGPR[i] - 0x100), 1, 1, 0);
							createLSWordImm(code, armStr, condAlways, scratchReg, stackPtr, paramOffset + offset, 1, 1, 0);
						} else {
							createLSWordImm(code, armStr, condAlways, srcGPR[i], stackPtr, paramOffset + offset, 1, 1, 0);
							srcGPRcount[srcGPR[i]]--; 
						}
						offset += 4;
						srcGPRcount[i]--; 
						done = false;
					}
				} else {	// copy to register
					if (srcGPRcount[i] == 0) { // check if register no longer used for parameter
						if (dbg) StdStreams.vrb.println("\tGPR: parameter " + (i-paramStartGPR) + " from register R" + srcGPR[i] + " to R" + i);
						if (srcGPR[i] >= 0x100) {	// copy from stack
							createLSWordImm(code, armLdr, condAlways, i, stackPtr, code.localVarOffset + 4 * (srcGPR[i] - 0x100), 1, 1, 0);
						} else {
							createDataProcMovReg(code, armMov, condAlways, i,  srcGPR[i], noShift, 0);
							srcGPRcount[srcGPR[i]]--; 
						}
						srcGPRcount[i]--; 
						done = false;
					}
				}
				i++; 
			}
		}
		if (dbg) {
			StdStreams.vrb.print("\tsrcGPR = ");
			for (int k = paramStartGPR; srcGPR[k] != -1; k++) StdStreams.vrb.print(srcGPR[k] + ","); 
			StdStreams.vrb.print("\tsrcGPRcount = ");
			for (int n = 0; n < nofGPR; n++) StdStreams.vrb.print(srcGPRcount[n] + ","); 
			StdStreams.vrb.println();
		}

		// resolve cycles
		if (dbg) StdStreams.vrb.println("\tresolve cycles");
		done = false;
		while (!done) {
			i = paramStartGPR + 2; done = true;
			while (srcGPR[i] != -1) {
				int src = -1;
				if (srcGPRcount[i] == 1) {
					src = i;
					createDataProcMovReg(code, armMov, condAlways, scratchReg,  srcGPR[i], noShift, 0);
					if (dbg) StdStreams.vrb.println("\tGPR: from register " + srcGPR[i] + " to " + scratchReg);
					srcGPRcount[srcGPR[i]]--;
					done = false;
				}
				boolean done1 = false;
				while (!done1) {
					int k = paramStartGPR + 2; done1 = true;
					while (srcGPR[k] != -1) {
						if (srcGPRcount[k] == 0 && k != src) {
							createDataProcMovReg(code, armMov, condAlways, k,  srcGPR[k], noShift, 0);
							if (dbg) StdStreams.vrb.println("\tGPR: from register " + srcGPR[k] + " to " + k);
							srcGPRcount[k]--; srcGPRcount[srcGPR[k]]--; 
							done1 = false;
						}
						k++; 
					}
				}
				if (src != -1) {
					createDataProcMovReg(code, armMov, condAlways, src,  scratchReg, noShift, 0);
					if (dbg) StdStreams.vrb.println("\tGPR: from register " + scratchReg + " to " + src);
					srcGPRcount[src]--;
				}
				i++;
			}
		}
		if (dbg) StdStreams.vrb.println("\tdone");
	}

	// data processing with second operand as immediate value, op in bits 24 to 20
	private void createDataProcImm(Code32 code, int op, int cond, int Rd, int Rn, int imm12) {
		code.instructions[code.iCount] = (cond << 28) | (1 << 25) | op | (Rn << 16) | (Rd << 12) | imm12;
		code.incInstructionNum();
	}

	// data processing with second operand as immediate value, no result (Rd = 0), op in bits 24 to 20, use for TST, TEQ, CMP, CMN
	private void createDataProcCmpImm(Code32 code, int op, int cond, int Rn, int imm12) {
		code.instructions[code.iCount] = (cond << 28) | (1 << 25) | op | (Rn << 16) | imm12;
		code.incInstructionNum();
	}
	
	// data processing with operand as immediate value, no second operand (Rn = 0), op in bits 24 to 20, use for MOV, MVN
	private void createDataProcMovImm(Code32 code, int op, int cond, int Rd, int imm12) {
		code.instructions[code.iCount] = (cond << 28) | (1 << 25) | op | (Rd << 12) | imm12;
		code.incInstructionNum();
	}
	
	// 16 bit immediate load, op in bits 24 to 20, use for MOVW, MOVT
	private void createMovw(Code32 code, int op, int cond, int Rd, int imm16) {
		code.instructions[code.iCount] = (cond << 28) | (1 << 25) | op | (Rd << 12) | ((imm16 & 0xf000) << 4) | (imm16 & 0xfff);
		code.incInstructionNum();
	}

	// data processing with both operands in registers, Rn = 1st op, Rm = 2nd, op in bits 24 to 20
	private void createDataProcReg(Code32 code, int op, int cond, int Rd, int Rn, int Rm, int shiftType, int shiftAmount) {
		code.instructions[code.iCount] = (cond << 28) | op | (Rn << 16) | (Rd << 12) | (shiftAmount << 7) | (shiftType << 5) | Rm;
		code.incInstructionNum();
	}

	// data processing with a single operand in register, no second operand (Rn = 0), op in bits 24 to 20, use for MOV, MVN
	// MOV must be used with shiftType == noShift and shiftAmount == 0
	private void createDataProcMovReg(Code32 code, int op, int cond, int Rd, int Rm, int shiftType, int shiftAmount) {
		if ((Rd == Rm) && (shiftAmount == 0)) return;	// mov Rx, Rx makes no sense	
		if (shiftAmount == 0) code.instructions[code.iCount] = (cond << 28) | op | (Rd << 12) | Rm;	// shifting with imm=0 is not valid
		else code.instructions[code.iCount] = (cond << 28) | op | (Rd << 12) | (shiftAmount << 7) | (shiftType << 5) | Rm;
		code.incInstructionNum();
	}

	// data processing with a single operand in register, no second operand (Rn = 0), op in bits 24 to 20, use for RRX
	private void createDataProcRRX(Code32 code, int op, int cond, int Rd, int Rm) {
		code.instructions[code.iCount] = (cond << 28) | op | (Rd << 12) | Rm;
		code.incInstructionNum();
	}

	// data processing with second operand in register, no result (Rd = 0), Rn = 1st op, Rm = 2nd op, op in bits 24 to 20, use for TST, TEQ, CMP, CMN
	// calculates Rn - Rm
	private void createDataProcCmpReg(Code32 code, int op, int cond, int Rn, int Rm, int shiftType, int shiftAmount) {
		code.instructions[code.iCount] = (cond << 28) | op | (Rn << 16) | (shiftAmount << 7) | (shiftType << 5) | Rm;
		code.incInstructionNum();
	}

	// data processing with operand in register shifted by immediate, op in bits 24 to 20, use for LSL, LSR, ASR, RRX, ROR
	private void createDataProcShiftImm(Code32 code, int op, int cond, int Rd, int Rm, int shiftAmount) {
		code.instructions[code.iCount] = (cond << 28) | op | (Rd << 12) | (shiftAmount << 7) | Rm;
		code.incInstructionNum();
	}

	// data processing with third operand in register denoting shift, Rn = 1st op, Rm = 2nd op, Rs = 3rd, op in bits 24 to 20
	private void createDataProcRegShiftReg(Code32 code, int op, int cond, int Rd, int Rn, int Rm, int shiftType, int Rs) {
		code.instructions[code.iCount] = (cond << 28) | op | (Rn << 16) | (Rd << 12) | (Rs << 8) | (shiftType << 5) | (1 << 4) | Rm;
		code.incInstructionNum();
	}
	
	// data processing with third operand in register denoting shift, no result (Rd = 0), Rn = 1st op, Rm = 2nd op, Rs = 3rd, op in bits 24 to 20, use with TST, TEQ, CMP, CMN
	private void createDataProcCmpRegShiftReg(Code32 code, int op, int cond, int Rn, int Rm, int shiftType, int Rs) {
		code.instructions[code.iCount] = (cond << 28) | op | (Rn << 16) | (Rs << 8) | (shiftType << 5) | (1 << 4) | Rm;
		code.incInstructionNum();
	}

	// data processing with third operand in register denoting shift, no 1st op, op in bits 24 to 20, use for MVN
	private void createDataProcMovShiftReg(Code32 code, int op, int cond, int Rd, int Rm, int shiftType, int Rs) {
		code.instructions[code.iCount] = (cond << 28) | op | (Rd << 12) | (Rs << 8) | (shiftType << 5) | (1 << 4) | Rm;
		code.incInstructionNum();
	}

	// data processing with second operand in register denoting shift, op in bits 24 to 20, use for LSL, LSR, ASR, ROR
	private void createDataProcShiftReg(Code32 code, int op, int cond, int Rd, int Rn, int Rm) {
		code.instructions[code.iCount] = (cond << 28) | op | (Rd << 12) | (Rm << 8) | (1 << 4) | Rn;
		code.incInstructionNum();
	}

	// multiply, op in bits 23 to 20, use for MUL
	private void createMul(Code32 code, int op, int cond, int Rd, int Rn, int Rm) {
		code.instructions[code.iCount] = (cond << 28) | op | (Rd << 16) | (Rm << 8) | (0x9 << 4) | Rn;
		code.incInstructionNum();
	}
	
	// multiply and accumulate, op in bits 23 to 20, use for MLA, MLS
	private void createMulAcc(Code32 code, int op, int cond, int Rd, int Ra, int Rn, int Rm) {
		code.instructions[code.iCount] = (cond << 28) | op | (Rd << 16) | (Ra << 12) | (Rm << 8) | (0x9 << 4) | Rn;
		code.incInstructionNum();
	}
	
	// multiply with long result, op in bits 23 to 20, use for UMULL, UMLAL, SMULL, SMLAL
	private void createMulLong(Code32 code, int op, int cond, int RdHigh, int RdLow, int Rn, int Rm) {
		code.instructions[code.iCount] = (cond << 28) | op | (RdHigh << 16) | (RdLow << 12) | (Rm << 8) | (0x9 << 4) | Rn;
		code.incInstructionNum();
	}
	
	// packing, unpacking (sign extension), reversal
	private void createPacking(Code32 code, int opCode, int cond, int Rd, int Rm) {
		code.instructions[code.iCount] = (cond << 28) | opCode | (0xd << 23) | (Rd << 12) | (1 << 4) | Rm;
		code.incInstructionNum();
	}
	
	// media instructions, use for SBFX, UBFX
	private void createMedia(Code32 code, int op, int cond, int Rd, int Rn, int lsb, int width) {
		code.instructions[code.iCount] = (cond << 28) | op | ((width-1) << 16) | (Rd << 12) | (lsb << 7) | Rn;
		code.incInstructionNum();
	}
		
	// branch (immediate) relative, +-32M (B, BL, BLX(imm)) 
	private void createBranchImm(Code32 code, int op, int cond, int imm24) {
		code.instructions[code.iCount] = (cond << 28) | op | (imm24 & 0xffffff);
		code.incInstructionNum();
	}
	
	// branch (register) absolute, whole range (including BLX(reg), BX, BXJ)
	private void createBranchReg(Code32 code, int op, int cond, int Rm) {
		code.instructions[code.iCount] = (cond << 28) | op | (Rm << 0);
		code.incInstructionNum();
	}
	
	// load/store word and unsigned byte (immediate)	(LDR, LDRB, STR, STRB, LDRH, LDRSH, LDRSB, STRH)
	// P = 1 -> preindexed, U = 1 -> add offset, W = 1 -> write back
	private void createLSWordImm(Code32 code, int opCode, int cond, int Rt, int Rn, int imm12, int P, int U, int W) {
		if (opCode == armLdr || opCode == armLdrb || opCode == armStr || opCode == armStrb)
			code.instructions[code.iCount] = (cond << 28) | opCode | (Rt << 12) | (Rn << 16) | (imm12 << 0) | (P << 24) | (U << 23) | (W << 21);
		else	// extra load / store
			code.instructions[code.iCount] = (cond << 28) | opCode | (Rt << 12) | (Rn << 16) | ((imm12 & 0xf0) << 4) | (imm12 & 0xf) | (P << 24) | (U << 23) | (W << 21) | (1 << 22);
		code.incInstructionNum();
	}

	// ...(LDR, LDRB	:	(literal))
	private void createLSWordLit(Code32 code, int opCode, int cond, int Rt, int imm12, int P, int U, int W) {
		code.instructions[code.iCount] = (cond << 28) | opCode | (Rt << 12) | (0xf << 16) | (imm12 << 0) | (P << 24) | (U << 23) | (W << 21);
		code.incInstructionNum();
	}
	// load/store word and unsigned byte (register)	(LDR, LDRB, STR, STRB, LDRH, LDRSH, LDRSB, STRH)
	// LDRH, LDRSH, LDRSB, STRH do not allow register shifts 
	// P = 1 -> preindexed, U = 1 -> add offset, W = 1 -> write back
	private void createLSWordReg(Code32 code, int opCode, int cond, int Rt, int Rn, int Rm, int shiftType, int shiftAmount, int P, int U, int W) {
		if (opCode == armLdr || opCode == armLdrb || opCode == armStr || opCode == armStrb)
			code.instructions[code.iCount] = (cond << 28) | opCode | (Rt << 12) | (Rn << 16) | (Rm << 0) | (shiftType << 5) | (shiftAmount << 7) | (P << 24) | (U << 23) | (W << 21) | (1 << 25);
		else	// extra load / store
			code.instructions[code.iCount] = (cond << 28) | opCode | (Rt << 12) | (Rn << 16) | (Rm << 0) | (P << 24) | (U << 23) | (W << 21);
		code.incInstructionNum();
	}
	
	// block data transfer	(LDMxx, STMxx)
	private void createBlockDataTransfer(Code32 code, int opCode, int cond, int Rn, int regList, int W) {
		code.instructions[code.iCount] = (cond << 28) | opCode | (Rn << 16) | regList | (W << 21);
		code.incInstructionNum();
	}
	
	// block data transfer	(POP, PUSH)
	private void createBlockDataTransfer(Code32 code, int opCode, int cond, int regList) {
		createBlockDataTransfer(code, opCode, cond, stackPtr, regList, 1);
	}
	
	// block data transfer	(VPOP, VPUSH)
	private void createBlockDataTransferExtr(Code32 code, int opCode, int cond, int Vd, int nof, boolean single) {
		if (single) code.instructions[code.iCount] = (cond << 28) | opCode | (((Vd>>1)&0xf) << 12) | ((Vd&1) << 22) | nof;
		else code.instructions[code.iCount] = (cond << 28) | opCode | (1 << 8) | ((Vd&0xf) << 12) | ((Vd>>4) << 22) | nof * 2;
		code.incInstructionNum();
	}
	
	// load/store extension registers (VLDR, VSTR)
	private void createLSExtReg(Code32 code, int opCode, int cond, int Vd, int Rn, int imm, boolean single) {
		imm >>= 2;	// immediates are multiples of 4
		code.instructions[code.iCount] = (cond << 28) | opCode | (Rn << 16) | (imm & 0xff) | ((imm>=0)?(1<<23):0);
		if (single) code.instructions[code.iCount] |= (((Vd>>1)&0xf) << 12) | ((Vd&1) << 22);
		else code.instructions[code.iCount] |= (1 << 8) | ((Vd&0xf) << 12) | ((Vd>>4) << 22);
		code.incInstructionNum();
	}

	// floating point data processing (VADD, VSUB, VMUL, VDIV, VMLA, VMOV (moving between ext. regs))
	private void createFPdataProc(Code32 code, int opCode, int cond, int Vd, int Vn, int Vm, boolean single) {
		if (opCode == armVmov && (Vd == Vm)) return;	// mov Vx, Vx makes no sense	
		code.instructions[code.iCount] = (cond << 28) | opCode;
		if (single) code.instructions[code.iCount] |= (((Vd>>1)&0xf) << 12) | ((Vd&1) << 22) | (((Vn>>1)&0xf) << 16) | ((Vn&1) << 7) | ((Vm>>1)&0xf) | ((Vm&1) << 5);
		else code.instructions[code.iCount] |= (1 << 8) | ((Vd&0xf) << 12) | ((Vd>>4) << 22) | ((Vn&0xf) << 16) | ((Vn>>4) << 7) | (Vm&0xf) | ((Vm>>4) << 5);
		code.incInstructionNum();
	}

	// floating point data processing (VNEG, VCMP, VCMPE)
	private void createFPdataProc(Code32 code, int opCode, int cond, int Vd, int Vm, boolean single) {
		code.instructions[code.iCount] = (cond << 28) | opCode;
		if (single) code.instructions[code.iCount] |= (((Vd>>1)&0xf) << 12) | ((Vd&1) << 22) | ((Vm>>1)&0xf) | ((Vm&1) << 5);
		else code.instructions[code.iCount] |= (1 << 8) | ((Vd&0xf) << 12) | ((Vd>>4) << 22) | (Vm&0xf) | ((Vm>>4) << 5);
		code.incInstructionNum();
	}

	// floating point data processing (VCVT, between floating point and integer)
	private void createFPdataConv(Code32 code, int opCode, int cond, int Vd, int Vm, int opc, int op, boolean single) {
		code.instructions[code.iCount] = (cond << 28) | opCode;
		if ((opc & 4) != 0) {	// to integer
			if (single) code.instructions[code.iCount] |= (((Vd>>1)&0xf) << 12) | ((Vd&1) << 22) | (opc << 16) | (op << 7) | ((Vm>>1)&0xf) | ((Vm&1) << 5);
			else code.instructions[code.iCount] |= (1 << 8) | (((Vd>>1)&0xf) << 12) | ((Vd&1) << 22) | (opc << 16) | (op << 7) | (Vm&0xf) | ((Vm>>4) << 5);
		} else {	// to floating point
			if (single) code.instructions[code.iCount] |= (((Vd>>1)&0xf) << 12) | ((Vd&1) << 22) | (opc << 16) | (op << 7) | ((Vm>>1)&0xf) | ((Vm&1) << 5);
			else code.instructions[code.iCount] |= (1 << 8) | ((Vd&0xf) << 12) | ((Vd>>4) << 22) | (opc << 16) | (op << 7) | ((Vm>>1)&0xf) | ((Vm&1) << 5);
		}
		code.incInstructionNum();
	}

	// floating point data processing (VCVT, between single and double precision)
	private void createFPdataConvPrec(Code32 code, int opCode, int cond, int Vd, int Vm, boolean toSingle) {
		code.instructions[code.iCount] = (cond << 28) | opCode | (7 << 16);
		if (toSingle) code.instructions[code.iCount] |= (1 << 8) | (((Vd>>1)&0xf) << 12) | ((Vd&1) << 22) | (Vm&0xf) | ((Vm>>4) << 5);
		else code.instructions[code.iCount] |= ((Vd&0xf) << 12) | ((Vd>>4) << 22) | ((Vm>>1)&0xf) | ((Vm&1) << 5);
		code.incInstructionNum();
	}

	// floating point register moving between arm and fp registers (VMOV)
	// important: if toArm -> Vm is source register and Rt is destination register
	private void createFPregMove(Code32 code, int opCode, int cond, int Vm, int Rt, int Rt2, boolean toArm, boolean single) {
		code.instructions[code.iCount] = (cond << 28) | opCode;
		if (single) code.instructions[code.iCount] |= (((Vm>>1)&0xf) << 16) | ((Vm&1) << 7) | (Rt << 12);
		else code.instructions[code.iCount] |= (Vm&0xf) | ((Vm>>4) << 5) | (Rt << 12) | (Rt2 << 16);
		if (toArm) code.instructions[code.iCount] |= (1 << 20);
		code.incInstructionNum();
	}

	// coprocessor (MCR, MRC)
	private void createCoProc(Code32 code, int opCode, int cond, int coproc, int opc1, int Rt, int CRn, int CRm, int opc2) {
		code.instructions[code.iCount] = (cond << 28) | opCode | (coproc << 8) | (opc1 << 21) | (Rt << 12) | (CRn << 16) | CRm | (opc2 << 5);
		code.incInstructionNum();
	}

	// coprocessor FPU (VMRS, VMSR)
	private void createCoProcFPU(Code32 code, int opCode, int cond, int Rt) {
		code.instructions[code.iCount] = (cond << 28) | opCode | (Rt << 12);
		code.incInstructionNum();
	}

	// count leading zeroes (CLZ)
	private void createClz(Code32 code, int opCode, int cond, int Rd, int Rm) {
		code.instructions[code.iCount] = (cond << 28) | opCode | (Rd << 12) | Rm;
		code.incInstructionNum();
	}
	
	// supervisor call (SVC)
	private void createSvc(Code32 code, int opCode, int cond, int val) {
		code.instructions[code.iCount] = (cond << 28) | opCode | val;
		code.incInstructionNum();
	}

	// floating point data processing, immediate (VMOV), could not make it work, gave strange constants
	private void createFPdataProcImm(Code32 code, int opCode, int cond, int Vd, int imm, boolean single) {
		code.instructions[code.iCount] = (cond << 28) | opCode;
		if (single) code.instructions[code.iCount] |= (((Vd>>1)&0xf) << 12) | ((Vd&1) << 22) | ((imm&0xf0) << 12) | (imm&0xf);
		else code.instructions[code.iCount] |= (1 << 8) | ((Vd&0xf) << 12) | ((Vd>>4) << 22) | ((imm&0xf0) << 12) | (imm&0xf);
		code.incInstructionNum();
	}

	private void createIpat(Code32 code, int pat) {
		code.instructions[code.iCount] = pat;
		code.incInstructionNum();
	}

	/*
	 * loads a constant (up to 32 Bit) with immediate instructions
	 */
	private void loadConstant(Code32 code, int reg, int val) {
		int low = val & 0xffff;
		int high = (val >> 16) & 0xffff;
		if (low != 0 && high != 0) {
			createMovw(code, armMovw, condAlways, reg, low);
			createMovw(code, armMovt, condAlways, reg, high);
		} else if (low == 0 && high != 0) {
			createMovw(code, armMovw, condAlways, reg, 0);
			createMovw(code, armMovt, condAlways, reg, high);
		} else if (low != 0 && high == 0) {
			createMovw(code, armMovw, condAlways, reg, low);
		} else createMovw(code, armMovw, condAlways, reg, 0);
	}
	
	/*
	 * loads the address of a method or a class field into a register
	 * both instructions will later be fixed
	 */
	private void loadConstantAndFixup(Code32 code, int reg, Item item) {
		if (code.lastFixup < 0 || code.lastFixup > 0xffff) {ErrorReporter.reporter.error(602); return;}
		createMovw(code, armMovw, condAlways, reg, code.lastFixup);
		createMovw(code, armMovt, condAlways, reg, 0);
		code.lastFixup = code.iCount - 2;
		code.fixups[code.fCount] = item;
		code.fCount++;
		int len = code.fixups.length;
		if (code.fCount == len) {
			Item[] newFixups = new Item[2 * len];
			for (int k = 0; k < len; k++)
				newFixups[k] = code.fixups[k];
			code.fixups = newFixups;
		}		
	}
	
	/*
	 * inserts a BL instruction
	 * it's offset will later be fixed
	 */
	private void insertBLAndFixup(Code32 code, Item item) {
		if (code.lastFixup < 0 || code.lastFixup > 0xffff) {ErrorReporter.reporter.error(602); return;}
		createBranchImm(code, armBl, condAlways, code.lastFixup);
		code.lastFixup = code.iCount - 1;
		code.fixups[code.fCount] = item;
		code.fCount++;
		int len = code.fixups.length;
		if (code.fCount == len) {
			Item[] newFixups = new Item[2 * len];
			for (int k = 0; k < len; k++)
				newFixups[k] = code.fixups[k];
			code.fixups = newFixups;
		}		
	}
	
	public void doFixups(Code32 code) {
		if (dbg) StdStreams.vrb.println("\t\tlastFixup at instruction " + code.lastFixup);		
		int currInstr = code.lastFixup;
		int currFixup = code.fCount - 1;
		while (currFixup >= 0) {
			Item item = code.fixups[currFixup];
			int addr;
			if (item == null) // item is null, if constant null is loaded (aconst_null) 
				addr = 0;
			else 
				addr = item.address;
			if (dbg) { 
				if (item == null) StdStreams.vrb.print("\tnull"); 
				else StdStreams.vrb.println("\t\t" + item.name + " at 0x" + Integer.toHexString(addr) + " currInstr=" + currInstr);
			}
			int[] instrs = code.instructions;
			int nextInstr;
			if (item instanceof Method) {
				if (((instrs[currInstr] >> 24) & 0xe) == 0xa) { // must be a branch to a method (BL)
					nextInstr = instrs[currInstr] & 0xffff;
					int branchOffset = ((addr - code.ssa.cfg.method.address) >> 2) - currInstr - 2;	// -2: account for pipelining
					assert (branchOffset < 0x1000000) && (branchOffset > 0xff000000);
					if ((branchOffset >= 0x1000000) || (branchOffset <= 0xff000000)) {ErrorReporter.reporter.error(650); return;}
					instrs[currInstr] = (instrs[currInstr] & 0xff000000) | (branchOffset & 0xffffff);
				} else {	// load the address of a method into a register
					nextInstr = (instrs[currInstr] & 0xfff) + ((instrs[currInstr] & 0xf0000) >> 4);
					int val = item.address & 0xffff;
					instrs[currInstr] = (instrs[currInstr] & 0xfff0f000) | ((val & 0xf000) << 4) | (val & 0xfff);
					val = (item.address >> 16) & 0xffff;
					instrs[currInstr+1] = (instrs[currInstr+1] & 0xfff0f000) | ((val & 0xf000) << 4) | (val & 0xfff);				
				}
			} else {	// must be a load / store instruction
				nextInstr = (instrs[currInstr] & 0xfff) + ((instrs[currInstr] & 0xf0000) >> 4);
				int val = item.address & 0xffff;
				instrs[currInstr] = (instrs[currInstr] & 0xfff0f000) | ((val & 0xf000) << 4) | (val & 0xfff);
				val = (item.address >> 16) & 0xffff;
				instrs[currInstr+1] = (instrs[currInstr+1] & 0xfff0f000) | ((val & 0xf000) << 4) | (val & 0xfff);
			}
			currInstr = nextInstr;
			currFixup--;
		}
		// fix addresses of exception information
		if (code.ssa == null) return;	// compiler specific subroutines have no unwinding or exception table
		if ((code.ssa.cfg.method.accAndPropFlags & (1 << dpfExcHnd)) != 0) return;	// exception methods have no unwinding or exception table
		if (dbg) StdStreams.vrb.print("\n\tFixup of exception table for method: " + code.ssa.cfg.method.owner.name + "." + code.ssa.cfg.method.name +  code.ssa.cfg.method.methDescriptor + "\n");		
		currInstr = code.excTabCount;
		int count = 0;
		while (code.instructions[currInstr] != 0xffffffff) {
			SSAInstruction ssaInstr = code.ssa.searchBca(code.instructions[currInstr]);	
			assert ssaInstr != null;
			code.instructions[currInstr++] = code.ssa.cfg.method.address + ssaInstr.machineCodeOffset * 4;	// start
			
			ssaInstr = code.ssa.searchBca(code.instructions[currInstr]);	
			assert ssaInstr != null;
			code.instructions[currInstr++] = code.ssa.cfg.method.address + ssaInstr.machineCodeOffset * 4;	// end
			
			ExceptionTabEntry[] tab = code.ssa.cfg.method.exceptionTab;
			assert tab != null;
			ExceptionTabEntry entry = tab[count];
			assert entry != null;
			if (entry.catchType != null) code.instructions[currInstr++] = entry.catchType.address;	// type 
			else code.instructions[currInstr++] = 0;	// finally 
			
			ssaInstr = code.ssa.searchBca(code.instructions[currInstr] + 1);	// add 1, as first store is omitted	
			assert ssaInstr != null;
			code.instructions[currInstr++] = code.ssa.cfg.method.address + ssaInstr.machineCodeOffset * 4;	// handler
//			currInstr++; //muss wieder weg!!!!!!!!!!!!!!!!!!!!! TODO
			count++;
		}
		// fix addresses of variable and constant segment
		currInstr++;
		Class clazz = code.ssa.cfg.method.owner;
		code.instructions[currInstr] = clazz.varSegment.address + clazz.varOffset;
	}

	private void insertProlog(Code32 code, int stackSize) {
		if (dbg) StdStreams.vrb.println("prolog: nofMoveGPR=" + nofMoveGPR + " nofMoveFPR=" + nofMoveFPR);
		code.iCount = 0;	
		createDataProcMovReg(code, armMov, condAlways, scratchReg, stackPtr, noShift, 0);	// make copy for back trace
		int regList = 1 << LR;
		if (nofNonVolGPR > 0) 
			for (int i = 0; i < nofNonVolGPR; i++) regList += 1 << (topGPR - i); 
		createBlockDataTransfer(code, armPush, condAlways, regList);	// store LR and nonvolatiles
		// enFloatsInExc could be true, even if this is no exception method
		// such a case arises when this method is called from within an exception method
//		if (enFloatsInExc) {
//			createIrD(code, ppcMfmsr, 0);
//			createIrArSuimm(code, ppcOri, 0, 0, 0x2000);
//			createIrS(code, ppcMtmsr, 0);
//			createIrS(code, ppcIsync, 0);	// must context synchronize after setting of FP bit
//		}
//		int offset = FPRoffset;
		if (nofNonVolEXTRD > 0) {	// store nonvolatiles EXTRD 
			if (nofNonVolEXTRD <= 16)
				createBlockDataTransferExtr(code, armVpush, condAlways, (topEXTR - nofNonVolEXTRD + 1), nofNonVolEXTRD, false);
			else {
				createBlockDataTransferExtr(code, armVpush, condAlways, 16, 16, false);
				createBlockDataTransferExtr(code, armVpush, condAlways, (topEXTR - nofNonVolEXTRD + 1), nofNonVolEXTRD - 16, false);			
			}
		}
		if (nofNonVolEXTRS > 0) {	// store nonvolatiles EXTRS
			if (nofNonVolEXTRS <= 16)
				createBlockDataTransferExtr(code, armVpush, condAlways, (topEXTR - nofNonVolEXTRS + 1), nofNonVolEXTRS, true);
			else {
				createBlockDataTransferExtr(code, armVpush, condAlways, 16, 16, true);
				createBlockDataTransferExtr(code, armVpush, condAlways, (topEXTR - nofNonVolEXTRS + 1), nofNonVolEXTRS - 16, true);			
			}
		}
//		if (enFloatsInExc) {
//			for (int i = 0; i < nonVolStartFPR; i++) {	// save volatiles
//				createIrSrAd(code, ppcStfd, i, stackPtr, offset);
//				offset += 8;
//			}
//			createIrD(code, ppcMffs, 0);
//			createIrSrAd(code, ppcStfd, 0, stackPtr, offset);
//		}
		int localStorage = stackSize - (8 + nofNonVolGPR * 4 + nofNonVolEXTRD * 8 + nofNonVolEXTRS * 4);
		// add space for locals, parameters, and storage for auxiliary registers for invokeinterface
		if (localStorage > 0) createDataProcImm(code, armSub, condAlways, stackPtr, stackPtr, localStorage);
		createBlockDataTransfer(code, armPushSingle, condAlways, scratchReg << 12);	// store back trace
		if (dbg) {
			StdStreams.vrb.print("\tmoveGPRsrc = ");
			for (int i = 0; i < nofMoveGPR; i++) StdStreams.vrb.print(moveGPRsrc[i] + ","); 
			StdStreams.vrb.print("\tmoveGPRdst = ");
			for (int i = 0; i < nofMoveGPR; i++) StdStreams.vrb.print(moveGPRdst[i] + ","); 
			StdStreams.vrb.println();
			StdStreams.vrb.print("\tmoveFPRsrc = ");
			for (int i = 0; i < nofMoveFPR; i++) StdStreams.vrb.print(moveFPRsrc[i] + ","); 
			StdStreams.vrb.print("\tmoveFPRtype = ");
			for (int i = 0; i < nofMoveFPR; i++) StdStreams.vrb.print(moveFPRtype[i] + ","); 
			StdStreams.vrb.print("\tmoveFPRdst = ");
			for (int i = 0; i < nofMoveFPR; i++) StdStreams.vrb.print(moveFPRdst[i] + ","); 
			StdStreams.vrb.println();
		}
		int offset = 0;
		for (int i = 0; i < nofMoveGPR; i++) {
			if (moveGPRsrc[i]+paramStartGPR <= paramEndGPR) {// copy from parameter register
				if (dbg) StdStreams.vrb.println("Prolog: copy parameter " + moveGPRsrc[i] + " into R" + moveGPRdst[i]);
				if (moveGPRdst[i] < 0x100)	// to other register
					createDataProcMovReg(code, armMov, condAlways, moveGPRdst[i], moveGPRsrc[i]+paramStartGPR, noShift, 0);
				else 	// to stack slot (locals)
					createLSWordImm(code, armStr, condAlways, moveGPRsrc[i]+paramStartGPR, stackPtr, code.localVarOffset + 4 * (moveGPRdst[i] - 0x100), 1, 1, 0);
			} else { // copy from stack slot (parameters)
				if (dbg) StdStreams.vrb.println("\tProlog: copy parameter " + moveGPRsrc[i] + " from stack slot into R" + moveGPRdst[i]);
				if (dbg) StdStreams.vrb.println("\tstackSize=" + stackSize);
				if (dbg) StdStreams.vrb.println("\tparamOffset=" + paramOffset);
				if (dbg) StdStreams.vrb.println("\toffset=" + offset);
				if (moveGPRdst[i] < 0x100)	// to register
					createLSWordImm(code,armLdr, condAlways, moveGPRdst[i], stackPtr, stackSize + paramOffset + offset, 1, 1, 0);
				else { 	// to stack slot (locals)
					createLSWordImm(code, armLdr, condAlways, scratchReg, stackPtr, stackSize + paramOffset + offset, 1, 1, 0);
					createLSWordImm(code, armStr, condAlways, scratchReg, stackPtr, code.localVarOffset + 4 * (moveGPRdst[i] - 0x100), 1, 1, 0);
				}	
				offset += 4;
			}
		}
		for (int i = 0; i < nofMoveFPR; i++) {
			if (moveFPRsrc[i]+paramStartEXTR <= paramEndEXTR) {// copy from parameter register
				if (moveFPRdst[i] < 0x100) {
					if (moveFPRtype[i]) {
						if (dbg) StdStreams.vrb.println("Prolog: copy parameter from S" + (moveFPRsrc[i]+paramStartEXTR) * 2 + " into S" + moveFPRdst[i]);
						createFPdataProc(code, armVmov, condAlways, moveFPRdst[i], 0, (moveFPRsrc[i]+paramStartEXTR) * 2, true);
					} else {
						if (dbg) StdStreams.vrb.println("Prolog: copy parameter from D" + moveFPRsrc[i]+paramStartEXTR + " into D" + moveFPRdst[i]);
						createFPdataProc(code, armVmov, condAlways, moveFPRdst[i], 0, moveFPRsrc[i]+paramStartEXTR, false);
					}
				} else {	// copy to stack slot (locals)
					if (moveFPRtype[i]) { 
						if (dbg) StdStreams.vrb.println("Prolog: copy parameter from S" + (moveFPRsrc[i]+paramStartEXTR) * 2 + " to stack slot " + moveFPRdst[i]);
						createLSExtReg(code, armVstr, condAlways, (moveFPRsrc[i]+paramStartEXTR) * 2, stackPtr, code.localVarOffset + 4 * (moveFPRdst[i] - 0x100), true);
					} else {
						if (dbg) StdStreams.vrb.println("Prolog: copy parameter from D" + (moveFPRsrc[i]+paramStartEXTR) + " to stack slot " + moveFPRdst[i]);
						createLSExtReg(code, armVstr, condAlways, moveFPRsrc[i]+paramStartEXTR, stackPtr, code.localVarOffset + 4 * (moveFPRdst[i] - 0x100), false);
					}
				}
			} else { // copy from stack slot (parameters)
				if (dbg) StdStreams.vrb.println("Prolog: copy parameter from stack into D" + moveFPRdst[i]);
				if (moveFPRdst[i] < 0x100)
					createLSExtReg(code, armVldr, condAlways, moveFPRdst[i], stackPtr, stackSize + paramOffset + offset, moveFPRtype[i]);
				else {
					createLSExtReg(code, armVldr, condAlways, scratchReg, stackPtr, stackSize + paramOffset + offset, false);
					createLSExtReg(code, armVstr, condAlways, scratchReg, stackPtr, code.localVarOffset + 4 * (moveFPRdst[i] - 0x100), false);
				}
				offset += 8;
			}
		}
		if (dbg) StdStreams.vrb.println("prolog done");
	}

	private void insertEpilog(Code32 code, int stackSize) {
		int epilogStart = code.iCount;
		int localStorage = stackSize - (4 + nofNonVolGPR * 4 + nofNonVolEXTRD * 8 + nofNonVolEXTRS * 4);
		createDataProcImm(code, armAdd, condAlways, stackPtr, stackPtr, localStorage);
//		int offset = GPRoffset - 8;
//		if (enFloatsInExc) {
//			createIrDrAd(ppcLfd, 0, stackPtr, offset);
//			createIFMrB(ppcMtfsf, 0xff, 0);
//			offset -= 8;
//			for (int i = nonVolStartFPR - 1; i >= 0; i--) {
//				createIrDrAd(ppcLfd, i, stackPtr, offset);
//				offset -= 8;
//			}
//		}
		if (nofNonVolEXTRS > 0) {
			if (nofNonVolEXTRS <= 16)
				createBlockDataTransferExtr(code, armVpop, condAlways, (topEXTR - nofNonVolEXTRS + 1), nofNonVolEXTRS, true);
			else {
				createBlockDataTransferExtr(code, armVpop, condAlways, (topEXTR - nofNonVolEXTRS + 1), nofNonVolEXTRS - 16, true);			
				createBlockDataTransferExtr(code, armVpop, condAlways, 16, 16, true);
			}
		}
		if (nofNonVolEXTRD > 0) {
			if (nofNonVolEXTRD <= 16)
				createBlockDataTransferExtr(code, armVpop, condAlways, (topEXTR - nofNonVolEXTRD + 1), nofNonVolEXTRD, false);
			else {
				createBlockDataTransferExtr(code, armVpop, condAlways, (topEXTR - nofNonVolEXTRD + 1), nofNonVolEXTRD - 16, false);			
				createBlockDataTransferExtr(code, armVpop, condAlways, 16, 16, false);
			}
		}
		int regList = 1 << PC;
		if (nofNonVolGPR > 0)
			for (int i = 0; i < nofNonVolGPR; i++) regList += 1 << (topGPR - i); 
		createBlockDataTransfer(code, armPop, condAlways, regList);
		
		createIpat(code, (-(code.iCount-epilogStart)*4) & 0xff);
		code.excTabCount = code.iCount;
		ExceptionTabEntry[] tab = code.ssa.cfg.method.exceptionTab;
		if (tab != null) {
			for (int i = 0; i < tab.length; i++) {
				ExceptionTabEntry entry = tab[i];
				createIpat(code, entry.startPc);
				createIpat(code, entry.endPc);
				if (entry.catchType != null) createIpat(code, entry.catchType.address); else createIpat(code, 0);
				createIpat(code, entry.handlerPc);
			}
		}
		createIpat(code, 0xffffffff);	// end of exception information
		createIpat(code, 0);	// address of variable segment 
//		createIpat(code, 0);	// address of constant segment 		
	}

	private void insertPrologException(Code32 code, int stackSize) {
		code.iCount = 0;
		createBlockDataTransfer(code, armPush, condAlways, 0x5fff);
//		if (enFloatsInExc) {
//			createIrD(ppcMfmsr, 0);
//			createIrArSuimm(ppcOri, 0, 0, 0x2000);
//			createIrS(ppcMtmsr, 0);
//			createIrS(ppcIsync, 0);	// must context synchronize after setting of FP bit
//			int offset = FPRoffset;
//			if (nofNonVolFPR > 0) {
//				for (int i = 0; i < nofNonVolFPR; i++) {
//					createIrSrAd(ppcStfd, topFPR-i, stackPtr, offset);
//					offset += 8;
//				}
//			}
//			for (int i = 0; i < nonVolStartFPR; i++) {
//				createIrSrAd(ppcStfd, i, stackPtr, offset);
//				offset += 8;
//			}
//			createIrD(ppcMffs, 0);
//			createIrSrAd(ppcStfd, 0, stackPtr, offset);
//		}
	}

	private void insertEpilogException(Code32 code, int stackSize) {
//		int offset = GPRoffset - 8;
//		if (enFloatsInExc) {
//			createIrDrAd(ppcLfd, 0, stackPtr, offset);
//			createIFMrB(ppcMtfsf, 0xff, 0);
//			offset -= 8;
//			for (int i = nonVolStartFPR - 1; i >= 0; i--) {
//				createIrDrAd(ppcLfd, i, stackPtr, offset);
//				offset -= 8;
//			}
//		}
//		if (nofNonVolFPR > 0) {
//			for (int i = nofNonVolFPR - 1; i >= 0; i--) {
//				createIrDrAd(ppcLfd, topFPR-i, stackPtr, offset);
//				offset -= 8;
//			}
//		}
		createBlockDataTransfer(code, armPop, condAlways, 0x5fff);
//		createBranchImm(code, armB, condAlways, -2);
		createDataProcImm(code, armSubs, condAlways, PC, LR, 4);
	}

	int regAux1 = paramEndGPR; // use parameter registers for interface delegation methods
	int regAux2 = paramEndGPR - 1; // use parameter registers
	int regAux3 = paramEndGPR - 2; // use parameter registers
	
	public void generateCompSpecSubroutines() {
		Method m = Method.getCompSpecSubroutine("imDelegIiMm");
		if (m != null) { 
			Code32 code = new Code32(null);
			m.machineCode = code;
			createLSWordImm(code, armStr, condAlways, regAux3, stackPtr, intfMethStorageOffset, 1, 0, 0);	// backup auxiliary registers onto stack
			createLSWordImm(code, armStr, condAlways, regAux2, stackPtr, intfMethStorageOffset + 4, 1, 0, 0);
			createLSWordImm(code, armStr, condAlways, regAux1, stackPtr, intfMethStorageOffset + 8, 1, 0, 0);
			createDataProcImm(code, armOrr, condAlways, regAux3, scratchReg, 0xff);	// interface id
			createDataProcImm(code, armOrr, condAlways, regAux3, regAux3, packImmediate(0xff00));
			createLSWordImm(code, armLdr, condAlways, regAux2, paramStartGPR, 4, 1, 0, 0);	// get tag
			int offset = (Class.maxExtensionLevelStdClasses + 1) * Linker32.slotSize + Linker32.tdBaseClass0Offset;
			createDataProcImm(code, armAdd, condAlways, regAux2, regAux2, packImmediate(offset));	// set to address before first interface 
			createLSWordImm(code, armLdr, condAlways, regAux1, regAux2, 4, 1, 1, 1);	// get interface
			createDataProcReg(code, armCmp, condAlways, 0, regAux1, regAux3, noShift, 0);
			createBranchImm(code, armB, condGT, -4);

			createMedia(code, armSbfx, condAlways, regAux1, regAux1, 0, 16);	// contains method offset within its interface
			createMovw(code, armMovw, condAlways, regAux3, 0xffff);
			createDataProcReg(code, armAnd, condAlways, scratchReg, regAux3, scratchReg, noShift, 0);	// mask interface method offset
			createDataProcReg(code, armAdd, condAlways, scratchReg, scratchReg, regAux1, noShift, 0);	// offset = interface method offset + method offset
			createLSWordImm(code, armLdr, condAlways, regAux2, paramStartGPR, 4, 1, 0, 0);	// reload tag
			createLSWordReg(code, armLdr, condAlways, scratchReg, regAux2, scratchReg, noShift, 0, 1, 1, 0);	// get method address
			createLSWordImm(code, armLdr, condAlways, regAux1, stackPtr, intfMethStorageOffset + 8, 1, 0, 0);	// restore auxiliary registers from stack
			createLSWordImm(code, armLdr, condAlways, regAux2, stackPtr, intfMethStorageOffset + 4, 1, 0, 0);
			createLSWordImm(code, armLdr, condAlways, regAux3, stackPtr, intfMethStorageOffset, 1, 0, 0);
			createDataProcMovReg(code, armMov, condAlways, PC, scratchReg, noShift, 0);	// jump to method 
//			createBranchImm(code, armB, condAlways, -2);
		}

		m = Method.getCompSpecSubroutine("handleException");
		if (m != null) { 
			Code32 code = new Code32(null);
			m.machineCode = code;
			createBranchImm(code, armB, condAlways, -2);
			// r0 contains reference to exception, r3 holds LR
			// r2 to r10 are used for auxiliary purposes

//			// search end of method
//			createIrArSrB(code, ppcOr, 4, 3, 3);
//			createIrDrAd(code, ppcLwzu, 9, 4, 4);	
//			createICRFrAsimm(code, ppcCmpli, CRF0, 9, 0xff);
//			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+GT, -2);
//			createIrArSrB(code, ppcOr, 10, 4, 4);	// keep for unwinding
//			createIrDrAd(code, ppcLwzu, 5, 4, 4);	//  R4 now points to first entry of exception table
//		
//			// search catch, label 1
//			int label1 = m.machineCode.iCount;
//			createICRFrAsimm(code, ppcCmpi, CRF0, 5, -1);
//			int label2 = m.machineCode.iCount;
//			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, 0);	// catch not found, goto label 2
//			createIrDrAd(code, ppcLwz, 5, 4, 0);	// start 
//			createICRFrArB(code, ppcCmp, CRF0, 3, 5);		
//			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+LT, 17);
//			createIrDrAd(code, ppcLwz, 5, 4, 4);	// end 
//			createICRFrArB(code, ppcCmp, CRF0, 3, 5);		
//			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+GT, 14);
//			createIrDrAd(code, ppcLwz, 5, 4, 8);	// type 
//			
//			createICRFrAsimm(code, ppcCmpi, CRF0, 5, 0);	// check if type "any", caused by finally
//			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, 8);
////			m.machineCode.createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 3);
////			m.machineCode.createIrDrAsimm(ppcAddi, 18, 18, 0x1000);	
////			m.machineCode.createIBOBIBD(ppcBc, BOalways, 4*CRF0+EQ, 8);
//			
//			createIrDrAd(code, ppcLwz, 6, 5, -4);	// get extension level of exception 
//			createIrArSSHMBME(code, ppcRlwinm, 6, 6, 2, 0, 31);	// *4
//			createIrDrAsimm(code, ppcAddi, 6, 6, Linker32.tdBaseClass0Offset);	
//			createIrDrAd(code, ppcLwz, 7, 2, -4);	// get tag 
//			createIrDrArB(code, ppcLwzx, 8, 7, 6);	 
//			createICRFrArB(code, ppcCmp, CRF0, 8, 5);		
//			createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 4);		
//			createIrDrAd(code, ppcLwz, 0, 4, 12);	// get handler address
//			createIrSspr(code, ppcMtspr, SRR0, 0);
//			createIrfi(code, ppcRfi);	// return to catch
//			
//			createIrDrAd(code, ppcLwzu, 5, 4, 16);	
//			createIBOBIBD(code, ppcBc, BOalways, 0, 0);	// jump to label 1
//			correctJmpAddr(m.machineCode.instructions, m.machineCode.iCount-1, label1);
//			
//			// catch not found, unwind, label 2
//			correctJmpAddr(m.machineCode.instructions, label2, m.machineCode.iCount);
//			createIrDrAd(code, ppcLwz, 5, stackPtr, 0);	// get back pointer
//			createIrDrAd(code, ppcLwz, 3, 5, -4);	// get LR from stack
//			loadConstantAndFixup(code, 6, m);
//			createIrSrAd(code, ppcStw, 6, 5, -4);	// put addr of handleException
//			createIrArS(code, ppcExtsb, 9, 9);
//			createIrDrArB(code, ppcAdd, 9, 10, 9);
//			createIrSspr(code, ppcMtspr, LR, 9);
////			m.machineCode.createIBOBIBD(ppcBc, BOalways, 4*CRF0+GT, 0);
//			createIBOBILK(code, ppcBclr, BOalways, 0, false); // branch to epilog
		}
	}

}


