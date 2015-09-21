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

package ch.ntb.inf.deep.cg;

import ch.ntb.inf.deep.classItems.*;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.Linker32;
import ch.ntb.inf.deep.ssa.*;
import ch.ntb.inf.deep.strings.HString;

public abstract class CodeGen implements SSAInstructionOpcs, SSAInstructionMnemonics, SSAValueType, ICjvmInstructionOpcs, ICclassFileConsts, ICdescAndTypeConsts {
	protected static final boolean dbg = false;

	protected static final int defaultNofInstr = 32;
	protected static final int defaultNofFixup = 8;

	protected static int objectSize, stringSize;
	protected static StdConstant int2floatConst1 = null;	// 2^52+2^31, for int -> float conversions
	protected static StdConstant int2floatConst2 = null;	// 2^32, for long -> float conversions
	protected static StdConstant int2floatConst3 = null;	// 2^52, for long -> float conversions

	public static int idGET1, idGET2, idGET4, idGET8;
	public static int idPUT1, idPUT2, idPUT4, idPUT8;
	public static int idBIT, idASM, idHALT, idADR_OF_METHOD, idREF;
	public static int idENABLE_FLOATS;
	public static int idGETGPR, idGETFPR, idGETSPR;
	public static int idPUTGPR, idPUTFPR, idPUTSPR;
	public static int idDoubleToBits, idBitsToDouble;
	public static int idFloatToBits, idBitsToFloat;

	protected static Method stringNewstringMethod;
	protected static Method heapNewstringMethod;
	protected static Method strInitC;
	protected static Method strInitCII;
	protected static Method strAllocC;
	protected static Method strAllocCII;
	
	public SSA ssa;	// reference to the SSA of a method
	public int[] instructions;	// contains machine instructions for the ssa of a method
	public int iCount;	// nof instructions for this method
	protected int excTabCount;	// start of exception information in instruction array
	
	public Item[] fixups;	// contains all references whose address has to be fixed by the linker
	protected int fCount;	//nof fixups
	protected int lastFixup;	// instr number where the last fixup is found

	public CodeGen() {}
	
	public CodeGen(SSA ssa) {
		this.ssa = ssa;
		instructions = new int[defaultNofInstr];
		fixups = new Item[defaultNofFixup];
//		nofParamGPR = 0; nofParamFPR = 0;
//		nofNonVolGPR = 0; nofNonVolFPR = 0;
//		nofVolGPR = 0; nofVolFPR = 0;
//		nofMoveGPR = 0; nofMoveFPR = 0;
//		tempStorage = false;
//		enFloatsInExc = false;
//		recParamSlotsOnStack = 0; callParamSlotsOnStack = 0;
//		if (dbg) StdStreams.vrb.println("generate code for " + ssa.cfg.method.owner.name + "." + ssa.cfg.method.name);
//		for (int i = 0; i < maxNofParam; i++) {
//			paramType[i] = tVoid;
//			paramRegNr[i] = -1;
//			paramRegEnd[i] = -1;
//		}
//
//		// make local copy
//		int maxStackSlots = ssa.cfg.method.maxStackSlots;
//		int i = maxStackSlots;
//		while ((i < ssa.isParam.length) && ssa.isParam[i]) {
//			int type = ssa.paramType[i] & ~(1<<ssaTaFitIntoInt);
//			paramType[i - maxStackSlots] = type;
//			paramHasNonVolReg[i - maxStackSlots] = false;
//			if (type == tLong || type == tDouble) i++;
//			i++;
//		}
//		nofParam = i - maxStackSlots;
//		if (nofParam > maxNofParam) {ErrorReporter.reporter.error(601); return;}
//		if (dbg) StdStreams.vrb.println("nofParam = " + nofParam);
//		
//		if (dbg) StdStreams.vrb.println("build intervals");
////		StdStreams.vrb.print(ssa.cfg.toString());
////		StdStreams.vrb.println(ssa.toString());
//		RegAllocator.buildIntervals(ssa);
//		
//		if (dbg) StdStreams.vrb.println("assign registers to parameters");
//		SSANode b = (SSANode) ssa.cfg.rootNode;
//		while (b.next != null) {
//			b = (SSANode) b.next;
//		}	
//		lastExitSet = b.exitSet;
//		// determine, which parameters go into which register
//		parseExitSet(lastExitSet, maxStackSlots);
//		if (dbg) {
//			StdStreams.vrb.print("parameter go into register: ");
//			for (int n = 0; paramRegNr[n] != -1; n++) StdStreams.vrb.print(paramRegNr[n] + "  "); 
//			StdStreams.vrb.println();
//		}
//		
//		if (dbg) StdStreams.vrb.println("allocate registers");
//		RegAllocator.assignRegisters(this);
//		
////		StdStreams.vrb.print(ssa.cfg.toString());
//		if (dbg) {
//			StdStreams.vrb.println(RegAllocator.joinsToString());
//		}
//		if (dbg) {
//			StdStreams.vrb.print("register usage in method: nofNonVolGPR = " + nofNonVolGPR + ", nofVolGPR = " + nofVolGPR);
//			StdStreams.vrb.println(", nofNonVolFPR = " + nofNonVolFPR + ", nofVolFPR = " + nofVolFPR);
//			StdStreams.vrb.print("register usage for parameters: nofParamGPR = " + nofParamGPR + ", nofParamFPR = " + nofParamFPR);
//			StdStreams.vrb.println(", receive parameters slots on stack = " + recParamSlotsOnStack);
//			StdStreams.vrb.println("max. parameter slots for any call in this method = " + callParamSlotsOnStack);
//			StdStreams.vrb.print("parameter end at instr no: ");
//			for (int n = 0; n < nofParam; n++) 
//				if (paramRegEnd[n] != -1) StdStreams.vrb.print(paramRegEnd[n] + "  "); 
//			StdStreams.vrb.println();
//		}
//		if ((ssa.cfg.method.accAndPropFlags & (1 << dpfExcHnd)) != 0) {	// exception
//			if (ssa.cfg.method.name == HString.getRegisteredHString("reset")) {	// reset has no prolog
//			} else if (ssa.cfg.method.name == HString.getRegisteredHString("programExc")) {	// special treatment for exception handling
//				iCount = 0;
//				createIrSrAsimm(ppcStwu, stackPtr, stackPtr, -24);
//				createIrSspr(ppcMtspr, EID, 0);	// must be set for further debugger exceptions
//				createIrSrAd(ppcStmw, 28, stackPtr, 4);
//				createIrArSrB(ppcOr, 31, paramStartGPR, paramStartGPR);	// copy exception into nonvolatile
//			} else {
//				stackSize = calcStackSizeException();
//				insertPrologException();
//			}
//		} else {
//			stackSize = calcStackSize();
//			insertProlog();	// builds stack frame and copies parameters
//		}
//		
//		SSANode node = (SSANode)ssa.cfg.rootNode;
//		while (node != null) {
//			node.codeStartIndex = iCount;
//			translateSSA(node);
//			node.codeEndIndex = iCount-1;
//			node = (SSANode) node.next;
//		}
//		node = (SSANode)ssa.cfg.rootNode;
//		while (node != null) {	// resolve local branch targets
//			if (node.nofInstr > 0) {
//				if ((node.instructions[node.nofInstr-1].ssaOpcode == sCbranch) || (node.instructions[node.nofInstr-1].ssaOpcode == sCswitch)) {
//					int code = this.instructions[node.codeEndIndex];
//					CFGNode[] successors = node.successors;
//					switch (code & 0xfc000000) {
//					case ppcB:			
//						if ((code & 0xffff) != 0) {	// switch
//							int nofCases = (code & 0xffff) >> 2;
//							int k;
//							for (k = 0; k < nofCases; k++) {
//								int branchOffset = ((SSANode)successors[k]).codeStartIndex - (node.codeEndIndex+1-(nofCases-k)*2);
//								this.instructions[node.codeEndIndex+1-(nofCases-k)*2] |= (branchOffset << 2) & 0x3ffffff;
//							}
//							int branchOffset = ((SSANode)successors[k]).codeStartIndex - node.codeEndIndex;
//							this.instructions[node.codeEndIndex] &= 0xfc000000;
//							this.instructions[node.codeEndIndex] |= (branchOffset << 2) & 0x3ffffff;
//						} else {
//							int branchOffset = ((SSANode)successors[0]).codeStartIndex - node.codeEndIndex;
//							this.instructions[node.codeEndIndex] |= (branchOffset << 2) & 0x3ffffff;
//						}
//						break;
//					case ppcBc:
//						int branchOffset = ((SSANode)successors[1]).codeStartIndex - node.codeEndIndex;
//						this.instructions[node.codeEndIndex] |= (branchOffset << 2) & 0xffff;
//						break;
//					}
//				} else if (node.instructions[node.nofInstr-1].ssaOpcode == sCreturn) {
//					if (node.next != null) {
//						int branchOffset = iCount - node.codeEndIndex;
//						this.instructions[node.codeEndIndex] |= (branchOffset << 2) & 0x3ffffff;
//					}
//				}
//			}
//			node = (SSANode) node.next;
//		}
//		if ((ssa.cfg.method.accAndPropFlags & (1 << dpfExcHnd)) != 0) {	// exception
//			if (ssa.cfg.method.name == HString.getRegisteredHString("reset")) {	// reset needs no epilog
//			} else if (ssa.cfg.method.name == HString.getRegisteredHString("programExc")) {	// special treatment for exception handling
//				Method m = Method.getCompSpecSubroutine("handleException");
//				assert m != null;
//				loadConstantAndFixup(31, m);
//				createIrSspr(ppcMtspr, LR, 31);
//				createIrDrAd(ppcLmw, 28, stackPtr, 4);
//				createIrDrAsimm(ppcAddi, stackPtr, stackPtr, 24);
//				createIBOBILK(ppcBclr, BOalways, 0, false);
//			} else {
//				insertEpilogException(stackSize);
//			}
//		} else {
//			insertEpilog(stackSize);
//		}
//		if (dbg) {StdStreams.vrb.print(ssa.toString()); StdStreams.vrb.print(toString());}
	}

//	private static void parseExitSet(SSAValue[] exitSet, int maxStackSlots) {
//		nofParamGPR = 0; nofParamFPR = 0;
//		nofMoveGPR = 0; nofMoveFPR = 0;
//		if(dbg) StdStreams.vrb.print("[");
//		for (int i = 0; i < nofParam; i++) {
//			int type = paramType[i];
//			if(dbg) StdStreams.vrb.print("(" + svNames[type] + ")");
//			if (type == tLong) {
//				if (exitSet[i+maxStackSlots] != null) {	// if null -> parameter is never used
//					if(dbg) StdStreams.vrb.print("r");
//					if (paramHasNonVolReg[i]) {
//						int reg = RegAllocator.reserveReg(gpr, true);
//						int regLong = RegAllocator.reserveReg(gpr, true);
//						moveGPRsrc[nofMoveGPR] = nofParamGPR;
//						moveGPRsrc[nofMoveGPR+1] = nofParamGPR+1;
//						moveGPRdst[nofMoveGPR++] = reg;
//						moveGPRdst[nofMoveGPR++] = regLong;
//						paramRegNr[i] = reg;
//						paramRegNr[i+1] = regLong;
//						if(dbg) StdStreams.vrb.print(reg + ",r" + regLong);
//					} else {
//						int reg = paramStartGPR + nofParamGPR;
//						if (reg <= paramEndGPR) RegAllocator.reserveReg(gpr, reg);
//						else {
//							reg = RegAllocator.reserveReg(gpr, false);
//							moveGPRsrc[nofMoveGPR] = nofParamGPR;
//							moveGPRdst[nofMoveGPR++] = reg;
//						}
//						int regLong = paramStartGPR + nofParamGPR + 1;
//						if (regLong <= paramEndGPR) RegAllocator.reserveReg(gpr, regLong);
//						else {
//							regLong = RegAllocator.reserveReg(gpr, false);
//							moveGPRsrc[nofMoveGPR] = nofParamGPR + 1;
//							moveGPRdst[nofMoveGPR++] = regLong;
//						}
//						paramRegNr[i] = reg;
//						paramRegNr[i+1] = regLong;
//						if(dbg) StdStreams.vrb.print(reg + ",r" + regLong);
//					}
//				}
//				nofParamGPR += 2;	// see comment below for else type 
//				i++;
//			} else if (type == tFloat || type == tDouble) {
//				if (exitSet[i+maxStackSlots] != null) {	// if null -> parameter is never used
//					if(dbg) StdStreams.vrb.print("fr");
//					if (paramHasNonVolReg[i]) {
//						int reg = RegAllocator.reserveReg(fpr, true);
//						moveFPRsrc[nofMoveFPR] = nofParamFPR;
//						moveFPRdst[nofMoveFPR++] = reg;
//						paramRegNr[i] = reg;
//						if(dbg) StdStreams.vrb.print(reg);
//					} else {
//						int reg = paramStartFPR + nofParamFPR;
//						if (reg <= paramEndFPR) RegAllocator.reserveReg(fpr, reg);
//						else {
//							reg = RegAllocator.reserveReg(fpr, false);
//							moveFPRsrc[nofMoveFPR] = nofParamFPR;
//							moveFPRdst[nofMoveFPR++] = reg;
//						}
//						paramRegNr[i] = reg;
//						if(dbg) StdStreams.vrb.print(reg);
//					}
//				}
//				nofParamFPR++;	// see comment below for else type 
//				if (type == tDouble) {i++; paramRegNr[i] = paramRegNr[i-1];}
//			} else {
//				if (exitSet[i+maxStackSlots] != null) {	// if null -> parameter is never used
//					if(dbg) StdStreams.vrb.print("r");
//					if (paramHasNonVolReg[i]) {
//						int reg = RegAllocator.reserveReg(gpr, true);
//						moveGPRsrc[nofMoveGPR] = nofParamGPR;
//						moveGPRdst[nofMoveGPR++] = reg;
//						paramRegNr[i] = reg;
//						if(dbg) StdStreams.vrb.print(reg);
//					} else {
//						int reg = paramStartGPR + nofParamGPR;
//						if (reg <= paramEndGPR) RegAllocator.reserveReg(gpr, reg);
//						else {
//							reg = RegAllocator.reserveReg(gpr, false);
//							moveGPRsrc[nofMoveGPR] = nofParamGPR;
//							moveGPRdst[nofMoveGPR++] = reg;
//						}
//						paramRegNr[i] = reg;
//						if(dbg) StdStreams.vrb.print(reg);
//					}
//				}
//				nofParamGPR++;	// even if the parameter is not used, the calling method
//				// assigns a register and we have to do here the same
//			}
//			if (i < nofParam - 1) if(dbg) StdStreams.vrb.print(", ");
//		}
//		int nof = nofParamGPR - (paramEndGPR - paramStartGPR + 1);
//		if (nof > 0) recParamSlotsOnStack = nof;
//		nof = nofParamFPR - (paramEndFPR - paramStartFPR + 1);
//		if (nof > 0) recParamSlotsOnStack += nof*2;
//		
//		if(dbg) StdStreams.vrb.println("]");
//	}
//
//	private static int calcStackSize() {
//		int size = 8 + callParamSlotsOnStack * 4 + nofNonVolGPR * 4 + nofNonVolFPR * 8 + (tempStorage? tempStorageSize : 0);
//		if (enFloatsInExc) size += nonVolStartFPR * 8 + 8;	// save volatile FPR's and FPSCR
//		int padding = (16 - (size % 16)) % 16;
//		size = size + padding;
//		LRoffset = size - 4;
//		GPRoffset = LRoffset - nofNonVolGPR * 4;
//		FPRoffset = GPRoffset - nofNonVolFPR * 8;
//		if (enFloatsInExc) FPRoffset -= nonVolStartFPR * 8 + 8;
//		tempStorageOffset = FPRoffset - tempStorageSize;
//		paramOffset = 4;
//		return size;
//	}
//
//	private static int calcStackSizeException() {
//		int size = 28 + nofGPR * 4 + (tempStorage? tempStorageSize : 0);
//		if (enFloatsInExc) {
//			size += nofNonVolFPR * 8;	// save used nonvolatile FPR's
//			size += nonVolStartFPR * 8 + 8;	// save all volatile FPR's and FPSCR
//		}
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
//		if (enFloatsInExc) FPRoffset -= nonVolStartFPR * 8 + 8;
//		tempStorageOffset = FPRoffset - tempStorageSize;
//		paramOffset = 4;
//		return size;
//	}

//
//	private static void correctJmpAddr(int[] instructions, int count1, int count2) {
//		instructions[count1] |= ((count2 - count1) << 2) & 0xffff;
//	}
//
//	private void copyParameters(SSAValue[] opds) {
//		int offset = 0;
//		for (int k = 0; k < nofGPR; k++) {srcGPR[k] = 0; srcGPRcount[k] = 0;}
//		for (int k = 0; k < nofFPR; k++) {srcFPR[k] = 0; srcFPRcount[k] = 0;}
//
//		// get info about in which register parameters are located
//		// parameters which go onto the stack are treated equally
//		for (int k = 0, kGPR = 0, kFPR = 0; k < opds.length; k++) {
//			int type = opds[k].type & ~(1<<ssaTaFitIntoInt);
//			if (type == tLong) {
//				srcGPR[kGPR + paramStartGPR] = opds[k].regLong;
//				srcGPR[kGPR + 1 + paramStartGPR] = opds[k].reg;
//				kGPR += 2;
//			} else if (type == tFloat || type == tDouble) {
//				srcFPR[kFPR + paramStartFPR] = opds[k].reg;
//				kFPR++;
//			} else {
//				srcGPR[kGPR + paramStartGPR] = opds[k].reg;
//				kGPR++;
//			}
//		}
//		
//		// count register usage
//		int i = paramStartGPR;
//
//		if (dbg) {
//			StdStreams.vrb.print("srcGPR = ");
//			for (int k = paramStartGPR; srcGPR[k] != 0; k++) StdStreams.vrb.print(srcGPR[k] + ","); 
//			StdStreams.vrb.println();
//			StdStreams.vrb.print("srcGPRcount = ");
//			for (int n = paramStartGPR; srcGPR[n] != 0; n++) StdStreams.vrb.print(srcGPRcount[n] + ","); 
//			StdStreams.vrb.println();
//		}
//
//		while (srcGPR[i] != 0) srcGPRcount[srcGPR[i++]]++;
//		i = paramStartFPR;
//		while (srcFPR[i] != 0) srcFPRcount[srcFPR[i++]]++;
////		if (dbg) {
////			StdStreams.vrb.print("srcGPR = ");
////			for (i = paramStartGPR; srcGPR[i] != 0; i++) StdStreams.vrb.print(srcGPR[i] + ","); 
////			StdStreams.vrb.println();
////			StdStreams.vrb.print("srcGPRcount = ");
////			for (i = paramStartGPR; srcGPR[i] != 0; i++) StdStreams.vrb.print(srcGPRcount[i] + ","); 
////			StdStreams.vrb.println();
////		}
//		
//		// handle move to itself
//		i = paramStartGPR;
//		while (srcGPR[i] != 0) {
//			if (srcGPR[i] == i) {
////				if (dbg) StdStreams.vrb.println("move to itself");
//				if (i <= paramEndGPR) srcGPRcount[i]--;
//				else srcGPRcount[i]--;	// copy to stack
//			}
//			i++;
//		}
////		if (dbg) {
////			StdStreams.vrb.print("srcGPR = ");
////			for (i = paramStartGPR; srcGPR[i] != 0; i++) StdStreams.vrb.print(srcGPR[i] + ","); 
////			StdStreams.vrb.println();
////			StdStreams.vrb.print("srcGPRcount = ");
////			for (i = paramStartGPR; srcGPR[i] != 0; i++) StdStreams.vrb.print(srcGPRcount[i] + ","); 
////			StdStreams.vrb.println();
////		}
//		i = paramStartFPR;
//		while (srcFPR[i] != 0) {
//			if (srcFPR[i] == i) {
//				if (i <= paramEndFPR) srcFPRcount[i]--;
//				else srcFPRcount[i]--;	// copy to stack
//			}
//			i++;
//		}
//
//		// move registers 
//		boolean done = false;
//		while (!done) {
//			i = paramStartGPR; done = true;
//			while (srcGPR[i] != 0) {
//				if (i > paramEndGPR) {	// copy to stack
//					if (srcGPRcount[i] >= 0) { // check if not done yet
//						if (dbg) StdStreams.vrb.println("\tGPR: parameter " + (i-paramStartGPR) + " from register " + srcGPR[i] + " to stack slot");
//						createIrSrAsimm(ppcStw, srcGPR[i], stackPtr, paramOffset + offset);
//						offset += 4;
//						srcGPRcount[i]=-1; srcGPRcount[srcGPR[i]]--; 
//						done = false;
//					}
//				} else {
//					if (srcGPRcount[i] == 0) { // check if register no longer used for parameter
//						if (dbg) StdStreams.vrb.println("\tGPR: parameter " + (i-paramStartGPR) + " from register " + srcGPR[i] + " to " + i);
//						createIrArSrB(ppcOr, i, srcGPR[i], srcGPR[i]);
//						srcGPRcount[i]--; srcGPRcount[srcGPR[i]]--; 
//						done = false;
//					}
//				}
//				i++; 
//			}
//		}
////		if (dbg) StdStreams.vrb.println();
//		done = false;
//		while (!done) {
//			i = paramStartFPR; done = true;
//			while (srcFPR[i] != 0) {
//				if (i > paramEndFPR) {	// copy to stack
//					if (srcFPRcount[i] >= 0) { // check if not done yet
//						if (dbg) StdStreams.vrb.println("\tFPR: parameter " + (i-paramStartFPR) + " from register " + srcFPR[i] + " to stack slot");
//						createIrSrAd(ppcStfd, srcFPR[i], stackPtr, paramOffset + offset);
//						offset += 8;
//						srcFPRcount[i]=-1; srcFPRcount[srcFPR[i]]--; 
//						done = false;
//					}
//				} else {
//					if (srcFPRcount[i] == 0) { // check if register no longer used for parameter
//						if (dbg) StdStreams.vrb.println("\tFPR: parameter " + (i-paramStartFPR) + " from register " + srcFPR[i] + " to " + i);
//						createIrDrB(ppcFmr, i, srcFPR[i]);
//						srcFPRcount[i]--; srcFPRcount[srcFPR[i]]--; 
//						done = false;
//					}
//				}
//				i++; 
//			}
//		}
//
//		// resolve cycles
//		done = false;
//		while (!done) {
//			i = paramStartGPR; done = true;
//			while (srcGPR[i] != 0) {
//				int src = 0;
//				if (srcGPRcount[i] == 1) {
//					src = i;
//					createIrArSrB(ppcOr, 0, srcGPR[i], srcGPR[i]);
//					srcGPRcount[srcGPR[i]]--;
//					done = false;
//				}
//				boolean done1 = false;
//				while (!done1) {
//					int k = paramStartGPR; done1 = true;
//					while (srcGPR[k] != 0) {
//						if (srcGPRcount[k] == 0 && k != src) {
//							createIrArSrB(ppcOr, k, srcGPR[k], srcGPR[k]);
//							srcGPRcount[k]--; srcGPRcount[srcGPR[k]]--; 
//							done1 = false;
//						}
//						k++; 
//					}
//				}
//				if (src != 0) {
//					createIrArSrB(ppcOr, src, 0, 0);
//					srcGPRcount[src]--;
//				}
//				i++;
//			}
//		}
//		done = false;
//		while (!done) {
//			i = paramStartFPR; done = true;
//			while (srcFPR[i] != 0) {
//				int src = 0;
//				if (srcFPRcount[i] == 1) {
//					src = i;
//					createIrDrB(ppcFmr, 0, srcFPR[i]);
//					srcFPRcount[srcFPR[i]]--;
//					done = false;
//				}
//				boolean done1 = false;
//				while (!done1) {
//					int k = paramStartFPR; done1 = true;
//					while (srcFPR[k] != 0) {
//						if (srcFPRcount[k] == 0 && k != src) {
//							createIrDrB(ppcFmr, k, srcFPR[k]);
//							srcFPRcount[k]--; srcFPRcount[srcFPR[k]]--; 
//							done1 = false;
//						}
//						k++; 
//					}
//				}
//				if (src != 0) {
//					createIrDrB(ppcFmr, src, 0);
//					srcFPRcount[src]--;
//				}
//				i++;
//			}
//		}
//	}
//
//	// copy parameters for subroutines into registers r30/r31, r28/r29
//	private void copyParametersSubroutine(int op0regLong, int op0reg, int op1regLong, int op1reg) {
//		for (int k = 0; k < nofGPR; k++) {srcGPR[k] = 0; srcGPRcount[k] = 0;}
//
//		// get info about in which register parameters are located
//		srcGPR[topGPR] = op0reg;
//		srcGPR[topGPR-1] = op0regLong;
//		if (op1regLong != 0 && op1reg != 0) {srcGPR[topGPR-2] = op1reg; srcGPR[topGPR-3] = op1regLong;}
//		
//		// count register usage
//		int i = topGPR;
//		while (srcGPR[i] != 0) srcGPRcount[srcGPR[i--]]++;
//		
//		// handle move to itself
//		i = topGPR;
//		while (srcGPR[i] != 0) {
//			if (srcGPR[i] == i) srcGPRcount[i]--;
//			i--;
//		}
//
//		// move registers 
//		boolean done = false;
//		while (!done) {
//			i = topGPR; done = true;
//			while (srcGPR[i] != 0) {
//				if (srcGPRcount[i] == 0) { // check if register no longer used for parameter
//					createIrArSrB(ppcOr, i, srcGPR[i], srcGPR[i]);
//					srcGPRcount[i]--; srcGPRcount[srcGPR[i]]--; 
//					done = false;
//				}
//				i--; 
//			}
//		}
//
//		// resolve cycles
//		done = false;
//		while (!done) {
//			i = topGPR; done = true;
//			while (srcGPR[i] != 0) {
//				int src = 0;
//				if (srcGPRcount[i] == 1) {
//					src = i;
//					createIrArSrB(ppcOr, 0, srcGPR[i], srcGPR[i]);
//					srcGPRcount[srcGPR[i]]--;
//					done = false;
//				}
//				boolean done1 = false;
//				while (!done1) {
//					int k = topGPR; done1 = true;
//					while (srcGPR[k] != 0) {
//						if (srcGPRcount[k] == 0 && k != src) {
//							createIrArSrB(ppcOr, k, srcGPR[k], srcGPR[k]);
//							srcGPRcount[k]--; srcGPRcount[srcGPR[k]]--; 
//							done1 = false;
//						}
//						k--; 
//					}
//				}
//				if (src != 0) {
//					createIrArSrB(ppcOr, src, 0, 0);
//					srcGPRcount[src]--;
//				}
//				i--;
//			}
//		}
//	}	
//
//	private static int getInt(byte[] bytes, int index){
//		return (((bytes[index]<<8) | (bytes[index+1]&0xFF))<<8 | (bytes[index+2]&0xFF))<<8 | (bytes[index+3]&0xFF);
//	}
//
//	private void loadConstant(int reg, int val) {
//		assert(reg != 0);
//		int low = val & 0xffff;
//		int high = (val >> 16) & 0xffff;
//		if ((low >> 15) == 0) {
//			if (low != 0 && high != 0) {
//				createIrDrAsimm(ppcAddi, reg, 0, low);
//				createIrDrAsimm(ppcAddis, reg, reg, high);
//			} else if (low == 0 && high != 0) {
//				createIrDrAsimm(ppcAddis, reg, 0, high);		
//			} else if (low != 0 && high == 0) {
//				createIrDrAsimm(ppcAddi, reg, 0, low);
//			} else createIrDrAsimm(ppcAddi, reg, 0, 0);
//		} else {
//			createIrDrAsimm(ppcAddi, reg, 0, low);
//			if (((high + 1) & 0xffff) != 0) createIrDrAsimm(ppcAddis, reg, reg, high + 1);
//		}
//	}
//	
//	private void loadConstantAndFixup(int reg, Item item) {
//		assert(reg != 0);
//		if (lastFixup < 0 || lastFixup > 32768) {ErrorReporter.reporter.error(602); return;}
//		createIrDrAsimm(ppcAddi, reg, 0, lastFixup);
//		createIrDrAsimm(ppcAddis, reg, reg, 0);
//		lastFixup = iCount - 2;
//		fixups[fCount] = item;
//		fCount++;
//		int len = fixups.length;
//		if (fCount == len) {
//			Item[] newFixups = new Item[2 * len];
//			for (int k = 0; k < len; k++)
//				newFixups[k] = fixups[k];
//			fixups = newFixups;
//		}		
//	}
//	
	public abstract void doFixups();

	protected void incInstructionNum() {
		iCount++;
		int len = instructions.length;
		if (iCount == len) {
			int[] newInstructions = new int[2 * len];
			for (int k = 0; k < len; k++)
				newInstructions[k] = instructions[k];
			instructions = newInstructions;
		}
	}

	public abstract void generateCompSpecSubroutines();

	public static void init() { 
		Class cls = (Class)RefType.refTypeList.getItemByName("ch/ntb/inf/deep/unsafe/US");
		if (cls == null) {ErrorReporter.reporter.error(630); return;}
		Method m = Configuration.getOS().getSystemMethodByName(cls, "PUT1");
		if (m != null) idPUT1 = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "PUT2"); 
		if (m != null) idPUT2 = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "PUT4"); 
		if (m != null) idPUT4 = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "PUT8"); 
		if (m != null) idPUT8 = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "GET1"); 
		if (m != null) idGET1 = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "GET2"); 
		if (m != null) idGET2 = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "GET4"); 
		if (m != null) idGET4 = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "GET8"); 
		if (m != null) idGET8 = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "BIT"); 
		if (m != null) idBIT = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "ASM"); 
		if (m != null) idASM = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "GETGPR"); 
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
		m = Configuration.getOS().getSystemMethodByName(cls, "ADR_OF_METHOD"); 
		if (m != null) idADR_OF_METHOD = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "HALT"); 
		if (m != null) idHALT = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "ENABLE_FLOATS"); 
		if (m != null) idENABLE_FLOATS = m.id; else {ErrorReporter.reporter.error(631); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "REF"); 
		if (m != null) idREF = m.id; else {ErrorReporter.reporter.error(631); return;}
		
		cls = (Class)RefType.refTypeList.getItemByName("ch/ntb/inf/deep/lowLevel/LL");
		if (cls == null) {ErrorReporter.reporter.error(632); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "doubleToBits"); 
		if(m != null) idDoubleToBits = m.id; else {ErrorReporter.reporter.error(633); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "bitsToDouble"); 
		if(m != null) idBitsToDouble = m.id; else {ErrorReporter.reporter.error(633); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "floatToBits"); 
		if(m != null) idFloatToBits = m.id; else {ErrorReporter.reporter.error(633); return;}
		m = Configuration.getOS().getSystemMethodByName(cls, "bitsToFloat"); 
		if(m != null) idBitsToFloat = m.id; else {ErrorReporter.reporter.error(633); return;}
		
		objectSize = Type.wktObject.objectSize;
		stringSize = Type.wktString.objectSize;
		
		int2floatConst1 = new StdConstant(HString.getRegisteredHString("int2floatConst1"), (double)(0x10000000000000L + 0x80000000L));
		int2floatConst2 = new StdConstant(HString.getRegisteredHString("int2floatConst2"), (double)0x100000000L);
		int2floatConst3 = new StdConstant(HString.getRegisteredHString("int2floatConst3"), (double)0x10000000000000L);
		Linker32.globalConstantTable = null;
		Linker32.addGlobalConstant(int2floatConst1);
		Linker32.addGlobalConstant(int2floatConst2);
		Linker32.addGlobalConstant(int2floatConst3);
		
		Method.createCompSpecSubroutine("handleException");
		
		final Class stringClass = (Class)Type.wktString;
		final Class heapClass = Configuration.getOS().heapClass;	
		if ((stringClass != null) && (stringClass.methods != null)) {	// check if string class is loaded at all
			stringNewstringMethod = (Method)stringClass.methods.getItemByName("newstring"); 
			if(heapClass != null) {
				heapNewstringMethod = (Method)heapClass.methods.getItemByName("newstring"); 
			}
			if(dbg) {
				if (stringNewstringMethod != null) StdStreams.vrb.println("stringNewstringMethod = " + stringNewstringMethod.name + stringNewstringMethod.methDescriptor); else StdStreams.vrb.println("stringNewstringMethod: not found");
				if (heapNewstringMethod != null) StdStreams.vrb.println("heapNewstringMethod = " + heapNewstringMethod.name + heapNewstringMethod.methDescriptor); else StdStreams.vrb.println("heapNewstringMethod: not found");
			}
			
			m = (Method)stringClass.methods;		
			while (m != null) {
				if (m.name.equals(HString.getRegisteredHString("<init>"))) {
					if (m.methDescriptor.equals(HString.getRegisteredHString("([C)V"))) strInitC = m; 
					else if (m.methDescriptor.equals(HString.getRegisteredHString("([CII)V"))) strInitCII = m;
				}
				m = (Method)m.next;
			}		
			if(dbg) {
				if (strInitC != null) StdStreams.vrb.println("stringInitC = " + strInitC.name + strInitC.methDescriptor); else StdStreams.vrb.println("stringInitC: not found");
				if (strInitCII != null) StdStreams.vrb.println("stringInitCII = " + strInitCII.name + strInitCII.methDescriptor); else StdStreams.vrb.println("stringInitCII: not found");
			}
			
			m = (Method)stringClass.methods;		
			while (m != null) {
				if (m.name.equals(HString.getRegisteredHString("allocateString"))) {
					if (m.methDescriptor.equals(HString.getRegisteredHString("(I[C)Ljava/lang/String;"))) strAllocC = m; 
					else if (m.methDescriptor.equals(HString.getRegisteredHString("(I[CII)Ljava/lang/String;"))) strAllocCII = m;
				}
				m = (Method)m.next;
			}		
			if(dbg) {
				if (strAllocC != null) StdStreams.vrb.println("allocateStringC = " + strAllocC.name + strAllocC.methDescriptor); else StdStreams.vrb.println("allocateStringC: not found");
				if (strAllocCII != null) StdStreams.vrb.println("allocateStringCII = " + strAllocCII.name + strAllocCII.methDescriptor); else StdStreams.vrb.println("allocateStringCII: not found");
			}
		}
	}
}


