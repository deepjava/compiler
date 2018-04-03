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
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.Linker32;
import ch.ntb.inf.deep.ssa.*;
import ch.ntb.inf.deep.ssa.instruction.*;
import ch.ntb.inf.deep.strings.HString;

public class CodeGenARM extends CodeGen implements InstructionOpcs, Registers {

	private static final int arrayLenOffset = 8;	
	// used for some floating point operations and compiler specific subroutines
	private static final int tempStorageSize = 48;	// 1 FPR (temp) + 8 GPRs
	
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
	static boolean tempStorage;
	static boolean enFloatsInExc;

	// information about the src registers for parameters of a call to a method within this method
	private static int[] srcGPR = new int[nofGPR];
	private static int[] srcFPR = new int[nofFPR];
	private static int[] srcGPRcount = new int[nofGPR];
	private static int[] srcFPRcount = new int[nofFPR];

	private static boolean newString;

	public CodeGenARM() {}

	public void translateMethod(Method method) {
		init(method);
		SSA ssa = method.ssa;
		Code32 code = method.machineCode;
		
		if (dbg) StdStreams.vrb.println("build intervals");

		tempStorage = false;
		enFloatsInExc = false;
		RegAllocator.regsGPR = regsGPRinitial;
		RegAllocator.regsFPR = regsFPRinitial;

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
		RegAllocatorARM.assignRegisters();
		if (!RegAllocator.fullRegSet) {	// repeat with a reduced register set
			if (dbg) StdStreams.vrb.println("register allocation for method " + method.owner.name + "." + method.name + " was not successful, run again and use stack slots");
			if (RegAllocator.useLongs) RegAllocator.regsGPR = regsGPRinitial & ~(0x1f << nonVolStartGPR); // change later to 0xff
			else RegAllocator.regsGPR = regsGPRinitial & ~(0x1f << nonVolStartGPR);
			if (dbg) StdStreams.vrb.println("regsGPRinitial = 0x" + Integer.toHexString(RegAllocator.regsGPR));
			RegAllocator.regsFPR = regsFPRinitial& ~(0x7 << nonVolStartFPR);
			if (dbg) StdStreams.vrb.println("regsFPRinitial = 0x" + Integer.toHexString(RegAllocator.regsFPR));
			RegAllocator.stackSlotSpilledRegs = -1;
			parseExitSet(lastExitSet, method.maxStackSlots);
			if (dbg) {
				StdStreams.vrb.print("parameter go into register: ");
				for (int n = 0; paramRegNr[n] != -1; n++) StdStreams.vrb.print(paramRegNr[n] + "  "); 
				StdStreams.vrb.println();
			}
			RegAllocatorARM.resetRegisters();
			RegAllocatorARM.assignRegisters();
		}
//		StdStreams.vrb.print(ssa.toString());

		if (dbg) {
			StdStreams.vrb.println(RegAllocatorARM.joinsToString());
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
//				createIrSrAsimm(ppcStwu, stackPtr, stackPtr, -24);
//				createIrSspr(ppcMtspr, EID, 0);	// must be set for further debugger exceptions
//				createIrSrAd(ppcStmw, 28, stackPtr, 4);
//				createIrArSrB(ppcOr, 31, paramStartGPR, paramStartGPR);	// copy exception into nonvolatile
			} else {
				stackSize = calcStackSizeException();
				insertPrologException();
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
					if ((instr & 0x0f000000) == armB) {		
						if ((instr & 0xffff) != 0) {	// switch
							int nofCases = instr & 0xffff;
							int k;
							for (k = 0; k < nofCases; k++) {
								int branchOffset = ((SSANode)successors[k]).codeStartIndex - (node.codeEndIndex+1-(nofCases-k)*2);
								code.instructions[node.codeEndIndex+1-(nofCases-k)*2] |= (branchOffset - 2) & 0xffffff;
							}
							int branchOffset = ((SSANode)successors[k]).codeStartIndex - node.codeEndIndex;
							code.instructions[node.codeEndIndex] &= 0xff000000;
							code.instructions[node.codeEndIndex] |= (branchOffset - 2) & 0xffffff;
						} else {
							int branchOffset;
							if ((instr & (condAlways << 28)) == condAlways << 28)
								branchOffset = ((SSANode)successors[0]).codeStartIndex - node.codeEndIndex;
							else 
								branchOffset = ((SSANode)successors[1]).codeStartIndex - node.codeEndIndex;
							code.instructions[node.codeEndIndex] |= (branchOffset - 2) & 0xffffff;	// account for pipeline
						}
					}
				} else if (node.instructions[node.nofInstr-1].ssaOpcode == sCreturn) {
					if (node.next != null) {
						int branchOffset = code.iCount - node.codeEndIndex;
						code.instructions[node.codeEndIndex] |= (branchOffset - 2) & 0xffffff;	// account for pipeline
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
//				loadConstantAndFixup(code, 31, m);
//				createIrSspr(ppcMtspr, LR, 31);
//				createIrDrAd(ppcLmw, 28, stackPtr, 4);
//				createIrDrAsimm(ppcAddi, stackPtr, stackPtr, 24);
//				createIBOBILK(ppcBclr, BOalways, 0, false);
			} else {
				insertEpilogException(stackSize);
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
						int reg = RegAllocatorARM.reserveReg(gpr, true);
						int regLong = RegAllocatorARM.reserveReg(gpr, true);
						moveGPRsrc[nofMoveGPR] = nofParamGPR;
						moveGPRsrc[nofMoveGPR+1] = nofParamGPR+1;
						moveGPRdst[nofMoveGPR++] = reg;
						moveGPRdst[nofMoveGPR++] = regLong;
						paramRegNr[i] = reg;
						paramRegNr[i+1] = regLong;
						if(dbg) StdStreams.vrb.print(reg + ",r" + regLong);
					} else {
						int reg = paramStartGPR + nofParamGPR;
						if (reg <= paramEndGPR) RegAllocatorARM.reserveReg(gpr, reg);
						else {
							reg = RegAllocatorARM.reserveReg(gpr, false);
							moveGPRsrc[nofMoveGPR] = nofParamGPR;
							moveGPRdst[nofMoveGPR++] = reg;
						}
						int regLong = paramStartGPR + nofParamGPR + 1;
						if (regLong <= paramEndGPR) RegAllocatorARM.reserveReg(gpr, regLong);
						else {
							regLong = RegAllocatorARM.reserveReg(gpr, false);
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
						int reg = RegAllocatorARM.reserveReg(fpr, true);
						moveFPRsrc[nofMoveFPR] = nofParamFPR;
						moveFPRdst[nofMoveFPR++] = reg;
						paramRegNr[i] = reg;
						if(dbg) StdStreams.vrb.print(reg);
					} else {
						int reg = paramStartFPR + nofParamFPR;
						if (reg <= paramEndFPR) RegAllocatorARM.reserveReg(fpr, reg);
						else {
							reg = RegAllocatorARM.reserveReg(fpr, false);
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
						int reg = RegAllocatorARM.reserveReg(gpr, true);	// nonvolatile or stack slot
						moveGPRsrc[nofMoveGPR] = nofParamGPR;
						moveGPRdst[nofMoveGPR++] = reg;
						paramRegNr[i] = reg;
						if(dbg) StdStreams.vrb.print(reg);
					} else {
						int reg = paramStartGPR + nofParamGPR;
						if (reg <= paramEndGPR) RegAllocatorARM.reserveReg(gpr, reg); // mark as reserved
						else {
							reg = RegAllocatorARM.reserveReg(gpr, false);	// volatile, nonvolatile or stack slot
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
		int size = 4 + callParamSlotsOnStack * 4 + nofNonVolGPR * 4 + nofNonVolFPR * 8 + RegAllocator.maxLocVarStackSlots * 4 + (tempStorage? tempStorageSize : 0);
		if (enFloatsInExc) size += nonVolStartFPR * 8 + 8;	// save volatile FPR's and FPSCR
//		int padding = (16 - (size % 16)) % 16;
//		size = size + padding;
		LRoffset = size - 4;
		GPRoffset = LRoffset - nofNonVolGPR * 4;
		FPRoffset = GPRoffset - nofNonVolFPR * 8;
		if (enFloatsInExc) FPRoffset -= nonVolStartFPR * 8 + 8;
		localVarOffset = FPRoffset - RegAllocator.maxLocVarStackSlots * 4;
		tempStorageOffset = FPRoffset - tempStorageSize;	//TODO change as in ppc
		paramOffset = 4;
		return size;
	}

	private static int calcStackSizeException() {
		int size = 28 + nofGPR * 4 + RegAllocator.maxLocVarStackSlots * 4 + (tempStorage? tempStorageSize : 0);
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
		tempStorageOffset = FPRoffset - tempStorageSize;
		paramOffset = 4;
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
//			instr.machineCodeOffset = iCount;
			if (node.isCatch && i == 0 && node.loadLocalExc > -1) {	
				if (dbg) StdStreams.vrb.println("enter move register intruction for local 'exception' in catch clause: from R" + paramStartGPR + " to R" + node.instructions[node.loadLocalExc].result.reg);
//				createIrArSrB(ppcOr, node.instructions[node.loadLocalExc].result.reg, paramStartGPR, paramStartGPR);
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
					createLSWordImm(code, armLdr, condAlways, src3RegLong, stackPtr, localVarOffset + 4 * slot, 1, 1, 0);	
				}
				if (src3Reg >= 0x100) {
					assert false;
					if (dbg) StdStreams.vrb.println("opd3 reg on stack slot for instr: " + instr.toString());
					int slot = src3Reg & 0xff;
					if ((opds[2].type == tFloat) || (opds[2].type == tDouble)) {
						src3Reg = nonVolStartFPR + 0;
//						createIrDrAd(code, ppcLfd, src3Reg, stackPtr, localVarOffset + 4 * slot);
					} else {
						src3Reg = nonVolStartGPR + 0;
						createLSWordImm(code, armLdr, condAlways, src3Reg, stackPtr, localVarOffset + 4 * slot, 1, 1, 0);	
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
						createLSWordImm(code, armLdr, condAlways, src2RegLong, stackPtr, localVarOffset + 4 * slot, 1, 1, 0);	
					}
					if (src2Reg >= 0x100) {
						if (dbg) StdStreams.vrb.println("opd2 reg on stack slot for instr: " + instr.toString());
						int slot = src2Reg & 0xff;
						if ((opds[1].type == tFloat) || (opds[1].type == tDouble)) {
							src2Reg = nonVolStartFPR + 2;
//							createIrDrAd(code, ppcLfd, src2Reg, stackPtr, localVarOffset + 4 * slot);
						} else {
							src2Reg = nonVolStartGPR + 2;
							createLSWordImm(code, armLdr, condAlways, src2Reg, stackPtr, localVarOffset + 4 * slot, 1, 1, 0);	
						}
					}			
				}
				src1Reg = opds[0].reg; 
				src1RegLong = opds[0].regLong;
				if (src1RegLong >= 0x100) {
					if (dbg) StdStreams.vrb.println("opd1 regLong on stack slot for instr: " + instr.toString());
					int slot = src1RegLong & 0xff;
					src1RegLong = nonVolStartGPR + 6;
					createLSWordImm(code, armLdr, condAlways, src1RegLong, stackPtr, localVarOffset + 4 * slot, 1, 1, 0);	
				}
				if (src1Reg >= 0x100) {
					if (dbg) StdStreams.vrb.println("opd1 reg on stack slot for instr: " + instr.toString());
					int slot = src1Reg & 0xff;
					if ((opds[0].type == tFloat) || (opds[0].type == tDouble)) {
						src1Reg = nonVolStartFPR + 1;
//						createIrDrAd(code, ppcLfd, src1Reg, stackPtr, localVarOffset + 4 * slot);
					} else {
						src1Reg = nonVolStartGPR + 1;
						createLSWordImm(code, armLdr, condAlways, src1Reg, stackPtr, localVarOffset + 4 * slot, 1, 1, 0);	
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
						break;
					case tDouble:
						constant = (StdConstant)res.constant;
						if (constant.valueH == 0) {	// 0.0 must be loaded directly as it's not in the cp
//							createIrDrAsimm(code, ppcAddi, gAux1, 0, 0);
//							createIrSrAd(code, ppcStw, gAux1, stackPtr, tempStorageOffset);
//							createIrSrAd(code, ppcStw, gAux1, stackPtr, tempStorageOffset+4);
//							createIrDrAd(code, ppcLfd, dReg, stackPtr, tempStorageOffset);
						} else if (constant.valueH == 0x3ff00000) {	// 1.0
//							createIrDrAsimm(code, ppcAddis, gAux1, 0, 0x3ff0);
//							createIrSrAd(code, ppcStw, gAux1, stackPtr, tempStorageOffset);
//							createIrDrAsimm(code, ppcAddis, gAux1, 0, 0);
//							createIrSrAd(code, ppcStw, gAux1, stackPtr, tempStorageOffset+4);
//							createIrDrAd(code, ppcLfd, dReg, stackPtr, tempStorageOffset);
						} else {
							loadConstantAndFixup(code, gAux1, constant);	// address of constant (in the const area) is loaded
							createLSExtReg(code, armVldr, condAlways, dReg, gAux1, 0, false);
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
//						createItrap(ppcTwi, TOifequal, refReg, 0);
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
					createLSWordImm(code, armLdr, condAlways, dRegLong, refReg, offset, 1, 1, 0);
					createLSWordImm(code, armLdr, condAlways, dReg, refReg, offset + 4, 1, 1, 0);
					break;
				case tFloat: case tDouble:
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
//					createIrDrAd(ppcLwz, res.regGPR1, refReg, objectSize);	// read field "count", must be first field
					createDataProcMovReg(code, armLsl, condAlways, dReg, refReg, noShift, objectSize);	// read field "count", must be first field
//					createItrap(ppcTw, TOifgeU, indexReg, res.regGPR1);
					switch (res.type & 0x7fffffff) {	// type to read
					case tByte:
						createDataProcMovReg(code, armLsl, condAlways, LR, indexReg, noShift, 1);
						createDataProcImm(code, armAdd, condAlways, dReg, refReg, stringSize - 4);	// add index of field "value" to index
						createLSWordRegA(code, armLdrsb, condAlways, dReg, LR, dReg, noShift, 0, 1, 1, 0);
						break;
					case tChar:
						createDataProcMovReg(code, armLsl, condAlways, LR, indexReg, noShift, 1);
						createDataProcImm(code, armAdd, condAlways, dReg, refReg, stringSize - 4);	// add index of field "value" to index
						createLSWordRegA(code, armLdrh, condAlways, dReg, LR, dReg, noShift, 0, 1, 1, 0);
						break;
					default:
						ErrorReporter.reporter.error(610);
						assert false : "result of SSA instruction has wrong type";
						return;
					}
				} else {
//					createItrap(ppcTwi, TOifequal, refReg, 0);
//					createIrDrAd(ppcLha, res.regGPR1, refReg, -arrayLenOffset);
//					createItrap(ppcTw, TOifgeU, indexReg, res.regGPR1);
					switch (res.type & ~(1<<ssaTaFitIntoInt)) {	// type to read
					case tByte: case tBoolean:
						createDataProcImm(code, armAdd, condAlways, LR, refReg, objectSize);
						createLSWordRegA(code, armLdrsb, condAlways, dReg, LR, indexReg, noShift, 0, 1, 1, 0);
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
					case tLong: case tFloat: case tDouble:
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
//					createItrap(ppcTwi, TOifequal, refReg, 0);
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
					createLSWordImm(code, armStr, condAlways, valRegLong, refReg, offset, 1, 1, 0);
					createLSWordImm(code, armStr, condAlways, valReg, refReg, offset + 4, 1, 1, 0);
					break;
				case tFloat: case tDouble:
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
//					createItrap(ppcTwi, TOifequal, refReg, 0);
//					createIrDrAd(ppcLha, res.regGPR1, refReg, -arrayLenOffset);
//					createItrap(ppcTw, TOifgeU, indexReg, res.regGPR1);
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
					case tAlong: case tAfloat: case tAdouble:
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
				case tLong: case tFloat:
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
				case tLong: case tFloat: case tDouble:
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
				case tLong: case tFloat: case tDouble:
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
						if (RegAllocatorARM.isPowerOf2(immVal)) {	// is power of 2
							int shift = 0;
							while (immVal > 1) {shift++; immVal >>= 1;}
							if (shift == 0) 
								createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
							else {
								createDataProcMovReg(code, armAsr, condAlways, LR, src1Reg, noShift, shift - 1);
								createDataProcMovReg(code, armLsr, condAlways, LR, LR, noShift, 32 - shift);
								createDataProcReg(code, armAdd, condAlways, LR, src1Reg, LR, noShift, 0);
								createDataProcMovReg(code, armAsr, condAlways, dReg, LR, noShift, shift);
							}
						} else {	// A = 2^(32+n) / immVal
							int val = Integer.highestOneBit(immVal);
							loadConstant(code, LR, (int) (0x100000001L * val / immVal));
							createMulLong(code, armUmull, condAlways, dReg, LR, src1Reg, LR);
							createDataProcMovReg(code, armAsr, condAlways, dReg, dReg, noShift, Integer.numberOfTrailingZeros(val));
						}
					} else {
//						 CMP             src2Reg, #0	//TODO
//						 BEQ divide_end
//						 ;check for divide by zero!

						loadConstant(code, dReg, 0);	// clear dReg to accumulate result
						loadConstant(code, gAux1, 1); // set bit 0 in aux register, which will be shifted left then right
						// loop 1, shift denominator until it is bigger than nominator
						createDataProcCmpReg(code, armCmp, condAlways, src2Reg, src1Reg, noShift, 0);
						createDataProcMovReg(code, armMov, condLS, src2Reg, src2Reg, LSL, 1);
						createDataProcMovReg(code, armMov, condLS, gAux1, gAux1, LSL, 1);
						createBranchImm(code, armB, condLS, -5);
						// loop 2
						createDataProcCmpReg(code, armCmp, condAlways, src1Reg, src2Reg, noShift, 0);
						createDataProcReg(code, armSub, condCS, src1Reg, src1Reg, src2Reg, noShift, 0);
						createDataProcReg(code, armAdd, condCS, dReg, dReg, gAux1, noShift, 0);
						createDataProcMovReg(code, armMovs, condAlways, gAux1, gAux1, LSR, 1);
						createDataProcMovReg(code, armMov, condCC, src2Reg, src2Reg, LSR, 1);
						createBranchImm(code, armB, condCC, -7);
					}
					break;
				case tLong: case tFloat: case tDouble:
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
						if (RegAllocatorARM.isPowerOf2(immVal)) {	// is power of 2
							int shift = 0;
							while (immVal > 1) {shift++; immVal >>= 1;}
							if (shift == 0) 
								createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
							else {
								createDataProcMovReg(code, armAsr, condAlways, LR, src1Reg, noShift, shift - 1);
								createDataProcMovReg(code, armLsr, condAlways, LR, LR, noShift, 32 - shift);
								createDataProcReg(code, armAdd, condAlways, LR, src1Reg, LR, noShift, 0);
								createDataProcMovReg(code, armAsr, condAlways, dReg, LR, noShift, shift);
							}
						} else {	// A = 2^(32+n) / immVal
							int val = Integer.highestOneBit(immVal);
							loadConstant(code, LR, (int) (0x100000001L * val / immVal));
							createMulLong(code, armUmull, condAlways, dReg, LR, src1Reg, LR);
							createDataProcMovReg(code, armAsr, condAlways, dReg, dReg, noShift, Integer.numberOfTrailingZeros(val));
							loadConstant(code, LR, immVal);
							createMul(code, armMul, condAlways, LR, LR, dReg);
							createDataProcReg(code, armSub, condAlways, dReg, src1Reg, LR, noShift, 0);
						}
					} else {
//						assert false;
					}
					break;
				case tLong: case tFloat: case tDouble:
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
				case tFloat: case tDouble:
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
						if (immVal < 32) {
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
					} else { 
						createDataProcImm(code, armAnd, condAlways, scratchReg, src2Reg, 0x3f);
						createDataProcImm(code, armRsbs, condAlways, gAux1, scratchReg, 32);
						createDataProcShiftReg(code, armLsr, condGE, gAux1, src1Reg, gAux1);
						createDataProcShiftReg(code, armLsl, condGE, dRegLong, src1RegLong, scratchReg);
						createDataProcReg(code, armOrr, condGE, dRegLong, gAux1, dRegLong, noShift, 0);
						createDataProcShiftReg(code, armLsl, condGE, dReg, src1Reg, scratchReg);
						createDataProcImm(code, armSub, condLT, scratchReg, src2Reg, 32);
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
						createDataProcMovReg(code, armAsr, condAlways, dReg, src1Reg, noShift, immVal);
					} else {
						createDataProcImm(code, armAnd, condAlways, src2Reg, src2Reg, 0x1f);	// arm takes the lowest 8 bit, whereas java allows only 5 bits
						createDataProcShiftReg(code, armAsr, condAlways, dReg, src1Reg, src2Reg);
					}
				} else if (type == tLong) {
					if (src2Reg < 0) {	
						int immVal = ((StdConstant)opds[1].constant).valueH % 64;
						if (immVal == 0) {
							createDataProcMovReg(code, armMov, condAlways, dRegLong, src1RegLong, noShift, 0);
							createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
						} else if (immVal < 32) {
							createDataProcShiftImm(code, armLsl, condAlways, dReg, src1RegLong, 32 - immVal);
							createDataProcReg(code, armOrr, condAlways, dReg, dReg, src1Reg, ASR, immVal);
							createDataProcShiftImm(code, armAsr, condAlways, dRegLong, src1RegLong, immVal);
						} else if (immVal == 32) {
							createDataProcMovReg(code, armMov, condAlways, dReg, src1RegLong, noShift, 0);
							createMedia(code, armSbfx, condAlways, dRegLong, dReg, 31, 1);							
						} else {
							createDataProcShiftImm(code, armAsr, condAlways, dReg, src1RegLong, immVal - 32);
							createMedia(code, armSbfx, condAlways, dRegLong, dReg, 31, 1);							
						}
					} else { 
						createDataProcImm(code, armAnd, condAlways, scratchReg, src2Reg, 0x3f);
						createDataProcImm(code, armRsbs, condAlways, dReg, scratchReg, 32);
						createDataProcShiftReg(code, armLsl, condGE, dReg, src1RegLong, dReg);
						createDataProcShiftReg(code, armLsr, condGE, dRegLong, src1Reg, scratchReg);
						createDataProcReg(code, armOrr, condGE, dReg, dReg, dRegLong, noShift, 0);
						createDataProcShiftReg(code, armAsr, condGE, dRegLong, src1RegLong, scratchReg);
						createDataProcImm(code, armSub, condLT, scratchReg, src2Reg, 32);
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
						createDataProcMovReg(code, armLsr, condAlways, dReg, src1Reg, noShift, immVal);
					} else {
						createDataProcImm(code, armAnd, condAlways, src2Reg, src2Reg, 0x1f);	// arm takes the lowest 8 bit, whereas java allows only 5 bits
						createDataProcShiftReg(code, armLsr, condAlways, dReg, src1Reg, src2Reg);
					}
				} else if (type == tLong) {
					if (src2Reg < 0) {	
						int immVal = ((StdConstant)opds[1].constant).valueH % 64;
						if (immVal == 0) {
							createDataProcMovReg(code, armMov, condAlways, dRegLong, src1RegLong, noShift, 0);
							createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
						} else if (immVal < 32) {
							createDataProcShiftImm(code, armLsl, condAlways, dReg, src1RegLong, 32 - immVal);
							createDataProcReg(code, armOrr, condAlways, dReg, dReg, src1Reg, LSR, immVal);
							createDataProcShiftImm(code, armLsr, condAlways, dRegLong, src1RegLong, immVal);
						} else if (immVal == 32) {
							createDataProcMovReg(code, armMov, condAlways, dReg, src1RegLong, noShift, 0);
							createMovw(code, armMovw, condAlways, dRegLong, 0);							
						} else {
							createDataProcShiftImm(code, armLsr, condAlways, dReg, src1RegLong, immVal - 32);
							createMovw(code, armMovw, condAlways, dRegLong, 0);
						}
					} else { 
						createDataProcImm(code, armAnd, condAlways, scratchReg, src2Reg, 0x3f);
						createDataProcImm(code, armRsbs, condAlways, dReg, scratchReg, 32);
						createDataProcShiftReg(code, armLsl, condGE, dReg, src1RegLong, dReg);
						createDataProcShiftReg(code, armLsr, condGE, dRegLong, src1Reg, scratchReg);
						createDataProcReg(code, armOrr, condGE, dReg, dReg, dRegLong, noShift, 0);
						createDataProcShiftReg(code, armLsr, condGE, dRegLong, src1RegLong, scratchReg);
						createDataProcImm(code, armSub, condLT, scratchReg, src2Reg, 32);
						createDataProcShiftReg(code, armLsr, condLT, dReg, src1RegLong, scratchReg);
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
				case tFloat: case tDouble:
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
					assert false : "not done yet";
					break;
				case tChar: 
					assert false : "not done yet";
					break;
				case tShort: 
					assert false : "not done yet";
					break;
				case tInteger:
					createDataProcMovReg(code, armMov, condAlways, dReg, src1Reg, noShift, 0);
					break;
				case tFloat:
					assert false : "not done yet";
					break;
				case tDouble:
//					assert false : "not done yet";
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}
			case sCconvFloat: {	// float -> other type
//				ErrorReporter.reporter.error(610);
//				assert false : "result of SSA instruction has wrong type";
				break;}
			case sCconvDouble: {	// double -> other type
//				ErrorReporter.reporter.error(610);
//				assert false : "result of SSA instruction has wrong type";
				break;}
			case sCcmpl: case sCcmpg: {
				int type = opds[0].type & ~(1<<ssaTaFitIntoInt);
				if (type == tLong) {
					instr = node.instructions[i+1];
					if (instr.ssaOpcode == sCregMove) {i++; instr = node.instructions[i+1]; assert false;}
					assert instr.ssaOpcode == sCbranch : "sCcompl or sCcompg is not followed by branch instruction";
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
						createDataProcCmpReg(code, armCmp, condAlways, src1RegLong, src2RegLong, noShift, 0);
						createDataProcCmpReg(code, armCmp, condEQ, src1Reg, src2Reg, noShift, 0);
						createBranchImm(code, armB, condLT, 0);
					} else if (bci == bCifge) {
						createDataProcCmpReg(code, armCmp, condAlways, src1RegLong, src2RegLong, noShift, 0);
						createDataProcCmpReg(code, armCmp, condEQ, src1Reg, src2Reg, noShift, 0);
						createBranchImm(code, armB, condGE, 0);
					} else if (bci == bCifgt) {
						createDataProcCmpReg(code, armCmp, condAlways, src1RegLong, src2RegLong, noShift, 0);
						createDataProcCmpReg(code, armCmp, condEQ, src1Reg, src2Reg, noShift, 0);
						createBranchImm(code, armB, condGT, 0);
					} else if (bci == bCifle) {
						createDataProcCmpReg(code, armCmp, condAlways, src1RegLong, src2RegLong, noShift, 0);
						createDataProcCmpReg(code, armCmp, condEQ, src1Reg, src2Reg, noShift, 0);
						createBranchImm(code, armB, condLE, 0);
					} else {
						ErrorReporter.reporter.error(623);
						assert false : "sCcompl or sCcompg is not followed by branch instruction";
						return;
					}
				} else if (type == tFloat  || type == tDouble) {
					i--;
				} else {
					ErrorReporter.reporter.error(611);
					assert false : "operand of SSA instruction has wrong type";
					return;
				}
				i++;
				break;}
//				createDataProcCmpReg(code, armCmp, condAlways, 0, src2RegLong, src1RegLong, noShift, 0);
//				createDataProcReg(code, armCmp, condAlways, 0, src2Reg, src1Reg, noShift, 0);
			case sCinstanceof: {
//				assert false;
				break;}
			case sCcheckcast: {
//				assert false;
				break;}
			case sCthrow: {
//				assert false;
				break;}
			case sCalength: {
				int refReg = src1Reg;
//				createItrap(ppcTwi, TOifequal, refReg, 0);
				createLSWordImm(code, armLdrsh, condAlways, dReg, refReg, arrayLenOffset, 1, 0, 0);
				break;}
			case sCcall: {
				Call call = (Call)instr;
				Method m = (Method)call.item;
				if ((m.accAndPropFlags & (1 << dpfSynthetic)) != 0) {
					if (m.id == idGET1) {	// GET1
						createLSWordImm(code, armLdrsb, condAlways, dReg, src1Reg, 0, 0, 0, 0);
					} else if (m.id == idGET2) { // GET2
						createLSWordImm(code, armLdrsh, condAlways, dReg, src1Reg, 0, 0, 0, 0);
					} else if (m.id == idGET4) { // GET4
						createLSWordImm(code, armLdr, condAlways, dReg, src1Reg, 0, 0, 0, 0);
					} else if (m.id == idGET8) { // GET8
						createLSWordImm(code, armLdr, condAlways, dRegLong, src1Reg, 0, 0, 0, 0);
						createLSWordImm(code, armLdr, condAlways, dReg, src1Reg, 4, 1, 1, 0);
					} else if (m.id == idPUT1) { // PUT1
						createLSWordImm(code, armStrb, condAlways, src2Reg, src1Reg, 0, 0, 0, 0);
					} else if (m.id == idPUT2) { // PUT2
						createLSWordImm(code, armStrh, condAlways, src2Reg, src1Reg, 0, 0, 0, 0);
					} else if (m.id == idPUT4) { // PUT4
						createLSWordImm(code, armStr, condAlways, src2Reg, src1Reg, 0, 0, 0, 0);
					} else if (m.id == idPUT8) { // PUT8
						createLSWordImm(code, armStr, condAlways, src2RegLong, src1Reg, 0, 0, 0, 0);
						createLSWordImm(code, armStr, condAlways, src2Reg, src1Reg, 4, 1, 1, 0);
//					} else if (m.id == idBIT) { // BIT
//						createIrDrAd(ppcLbz, res.reg, opds[0].reg, 0);
//						createIrDrAsimm(ppcSubfic, 0, opds[1].reg, 32);
//						createIrArSrBMBME(ppcRlwnm, res.reg, res.reg, 0, 31, 31);
					} else if (m.id == idGETGPR) { // GETGPR
						int gpr = ((StdConstant)opds[0].constant).valueH;
//						createIrArSrB(ppcOr, res.reg, gpr, gpr);
						createDataProcMovReg(code, armMov, condAlways, dReg, gpr, noShift, 0);
//					} else if (m.id == idGETFPR) { // GETFPR
//						int fpr = ((StdConstant)opds[0].constant).valueH;
//						createIrDrB(ppcFmr, res.reg, fpr);
//					} else if (m.id == idGETSPR) { // GETSPR
//						int spr = ((StdConstant)opds[0].constant).valueH;
//						createIrSspr(ppcMfspr, spr, res.reg);
					} else if (m.id == idPUTGPR) { // PUTGPR
						int gpr = ((StdConstant)opds[0].constant).valueH;
//						if (src2Reg < 0) {
//							int immVal = ((StdConstant)opds[1].constant).valueH;
//							createIrDrAsimm(code, ppcAddi, gpr, 0, immVal);
//						} else 
						createDataProcMovReg(code, armMov, condAlways, gpr, src2Reg, noShift, 0);
//					} else if (m.id == idPUTFPR) { // PUTFPR
//						int fpr = ((StdConstant)opds[0].constant).valueH;
//						createIrDrB(ppcFmr, fpr, opds[1].reg);
//					} else if (m.id == idPUTSPR) { // PUTSPR
//						createIrArSrB(ppcOr, 0, opds[1].reg, opds[1].reg);
//						int spr = ((StdConstant)opds[0].constant).valueH;
//						createIrSspr(ppcMtspr, spr, 0);
//					} else if (m.id == idHALT) { // HALT	// TODO
//						createItrap(ppcTw, TOalways, 0, 0);
					} else if (m.id == idASM) { // ASM
						code.instructions[code.iCount] = InstructionDecoder.dec.getCode(((StringLiteral)opds[0].constant).string.toString());
//						System.out.println(((StringLiteral)opds[0].constant).string.toString());
//						System.out.println(Integer.toHexString(InstructionDecoder.dec.getCode(((StringLiteral)opds[0].constant).string.toString())));
//						System.out.println(InstructionDecoder.dec.getMnemonic((InstructionDecoder.dec.getCode(((StringLiteral)opds[0].constant).string.toString()))));
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
//						createIrSrAd(ppcStfd, opds[0].reg, stackPtr, tempStorageOffset);
//						createIrDrAd(ppcLwz, res.regLong, stackPtr, tempStorageOffset);
//						createIrDrAd(ppcLwz, res.reg, stackPtr, tempStorageOffset + 4);
					} else if (m.id == idBitsToDouble) { // BitsToDouble
//						createIrSrAd(ppcStw, opds[0].regLong, stackPtr, tempStorageOffset);
//						createIrSrAd(ppcStw, opds[0].reg, stackPtr, tempStorageOffset+4);
//						createIrDrAd(ppcLfd, res.reg, stackPtr, tempStorageOffset);
					} else if (m.id == idFloatToBits) { // FloatToBits
//						createIrSrAd(ppcStfs, opds[0].reg, stackPtr, tempStorageOffset);
//						createIrDrAd(ppcLwz, res.reg, stackPtr, tempStorageOffset);
					} else if (m.id == idBitsToFloat) { // BitsToFloat
//						createIrSrAd(ppcStw, opds[0].reg, stackPtr, tempStorageOffset);
//						createIrDrAd(ppcLfs, 0, stackPtr, tempStorageOffset);
//						createIrDrB(ppcFmr, res.reg, 0);
					} else {
						ErrorReporter.reporter.error(626, m.name.toString());
						assert false : "not implemented " + m.name.toString();
					}
				} else {	// real method (not synthetic)
					// copy parameters into registers and to stack if not enough registers
//					if (dbg) StdStreams.vrb.println("call to " + m.name + ": copy parameters");
//					copyParameters(code, opds);
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
						int refReg = src1Reg;
						int offset = (Class.maxExtensionLevelStdClasses + 1) * Linker32.slotSize + Linker32.tdBaseClass0Offset;
//						createItrap(ppcTwi, TOifequal, refReg, 0);
//						createIrDrAd(ppcLwz, res.regGPR1, refReg, -4);
//						createIrDrAd(ppcLwz, res.regGPR1, res.regGPR1, offset);	// delegate method
//						createIrSspr(ppcMtspr, LR, res.regGPR1);
					} else if (call.invokespecial) {	// invokespecial
						if (newString) {	// special treatment for strings
							if (m == strInitC) m = strAllocC;
							else if (m == strInitCII) m = strAllocCII;	// addr of corresponding allocate method
							else if (m == strInitCII) m = strAllocCII;
							if (dbg) StdStreams.vrb.println("call to " + m.name + ": copy parameters");
							copyParameters(code, opds);
							insertBLAndFixup(code, m);
						} else {
							int refReg = src1Reg;
							if (dbg) StdStreams.vrb.println("call to " + m.name + ": copy parameters");
							copyParameters(code, opds);
//							createItrap(ppcTwi, TOifequal, refReg, 0);
							insertBLAndFixup(code, m);
						}
					} else {	// invokevirtual 
						int refReg = src1Reg;
						int offset = Linker32.tdMethTabOffset;
						offset -= m.index * Linker32.slotSize; 
//						createItrap(ppcTwi, TOifequal, refReg, 0);
						createLSWordImm(code, armLdr, condAlways, LR, refReg, 4, 1, 0, 0);
						createLSWordImm(code, armLdr, condAlways, LR, LR, -offset, 1, 0, 0);
						if (dbg) StdStreams.vrb.println("call to " + m.name + ": copy parameters");
						copyParameters(code, opds);
						createBranchReg(code, armBlxReg, condAlways, LR);
					}

//					if ((m.accAndPropFlags & (1<<dpfInterfCall)) != 0) {	// invokeinterface
//						// interface info goes into last parameter register
//						loadConstant(paramEndGPR, m.owner.index << 16 | m.index * 4);	// interface id and method offset						// check if param = maxParam in reg -2
//					}
					
					if (newString) {
						int sizeOfObject = Type.wktObject.objectSize;
						createDataProcImm(code, armMov, condAlways, paramStartGPR + opds.length, 0, sizeOfObject); // reg after last parameter
					}

					// get result
					int type = res.type & ~(1<<ssaTaFitIntoInt);
					if (type == tLong) {
//						assert false;
//						if (res.regLong == returnGPR2) {
//							if (res.reg == returnGPR1) {	// returnGPR2 -> r0, returnGPR1 -> r3, r0 -> r2
//								createIrArSrB(ppcOr, 0, returnGPR2, returnGPR2);
//								createIrArSrB(ppcOr, res.regLong, returnGPR1, returnGPR1);
//								createIrArSrB(ppcOr, res.reg, 0, 0);
//							} else {	// returnGPR2 -> reg, returnGPR1 -> r3
//								createIrArSrB(ppcOr, res.reg, returnGPR2, returnGPR2);
//								createIrArSrB(ppcOr, res.regLong, returnGPR1, returnGPR1);
//							}
//						} else { // returnGPR1 -> regLong, returnGPR2 -> reg
//							createIrArSrB(ppcOr, res.regLong, returnGPR1, returnGPR1);
//							createIrArSrB(ppcOr, res.reg, returnGPR2, returnGPR2);
//						}
					} else if (type == tFloat || type == tDouble) {
//						assert false;
//						createIrDrB(ppcFmr, res.reg, returnFPR);
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
					assert false;
//					method = CFR.getNewMemoryMethod(bCmultianewarray);
//					loadConstantAndFixup(res.regGPR1, method);	// addr of multianewarray
//					createIrSspr(ppcMtspr, LR, res.regGPR1);
//					// copy dimensions
//					for (int k = 0; k < nofGPR; k++) {srcGPR[k] = 0; srcGPRcount[k] = 0;}
//
//					// get info about in which register parameters are located
//					// the first two parameter registers are used for nofDim and ref
//					// therefore start is at paramStartGPR + 2
//					for (int k = 0, kGPR = 0; k < opds.length; k++) {
//						int type = opds[k].type & ~(1<<ssaTaFitIntoInt);
//						if (type == tLong) {
//							srcGPR[kGPR + paramStartGPR + 2] = opds[k].regLong;	
//							srcGPR[kGPR + 1 + paramStartGPR + 2] = opds[k].reg;
//							kGPR += 2;
//						} else {
//							srcGPR[kGPR + paramStartGPR + 2] = opds[k].reg;
//							kGPR++;
//						}
//					}
//					
//					// count register usage
//					int cnt = paramStartGPR + 2;
//					while (srcGPR[cnt] != 0) srcGPRcount[srcGPR[cnt++]]++;
//					
//					// handle move to itself
//					cnt = paramStartGPR + 2;
//					while (srcGPR[cnt] != 0) {
//						if (srcGPR[cnt] == cnt) srcGPRcount[cnt]--;
//						cnt++;
//					}
//
//					// move registers 
//					boolean done = false;
//					while (!done) {
//						cnt = paramStartGPR + 2; done = true;
//						while (srcGPR[cnt] != 0) {
//							if (srcGPRcount[cnt] == 0) { // check if register no longer used for parameter
//								if (dbg) StdStreams.vrb.println("\tGPR: parameter " + (cnt-paramStartGPR) + " from register " + srcGPR[cnt] + " to " + cnt);
//								createIrArSrB(ppcOr, cnt, srcGPR[cnt], srcGPR[cnt]);
//								srcGPRcount[cnt]--; srcGPRcount[srcGPR[cnt]]--; 
//								done = false;
//							}
//							cnt++; 
//						}
//					}
//					if (dbg) StdStreams.vrb.println();
//
//					// resolve cycles
//					done = false;
//					while (!done) {
//						cnt = paramStartGPR + 2; done = true;
//						while (srcGPR[cnt] != 0) {
//							int src = 0;
//							if (srcGPRcount[cnt] == 1) {
//								src = cnt;
//								createIrArSrB(ppcOr, 0, srcGPR[cnt], srcGPR[cnt]);
//								srcGPRcount[srcGPR[cnt]]--;
//								done = false;
//							}
//							boolean done1 = false;
//							while (!done1) {
//								int k = paramStartGPR + 2; done1 = true;
//								while (srcGPR[k] != 0) {
//									if (srcGPRcount[k] == 0 && k != src) {
//										createIrArSrB(ppcOr, k, srcGPR[k], srcGPR[k]);
//										srcGPRcount[k]--; srcGPRcount[srcGPR[k]]--; 
//										done1 = false;
//									}
//									k++; 
//								}
//							}
//							if (src != 0) {
//								createIrArSrB(ppcOr, src, 0, 0);
//								srcGPRcount[src]--;
//							}
//							cnt++;
//						}
//					}
//					loadConstantAndFixup(paramStartGPR, item);	// ref to type descriptor
//					createIrDrAsimm(ppcAddi, paramStartGPR+1, 0, opds.length);	// nofDimensions
//					createIBOBILK(ppcBclr, BOalways, 0, true);
//					createIrArSrB(ppcOr, res.reg, returnGPR1, returnGPR1);
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
				case bCdreturn:
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
						createDataProcCmpImm(code, armCmp, condAlways, src1Reg, low + k);
						createBranchImm(code, armB, condEQ, 0);
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
						createDataProcCmpImm(code, armCmp, condAlways, src1Reg, key);
						createBranchImm(code, armB, condEQ, 0);
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
				case tLong: case tFloat: case tDouble:
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
			if (dRegLongSlot >= 0) createLSWordImm(code, armStr, condAlways, dRegLong, stackPtr, localVarOffset + 4 * dRegLongSlot, 1, 1, 0);
			if (dRegSlot >= 0) {
				if ((res.type == tFloat) || (res.type == tDouble)); // createIrSrAd(code, ppcStfd, dReg, stackPtr, localVarOffset + 4 * dRegSlot);
				else createLSWordImm(code, armStr, condAlways, dReg, stackPtr, localVarOffset + 4 * dRegSlot, 1, 1, 0);
			}
		}
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

	private static void correctJmpAddr(int[] instructions, int count1, int count2) {
		instructions[count1] |= ((count2 - count1) << 2) & 0xffff;
	}

	// copy parameters for methods into parameter registers or onto stack
	private void copyParameters(Code32 code, SSAValue[] opds) {
		int offset = 0;
		for (int k = 0; k < nofGPR; k++) {srcGPR[k] = -1; srcGPRcount[k] = 0;}
		for (int k = 0; k < nofFPR; k++) {srcFPR[k] = -1; srcFPRcount[k] = 0;}

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
			for (int k = paramStartGPR; srcGPR[k] != -1; k++) StdStreams.vrb.print(srcGPR[k] + ","); 
			StdStreams.vrb.println();
			StdStreams.vrb.print("srcGPRcount = ");
			for (int n = paramStartGPR; srcGPR[n] != -1; n++) StdStreams.vrb.print(srcGPRcount[n] + ","); 
			StdStreams.vrb.println();
		}

		// count register usage
		int i = paramStartGPR;
		while (srcGPR[i] != -1) {
			if (srcGPR[i] <= topGPR) srcGPRcount[srcGPR[i]]++;
			i++;
		}
		i = paramStartFPR;
		while (srcFPR[i] != -1) {
			if (srcFPR[i] <= topFPR) srcFPRcount[srcFPR[i]]++;
			i++;
		}
		if (dbg) {
			StdStreams.vrb.print("srcGPR = ");
			for (i = paramStartGPR; srcGPR[i] != -1; i++) StdStreams.vrb.print(srcGPR[i] + ","); 
			StdStreams.vrb.println();
			StdStreams.vrb.print("srcGPRcount = ");
			for (i = paramStartGPR; srcGPR[i] != -1; i++) StdStreams.vrb.print(srcGPRcount[i] + ","); 
			StdStreams.vrb.println();
		}
		
		// handle move to itself
		i = paramStartGPR;
		while (srcGPR[i] != -1) {
			if (srcGPR[i] == i) srcGPRcount[i]--;
			i++;
		}
		i = paramStartFPR;
		while (srcFPR[i] != -1) {
			if (srcFPR[i] == i) srcFPRcount[i]--;
			i++;
		}
		if (dbg) {
			StdStreams.vrb.print("srcGPR = ");
			for (i = paramStartGPR; srcGPR[i] != -1; i++) StdStreams.vrb.print(srcGPR[i] + ","); 
			StdStreams.vrb.println();
			StdStreams.vrb.print("srcGPRcount = ");
			for (i = paramStartGPR; srcGPR[i] != -1; i++) StdStreams.vrb.print(srcGPRcount[i] + ","); 
			StdStreams.vrb.println();
		}

		// move registers 
		boolean done = false;
		while (!done) {
			i = paramStartGPR; done = true;
			while (srcGPR[i] != -1) {
				if (i > paramEndGPR) {	// copy to stack
					if (srcGPRcount[i] >= 0) { // check if not done yet
						if (dbg) StdStreams.vrb.println("\tGPR: parameter " + (i-paramStartGPR) + " from register " + srcGPR[i] + " to stack slot");
						if (srcGPR[i] >= 0x100) {	// copy from stack slot to stack (into parameter area)
							assert false;
//							createIrDrAd(code, ppcLwz, 0, stackPtr, localVarOffset + 4 * (srcGPR[i] - 0x100));
//							createIrSrAsimm(code, ppcStw, 0, stackPtr, paramOffset + offset);
						} else {
							assert false;
//							createIrSrAsimm(code, ppcStw, srcGPR[i], stackPtr, paramOffset + offset);
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
//							assert false;
//							createIrDrAd(code, ppcLwz, i, stackPtr, localVarOffset + 4 * (srcGPR[i] - 0x100));
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
		if (dbg) StdStreams.vrb.println();
		done = false;
		while (!done) {
			i = paramStartFPR; done = true;
			while (srcFPR[i] != -1) {
				if (i > paramEndFPR) {	// copy to stack
					if (srcFPRcount[i] >= 0) { // check if not done yet
						if (dbg) StdStreams.vrb.println("\tFPR: parameter " + (i-paramStartFPR) + " from register " + srcFPR[i] + " to stack slot");
						if (srcFPR[i] >= 0x100) {	// copy from stack slot to stack (into parameter area)
//							createIrDrAd(code, ppcLfd, 0, stackPtr, localVarOffset + 4 * (srcFPR[i] - 0x100));
//							createIrSrAd(code, ppcStfd, 0, stackPtr, paramOffset + offset);
						} else {
//							createIrSrAd(code, ppcStfd, srcFPR[i], stackPtr, paramOffset + offset);
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
//							createIrDrAd(code, ppcLfd, i, stackPtr, localVarOffset + 4 * (srcFPR[i] - 0x100));
						} else {
//							createIrDrB(code, ppcFmr, i, srcFPR[i]);
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
		if (dbg) StdStreams.vrb.println("resolve cycles");
		done = false;
		while (!done) {
			i = paramStartGPR; done = true;
			while (srcGPR[i] != -1) {
				int src = -1;	//TODO noch pruefen, was 0, see 10 lines below
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
			i = paramStartFPR; done = true;
			while (srcFPR[i] != -1) {
				int src = 0;
				if (srcFPRcount[i] == 1) {
					src = i;
//					createIrDrB(code, ppcFmr, 0, srcFPR[i]);
					srcFPRcount[srcFPR[i]]--;
					done = false;
				}
				boolean done1 = false;
				while (!done1) {
					int k = paramStartFPR; done1 = true;
					while (srcFPR[k] != -1) {
						if (srcFPRcount[k] == 0 && k != src) {
//							createIrDrB(code, ppcFmr, k, srcFPR[k]);
							srcFPRcount[k]--; srcFPRcount[srcFPR[k]]--; 
							done1 = false;
						}
						k++; 
					}
				}
				if (src != 0) {
//					createIrDrB(code, ppcFmr, src, 0);
					srcFPRcount[src]--;
				}
				i++;
			}
		}
		if (dbg) StdStreams.vrb.println("done");
	}

	// copy parameters for subroutines into registers r30/r31, r28/r29
	private void copyParametersSubroutine(int op0regLong, int op0reg, int op1regLong, int op1reg) {
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
//					createIrArSrB(ppcOr, i, srcGPR[i], srcGPR[i]);
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
//					createIrArSrB(ppcOr, 0, srcGPR[i], srcGPR[i]);
					srcGPRcount[srcGPR[i]]--;
					done = false;
				}
				boolean done1 = false;
				while (!done1) {
					int k = topGPR; done1 = true;
					while (srcGPR[k] != 0) {
						if (srcGPRcount[k] == 0 && k != src) {
//							createIrArSrB(ppcOr, k, srcGPR[k], srcGPR[k]);
							srcGPRcount[k]--; srcGPRcount[srcGPR[k]]--; 
							done1 = false;
						}
						k--; 
					}
				}
				if (src != 0) {
//					createIrArSrB(ppcOr, src, 0, 0);
					srcGPRcount[src]--;
				}
				i--;
			}
		}
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
		if (shiftAmount == 0) code.instructions[code.iCount] = (cond << 28) | armMov | (Rd << 12) | Rm;	// shifting with imm=0 is not valid
		else code.instructions[code.iCount] = (cond << 28) | op | (Rd << 12) | (shiftAmount << 7) | (shiftType << 5) | Rm;
		code.incInstructionNum();
	}

	// data processing with second operand in register, no result (Rd = 0), Rn = 1st op, Rm = 2nd op, op in bits 24 to 20, use for TST, TEQ, CMP, CMN
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
		
	// branch (immediate) (B, BL, BLX(imm)) 
	private void createBranchImm(Code32 code, int op, int cond, int imm24) {
		code.instructions[code.iCount] = (cond << 28) | op | (imm24 & 0xffffff);
		code.incInstructionNum();
	}
	
	// branch (register) (including BLX(reg), BX, BXJ)
	private void createBranchReg(Code32 code, int op, int cond, int Rm) {
		code.instructions[code.iCount] = (cond << 28) | op | (Rm << 0);
		code.incInstructionNum();
	}
	
	// Load/store word and unsigned byte	(LDR, LDRB, STR, STRB)	(LDRT / LDRBT / STRT / STRBT)
		// (LDR, LDRB, STR, STRB	:	(immediate))		(LDRT / LDRBT / STRT / STRBT	:	A1)
	private void createLSWordImm(Code32 code, int opCode, int cond, int Rt, int Rn, int imm12, int P, int U, int W) {
		if (opCode == armLdr || opCode == armLdrb || opCode == armStr || opCode == armStrb)
			code.instructions[code.iCount] = (cond << 28) | opCode | (Rt << 12) | (Rn << 16) | (imm12 << 0) | (P << 24) | (U << 23) | (W << 21);
		else	// extra load / store
			code.instructions[code.iCount] = (cond << 28) | opCode | (1 << 22) | (Rt << 12) | (Rn << 16) | (imm12 << 0) | (P << 24) | (U << 23) | (W << 21);	
		code.incInstructionNum();
	}

	// ...(LDR, LDRB	:	(literal))
	private void createLSWordLit(Code32 code, int opCode, int cond, int Rt, int imm12, int P, int U, int W) {
		code.instructions[code.iCount] = (cond << 28) | opCode | (Rt << 12) | (0xf << 16) | (imm12 << 0) | (P << 24) | (U << 23) | (W << 21);
		code.incInstructionNum();
	}
	// ...(LDR, LDRB, STR, STRB	:	(register))		(LDRT / LDRBT / STRT / STRBT	:	A2)
	private void createLSWordReg(Code32 code, int opCode, int cond, int Rt, int Rn, int Rm, int shiftType, int shiftAmount, int P, int U, int W) {
		code.instructions[code.iCount] = (cond << 28) | opCode | (Rt << 12) | (Rn << 16) | (Rm << 0) | (shiftType << 5) | (shiftAmount << 7) | (P << 24) | (U << 23) | (W << 21) | (1 << 25);
		code.incInstructionNum();
	}
	
	// ...(LDR, LDRB, STR, STRB	:	(register))		(LDRT / LDRBT / STRT / STRBT	:	A2)
	private void createLSWordRegA(Code32 code, int opCode, int cond, int Rt, int Rn, int Rm, int shiftType, int shiftAmount, int P, int U, int W) {
		code.instructions[code.iCount] = (cond << 28) | opCode | (Rt << 12) | (Rn << 16) | (Rm << 0) | (shiftType << 5) | (shiftAmount << 7) | (P << 24) | (U << 23) | (W << 21);
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
	
	// load/store extension registers (VLDR, VSTR)
	private void createLSExtReg(Code32 code, int opCode, int cond, int Vd, int Rn, int imm, boolean single) {
		code.instructions[code.iCount] = (cond << 28) | opCode | (Rn << 16) | (imm & 0xff) | ((imm>0)?(1<<23):0);
		if (single) code.instructions[code.iCount] |= (((Vd>>1)&0xf) << 12) | ((Vd&1) << 22);
		else code.instructions[code.iCount] |= (1 << 8) | ((Vd&0xf) << 12) | ((Vd>>4) << 22);
		code.incInstructionNum();
	}

	// floating point data processing (VADD)
	private void createFPdataProc(Code32 code, int opCode, int cond, int Vd, int Vn, int Vm, boolean single) {
		code.instructions[code.iCount] = (cond << 28) | opCode;
		if (single) code.instructions[code.iCount] |= (((Vd>>1)&0xf) << 12) | ((Vd&1) << 22) | (((Vn>>1)&0xf) << 16) | ((Vn&1) << 7) | ((Vm>>1)&0xf) | ((Vm&1) << 5);
		else code.instructions[code.iCount] |= (1 << 8) | ((Vd&0xf) << 12) | ((Vd>>4) << 22) | ((Vn&0xf) << 16) | ((Vn>>4) << 7) | (Vm&0xf) | ((Vm>>4) << 5);
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
	 * inserts a load register literal instruction
	 * the address of a class constant is taken relative to the PC and will later be fixed
	 */
	private void loadConstantFromPoolAndFixup(Code32 code, int reg, Item item) {
		if (code.lastFixup < 0 || code.lastFixup > 4096) {ErrorReporter.reporter.error(602); return;}
		createLSWordLit(code, armLdr, condAlways, reg, code.lastFixup, 1, 1, 0);
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

	/*
	 * reads or writes the content of a class variable
	 * both instructions will later be fixed
	 */
	private void loadStoreVarAndFixup(Code32 code, int opCode, int reg, Item item) {
		if (code.lastFixup < 0 || code.lastFixup > 4096) {ErrorReporter.reporter.error(602); return;}
//		createLSWordLit(code, armLdr, condAlways, LR, code.lastFixup, 1, 1, 0);
		createMovw(code, armMovw, condAlways, LR, code.lastFixup);
		createMovw(code, armMovt, condAlways, LR, 0);
		createLSWordImm(code, opCode, condAlways, reg, LR, 0, 1, 1, 0);
		code.lastFixup = code.iCount - 3;
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
	 * loads the address of a method or a class field
	 * both instructions will later be fixed
	 */
	private void loadConstantAndFixup(Code32 code, int reg, Item item) {
		if (code.lastFixup < 0 || code.lastFixup > 4096) {ErrorReporter.reporter.error(602); return;}
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
	
//	/*
//	 * writes the content of a class variable
//	 * both instructions will later be fixed
//	 */
//	private void storeVarAndFixup(Code32 code, int opCode, int srcReg, Item item) {
//		if (code.lastFixup < 0 || code.lastFixup > 4096) {ErrorReporter.reporter.error(602); return;}
//		createLSWordLit(code, armLdr, condAlways, LR, code.lastFixup, 1, 1, 0);
//		createLSWordImm(code, opCode, condAlways, srcReg, LR, 0, 1, 1, 0);
//		code.lastFixup = code.iCount - 2;
//		code.fixups[code.fCount] = item;
//		code.fCount++;
//		int len = code.fixups.length;
//		if (code.fCount == len) {
//			Item[] newFixups = new Item[2 * len];
//			for (int k = 0; k < len; k++)
//				newFixups[k] = code.fixups[k];
//			code.fixups = newFixups;
//		}		
//	}
	
	/*
	 * inserts a BL instruction
	 * it's offset will later be fixed
	 */
	private void insertBLAndFixup(Code32 code, Item item) {
		if (code.lastFixup < 0 || code.lastFixup >= 4096) {ErrorReporter.reporter.error(602); return;}
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
				else StdStreams.vrb.println("\t" + item.name + " at 0x" + Integer.toHexString(addr) + " currInstr=" + currInstr);
			}
			int[] instrs = code.instructions;
			int nextInstr = instrs[currInstr] & 0xfff;
			if (item instanceof Method) {
				if (((instrs[currInstr] >> 24) & 0xe) == 0xa) { // must be a branch to a method
					int branchOffset = ((addr - code.ssa.cfg.method.address) >> 2) - currInstr - 2;	// -2: account for pipelining
					assert (branchOffset < 0x1000000) && (branchOffset > 0xff000000);
					if ((branchOffset >= 0x1000000) || (branchOffset <= 0xff000000)) {ErrorReporter.reporter.error(650); return;}
					instrs[currInstr] = (instrs[currInstr] & 0xff000000) | (branchOffset & 0xffffff);
				} else {
					int val = item.address & 0xffff;
					instrs[currInstr] = (instrs[currInstr] & 0xfff0f000) | ((val & 0xf000) << 4) | (val & 0xfff);
					val = (item.address >> 16) & 0xffff;
					instrs[currInstr+1] = (instrs[currInstr+1] & 0xfff0f000) | ((val & 0xf000) << 4) | (val & 0xfff);				
				}
			} else {	// must be a load / store instruction
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
//			SSAInstruction ssaInstr = code.ssa.searchBca(code.instructions[currInstr]);	
//			assert ssaInstr != null;
//			code.instructions[currInstr++] = code.ssa.cfg.method.address + ssaInstr.machineCodeOffset * 4;	// start
//			
//			ssaInstr = code.ssa.searchBca(code.instructions[currInstr]);	
//			assert ssaInstr != null;
//			code.instructions[currInstr++] = code.ssa.cfg.method.address + ssaInstr.machineCodeOffset * 4;	// end
//			
//			ExceptionTabEntry[] tab = code.ssa.cfg.method.exceptionTab;
//			assert tab != null;
//			ExceptionTabEntry entry = tab[count];
//			assert entry != null;
//			if (entry.catchType != null) code.instructions[currInstr++] = entry.catchType.address;	// type 
//			else code.instructions[currInstr++] = 0;	// finally 
//			
//			ssaInstr = code.ssa.searchBca(code.instructions[currInstr] + 1);	// add 1, as first store is ommitted	
//			assert ssaInstr != null;
//			code.instructions[currInstr++] = code.ssa.cfg.method.address + ssaInstr.machineCodeOffset * 4;	// handler
			currInstr++; //muss wieder weg!!!!!!!!!!!!!!!!!!!!!
			count++;
		}
		// fix addresses of variable and constant segment
		currInstr++;
		Class clazz = code.ssa.cfg.method.owner;
		code.instructions[currInstr] = clazz.varSegment.address + clazz.varOffset;
	}

	private void insertProlog(Code32 code) {
		code.iCount = 0;
		
		int regList = 1 << LR;
		if (nofNonVolGPR > 0) 
			for (int i = 0; i < nofNonVolGPR; i++) regList += 1 << (topGPR - i); 
		createBlockDataTransfer(code, armPush, condAlways, regList);
		// enFloatsInExc could be true, even if this is no exception method
		// such a case arises when this method is called from within an exception method
//		if (enFloatsInExc) {
//			createIrD(code, ppcMfmsr, 0);
//			createIrArSuimm(code, ppcOri, 0, 0, 0x2000);
//			createIrS(code, ppcMtmsr, 0);
//			createIrS(code, ppcIsync, 0);	// must context synchronize after setting of FP bit
//		}
		int offset = FPRoffset;
//		if (nofNonVolFPR > 0) {
//			for (int i = 0; i < nofNonVolFPR; i++) {
//				createIrSrAd(code, ppcStfd, topFPR-i, stackPtr, offset);
//				offset += 8;
//			}
//		}
//		if (enFloatsInExc) {
//			for (int i = 0; i < nonVolStartFPR; i++) {	// save volatiles
//				createIrSrAd(code, ppcStfd, i, stackPtr, offset);
//				offset += 8;
//			}
//			createIrD(code, ppcMffs, 0);
//			createIrSrAd(code, ppcStfd, 0, stackPtr, offset);
//		}
		createDataProcImm(code, armSub, condAlways, stackPtr, stackPtr, RegAllocator.maxLocVarStackSlots * 4 );
		if (dbg) {
			StdStreams.vrb.print("moveGPRsrc = ");
			for (int i = 0; moveGPRsrc[i] != 0; i++) StdStreams.vrb.print(moveGPRsrc[i] + ","); 
			StdStreams.vrb.println();
			StdStreams.vrb.print("moveGPRdst = ");
			for (int i = 0; moveGPRdst[i] != 0; i++) StdStreams.vrb.print(moveGPRdst[i] + ","); 
			StdStreams.vrb.println();
			StdStreams.vrb.print("moveFPRsrc = ");
			for (int i = 0; moveFPRsrc[i] != 0; i++) StdStreams.vrb.print(moveFPRsrc[i] + ","); 
			StdStreams.vrb.println();
			StdStreams.vrb.print("moveFPRdst = ");
			for (int i = 0; moveFPRdst[i] != 0; i++) StdStreams.vrb.print(moveFPRdst[i] + ","); 
			StdStreams.vrb.println();
		}
		offset = 0;
		for (int i = 0; i < nofMoveGPR; i++) {
			if (moveGPRsrc[i]+paramStartGPR <= paramEndGPR) {// copy from parameter register
				if (dbg) StdStreams.vrb.println("Prolog: copy parameter " + moveGPRsrc[i] + " into GPR" + moveGPRdst[i]);
				if (moveGPRdst[i] < 0x100)
					createDataProcMovReg(code, armMov, condAlways, moveGPRdst[i], moveGPRsrc[i]+paramStartGPR, noShift, 0);
//				else 	// copy to stack slot (locals)
//					assert false;
//					createIrSrAd(code, ppcStw, moveGPRsrc[i]+paramStartGPR, stackPtr, localVarOffset + 4 * (moveGPRdst[i] - 0x100));
			} else { // copy from stack slot (parameters)
				if (dbg) StdStreams.vrb.println("Prolog: copy parameter " + moveGPRsrc[i] + " from stack slot into GPR" + moveGPRdst[i]);
				if (moveGPRdst[i] < 0x100)
					createLSWordImm(code,armLdr, condAlways, moveGPRdst[i], stackPtr, stackSize + paramOffset + offset, 0, 0, 0);
				else { 	// copy to stack slot (locals)
					assert false;
//					createIrDrAd(code, ppcLwz, 0, stackPtr, stackSize + paramOffset + offset);
//					createIrSrAd(code, ppcStw, 0, stackPtr, localVarOffset + 4 * (moveGPRdst[i] - 0x100));
				}	
				offset += 4;
			}
		}
//		for (int i = 0; i < nofMoveFPR; i++) {
//			if (moveFPRsrc[i]+paramStartFPR <= paramEndFPR) {// copy from parameter register
//				if (dbg) StdStreams.vrb.println("Prolog: copy parameter " + moveFPRsrc[i] + " into FPR" + moveFPRdst[i]);
//				if (moveFPRdst[i] < 0x100)
//					createIrDrB(code, ppcFmr, moveFPRdst[i], moveFPRsrc[i]+paramStartFPR);
//				else	// copy to stack slot (locals)
//					createIrSrAd(code, ppcStfd, moveFPRsrc[i]+paramStartFPR, stackPtr, localVarOffset + 4 * (moveFPRdst[i] - 0x100));
//			} else { // copy from stack slot (parameters)
//				if (dbg) StdStreams.vrb.println("Prolog: copy parameter " + moveFPRsrc[i] + " from stack slot into FPR" + moveFPRdst[i]);
//				if (moveFPRdst[i] < 0x100)
//					createIrDrAd(code, ppcLfd, moveFPRdst[i], stackPtr, stackSize + paramOffset + offset);
//				else {
//					createIrDrAd(code, ppcLfd, 0, stackPtr, stackSize + paramOffset + offset);
//					createIrSrAd(code, ppcStfd, 0, stackPtr, localVarOffset + 4 * (moveFPRdst[i] - 0x100));
//				}
//				offset += 8;
//			}
//		}
	}

	private void insertEpilog(Code32 code, int stackSize) {
		int epilogStart = code.iCount;
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
		createDataProcImm(code, armAdd, condAlways, stackPtr, stackPtr, RegAllocator.maxLocVarStackSlots * 4 );
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

	private void insertPrologException() {
//		iCount = 0;
//		createIrSrAsimm(ppcStwu, stackPtr, stackPtr, -stackSize);
//		createIrSrAsimm(ppcStw, 0, stackPtr, GPRoffset);
//		createIrSspr(ppcMfspr, SRR0, 0);
//		createIrSrAsimm(ppcStw, 0, stackPtr, SRR0offset);
//		createIrSspr(ppcMfspr, SRR1, 0);
//		createIrSrAsimm(ppcStw, 0, stackPtr, SRR1offset);
//		createIrSspr(ppcMtspr, EID, 0);
//		createIrSspr(ppcMfspr, LR, 0);
//		createIrSrAsimm(ppcStw, 0, stackPtr, LRoffset);
//		createIrSspr(ppcMfspr, XER, 0);
//		createIrSrAsimm(ppcStw, 0, stackPtr, XERoffset);
//		createIrSspr(ppcMfspr, CTR, 0);
//		createIrSrAsimm(ppcStw, 0, stackPtr, CTRoffset);
//		createIrD(ppcMfcr, 0);
//		createIrSrAsimm(ppcStw, 0, stackPtr, CRoffset);
//		createIrSrAd(ppcStmw, 2, stackPtr, GPRoffset + 8);
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

	private void insertEpilogException(int stackSize) {
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
//		createIrDrAd(ppcLmw, 2, stackPtr, GPRoffset + 8);
//		createIrDrAd(ppcLwz, 0, stackPtr, CRoffset);
//		createICRMrS(ppcMtcrf, 0xff, 0);
//		createIrDrAd(ppcLwz, 0, stackPtr, CTRoffset);
//		createIrSspr(ppcMtspr, CTR, 0);
//		createIrDrAd(ppcLwz, 0, stackPtr, XERoffset);
//		createIrSspr(ppcMtspr, XER, 0);
//		createIrDrAd(ppcLwz, 0, stackPtr, LRoffset);
//		createIrSspr(ppcMtspr, LR, 0);
//		createIrDrAd(ppcLwz, 0, stackPtr, SRR1offset);
//		createIrSspr(ppcMtspr, SRR1, 0);
//		createIrDrAd(ppcLwz, 0, stackPtr, SRR0offset);
//		createIrSspr(ppcMtspr, SRR0, 0);
//		createIrDrAd(ppcLwz, 0, stackPtr, GPRoffset);
////		createIrDrAsimm(ppcAddi, stackPtr, stackPtr, stackSize);
//		createIrfi(ppcRfi);
	}

	public void generateCompSpecSubroutines() {
		Method m = Method.getCompSpecSubroutine("longToDouble");
		// long is passed in r30/r31, r29 can be used for general purposes
		// faux1 and faux2 are used as general purpose FPR's, result is passed in f0 
		if (m != null) { 
			Code32 code = new Code32(null);
			m.machineCode = code;
		}
		
		m = Method.getCompSpecSubroutine("remLong");
		if (m != null) { 
			Code32 code = new Code32(null);
			m.machineCode = code;
		}
	
		m = Method.getCompSpecSubroutine("doubleToLong");
		if (m != null) { 
			Code32 code = new Code32(null);
			m.machineCode = code;
		}
	
		m = Method.getCompSpecSubroutine("divLong");
		if (m != null) { 
			Code32 code = new Code32(null);
			m.machineCode = code;
		}
	
		m = Method.getCompSpecSubroutine("handleException");
		if (m != null) { 
			Code32 code = new Code32(null);
			m.machineCode = code;
		}
	}

}


