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

package ch.ntb.inf.deep.cg.ppc;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.cg.Code32;
import ch.ntb.inf.deep.cg.CodeGen;
import ch.ntb.inf.deep.cg.InstructionDecoder;
import ch.ntb.inf.deep.cg.RegAllocator;
import ch.ntb.inf.deep.classItems.*;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.Linker32;
import ch.ntb.inf.deep.ssa.*;
import ch.ntb.inf.deep.ssa.instruction.*;
import ch.ntb.inf.deep.strings.HString;

public class CodeGenPPC extends CodeGen implements InstructionOpcs, Registers {

	private static final int arrayLenOffset = 6;	
	// used for some floating point operations and compiler specific subroutines
	private static final int tempStorageSize = 48;	// 1 FPR (temp) + 8 GPRs
	
	public static int idHALT, idENABLE_FLOATS;
	public static int idGETGPR, idGETFPR, idGETSPR;
	public static int idPUTGPR, idPUTFPR, idPUTSPR;

	private static int LRoffset;	
	private static int XERoffset;	
	private static int CRoffset;	
	private static int CTRoffset;	
	private static int SRR0offset;	
	private static int SRR1offset;	
	private static int paramOffset;
	private static int GPRoffset;	
	private static int FPRoffset;	
	private static int localVarOffset;
	private static int tempStorageOffset;	
	private static int stackSize;
	static boolean enFloatsInExc;

	// information about the src registers for parameters of a call to a method within this method
	private static int[] srcGPR = new int[nofGPR];
	private static int[] srcFPR = new int[nofFPR];
	private static int[] srcGPRcount = new int[nofGPR];
	private static int[] srcFPRcount = new int[nofFPR];
	
	private static boolean newString;
	
	public CodeGenPPC() {}

	public void init() { 
		Class cls = Configuration.getOS().usClass;
//		Class cls = (Class)RefType.refTypeList.getItemByName("ch/ntb/inf/deep/unsafe/ppc/US");
		if (cls == null) {ErrorReporter.reporter.error(630); return;}
		Method m = Configuration.getOS().getSystemMethodByName(cls, "GETGPR"); 
		if (m != null) idGETGPR = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "GETFPR"); 
		if (m != null) idGETFPR = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "GETSPR"); 
		if (m != null) idGETSPR = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "PUTGPR"); 
		if (m != null) idPUTGPR = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "PUTFPR"); 
		if (m != null) idPUTFPR = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "PUTSPR"); 
		if (m != null) idPUTSPR = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "HALT"); 
		if (m != null) idHALT = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "ENABLE_FLOATS"); 
		if (m != null) idENABLE_FLOATS = m.id; else {ErrorReporter.reporter.error(631); return;}
		
		super.init();
	}
	
	public void translateMethod(Method method) {
		init(method);
		SSA ssa = method.ssa;
		Code32 code = method.machineCode;
		
		if (dbg) StdStreams.vrb.println("build intervals");

		enFloatsInExc = false;
		RegAllocatorPPC.regsGPR = regsGPRinitial;
		RegAllocatorPPC.regsFPR = regsFPRinitial;

		RegAllocator.buildIntervals(ssa);
		
		if (dbg) StdStreams.vrb.println("assign registers to parameters");
		SSANode b = (SSANode) ssa.cfg.rootNode;
		while (b.next != null) {
			b = (SSANode) b.next;
		}	
		SSAValue[] lastExitSet = b.exitSet;
		// determine, which parameters go into which register
		parseExitSet(lastExitSet, method.maxStackSlots);
		if (dbg) {
			StdStreams.vrb.print("parameter go into register: ");
			for (int n = 0; paramRegNr[n] != -1; n++) StdStreams.vrb.print(paramRegNr[n] + "  "); 
			StdStreams.vrb.println();
		}
//		StdStreams.vrb.print(ssa.toString());
		
		if (dbg) StdStreams.vrb.println("allocate registers");
		RegAllocatorPPC.assignRegisters();
		if (!RegAllocator.fullRegSet) {	// repeat with a reduced register set
			if (dbg) StdStreams.vrb.println("register allocation for method " + method.owner.name + "." + method.name + " was not successful, run again and use stack slots");
			if (RegAllocator.useLongs) RegAllocatorPPC.regsGPR = regsGPRinitial & ~(0xff << nonVolStartGPR);
			else RegAllocatorPPC.regsGPR = regsGPRinitial & ~(0x1f << nonVolStartGPR);
			if (dbg) StdStreams.vrb.println("regsGPRinitial = 0x" + Integer.toHexString(RegAllocatorPPC.regsGPR));
			RegAllocatorPPC.regsFPR = regsFPRinitial& ~(0x7 << nonVolStartFPR);
			if (dbg) StdStreams.vrb.println("regsFPRinitial = 0x" + Integer.toHexString(RegAllocatorPPC.regsFPR));
			RegAllocator.stackSlotSpilledRegs = -1;
			parseExitSet(lastExitSet, method.maxStackSlots);
			if (dbg) {
				StdStreams.vrb.print("parameter go into register: ");
				for (int n = 0; paramRegNr[n] != -1; n++) StdStreams.vrb.print(paramRegNr[n] + "  "); 
				StdStreams.vrb.println();
			}
			RegAllocatorPPC.resetRegisters();
			RegAllocatorPPC.assignRegisters();
		}
//		StdStreams.vrb.print(ssa.toString());
		
		if (dbg) {
			StdStreams.vrb.println(RegAllocatorPPC.joinsToString());
		}
		if (dbg) {
			StdStreams.vrb.print("register usage in method: nofNonVolGPR = " + nofNonVolGPR + ", nofVolGPR = " + nofVolGPR);
			StdStreams.vrb.println(", nofNonVolFPR = " + nofNonVolFPR + ", nofVolFPR = " + nofVolFPR);
			StdStreams.vrb.print("register usage for parameters: nofParamGPR = " + nofParamGPR + ", nofParamFPR = " + nofParamFPR);
			StdStreams.vrb.println(", receive parameters slots on stack = " + recParamSlotsOnStack);
			StdStreams.vrb.println("max. parameter slots for any call in this method = " + callParamSlotsOnStack);
			StdStreams.vrb.print("parameter end at instr no: ");
			for (int n = 0; n < nofParam; n++) 
				if (paramRegEnd[n] != -1) StdStreams.vrb.print(paramRegEnd[n] + "  "); 
			StdStreams.vrb.println();
		}
		if ((method.accAndPropFlags & (1 << dpfExcHnd)) != 0) {	// exception
			if (method.name == HString.getRegisteredHString("reset")) {	// reset has no prolog
			} else if (method.name == HString.getRegisteredHString("programExc")) {	// special treatment for exception handling
				code.iCount = 0;
				createIrSrAsimm(code, ppcStwu, stackPtr, stackPtr, -24);
				createIrSspr(code, ppcMtspr, EID, 0);	// must be set for further debugger exceptions
				createIrSrAd(code, ppcStmw, 28, stackPtr, 4);
				createIrArSrB(code, ppcOr, 31, paramStartGPR, paramStartGPR);	// copy exception into nonvolatile
			} else {
				stackSize = calcStackSizeException();
				insertPrologException(code);
			}
		} else {
			stackSize = calcStackSize();
			insertProlog(code);	// builds stack frame and copies parameters
		}
		
		SSANode node = (SSANode)ssa.cfg.rootNode;
		while (node != null) {
			node.codeStartIndex = code.iCount;
			translateSSA(node, method);
			node.codeEndIndex = code.iCount-1;
			node = (SSANode) node.next;
		}
		node = (SSANode)ssa.cfg.rootNode;
		while (node != null) {	// resolve local branch targets
			if (node.nofInstr > 0) {
				if ((node.instructions[node.nofInstr-1].ssaOpcode == sCbranch) || (node.instructions[node.nofInstr-1].ssaOpcode == sCswitch)) {
					int instr = code.instructions[node.codeEndIndex];
					CFGNode[] successors = node.successors;
					switch (instr & 0xfc000000) {
					case ppcB:			
						if ((instr & 0xffff) != 0) {	// switch
							int nofCases = (instr & 0xffff) >> 2;
							int k;
							for (k = 0; k < nofCases; k++) {
								int branchOffset = ((SSANode)successors[k]).codeStartIndex - (node.codeEndIndex+1-(nofCases-k)*2);
								code.instructions[node.codeEndIndex+1-(nofCases-k)*2] |= (branchOffset << 2) & 0x3ffffff;
							}
							int branchOffset = ((SSANode)successors[k]).codeStartIndex - node.codeEndIndex;
							code.instructions[node.codeEndIndex] &= 0xfc000000;
							code.instructions[node.codeEndIndex] |= (branchOffset << 2) & 0x3ffffff;
						} else {
							int branchOffset = ((SSANode)successors[0]).codeStartIndex - node.codeEndIndex;
							code.instructions[node.codeEndIndex] |= (branchOffset << 2) & 0x3ffffff;
						}
						break;
					case ppcBc:
						int branchOffset = ((SSANode)successors[1]).codeStartIndex - node.codeEndIndex;
						code.instructions[node.codeEndIndex] |= (branchOffset << 2) & 0xffff;
						break;
					}
				} else if (node.instructions[node.nofInstr-1].ssaOpcode == sCreturn) {
					if (node.next != null) {
						int branchOffset = code.iCount - node.codeEndIndex;
						code.instructions[node.codeEndIndex] |= (branchOffset << 2) & 0x3ffffff;
					}
				}
			}
			node = (SSANode) node.next;
		}
		if ((method.accAndPropFlags & (1 << dpfExcHnd)) != 0) {	// exception
			if (method.name == HString.getRegisteredHString("reset")) {	// reset needs no epilog
			} else if (method.name == HString.getRegisteredHString("programExc")) {	// special treatment for exception handling
				Method m = Method.getCompSpecSubroutine("handleException");
				assert m != null;
				loadConstantAndFixup(code, 31, m);
				createIrSspr(code, ppcMtspr, LR, 31);
				createIrDrAd(code, ppcLmw, 28, stackPtr, 4);
				createIrDrAsimm(code, ppcAddi, stackPtr, stackPtr, 24);
				createIBOBILK(code, ppcBclr, BOalways, 0, false);
			} else {
				insertEpilogException(code, stackSize);
			}
		} else {
			insertEpilog(code, stackSize);
		}
		if (dbg) {StdStreams.vrb.print(ssa.toString()); StdStreams.vrb.print(code.toString());}
	}

	private static void parseExitSet(SSAValue[] exitSet, int maxStackSlots) {
		nofParamGPR = 0; nofParamFPR = 0;
		nofMoveGPR = 0; nofMoveFPR = 0;
		if(dbg) StdStreams.vrb.print("[");
		for (int i = 0; i < nofParam; i++) {
			int type = paramType[i];
			if(dbg) StdStreams.vrb.print("(" + svNames[type] + ")");
			if (type == tLong) {
				if (exitSet[i+maxStackSlots] != null) {	// if null -> parameter is never used
					RegAllocator.useLongs = true;
					if(dbg) StdStreams.vrb.print("r");
					if (paramHasNonVolReg[i]) {
						int reg = RegAllocatorPPC.reserveReg(gpr, true);
						int regLong = RegAllocatorPPC.reserveReg(gpr, true);
						moveGPRsrc[nofMoveGPR] = nofParamGPR;
						moveGPRsrc[nofMoveGPR+1] = nofParamGPR+1;
						moveGPRdst[nofMoveGPR++] = reg;
						moveGPRdst[nofMoveGPR++] = regLong;
						paramRegNr[i] = reg;
						paramRegNr[i+1] = regLong;
						if(dbg) StdStreams.vrb.print(reg + ",r" + regLong);
					} else {
						int reg = paramStartGPR + nofParamGPR;
						if (reg <= paramEndGPR) RegAllocatorPPC.reserveReg(gpr, reg);
						else {
							reg = RegAllocatorPPC.reserveReg(gpr, false);
							moveGPRsrc[nofMoveGPR] = nofParamGPR;
							moveGPRdst[nofMoveGPR++] = reg;
						}
						int regLong = paramStartGPR + nofParamGPR + 1;
						if (regLong <= paramEndGPR) RegAllocatorPPC.reserveReg(gpr, regLong);
						else {
							regLong = RegAllocatorPPC.reserveReg(gpr, false);
							moveGPRsrc[nofMoveGPR] = nofParamGPR + 1;
							moveGPRdst[nofMoveGPR++] = regLong;
						}
						paramRegNr[i] = reg;
						paramRegNr[i+1] = regLong;
						if(dbg) StdStreams.vrb.print(reg + ",r" + regLong);
					}
				}
				nofParamGPR += 2;	// see comment below for else type 
				i++;
			} else if (type == tFloat || type == tDouble) {
				if (exitSet[i+maxStackSlots] != null) {	// if null -> parameter is never used
					if(dbg) StdStreams.vrb.print("fr");
					if (paramHasNonVolReg[i]) {
						int reg = RegAllocatorPPC.reserveReg(fpr, true);
						moveFPRsrc[nofMoveFPR] = nofParamFPR;
						moveFPRdst[nofMoveFPR++] = reg;
						paramRegNr[i] = reg;
						if(dbg) StdStreams.vrb.print(reg);
					} else {
						int reg = paramStartFPR + nofParamFPR;
						if (reg <= paramEndFPR) RegAllocatorPPC.reserveReg(fpr, reg);
						else {
							reg = RegAllocatorPPC.reserveReg(fpr, false);
							moveFPRsrc[nofMoveFPR] = nofParamFPR;
							moveFPRdst[nofMoveFPR++] = reg;
						}
						paramRegNr[i] = reg;
						if(dbg) StdStreams.vrb.print(reg);
					}
				}
				nofParamFPR++;	// see comment below for else type 
				if (type == tDouble) {i++; paramRegNr[i] = paramRegNr[i-1];}
			} else {
				if (exitSet[i+maxStackSlots] != null) {	// if null -> parameter is never used
					if(dbg) StdStreams.vrb.print("r");
					if (paramHasNonVolReg[i]) {
						int reg = RegAllocatorPPC.reserveReg(gpr, true);	// nonvolatile or stack slot
						moveGPRsrc[nofMoveGPR] = nofParamGPR;
						moveGPRdst[nofMoveGPR++] = reg;
						paramRegNr[i] = reg;
						if(dbg) StdStreams.vrb.print(reg);
					} else {
						int reg = paramStartGPR + nofParamGPR;
						if (reg <= paramEndGPR) RegAllocatorPPC.reserveReg(gpr, reg); // mark as reserved
						else {
							reg = RegAllocatorPPC.reserveReg(gpr, false);	// volatile, nonvolatile or stack slot
							moveGPRsrc[nofMoveGPR] = nofParamGPR;
							moveGPRdst[nofMoveGPR++] = reg;
						}
						paramRegNr[i] = reg;
						if(dbg) StdStreams.vrb.print(reg);
					}
				}
				nofParamGPR++;	// even if the parameter is not used, the calling method
				// assigns a register and we have to account for this here
			}
			if (i < nofParam - 1) if(dbg) StdStreams.vrb.print(", ");
		}
		int nof = nofParamGPR - (paramEndGPR - paramStartGPR + 1);
		if (nof > 0) recParamSlotsOnStack = nof;
		nof = nofParamFPR - (paramEndFPR - paramStartFPR + 1);
		if (nof > 0) recParamSlotsOnStack += nof*2;
		
		if(dbg) StdStreams.vrb.println("]");
	}

	private static int calcStackSize() {
		int size = 8 + callParamSlotsOnStack * 4 + nofNonVolGPR * 4 + nofNonVolFPR * 8 + RegAllocator.maxLocVarStackSlots * 4 + tempStorageSize;
		if (enFloatsInExc) size += nonVolStartFPR * 8 + 8;	// save volatile FPR's and FPSCR
		int padding = (16 - (size % 16)) % 16;
		size = size + padding;
		LRoffset = size - 4;
		GPRoffset = LRoffset - nofNonVolGPR * 4;
		FPRoffset = GPRoffset - nofNonVolFPR * 8;
		if (enFloatsInExc) FPRoffset -= nonVolStartFPR * 8 + 8;
		localVarOffset = FPRoffset - RegAllocator.maxLocVarStackSlots * 4;
		tempStorageOffset = 4;
		paramOffset = tempStorageOffset + tempStorageSize;
		return size;
	}

	private static int calcStackSizeException() {
		int size = 28 + nofGPR * 4 + RegAllocator.maxLocVarStackSlots * 4 + tempStorageSize;
		if (enFloatsInExc) {
			size += nofNonVolFPR * 8;	// save used nonvolatile FPR's
			size += nonVolStartFPR * 8 + 8;	// save all volatile FPR's and FPSCR
		}
		int padding = (16 - (size % 16)) % 16;
		size = size + padding;
		LRoffset = size - 4;
		XERoffset = LRoffset - 4;
		CRoffset = XERoffset - 4;
		CTRoffset = CRoffset - 4;
		SRR1offset = CTRoffset - 4;
		SRR0offset = SRR1offset - 4;
		GPRoffset = SRR0offset - nofGPR * 4;
		FPRoffset = GPRoffset - nofNonVolFPR * 8;
		if (enFloatsInExc) FPRoffset -= nonVolStartFPR * 8 + 8;
		localVarOffset = FPRoffset - RegAllocator.maxLocVarStackSlots * 4;
		tempStorageOffset = 4;
		paramOffset = tempStorageOffset + tempStorageSize;
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
				createIrArSrB(code, ppcOr, node.instructions[node.loadLocalExc].result.reg, paramStartGPR, paramStartGPR);
			}

			if (dbg) StdStreams.vrb.println("handle instruction " + instr.toString());
			if (instr.ssaOpcode == sCloadLocal) continue;	
			SSAValue[] opds = instr.getOperands();
			if (instr.ssaOpcode == sCstoreToArray) {
				src3Reg = opds[2].reg; 
				src3RegLong = opds[2].regLong;
				if (src3RegLong >= 0x100) {
					if (dbg) StdStreams.vrb.println("opd3 regLong on stack slot for instr: " + instr.toString());
					int slot = src3RegLong & 0xff;
					src3RegLong = nonVolStartGPR + 5;
					createIrDrAd(code, ppcLwz, src3RegLong, stackPtr, localVarOffset + 4 * slot);
				}
				if (src3Reg >= 0x100) {
					if (dbg) StdStreams.vrb.println("opd3 reg on stack slot for instr: " + instr.toString());
					int slot = src3Reg & 0xff;
					if ((opds[2].type == tFloat) || (opds[2].type == tDouble)) {
						src3Reg = nonVolStartFPR + 0;
						createIrDrAd(code, ppcLfd, src3Reg, stackPtr, localVarOffset + 4 * slot);
					} else {
						src3Reg = nonVolStartGPR + 0;
						createIrDrAd(code, ppcLwz, src3Reg, stackPtr, localVarOffset + 4 * slot);
					}
				}			
			}
			if (opds != null && opds.length != 0) {
				if (opds.length >= 2) {
					src2Reg = opds[1].reg; 
					src2RegLong = opds[1].regLong;
					if (src2RegLong >= 0x100) {
						if (dbg) StdStreams.vrb.println("opd2 regLong on stack slot for instr: " + instr.toString());
						int slot = src2RegLong & 0xff;
						src2RegLong = nonVolStartGPR + 7;
						createIrDrAd(code, ppcLwz, src2RegLong, stackPtr, localVarOffset + 4 * slot);
					}
					if (src2Reg >= 0x100) {
						if (dbg) StdStreams.vrb.println("opd2 reg on stack slot for instr: " + instr.toString());
						int slot = src2Reg & 0xff;
						if ((opds[1].type == tFloat) || (opds[1].type == tDouble)) {
							src2Reg = nonVolStartFPR + 2;
							createIrDrAd(code, ppcLfd, src2Reg, stackPtr, localVarOffset + 4 * slot);
						} else {
							src2Reg = nonVolStartGPR + 2;
							createIrDrAd(code, ppcLwz, src2Reg, stackPtr, localVarOffset + 4 * slot);
						}
					}			
				}
				src1Reg = opds[0].reg; 
				src1RegLong = opds[0].regLong;
				if (src1RegLong >= 0x100) {
					if (dbg) StdStreams.vrb.println("opd1 regLong on stack slot for instr: " + instr.toString());
					int slot = src1RegLong & 0xff;
					src1RegLong = nonVolStartGPR + 6;
					createIrDrAd(code, ppcLwz, src1RegLong, stackPtr, localVarOffset + 4 * slot);
				}
				if (src1Reg >= 0x100) {
					if (dbg) StdStreams.vrb.println("opd1 reg on stack slot for instr: " + instr.toString());
					int slot = src1Reg & 0xff;
					if ((opds[0].type == tFloat) || (opds[0].type == tDouble)) {
						src1Reg = nonVolStartFPR + 1;
						createIrDrAd(code, ppcLfd, src1Reg, stackPtr, localVarOffset + 4 * slot);
					} else {
						src1Reg = nonVolStartGPR + 1;
						createIrDrAd(code, ppcLwz, src1Reg, stackPtr, localVarOffset + 4 * slot);
					}
				}			
			}
			dRegLong = res.regLong;
			int dRegLongSlot = -1;
			if (dRegLong >= 0x100) {
				if (dbg) StdStreams.vrb.println("res regLong on stack slot for instr: " + instr.toString());
				dRegLongSlot = dRegLong & 0xff;
				dRegLong = nonVolStartGPR + 5;
			}
			dReg = res.reg;
			int dRegSlot = -1;
			if (dReg >= 0x100) {
				if (dbg) StdStreams.vrb.println("res reg on stack slot for instr: " + instr.toString());
				dRegSlot = dReg & 0xff;
				if ((res.type == tFloat) || (res.type == tDouble)) dReg = nonVolStartFPR + 0;
				else dReg = nonVolStartGPR + 0;
			}

			int gAux1 = res.regGPR1;
			if (gAux1 >= 0x100) gAux1 = nonVolStartGPR + 3;
			int gAux2 = res.regGPR2;
			if (gAux2 >= 0x100) gAux2 = nonVolStartGPR + 4;
			
			switch (instr.ssaOpcode) { 
			case sCloadConst: {
				if (dReg >= 0) {	// else immediate opd, does not have to be loaded into a register
					switch (res.type & ~(1<<ssaTaFitIntoInt)) {
					case tByte: case tShort: case tInteger:
						int immVal = ((StdConstant)res.constant).valueH;
						loadConstant(code, dReg, immVal);
					break;
					case tLong:	
						StdConstant constant = (StdConstant)res.constant;
						long immValLong = ((long)(constant.valueH)<<32) | (constant.valueL&0xFFFFFFFFL);
						loadConstant(code, dRegLong, (int)(immValLong >> 32));
						loadConstant(code, dReg, (int)immValLong);
						break;	
					case tFloat:	// load from const pool
						constant = (StdConstant)res.constant;
						if (constant.valueH == 0) {	// 0.0 must be loaded directly as it's not in the cp
							createIrDrAsimm(code, ppcAddi, gAux1, 0, 0);
							createIrSrAd(code, ppcStw, gAux1, stackPtr, tempStorageOffset);
							createIrDrAd(code, ppcLfs, dReg, stackPtr, tempStorageOffset);
						} else if (constant.valueH == 0x3f800000) {	// 1.0
							createIrDrAsimm(code, ppcAddis, gAux1, 0, 0x3f80);
							createIrSrAd(code, ppcStw, gAux1, stackPtr, tempStorageOffset);
							createIrDrAd(code, ppcLfs, dReg, stackPtr, tempStorageOffset);
						} else if (constant.valueH == 0x40000000) {	// 2.0
							createIrDrAsimm(code, ppcAddis, gAux1, 0, 0x4000);
							createIrSrAd(code, ppcStw, gAux1, stackPtr, tempStorageOffset);
							createIrDrAd(code, ppcLfs, dReg, stackPtr, tempStorageOffset);
						} else {
							loadConstantAndFixup(code, gAux1, constant);
							createIrDrAd(code, ppcLfs, dReg, gAux1, 0);
						}
						break;
					case tDouble:
						constant = (StdConstant)res.constant;
						if (constant.valueH == 0) {	// 0.0 must be loaded directly as it's not in the cp
							createIrDrAsimm(code, ppcAddi, gAux1, 0, 0);
							createIrSrAd(code, ppcStw, gAux1, stackPtr, tempStorageOffset);
							createIrSrAd(code, ppcStw, gAux1, stackPtr, tempStorageOffset+4);
							createIrDrAd(code, ppcLfd, dReg, stackPtr, tempStorageOffset);
						} else if (constant.valueH == 0x3ff00000) {	// 1.0{
							createIrDrAsimm(code, ppcAddis, gAux1, 0, 0x3ff0);
							createIrSrAd(code, ppcStw, gAux1, stackPtr, tempStorageOffset);
							createIrDrAsimm(code, ppcAddis, gAux1, 0, 0);
							createIrSrAd(code, ppcStw, gAux1, stackPtr, tempStorageOffset+4);
							createIrDrAd(code, ppcLfd, dReg, stackPtr, tempStorageOffset);
						} else {
							loadConstantAndFixup(code, gAux1, constant);
							createIrDrAd(code, ppcLfd, dReg, gAux1, 0);
						}
						break;
					case tRef: case tAbyte: case tAshort: case tAchar: case tAinteger:
					case tAlong: case tAfloat: case tAdouble: case tAboolean: case tAref:
						if (res.constant == null) {// object = null
							loadConstant(code, dReg, 0);
						} else if ((meth.owner.accAndPropFlags & (1<<apfEnum)) != 0 && meth.name.equals(HString.getHString("valueOf"))) {	// special case 
							loadConstantAndFixup(code, dReg, res.constant); // load address of static field "ENUM$VALUES"
							createIrDrAd(code, ppcLwz, dReg, dReg, 0);	// load reference to object on heap
						} else {	// ref to constant string
							loadConstantAndFixup(code, dReg, res.constant);
						}
						break;
					default:
						ErrorReporter.reporter.error(610);
						assert false : "result of SSA instruction has wrong type";
						return;
					}
				}  
				break;}	// sCloadConst
			case sCloadFromField: {
				int offset = 0, refReg;			
				if (opds == null) {	// getstatic
					refReg = gAux1;
					Item field = ((NoOpndRef)instr).field;
					loadConstantAndFixup(code, refReg, field);
				} else {	// getfield
					refReg = src1Reg;
					if ((meth.owner == Type.wktString) &&	// string access needs special treatment
							((MonadicRef)instr).item.name.equals(HString.getRegisteredHString("value"))) {
						createIrArSrB(code, ppcOr, dReg, refReg, refReg);	// result contains ref to string
						stringCharRef = ((MonadicRef)instr).item;	// ref to "value"
						break;	
					} else {
						offset = ((MonadicRef)instr).item.offset;
						createItrap(code, ppcTwi, TOifequal, refReg, 0);
					}
				}
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tBoolean: case tByte:
					createIrDrAd(code, ppcLbz, dReg, refReg, offset);
					createIrArS(code, ppcExtsb, dReg, dReg);
					break;
				case tShort: 
					createIrDrAd(code, ppcLha, dReg, refReg, offset);
					break;
				case tInteger: case tRef: case tAref: case tAboolean:
				case tAchar: case tAfloat: case tAdouble: case tAbyte: 
				case tAshort: case tAinteger: case tAlong:
					createIrDrAd(code, ppcLwz, dReg, refReg, offset);
					break;
				case tChar: 
					createIrDrAd(code, ppcLhz, dReg, refReg, offset);
					break;
				case tLong:
					createIrDrAd(code, ppcLwz, dRegLong, refReg, offset);
					createIrDrAd(code, ppcLwz, dReg, refReg, offset + 4);
					break;
				case tFloat: 
					createIrDrAd(code, ppcLfs, dReg, refReg, offset);
					break;
				case tDouble: 
					createIrDrAd(code, ppcLfd, dReg, refReg, offset);
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
				if (meth.owner == Type.wktString && opds[0].owner instanceof MonadicRef && ((MonadicRef)opds[0].owner).item == stringCharRef) {	// string access needs special treatment
					createIrDrAd(code, ppcLwz, gAux1, refReg, objectSize);	// read field "count", must be first field
					createItrap(code, ppcTw, TOifgeU, indexReg, gAux1);
					switch (res.type & 0x7fffffff) {	// type to read
					case tByte:
						createIrDrAsimm(code, ppcAddi, gAux2, refReg, stringSize - 4);	// add index of field "value" to index
						createIrDrArB(code, ppcLbzx, dReg, gAux2, indexReg);
						createIrArS(code, ppcExtsb, dReg, dReg);
						break;
					case tChar: 
						createIrArSSHMBME(code, ppcRlwinm, gAux1, indexReg, 1, 0, 30);
						createIrDrAsimm(code, ppcAddi, gAux2, refReg, stringSize - 4);	// add index of field "value" to index
						createIrDrArB(code, ppcLhzx, dReg, gAux1, gAux2);
						break;
					default:
						ErrorReporter.reporter.error(610);
						assert false : "result of SSA instruction has wrong type";
						return;
					}
				} else {
					createItrap(code, ppcTwi, TOifequal, refReg, 0);
					createIrDrAd(code, ppcLha, gAux1, refReg, -arrayLenOffset);
					createItrap(code, ppcTw, TOifgeU, indexReg, gAux1);
					switch (res.type & ~(1<<ssaTaFitIntoInt)) {	// type to read
					case tByte: case tBoolean:
						createIrDrAsimm(code, ppcAddi, gAux2, refReg, objectSize);
						createIrDrArB(code, ppcLbzx, dReg, gAux2, indexReg);
						createIrArS(code, ppcExtsb, dReg, dReg);
						break;
					case tShort: 
						createIrArSSHMBME(code, ppcRlwinm, gAux1, indexReg, 1, 0, 30);
						createIrDrAsimm(code, ppcAddi, gAux2, refReg, objectSize);
						createIrDrArB(code, ppcLhax, dReg, gAux1, gAux2);
						break;
					case tInteger: case tRef: case tAref: case tAchar: case tAfloat: 
					case tAdouble: case tAbyte: case tAshort: case tAinteger: case tAlong:
						createIrArSSHMBME(code, ppcRlwinm, gAux1, indexReg, 2, 0, 29);
						createIrDrAsimm(code, ppcAddi, gAux2, refReg, objectSize);
						createIrDrArB(code, ppcLwzx, dReg, gAux1, gAux2);
						break;
					case tLong: 
						createIrArSSHMBME(code, ppcRlwinm, gAux1, indexReg, 3, 0, 28);
						createIrDrAsimm(code, ppcAddi, gAux2, refReg, objectSize);
						createIrDrArB(code, ppcLwzux, dRegLong, gAux1, gAux2);
						createIrDrAd(code, ppcLwz, dReg, gAux1, 4);
						break;
					case tFloat:
						createIrArSSHMBME(code, ppcRlwinm, gAux1, indexReg, 2, 0, 29);
						createIrDrAsimm(code, ppcAddi, gAux2, refReg, objectSize);
						createIrDrArB(code, ppcLfsx, dReg, gAux1, gAux2);
						break;
					case tDouble: 
						createIrArSSHMBME(code, ppcRlwinm, gAux1, indexReg, 3, 0, 28);
						createIrDrAsimm(code, ppcAddi, gAux2, refReg, objectSize);
						createIrDrArB(code, ppcLfdx, dReg, gAux1, gAux2);
						break;
					case tChar: 
						createIrArSSHMBME(code, ppcRlwinm, gAux1, indexReg, 1, 0, 30);
						createIrDrAsimm(code, ppcAddi, gAux2, refReg, objectSize);
						createIrDrArB(code, ppcLhzx, dReg, gAux1, gAux2);
						break;
					default:
						ErrorReporter.reporter.error(610);
						assert false : "result of SSA instruction has wrong type";
						return;
					}
				}
				break;}	// sCloadFromArray
			case sCstoreToField: {
				int valReg, valRegLong, refReg, offset, type;
				if (opds.length == 1) {	// putstatic
					valReg = src1Reg;
					valRegLong = src1RegLong;
					refReg = gAux1;
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
					createItrap(code, ppcTwi, TOifequal, refReg, 0);
				}
				switch (type) {
				case tBoolean: case tByte: 
					createIrSrAd(code, ppcStb, valReg, refReg, offset);
					break;
				case tShort: case tChar:
					createIrSrAd(code, ppcSth, valReg, refReg, offset);
					break;
				case tInteger: case tRef: case tAref: case tAboolean:
				case tAchar: case tAfloat: case tAdouble: case tAbyte: 
				case tAshort: case tAinteger: case tAlong:
					createIrSrAd(code, ppcStw, valReg, refReg, offset);
					break;
				case tLong:
					createIrSrAd(code, ppcStw, valRegLong, refReg, offset);
					createIrSrAd(code, ppcStw, valReg, refReg, offset + 4);
					break;
				case tFloat: 
					createIrSrAd(code, ppcStfs, valReg, refReg, offset);
					break;
				case tDouble: 
					createIrSrAd(code, ppcStfd, valReg, refReg, offset);
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
				if (meth.owner == Type.wktString && opds[0].owner instanceof MonadicRef && ((MonadicRef)opds[0].owner).item == stringCharRef) {	// string access needs special treatment
					createIrArSSHMBME(code, ppcRlwinm, gAux1, indexReg, 1, 0, 30);
					createIrDrAsimm(code, ppcAddi, gAux2, refReg, stringSize - 4);	// add index of field "value" to index
					createIrSrArB(code, ppcSthx, valReg, gAux1, gAux2);
				} else {
					createItrap(code, ppcTwi, TOifequal, refReg, 0);
					createIrDrAd(code, ppcLha, gAux1, refReg, -arrayLenOffset);
					createItrap(code, ppcTw, TOifgeU, indexReg, gAux1);
					switch (opds[0].type & ~(1<<ssaTaFitIntoInt)) {
					case tAbyte: case tAboolean:
						createIrDrAsimm(code, ppcAddi, gAux2, refReg, objectSize);
						createIrSrArB(code, ppcStbx, valReg, indexReg, gAux2);
						break;
					case tAshort: case tAchar: 
						createIrArSSHMBME(code, ppcRlwinm, gAux1, indexReg, 1, 0, 30);
						createIrDrAsimm(code, ppcAddi, gAux2, refReg, objectSize);
						createIrSrArB(code, ppcSthx, valReg, gAux1, gAux2);
						break;
					case tAref: case tRef: case tAinteger:
						createIrArSSHMBME(code, ppcRlwinm, gAux1, indexReg, 2, 0, 29);
						createIrDrAsimm(code, ppcAddi, gAux2, refReg, objectSize);
						createIrSrArB(code, ppcStwx, valReg, gAux1, gAux2);
						break;
					case tAlong: 
						createIrArSSHMBME(code, ppcRlwinm, gAux1, indexReg, 3, 0, 28);
						createIrDrAsimm(code, ppcAddi, gAux2, refReg, objectSize);
						createIrSrArB(code, ppcStwux, src3RegLong, gAux1, gAux2);
						createIrSrAd(code, ppcStw, valReg, gAux1, 4);
						break;
					case tAfloat:  
						createIrArSSHMBME(code, ppcRlwinm, gAux1, indexReg, 2, 0, 29);
						createIrDrAsimm(code, ppcAddi, gAux2, refReg, objectSize);
						createIrSrArB(code, ppcStfsx, valReg, gAux1, gAux2);
						break;
					case tAdouble: 
						createIrArSSHMBME(code, ppcRlwinm, gAux1, indexReg, 3, 0, 28);
						createIrDrAsimm(code, ppcAddi, gAux2, refReg, objectSize);
						createIrSrArB(code, ppcStfdx, valReg, gAux1, gAux2);
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
					if (src1Reg < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						createIrDrAsimm(code, ppcAddi, dReg, src2Reg, immVal);
					} else if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						createIrDrAsimm(code, ppcAddi, dReg, src1Reg, immVal);
					} else {
						createIrDrArB(code, ppcAdd, dReg, src1Reg, src2Reg);
					}
					break;
				case tLong:
					if (src1Reg < 0) {
						long immValLong = ((long)(((StdConstant)opds[0].constant).valueH)<<32) | (((StdConstant)opds[0].constant).valueL&0xFFFFFFFFL);
						createIrDrAsimm(code, ppcAddic, dReg, src2Reg, (int)immValLong);
						createIrDrA(code, ppcAddze, dRegLong, src2RegLong);
					} else if (src2Reg < 0) {
						long immValLong = ((long)(((StdConstant)opds[1].constant).valueH)<<32) | (((StdConstant)opds[1].constant).valueL&0xFFFFFFFFL);
						createIrDrAsimm(code, ppcAddic, dReg, src1Reg, (int)immValLong);
						createIrDrA(code, ppcAddze, dRegLong, src1RegLong);
					} else {
						createIrDrArB(code, ppcAddc, dReg, src1Reg, src2Reg);
						createIrDrArB(code, ppcAdde, dRegLong, src1RegLong, src2RegLong);
					}	
					break;
				case tFloat:
					createIrDrArB(code, ppcFadds, dReg, src1Reg, src2Reg);
					break;
				case tDouble:
					createIrDrArB(code, ppcFadd, dReg, src1Reg, src2Reg);
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
					if (src1Reg < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						createIrDrAsimm(code, ppcSubfic, dReg, src2Reg, immVal);
					} else if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						createIrDrAsimm(code, ppcAddi, dReg, src1Reg, -immVal);
					} else {
						createIrDrArB(code, ppcSubf, dReg, src2Reg, src1Reg);
					}
					break;
				case tLong:
					if (src1Reg < 0) {
						long immValLong = ((long)(((StdConstant)opds[0].constant).valueH)<<32) | (((StdConstant)opds[0].constant).valueL&0xFFFFFFFFL);
						createIrDrAsimm(code, ppcSubfic, dReg, src2Reg, (int)immValLong);
						createIrDrA(code, ppcSubfze, dRegLong, src2RegLong);
					} else if (src2Reg < 0) {
						long immValLong = ((long)(((StdConstant)opds[1].constant).valueH)<<32) | (((StdConstant)opds[1].constant).valueL&0xFFFFFFFFL);
						createIrDrAsimm(code, ppcAddic, dReg, src1Reg, -(int)immValLong);
						createIrDrA(code, ppcAddme, dRegLong, src1RegLong);
					} else {
						createIrDrArB(code, ppcSubfc, dReg, src2Reg, src1Reg);
						createIrDrArB(code, ppcSubfe, dRegLong, src2RegLong, src1RegLong);
					}
					break;
				case tFloat:
					createIrDrArB(code, ppcFsubs, dReg, src1Reg, src2Reg);
					break;
				case tDouble:
					createIrDrArB(code, ppcFsub, dReg, src1Reg, src2Reg);
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
					if (src1Reg < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						createIrDrAsimm(code, ppcMulli, dReg, src2Reg, immVal);
					} else if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						if (src2Reg == -1) {
							createIrDrAsimm(code, ppcMulli, dReg, src1Reg, immVal);
						} else {	// is power of 2
							int shift = 0;
							while (immVal > 1) {shift++; immVal >>= 1;}
							if (shift == 0) createIrArSrB(code, ppcOr, dReg, src1Reg, src1Reg);
							else createIrArSSHMBME(code, ppcRlwinm, dReg, src1Reg, shift, 0, 31-shift);
						}
					} else {
						createIrDrArB(code, ppcMullw, dReg, src1Reg, src2Reg);
					}
					break;
				case tLong:
					if (src2Reg < 0) {	// is power of 2
						long immValLong = ((long)(((StdConstant)opds[1].constant).valueH)<<32) | (((StdConstant)opds[1].constant).valueL&0xFFFFFFFFL);
						int shift = 0;
						while (immValLong > 1) {shift++; immValLong >>= 1;}
						if (shift == 0) {
							createIrArSrB(code, ppcOr, dRegLong, src1RegLong, src1RegLong);
							createIrArSrB(code, ppcOr, dReg, src1Reg, src1Reg);
						} else {
							if (shift < 32) {
								createIrArSSHMBME(code, ppcRlwinm, dRegLong, src1Reg, shift, 32-shift, 31);
								createIrArSSHMBME(code, ppcRlwimi, dRegLong, src1RegLong, shift, 0, 31-shift);
								createIrArSSHMBME(code, ppcRlwinm, dReg, src1Reg, shift, 0, 31-shift);
							} else {
								createIrDrAsimm(code, ppcAddi, dReg, 0, 0);
								createIrArSSHMBME(code, ppcRlwinm, dRegLong, src1Reg, shift-32, 0, 63-shift);
							}
						}
					} else {
						createIrDrArB(code, ppcMullw, gAux1, src1RegLong, src2Reg);
						createIrDrArB(code, ppcMullw, gAux2, src1Reg, src2RegLong);
						createIrDrArB(code, ppcAdd, gAux1, gAux1, gAux2);
						createIrDrArB(code, ppcMulhwu, gAux2, src1Reg, src2Reg);
						createIrDrArB(code, ppcAdd, dRegLong, gAux1, gAux2);
						createIrDrArB(code, ppcMullw, dReg, src1Reg, src2Reg);
					}
					break;
				case tFloat:
					createIrDrArC(code, ppcFmuls, dReg, src1Reg, src2Reg);
					break;
				case tDouble:
					createIrDrArC(code, ppcFmul, dReg, src1Reg, src2Reg);
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
						int immVal = ((StdConstant)opds[1].constant).valueH;	// is power of 2
						int shift = 0;
						while (immVal > 1) {shift++; immVal >>= 1;}
						if (shift == 0) 
							createIrArSrB(code, ppcOr, dReg, src1Reg, src1Reg);
						else {
							createIrArSSH(code, ppcSrawi, 0, src1Reg, shift-1);
							createIrArSSHMBME(code, ppcRlwinm, 0, 0, shift, 32 - shift, 31);	
							createIrDrArB(code, ppcAdd, 0, src1Reg, 0);
							createIrArSSH(code, ppcSrawi, dReg, 0, shift);
						}
					} else {
						createItrap(code, ppcTwi, TOifequal, src2Reg, 0);
						createIrDrArB(code, ppcDivw, dReg, src1Reg, src2Reg);
					}
					break;
				case tLong:
					if (src2Reg < 0) {	// is power of 2
						long immValLong = ((long)(((StdConstant)opds[1].constant).valueH)<<32) | (((StdConstant)opds[1].constant).valueL&0xFFFFFFFFL);
						int shift = 0;
						while (immValLong > 1) {shift++; immValLong >>= 1;}
						if (shift == 0) {
							createIrArSrB(code, ppcOr, dReg, src1Reg, src1Reg);
							createIrArSrB(code, ppcOr, dRegLong, src1RegLong, src1RegLong);
						} else if (shift < 32) {
							int sh1 = shift - 1;																// shift right arithmetic immediate by shift-1
							createIrArSSHMBME(code, ppcRlwinm, gAux1, src1Reg, (32-sh1)%32, sh1, 31);				// sh1 can be 0!
							createIrArSSHMBME(code, ppcRlwimi, gAux1, src1RegLong, (32-sh1)%32, 0, (sh1-1+32)%32);			
							createIrArSSH(code, ppcSrawi, gAux2, src1RegLong, sh1);																																				
							createIrArSSHMBME(code, ppcRlwinm, gAux1, gAux2, shift, 32-shift, 31);		// shift right immediate by 64-shift	
							createIrDrAsimm(code, ppcAddi, gAux2, 0, 0);
							createIrDrArB(code, ppcAddc, gAux1, gAux1, src1Reg);							// add
							createIrDrArB(code, ppcAdde, gAux2, gAux2, src1RegLong);					 
							createIrArSSHMBME(code, ppcRlwinm, dReg, gAux1, 32-shift, shift, 31);				// shift right arithmetic immediate by shift
							createIrArSSHMBME(code, ppcRlwimi, dReg, gAux2, 32-shift, 0, shift-1);				
							createIrArSSH(code, ppcSrawi, dRegLong, gAux2, shift);															
						} else {
							int sh1 = shift % 32;
							createIrArSSH(code, ppcSrawi, gAux1, src1RegLong, (sh1-1+32)%32);				// shift right arithmetic immediate by shift-1
							createIrArSSH(code, ppcSrawi, gAux2, src1RegLong, 31);							// sh1 can be 0!							
							sh1 = (64 - shift) % 32;															// shift right immediate by 64-shift
							createIrArSSHMBME(code, ppcRlwinm, gAux1, gAux1, (32-sh1)%32, sh1, 31);		
							createIrArSSHMBME(code, ppcRlwimi, gAux1, gAux2, (32-sh1)%32, 0, (sh1-1)&0x1f);			
							createIrArSSHMBME(code, ppcRlwinm, gAux2, gAux2, (32-sh1)%32, sh1, 31);		
							createIrDrArB(code, ppcAddc, gAux1, gAux1, src1Reg);							// add
							createIrDrArB(code, ppcAdde, gAux2, gAux2, src1RegLong);					
							sh1 = shift % 32;
							createIrArSSH(code, ppcSrawi, dReg, gAux2, sh1);									// shift right arithmetic immediate by shift
							createIrArSSH(code, ppcSrawi, dRegLong, gAux2, 31);																
						}
					} else { // not a power of 2
						createICRFrAsimm(code, ppcCmpi, CRF1, src2RegLong, -1); // is divisor negative?
						createIBOBIBD(code, ppcBc, BOtrue, 4*CRF1+GT, 4);	
						createIrDrAsimm(code, ppcSubfic, gAux2, src2Reg, 0);	// negate divisor
						createIrDrA(code, ppcSubfze, gAux1, src2RegLong);
						createIBOBIBD(code, ppcBc, BOalways, 0, 3);
						createIrArSrB(code, ppcOr, gAux2, src2Reg, src2Reg); // copy if not negative
						createIrArSrB(code, ppcOr, gAux1, src2RegLong, src2RegLong);
						// test, if divisor = 0, if so, throw exception
						createICRFrAsimm(code, ppcCmpi, CRF3, gAux2, 0);
						createIBOBIBD(code, ppcBc, BOfalse, 4*CRF3+EQ, 3);	
						createItrap(code, ppcTwi, TOifequal, gAux1, 0);
						createIrDrArB(code, ppcDivw, 0, 0, 0);	// this instruction solely serves the trap handler to
						// identify that it's a arithmetic exception
						
						createIrSrAd(code, ppcStmw, 26, stackPtr, tempStorageOffset + 8);
						copyParametersSubroutine(code, src1RegLong, src1Reg, gAux1, gAux2);
						Method m = Method.getCompSpecSubroutine("divLong");
						loadConstantAndFixup(code, 26, m);	// use a register which contains no operand 
						createIrSspr(code, ppcMtspr, LR, 26);
						createIBOBILK(code, ppcBclr, BOalways, 0, true);

						createIrDrAd(code, ppcLmw, 27, stackPtr, tempStorageOffset + 8 + 4); // restore
						createIrArSrB(code, ppcOr, dReg, 26, 26);
						if (dReg != 26) // restore last register if not destination register
							createIrDrAd(code, ppcLwz, 26, stackPtr, tempStorageOffset + 8);
						createIrArSrB(code, ppcOr, dRegLong, 0, 0);
					}
					break;
				case tFloat:
					createIrDrArB(code, ppcFdivs, dReg, src1Reg, src2Reg);
					break;
				case tDouble:
					createIrDrArB(code, ppcFdiv, dReg, src1Reg, src2Reg);
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
						int immVal = ((StdConstant)opds[1].constant).valueH;	// is power of 2
						int shift = 0;
						while (immVal > 1) {shift++; immVal >>= 1;}
						if (shift == 0) 
							loadConstant(code, dReg, 0);
						else {
							createIrArSSH(code, ppcSrawi, 0, src1Reg, shift-1);
							createIrArSSHMBME(code, ppcRlwinm, 0, 0, shift, 32 - shift, 31);	
							createIrDrArB(code, ppcAdd, 0, src1Reg, 0);
							createIrArSSH(code, ppcSrawi, dReg, 0, shift);
							createIrArSSHMBME(code, ppcRlwinm, 0, dReg, shift, 0, 31-shift);
							createIrDrArB(code, ppcSubf, dReg, 0, src1Reg);
						}
					} else {
						createItrap(code, ppcTwi, TOifequal, src2Reg, 0);
						createIrDrArB(code, ppcDivw, 0, src1Reg, src2Reg);
						createIrDrArB(code, ppcMullw, 0, 0, src2Reg);
						createIrDrArB(code, ppcSubf, dReg ,0, src1Reg);
					}
					break;
				case tLong:
					if (src2Reg < 0) {	// is power of 2
						long immValLong = ((long)(((StdConstant)opds[1].constant).valueH)<<32) | (((StdConstant)opds[1].constant).valueL&0xFFFFFFFFL);
						int shift = 0;
						while (immValLong > 1) {shift++; immValLong >>= 1;}
						if (shift == 0) {
							loadConstant(code, dRegLong, 0);
							loadConstant(code, dReg, 0);
						} else if (shift < 32) {
							int sh1 = shift - 1;																// shift right arithmetic immediate by shift-1
							createIrArSSHMBME(code, ppcRlwinm, gAux1, src1Reg, (32-sh1)%32, sh1, 31);					
							createIrArSSHMBME(code, ppcRlwimi, gAux1, src1RegLong, (32-sh1)%32, 0, (sh1-1+32)%32);			
							createIrArSSH(code, ppcSrawi, gAux2, src1RegLong, sh1);																																				
							createIrArSSHMBME(code, ppcRlwinm, gAux1, gAux2, shift, 32 - shift, 31);		// shift right immediate by 64-shift	
							createIrDrAsimm(code, ppcAddi, gAux2, 0, 0);
							createIrDrArB(code, ppcAddc, gAux1, gAux1, src1Reg);							// add
							createIrDrArB(code, ppcAdde, gAux2, gAux2, src1RegLong);					 
							createIrArSSHMBME(code, ppcRlwinm, gAux1, gAux1, 32-shift, shift, 31);		// shift right arithmetic immediate by shift
							createIrArSSHMBME(code, ppcRlwimi, gAux1, gAux2, 32-shift, 0, shift-1);				
							createIrArSSH(code, ppcSrawi, gAux2, gAux2, shift);															
							
							createIrArSSHMBME(code, ppcRlwinm, 0, gAux1, shift, 32-shift, 31);					// multiply
							createIrArSSHMBME(code, ppcRlwimi, 0, gAux2, shift, 0, 31-shift);
							createIrArSSHMBME(code, ppcRlwinm, gAux1, gAux1, shift, 0, 31-shift);
							
							createIrDrArB(code, ppcSubfc, dReg, gAux1, src1Reg);									// subtract
							createIrDrArB(code, ppcSubfe, dRegLong, 0, src1RegLong);
						} else {
							int sh1 = shift % 32;
							createIrArSSH(code, ppcSrawi, gAux1, src1RegLong, (sh1-1+32)%32);				// shift right arithmetic immediate by shift-1
							createIrArSSH(code, ppcSrawi, gAux2, src1RegLong, 31);															
							sh1 = (64 - shift) % 32;															// shift right immediate by 64-shift
							createIrArSSHMBME(code, ppcRlwinm, gAux1, gAux1, (32-sh1)%32, sh1, 31);						
							createIrArSSHMBME(code, ppcRlwimi, gAux1, gAux2, (32-sh1)%32, 0, (sh1-1)&0x1f);					
							createIrArSSHMBME(code, ppcRlwinm, gAux2, gAux2, (32-sh1)%32, sh1, 31);		
							createIrDrArB(code, ppcAddc, gAux1, gAux1, src1Reg);							// add
							createIrDrArB(code, ppcAdde, gAux2, gAux2, src1RegLong);					 
							sh1 = shift % 32;																	// shift right arithmetic immediate by shift
							createIrArSSH(code, ppcSrawi, gAux1, gAux2, sh1);									
							createIrArSSH(code, ppcSrawi, gAux2, gAux2, 31);									
							
							createIrArSSHMBME(code, ppcRlwinm, gAux2, gAux1, shift-32, 0, 63-shift);		// multiply
							createIrDrAsimm(code, ppcAddi, gAux1, 0, 0);									
							
							createIrDrArB(code, ppcSubfc, dReg, gAux1, src1Reg);									// subtract
							createIrDrArB(code, ppcSubfe, dRegLong, gAux2, src1RegLong);
						}
					} else { // not a power of 2
						createICRFrAsimm(code, ppcCmpi, CRF1, src2RegLong, -1); // is divisor negative?
						createIBOBIBD(code, ppcBc, BOtrue, 4*CRF1+GT, 4);	
						createIrDrAsimm(code, ppcSubfic, gAux2, src2Reg, 0);	// negate divisor
						createIrDrA(code, ppcSubfze, gAux1, src2RegLong);
						createIBOBIBD(code, ppcBc, BOalways, 0, 3);
						createIrArSrB(code, ppcOr, gAux1, src2RegLong, src2RegLong); // copy if not negative
						createIrArSrB(code, ppcOr, gAux2, src2Reg, src2Reg);
						// test, if divisor = 0, if so, throw exception
						createICRFrAsimm(code, ppcCmpi, CRF1, src2Reg, 0);
						createIBOBIBD(code, ppcBc, BOfalse, 4*CRF1+EQ, 3);	
						createItrap(code, ppcTwi, TOifequal, src2RegLong, 0);
						createIrDrArB(code, ppcDivw, src2Reg, src2Reg, src2Reg);	// this instruction solely serves the trap handler to
						// identify that it's a arithmetic exception

						createIrSrAd(code, ppcStmw, 24, stackPtr, tempStorageOffset + 8);
						copyParametersSubroutine(code, src1RegLong, src1Reg, gAux1, gAux2);
						Method m = Method.getCompSpecSubroutine("remLong");
						loadConstantAndFixup(code, 24, m);	// use a register which contains no operand 
						createIrSspr(code, ppcMtspr, LR, 24);
						createIBOBILK(code, ppcBclr, BOalways, 0, true);

						createIrDrAd(code, ppcLmw, 25, stackPtr, tempStorageOffset + 8 + 4); // restore
						createIrArSrB(code, ppcOr, dReg, 24, 24);
						if (dReg != 24) // restore last register if not destination register
							createIrDrAd(code, ppcLwz, 24, stackPtr, tempStorageOffset + 8);
						createIrArSrB(code, ppcOr, dRegLong, 0, 0);
					}
					break;
				case tFloat:	// correct if a / b < 32 bit
					createIrDrArB(code, ppcFdiv, dReg, src1Reg, src2Reg);
					createIrDrB(code, ppcFctiwz, 0, dReg);
					createIrSrAd(code, ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(code, ppcLwz, gAux1, stackPtr, tempStorageOffset + 4);
					Item item = int2floatConst1;	// ref to 2^52+2^31;					
					createIrDrAsimm(code, ppcAddis, 0, 0, 0x4330);	// preload 2^52
					createIrSrAd(code, ppcStw, 0, stackPtr, tempStorageOffset);
					createIrArSuimm(code, ppcXoris, 0, gAux1, 0x8000);
					createIrSrAd(code, ppcStw, 0, stackPtr, tempStorageOffset+4);
					loadConstantAndFixup(code, gAux1, item);
					createIrDrAd(code, ppcLfd, dReg, gAux1, 0);
					createIrDrAd(code, ppcLfd, 0, stackPtr, tempStorageOffset);
					createIrDrArB(code, ppcFsub, dReg, 0, dReg);
					createIrDrArC(code, ppcFmul, dReg, dReg, src2Reg);
					createIrDrArB(code, ppcFsub, dReg, src1Reg, dReg);
					break;
				case tDouble:	// correct if a / b < 32 bit
					createIrDrArB(code, ppcFdiv, dReg, src1Reg, src2Reg);
					createIrDrB(code, ppcFctiwz, 0, dReg);
					createIrSrAd(code, ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(code, ppcLwz, gAux1, stackPtr, tempStorageOffset + 4);
					item = int2floatConst1;	// ref to 2^52+2^31;					
					createIrDrAsimm(code, ppcAddis, 0, 0, 0x4330);	// preload 2^52
					createIrSrAd(code, ppcStw, 0, stackPtr, tempStorageOffset);
					createIrArSuimm(code, ppcXoris, 0, gAux1, 0x8000);
					createIrSrAd(code, ppcStw, 0, stackPtr, tempStorageOffset+4);
					loadConstantAndFixup(code, gAux1, item);
					createIrDrAd(code, ppcLfd, dReg, gAux1, 0);
					createIrDrAd(code, ppcLfd, 0, stackPtr, tempStorageOffset);
					createIrDrArB(code, ppcFsub, dReg, 0, dReg);
					createIrDrArC(code, ppcFmul, dReg, dReg, src2Reg);
					createIrDrArB(code, ppcFsub, dReg, src1Reg, dReg);
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
					createIrDrA(code, ppcNeg, dReg, src1Reg);
					break;
				case tLong:
					createIrDrAsimm(code, ppcSubfic, dReg, src1Reg, 0);
					createIrDrA(code, ppcSubfze, dRegLong, src1RegLong);
					break;
				case tFloat: case tDouble:
					createIrDrB(code, ppcFneg, dReg, src1Reg);
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
						createIrArSSHMBME(code, ppcRlwinm, dReg, src1Reg, immVal, 0, 31-immVal);
					} else {
						createIrArSSHMBME(code, ppcRlwinm, 0, src2Reg, 0, 27, 31);
						createIrArSrB(code, ppcSlw, dReg, src1Reg, 0);
					}
				} else if (type == tLong) {
					if (src2Reg < 0) {	
						int immVal = ((StdConstant)opds[1].constant).valueH % 64;
						if (immVal < 32) {
							createIrArSSHMBME(code, ppcRlwinm, 0, src1Reg, immVal, 32-immVal, 31);
							createIrArSSHMBME(code, ppcRlwimi, 0, src1RegLong, immVal, 0, 31-immVal);
							createIrArSSHMBME(code, ppcRlwinm, dReg, src1Reg, immVal, 0, 31-immVal);
							createIrArSrB(code, ppcOr, dRegLong, 0, 0);
						} else {
							createIrArSSHMBME(code, ppcRlwinm, dRegLong, src1Reg, immVal-32, 0, 63-immVal);
							createIrDrAsimm(code, ppcAddi, dReg, 0, 0);
						}
					} else { 
						createIrDrAsimm(code, ppcSubfic, gAux1, src2Reg, 32);
						createIrArSrB(code, ppcSlw, dRegLong, src1RegLong, src2Reg);
						createIrArSrB(code, ppcSrw, 0, src1Reg, gAux1);
						createIrArSrB(code, ppcOr, dRegLong, dRegLong, 0);
						createIrDrAsimm(code, ppcAddi, gAux1, src2Reg, -32);
						createIrArSrB(code, ppcSlw, 0, src1Reg, gAux1);
						createIrArSrB(code, ppcOr, dRegLong, dRegLong, 0);
						createIrArSrB(code, ppcSlw, dReg, src1Reg, src2Reg);
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
						createIrArSSH(code, ppcSrawi, dReg, src1Reg, immVal);
					} else {
						createIrArSSHMBME(code, ppcRlwinm, 0, src2Reg, 0, 27, 31);
						createIrArSrB(code, ppcSraw, dReg, src1Reg, 0);
					}
				} else if (type == tLong) {
					if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH % 64;
						if (immVal == 0) {
							createIrArSrB(code, ppcOr, dReg, src1Reg, src1Reg);
							createIrArSrB(code, ppcOr, dRegLong, src1RegLong, src1RegLong);
						} else if (immVal < 32) {
							createIrArSSHMBME(code, ppcRlwinm, dReg, src1Reg, 32-immVal, immVal, 31);
							createIrArSSHMBME(code, ppcRlwimi, dReg, src1RegLong, 32-immVal, 0, immVal-1);
							createIrArSSH(code, ppcSrawi, dRegLong, src1RegLong, immVal);
						} else {
							immVal %= 32;
							createIrArSSH(code, ppcSrawi, dReg, src1RegLong, immVal);
							createIrArSSH(code, ppcSrawi, dRegLong, src1RegLong, 31);
						}
					} else {
						createIrArSSHMBME(code, ppcRlwinm, 0, src2Reg, 0, 26, 31);
						createIrDrAsimm(code, ppcSubfic, gAux1, 0, 32);
						createIrArSrB(code, ppcSrw, dReg, src1Reg, src2Reg);
						createIrArSrB(code, ppcSlw, 0, src1RegLong, gAux1);
						createIrArSrB(code, ppcOr, dReg, dReg, 0);
						createIrArSSHMBME(code, ppcRlwinm, 0, src2Reg, 0, 26, 31);
						createIrDrAsimm(code, ppcAddicp, gAux1, 0, -32);
						createIrArSrB(code, ppcSraw, 0, src1RegLong, gAux1);
						createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+GT, 2);
						createIrArSuimm(code, ppcOri, dReg, 0, 0);
						createIrArSrB(code, ppcSraw, dRegLong, src1RegLong, src2Reg);
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
						createIrArSSHMBME(code, ppcRlwinm, dReg, src1Reg, (32-immVal)%32, immVal, 31);
					} else {
						createIrArSSHMBME(code, ppcRlwinm, 0, src2Reg, 0, 27, 31);
						createIrArSrB(code, ppcSrw, dReg, src1Reg, 0);
					}
				} else if (type == tLong) {
					if (src2Reg < 0) {	
						int immVal = ((StdConstant)opds[1].constant).valueH % 64;
						if (immVal == 0) {
							createIrArSrB(code, ppcOr, dReg, src1Reg, src1Reg);
							createIrArSrB(code, ppcOr, dRegLong, src1RegLong, src1RegLong);
						} else if (immVal < 32) {
							createIrArSSHMBME(code, ppcRlwinm, dReg, src1Reg, (32-immVal)%32, immVal, 31);
							createIrArSSHMBME(code, ppcRlwimi, dReg, src1RegLong, (32-immVal)%32, 0, (immVal-1)&0x1f);
							createIrArSSHMBME(code, ppcRlwinm, dRegLong, src1RegLong, (32-immVal)%32, immVal, 31);
						} else {
							createIrArSSHMBME(code, ppcRlwinm, dReg, src1RegLong, (64-immVal)%32, immVal-32, 31);
							createIrDrAsimm(code, ppcAddi, dRegLong, 0, 0);
						}
					} else {
						createIrDrAsimm(code, ppcSubfic, gAux1, src2Reg, 32);
						createIrArSrB(code, ppcSrw, dReg, src1Reg, src2Reg);
						createIrArSrB(code, ppcSlw, 0, src1RegLong, gAux1);
						createIrArSrB(code, ppcOr, dReg, dReg, 0);
						createIrDrAsimm(code, ppcAddi, gAux1, src2Reg, -32);
						createIrArSrB(code, ppcSrw, 0, src1RegLong, gAux1);
						createIrArSrB(code, ppcOr, dReg, dReg, 0);
						createIrArSrB(code, ppcSrw, dRegLong, src1RegLong, src2Reg);
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
						if (immVal >= 0)
							createIrArSuimm(code, ppcAndi, dReg, src2Reg, immVal);
						else {
							createIrDrAsimm(code, ppcAddi, 0, 0, immVal);
							createIrArSrB(code, ppcAnd, dReg, 0, src2Reg);
						}
					} else if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						if (immVal >= 0)
							createIrArSuimm(code, ppcAndi, dReg, src1Reg, immVal);
						else {
							createIrDrAsimm(code, ppcAddi, 0, 0, immVal);
							createIrArSrB(code, ppcAnd, dReg, 0, src1Reg);
						}
					} else
						createIrArSrB(code, ppcAnd, dReg, src1Reg, src2Reg);
					break;
				case tLong:
					if (src1Reg < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueL;
						if (immVal >= 0) {
							createIrArSuimm(code, ppcAndi, dRegLong, src2RegLong, 0);
							createIrArSuimm(code, ppcAndi, dReg, src2Reg, (int)immVal);
						} else {
							createIrDrAsimm(code, ppcAddi, 0, 0, immVal);
							createIrArSrB(code, ppcOr, dRegLong, src2RegLong, src2RegLong);
							createIrArSrB(code, ppcAnd, dReg, src2Reg, 0);
						}
					} else if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueL;
						if (immVal >= 0) {
							createIrArSuimm(code, ppcAndi, dRegLong, src1RegLong, 0);
							createIrArSuimm(code, ppcAndi, dReg, src1Reg, (int)immVal);
						} else {
							createIrDrAsimm(code, ppcAddi, 0, 0, immVal);
							createIrArSrB(code, ppcOr, dRegLong, src1RegLong, src1RegLong);
							createIrArSrB(code, ppcAnd, dReg, src1Reg, 0);
						}
					} else {
						createIrArSrB(code, ppcAnd, dRegLong, src1RegLong, src2RegLong);
						createIrArSrB(code, ppcAnd, dReg, src1Reg, src2Reg);
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
						createIrArSuimm(code, ppcOri, dReg, src2Reg, immVal);
						if (immVal < 0)
							createIrArSuimm(code, ppcOris, dReg, dReg, 0xffff);					
					} else if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						createIrArSuimm(code, ppcOri, dReg, src1Reg, immVal);
						if (immVal < 0)
							createIrArSuimm(code, ppcOris, dReg, dReg, 0xffff);					
					} else
						createIrArSrB(code, ppcOr, dReg, src1Reg, src2Reg);
					break;
				case tLong:
					if (src1Reg < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueL;
						createIrArSrB(code, ppcOr, dRegLong, src2RegLong, src2RegLong);
						createIrArSuimm(code, ppcOri, dReg, src2Reg, (int)immVal);
						if (immVal < 0) {
							createIrArSuimm(code, ppcOris, dReg, dReg, 0xffff);	
							createIrDrAsimm(code, ppcAddi, dRegLong, 0, -1);	
						}
					} else if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueL;
						createIrArSrB(code, ppcOr, dRegLong, src1RegLong, src1RegLong);
						createIrArSuimm(code, ppcOri, dReg, src1Reg, (int)immVal);
						if (immVal < 0) {
							createIrArSuimm(code, ppcOris, dReg, dReg, 0xffff);					
							createIrDrAsimm(code, ppcAddi, dRegLong, 0, -1);	
						}
					} else {
						createIrArSrB(code, ppcOr, dRegLong, src1RegLong, src2RegLong);
						createIrArSrB(code, ppcOr, dReg, src1Reg, src2Reg);
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
						createIrArSuimm(code, ppcXori, dReg, src2Reg, immVal);
						if (immVal < 0)
							createIrArSuimm(code, ppcXoris, dReg, dReg, 0xffff);
						else 
							createIrArSuimm(code, ppcXoris, dReg, dReg, 0);
					} else if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						createIrArSuimm(code, ppcXori, dReg, src1Reg, immVal);
						if (immVal < 0)
							createIrArSuimm(code, ppcXoris, dReg, dReg, 0xffff);
						else 
							createIrArSuimm(code, ppcXoris, dReg, dReg, 0);
					} else
						createIrArSrB(code, ppcXor, dReg, src1Reg, src2Reg);
					break;
				case tLong:
					if (src1Reg < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueL;
						createIrArSuimm(code, ppcXori, dReg, src2Reg, (int)immVal);
						if (immVal < 0) {
							createIrArSuimm(code, ppcXoris, dReg, dReg, 0xffff);
							createIrArSuimm(code, ppcXori, dRegLong, src2RegLong, 0xffff);
							createIrArSuimm(code, ppcXoris, dRegLong, dRegLong, 0xffff);
						} else {
							createIrArSrB(code, ppcOr, dRegLong, src2RegLong, src2RegLong);
							createIrArSuimm(code, ppcXoris, dReg, dReg, 0);
						}
					} else if (src2Reg < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueL;
						createIrArSuimm(code, ppcXori, dReg, src1Reg, (int)immVal);
						if (immVal < 0) {
							createIrArSuimm(code, ppcXoris, dReg, dReg, 0xffff);
							createIrArSuimm(code, ppcXori, dRegLong, src1RegLong, 0xffff);
							createIrArSuimm(code, ppcXoris, dRegLong, dRegLong, 0xffff);
						} else {
							createIrArSrB(code, ppcOr, dRegLong, src1RegLong, src1RegLong);
							createIrArSuimm(code, ppcXoris, dReg, dReg, 0);
						}
					} else {
						createIrArSrB(code, ppcXor, dRegLong, src1RegLong, src2RegLong);
						createIrArSrB(code, ppcXor, dReg, src1Reg, src2Reg);
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
					createIrArS(code, ppcExtsb, dReg, src1Reg);
					break;
				case tChar: 
					createIrArSSHMBME(code, ppcRlwinm, dReg, src1Reg, 0, 16, 31);
					break;
				case tShort: 
					createIrArS(code, ppcExtsh, dReg, src1Reg);
					break;
				case tLong:
					createIrArSrB(code, ppcOr, dReg, src1Reg, src1Reg);
					createIrArSSH(code, ppcSrawi, dRegLong, src1Reg, 31);
					break;
				case tFloat:
					Item item = int2floatConst1;	// ref to 2^52+2^31;					
					createIrDrAsimm(code, ppcAddis, 0, 0, 0x4330);	// preload 2^52
					createIrSrAd(code, ppcStw, 0, stackPtr, tempStorageOffset);
					createIrArSuimm(code, ppcXoris, 0, src1Reg, 0x8000);
					createIrSrAd(code, ppcStw, 0, stackPtr, tempStorageOffset+4);
					loadConstantAndFixup(code, gAux1, item);
					createIrDrAd(code, ppcLfd, dReg, gAux1, 0);
					createIrDrAd(code, ppcLfd, 0, stackPtr, tempStorageOffset);
					createIrDrArB(code, ppcFsub, dReg, 0, dReg);
					createIrDrB(code, ppcFrsp, dReg, dReg);
					break;
				case tDouble:
//					instructions[iCount] = ppcMtfsfi | (7 << 23) | (4  << 12);
//					incInstructionNum();
					item = int2floatConst1;	// ref to 2^52+2^31;					
					createIrDrAsimm(code, ppcAddis, 0, 0, 0x4330);	// preload 2^52
					createIrSrAd(code, ppcStw, 0, stackPtr, tempStorageOffset);
					createIrArSuimm(code, ppcXoris, 0, src1Reg, 0x8000);
					createIrSrAd(code, ppcStw, 0, stackPtr, tempStorageOffset+4);
					loadConstantAndFixup(code, gAux1, item);
					createIrDrAd(code, ppcLfd, dReg, gAux1, 0);
					createIrDrAd(code, ppcLfd, 0, stackPtr, tempStorageOffset);
					createIrDrArB(code, ppcFsub, dReg, 0, dReg);
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
					createIrArS(code, ppcExtsb, dReg, src1Reg);
					break;
				case tChar: 
					createIrArSSHMBME(code, ppcRlwinm, dReg, src1Reg, 0, 16, 31);
					break;
				case tShort: 
					createIrArS(code, ppcExtsh, dReg, src1Reg);
					break;
				case tInteger:
					createIrArSrB(code, ppcOr, dReg, src1Reg, src1Reg);
					break;
				case tFloat:
					createIrSrAd(code, ppcStmw, 29, stackPtr, tempStorageOffset + 8);
					Method m = Method.getCompSpecSubroutine("longToDouble");
					copyParametersSubroutine(code, src1RegLong, src1Reg, 0, 0);
					loadConstantAndFixup(code, 29, m);	// use a register which contains no operand 
					createIrSspr(code, ppcMtspr, LR, 29);
					createIBOBILK(code, ppcBclr, BOalways, 0, true);
					createIrDrAd(code, ppcLmw, 29, stackPtr, tempStorageOffset + 8);
					createIrDrB(code, ppcFmr, dReg, 0);	// get result
					createIrDrB(code, ppcFrsp, dReg, dReg);
					break;
				case tDouble:
					createIrSrAd(code, ppcStmw, 29, stackPtr, tempStorageOffset + 8);
					m = Method.getCompSpecSubroutine("longToDouble");
					copyParametersSubroutine(code, src1RegLong, src1Reg, 0, 0);
					loadConstantAndFixup(code, 29, m);	// use a register which contains no operand 
					createIrSspr(code, ppcMtspr, LR, 29);
					createIBOBILK(code, ppcBclr, BOalways, 0, true);
					createIrDrAd(code, ppcLmw, 29, stackPtr, tempStorageOffset + 8);
					createIrDrB(code, ppcFmr, dReg, 0);	// get result
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
					createIrDrB(code, ppcFctiw, 0, src1Reg);
					createIrSrAd(code, ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(code, ppcLwz, 0, stackPtr, tempStorageOffset + 4);
					createIrArS(code, ppcExtsb, dReg, 0);
					break;
				case tChar: 
					createIrDrB(code, ppcFctiw, 0, src1Reg);
					createIrSrAd(code, ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(code, ppcLwz, 0, stackPtr, tempStorageOffset + 4);
					createIrArSSHMBME(code, ppcRlwinm, dReg, 0, 0, 16, 31);
					break;
				case tShort: 
					createIrDrB(code, ppcFctiw, 0, src1Reg);
					createIrSrAd(code, ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(code, ppcLwz, 0, stackPtr, tempStorageOffset + 4);
					createIrArS(code, ppcExtsh, dReg, 0);
					break;
				case tInteger:
					createIrDrB(code, ppcFctiwz, 0, src1Reg);
					createIrSrAd(code, ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(code, ppcLwz, dReg, stackPtr, tempStorageOffset + 4);
					break;
				case tLong:	
					createIrSrAd(code, ppcStmw, 28, stackPtr, tempStorageOffset + 8);
					Method m = Method.getCompSpecSubroutine("doubleToLong");
					createIrDrB(code, ppcFmr, 0, src1Reg);
					loadConstantAndFixup(code, 29, m);	// use a register which contains no operand 
					createIrSspr(code, ppcMtspr, LR, 29);
					createIBOBILK(code, ppcBclr, BOalways, 0, true);
					createIrDrAd(code, ppcLmw, 29, stackPtr, tempStorageOffset + 8 + 4); // restore
					createIrArSrB(code, ppcOr, dReg, 28, 28);
					if (dReg != 28) // restore last register if not destination register
						createIrDrAd(code, ppcLwz, 28, stackPtr, tempStorageOffset + 8);
					createIrArSrB(code, ppcOr, dRegLong, 0, 0);
					break;
				case tDouble:
					createIrDrB(code, ppcFmr, dReg, src1Reg);
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
					createIrDrB(code, ppcFctiw, 0, src1Reg);
					createIrSrAd(code, ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(code, ppcLwz, 0, stackPtr, tempStorageOffset + 4);
					createIrArS(code, ppcExtsb, dReg, 0);
					break;
				case tChar: 
					createIrDrB(code, ppcFctiw, 0, src1Reg);
					createIrSrAd(code, ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(code, ppcLwz, 0, stackPtr, tempStorageOffset + 4);
					createIrArSSHMBME(code, ppcRlwinm, dReg, 0, 0, 16, 31);
					break;
				case tShort: 
					createIrDrB(code, ppcFctiw, 0, src1Reg);
					createIrSrAd(code, ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(code, ppcLwz, 0, stackPtr, tempStorageOffset + 4);
					createIrArS(code, ppcExtsh, dReg, 0);
					break;
				case tInteger:
					createIrDrB(code, ppcFctiwz, 0, src1Reg);
					createIrSrAd(code, ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(code, ppcLwz, dReg, stackPtr, tempStorageOffset + 4);
					break;
				case tLong:	
					createIrSrAd(code, ppcStmw, 28, stackPtr, tempStorageOffset + 8);
					Method m = Method.getCompSpecSubroutine("doubleToLong");
					createIrDrB(code, ppcFmr, 0, src1Reg);
					loadConstantAndFixup(code, 29, m);	// use a register which contains no operand 
					createIrSspr(code, ppcMtspr, LR, 29);
					createIBOBILK(code, ppcBclr, BOalways, 0, true);
					createIrDrAd(code, ppcLmw, 29, stackPtr, tempStorageOffset + 8 + 4); // restore
					createIrArSrB(code, ppcOr, dReg, 28, 28);
					if (dReg != 28) // restore last register if not destination register
						createIrDrAd(code, ppcLwz, 28, stackPtr, tempStorageOffset + 8);
					createIrArSrB(code, ppcOr, dRegLong, 0, 0);
					break;
				case tFloat:
					createIrDrB(code, ppcFrsp, dReg, src1Reg);
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
					createICRFrArB(code, ppcCmp, CRF0, src1RegLong, src2RegLong);
					createICRFrArB(code, ppcCmpl, CRF1, src1Reg, src2Reg);
					instr = node.instructions[i+1];
					if (instr.ssaOpcode == sCregMove) {i++; instr = node.instructions[i+1]; assert false;}
					assert instr.ssaOpcode == sCbranch : "sCcompl or sCcompg is not followed by branch instruction";
					int bci = meth.cfg.code[node.lastBCA] & 0xff;
					if (bci == bCifeq) {
						createIcrbDcrbAcrbB(code, ppcCrand, CRF0EQ, CRF0EQ, CRF1EQ);
						createIBOBIBD(code, ppcBc, BOtrue, CRF0EQ, 0);
					} else if (bci == bCifne) {
						createIcrbDcrbAcrbB(code, ppcCrand, CRF0EQ, CRF0EQ, CRF1EQ);
						createIBOBIBD(code, ppcBc, BOfalse, CRF0EQ, 0);
					} else if (bci == bCiflt) {
						createIcrbDcrbAcrbB(code, ppcCrand, CRF1LT, CRF0EQ, CRF1LT);
						createIcrbDcrbAcrbB(code, ppcCror, CRF0LT, CRF1LT, CRF0LT);
						createIBOBIBD(code, ppcBc, BOtrue, CRF0LT, 0);
					} else if (bci == bCifge) {
						createIcrbDcrbAcrbB(code, ppcCrand, CRF1LT, CRF0EQ, CRF1LT);
						createIcrbDcrbAcrbB(code, ppcCror, CRF0LT, CRF1LT, CRF0LT);
						createIBOBIBD(code, ppcBc, BOfalse, CRF0LT, 0);
					} else if (bci == bCifgt) {
						createIcrbDcrbAcrbB(code, ppcCrand, CRF1LT, CRF0EQ, CRF1GT);
						createIcrbDcrbAcrbB(code, ppcCror, CRF0LT, CRF1LT, CRF0GT);
						createIBOBIBD(code, ppcBc, BOtrue, CRF0LT, 0);
					} else if (bci == bCifle) {
						createIcrbDcrbAcrbB(code, ppcCrand, CRF1LT, CRF0EQ, CRF1GT);
						createIcrbDcrbAcrbB(code, ppcCror, CRF0LT, CRF1LT, CRF0GT);
						createIBOBIBD(code, ppcBc, BOfalse, CRF0LT, 0);
					} else {
						ErrorReporter.reporter.error(623);
						assert false : "sCcompl or sCcompg is not followed by branch instruction";
						return;
					}
				} else if (type == tFloat  || type == tDouble) {
					createICRFrArB(code, ppcFcmpu, CRF1, src1Reg, src2Reg);
					instr = node.instructions[i+1];
					assert instr.ssaOpcode == sCbranch : "sCcompl or sCcompg is not followed by branch instruction";
					int bci = meth.cfg.code[node.lastBCA] & 0xff;
					if (bci == bCifeq) 
						createIBOBIBD(code, ppcBc, BOtrue, CRF1EQ, 0);
					else if (bci == bCifne)
						createIBOBIBD(code, ppcBc, BOfalse, CRF1EQ, 0);
					else if (bci == bCiflt)
						createIBOBIBD(code, ppcBc, BOtrue, CRF1LT, 0);
					else if (bci == bCifge)
						createIBOBIBD(code, ppcBc, BOfalse, CRF1LT, 0);
					else if (bci == bCifgt)
						createIBOBIBD(code, ppcBc, BOtrue, CRF1GT, 0);
					else if (bci == bCifle)
						createIBOBIBD(code, ppcBc, BOfalse, CRF1GT, 0);
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
				i++;
				break;}
			case sCinstanceof: {
				MonadicRef ref = (MonadicRef)instr;
				Type t = (Type)ref.item;
				if (t.category == tcRef) {	// object (to test for) is regular class or interface
					if ((t.accAndPropFlags & (1<<apfInterface)) != 0) {	// object is interface
						createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, 0);	// is null?
						createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 3);	// jump to label 2
						// label 1
						createIrDrAsimm(code, ppcAddi, dReg, 0, 0);
						createIBOBIBD(code, ppcBc, BOalways, 4*CRF0, 13);	// jump to end
						// label 2
						createIrDrAd(code, ppcLbz, gAux1, src1Reg, -7);	// get array bit
						createICRFrAsimm(code, ppcCmpi, CRF0, gAux1, 0);	// is array?
						createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, -4);	// jump to label 1
						createIrDrAd(code, ppcLwz, gAux1, src1Reg, -4);	// get tag
						createIrDrAd(code, ppcLwz, 0, gAux1, Linker32.tdIntfTypeChkTableOffset);
						createIrDrArB(code, ppcAdd, gAux1, gAux1, 0);
						// label 3
						createIrDrAd(code, ppcLhzu, 0, gAux1, 0);
						createICRFrAsimm(code, ppcCmpi, CRF0, 0, ((Class)t).chkId);	// is interface chkId
						createIrDrAsimm(code, ppcAddi, gAux1, gAux1, 2);
						createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+GT, -3);	// jump to label 3			
						createIrD(code, ppcMfcr, dReg);
						createIrArSSHMBME(code, ppcRlwinm, dReg, dReg, 3, 31, 31);
					} else {	// regular class
						int offset = ((Class)t).extensionLevel;
						if (t.name.equals(HString.getHString("java/lang/Object"))) {
							createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, 0);	// is null?
							createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 3);	// jump to label 1
							createIrDrAsimm(code, ppcAddi, dReg, 0, 0);
							createIBOBIBD(code, ppcBc, BOalways, 4*CRF0, 2);	// jump to end
							// label 1
							createIrDrAsimm(code, ppcAddi, dReg, 0, 1);
						} else { // regular class but not java/lang/Object
							createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, 0);	// is null?
							createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 3);	// jump to label 2
							// label 1
							createIrDrAsimm(code, ppcAddi, dReg, 0, 0);
							createIBOBIBD(code, ppcBc, BOalways, 4*CRF0, 11);	// jump to end
							// label 2
							createIrDrAd(code, ppcLbz, gAux1, src1Reg, -7);	// get array bit
							createICRFrAsimm(code, ppcCmpi, CRF0, gAux1, 0);	// is array?
							createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, -4);	// jump to label 1
							createIrDrAd(code, ppcLwz, gAux1, src1Reg, -4);	// get tag
							createIrDrAd(code, ppcLwz, 0, gAux1, Linker32.tdBaseClass0Offset + offset * 4);
							loadConstantAndFixup(code, gAux1, t);	// addr of type
							createICRFrArB(code, ppcCmpl, CRF0, 0, gAux1);
							createIrD(code, ppcMfcr, dReg);
							createIrArSSHMBME(code, ppcRlwinm, dReg, dReg, 3, 31, 31);
						}
					}
				} else {	// object is an array
					if (((Array)t).componentType.category == tcPrimitive) {  // array of base type
						// test if not null
						createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, 0);	// is null?
						createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 3);	// jump to label 2
						// label 1
						createIrDrAsimm(code, ppcAddi, dReg, 0, 0);
						createIBOBIBD(code, ppcBc, BOalways, 4*CRF0, 10);	// jump to end
						// label 2
						createIrDrAd(code, ppcLbz, gAux1, src1Reg, -7);	// get array bit
						createICRFrAsimm(code, ppcCmpi, CRF0, gAux1, 0);	// is not array?
						createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, -4);	// jump to label 1
						createIrDrAd(code, ppcLwz, 0, src1Reg, -4);	// get tag
						loadConstantAndFixup(code, gAux1, t);	// addr of type
						createICRFrArB(code, ppcCmpl, CRF0, 0, gAux1);
						createIrD(code, ppcMfcr, dReg);
						createIrArSSHMBME(code, ppcRlwinm, dReg, dReg, 3, 31, 31);
					} else {	// array of regular classes or interfaces
						int nofDim = ((Array)t).dimension;
						Item compType = RefType.refTypeList.getItemByName(((Array)t).componentType.name.toString());
						int offset = ((Class)(((Array)t).componentType)).extensionLevel;
						if (((Array)t).componentType.name.equals(HString.getHString("java/lang/Object"))) {
							// test if not null
							createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, 0);	// is null?
							createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 3);	// jump to label 2
							// label 1
							createIrDrAsimm(code, ppcAddi, dReg, 0, 0);
							createIBOBIBD(code, ppcBc, BOalways, 4*CRF0, 16);	// jump to end
							// label 2
							createIrDrAd(code, ppcLbz, gAux1, src1Reg, -7);	// get array bit
							createICRFrAsimm(code, ppcCmpi, CRF0, gAux1, 0);	// is not array?
							createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, -4);	// jump to label 1

							createIrDrAd(code, ppcLwz, gAux1, src1Reg, -4);	// get tag
							createIrDrAd(code, ppcLwz, 0, gAux1, 0);	
							createIrArSSHMBME(code, ppcRlwinm, gAux1, 0, 16, 17, 31);	// get dim
							createICRFrAsimm(code, ppcCmpi, CRF0, 0, 0);	// check if array of primitive type
							createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+LT, 5);	// jump to label 3					
							createICRFrAsimm(code, ppcCmpi, CRF0, gAux1, nofDim);
							createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+LT, -11);	// jump to label 1	
							createIrDrAsimm(code, ppcAddi, dReg, 0, 1);
							createIBOBIBD(code, ppcBc, BOalways, 4*CRF0, 4);	// jump to end
							// label 3, is array of primitive type
							createICRFrAsimm(code, ppcCmpi, CRF0, gAux1, nofDim);
							createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+GT, -15);	// jump to label 1	
							createIrDrAsimm(code, ppcAddi, dReg, 0, 1);
						} else {	// array of regular classes or interfaces but not java/lang/Object
							if ((compType.accAndPropFlags & (1<<apfInterface)) != 0) {	// array of interfaces
								createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, 0);	// is null?
								createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 3);	// jump to label 2
								// label 1
								createIrDrAsimm(code, ppcAddi, dReg, 0, 0);
								createIBOBIBD(code, ppcBc, BOalways, 4*CRF0, 20);	// jump to end
								// label 2
								createIrDrAd(code, ppcLbz, gAux1, src1Reg, -7);	// get array bit
								createICRFrAsimm(code, ppcCmpi, CRF0, gAux1, 0);	// is not array?
								createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, -4);	// jump to label 1

								createIrDrAd(code, ppcLwz, gAux1, src1Reg, -4);	// get tag
								createIrDrAd(code, ppcLwz, 0, gAux1, 0);			
								createIrArSSHMBME(code, ppcRlwinm, 0, 0, 16, 17, 31);	// get dim
								createICRFrAsimm(code, ppcCmpi, CRF0, 0, nofDim);
								createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, -9);	// jump to label 1					

								createIrDrAd(code, ppcLwz, gAux1, gAux1, 8 + nofDim * 4);	// get component type
								createICRFrAsimm(code, ppcCmpi, CRF0, gAux1, 0);	// is 0?
								createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, -12);	// jump to label 1					

								createIrDrAd(code, ppcLwz, 0, gAux1, Linker32.tdIntfTypeChkTableOffset);
								createIrDrArB(code, ppcAdd, gAux1, gAux1, 0);
								// label 3
								createIrDrAd(code, ppcLhzu, 0, gAux1, 0);
								createICRFrAsimm(code, ppcCmpi, CRF0, 0, ((Class)compType).chkId);	// is interface chkId
								createIrDrAsimm(code, ppcAddi, gAux1, gAux1, 2);
								createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+GT, -3);	// jump to label 3			
								createIrD(code, ppcMfcr, dReg);	
								createIrArSSHMBME(code, ppcRlwinm, dReg, dReg, 3, 31, 31);			
							} else {	// array of regular classes
								// test if not null
								createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, 0);	// is null?
								createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 3);	// jump to label 2
								// label 1
								createIrDrAsimm(code, ppcAddi, dReg, 0, 0);
								createIBOBIBD(code, ppcBc, BOalways, 4*CRF0, 18);	// jump to end
								// label 2
								createIrDrAd(code, ppcLbz, gAux1, src1Reg, -7);	// get array bit
								createICRFrAsimm(code, ppcCmpi, CRF0, gAux1, 0);	// is not array?
								createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, -4);	// jump to label 1

								createIrDrAd(code, ppcLwz, gAux1, src1Reg, -4);	// get tag
								createIrDrAd(code, ppcLwz, 0, gAux1, 0);	
								createIrArSSHMBME(code, ppcRlwinm, 0, 0, 16, 17, 31);	// get dim
								createICRFrAsimm(code, ppcCmpi, CRF0, 0, nofDim);
								createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, -9);	// jump to label 1					

								createIrDrAd(code, ppcLwz, gAux1, gAux1, 8 + nofDim * 4);	// get component type
								createICRFrAsimm(code, ppcCmpi, CRF0, gAux1, 0);	// is 0?
								createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, -12);	// jump to label 1					

								createIrDrAd(code, ppcLwz, 0, gAux1, Linker32.tdBaseClass0Offset + offset * 4);
								loadConstantAndFixup(code, gAux1, compType);	// addr of component type
								createICRFrArB(code, ppcCmpl, CRF0, 0, gAux1);
								createIrD(code, ppcMfcr, dReg);
								createIrArSSHMBME(code, ppcRlwinm, dReg, dReg, 3, 31, 31);
							}
						}
					}
				}
				break;}
			case sCcheckcast: {
				// this ssa instruction must be translated, so that only "twi, TOifnequal" is used
				// this enables the trap handler to throw a ClassCastException
				MonadicRef ref = (MonadicRef)instr;
				Type t = (Type)ref.item;
				if (t.category == tcRef) {	// object (to test for) is regular class or interface
					if ((t.accAndPropFlags & (1<<apfInterface)) != 0) {	// object is interface
						createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, 0);	// is null?
						createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, 11);	// jump to end
						createIrDrAd(code, ppcLbz, gAux1, src1Reg, -7);	// get array bit
						createItrapSimm(code, ppcTwi, TOifnequal, gAux1, 0);	// is not array?
						createIrDrAd(code, ppcLwz, gAux1, src1Reg, -4);	// get tag
						createIrDrAd(code, ppcLwz, 0, gAux1, Linker32.tdIntfTypeChkTableOffset);
						createIrDrArB(code, ppcAdd, gAux1, gAux1, 0);
						// label 1
						createIrDrAd(code, ppcLhzu, 0, gAux1, 0);
						createICRFrAsimm(code, ppcCmpi, CRF0, 0, ((Class)t).chkId);	// is interface chkId
						createIrDrAsimm(code, ppcAddi, gAux1, gAux1, 2);
						createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+GT, -3);	// jump to label 1			
						createItrapSimm(code, ppcTwi, TOifnequal, 0, ((Class)t).chkId);	// chkId is not equal
					} else {	// object is regular class
						int offset = ((Class)t).extensionLevel;
						createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, 0);	// is null?
						createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, 8);	// jump to end
						createIrDrAd(code, ppcLbz, gAux1, src1Reg, -7);	// get array bit
						createItrapSimm(code, ppcTwi, TOifnequal, gAux1, 0);	// is not array?
						createIrDrAd(code, ppcLwz, gAux1, src1Reg, -4);	// get tag
						createIrDrAd(code, ppcLwz, 0, gAux1, Linker32.tdBaseClass0Offset + offset * 4);
						loadConstantAndFixup(code, gAux1, t);	// addr of type
						createItrap(code, ppcTw, TOifnequal, gAux1, 0);
					}
				} else {	// object (to test for) is an array
					if (((Array)t).componentType.category == tcPrimitive) {  // array of base type
						createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, 0);	// is null?
						createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, 8);	// jump to end
						createIrDrAd(code, ppcLbz, gAux1, src1Reg, -7);	// get array bit
						createIrArSuimm(code, ppcAndi, gAux1, gAux1, 0x80);
						createItrapSimm(code, ppcTwi, TOifnequal, gAux1, 0x80);	// is array?
						createIrDrAd(code, ppcLwz, 0, src1Reg, -4);	// get tag
						loadConstantAndFixup(code, gAux1, t);	// addr of type
						createItrap(code, ppcTw, TOifnequal, gAux1, 0);
					} else {	// array of regular classes or interfaces
						int nofDim = ((Array)t).dimension;
						Item compType = RefType.refTypeList.getItemByName(((Array)t).componentType.name.toString());
						if (((Array)t).componentType.name.equals(HString.getHString("java/lang/Object"))) {
							createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, 0);	// is null?
							createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, 15);	// jump to end
							createIrDrAd(code, ppcLbz, gAux1, src1Reg, -7);	// get array bit
							createIrArSuimm(code, ppcAndi, gAux1, gAux1, 0x80);
							createItrapSimm(code, ppcTwi, TOifnequal, gAux1, 0x80);	// is array?

							createIrDrAd(code, ppcLwz, gAux1, src1Reg, -4);	// get tag
							createIrDrAd(code, ppcLwz, 0, gAux1, 0);	
							createIrArSSHMBME(code, ppcRlwinm, gAux1, 0, 16, 17, 31);	// get dim
							createICRFrAsimm(code, ppcCmpi, CRF0, 0, 0);	// check if array of primitive type
							createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+LT, 4);	// jump to label 3	
							
							createICRFrAsimm(code, ppcCmpi, CRF0, gAux1, nofDim);
							createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+LT, 5);	// jump to end	
							createItrap(code, ppcTwi, TOifnequal, gAux1, -1);	// trap always
							// label 3, is array of primitive type
							createICRFrAsimm(code, ppcCmpi, CRF0, gAux1, nofDim);
							createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+GT, 2);	// jump to end	
							createItrap(code, ppcTwi, TOifnequal, gAux1, -1);	// trap always
						} else {	// array of regular classes or interfaces but not java/lang/Object
							if ((compType.accAndPropFlags & (1<<apfInterface)) != 0) {	// array of interfaces
								createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, 0);	// is null?
								createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, 19);	// jump to end
								createIrDrAd(code, ppcLbz, gAux1, src1Reg, -7);	// get array bit
								createIrArSuimm(code, ppcAndi, gAux1, gAux1, 0x80);
								createItrapSimm(code, ppcTwi, TOifnequal, gAux1, 0x80);	// is array?

								createIrDrAd(code, ppcLwz, gAux1, src1Reg, -4);	// get tag
								createIrDrAd(code, ppcLwz, 0, gAux1, 0);	
								createIrArSSHMBME(code, ppcRlwinm, 0, 0, 16, 17, 31);	// get dim
								createItrapSimm(code, ppcTwi, TOifnequal, 0, nofDim);	// check dim

								createIrDrAd(code, ppcLwz, gAux1, gAux1, 8 + nofDim * 4);	// get component type
								createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, 0);	// is null?
								createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 2);
								createItrapSimm(code, ppcTwi, TOifnequal, src1Reg, -1);	// is 0?
								createIrDrAd(code, ppcLwz, 0, gAux1, Linker32.tdIntfTypeChkTableOffset);
								createIrDrArB(code, ppcAdd, gAux1, gAux1, 0);
								// label 1
								createIrDrAd(code, ppcLhzu, 0, gAux1, 0);
								createICRFrAsimm(code, ppcCmpi, CRF0, 0, ((Class)compType).chkId);	// is interface chkId
								createIrDrAsimm(code, ppcAddi, gAux1, gAux1, 2);
								createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+GT, -3);	// jump to label 1			
								createItrapSimm(code, ppcTwi, TOifnequal, 0, ((Class)compType).chkId);	// chkId is not equal
							} else {	// array of regular classes
								int offset = ((Class)(((Array)t).componentType)).extensionLevel;
								createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, 0);	// is null?
								createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, 16);	// jump to end
								createIrDrAd(code, ppcLbz, gAux1, src1Reg, -7);	// get array bit
								createIrArSuimm(code, ppcAndi, gAux1, gAux1, 0x80);
								createItrapSimm(code, ppcTwi, TOifnequal, gAux1, 0x80);	// is array?

								createIrDrAd(code, ppcLwz, gAux1, src1Reg, -4);	// get tag
								createIrDrAd(code, ppcLwz, 0, gAux1, 0);	
								createIrArSSHMBME(code, ppcRlwinm, 0, 0, 16, 17, 31);	// get dim
								createItrapSimm(code, ppcTwi, TOifnequal, 0, nofDim);	// check dim

								createIrDrAd(code, ppcLwz, gAux1, gAux1, 8 + nofDim * 4);	// get component type
								createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, 0);	// is null?
								createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 2);
								createItrapSimm(code, ppcTwi, TOifnequal, src1Reg, -1);	// is 0?

								createIrDrAd(code, ppcLwz, 0, gAux1, Linker32.tdBaseClass0Offset + offset * 4);
								loadConstantAndFixup(code, gAux1, compType);	// addr of component type
								createItrap(code, ppcTw, TOifnequal, gAux1, 0);
							}
						}
					}
				}
				break;}
			case sCthrow: {
				createIrArSrB(code, ppcOr, paramStartGPR, src1Reg, src1Reg);	// put exception into parameter register
				createItrap(code, ppcTw, TOalways, 0, 0);
				break;}
			case sCalength: {
				int refReg = src1Reg;
				createItrap(code, ppcTwi, TOifequal, refReg, 0);
				createIrDrAd(code, ppcLha , dReg, refReg, -arrayLenOffset);
				break;}
			case sCcall: {
				Call call = (Call)instr;
				Method m = (Method)call.item;
				if ((m.accAndPropFlags & (1 << dpfSynthetic)) != 0) {
					if (m.id == idGET1) {	// GET1
						createIrDrAd(code, ppcLbz, dReg, src1Reg, 0);
						createIrArS(code, ppcExtsb, dReg, dReg);
					} else if (m.id == idGET2) { // GET2
						createIrDrAd(code, ppcLha, dReg, src1Reg, 0);
					} else if (m.id == idGET4) { // GET4
						createIrDrAd(code, ppcLwz, dReg, src1Reg, 0);
					} else if (m.id == idGET8) { // GET8
						createIrDrAd(code, ppcLwz, dRegLong, src1Reg, 0);
						createIrDrAd(code, ppcLwz, dReg, src1Reg, 4);
					} else if (m.id == idPUT1) { // PUT1
						createIrSrAd(code, ppcStb, src2Reg, src1Reg, 0);
					} else if (m.id == idPUT2) { // PUT2
						createIrSrAd(code, ppcSth, src2Reg, src1Reg, 0);
					} else if (m.id == idPUT4) { // PUT4
						createIrSrAd(code, ppcStw, src2Reg, src1Reg, 0);
					} else if (m.id == idPUT8) { // PUT8
						createIrSrAd(code, ppcStw, src2RegLong, src1Reg, 0);
						createIrSrAd(code, ppcStw, src2Reg, src1Reg, 4);
					} else if (m.id == idBIT) { // BIT
						createIrDrAd(code, ppcLbz, dReg, src1Reg, 0);
						createIrDrAsimm(code, ppcSubfic, 0, src2Reg, 32);
						createIrArSrBMBME(code, ppcRlwnm, dReg, dReg, 0, 31, 31);
					} else if (m.id == idGETGPR) { // GETGPR
						int gpr = ((StdConstant)opds[0].constant).valueH;
						createIrArSrB(code, ppcOr, dReg, gpr, gpr);
					} else if (m.id == idGETFPR) { // GETFPR
						int fpr = ((StdConstant)opds[0].constant).valueH;
						createIrDrB(code, ppcFmr, dReg, fpr);
					} else if (m.id == idGETSPR) { // GETSPR
						int spr = ((StdConstant)opds[0].constant).valueH;
						createIrSspr(code, ppcMfspr, spr, dReg);
					} else if (m.id == idPUTGPR) { // PUTGPR
						int gpr = ((StdConstant)opds[0].constant).valueH;
						if (src2Reg < 0) {
							int immVal = ((StdConstant)opds[1].constant).valueH;
							createIrDrAsimm(code, ppcAddi, gpr, 0, immVal);
						} else 
							createIrArSrB(code, ppcOr, gpr, src2Reg, src2Reg);
					} else if (m.id == idPUTFPR) { // PUTFPR
						int fpr = ((StdConstant)opds[0].constant).valueH;
						createIrDrB(code, ppcFmr, fpr, src2Reg);
					} else if (m.id == idPUTSPR) { // PUTSPR
						int spr = ((StdConstant)opds[0].constant).valueH;
						createIrSspr(code, ppcMtspr, spr, src2Reg);
					} else if (m.id == idHALT) { // HALT	// TODO
						createItrap(code, ppcTw, TOalways, 0, 0);
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
						}
						else{
							Item m1 = clazz.methods.getItemByName(methName.toString());
							loadConstantAndFixup(code, dReg, m1);	// addr of method
						}
					} else if (m.id == idREF) { // REF
						createIrArSrB(code, ppcOr, dReg, src1Reg, src1Reg);
					} else if (m.id == idDoubleToBits) { // DoubleToBits
						createIrSrAd(code, ppcStfd, src1Reg, stackPtr, tempStorageOffset);
						createIrDrAd(code, ppcLwz, dRegLong, stackPtr, tempStorageOffset);
						createIrDrAd(code, ppcLwz, dReg, stackPtr, tempStorageOffset + 4);
					} else if (m.id == idBitsToDouble) { // BitsToDouble
						createIrSrAd(code, ppcStw, src1RegLong, stackPtr, tempStorageOffset);
						createIrSrAd(code, ppcStw, src1Reg, stackPtr, tempStorageOffset+4);
						createIrDrAd(code, ppcLfd, dReg, stackPtr, tempStorageOffset);
					} else if (m.id == idFloatToBits) { // FloatToBits
						createIrSrAd(code, ppcStfs, src1Reg, stackPtr, tempStorageOffset);
						createIrDrAd(code, ppcLwz, dReg, stackPtr, tempStorageOffset);
					} else if (m.id == idBitsToFloat) { // BitsToFloat
						createIrSrAd(code, ppcStw, src1Reg, stackPtr, tempStorageOffset);
						createIrDrAd(code, ppcLfs, 0, stackPtr, tempStorageOffset);
						createIrDrB(code, ppcFmr, dReg, 0);
					} else if (m.id == idENABLE_FLOATS) { // nothing to do
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
							loadConstantAndFixup(code, gAux1, m);	
							createIrSspr(code, ppcMtspr, LR, gAux1); 
						} else {
							loadConstantAndFixup(code, gAux1, m);	// addr of method
							createIrSspr(code, ppcMtspr, LR, gAux1);
						}
					} else if ((m.accAndPropFlags & (1<<dpfInterfCall)) != 0) {	// invokeinterface
						int refReg = src1Reg;
						int offset = (Class.maxExtensionLevelStdClasses + 1) * Linker32.slotSize + Linker32.tdBaseClass0Offset;
						createItrap(code, ppcTwi, TOifequal, refReg, 0);
						createIrDrAd(code, ppcLwz, gAux1, refReg, -4);
						createIrDrAd(code, ppcLwz, gAux1, gAux1, offset);	// delegate method
						createIrSspr(code, ppcMtspr, LR, gAux1);
					} else if (call.invokespecial) {	// invokespecial
						if (newString) {	// special treatment for strings
							if (m == strInitC) m = strAllocC;
							else if (m == strInitCII) m = strAllocCII;	// addr of corresponding allocate method
							else if (m == strInitCII) m = strAllocCII;
							loadConstantAndFixup(code, gAux1, m);	
							createIrSspr(code, ppcMtspr, LR, gAux1);
						} else {
							int refReg = src1Reg;
							createItrap(code, ppcTwi, TOifequal, refReg, 0);
							loadConstantAndFixup(code, gAux1, m);	// addr of init method
							createIrSspr(code, ppcMtspr, LR, gAux1);
						}
					} else {	// invokevirtual 
						int refReg = src1Reg;
						int offset = Linker32.tdMethTabOffset;
						offset -= m.index * Linker32.slotSize; 
						createItrap(code, ppcTwi, TOifequal, refReg, 0);
						createIrDrAd(code, ppcLwz, gAux1, refReg, -4);
						createIrDrAd(code, ppcLwz, gAux1, gAux1, offset);
						createIrSspr(code, ppcMtspr, LR, gAux1);
					}
					
					// copy parameters into registers and to stack if not enough registers
					if (dbg) StdStreams.vrb.println("call to " + meth.name + ": copy parameters");
					copyParameters(code, opds);
					
					if ((m.accAndPropFlags & (1<<dpfInterfCall)) != 0) {	// invokeinterface
						// interface info goes into last parameter register
						loadConstant(code, paramEndGPR, m.owner.index << 16 | m.index * 4);	// interface id and method offset						// check if param = maxParam in reg -2
					}
					
					if (newString) {
						int sizeOfObject = Type.wktObject.objectSize;
						createIrDrAsimm(code, ppcAddi, paramStartGPR+opds.length, 0, sizeOfObject); // reg after last parameter
					}
					createIBOBILK(code, ppcBclr, BOalways, 0, true);
					
					// get result
					int type = res.type & ~(1<<ssaTaFitIntoInt);
					if (type == tLong) {
						if (dRegLong == returnGPR2) {
							if (dReg == returnGPR1) {	// returnGPR2 -> r0, returnGPR1 -> r3, r0 -> r2
								createIrArSrB(code, ppcOr, 0, returnGPR2, returnGPR2);
								createIrArSrB(code, ppcOr, dRegLong, returnGPR1, returnGPR1);
								createIrArSrB(code, ppcOr, dReg, 0, 0);
							} else {	// returnGPR2 -> reg, returnGPR1 -> r3
								createIrArSrB(code, ppcOr, dReg, returnGPR2, returnGPR2);
								createIrArSrB(code, ppcOr, dRegLong, returnGPR1, returnGPR1);
							}
						} else { // returnGPR1 -> regLong, returnGPR2 -> reg
							createIrArSrB(code, ppcOr, dRegLong, returnGPR1, returnGPR1);
							createIrArSrB(code, ppcOr, dReg, returnGPR2, returnGPR2);
						}
					} else if (type == tFloat || type == tDouble) {
						createIrDrB(code, ppcFmr, dReg, returnFPR);
					} else if (type == tVoid) {
						if (newString) {
							newString = false;
							createIrArSrB(code, ppcOr, stringReg, returnGPR1, returnGPR1); // stringReg was set by preceding sCnew
						}
					} else
						createIrArSrB(code, ppcOr, dReg, returnGPR1, returnGPR1);
					
				}
				break;}	//sCcall
			case sCnew: {
				Item item = ((Call)instr).item;	// item = ref
				Item m;
				if (opds == null) {	// bCnew
					if (item == Type.wktString) {
						newString = true;	// allocation of strings is postponed
						stringReg = dReg;
						loadConstantAndFixup(code, dReg, item);	// ref to string
					} else {
						m = CFR.getNewMemoryMethod(bCnew);
						loadConstantAndFixup(code, paramStartGPR, m);	// addr of new
						createIrSspr(code, ppcMtspr, LR, paramStartGPR);
						loadConstantAndFixup(code, paramStartGPR, item);	// ref
						createIBOBILK(code, ppcBclr, BOalways, 0, true);
						createIrArSrB(code, ppcOr, dReg, returnGPR1, returnGPR1);
					}
				} else if (opds.length == 1) {
					switch (res.type  & ~(1<<ssaTaFitIntoInt)) {
					case tAboolean: case tAchar: case tAfloat: case tAdouble:
					case tAbyte: case tAshort: case tAinteger: case tAlong:	// bCnewarray
						m = CFR.getNewMemoryMethod(bCnewarray);
						loadConstantAndFixup(code, gAux1, m);	// addr of newarray
						createIrSspr(code, ppcMtspr, LR, gAux1);
						createIrArSrB(code, ppcOr, paramStartGPR, src1Reg, src1Reg);	// nof elems
						createIrDrAsimm(code, ppcAddi, paramStartGPR + 1, 0, (instr.result.type & 0x7fffffff) - 10);	// type
						loadConstantAndFixup(code, paramStartGPR + 2, item);	// ref to type descriptor
						createIBOBILK(code, ppcBclr, BOalways, 0, true);
						createIrArSrB(code, ppcOr, dReg, returnGPR1, returnGPR1);
						break;
					case tAref:	// bCanewarray
						m = CFR.getNewMemoryMethod(bCanewarray);
						loadConstantAndFixup(code, gAux1, m);	// addr of anewarray
						createIrSspr(code, ppcMtspr, LR, gAux1);
						createIrArSrB(code, ppcOr, paramStartGPR, src1Reg, src1Reg);	// nof elems
						loadConstantAndFixup(code, paramStartGPR + 1, item);	// ref to type descriptor
						createIBOBILK(code, ppcBclr, BOalways, 0, true);
						createIrArSrB(code, ppcOr, dReg, returnGPR1, returnGPR1);
						break;
					default:
						ErrorReporter.reporter.error(612);
						assert false : "operand of new instruction has wrong type";
						return;
					}
				} else { // bCmultianewarray:
					m = CFR.getNewMemoryMethod(bCmultianewarray);
					loadConstantAndFixup(code, gAux1, m);	// addr of multianewarray
					createIrSspr(code, ppcMtspr, LR, gAux1);
					// copy dimensions
					for (int k = 0; k < nofGPR; k++) {srcGPR[k] = 0; srcGPRcount[k] = 0;}

					// get info about in which register parameters are located
					// the first two parameter registers are used for nofDim and ref
					// therefore start is at paramStartGPR + 2
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
					
					// count register usage
					int cnt = paramStartGPR + 2;
					while (srcGPR[cnt] != 0) srcGPRcount[srcGPR[cnt++]]++;
					
					// handle move to itself
					cnt = paramStartGPR + 2;
					while (srcGPR[cnt] != 0) {
						if (srcGPR[cnt] == cnt) srcGPRcount[cnt]--;
						cnt++;
					}

					// move registers 
					boolean done = false;
					while (!done) {
						cnt = paramStartGPR + 2; done = true;
						while (srcGPR[cnt] != 0) {
							if (srcGPRcount[cnt] == 0) { // check if register no longer used for parameter
								if (dbg) StdStreams.vrb.println("\tGPR: parameter " + (cnt-paramStartGPR) + " from register " + srcGPR[cnt] + " to " + cnt);
								createIrArSrB(code, ppcOr, cnt, srcGPR[cnt], srcGPR[cnt]);
								srcGPRcount[cnt]--; srcGPRcount[srcGPR[cnt]]--; 
								done = false;
							}
							cnt++; 
						}
					}
					if (dbg) StdStreams.vrb.println();

					// resolve cycles
					done = false;
					while (!done) {
						cnt = paramStartGPR + 2; done = true;
						while (srcGPR[cnt] != 0) {
							int src = 0;
							if (srcGPRcount[cnt] == 1) {
								src = cnt;
								createIrArSrB(code, ppcOr, 0, srcGPR[cnt], srcGPR[cnt]);
								srcGPRcount[srcGPR[cnt]]--;
								done = false;
							}
							boolean done1 = false;
							while (!done1) {
								int k = paramStartGPR + 2; done1 = true;
								while (srcGPR[k] != 0) {
									if (srcGPRcount[k] == 0 && k != src) {
										createIrArSrB(code, ppcOr, k, srcGPR[k], srcGPR[k]);
										srcGPRcount[k]--; srcGPRcount[srcGPR[k]]--; 
										done1 = false;
									}
									k++; 
								}
							}
							if (src != 0) {
								createIrArSrB(code, ppcOr, src, 0, 0);
								srcGPRcount[src]--;
							}
							cnt++;
						}
					}
					loadConstantAndFixup(code, paramStartGPR, item);	// ref to type descriptor
					createIrDrAsimm(code, ppcAddi, paramStartGPR+1, 0, opds.length);	// nofDimensions
					createIBOBILK(code, ppcBclr, BOalways, 0, true);
					createIrArSrB(code, ppcOr, dReg, returnGPR1, returnGPR1);
				}
				break;}
			case sCreturn: {
				int bci = meth.cfg.code[node.lastBCA] & 0xff;
				switch (bci) {
				case bCreturn:
					break;
				case bCireturn:
				case bCareturn:
					createIrArSrB(code, ppcOr, returnGPR1, src1Reg, src1Reg);
					break;
				case bClreturn:
					createIrArSrB(code, ppcOr, returnGPR1, src1RegLong, src1RegLong);
					createIrArSrB(code, ppcOr, returnGPR2, src1Reg, src1Reg);
					break;
				case bCfreturn:
				case bCdreturn:
					createIrDrB(code, ppcFmr, returnFPR, src1Reg);
					break;
				default:
					ErrorReporter.reporter.error(620);
					assert false : "return instruction not implemented";
					return;
				}
				if (node.next != null)	// last node needs no branch, other nodes branch to epilogue 
					createIli(code, ppcB, 0, false);
				break;}
			case sCbranch:
			case sCswitch: {
				int bci = meth.cfg.code[node.lastBCA] & 0xff;
				switch (bci) {
				case bCgoto:
					createIli(code, ppcB, 0, false);
					break;
				case bCif_acmpeq:
				case bCif_acmpne:
					createICRFrArB(code, ppcCmp, CRF0, src2Reg, src1Reg);
					if (bci == bCif_acmpeq)
						createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, 0);
					else
						createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 0);
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
						createICRFrAsimm(code, ppcCmpi, CRF0, src2Reg, immVal);
					} else if (src2Reg < 0) {
						inverted = true;
						int immVal = ((StdConstant)opds[1].constant).valueH;
						createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, immVal);
					} else {
						createICRFrArB(code, ppcCmp, CRF0, src2Reg, src1Reg);
					}
					if (!inverted) {
						if (bci == bCif_icmpeq) 
							createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, 0);
						else if (bci == bCif_icmpne)
							createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 0);
						else if (bci == bCif_icmplt)
							createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+LT, 0);
						else if (bci == bCif_icmpge)
							createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+LT, 0);
						else if (bci == bCif_icmpgt)
							createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+GT, 0);
						else if (bci == bCif_icmple)
							createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+GT, 0);
					} else {
						if (bci == bCif_icmpeq) 
							createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, 0);
						else if (bci == bCif_icmpne)
							createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 0);
						else if (bci == bCif_icmplt)
							createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+GT, 0);
						else if (bci == bCif_icmpge)
							createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+GT, 0);
						else if (bci == bCif_icmpgt)
							createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+LT, 0);
						else if (bci == bCif_icmple)
							createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+LT, 0);
					}
					break; 
				case bCifeq:
				case bCifne:
				case bCiflt:
				case bCifge:
				case bCifgt:
				case bCifle: 
					createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, 0);
					if (bci == bCifeq) 
						createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, 0);
					else if (bci == bCifne)
						createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 0);
					else if (bci == bCiflt)
						createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+LT, 0);
					else if (bci == bCifge)
						createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+LT, 0);
					else if (bci == bCifgt)
						createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+GT, 0);
					else if (bci == bCifle)
						createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+GT, 0);
					break;
				case bCifnonnull:
				case bCifnull: 
					createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, 0);
					if (bci == bCifnonnull)
						createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 0);
					else
						createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, 0);
					break;
				case bCtableswitch:
					int addr = node.lastBCA + 1;
					addr = (addr + 3) & -4; // round to the next multiple of 4
					addr += 4; // skip default offset
					int low = getInt(meth.cfg.code, addr);
					int high = getInt(meth.cfg.code, addr + 4);
					int nofCases = high - low + 1;
					for (int k = 0; k < nofCases; k++) {
						createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, low + k);
						createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, 0);
					}
					createIli(code, ppcB, nofCases, false);
					break;
				case bClookupswitch:
					addr = node.lastBCA + 1;
					addr = (addr + 3) & -4; // round to the next multiple of 4
					addr += 4; // skip default offset
					int nofPairs = getInt(meth.cfg.code, addr);
					for (int k = 0; k < nofPairs; k++) {
						int key = getInt(meth.cfg.code, addr + 4 + k * 8);
						createICRFrAsimm(code, ppcCmpi, CRF0, src1Reg, key);
						createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, 0);
					}
					createIli(code, ppcB, nofPairs, true);
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
					if (dReg >= 0x100) {
						createIrSrAd(code, ppcStw, src1Reg, stackPtr, localVarOffset + 4 * (dReg & 0xff));
					} else if (src1Reg >= 0x100) {
						createIrDrAd(code, ppcLwz, dReg, stackPtr, localVarOffset + 4 * (src1Reg & 0xff));
					} else
						createIrArSrB(code, ppcOr, dReg, src1Reg, src1Reg);
					break;
				case tLong:
					if (dRegLong >= 0x100) {
						createIrSrAd(code, ppcStw, src1RegLong, stackPtr, localVarOffset + 4 * (dRegLong & 0xff));
					} else if (src1RegLong >= 0x100) {
						createIrDrAd(code, ppcLwz, dRegLong, stackPtr, localVarOffset + 4 * (src1RegLong & 0xff));
					} else
						createIrArSrB(code, ppcOr, dRegLong, src1RegLong, src1RegLong);
					if (dReg >= 0x100) {
						createIrSrAd(code, ppcStw, src1Reg, stackPtr, localVarOffset + 4 * (dReg & 0xff));
					} else if (src1Reg >= 0x100) {
						createIrDrAd(code, ppcLwz, dReg, stackPtr, localVarOffset + 4 * (src1Reg & 0xff));						
					} else
						createIrArSrB(code, ppcOr, dReg, src1Reg, src1Reg);
					break;
				case tFloat: case tDouble:
					createIrDrB(code, ppcFmr, dReg, src1Reg);
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

			if (dRegLongSlot >= 0) createIrSrAd(code, ppcStw, dRegLong, stackPtr, localVarOffset + 4 * dRegLongSlot);
			if (dRegSlot >= 0) {
				if ((res.type == tFloat) || (res.type == tDouble)) createIrSrAd(code, ppcStfd, dReg, stackPtr, localVarOffset + 4 * dRegSlot);
				else createIrSrAd(code, ppcStw, dReg, stackPtr, localVarOffset + 4 * dRegSlot);
			}
		}
	}

	private static void correctJmpAddr(int[] instructions, int count1, int count2) {
		instructions[count1] |= ((count2 - count1) << 2) & 0xffff;
	}

	// copy parameters for methods into parameter registers or onto stack
	private void copyParameters(Code32 code, SSAValue[] opds) {
		int offset = 0;
		for (int k = 0; k < nofGPR; k++) {srcGPR[k] = 0; srcGPRcount[k] = 0;}
		for (int k = 0; k < nofFPR; k++) {srcFPR[k] = 0; srcFPRcount[k] = 0;}

		// get info about in which register parameters are located
		// parameters which go onto the stack are treated equally
		for (int k = 0, kGPR = 0, kFPR = 0; k < opds.length; k++) {
			int type = opds[k].type & ~(1<<ssaTaFitIntoInt);
			if (type == tLong) {
				srcGPR[kGPR + paramStartGPR] = opds[k].regLong;
				srcGPR[kGPR + 1 + paramStartGPR] = opds[k].reg;
				kGPR += 2;
			} else if (type == tFloat || type == tDouble) {
				srcFPR[kFPR + paramStartFPR] = opds[k].reg;
				kFPR++;
			} else {
				srcGPR[kGPR + paramStartGPR] = opds[k].reg;
				kGPR++;
			}
		}

		if (dbg) {
			StdStreams.vrb.print("srcGPR = ");
			for (int k = paramStartGPR; srcGPR[k] != 0; k++) StdStreams.vrb.print(srcGPR[k] + ","); 
			StdStreams.vrb.println();
			StdStreams.vrb.print("srcGPRcount = ");
			for (int n = paramStartGPR; srcGPR[n] != 0; n++) StdStreams.vrb.print(srcGPRcount[n] + ","); 
			StdStreams.vrb.println();
		}

		// count register usage
		int i = paramStartGPR;
		while (srcGPR[i] != 0) {
			if (srcGPR[i] <= topGPR) srcGPRcount[srcGPR[i]]++;
			i++;
		}
		i = paramStartFPR;
		while (srcFPR[i] != 0) {
			if (srcFPR[i] <= topFPR) srcFPRcount[srcFPR[i]]++;
			i++;
		}
//		if (dbg) {
//			StdStreams.vrb.print("srcGPR = ");
//			for (i = paramStartGPR; srcGPR[i] != 0; i++) StdStreams.vrb.print(srcGPR[i] + ","); 
//			StdStreams.vrb.println();
//			StdStreams.vrb.print("srcGPRcount = ");
//			for (i = paramStartGPR; srcGPR[i] != 0; i++) StdStreams.vrb.print(srcGPRcount[i] + ","); 
//			StdStreams.vrb.println();
//		}
		
		// handle move to itself
		i = paramStartGPR;
		while (srcGPR[i] != 0) {
			if (srcGPR[i] == i) srcGPRcount[i]--;
			i++;
		}
		i = paramStartFPR;
		while (srcFPR[i] != 0) {
			if (srcFPR[i] == i) srcFPRcount[i]--;
			i++;
		}
//		if (dbg) {
//			StdStreams.vrb.print("srcGPR = ");
//			for (i = paramStartGPR; srcGPR[i] != 0; i++) StdStreams.vrb.print(srcGPR[i] + ","); 
//			StdStreams.vrb.println();
//			StdStreams.vrb.print("srcGPRcount = ");
//			for (i = paramStartGPR; srcGPR[i] != 0; i++) StdStreams.vrb.print(srcGPRcount[i] + ","); 
//			StdStreams.vrb.println();
//		}

		// move registers 
		boolean done = false;
		while (!done) {
			i = paramStartGPR; done = true;
			while (srcGPR[i] != 0) {
				if (i > paramEndGPR) {	// copy to stack
					if (srcGPRcount[i] >= 0) { // check if not done yet
						if (dbg) StdStreams.vrb.println("\tGPR: parameter " + (i-paramStartGPR) + " from register " + srcGPR[i] + " to stack slot");
						if (srcGPR[i] >= 0x100) {	// copy from stack slot to stack (into parameter area)
							createIrDrAd(code, ppcLwz, 0, stackPtr, localVarOffset + 4 * (srcGPR[i] - 0x100));
							createIrSrAsimm(code, ppcStw, 0, stackPtr, paramOffset + offset);
						} else {
							createIrSrAsimm(code, ppcStw, srcGPR[i], stackPtr, paramOffset + offset);
							srcGPRcount[srcGPR[i]]--; 
						}
						offset += 4;
						srcGPRcount[i]--; 
						done = false;
					}
				} else {	// copy to register
					if (srcGPRcount[i] == 0) { // check if register no longer used for parameter
						if (dbg) StdStreams.vrb.println("\tGPR: parameter " + (i-paramStartGPR) + " from register " + srcGPR[i] + " to " + i);
						if (srcGPR[i] >= 0x100) {	// copy from stack
							createIrDrAd(code, ppcLwz, i, stackPtr, localVarOffset + 4 * (srcGPR[i] - 0x100));
						} else {
							createIrArSrB(code, ppcOr, i, srcGPR[i], srcGPR[i]);
							srcGPRcount[srcGPR[i]]--; 
						}
						srcGPRcount[i]--; 
						done = false;
					}
				}
				i++; 
			}
		}
//		if (dbg) StdStreams.vrb.println();
		done = false;
		while (!done) {
			i = paramStartFPR; done = true;
			while (srcFPR[i] != 0) {
				if (i > paramEndFPR) {	// copy to stack
					if (srcFPRcount[i] >= 0) { // check if not done yet
						if (dbg) StdStreams.vrb.println("\tFPR: parameter " + (i-paramStartFPR) + " from register " + srcFPR[i] + " to stack slot");
						if (srcFPR[i] >= 0x100) {	// copy from stack slot to stack (into parameter area)
							createIrDrAd(code, ppcLfd, 0, stackPtr, localVarOffset + 4 * (srcFPR[i] - 0x100));
							createIrSrAd(code, ppcStfd, 0, stackPtr, paramOffset + offset);
						} else {
							createIrSrAd(code, ppcStfd, srcFPR[i], stackPtr, paramOffset + offset);
							srcFPRcount[srcFPR[i]]--;
						}
						offset += 8;
						srcFPRcount[i]--;  
						done = false;
					}
				} else {	// copy to register
					if (srcFPRcount[i] == 0) { // check if register no longer used for parameter
						if (dbg) StdStreams.vrb.println("\tFPR: parameter " + (i-paramStartFPR) + " from register " + srcFPR[i] + " to " + i);
						if (srcFPR[i] >= 0x100) {	// copy from stack
							createIrDrAd(code, ppcLfd, i, stackPtr, localVarOffset + 4 * (srcFPR[i] - 0x100));
						} else {
							createIrDrB(code, ppcFmr, i, srcFPR[i]);
							srcFPRcount[srcFPR[i]]--; 
						}
						srcFPRcount[i]--;  
						done = false;
					}
				}
				i++; 
			}
		}

		// resolve cycles
		done = false;
		while (!done) {
			i = paramStartGPR; done = true;
			while (srcGPR[i] != 0) {
				int src = 0;
				if (srcGPRcount[i] == 1) {
					src = i;
					createIrArSrB(code, ppcOr, 0, srcGPR[i], srcGPR[i]);
					srcGPRcount[srcGPR[i]]--;
					done = false;
				}
				boolean done1 = false;
				while (!done1) {
					int k = paramStartGPR; done1 = true;
					while (srcGPR[k] != 0) {
						if (srcGPRcount[k] == 0 && k != src) {
							createIrArSrB(code, ppcOr, k, srcGPR[k], srcGPR[k]);
							srcGPRcount[k]--; srcGPRcount[srcGPR[k]]--; 
							done1 = false;
						}
						k++; 
					}
				}
				if (src != 0) {
					createIrArSrB(code, ppcOr, src, 0, 0);
					srcGPRcount[src]--;
				}
				i++;
			}
		}
		done = false;
		while (!done) {
			i = paramStartFPR; done = true;
			while (srcFPR[i] != 0) {
				int src = 0;
				if (srcFPRcount[i] == 1) {
					src = i;
					createIrDrB(code, ppcFmr, 0, srcFPR[i]);
					srcFPRcount[srcFPR[i]]--;
					done = false;
				}
				boolean done1 = false;
				while (!done1) {
					int k = paramStartFPR; done1 = true;
					while (srcFPR[k] != 0) {
						if (srcFPRcount[k] == 0 && k != src) {
							createIrDrB(code, ppcFmr, k, srcFPR[k]);
							srcFPRcount[k]--; srcFPRcount[srcFPR[k]]--; 
							done1 = false;
						}
						k++; 
					}
				}
				if (src != 0) {
					createIrDrB(code, ppcFmr, src, 0);
					srcFPRcount[src]--;
				}
				i++;
			}
		}
	}

	// copy parameters for subroutines into registers r30/r31, r28/r29
	private void copyParametersSubroutine(Code32 code, int op0regLong, int op0reg, int op1regLong, int op1reg) {
		for (int k = 0; k < nofGPR; k++) {srcGPR[k] = 0; srcGPRcount[k] = 0;}

		// get info about in which register parameters are located
		srcGPR[topGPR] = op0reg;
		srcGPR[topGPR-1] = op0regLong;
		if (op1regLong != 0 && op1reg != 0) {srcGPR[topGPR-2] = op1reg; srcGPR[topGPR-3] = op1regLong;}
		
		// count register usage
		int i = topGPR;
		while (srcGPR[i] != 0) srcGPRcount[srcGPR[i--]]++;
		
		// handle move to itself
		i = topGPR;
		while (srcGPR[i] != 0) {
			if (srcGPR[i] == i) srcGPRcount[i]--;
			i--;
		}

		// move registers 
		boolean done = false;
		while (!done) {
			i = topGPR; done = true;
			while (srcGPR[i] != 0) {
				if (srcGPRcount[i] == 0) { // check if register no longer used for parameter
					createIrArSrB(code, ppcOr, i, srcGPR[i], srcGPR[i]);
					srcGPRcount[i]--; srcGPRcount[srcGPR[i]]--; 
					done = false;
				}
				i--; 
			}
		}

		// resolve cycles
		done = false;
		while (!done) {
			i = topGPR; done = true;
			while (srcGPR[i] != 0) {
				int src = 0;
				if (srcGPRcount[i] == 1) {
					src = i;
					createIrArSrB(code, ppcOr, 0, srcGPR[i], srcGPR[i]);
					srcGPRcount[srcGPR[i]]--;
					done = false;
				}
				boolean done1 = false;
				while (!done1) {
					int k = topGPR; done1 = true;
					while (srcGPR[k] != 0) {
						if (srcGPRcount[k] == 0 && k != src) {
							createIrArSrB(code, ppcOr, k, srcGPR[k], srcGPR[k]);
							srcGPRcount[k]--; srcGPRcount[srcGPR[k]]--; 
							done1 = false;
						}
						k--; 
					}
				}
				if (src != 0) {
					createIrArSrB(code, ppcOr, src, 0, 0);
					srcGPRcount[src]--;
				}
				i--;
			}
		}
	}	

	private void createIrArS(Code32 code, int opCode, int rA, int rS) {
		code.instructions[code.iCount] = opCode | (rS << 21) | (rA << 16);
		code.incInstructionNum();
	}

	private void createIrD(Code32 code, int opCode, int rD) {
		code.instructions[code.iCount] = opCode | (rD << 21);
		code.incInstructionNum();
	}

	private void createIrS(Code32 code, int opCode, int rD) {
		code.instructions[code.iCount] = opCode | (rD << 21);
		code.incInstructionNum();
	}

	private void createIrDrArB(Code32 code, int opCode, int rD, int rA, int rB) {
		code.instructions[code.iCount] = opCode | (rD << 21) | (rA << 16) | (rB << 11);
		code.incInstructionNum();
	}

	private void createIrDrArC(Code32 code, int opCode, int rD, int rA, int rC) {
		code.instructions[code.iCount] = opCode | (rD << 21) | (rA << 16) | (rC << 6);
		code.incInstructionNum();
	}

	private void createIrDrArCrB(Code32 code, int opCode, int rD, int rA, int rC, int rB) {
		code.instructions[code.iCount] = opCode | (rD << 21) | (rA << 16) | (rC << 6) | (rB << 11);
		code.incInstructionNum();
	}
	
	private void createIrSrArB(Code32 code, int opCode, int rS, int rA, int rB) {
		code.instructions[code.iCount] = opCode | (rS << 21) | (rA << 16) | (rB << 11);
		code.incInstructionNum();
	}

	private void createIrArSrB(Code32 code, int opCode, int rA, int rS, int rB) {
		if ((opCode == ppcOr) && (rA == rS) && (rA == rB)) return; 	// lr x,x makes no sense
		code.instructions[code.iCount] = opCode | (rS << 21) | (rA << 16) | (rB << 11);
		code.incInstructionNum();
	}

	private void createIrDrAd(Code32 code, int opCode, int rD, int rA, int d) {
		code.instructions[code.iCount] = opCode | (rD << 21) | (rA << 16) | (d  & 0xffff);
		code.incInstructionNum();
	}

	private void createIrDrAsimm(Code32 code, int opCode, int rD, int rA, int simm) {
		code.instructions[code.iCount] = opCode | (rD << 21) | (rA << 16) | (simm  & 0xffff);
		code.incInstructionNum();
	}

	private void createIrArSuimm(Code32 code, int opCode, int rA, int rS, int uimm) {
		code.instructions[code.iCount] = opCode | (rA << 16) | (rS << 21) | (uimm  & 0xffff);
		code.incInstructionNum();
	}

	private void createIrSrAsimm(Code32 code, int opCode, int rS, int rA, int simm) {
		code.instructions[code.iCount] = opCode | (rS << 21) | (rA << 16) | (simm  & 0xffff);
		code.incInstructionNum();
	}

	private void createIrSrAd(Code32 code, int opCode, int rS, int rA, int d) {
		code.instructions[code.iCount] = opCode | (rS << 21) | (rA << 16) | (d  & 0xffff);
		code.incInstructionNum();
	}

	private void createIrArSSH(Code32 code, int opCode, int rA, int rS, int SH) {
		code.instructions[code.iCount] = opCode | (rS << 21) | (rA << 16) | (SH << 11);
		code.incInstructionNum();
	}

	private void createIrArSSHMBME(Code32 code, int opCode, int rA, int rS, int SH, int MB, int ME) {
		code.instructions[code.iCount] = opCode | (rS << 21) | (rA << 16) | (SH << 11) | (MB << 6) | (ME << 1);
		code.incInstructionNum();
	}

	private void createIrArSrBMBME(Code32 code, int opCode, int rA, int rS, int rB, int MB, int ME) {
		code.instructions[code.iCount] = opCode | (rS << 21) | (rA << 16) | (rB << 11) | (MB << 6) | (ME << 1);
		code.incInstructionNum();
	}

	private void createItrap(Code32 code, int opCode, int TO, int rA, int rB) {
		code.instructions[code.iCount] = opCode | (TO << 21) | (rA << 16) | (rB << 11);
		code.incInstructionNum();
	}

	private void createItrapSimm(Code32 code, int opCode, int TO, int rA, int imm) {
		code.instructions[code.iCount] = opCode | (TO << 21) | (rA << 16) | (imm & 0xffff);
		code.incInstructionNum();
	}

	private void createIli(Code32 code, int opCode, int LI, boolean link) {
		code.instructions[code.iCount] = opCode | (LI << 2 | (link ? 1 : 0));
		code.incInstructionNum();
	}

	private void createIBOBIBD(Code32 code, int opCode, int BO, int BI, int BD) {
		code.instructions[code.iCount] = opCode | (BO << 21) | (BI << 16) | ((BD << 2)&0xffff);
		code.incInstructionNum();
	}

	private void createIBOBILK(Code32 code, int opCode, int BO, int BI, boolean link) {
		code.instructions[code.iCount] = opCode | (BO << 21) | (BI << 16) | (link?1:0);
		code.incInstructionNum();
	}

	private void createICRFrArB(Code32 code, int opCode, int crfD, int rA, int rB) {
		code.instructions[code.iCount] = opCode | (crfD << 23) | (rA << 16) | (rB << 11);
		code.incInstructionNum();
	}

	private void createICRFrAsimm(Code32 code, int opCode, int crfD, int rA, int simm) {
		code.instructions[code.iCount] = opCode | (crfD << 23) | (rA << 16) | (simm & 0xffff);
		code.incInstructionNum();
	}

	private void createIcrbDcrbAcrbB(Code32 code, int opCode, int crbD, int crbA, int crbB) {
		code.instructions[code.iCount] = opCode | (crbD << 21) | (crbA << 16) | (crbB << 11);
		code.incInstructionNum();
	}
	
	private void createICRMrS(Code32 code, int opCode, int CRM, int rS) {
		code.instructions[code.iCount] = opCode | (rS << 21) | (CRM << 12);
		code.incInstructionNum();
	}
	
	private void createIFMrB(Code32 code, int opCode, int FM, int rB) {
		code.instructions[code.iCount] = opCode | (FM << 17) | (rB << 11);
		code.incInstructionNum();
	}
	
	private void createIrDrA(Code32 code, int opCode, int rD, int rA) {
		code.instructions[code.iCount] = opCode | (rD << 21) | (rA << 16);
		code.incInstructionNum();
	}

	private void createIrDrB(Code32 code, int opCode, int rD, int rB) {
		if ((opCode == ppcFmr) && (rD == rB)) return; 	// fmr x,x makes no sense
		code.instructions[code.iCount] = opCode | (rD << 21) | (rB << 11);
		code.incInstructionNum();
	}

	private void createIrSspr(Code32 code, int opCode, int spr, int rS) {
		int temp = ((spr & 0x1F) << 5) | ((spr & 0x3E0) >> 5);
		if (spr == 268 || spr == 269) opCode = ppcMftb;
		code.instructions[code.iCount] = opCode | (temp << 11) | (rS << 21);
		code.incInstructionNum();
	}

	private void createIrfi(Code32 code, int opCode) {
		code.instructions[code.iCount] = opCode;
		code.incInstructionNum();
	}

	private void createIpat(Code32 code, int pat) {
		code.instructions[code.iCount] = pat;
		code.incInstructionNum();
	}

	private void loadConstant(Code32 code, int reg, int val) {
		assert(reg != 0);
		int low = val & 0xffff;
		int high = (val >> 16) & 0xffff;
		if ((low >> 15) == 0) {
			if (low != 0 && high != 0) {
				createIrDrAsimm(code, ppcAddi, reg, 0, low);
				createIrDrAsimm(code, ppcAddis, reg, reg, high);
			} else if (low == 0 && high != 0) {
				createIrDrAsimm(code, ppcAddis, reg, 0, high);		
			} else if (low != 0 && high == 0) {
				createIrDrAsimm(code, ppcAddi, reg, 0, low);
			} else createIrDrAsimm(code, ppcAddi, reg, 0, 0);
		} else {
			createIrDrAsimm(code, ppcAddi, reg, 0, low);
			if (((high + 1) & 0xffff) != 0) createIrDrAsimm(code, ppcAddis, reg, reg, high + 1);
		}
	}
	
	/* 
	 * Loads a 32 bit constant into a register
	 * the value of the constant will later be fixed
	 */
	private void loadConstantAndFixup(Code32 code, int reg, Item item) {
		assert(reg != 0);
		if (code.lastFixup < 0 || code.lastFixup > 32768) {ErrorReporter.reporter.error(602); return;}
		createIrDrAsimm(code, ppcAddi, reg, 0, code.lastFixup);
		createIrDrAsimm(code, ppcAddis, reg, reg, 0);
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
	
	public void doFixups(Code32 code) {
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
				else StdStreams.vrb.println("\t" + item.name + " at 0x" + Integer.toHexString(addr));
			}
			int low = addr & 0xffff;
			int high = (addr >> 16) & 0xffff;
			if (!((low >> 15) == 0)) high++;
			int nextInstr = code.instructions[currInstr] & 0xffff;
			code.instructions[currInstr] = (code.instructions[currInstr] & 0xffff0000) | (low & 0xffff);
			code.instructions[currInstr+1] = (code.instructions[currInstr+1] & 0xffff0000) | (high & 0xffff);
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
			
			ssaInstr = code.ssa.searchBca(code.instructions[currInstr] + 1);	// add 1, as first store is ommitted	
			assert ssaInstr != null;
			code.instructions[currInstr++] = code.ssa.cfg.method.address + ssaInstr.machineCodeOffset * 4;	// handler
			count++;
		}
	}

	private void insertProlog(Code32 code) {
		code.iCount = 0;
		createIrSrAsimm(code, ppcStwu, stackPtr, stackPtr, -stackSize);
		createIrSspr(code, ppcMfspr, LR, 0);
		createIrSrAsimm(code, ppcStw, 0, stackPtr, LRoffset);
		if (nofNonVolGPR > 0) {
			createIrSrAd(code, ppcStmw, nofGPR-nofNonVolGPR, stackPtr, GPRoffset);
		}
		// enFloatsInExc could be true, even if this is no exception method
		// such a case arises when this method is called from within an exception method
		if (enFloatsInExc) {
			createIrD(code, ppcMfmsr, 0);
			createIrArSuimm(code, ppcOri, 0, 0, 0x2000);
			createIrS(code, ppcMtmsr, 0);
			createIrS(code, ppcIsync, 0);	// must context synchronize after setting of FP bit
		}
		int offset = FPRoffset;
		if (nofNonVolFPR > 0) {
			for (int i = 0; i < nofNonVolFPR; i++) {
				createIrSrAd(code, ppcStfd, topFPR-i, stackPtr, offset);
				offset += 8;
			}
		}
		if (enFloatsInExc) {
			for (int i = 0; i < nonVolStartFPR; i++) {	// save volatiles
				createIrSrAd(code, ppcStfd, i, stackPtr, offset);
				offset += 8;
			}
			createIrD(code, ppcMffs, 0);
			createIrSrAd(code, ppcStfd, 0, stackPtr, offset);
		}
//		if (dbg) {
//			StdStreams.vrb.print("moveGPRsrc = ");
//			for (int i = 0; moveGPRsrc[i] != 0; i++) StdStreams.vrb.print(moveGPRsrc[i] + ","); 
//			StdStreams.vrb.println();
//			StdStreams.vrb.print("moveGPRdst = ");
//			for (int i = 0; moveGPRdst[i] != 0; i++) StdStreams.vrb.print(moveGPRdst[i] + ","); 
//			StdStreams.vrb.println();
//			StdStreams.vrb.print("moveFPRsrc = ");
//			for (int i = 0; moveFPRsrc[i] != 0; i++) StdStreams.vrb.print(moveFPRsrc[i] + ","); 
//			StdStreams.vrb.println();
//			StdStreams.vrb.print("moveFPRdst = ");
//			for (int i = 0; moveFPRdst[i] != 0; i++) StdStreams.vrb.print(moveFPRdst[i] + ","); 
//			StdStreams.vrb.println();
//		}
		offset = 0;
		for (int i = 0; i < nofMoveGPR; i++) {
			if (moveGPRsrc[i]+paramStartGPR <= paramEndGPR) {// copy from parameter register
				if (dbg) StdStreams.vrb.println("Prolog: copy parameter " + moveGPRsrc[i] + " into GPR" + moveGPRdst[i]);
				if (moveGPRdst[i] < 0x100)
					createIrArSrB(code, ppcOr, moveGPRdst[i], moveGPRsrc[i]+paramStartGPR, moveGPRsrc[i]+paramStartGPR);
				else 	// copy to stack slot (locals)
					createIrSrAd(code, ppcStw, moveGPRsrc[i]+paramStartGPR, stackPtr, localVarOffset + 4 * (moveGPRdst[i] - 0x100));
			} else { // copy from stack slot (parameters)
				if (dbg) StdStreams.vrb.println("Prolog: copy parameter " + moveGPRsrc[i] + " from stack slot into GPR" + moveGPRdst[i]);
				if (moveGPRdst[i] < 0x100)
					createIrDrAd(code, ppcLwz, moveGPRdst[i], stackPtr, stackSize + paramOffset + offset);
				else { 	// copy to stack slot (locals)
					createIrDrAd(code, ppcLwz, 0, stackPtr, stackSize + paramOffset + offset);
					createIrSrAd(code, ppcStw, 0, stackPtr, localVarOffset + 4 * (moveGPRdst[i] - 0x100));
				}	
				offset += 4;
			}
		}
		for (int i = 0; i < nofMoveFPR; i++) {
			if (moveFPRsrc[i]+paramStartFPR <= paramEndFPR) {// copy from parameter register
				if (dbg) StdStreams.vrb.println("Prolog: copy parameter " + moveFPRsrc[i] + " into FPR" + moveFPRdst[i]);
				if (moveFPRdst[i] < 0x100)
					createIrDrB(code, ppcFmr, moveFPRdst[i], moveFPRsrc[i]+paramStartFPR);
				else	// copy to stack slot (locals)
					createIrSrAd(code, ppcStfd, moveFPRsrc[i]+paramStartFPR, stackPtr, localVarOffset + 4 * (moveFPRdst[i] - 0x100));
			} else { // copy from stack slot (parameters)
				if (dbg) StdStreams.vrb.println("Prolog: copy parameter " + moveFPRsrc[i] + " from stack slot into FPR" + moveFPRdst[i]);
				if (moveFPRdst[i] < 0x100)
					createIrDrAd(code, ppcLfd, moveFPRdst[i], stackPtr, stackSize + paramOffset + offset);
				else {
					createIrDrAd(code, ppcLfd, 0, stackPtr, stackSize + paramOffset + offset);
					createIrSrAd(code, ppcStfd, 0, stackPtr, localVarOffset + 4 * (moveFPRdst[i] - 0x100));
				}
				offset += 8;
			}
		}
	}

	private void insertEpilog(Code32 code, int stackSize) {
		int epilogStart = code.iCount;
		int offset = GPRoffset - 8;
		// enFloatsInExc could be true, even if this is no exception method
		// such a case arises when this method is called from within an exception method
		if (enFloatsInExc) {
			createIrDrAd(code, ppcLfd, 0, stackPtr, offset);
			createIFMrB(code, ppcMtfsf, 0xff, 0);
			offset -= 8;
			for (int i = nonVolStartFPR - 1; i >= 0; i--) {
				createIrDrAd(code, ppcLfd, i, stackPtr, offset);
				offset -= 8;
			}
		}
		if (nofNonVolFPR > 0) {
			for (int i = nofNonVolFPR - 1; i >= 0; i--) {
				createIrDrAd(code, ppcLfd, topFPR-i, stackPtr, offset);
				offset -= 8;
			}
		}
		if (nofNonVolGPR > 0)
			createIrDrAd(code, ppcLmw, nofGPR - nofNonVolGPR, stackPtr, GPRoffset);
		createIrDrAd(code, ppcLwz, 0, stackPtr, LRoffset);
		createIrSspr(code, ppcMtspr, LR, 0);
		createIrDrAsimm(code, ppcAddi, stackPtr, stackPtr, stackSize);
		createIBOBILK(code, ppcBclr, BOalways, 0, false);
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
		createIpat(code, 0xffffffff);
	}

	private void insertPrologException(Code32 code) {
		code.iCount = 0;
		createIrSrAsimm(code, ppcStwu, stackPtr, stackPtr, -stackSize);
		createIrSrAsimm(code, ppcStw, 0, stackPtr, GPRoffset);
		createIrSspr(code, ppcMfspr, SRR0, 0);
		createIrSrAsimm(code, ppcStw, 0, stackPtr, SRR0offset);
		createIrSspr(code, ppcMfspr, SRR1, 0);
		createIrSrAsimm(code, ppcStw, 0, stackPtr, SRR1offset);
		createIrSspr(code, ppcMtspr, EID, 0);
		createIrSspr(code, ppcMfspr, LR, 0);
		createIrSrAsimm(code, ppcStw, 0, stackPtr, LRoffset);
		createIrSspr(code, ppcMfspr, XER, 0);
		createIrSrAsimm(code, ppcStw, 0, stackPtr, XERoffset);
		createIrSspr(code, ppcMfspr, CTR, 0);
		createIrSrAsimm(code, ppcStw, 0, stackPtr, CTRoffset);
		createIrD(code, ppcMfcr, 0);
		createIrSrAsimm(code, ppcStw, 0, stackPtr, CRoffset);
		createIrSrAd(code, ppcStmw, 2, stackPtr, GPRoffset + 8);
		if (enFloatsInExc) {
			createIrD(code, ppcMfmsr, 0);
			createIrArSuimm(code, ppcOri, 0, 0, 0x2000);
			createIrS(code, ppcMtmsr, 0);
			createIrS(code, ppcIsync, 0);	// must context synchronize after setting of FP bit
			int offset = FPRoffset;
			if (nofNonVolFPR > 0) {
				for (int i = 0; i < nofNonVolFPR; i++) {	// save nonvolatiles which are used in this exception method
					createIrSrAd(code, ppcStfd, topFPR-i, stackPtr, offset);
					offset += 8;
				}
			}
			for (int i = 0; i < nonVolStartFPR; i++) {	// save volatiles
				createIrSrAd(code, ppcStfd, i, stackPtr, offset);
				offset += 8;
			}
			createIrD(code, ppcMffs, 0);
			createIrSrAd(code, ppcStfd, 0, stackPtr, offset);
		}
	}

	private void insertEpilogException(Code32 code, int stackSize) {
		int offset = GPRoffset - 8;
		if (enFloatsInExc) {
			createIrDrAd(code, ppcLfd, 0, stackPtr, offset);
			createIFMrB(code, ppcMtfsf, 0xff, 0);
			offset -= 8;
			for (int i = nonVolStartFPR - 1; i >= 0; i--) {
				createIrDrAd(code, ppcLfd, i, stackPtr, offset);
				offset -= 8;
			}
		}
		if (nofNonVolFPR > 0) {
			for (int i = nofNonVolFPR - 1; i >= 0; i--) {
				createIrDrAd(code, ppcLfd, topFPR-i, stackPtr, offset);
				offset -= 8;
			}
		}
		createIrDrAd(code, ppcLmw, 2, stackPtr, GPRoffset + 8);
		createIrDrAd(code, ppcLwz, 0, stackPtr, CRoffset);
		createICRMrS(code, ppcMtcrf, 0xff, 0);
		createIrDrAd(code, ppcLwz, 0, stackPtr, CTRoffset);
		createIrSspr(code, ppcMtspr, CTR, 0);
		createIrDrAd(code, ppcLwz, 0, stackPtr, XERoffset);
		createIrSspr(code, ppcMtspr, XER, 0);
		createIrDrAd(code, ppcLwz, 0, stackPtr, LRoffset);
		createIrSspr(code, ppcMtspr, LR, 0);
		createIrDrAd(code, ppcLwz, 0, stackPtr, SRR1offset);
		createIrSspr(code, ppcMtspr, SRR1, 0);
		createIrDrAd(code, ppcLwz, 0, stackPtr, SRR0offset);
		createIrSspr(code, ppcMtspr, SRR0, 0);
		createIrDrAd(code, ppcLwz, 0, stackPtr, GPRoffset);
		createIrDrAsimm(code, ppcAddi, stackPtr, stackPtr, stackSize);
		createIrfi(code, ppcRfi);
	}

	public void generateCompSpecSubroutines() {
		Method m = Method.getCompSpecSubroutine("longToDouble");
		// long is passed in r30/r31, r29 can be used for general purposes
		// faux1 and faux2 are used as general purpose FPR's, result is passed in f0 
		if (m != null) { 
			Code32 code = new Code32(null);
			m.machineCode = code;
			Item item = int2floatConst1;	// ref to 2^52+2^31;					
			createIrDrAsimm(code, ppcAddis, 0, 0, 0x4330);	// preload 2^52
			createIrSrAd(code, ppcStw, 0, stackPtr, tempStorageOffset);
			createIrArSuimm(code, ppcXoris, 0, 30, 0x8000);	// op0.regLong
			createIrSrAd(code, ppcStw, 0, stackPtr, tempStorageOffset+4);
			loadConstantAndFixup(code, 29, item); // r29 as auxGPR1
			createIrDrAd(code, ppcLfd, faux1, 29, 0);	// r29 as auxGPR1
			createIrDrAd(code, ppcLfd, 0, stackPtr, tempStorageOffset);
			createIrDrArB(code, ppcFsub, faux2, 0, faux1);
			createIrSrAd(code, ppcStw, 31, stackPtr, tempStorageOffset+4); // op0.reg
			item = int2floatConst3;	// ref to 2^52;
			loadConstantAndFixup(code, 29, item); // r29 as auxGPR1
			createIrDrAd(code, ppcLfd, faux1, 29, 0);	// r29 as auxGPR1
			createIrDrAd(code, ppcLfd, 0, stackPtr, tempStorageOffset);
			createIrDrArB(code, ppcFsub, faux1, 0, faux1); 					
			item = int2floatConst2;	// ref to 2^32;
			loadConstantAndFixup(code, 29, item); // r29 as auxGPR1
			createIrDrAd(code, ppcLfd, 0, 29, 0); 	// r29 as auxGPR1
			createIrDrArCrB(code, ppcFmadd, 0, faux2, 0, faux1);
			createIBOBILK(code, ppcBclr, BOalways, 0, false);
		}

		m = Method.getCompSpecSubroutine("doubleToLong");
		// double is passed in f0, r29, r30 and r31 can be used for general purposes
		// result is returned in r0/r28 
		if (m != null) { 
			Code32 code = new Code32(null);
			m.machineCode = code;

			createIrSrAd(code, ppcStfd, 0, stackPtr, tempStorageOffset); // op0.reg
			createIrDrAd(code, ppcLwz, 29, stackPtr, tempStorageOffset); // r29 as auxGPR1
			createIrDrAd(code, ppcLwz, 28, stackPtr, tempStorageOffset+4); // r28 as res.reg
			createIrArSSHMBME(code, ppcRlwinm, 30, 29, 12, 21, 31); // r29 as auxGPR1, r30 as auxGPR2	
			createIrDrAsimm(code, ppcSubfic, 30, 30, 1075);	// r30 as auxGPR2
			createICRFrAsimm(code, ppcCmpi, CRF2, 29, 0); // r29 as auxGPR1, check if negative
			createIrDrAsimm(code, ppcAddis, 0, 0, 0xfff0);	
			createIrArSrB(code, ppcAndc, 29, 29, 0); // r29 as auxGPR1	
			createIrArSuimm(code, ppcOris, 29, 29, 0x10); // r29 as auxGPR1	
			createICRFrAsimm(code, ppcCmpi, CRF0, 30, 52);	// r30 as auxGPR2
			int label1jmp1 = code.iCount;
			createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+GT, 0);	// jump to label 1
			// double is < 1
			createIrDrAsimm(code, ppcAddi, 0, 0, 0); // r0 as res.regLong
			createIrDrAsimm(code, ppcAddi, 28, 0, 0); // r28 as res.reg 
			createIBOBILK(code, ppcBclr, BOalways, 0, false); // return
			//label 1
			correctJmpAddr(code.instructions, label1jmp1, code.iCount);
			createICRFrAsimm(code, ppcCmpi, CRF0, 30, 0); // r30 as auxGPR2
			int label2jmp1 = code.iCount;
			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+LT, 0);	// jump to label 2
			createIrDrAsimm(code, ppcSubfic, 0, 30, 32);// r30 as auxGPR2
			createIrArSrB(code, ppcSrw, 28, 28, 30); // r28 as res.reg, r30 as auxGPR2
			createIrArSrB(code, ppcSlw, 0, 29, 0); // r29 as auxGPR1
			createIrArSrB(code, ppcOr, 28, 28, 0); // r28 as res.reg
			createIrDrAsimm(code, ppcAddi, 0, 30, -32);	// r30 as auxGPR2
			createIrArSrB(code, ppcSrw, 0, 29, 0); // r29 as auxGPR1
			createIrArSrB(code, ppcOr, 28, 28, 0); // r28 as res.reg
			createIrArSrB(code, ppcSrw, 0, 29, 30); // r0 as res.regLong, r29 as auxGPR1, r30 as auxGPR2
			int label5jmp1 = code.iCount;
			createIBOBIBD(code, ppcBc, BOalways, 0, 0);	// jump to label 5
			//label 2
			correctJmpAddr(code.instructions, label2jmp1, code.iCount);
			createIrDrA(code, ppcNeg, 30, 30); // r30 as auxGPR2
			createICRFrAsimm(code, ppcCmpi, CRF0, 30, 11); // r30 as auxGPR2
			int label4jmp1 = code.iCount;
			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+LT, 0);	// jump to label 4
			int label3jmp1 = code.iCount;
			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF2+LT, 0);	// jump to label 3
			createIrDrAsimm(code, ppcAddi, 28, 0, -1); // r28 as res.reg
			createIrDrAsimm(code, ppcAddis, 0, 0, 0x7fff); // r0 as res.regLong
			createIrArSuimm(code, ppcOri, 0, 0, 0xffff); // r0 as res.regLong
			createIBOBILK(code, ppcBclr, BOalways, 0, false); // return
			//label 3
			correctJmpAddr(code.instructions, label3jmp1, code.iCount);
			createIrDrAsimm(code, ppcAddi, 28, 0, 0); // r28 as res.reg
			createIrDrAsimm(code, ppcAddis, 0, 0, 0x8000); // r0 as res.regLong
			createIBOBILK(code, ppcBclr, BOalways, 0, false); // return
			//label 4
			correctJmpAddr(code.instructions, label4jmp1, code.iCount);
			createIrDrAsimm(code, ppcSubfic, 0, 30, 32); // r30 as auxGPR2
			createIrArSrB(code, ppcSlw, 31, 29, 30); // r31 as auxGPR3, r29 as auxGPR1, r30 as auxGPR2
			createIrArSrB(code, ppcSrw, 0, 28, 0); // r28 as res.reg
			createIrArSrB(code, ppcOr, 31, 31, 0); // r31 as auxGPR3
			createIrDrAsimm(code, ppcAddi, 0, 30, -32); // r30 as auxGPR2
			createIrArSrB(code, ppcSlw, 0, 28, 0); // r28 as res.reg
			createIrArSrB(code, ppcOr, 0, 31, 0); // r0 as res.regLong, r31 as auxGPR3
			createIrArSrB(code, ppcSlw, 28, 28, 30); // r28 as res.reg, r30 as auxGPR2
			//label 5
			correctJmpAddr(code.instructions, label5jmp1, code.iCount);
			createIBOBILK(code, ppcBclr, BOfalse, 4*CRF2+LT, false); // return
			createIrDrAsimm(code, ppcSubfic, 28, 28, 0); // r28 as res.reg
			createIrDrA(code, ppcSubfze, 0, 0); // r0 as res.regLong
			createIBOBILK(code, ppcBclr, BOalways, 0, false); // return
		}

		m = Method.getCompSpecSubroutine("divLong");
		// long op0 is passed in r30/r31 (dividend), long op1 is passed in r28/r29 (divisor) 
		// r27 can be used for general purposes
		// faux1, faux2, faux3 are used as general purpose FPR's 
		// result is returned in r0/r26 
		if (m != null) { 
			Code32 code = new Code32(null);
			m.machineCode = code;

			createICRFrAsimm(code, ppcCmpi, CRF0, 28, 0);	// test if divisor < 2^32, op1.regLong
			int label1jmp1 = m.machineCode.iCount;
			createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 0);	// jump to label 1
			createICRFrAsimm(code, ppcCmpli, CRF0, 29, 0x7fff);	// test if divisor < 2^15, op1.reg
			int label1jmp2 = m.machineCode.iCount;
			createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+LT, 0);	// jump to label 1
			
			// divisor is small, use GPR's, op1.regLong (r28) is 0 and can be used as aux register
			createICRFrAsimm(code, ppcCmpi, CRF2, 30, 0); // is dividend negative?, op0.regLong
			createIrDrArB(code, ppcDivw, 28, 30, 29); // auxGPR2, op0.regLong, op1.reg
			createIrDrArB(code, ppcMullw, 0, 29, 28); // op1.reg, auxGPR2
			createIrDrArB(code, ppcSubf, 0, 0, 30); // op0.regLong
			createICRFrAsimm(code, ppcCmpi, CRF0, 0, 0); // is remainder negative?
			createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+LT, 3);	
			createIrDrArB(code, ppcAdd, 0, 0, 29);	// add divisor, op1.reg
			createIrDrAsimm(code, ppcAddi, 28, 28, -1); // auxGPR2 	
			createIrArSrB(code, ppcOr, 30, 28, 28); // (r30)res.regLong, auxGPR2
			createIrArSSHMBME(code, ppcRlwinm, 27, 0, 16, 0, 15); // auxGPR1
			createIrArSSHMBME(code, ppcRlwimi, 27, 31, 16, 16, 31); // auxGPR1, op0.reg
			createIrDrArB(code, ppcDivwu, 28, 27, 29); // auxGPR2, auxGPR1, op1.reg
			createIrDrArB(code, ppcMullw, 0, 29, 28); // op1.reg, auxGPR2
			createIrDrArB(code, ppcSubf, 0, 0, 27); // auxGPR1
			createIrArSSHMBME(code, ppcRlwinm, 27, 0, 16, 0, 15); // auxGPR1
			createIrArSSHMBME(code, ppcRlwimi, 27, 31, 0, 16, 31); // auxGPR1, op0.reg 
			createIrDrArB(code, ppcDivwu, 0, 27, 29); // auxGPR1, op1.reg
			createIrArSSHMBME(code, ppcRlwinm, 26, 28, 16, 0, 15); // res.reg, auxGPR2
			createIrArSSHMBME(code, ppcRlwimi, 26, 0, 0, 16, 31); // res.reg
			createIrDrArB(code, ppcMullw, 0, 29, 0); // op1.reg
			createIrDrArB(code, ppcSubf, 0, 0, 27); // auxGPR1
			createICRFrAsimm(code, ppcCmpi, CRF0, 0, 0); // is remainder > 0?
			createIcrbDcrbAcrbB(code, ppcCrand, CRF0EQ, CRF0GT, CRF2LT);
			createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 2);	
			createIrDrAsimm(code, ppcAddi, 26, 26, 1); // res.reg	
			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF1+GT, 3);	// was divisor negative? CRF1 set before subroutine
			createIrDrAsimm(code, ppcSubfic, 26, 26, 0); // negate result, res.reg
			createIrDrA(code, ppcSubfze, 30, 30);	// res.regLong
			createIrArSrB(code, ppcOr, 0, 30, 30); // copy res.regLong
			createIBOBILK(code, ppcBclr, BOalways, 0, false); // return

			//label 1, divisor is not small, use FPR's
			correctJmpAddr(m.machineCode.instructions, label1jmp1, m.machineCode.iCount);
			correctJmpAddr(m.machineCode.instructions, label1jmp2, m.machineCode.iCount);
			Item item = int2floatConst1;	// ref to 2^52+2^31;					
			createIrDrAsimm(code, ppcAddis, 0, 0, 0x4330);	// preload 2^52
			createIrSrAd(code, ppcStw, 0, stackPtr, tempStorageOffset);
			createIrArSuimm(code, ppcXoris, 0, 30, 0x8000); // op0.regLong
			createIrSrAd(code, ppcStw, 0, stackPtr, tempStorageOffset+4);
			loadConstantAndFixup(code, 27, item); // auxGPR1
			createIrDrAd(code, ppcLfd, faux2, 27, 0); // auxGPR1
			createIrDrAd(code, ppcLfd, 0, stackPtr, tempStorageOffset);
			createIrDrArB(code, ppcFsub, faux2, 0, faux2);
			createIrSrAd(code, ppcStw, 31, stackPtr, tempStorageOffset+4); // op0.reg
			item = int2floatConst3;	// ref to 2^52;
			loadConstantAndFixup(code, 27, item); // auxGPR1
			createIrDrAd(code, ppcLfd, faux1, 27, 0); // auxGPR1
			createIrDrAd(code, ppcLfd, 0, stackPtr, tempStorageOffset);
			createIrDrArB(code, ppcFsub, faux1, 0, faux1);					
			item = int2floatConst2;	// ref to 2^32;
			loadConstantAndFixup(code, 27, item); // auxGPR1
			createIrDrAd(code, ppcLfd, 0, 27, 0); // auxGPR1
			createIrDrArCrB(code, ppcFmadd, faux2, faux2, 0, faux1);

			item = int2floatConst1;	// ref to 2^52+2^31;					
			createIrDrAsimm(code, ppcAddis, 0, 0, 0x4330);	// preload 2^52
			createIrSrAd(code, ppcStw, 0, stackPtr, tempStorageOffset);
			createIrArSuimm(code, ppcXoris, 0, 28, 0x8000); // op1.regLong
			createIrSrAd(code, ppcStw, 0, stackPtr, tempStorageOffset+4);
			loadConstantAndFixup(code, 27, item); // auxGPR1
			createIrDrAd(code, ppcLfd, faux3, 27, 0); // auxGPR1
			createIrDrAd(code, ppcLfd, 0, stackPtr, tempStorageOffset);
			createIrDrArB(code, ppcFsub, faux3, 0, faux3);
			createIrSrAd(code, ppcStw, 29, stackPtr, tempStorageOffset+4); // op1.reg
			item = int2floatConst3;	// ref to 2^52;
			loadConstantAndFixup(code, 27, item); // auxGPR1
			createIrDrAd(code, ppcLfd, faux1, 27, 0); // auxGPR1
			createIrDrAd(code, ppcLfd, 0, stackPtr, tempStorageOffset);
			createIrDrArB(code, ppcFsub, faux1, 0, faux1);					
			item = int2floatConst2;	// ref to 2^32;
			loadConstantAndFixup(code, 27, item); // auxGPR1
			createIrDrAd(code, ppcLfd, 0, 27, 0); // auxGPR1
			createIrDrArCrB(code, ppcFmadd, faux3, faux3, 0, faux1);

			createIrDrArB(code, ppcFdiv, faux1, faux2, faux3);

			createIrSrAd(code, ppcStfd, faux1, stackPtr, tempStorageOffset);
			createIrDrAd(code, ppcLwz, 27, stackPtr, tempStorageOffset); // auxGPR1
			createIrDrAd(code, ppcLwz, 26, stackPtr, tempStorageOffset+4); // res.reg
			createIrArSSHMBME(code, ppcRlwinm, 28, 27, 12, 21, 31); // auxGPR2, auxGPR1	
			createIrDrAsimm(code, ppcSubfic, 28, 28, 1075);	
			createICRFrAsimm(code, ppcCmpi, CRF2, 27, 0);
			createIrDrAsimm(code, ppcAddis, 0, 0, 0xfff0);	
			createIrArSrB(code, ppcAndc, 27, 27, 0);	
			createIrArSuimm(code, ppcOris, 27, 27, 0x10);	
			createICRFrAsimm(code, ppcCmpi, CRF0, 28, 52);
			// double is < 1
			createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+GT, 4);
			createIrDrAsimm(code, ppcAddi, 0, 0, 0); // res.regLong
			createIrDrAsimm(code, ppcAddi, 26, 0, 0); // res.reg
			createIBOBILK(code, ppcBclr, BOalways, 0, false); // return

			createICRFrAsimm(code, ppcCmpi, CRF0, 28, 0);
			int label2jmp1 = m.machineCode.iCount;
			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+LT, 0);	// jump to label 2
			createIrDrAsimm(code, ppcSubfic, 0, 28, 32);
			createIrArSrB(code, ppcSrw, 26, 26, 28);
			createIrArSrB(code, ppcSlw, 0, 27, 0);
			createIrArSrB(code, ppcOr, 26, 26, 0);
			createIrDrAsimm(code, ppcAddi, 0, 28, -32);
			createIrArSrB(code, ppcSrw, 0, 27, 0);
			createIrArSrB(code, ppcOr, 26, 26, 0); // res.reg
			createIrArSrB(code, ppcSrw, 0, 27, 28); // res.regLong
			int label3jmp1 = m.machineCode.iCount;
			createIBOBIBD(code, ppcBc, BOalways, 0, 0);	// jump to label 3
			//label 2
			correctJmpAddr(code.instructions, label2jmp1, code.iCount);
			createIrDrAsimm(code, ppcSubfic, 0, 28, 32);
			createIrArSrB(code, ppcSlw, 29, 27, 28); // (r29) res.regLong
			createIrArSrB(code, ppcSrw, 0, 26, 0); // res.reg
			createIrArSrB(code, ppcOr, 29, 29, 0);
			createIrDrAsimm(code, ppcAddi, 0, 28, -32);
			createIrArSrB(code, ppcSlw, 0, 26, 0); // res.reg
			createIrArSrB(code, ppcOr, 0, 29, 0); // res.regLong is now in r0
			createIrArSrB(code, ppcSlw, 26, 26, 28); // res.reg
			//label 3
			correctJmpAddr(m.machineCode.instructions, label3jmp1, m.machineCode.iCount);
			createIBOBIBD(code, ppcBc, BOfalse, 4*CRF2+LT, 3);
			createIrDrAsimm(code, ppcSubfic, 26, 26, 0); // res.reg
			createIrDrA(code, ppcSubfze, 0, 0); // res.regLong
			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF1+GT, 3);	// jump to end
			createIrDrAsimm(code, ppcSubfic, 26, 26, 0); // res.reg
			createIrDrA(code, ppcSubfze, 0, 0); // res.regLong
			createIBOBILK(code, ppcBclr, BOalways, 0, false); // return
		}


		m = Method.getCompSpecSubroutine("remLong");
		// long op0 is passed in r30/r31 (dividend), long op1 is passed in r28/r29 (divisor) 
		// r25, r26, r27 can be used for general purposes
		// faux1, faux2, faux3 are used as general purpose FPR's 
		// result is returned in r0/r24 
		if (m != null) { 
			Code32 code = new Code32(null);
			m.machineCode = code;

			createICRFrAsimm(code, ppcCmpi, CRF0, 28, 0);	// test if divisor < 2^32, op1.regLong
			int label1jmp1 = m.machineCode.iCount;
			createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 0);	// jump to label 1
			createICRFrAsimm(code, ppcCmpli, CRF0, 29, 0x7fff);	// test if divisor < 2^15, op1.reg
			int label1jmp2 = m.machineCode.iCount;
			createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+LT, 0);	// jump to label 1
			
			// divisor is small, use GPR's, op1.regLong (r28) is 0 but it must be preserved for multiplication at the end
			createICRFrAsimm(code, ppcCmpi, CRF2, 30, 0); // is dividend negative?, op0.regLong
			createIrDrAsimm(code, ppcAddi, 19, 0, 0x1234);	
			createIrDrArB(code, ppcDivw, 25, 30, 29); // res.regLong, op0.regLong, op1.reg
			createIrDrArB(code, ppcMullw, 0, 29, 25); // op1.reg, res.regLong
			createIrDrArB(code, ppcSubf, 0, 0, 30); // op0.regLong
			createICRFrAsimm(code, ppcCmpi, CRF0, 0, 0); // is remainder negative?
			createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+LT, 3);	
			createIrDrArB(code, ppcAdd, 0, 0, 29);	// add divisor, op1.reg
			createIrDrAsimm(code, ppcAddi, 25, 25, -1); // res.regLong 	
			createIrArSSHMBME(code, ppcRlwinm, 27, 0, 16, 0, 15); // auxGPR1
			createIrArSSHMBME(code, ppcRlwimi, 27, 31, 16, 16, 31); // auxGPR1, op0.reg
			createIrDrArB(code, ppcDivwu, 26, 27, 29); // auxGPR2, auxGPR1, op1.reg
			createIrDrArB(code, ppcMullw, 0, 29, 26); // op1.reg, auxGPR2
			createIrDrArB(code, ppcSubf, 0, 0, 27); // auxGPR1
			createIrArSSHMBME(code, ppcRlwinm, 27, 0, 16, 0, 15); // auxGPR1
			createIrArSSHMBME(code, ppcRlwimi, 27, 31, 0, 16, 31); // auxGPR1, op0.reg 
			createIrDrArB(code, ppcDivwu, 0, 27, 29); // auxGPR1, op1.reg
			createIrArSSHMBME(code, ppcRlwinm, 24, 26, 16, 0, 15); // res.reg, auxGPR2
			createIrArSSHMBME(code, ppcRlwimi, 24, 0, 0, 16, 31); // res.reg
			createIrDrArB(code, ppcMullw, 0, 29, 0); // op1.reg
			createIrDrArB(code, ppcSubf, 0, 0, 27); // auxGPR1
			createICRFrAsimm(code, ppcCmpi, CRF0, 0, 0); // is remainder > 0?
			createIcrbDcrbAcrbB(code, ppcCrand, CRF0EQ, CRF0GT, CRF2LT);
			createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 2);	
			createIrDrAsimm(code, ppcAddi, 24, 24, 1); // res.reg	
			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF1+GT, 3);	// was divisor negative? CRF1 set before subroutine
			createIrDrAsimm(code, ppcSubfic, 24, 24, 0); // negate result, div.reg
			createIrDrA(code, ppcSubfze, 25, 25);	// div.regLong
			int label4jmp1 = m.machineCode.iCount;
			createIBOBIBD(code, ppcBc, BOalways, 0, 0);	// jump to label 4

			//label 1, divisor is not small, use FPR's
			correctJmpAddr(m.machineCode.instructions, label1jmp1, m.machineCode.iCount);
			correctJmpAddr(m.machineCode.instructions, label1jmp2, m.machineCode.iCount);
			Item item = int2floatConst1;	// ref to 2^52+2^31;					
			createIrDrAsimm(code, ppcAddis, 0, 0, 0x4330);	// preload 2^52
			createIrSrAd(code, ppcStw, 0, stackPtr, tempStorageOffset);
			createIrArSuimm(code, ppcXoris, 0, 30, 0x8000); // op0.regLong
			createIrSrAd(code, ppcStw, 0, stackPtr, tempStorageOffset+4);
			loadConstantAndFixup(code, 27, item); // auxGPR1
			createIrDrAd(code, ppcLfd, faux2, 27, 0); // auxGPR1
			createIrDrAd(code, ppcLfd, 0, stackPtr, tempStorageOffset);
			createIrDrArB(code, ppcFsub, faux2, 0, faux2);
			createIrSrAd(code, ppcStw, 31, stackPtr, tempStorageOffset+4); // op0.reg
			item = int2floatConst3;	// ref to 2^52;
			loadConstantAndFixup(code, 27, item); // auxGPR1
			createIrDrAd(code, ppcLfd, faux1, 27, 0); // auxGPR1
			createIrDrAd(code, ppcLfd, 0, stackPtr, tempStorageOffset);
			createIrDrArB(code, ppcFsub, faux1, 0, faux1);					
			item = int2floatConst2;	// ref to 2^32;
			loadConstantAndFixup(code, 27, item); // auxGPR1
			createIrDrAd(code, ppcLfd, 0, 27, 0); // auxGPR1
			createIrDrArCrB(code, ppcFmadd, faux2, faux2, 0, faux1);

			item = int2floatConst1;	// ref to 2^52+2^31;					
			createIrDrAsimm(code, ppcAddis, 0, 0, 0x4330);	// preload 2^52
			createIrSrAd(code, ppcStw, 0, stackPtr, tempStorageOffset);
			createIrArSuimm(code, ppcXoris, 0, 28, 0x8000); // op1.regLong
			createIrSrAd(code, ppcStw, 0, stackPtr, tempStorageOffset+4);
			loadConstantAndFixup(code, 27, item); // auxGPR1
			createIrDrAd(code, ppcLfd, faux3, 27, 0); // auxGPR1
			createIrDrAd(code, ppcLfd, 0, stackPtr, tempStorageOffset);
			createIrDrArB(code, ppcFsub, faux3, 0, faux3);
			createIrSrAd(code, ppcStw, 29, stackPtr, tempStorageOffset+4); // op1.reg
			item = int2floatConst3;	// ref to 2^52;
			loadConstantAndFixup(code, 27, item); // auxGPR1
			createIrDrAd(code, ppcLfd, faux1, 27, 0); // auxGPR1
			createIrDrAd(code, ppcLfd, 0, stackPtr, tempStorageOffset);
			createIrDrArB(code, ppcFsub, faux1, 0, faux1);					
			item = int2floatConst2;	// ref to 2^32;
			loadConstantAndFixup(code, 27, item); // auxGPR1
			createIrDrAd(code, ppcLfd, 0, 27, 0); // auxGPR1
			createIrDrArCrB(code, ppcFmadd, faux3, faux3, 0, faux1);

			createIrDrArB(code, ppcFdiv, faux1, faux2, faux3);

			createIrSrAd(code, ppcStfd, faux1, stackPtr, tempStorageOffset);
			createIrDrAd(code, ppcLwz, 27, stackPtr, tempStorageOffset); // auxGPR1
			createIrDrAd(code, ppcLwz, 24, stackPtr, tempStorageOffset+4); // div.reg
			createIrArSSHMBME(code, ppcRlwinm, 26, 27, 12, 21, 31); // auxGPR2, auxGPR1	
			createIrDrAsimm(code, ppcSubfic, 26, 26, 1075);	
			createICRFrAsimm(code, ppcCmpi, CRF2, 27, 0);
			createIrDrAsimm(code, ppcAddis, 0, 0, 0xfff0);	
			createIrArSrB(code, ppcAndc, 27, 27, 0);	
			createIrArSuimm(code, ppcOris, 27, 27, 0x10);	
			createICRFrAsimm(code, ppcCmpi, CRF0, 26, 52);
			// double is < 1
			createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+GT, 4);
			createIrDrAsimm(code, ppcAddi, 25, 0, 0); // div.regLong
			createIrDrAsimm(code, ppcAddi, 24, 0, 0); // div.reg
			int label4jmp2 = m.machineCode.iCount;
			createIBOBIBD(code, ppcBc, BOalways, 0, 0);	// jump to label 4

			createICRFrAsimm(code, ppcCmpi, CRF0, 26, 0);
			int label2jmp1 = m.machineCode.iCount;
			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+LT, 0);	// jump to label 2
			createIrDrAsimm(code, ppcSubfic, 0, 26, 32);
			createIrArSrB(code, ppcSrw, 24, 24, 26);
			createIrArSrB(code, ppcSlw, 0, 27, 0);
			createIrArSrB(code, ppcOr, 24, 24, 0);
			createIrDrAsimm(code, ppcAddi, 0, 26, -32);
			createIrArSrB(code, ppcSrw, 0, 27, 0);
			createIrArSrB(code, ppcOr, 24, 24, 0); // div.reg
			createIrArSrB(code, ppcSrw, 25, 27, 26); // div.regLong, aux1, aux2
			int label3jmp1 = m.machineCode.iCount;
			createIBOBIBD(code, ppcBc, BOalways, 0, 0);	// jump to label 3
			//label 2
			correctJmpAddr(m.machineCode.instructions, label2jmp1, m.machineCode.iCount);
			createIrDrAsimm(code, ppcSubfic, 0, 26, 32);
			createIrArSrB(code, ppcSlw, 25, 27, 26); // div.regLong
			createIrArSrB(code, ppcSrw, 0, 24, 0); // div.reg
			createIrArSrB(code, ppcOr, 25, 25, 0);
			createIrDrAsimm(code, ppcAddi, 0, 26, -32);
			createIrArSrB(code, ppcSlw, 0, 24, 0); // div.reg
			createIrArSrB(code, ppcOr, 25, 25, 0); // div.regLong
			createIrArSrB(code, ppcSlw, 24, 24, 26); // div.reg, aux2
			//label 3
			correctJmpAddr(m.machineCode.instructions, label3jmp1, m.machineCode.iCount);
			createIBOBIBD(code, ppcBc, BOfalse, 4*CRF2+LT, 3);
			createIrDrAsimm(code, ppcSubfic, 24, 24, 0); // div.reg
			createIrDrA(code, ppcSubfze, 25, 25); // div.regLong
			//label 4
			correctJmpAddr(m.machineCode.instructions, label4jmp1, m.machineCode.iCount);
			correctJmpAddr(m.machineCode.instructions, label4jmp2, m.machineCode.iCount);
			createIrDrArB(code, ppcMullw, 27, 25, 29); // auxGPR1, div.regLong, op1.reg
			createIrDrArB(code, ppcMullw, 26, 24, 28); // auxGPR2, div.reg, op1.regLong
			createIrDrArB(code, ppcAdd, 27, 27, 26);
			createIrDrArB(code, ppcMulhwu, 26, 24, 29); // auxGPR2, div.reg, op1.reg
			createIrDrArB(code, ppcAdd, 25, 27, 26);
			createIrDrArB(code, ppcMullw, 24, 24, 29); // res.reg, op1.reg

			createIrDrArB(code, ppcSubfc, 24, 24, 31); // res.reg, div.reg, op0.reg
			createIrDrArB(code, ppcSubfe, 0, 25, 30);	// res.regLong, div.regLong, op0.regLong
			createIBOBILK(code, ppcBclr, BOalways, 0, false); // return
		}

		int regAux1 = paramEndGPR; // use parameter registers for interface delegation methods
		int regAux2 = paramEndGPR - 1; // use parameter registers
		int regAux3 = paramEndGPR - 2; // use parameter registers

		m = Method.getCompSpecSubroutine("imDelegI1Mm");
		if (m != null) { 
			Code32 code = new Code32(null);
			m.machineCode = code;

			createIrDrAd(code, ppcLwz, regAux2, paramStartGPR, -4);	// get tag
			createIrDrAd(code, ppcLwz, 0, regAux2, (Class.maxExtensionLevelStdClasses + 1) * 4 + Linker32.tdBaseClass0Offset + 4);	// get interface
			createIrArS(code, ppcExtsh, 0, 0);
			createIrArSuimm(code, ppcAndi, regAux1, regAux1, 0xffff);	// mask method number
			createIrDrArB(code, ppcSubf, regAux1, regAux1, 0);	
			createIrDrArB(code, ppcLwzx, 0, regAux2, regAux1);
			createIrSspr(code, ppcMtspr, CTR, 0);
			createIBOBILK(code, ppcBcctr, BOalways, 0, false);	// no linking
		}

		m = Method.getCompSpecSubroutine("imDelegIiMm");
		if (m != null) { 
			Code32 code = new Code32(null);
			m.machineCode = code;

			createIrArSuimm(code, ppcOri, regAux3, regAux1, 0xffff);	// interface id
			createIrDrAd(code, ppcLwz, regAux2, paramStartGPR, -4);	// get tag
			createIrDrAsimm(code, ppcAddi, regAux2, regAux2, (Class.maxExtensionLevelStdClasses + 1) * Linker32.slotSize + Linker32.tdBaseClass0Offset);	// set to address before first interface 
			createIrDrAsimm(code, ppcAddi, regAux2, regAux2, 4);	// set to next interface
			createIrDrAd(code, ppcLwz, 0, regAux2, 0);	// get interface
			createICRFrArB(code, ppcCmpl, CRF0, 0, regAux3);
			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+GT, -3);
			createIrArS(code, ppcExtsh, 0, 0);
			createIrArSuimm(code, ppcAndi, regAux1, regAux1, 0xffff);	// mask method offset
			createIrDrArB(code, ppcAdd, regAux1, regAux1, 0);	
			createIrDrAd(code, ppcLwz, regAux2, paramStartGPR, -4);	// reload tag
			createIrDrArB(code, ppcLwzx, 0, regAux2, regAux1);
			createIrSspr(code, ppcMtspr, CTR, 0);
			createIBOBILK(code, ppcBcctr, BOalways, 0, false);	// no linking
		}
		
		m = Method.getCompSpecSubroutine("handleException");
		if (m != null) { 
			Code32 code = new Code32(null);
			m.machineCode = code;

			// r2 contains reference to exception, r3 holds SRR0
			// r4 to r10 are used for auxiliary purposes

			// search end of method
			createIrArSrB(code, ppcOr, 4, 3, 3);
			createIrDrAd(code, ppcLwzu, 9, 4, 4);	
			createICRFrAsimm(code, ppcCmpli, CRF0, 9, 0xff);
			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+GT, -2);
			createIrArSrB(code, ppcOr, 10, 4, 4);	// keep for unwinding
			createIrDrAd(code, ppcLwzu, 5, 4, 4);	//  R4 now points to first entry of exception table
		
			// search catch, label 1
			int label1 = m.machineCode.iCount;
			createICRFrAsimm(code, ppcCmpi, CRF0, 5, -1);
			int label2 = m.machineCode.iCount;
			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, 0);	// catch not found, goto label 2
			createIrDrAd(code, ppcLwz, 5, 4, 0);	// start 
			createICRFrArB(code, ppcCmp, CRF0, 3, 5);		
			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+LT, 17);
			createIrDrAd(code, ppcLwz, 5, 4, 4);	// end 
			createICRFrArB(code, ppcCmp, CRF0, 3, 5);		
			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+GT, 14);
			createIrDrAd(code, ppcLwz, 5, 4, 8);	// type 
			
			createICRFrAsimm(code, ppcCmpi, CRF0, 5, 0);	// check if type "any", caused by finally
			createIBOBIBD(code, ppcBc, BOtrue, 4*CRF0+EQ, 8);
//			m.machineCode.createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 3);
//			m.machineCode.createIrDrAsimm(ppcAddi, 18, 18, 0x1000);	
//			m.machineCode.createIBOBIBD(ppcBc, BOalways, 4*CRF0+EQ, 8);
			
			createIrDrAd(code, ppcLwz, 6, 5, -4);	// get extension level of exception 
			createIrArSSHMBME(code, ppcRlwinm, 6, 6, 2, 0, 31);	// *4
			createIrDrAsimm(code, ppcAddi, 6, 6, Linker32.tdBaseClass0Offset);	
			createIrDrAd(code, ppcLwz, 7, 2, -4);	// get tag 
			createIrDrArB(code, ppcLwzx, 8, 7, 6);	 
			createICRFrArB(code, ppcCmp, CRF0, 8, 5);		
			createIBOBIBD(code, ppcBc, BOfalse, 4*CRF0+EQ, 4);		
			createIrDrAd(code, ppcLwz, 0, 4, 12);	// get handler address
			createIrSspr(code, ppcMtspr, SRR0, 0);
			createIrfi(code, ppcRfi);	// return to catch
			
			createIrDrAd(code, ppcLwzu, 5, 4, 16);	
			createIBOBIBD(code, ppcBc, BOalways, 0, 0);	// jump to label 1
			correctJmpAddr(m.machineCode.instructions, m.machineCode.iCount-1, label1);
			
			// catch not found, unwind, label 2
			correctJmpAddr(m.machineCode.instructions, label2, m.machineCode.iCount);
			createIrDrAd(code, ppcLwz, 5, stackPtr, 0);	// get back pointer
			createIrDrAd(code, ppcLwz, 3, 5, -4);	// get LR from stack
			loadConstantAndFixup(code, 6, m);
			createIrSrAd(code, ppcStw, 6, 5, -4);	// put addr of handleException
			createIrArS(code, ppcExtsb, 9, 9);
			createIrDrArB(code, ppcAdd, 9, 10, 9);
			createIrSspr(code, ppcMtspr, LR, 9);
//			m.machineCode.createIBOBIBD(ppcBc, BOalways, 4*CRF0+GT, 0);
			createIBOBILK(code, ppcBclr, BOalways, 0, false); // branch to epilog
		}
	}

}


