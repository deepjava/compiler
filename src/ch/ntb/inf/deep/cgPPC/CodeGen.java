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

package ch.ntb.inf.deep.cgPPC;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.classItems.*;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.Linker32;
import ch.ntb.inf.deep.ssa.*;
import ch.ntb.inf.deep.ssa.instruction.*;
import ch.ntb.inf.deep.strings.HString;

public class CodeGen implements SSAInstructionOpcs, SSAInstructionMnemonics, SSAValueType, InstructionOpcs, Registers, ICjvmInstructionOpcs, ICclassFileConsts, ICdescAndTypeConsts {
	private static final boolean dbg = false;

	static final int maxNofParam = 32;
	private static final int defaultNofInstr = 32;
	private static final int defaultNofFixup = 8;
	private static final int arrayLenOffset = 6;	
	private static final int tempStorageSize = 48;	// 1 FPR(temp), 4 GPRs 
	
	private static int objectSize, stringSize;
	private static StdConstant int2floatConst1 = null;	// 2^52+2^31, for int -> float conversions
	private static StdConstant int2floatConst2 = null;	// 2^32, for long -> float conversions
	private static StdConstant int2floatConst3 = null;	// 2^52, for long -> float conversions
	
	static int idGET1, idGET2, idGET4, idGET8;
	static int idPUT1, idPUT2, idPUT4, idPUT8;
	static int idBIT, idASM, idHALT, idADR_OF_METHOD, idREF;
	static int idENABLE_FLOATS;
	static int idGETGPR, idGETFPR, idGETSPR;
	static int idPUTGPR, idPUTFPR, idPUTSPR;
	static int idDoubleToBits, idBitsToDouble;
	static int idFloatToBits, idBitsToFloat;
	
	private static Method stringNewstringMethod;
	private static Method heapNewstringMethod;
	private static Method strInitC;
	private static Method strInitCII;
	private static Method strAllocC;
	private static Method strAllocCII;

	private static int LRoffset;	
	private static int XERoffset;	
	private static int CRoffset;	
	private static int CTRoffset;	
	private static int SRR0offset;	
	private static int SRR1offset;	
	private static int paramOffset;
	private static int GPRoffset;	
	private static int FPRoffset;	
//	private static int localVarOffset;
	private static int tempStorageOffset;	
	private static int stackSize;
	static boolean tempStorage;
	static boolean enFloatsInExc;

	// nof parameter for a method, set by SSA, includes "this", long and doubles count as 2 parameters
	private static int nofParam;	
	// nofParamGPR + nofParamFPR = nofParam, set by last exit set of last node
	private static int nofParamGPR, nofParamFPR;	 
	// maximum nof registers used by this method
	static int nofNonVolGPR, nofNonVolFPR, nofVolGPR, nofVolFPR;
	// gives required stack space for parameters of this method if not enough registers
	static int recParamSlotsOnStack;
	// gives required stack space for parameters of any call in this method if not enough registers
	static int callParamSlotsOnStack;
	// type of parameter, set by SSA, includes "this", long and doubles count as 2 parameters
	static int[] paramType = new int[maxNofParam];
	// register type of parameter, long and doubles count as 2 parameters
	static boolean[] paramHasNonVolReg = new boolean[maxNofParam];
	// register of parameter, long and doubles count as 2 parameters
	static int[] paramRegNr = new int[maxNofParam];
	// last instruction where parameters is used
	static int[] paramRegEnd = new int[maxNofParam];
	
	// information about into which registers parameters of this method go 
	private static int nofMoveGPR, nofMoveFPR;
	private static int[] moveGPRsrc = new int[maxNofParam];
	private static int[] moveGPRdst = new int[maxNofParam];
	private static int[] moveFPRsrc = new int[maxNofParam];
	private static int[] moveFPRdst = new int[maxNofParam];
	
	// information about the src registers for parameters of a call to a method within this method
	private static int[] srcGPR = new int[nofGPR];
	private static int[] srcFPR = new int[nofFPR];
	private static int[] srcGPRcount = new int[nofGPR];
	private static int[] srcFPRcount = new int[nofFPR];
	
	private static SSAValue[] lastExitSet;
	private static boolean newString;
	
	public SSA ssa;	// reference to the SSA of a method
	public int[] instructions;	// contains machine instructions for the ssa of a method
	public int iCount;	// nof instructions for this method
	int excTabCount;	// start of exception information in instruction array
	
	Item[] fixups;	// contains all references whose address has to be fixed by the linker
	int fCount;	//nof fixups
	int lastFixup;	// instr number where the last fixup is found

	public CodeGen(SSA ssa) {
		this.ssa = ssa;
		instructions = new int[defaultNofInstr];
		fixups = new Item[defaultNofFixup];
		nofParamGPR = 0; nofParamFPR = 0;
		nofNonVolGPR = 0; nofNonVolFPR = 0;
		nofVolGPR = 0; nofVolFPR = 0;
		nofMoveGPR = 0; nofMoveFPR = 0;
		tempStorage = false;
		enFloatsInExc = false;
		recParamSlotsOnStack = 0; callParamSlotsOnStack = 0;
		if (dbg) StdStreams.vrb.println("generate code for " + ssa.cfg.method.owner.name + "." + ssa.cfg.method.name);
		for (int i = 0; i < maxNofParam; i++) {
			paramType[i] = tVoid;
			paramRegNr[i] = -1;
			paramRegEnd[i] = -1;
		}

		// make local copy
		int maxStackSlots = ssa.cfg.method.maxStackSlots;
		int i = maxStackSlots;
		while ((i < ssa.isParam.length) && ssa.isParam[i]) {
			int type = ssa.paramType[i] & ~(1<<ssaTaFitIntoInt);
			paramType[i - maxStackSlots] = type;
			paramHasNonVolReg[i - maxStackSlots] = false;
			if (type == tLong || type == tDouble) i++;
			i++;
		}
		nofParam = i - maxStackSlots;
		if (nofParam > maxNofParam) {ErrorReporter.reporter.error(601); return;}
		if (dbg) StdStreams.vrb.println("nofParam = " + nofParam);
		
		if (dbg) StdStreams.vrb.println("build intervals");
//		StdStreams.vrb.print(ssa.cfg.toString());
//		StdStreams.vrb.println(ssa.toString());
		RegAllocator.buildIntervals(ssa);
		
		if (dbg) StdStreams.vrb.println("assign registers to parameters");
		SSANode b = (SSANode) ssa.cfg.rootNode;
		while (b.next != null) {
			b = (SSANode) b.next;
		}	
		lastExitSet = b.exitSet;
		// determine, which parameters go into which register
		parseExitSet(lastExitSet, maxStackSlots);
		if (dbg) {
			StdStreams.vrb.print("parameter go into register: ");
			for (int n = 0; paramRegNr[n] != -1; n++) StdStreams.vrb.print(paramRegNr[n] + "  "); 
			StdStreams.vrb.println();
		}
		
		if (dbg) StdStreams.vrb.println("allocate registers");
		RegAllocator.assignRegisters(this);
		
//		StdStreams.vrb.print(ssa.cfg.toString());
		if (dbg) {
			StdStreams.vrb.println(RegAllocator.joinsToString());
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
		if ((ssa.cfg.method.accAndPropFlags & (1 << dpfExcHnd)) != 0) {	// exception
			if (ssa.cfg.method.name == HString.getRegisteredHString("reset")) {	// reset has no prolog
			} else if (ssa.cfg.method.name == HString.getRegisteredHString("programExc")) {
				iCount = 0;
				createIrSrAsimm(ppcStwu, stackPtr, stackPtr, -24);
//				createIrSspr(ppcMfspr, SRR0, 0);
//				createIrSrAsimm(ppcStw, 0, stackPtr, 16);
				createIrSspr(ppcMtspr, EID, 0);	// TODO, was passiert wenn gleich wieder eine exception? muss rein, sonst absturz wenn debugger exception
				createIrSrAd(ppcStmw, 28, stackPtr, 4);
				createIrArSrB(ppcOr, 31, paramStartGPR, paramStartGPR);	// copy exception into nonvolatile
			} else {
				stackSize = calcStackSizeException();
				insertPrologException();
			}
		} else {
			stackSize = calcStackSize();
			insertProlog();	// builds stack frame and copies parameters
		}
		
		SSANode node = (SSANode)ssa.cfg.rootNode;
		while (node != null) {
			node.codeStartIndex = iCount;
			translateSSA(node);
			node.codeEndIndex = iCount-1;
			node = (SSANode) node.next;
		}
		node = (SSANode)ssa.cfg.rootNode;
		while (node != null) {	// resolve local branch targets
			if (node.nofInstr > 0) {
				if ((node.instructions[node.nofInstr-1].ssaOpcode == sCbranch) || (node.instructions[node.nofInstr-1].ssaOpcode == sCswitch)) {
					int code = this.instructions[node.codeEndIndex];
					CFGNode[] successors = node.successors;
					switch (code & 0xfc000000) {
					case ppcB:			
						if ((code & 0xffff) != 0) {	// switch
							int nofCases = (code & 0xffff) >> 2;
							int k;
							for (k = 0; k < nofCases; k++) {
								int branchOffset = ((SSANode)successors[k]).codeStartIndex - (node.codeEndIndex+1-(nofCases-k)*2);
								this.instructions[node.codeEndIndex+1-(nofCases-k)*2] |= (branchOffset << 2) & 0x3ffffff;
							}
							int branchOffset = ((SSANode)successors[k]).codeStartIndex - node.codeEndIndex;
							this.instructions[node.codeEndIndex] &= 0xfc000000;
							this.instructions[node.codeEndIndex] |= (branchOffset << 2) & 0x3ffffff;
						} else {
							int branchOffset = ((SSANode)successors[0]).codeStartIndex - node.codeEndIndex;
							this.instructions[node.codeEndIndex] |= (branchOffset << 2) & 0x3ffffff;
						}
						break;
					case ppcBc:
						int branchOffset = ((SSANode)successors[1]).codeStartIndex - node.codeEndIndex;
						this.instructions[node.codeEndIndex] |= (branchOffset << 2) & 0xffff;
						break;
					}
				} else if (node.instructions[node.nofInstr-1].ssaOpcode == sCreturn) {
					if (node.next != null) {
						int branchOffset = iCount - node.codeEndIndex;
						this.instructions[node.codeEndIndex] |= (branchOffset << 2) & 0x3ffffff;
					}
				}
			}
			node = (SSANode) node.next;
		}
		if ((ssa.cfg.method.accAndPropFlags & (1 << dpfExcHnd)) != 0) {	// exception
			if (ssa.cfg.method.name == HString.getRegisteredHString("reset")) {	// reset needs no epilog
			} else if (ssa.cfg.method.name == HString.getRegisteredHString("programExc")) {	// TODO fertig machen
				Method m = Method.getCompSpecSubroutine("handleException");
				assert m != null;
				loadConstantAndFixup(31, m);
				createIrSspr(ppcMtspr, LR, 31);
				createIrDrAd(ppcLmw, 28, stackPtr, 4);
				createIrDrAsimm(ppcAddi, stackPtr, stackPtr, 24);
				createIBOBILK(ppcBclr, BOalways, 0, false);
			} else {
				insertEpilogException(stackSize);
			}
		} else {
			insertEpilog(stackSize);
		}
		if (dbg) {StdStreams.vrb.print(ssa.toString()); StdStreams.vrb.print(toString());}
	}

	public CodeGen() {}

	private static void parseExitSet(SSAValue[] exitSet, int maxStackSlots) {
		nofParamGPR = 0; nofParamFPR = 0;
		nofMoveGPR = 0; nofMoveFPR = 0;
		if(dbg) StdStreams.vrb.print("[");
		for (int i = 0; i < nofParam; i++) {
			int type = paramType[i];
			if(dbg) StdStreams.vrb.print("(" + svNames[type] + ")");
			if (type == tLong) {
				if (exitSet[i+maxStackSlots] != null) {	// if null -> parameter is never used
					if(dbg) StdStreams.vrb.print("r");
					if (paramHasNonVolReg[i]) {
						int reg = RegAllocator.reserveReg(gpr, true);
						int regLong = RegAllocator.reserveReg(gpr, true);
						moveGPRsrc[nofMoveGPR] = nofParamGPR;
						moveGPRsrc[nofMoveGPR+1] = nofParamGPR+1;
						moveGPRdst[nofMoveGPR++] = reg;
						moveGPRdst[nofMoveGPR++] = regLong;
						paramRegNr[i] = reg;
						paramRegNr[i+1] = regLong;
						if(dbg) StdStreams.vrb.print(reg + ",r" + regLong);
					} else {
						int reg = paramStartGPR + nofParamGPR;
						if (reg <= paramEndGPR) RegAllocator.reserveReg(gpr, reg);
						else {
							reg = RegAllocator.reserveReg(gpr, false);
							moveGPRsrc[nofMoveGPR] = nofParamGPR;
							moveGPRdst[nofMoveGPR++] = reg;
						}
						int regLong = paramStartGPR + nofParamGPR + 1;
						if (regLong <= paramEndGPR) RegAllocator.reserveReg(gpr, regLong);
						else {
							regLong = RegAllocator.reserveReg(gpr, false);
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
						int reg = RegAllocator.reserveReg(fpr, true);
						moveFPRsrc[nofMoveFPR] = nofParamFPR;
						moveFPRdst[nofMoveFPR++] = reg;
						paramRegNr[i] = reg;
						if(dbg) StdStreams.vrb.print(reg);
					} else {
						int reg = paramStartFPR + nofParamFPR;
						if (reg <= paramEndFPR) RegAllocator.reserveReg(fpr, reg);
						else {
							reg = RegAllocator.reserveReg(fpr, false);
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
						int reg = RegAllocator.reserveReg(gpr, true);
						moveGPRsrc[nofMoveGPR] = nofParamGPR;
						moveGPRdst[nofMoveGPR++] = reg;
						paramRegNr[i] = reg;
						if(dbg) StdStreams.vrb.print(reg);
					} else {
						int reg = paramStartGPR + nofParamGPR;
						if (reg <= paramEndGPR) RegAllocator.reserveReg(gpr, reg);
						else {
							reg = RegAllocator.reserveReg(gpr, false);
							moveGPRsrc[nofMoveGPR] = nofParamGPR;
							moveGPRdst[nofMoveGPR++] = reg;
						}
						paramRegNr[i] = reg;
						if(dbg) StdStreams.vrb.print(reg);
					}
				}
				nofParamGPR++;	// even if the parameter is not used, the calling method
				// assigns a register and we have to do here the same
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
		int size = 8 + callParamSlotsOnStack * 4 + nofNonVolGPR * 4 + nofNonVolFPR * 8 + (tempStorage? tempStorageSize : 0);
		if (enFloatsInExc) size += nonVolStartFPR * 8 + 8;	// save volatile FPR's and FPSCR
		int padding = (16 - (size % 16)) % 16;
		size = size + padding;
		LRoffset = size - 4;
		GPRoffset = LRoffset - nofNonVolGPR * 4;
		FPRoffset = GPRoffset - nofNonVolFPR * 8;
		if (enFloatsInExc) FPRoffset -= nonVolStartFPR * 8 + 8;
		tempStorageOffset = FPRoffset - tempStorageSize;
		paramOffset = 4;
		return size;
	}

	private static int calcStackSizeException() {
		int size = 28 + nofGPR * 4 + (tempStorage? tempStorageSize : 0);
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
		tempStorageOffset = FPRoffset - tempStorageSize;
		paramOffset = 4;
		return size;
	}

	private void translateSSA (SSANode node) {
		SSAValue[] opds;
		int stringReg = 0;
		Item stringCharRef = null;

		for (int i = 0; i < node.nofInstr; i++) {
			SSAInstruction instr = node.instructions[i];
			SSAValue res = instr.result;
			instr.machineCodeOffset = iCount;
			if (node.isCatch && i == 0 && node.loadLocalExc > -1) {	
				if (dbg) StdStreams.vrb.println("enter move register intruction for local 'exception' in catch clause: from R" + paramStartGPR + " to R" + node.instructions[node.loadLocalExc].result.reg);
				createIrArSrB(ppcOr, node.instructions[node.loadLocalExc].result.reg, paramStartGPR, paramStartGPR);
			}
			
			
			if (dbg) StdStreams.vrb.println("ssa opcode at " + instr.result.n + ": " + SSAInstructionMnemonics.scMnemonics[instr.ssaOpcode] + ", iCount=" + iCount);
			switch (instr.ssaOpcode) { 
			case sCloadConst: {
				int dReg = res.reg;
				if (dReg >= 0) {	// else immediate opd
					switch (res.type & ~(1<<ssaTaFitIntoInt)) {
					case tByte: case tShort: case tInteger:
						int immVal = ((StdConstant)res.constant).valueH;
						loadConstant(dReg, immVal);
					break;
					case tLong:	
						StdConstant constant = (StdConstant)res.constant;
						long immValLong = ((long)(constant.valueH)<<32) | (constant.valueL&0xFFFFFFFFL);
						loadConstant(res.regLong, (int)(immValLong >> 32));
						loadConstant(dReg, (int)immValLong);
						break;	
					case tFloat:	// load from const pool
						constant = (StdConstant)res.constant;
						if (constant.valueH == 0) {	// 0.0 must be loaded directly as it's not in the cp
							createIrDrAsimm(ppcAddi, res.regGPR1, 0, 0);
							createIrSrAd(ppcStw, res.regGPR1, stackPtr, tempStorageOffset);
							createIrDrAd(ppcLfs, res.reg, stackPtr, tempStorageOffset);
						} else if (constant.valueH == 0x3f800000) {	// 1.0
							createIrDrAsimm(ppcAddis, res.regGPR1, 0, 0x3f80);
							createIrSrAd(ppcStw, res.regGPR1, stackPtr, tempStorageOffset);
							createIrDrAd(ppcLfs, res.reg, stackPtr, tempStorageOffset);
						} else if (constant.valueH == 0x40000000) {	// 2.0
							createIrDrAsimm(ppcAddis, res.regGPR1, 0, 0x4000);
							createIrSrAd(ppcStw, res.regGPR1, stackPtr, tempStorageOffset);
							createIrDrAd(ppcLfs, res.reg, stackPtr, tempStorageOffset);
						} else {
							loadConstantAndFixup(res.regGPR1, constant);
							createIrDrAd(ppcLfs, res.reg, res.regGPR1, 0);
						}
						break;
					case tDouble:
						constant = (StdConstant)res.constant;
						if (constant.valueH == 0) {	// 0.0 must be loaded directly as it's not in the cp
							createIrDrAsimm(ppcAddi, res.regGPR1, 0, 0);
							createIrSrAd(ppcStw, res.regGPR1, stackPtr, tempStorageOffset);
							createIrSrAd(ppcStw, res.regGPR1, stackPtr, tempStorageOffset+4);
							createIrDrAd(ppcLfd, res.reg, stackPtr, tempStorageOffset);
						} else if (constant.valueH == 0x3ff00000) {	// 1.0{
							createIrDrAsimm(ppcAddis, res.regGPR1, 0, 0x3ff0);
							createIrSrAd(ppcStw, res.regGPR1, stackPtr, tempStorageOffset);
							createIrDrAsimm(ppcAddis, res.regGPR1, 0, 0);
							createIrSrAd(ppcStw, res.regGPR1, stackPtr, tempStorageOffset+4);
							createIrDrAd(ppcLfd, res.reg, stackPtr, tempStorageOffset);
						} else {
							loadConstantAndFixup(res.regGPR1, constant);
							createIrDrAd(ppcLfd, res.reg, res.regGPR1, 0);
						}
						break;
					case tRef: case tAbyte: case tAshort: case tAchar: case tAinteger:
					case tAlong: case tAfloat: case tAdouble: case tAboolean: case tAref:
						if (res.constant == null) {// object = null
							loadConstant(dReg, 0);
						} else if ((ssa.cfg.method.owner.accAndPropFlags & (1<<apfEnum)) != 0 && ssa.cfg.method.name.equals(HString.getHString("valueOf"))) {	// special case 
							loadConstantAndFixup(res.reg, res.constant); // load address of static field "ENUM$VALUES"
							createIrDrAd(ppcLwz, res.reg, res.reg, 0);	// load reference to object on heap
						} else {	// ref to constant string
							loadConstantAndFixup(res.reg, res.constant);
						}
						break;
					default:
						ErrorReporter.reporter.error(610);
						assert false : "result of SSA instruction has wrong type";
						return;
					}
				} else 
				break;}	// sCloadConst
			case sCloadLocal:
				break;	// sCloadLocal
			case sCloadFromField: {
				opds = instr.getOperands();
				int offset = 0, refReg;			
				if (opds == null) {	// getstatic
					refReg = res.regGPR1;
					Item field = ((NoOpndRef)instr).field;
					loadConstantAndFixup(refReg, field);
				} else {	// getfield
					refReg = opds[0].reg;
					if ((ssa.cfg.method.owner == Type.wktString) &&	// string access needs special treatment
							((MonadicRef)instr).item.name.equals(HString.getRegisteredHString("value"))) {
						createIrArSrB(ppcOr, res.reg, refReg, refReg);	// result contains ref to string
						stringCharRef = ((MonadicRef)instr).item;	// ref to "value"
						break;	
					} else {
						offset = ((MonadicRef)instr).item.offset;
						createItrap(ppcTwi, TOifequal, refReg, 0);
					}
				}
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tBoolean: case tByte:
					createIrDrAd(ppcLbz, res.reg, refReg, offset);
					createIrArS(ppcExtsb, res.reg, res.reg);
					break;
				case tShort: 
					createIrDrAd(ppcLha, res.reg, refReg, offset);
					break;
				case tInteger: case tRef: case tAref: case tAboolean:
				case tAchar: case tAfloat: case tAdouble: case tAbyte: 
				case tAshort: case tAinteger: case tAlong:
					createIrDrAd(ppcLwz, res.reg, refReg, offset);
					break;
				case tChar: 
					createIrDrAd(ppcLhz, res.reg, refReg, offset);
					break;
				case tLong:
					createIrDrAd(ppcLwz, res.regLong, refReg, offset);
					createIrDrAd(ppcLwz, res.reg, refReg, offset + 4);
					break;
				case tFloat: 
					createIrDrAd(ppcLfs, res.reg, refReg, offset);
					break;
				case tDouble: 
					createIrDrAd(ppcLfd, res.reg, refReg, offset);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCloadFromField
			case sCloadFromArray: {
				opds = instr.getOperands();
				int refReg = opds[0].reg;	// ref to array;
				int indexReg = opds[1].reg;	// index into array;
				if (ssa.cfg.method.owner == Type.wktString && opds[0].owner instanceof MonadicRef && ((MonadicRef)opds[0].owner).item == stringCharRef) {	// string access needs special treatment
					createIrDrAd(ppcLwz, res.regGPR1, refReg, objectSize);	// read field "count", must be first field
					createItrap(ppcTw, TOifgeU, indexReg, res.regGPR1);
					switch (res.type & 0x7fffffff) {	// type to read
					case tByte:
						createIrDrAsimm(ppcAddi, res.regGPR2, refReg, stringSize - 4);	// add index of field "value" to index
						createIrDrArB(ppcLbzx, res.reg, res.regGPR2, indexReg);
						createIrArS(ppcExtsb, res.reg, res.reg);
						break;
					case tChar: 
						createIrArSSHMBME(ppcRlwinm, res.regGPR1, indexReg, 1, 0, 30);
						createIrDrAsimm(ppcAddi, res.regGPR2, refReg, stringSize - 4);	// add index of field "value" to index
						createIrDrArB(ppcLhzx, res.reg, res.regGPR1, res.regGPR2);
						break;
					default:
						ErrorReporter.reporter.error(610);
						assert false : "result of SSA instruction has wrong type";
						return;
					}
				} else {
					createItrap(ppcTwi, TOifequal, refReg, 0);
					createIrDrAd(ppcLha, res.regGPR1, refReg, -arrayLenOffset);
					createItrap(ppcTw, TOifgeU, indexReg, res.regGPR1);
					switch (res.type & ~(1<<ssaTaFitIntoInt)) {	// type to read
					case tByte: case tBoolean:
						createIrDrAsimm(ppcAddi, res.regGPR2, refReg, objectSize);
						createIrDrArB(ppcLbzx, res.reg, res.regGPR2, indexReg);
						createIrArS(ppcExtsb, res.reg, res.reg);
						break;
					case tShort: 
						createIrArSSHMBME(ppcRlwinm, res.regGPR1, indexReg, 1, 0, 30);
						createIrDrAsimm(ppcAddi, res.regGPR2, refReg, objectSize);
						createIrDrArB(ppcLhax, res.reg, res.regGPR1, res.regGPR2);
						break;
					case tInteger: case tRef: case tAref: case tAchar: case tAfloat: 
					case tAdouble: case tAbyte: case tAshort: case tAinteger: case tAlong:
						createIrArSSHMBME(ppcRlwinm, res.regGPR1, indexReg, 2, 0, 29);
						createIrDrAsimm(ppcAddi, res.regGPR2, refReg, objectSize);
						createIrDrArB(ppcLwzx, res.reg, res.regGPR1, res.regGPR2);
						break;
					case tLong: 
						createIrArSSHMBME(ppcRlwinm, res.regGPR1, indexReg, 3, 0, 28);
						createIrDrAsimm(ppcAddi, res.regGPR2, refReg, objectSize);
						createIrDrArB(ppcLwzux, res.regLong, res.regGPR1, res.regGPR2);
						createIrDrAd(ppcLwz, res.reg, res.regGPR1, 4);
						break;
					case tFloat:
						createIrArSSHMBME(ppcRlwinm, res.regGPR1, indexReg, 2, 0, 29);
						createIrDrAsimm(ppcAddi, res.regGPR2, refReg, objectSize);
						createIrDrArB(ppcLfsx, res.reg, res.regGPR1, res.regGPR2);
						break;
					case tDouble: 
						createIrArSSHMBME(ppcRlwinm, res.regGPR1, indexReg, 3, 0, 28);
						createIrDrAsimm(ppcAddi, res.regGPR2, refReg, objectSize);
						createIrDrArB(ppcLfdx, res.reg, res.regGPR1, res.regGPR2);
						break;
					case tChar: 
						createIrArSSHMBME(ppcRlwinm, res.regGPR1, indexReg, 1, 0, 30);
						createIrDrAsimm(ppcAddi, res.regGPR2, refReg, objectSize);
						createIrDrArB(ppcLhzx, res.reg, res.regGPR1, res.regGPR2);
						break;
					default:
						ErrorReporter.reporter.error(610);
						assert false : "result of SSA instruction has wrong type";
						return;
					}
				}
				break;}	// sCloadFromArray
			case sCstoreToField: {
				opds = instr.getOperands();
				int valReg, valRegLong, refReg, offset, type;
				if (opds.length == 1) {	// putstatic
					valReg = opds[0].reg;
					valRegLong = opds[0].regLong;
					refReg = res.regGPR1;
					Item item = ((MonadicRef)instr).item;
					if(((Type)item.type).category == 'P')
						type = Type.getPrimitiveTypeIndex(item.type.name.charAt(0));
					else type = tRef; //is a Array or a Object 
					offset = 0;
					loadConstantAndFixup(res.regGPR1, item);
				} else {	// putfield
					refReg = opds[0].reg;
					valReg = opds[1].reg;
					valRegLong = opds[1].regLong;
					if(((Type)((DyadicRef)instr).field.type).category == 'P')
						type = Type.getPrimitiveTypeIndex(((DyadicRef)instr).field.type.name.charAt(0));
					else type = tRef;//is a Array or a Object 
					offset = ((DyadicRef)instr).field.offset;
					createItrap(ppcTwi, TOifequal, refReg, 0);
				}
				switch (type) {
				case tBoolean: case tByte: 
					createIrSrAd(ppcStb, valReg, refReg, offset);
					break;
				case tShort: case tChar:
					createIrSrAd(ppcSth, valReg, refReg, offset);
					break;
				case tInteger: case tRef: case tAref: case tAboolean:
				case tAchar: case tAfloat: case tAdouble: case tAbyte: 
				case tAshort: case tAinteger: case tAlong:
					createIrSrAd(ppcStw, valReg, refReg, offset);
					break;
				case tLong:
					createIrSrAd(ppcStw, valRegLong, refReg, offset);
					createIrSrAd(ppcStw, valReg, refReg, offset + 4);
					break;
				case tFloat: 
					createIrSrAd(ppcStfs, valReg, refReg, offset);
					break;
				case tDouble: 
					createIrSrAd(ppcStfd, valReg, refReg, offset);
					break;
				default:
					ErrorReporter.reporter.error(611);
					assert false : "operand of SSA instruction has wrong type";
					return;
				}
				break;}	// sCstoreToField
			case sCstoreToArray: {
				opds = instr.getOperands();
				int refReg = opds[0].reg;	// ref to array
				int indexReg = opds[1].reg;	// index into array
				int valReg = opds[2].reg;	// value to store
				if (ssa.cfg.method.owner == Type.wktString && opds[0].owner instanceof MonadicRef && ((MonadicRef)opds[0].owner).item == stringCharRef) {	// string access needs special treatment
					createIrArSSHMBME(ppcRlwinm, res.regGPR1, indexReg, 1, 0, 30);
					createIrDrAsimm(ppcAddi, res.regGPR2, opds[0].reg, stringSize - 4);	// add index of field "value" to index
					createIrSrArB(ppcSthx, valReg, res.regGPR1, res.regGPR2);
				} else {
					createItrap(ppcTwi, TOifequal, refReg, 0);
					createIrDrAd(ppcLha, res.regGPR1, refReg, -arrayLenOffset);
					createItrap(ppcTw, TOifgeU, indexReg, res.regGPR1);
					switch (opds[0].type & ~(1<<ssaTaFitIntoInt)) {
					case tAbyte: case tAboolean:
						createIrDrAsimm(ppcAddi, res.regGPR2, refReg, objectSize);
						createIrSrArB(ppcStbx, valReg, indexReg, res.regGPR2);
						break;
					case tAshort: case tAchar: 
						createIrArSSHMBME(ppcRlwinm, res.regGPR1, indexReg, 1, 0, 30);
						createIrDrAsimm(ppcAddi, res.regGPR2, refReg, objectSize);
						createIrSrArB(ppcSthx, valReg, res.regGPR1, res.regGPR2);
						break;
					case tAref: case tRef: case tAinteger:
						createIrArSSHMBME(ppcRlwinm, res.regGPR1, indexReg, 2, 0, 29);
						createIrDrAsimm(ppcAddi, res.regGPR2, refReg, objectSize);
						createIrSrArB(ppcStwx, valReg, res.regGPR1, res.regGPR2);
						break;
					case tAlong: 
						createIrArSSHMBME(ppcRlwinm, res.regGPR1, indexReg, 3, 0, 28);
						createIrDrAsimm(ppcAddi, res.regGPR2, refReg, objectSize);
						createIrSrArB(ppcStwux, opds[2].regLong, res.regGPR1, res.regGPR2);
						createIrSrAd(ppcStw, valReg, res.regGPR1, 4);
						break;
					case tAfloat:  
						createIrArSSHMBME(ppcRlwinm, res.regGPR1, indexReg, 2, 0, 29);
						createIrDrAsimm(ppcAddi, res.regGPR2, refReg, objectSize);
						createIrSrArB(ppcStfsx, valReg, res.regGPR1, res.regGPR2);
						break;
					case tAdouble: 
						createIrArSSHMBME(ppcRlwinm, res.regGPR1, indexReg, 3, 0, 28);
						createIrDrAsimm(ppcAddi, res.regGPR2, refReg, objectSize);
						createIrSrArB(ppcStfdx, valReg, res.regGPR1, res.regGPR2);
						break;
					default:
						ErrorReporter.reporter.error(611);
						assert false : "operand of SSA instruction has wrong type";
						return;
					}
				}
				break;}	// sCstoreToArray
			case sCadd: {
				opds = instr.getOperands();
				int sReg1 = opds[0].reg;
				int sReg2 = opds[1].reg;
				int dReg = res.reg;
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tInteger:
					if (sReg1 < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						createIrDrAsimm(ppcAddi, dReg, sReg2, immVal);
					} else if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						createIrDrAsimm(ppcAddi, dReg, sReg1, immVal);
					} else {
						createIrDrArB(ppcAdd, dReg, sReg1, sReg2);
					}
					break;
				case tLong:
					if (sReg1 < 0) {
						long immValLong = ((long)(((StdConstant)opds[0].constant).valueH)<<32) | (((StdConstant)opds[0].constant).valueL&0xFFFFFFFFL);
						createIrDrAsimm(ppcAddic, dReg, sReg2, (int)immValLong);
						createIrDrA(ppcAddze, res.regLong, opds[1].regLong);
					} else if (sReg2 < 0) {
						long immValLong = ((long)(((StdConstant)opds[1].constant).valueH)<<32) | (((StdConstant)opds[1].constant).valueL&0xFFFFFFFFL);
						createIrDrAsimm(ppcAddic, dReg, sReg1, (int)immValLong);
						createIrDrA(ppcAddze, res.regLong, opds[0].regLong);
					} else {
						createIrDrArB(ppcAddc, dReg, sReg1, sReg2);
						createIrDrArB(ppcAdde, res.regLong, opds[0].regLong, opds[1].regLong);
					}	
					break;
				case tFloat:
					createIrDrArB(ppcFadds, dReg, sReg1, sReg2);
					break;
				case tDouble:
					createIrDrArB(ppcFadd, dReg, sReg1, sReg2);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	//sCadd
			case sCsub: {
				opds = instr.getOperands();
				int sReg1 = opds[0].reg;
				int sReg2 = opds[1].reg;
				int dReg = res.reg;
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tInteger:
					if (sReg1 < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						createIrDrAsimm(ppcSubfic, dReg, sReg2, immVal);
					} else if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						createIrDrAsimm(ppcAddi, dReg, sReg1, -immVal);
					} else {
						createIrDrArB(ppcSubf, dReg, sReg2, sReg1);
					}
					break;
				case tLong:
					if (sReg1 < 0) {
						long immValLong = ((long)(((StdConstant)opds[0].constant).valueH)<<32) | (((StdConstant)opds[0].constant).valueL&0xFFFFFFFFL);
						createIrDrAsimm(ppcSubfic, dReg, sReg2, (int)immValLong);
						createIrDrA(ppcSubfze, res.regLong, opds[1].regLong);
					} else if (sReg2 < 0) {
						long immValLong = ((long)(((StdConstant)opds[1].constant).valueH)<<32) | (((StdConstant)opds[1].constant).valueL&0xFFFFFFFFL);
						createIrDrAsimm(ppcAddic, dReg, sReg1, -(int)immValLong);
						createIrDrA(ppcAddme, res.regLong, opds[0].regLong);
					} else {
						createIrDrArB(ppcSubfc, dReg, sReg2, sReg1);
						createIrDrArB(ppcSubfe, res.regLong, opds[1].regLong, opds[0].regLong);
					}
					break;
				case tFloat:
					createIrDrArB(ppcFsubs, dReg, sReg1, sReg2);
					break;
				case tDouble:
					createIrDrArB(ppcFsub, dReg, sReg1, sReg2);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCsub
			case sCmul: {
				opds = instr.getOperands();
				int sReg1 = opds[0].reg;
				int sReg2 = opds[1].reg;
				int dReg = res.reg;
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tInteger:
					if (sReg1 < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						createIrDrAsimm(ppcMulli, dReg, sReg2, immVal);
					} else if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						if (sReg2 == -1) {
							createIrDrAsimm(ppcMulli, dReg, sReg1, immVal);
						} else {	// is power of 2
							int shift = 0;
							while (immVal > 1) {shift++; immVal >>= 1;}
							if (shift == 0) createIrArSrB(ppcOr, dReg, sReg1, sReg1);
							else createIrArSSHMBME(ppcRlwinm, dReg, sReg1, shift, 0, 31-shift);
						}
					} else {
						createIrDrArB(ppcMullw, dReg, sReg1, sReg2);
					}
					break;
				case tLong:
					if (sReg2 < 0) {	// is power of 2
						long immValLong = ((long)(((StdConstant)opds[1].constant).valueH)<<32) | (((StdConstant)opds[1].constant).valueL&0xFFFFFFFFL);
						int shift = 0;
						while (immValLong > 1) {shift++; immValLong >>= 1;}
						if (shift == 0) {
							createIrArSrB(ppcOr, res.regLong, opds[0].regLong, opds[0].regLong);
							createIrArSrB(ppcOr, dReg, sReg1, sReg1);
						} else {
							if (shift < 32) {
								createIrArSSHMBME(ppcRlwinm, res.regLong, sReg1, shift, 32-shift, 31);
								createIrArSSHMBME(ppcRlwimi, res.regLong, opds[0].regLong, shift, 0, 31-shift);
								createIrArSSHMBME(ppcRlwinm, dReg, sReg1, shift, 0, 31-shift);
							} else {
								createIrDrAsimm(ppcAddi, dReg, 0, 0);
								createIrArSSHMBME(ppcRlwinm, res.regLong, sReg1, shift-32, 0, 63-shift);
							}
						}
					} else {
						createIrDrArB(ppcMullw, res.regGPR1, opds[0].regLong, sReg2);
						createIrDrArB(ppcMullw, res.regGPR2, sReg1, opds[1].regLong);
						createIrDrArB(ppcAdd, res.regGPR1, res.regGPR1, res.regGPR2);
						createIrDrArB(ppcMulhwu, res.regGPR2, sReg1, sReg2);
						createIrDrArB(ppcAdd, res.regLong, res.regGPR1, res.regGPR2);
						createIrDrArB(ppcMullw, res.reg, sReg1, sReg2);
					}
					break;
				case tFloat:
					createIrDrArC(ppcFmuls, dReg, sReg1, sReg2);
					break;
				case tDouble:
					createIrDrArC(ppcFmul, dReg, sReg1, sReg2);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	//sCmul
			case sCdiv: {
				opds = instr.getOperands();
				int sReg1 = opds[0].reg;
				int sReg2 = opds[1].reg;
				int dReg = res.reg;
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tByte: case tShort: case tInteger:
					if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;	// is power of 2
						int shift = 0;
						while (immVal > 1) {shift++; immVal >>= 1;}
						if (shift == 0) 
							createIrArSrB(ppcOr, dReg, sReg1, sReg1);
						else {
							createIrArSSH(ppcSrawi, 0, sReg1, shift-1);
							createIrArSSHMBME(ppcRlwinm, 0, 0, shift, 32 - shift, 31);	
							createIrDrArB(ppcAdd, 0, sReg1, 0);
							createIrArSSH(ppcSrawi, dReg, 0, shift);
						}
					} else {
						createItrap(ppcTwi, TOifequal, sReg2, 0);
						createIrDrArB(ppcDivw, dReg, sReg1, sReg2);
					}
					break;
				case tLong:
					int sReg1Long = opds[0].regLong;
					int sReg2Long = opds[1].regLong;
					if (sReg2 < 0) {	// is power of 2
						long immValLong = ((long)(((StdConstant)opds[1].constant).valueH)<<32) | (((StdConstant)opds[1].constant).valueL&0xFFFFFFFFL);
						int shift = 0;
						while (immValLong > 1) {shift++; immValLong >>= 1;}
						if (shift == 0) {
							createIrArSrB(ppcOr, dReg, sReg1, sReg1);
							createIrArSrB(ppcOr, res.regLong, sReg1Long, sReg1Long);
						} else if (shift < 32) {
							int sh1 = shift - 1;																// shift right arithmetic immediate by shift-1
							createIrArSSHMBME(ppcRlwinm, res.regGPR1, sReg1, (32-sh1)%32, sh1, 31);				// sh1 can be 0!
							createIrArSSHMBME(ppcRlwimi, res.regGPR1, sReg1Long, (32-sh1)%32, 0, (sh1-1+32)%32);			
							createIrArSSH(ppcSrawi, res.regGPR2, sReg1Long, sh1);																																				
							createIrArSSHMBME(ppcRlwinm, res.regGPR1, res.regGPR2, shift, 32-shift, 31);		// shift right immediate by 64-shift	
							createIrDrAsimm(ppcAddi, res.regGPR2, 0, 0);
							createIrDrArB(ppcAddc, res.regGPR1, res.regGPR1, sReg1);							// add
							createIrDrArB(ppcAdde, res.regGPR2, res.regGPR2, sReg1Long);					 
							createIrArSSHMBME(ppcRlwinm, dReg, res.regGPR1, 32-shift, shift, 31);				// shift right arithmetic immediate by shift
							createIrArSSHMBME(ppcRlwimi, dReg, res.regGPR2, 32-shift, 0, shift-1);				
							createIrArSSH(ppcSrawi, res.regLong, res.regGPR2, shift);															
						} else {
							int sh1 = shift % 32;
							createIrArSSH(ppcSrawi, res.regGPR1, sReg1Long, (sh1-1+32)%32);				// shift right arithmetic immediate by shift-1
							createIrArSSH(ppcSrawi, res.regGPR2, sReg1Long, 31);							// sh1 can be 0!							
							sh1 = (64 - shift) % 32;															// shift right immediate by 64-shift
							createIrArSSHMBME(ppcRlwinm, res.regGPR1, res.regGPR1, (32-sh1)%32, sh1, 31);		
							createIrArSSHMBME(ppcRlwimi, res.regGPR1, res.regGPR2, (32-sh1)%32, 0, (sh1-1)&0x1f);			
							createIrArSSHMBME(ppcRlwinm, res.regGPR2, res.regGPR2, (32-sh1)%32, sh1, 31);		
							createIrDrArB(ppcAddc, res.regGPR1, res.regGPR1, sReg1);							// add
							createIrDrArB(ppcAdde, res.regGPR2, res.regGPR2, sReg1Long);					
							sh1 = shift % 32;
							createIrArSSH(ppcSrawi, dReg, res.regGPR2, sh1);									// shift right arithmetic immediate by shift
							createIrArSSH(ppcSrawi, res.regLong, res.regGPR2, 31);																
						}
					} else { // not a power of 2
						createICRFrAsimm(ppcCmpi, CRF1, sReg2Long, -1); // is divisor negative?
						createIBOBIBD(ppcBc, BOtrue, 4*CRF1+GT, 4);	
						createIrDrAsimm(ppcSubfic, res.regGPR2, sReg2, 0);	// negate divisor
						createIrDrA(ppcSubfze, res.regGPR1, sReg2Long);
						createIBOBIBD(ppcBc, BOalways, 0, 3);
						createIrArSrB(ppcOr, res.regGPR2, sReg2, sReg2); // copy if not negative
						createIrArSrB(ppcOr, res.regGPR1, sReg2Long, sReg2Long);
						// test, if divisor = 0, if so, throw exception
						createICRFrAsimm(ppcCmpi, CRF3, res.regGPR2, 0);
						createIBOBIBD(ppcBc, BOfalse, 4*CRF3+EQ, 3);	
						createItrap(ppcTwi, TOifequal, res.regGPR1, 0);
						createIrDrArB(ppcDivw, 0, 0, 0);	// this instruction solely serves the trap handler to
						// identify that it's a arithmetic exception
						
						createIrSrAd(ppcStmw, 26, stackPtr, tempStorageOffset + 8);
						copyParametersSubroutine(sReg1Long, sReg1, res.regGPR1, res.regGPR2);
						Method m = Method.getCompSpecSubroutine("divLong");
						loadConstantAndFixup(26, m);	// use a register which contains no operand 
						createIrSspr(ppcMtspr, LR, 26);
						createIBOBILK(ppcBclr, BOalways, 0, true);

						createIrDrAd(ppcLmw, 27, stackPtr, tempStorageOffset + 8 + 4); // restore
						createIrArSrB(ppcOr, dReg, 26, 26);
						if (dReg != 26) // restore last register if not destination register
							createIrDrAd(ppcLwz, 26, stackPtr, tempStorageOffset + 8);
						createIrArSrB(ppcOr, res.regLong, 0, 0);
					}
					break;
				case tFloat:
					createIrDrArB(ppcFdivs, dReg, sReg1, sReg2);
					break;
				case tDouble:
					createIrDrArB(ppcFdiv, dReg, sReg1, sReg2);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCdiv
			case sCrem: {
				opds = instr.getOperands();
				int sReg1 = opds[0].reg;
				int sReg2 = opds[1].reg;
				int dReg = res.reg;
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tByte: case tShort: case tInteger:
					if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;	// is power of 2
						int shift = 0;
						while (immVal > 1) {shift++; immVal >>= 1;}
						if (shift == 0) 
							loadConstant(dReg, 0);
						else {
							createIrArSSH(ppcSrawi, 0, sReg1, shift-1);
							createIrArSSHMBME(ppcRlwinm, 0, 0, shift, 32 - shift, 31);	
							createIrDrArB(ppcAdd, 0, sReg1, 0);
							createIrArSSH(ppcSrawi, dReg, 0, shift);
							createIrArSSHMBME(ppcRlwinm, 0, dReg, shift, 0, 31-shift);
							createIrDrArB(ppcSubf, dReg, 0, sReg1);
						}
					} else {
						createItrap(ppcTwi, TOifequal, sReg2, 0);
						createIrDrArB(ppcDivw, 0, sReg1, sReg2);
						createIrDrArB(ppcMullw, 0, 0, sReg2);
						createIrDrArB(ppcSubf, dReg, 0 ,sReg1);
					}
					break;
				case tLong:
					int sReg1Long = opds[0].regLong;
					int sReg2Long = opds[1].regLong;
					if (sReg2 < 0) {	// is power of 2
						long immValLong = ((long)(((StdConstant)opds[1].constant).valueH)<<32) | (((StdConstant)opds[1].constant).valueL&0xFFFFFFFFL);
						int shift = 0;
						while (immValLong > 1) {shift++; immValLong >>= 1;}
						if (shift == 0) {
							loadConstant(res.regLong, 0);
							loadConstant(dReg, 0);
						} else if (shift < 32) {
							int sh1 = shift - 1;																// shift right arithmetic immediate by shift-1
							createIrArSSHMBME(ppcRlwinm, res.regGPR1, sReg1, (32-sh1)%32, sh1, 31);					
							createIrArSSHMBME(ppcRlwimi, res.regGPR1, sReg1Long, (32-sh1)%32, 0, (sh1-1+32)%32);			
							createIrArSSH(ppcSrawi, res.regGPR2, sReg1Long, sh1);																																				
							createIrArSSHMBME(ppcRlwinm, res.regGPR1, res.regGPR2, shift, 32 - shift, 31);		// shift right immediate by 64-shift	
							createIrDrAsimm(ppcAddi, res.regGPR2, 0, 0);
							createIrDrArB(ppcAddc, res.regGPR1, res.regGPR1, sReg1);							// add
							createIrDrArB(ppcAdde, res.regGPR2, res.regGPR2, sReg1Long);					 
							createIrArSSHMBME(ppcRlwinm, res.regGPR1, res.regGPR1, 32-shift, shift, 31);		// shift right arithmetic immediate by shift
							createIrArSSHMBME(ppcRlwimi, res.regGPR1, res.regGPR2, 32-shift, 0, shift-1);				
							createIrArSSH(ppcSrawi, res.regGPR2, res.regGPR2, shift);															
							
							createIrArSSHMBME(ppcRlwinm, 0, res.regGPR1, shift, 32-shift, 31);					// multiply
							createIrArSSHMBME(ppcRlwimi, 0, res.regGPR2, shift, 0, 31-shift);
							createIrArSSHMBME(ppcRlwinm, res.regGPR1, res.regGPR1, shift, 0, 31-shift);
							
							createIrDrArB(ppcSubfc, dReg, res.regGPR1, sReg1);									// subtract
							createIrDrArB(ppcSubfe, res.regLong, 0, sReg1Long);
						} else {
							int sh1 = shift % 32;
							createIrArSSH(ppcSrawi, res.regGPR1, sReg1Long, (sh1-1+32)%32);				// shift right arithmetic immediate by shift-1
							createIrArSSH(ppcSrawi, res.regGPR2, sReg1Long, 31);															
							sh1 = (64 - shift) % 32;															// shift right immediate by 64-shift
							createIrArSSHMBME(ppcRlwinm, res.regGPR1, res.regGPR1, (32-sh1)%32, sh1, 31);						
							createIrArSSHMBME(ppcRlwimi, res.regGPR1, res.regGPR2, (32-sh1)%32, 0, (sh1-1)&0x1f);					
							createIrArSSHMBME(ppcRlwinm, res.regGPR2, res.regGPR2, (32-sh1)%32, sh1, 31);		
							createIrDrArB(ppcAddc, res.regGPR1, res.regGPR1, sReg1);							// add
							createIrDrArB(ppcAdde, res.regGPR2, res.regGPR2, sReg1Long);					 
							sh1 = shift % 32;																	// shift right arithmetic immediate by shift
							createIrArSSH(ppcSrawi, res.regGPR1, res.regGPR2, sh1);									
							createIrArSSH(ppcSrawi, res.regGPR2, res.regGPR2, 31);									
							
							createIrArSSHMBME(ppcRlwinm, res.regGPR2, res.regGPR1, shift-32, 0, 63-shift);		// multiply
							createIrDrAsimm(ppcAddi, res.regGPR1, 0, 0);									
							
							createIrDrArB(ppcSubfc, dReg, res.regGPR1, sReg1);									// subtract
							createIrDrArB(ppcSubfe, res.regLong, res.regGPR2, sReg1Long);
						}
					} else { // not a power of 2
						createICRFrAsimm(ppcCmpi, CRF1, sReg2Long, -1); // is divisor negative?
						createIBOBIBD(ppcBc, BOtrue, 4*CRF1+GT, 4);	
						createIrDrAsimm(ppcSubfic, res.regGPR2, sReg2, 0);	// negate divisor
						createIrDrA(ppcSubfze, res.regGPR1, sReg2Long);
						createIBOBIBD(ppcBc, BOalways, 0, 3);
						createIrArSrB(ppcOr, res.regGPR1, sReg2Long, sReg2Long); // copy if not negative
						createIrArSrB(ppcOr, res.regGPR2, sReg2, sReg2);
						// test, if divisor = 0, if so, throw exception
						createICRFrAsimm(ppcCmpi, CRF1, sReg2, 0);
						createIBOBIBD(ppcBc, BOfalse, 4*CRF1+EQ, 3);	
						createItrap(ppcTwi, TOifequal, sReg2Long, 0);
						createIrDrArB(ppcDivw, sReg2, sReg2, sReg2);	// this instruction solely serves the trap handler to
						// identify that it's a arithmetic exception

						createIrSrAd(ppcStmw, 24, stackPtr, tempStorageOffset + 8);
						copyParametersSubroutine(sReg1Long, sReg1, res.regGPR1, res.regGPR2);
						Method m = Method.getCompSpecSubroutine("remLong");
						loadConstantAndFixup(24, m);	// use a register which contains no operand 
						createIrSspr(ppcMtspr, LR, 24);
						createIBOBILK(ppcBclr, BOalways, 0, true);

						createIrDrAd(ppcLmw, 25, stackPtr, tempStorageOffset + 8 + 4); // restore
						createIrArSrB(ppcOr, dReg, 24, 24);
						if (dReg != 24) // restore last register if not destination register
							createIrDrAd(ppcLwz, 24, stackPtr, tempStorageOffset + 8);
						createIrArSrB(ppcOr, res.regLong, 0, 0);
					}
					break;
				case tFloat:	// correct if a / b < 32 bit
					createIrDrArB(ppcFdiv, dReg, sReg1, sReg2);
					createIrDrB(ppcFctiwz, 0, dReg);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, res.regGPR1, stackPtr, tempStorageOffset + 4);
					Item item = int2floatConst1;	// ref to 2^52+2^31;					
					createIrDrAsimm(ppcAddis, 0, 0, 0x4330);	// preload 2^52
					createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset);
					createIrArSuimm(ppcXoris, 0, res.regGPR1, 0x8000);
					createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset+4);
					loadConstantAndFixup(res.regGPR1, item);
					createIrDrAd(ppcLfd, dReg, res.regGPR1, 0);
					createIrDrAd(ppcLfd, 0, stackPtr, tempStorageOffset);
					createIrDrArB(ppcFsub, dReg, 0, dReg);
					createIrDrArC(ppcFmul, dReg, dReg, sReg2);
					createIrDrArB(ppcFsub, dReg, sReg1, dReg);
					break;
				case tDouble:	// correct if a / b < 32 bit
					createIrDrArB(ppcFdiv, dReg, sReg1, sReg2);
					createIrDrB(ppcFctiwz, 0, dReg);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, res.regGPR1, stackPtr, tempStorageOffset + 4);
					item = int2floatConst1;	// ref to 2^52+2^31;					
					createIrDrAsimm(ppcAddis, 0, 0, 0x4330);	// preload 2^52
					createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset);
					createIrArSuimm(ppcXoris, 0, res.regGPR1, 0x8000);
					createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset+4);
					loadConstantAndFixup(res.regGPR1, item);
					createIrDrAd(ppcLfd, dReg, res.regGPR1, 0);
					createIrDrAd(ppcLfd, 0, stackPtr, tempStorageOffset);
					createIrDrArB(ppcFsub, dReg, 0, dReg);
					createIrDrArC(ppcFmul, dReg, dReg, sReg2);
					createIrDrArB(ppcFsub, dReg, sReg1, dReg);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCrem
			case sCneg: {
				opds = instr.getOperands();
				int type = res.type & ~(1<<ssaTaFitIntoInt);
				if (type == tInteger)
					createIrDrA(ppcNeg, res.reg, opds[0].reg);
				else if (type == tLong) {
					createIrDrAsimm(ppcSubfic, res.reg, opds[0].reg, 0);
					createIrDrA(ppcSubfze, res.regLong, opds[0].regLong);
				} else if (type == tFloat || type == tDouble)
					createIrDrB(ppcFneg, res.reg, opds[0].reg);
				else {
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCneg
			case sCshl: {
				opds = instr.getOperands();
				int sReg1 = opds[0].reg;
				int sReg2 = opds[1].reg;
				int dReg = res.reg;
				int type = res.type & ~(1<<ssaTaFitIntoInt);
				if (type == tInteger) {
					if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH % 32;
						createIrArSSHMBME(ppcRlwinm, dReg, sReg1, immVal, 0, 31-immVal);
					} else {
						createIrArSSHMBME(ppcRlwinm, 0, sReg2, 0, 27, 31);
						createIrArSrB(ppcSlw, dReg, sReg1, 0);
					}
				} else if (type == tLong) {
					if (sReg2 < 0) {	
						int immVal = ((StdConstant)opds[1].constant).valueH % 64;
						if (immVal < 32) {
							createIrArSSHMBME(ppcRlwinm, 0, sReg1, immVal, 32-immVal, 31);
							createIrArSSHMBME(ppcRlwimi, 0, opds[0].regLong, immVal, 0, 31-immVal);
							createIrArSSHMBME(ppcRlwinm, dReg, sReg1, immVal, 0, 31-immVal);
							createIrArSrB(ppcOr, res.regLong, 0, 0);
						} else {
							createIrArSSHMBME(ppcRlwinm, res.regLong, sReg1, immVal-32, 0, 63-immVal);
							createIrDrAsimm(ppcAddi, dReg, 0, 0);
						}
					} else { 
						createIrDrAsimm(ppcSubfic, res.regGPR1, sReg2, 32);
						createIrArSrB(ppcSlw, res.regLong, opds[0].regLong, sReg2);
						createIrArSrB(ppcSrw, 0, sReg1, res.regGPR1);
						createIrArSrB(ppcOr, res.regLong, res.regLong, 0);
						createIrDrAsimm(ppcAddi, res.regGPR1, sReg2, -32);
						createIrArSrB(ppcSlw, 0, sReg1, res.regGPR1);
						createIrArSrB(ppcOr, res.regLong, res.regLong, 0);
						createIrArSrB(ppcSlw, dReg, sReg1, sReg2);
					}
				} else {
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCshl
			case sCshr: {
				opds = instr.getOperands();
				int sReg1 = opds[0].reg;
				int sReg2 = opds[1].reg;
				int dReg = res.reg;
				int type = res.type & ~(1<<ssaTaFitIntoInt);
				if (type == tInteger) {
					if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH % 32;
						createIrArSSH(ppcSrawi, dReg, sReg1, immVal);
					} else {
						createIrArSSHMBME(ppcRlwinm, 0, sReg2, 0, 27, 31);
						createIrArSrB(ppcSraw, dReg, sReg1, 0);
					}
				} else if (type == tLong) {
					if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH % 64;
						if (immVal == 0) {
							createIrArSrB(ppcOr, dReg, sReg1, sReg1);
							createIrArSrB(ppcOr, res.regLong, opds[0].regLong, opds[0].regLong);
						} else if (immVal < 32) {
							createIrArSSHMBME(ppcRlwinm, dReg, sReg1, 32-immVal, immVal, 31);
							createIrArSSHMBME(ppcRlwimi, dReg, opds[0].regLong, 32-immVal, 0, immVal-1);
							createIrArSSH(ppcSrawi, res.regLong, opds[0].regLong, immVal);
						} else {
							immVal %= 32;
							createIrArSSH(ppcSrawi, res.reg, opds[0].regLong, immVal);
							createIrArSSH(ppcSrawi, res.regLong, opds[0].regLong, 31);
						}
					} else {
						createIrArSSHMBME(ppcRlwinm, 0, sReg2, 0, 26, 31);
						createIrDrAsimm(ppcSubfic, res.regGPR1, 0, 32);
						createIrArSrB(ppcSrw, dReg, sReg1, sReg2);
						createIrArSrB(ppcSlw, 0, opds[0].regLong, res.regGPR1);
						createIrArSrB(ppcOr, dReg, dReg, 0);
						createIrArSSHMBME(ppcRlwinm, 0, sReg2, 0, 26, 31);
						createIrDrAsimm(ppcAddicp, res.regGPR1, 0, -32);
						createIrArSrB(ppcSraw, 0, opds[0].regLong, res.regGPR1);
						createIBOBIBD(ppcBc, BOfalse, 4*CRF0+GT, 2);
						createIrArSuimm(ppcOri, dReg, 0, 0);
						createIrArSrB(ppcSraw, res.regLong, opds[0].regLong, sReg2);
					}
				} else {
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCshr
			case sCushr: {
				opds = instr.getOperands();
				int sReg1 = opds[0].reg;
				int sReg2 = opds[1].reg;
				int dReg = res.reg;
				int type = res.type & ~(1<<ssaTaFitIntoInt);
				if (type == tInteger) {
					if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH % 32;
						createIrArSSHMBME(ppcRlwinm, dReg, sReg1, (32-immVal)%32, immVal, 31);
					} else {
						createIrArSSHMBME(ppcRlwinm, 0, sReg2, 0, 27, 31);
						createIrArSrB(ppcSrw, dReg, sReg1, 0);
					}
				} else if (type == tLong) {
					if (sReg2 < 0) {	
						int immVal = ((StdConstant)opds[1].constant).valueH % 64;
						if (immVal == 0) {
							createIrArSrB(ppcOr, dReg, sReg1, sReg1);
							createIrArSrB(ppcOr, res.regLong, opds[0].regLong, opds[0].regLong);
						} else if (immVal < 32) {
							createIrArSSHMBME(ppcRlwinm, dReg, sReg1, (32-immVal)%32, immVal, 31);
							createIrArSSHMBME(ppcRlwimi, dReg, opds[0].regLong, (32-immVal)%32, 0, (immVal-1)&0x1f);
							createIrArSSHMBME(ppcRlwinm, res.regLong, opds[0].regLong, (32-immVal)%32, immVal, 31);
						} else {
							createIrArSSHMBME(ppcRlwinm, dReg, opds[0].regLong, (64-immVal)%32, immVal-32, 31);
							createIrDrAsimm(ppcAddi, res.regLong, 0, 0);
						}
					} else {
						createIrDrAsimm(ppcSubfic, res.regGPR1, sReg2, 32);
						createIrArSrB(ppcSrw, dReg, sReg1, sReg2);
						createIrArSrB(ppcSlw, 0, opds[0].regLong, res.regGPR1);
						createIrArSrB(ppcOr, dReg, dReg, 0);
						createIrDrAsimm(ppcAddi, res.regGPR1, sReg2, -32);
						createIrArSrB(ppcSrw, 0, opds[0].regLong, res.regGPR1);
						createIrArSrB(ppcOr, dReg, dReg, 0);
						createIrArSrB(ppcSrw, res.regLong, opds[0].regLong, sReg2);
					}
				} else {
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCushr
			case sCand: {
				opds = instr.getOperands();
				int sReg1 = opds[0].reg;
				int sReg2 = opds[1].reg;
				int dReg = res.reg;
				if ((res.type & ~(1<<ssaTaFitIntoInt)) == tInteger) {
					if (sReg1 < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						if (immVal >= 0)
							createIrArSuimm(ppcAndi, dReg, sReg2, immVal);
						else {
							createIrDrAsimm(ppcAddi, 0, 0, immVal);
							createIrArSrB(ppcAnd, dReg, 0, sReg2);
						}
					} else if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						if (immVal >= 0)
							createIrArSuimm(ppcAndi, dReg, sReg1, immVal);
						else {
							createIrDrAsimm(ppcAddi, 0, 0, immVal);
							createIrArSrB(ppcAnd, dReg, 0, sReg1);
						}
					} else
						createIrArSrB(ppcAnd, dReg, sReg1, sReg2);
				} else if ((res.type & ~(1<<ssaTaFitIntoInt)) == tLong) {
					if (sReg1 < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueL;
						if (immVal >= 0) {
							createIrArSuimm(ppcAndi, res.regLong, opds[1].regLong, 0);
							createIrArSuimm(ppcAndi, dReg, sReg2, (int)immVal);
						} else {
							createIrDrAsimm(ppcAddi, 0, 0, immVal);
							createIrArSrB(ppcOr, res.regLong, opds[1].regLong, opds[1].regLong);
							createIrArSrB(ppcAnd, dReg, sReg2, 0);
						}
					} else if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueL;
						if (immVal >= 0) {
							createIrArSuimm(ppcAndi, res.regLong, opds[0].regLong, 0);
							createIrArSuimm(ppcAndi, dReg, sReg1, (int)immVal);
						} else {
							createIrDrAsimm(ppcAddi, 0, 0, immVal);
							createIrArSrB(ppcOr, res.regLong, opds[0].regLong, opds[0].regLong);
							createIrArSrB(ppcAnd, dReg, sReg1, 0);
						}
					} else {
						createIrArSrB(ppcAnd, res.regLong, opds[0].regLong, opds[1].regLong);
						createIrArSrB(ppcAnd, dReg, sReg1, sReg2);
					}
				} else {
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
			break;}	// sCand
			case sCor: {
				opds = instr.getOperands();
				int sReg1 = opds[0].reg;
				int sReg2 = opds[1].reg;
				int dReg = res.reg;
				if ((res.type & ~(1<<ssaTaFitIntoInt)) == tInteger) {
					if (sReg1 < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						createIrArSuimm(ppcOri, dReg, sReg2, immVal);
						if (immVal < 0)
							createIrArSuimm(ppcOris, dReg, dReg, 0xffff);					
					} else if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						createIrArSuimm(ppcOri, dReg, sReg1, immVal);
						if (immVal < 0)
							createIrArSuimm(ppcOris, dReg, dReg, 0xffff);					
					} else
						createIrArSrB(ppcOr, dReg, sReg1, sReg2);
				} else if ((res.type & ~(1<<ssaTaFitIntoInt)) == tLong) {
					if (sReg1 < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueL;
						createIrArSrB(ppcOr, res.regLong, opds[1].regLong, opds[1].regLong);
						createIrArSuimm(ppcOri, dReg, sReg2, (int)immVal);
						if (immVal < 0) {
							createIrArSuimm(ppcOris, dReg, dReg, 0xffff);	
							createIrDrAsimm(ppcAddi, res.regLong, 0, -1);	
						}
					} else if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueL;
						createIrArSrB(ppcOr, res.regLong, opds[0].regLong, opds[0].regLong);
						createIrArSuimm(ppcOri, dReg, sReg1, (int)immVal);
						if (immVal < 0) {
							createIrArSuimm(ppcOris, dReg, dReg, 0xffff);					
							createIrDrAsimm(ppcAddi, res.regLong, 0, -1);	
						}
					} else {
						createIrArSrB(ppcOr, res.regLong, opds[0].regLong, opds[1].regLong);
						createIrArSrB(ppcOr, dReg, sReg1, sReg2);
					}
				} else {
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	//sCor
			case sCxor: {
				opds = instr.getOperands();
				int sReg1 = opds[0].reg;
				int sReg2 = opds[1].reg;
				int dReg = res.reg;
				if ((res.type & ~(1<<ssaTaFitIntoInt)) == tInteger) {
					if (sReg1 < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueH;
						createIrArSuimm(ppcXori, dReg, sReg2, immVal);
						if (immVal < 0)
							createIrArSuimm(ppcXoris, dReg, dReg, 0xffff);
						else 
							createIrArSuimm(ppcXoris, dReg, dReg, 0);
					} else if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueH;
						createIrArSuimm(ppcXori, dReg, sReg1, immVal);
						if (immVal < 0)
							createIrArSuimm(ppcXoris, dReg, dReg, 0xffff);
						else 
							createIrArSuimm(ppcXoris, dReg, dReg, 0);
					} else
						createIrArSrB(ppcXor, dReg, sReg1, sReg2);
				} else if ((res.type & ~(1<<ssaTaFitIntoInt)) == tLong) {
					if (sReg1 < 0) {
						int immVal = ((StdConstant)opds[0].constant).valueL;
						createIrArSuimm(ppcXori, dReg, sReg2, (int)immVal);
						if (immVal < 0) {
							createIrArSuimm(ppcXoris, dReg, dReg, 0xffff);
							createIrArSuimm(ppcXori, res.regLong, opds[1].regLong, 0xffff);
							createIrArSuimm(ppcXoris, res.regLong, res.regLong, 0xffff);
						} else {
							createIrArSrB(ppcOr, res.regLong, opds[1].regLong, opds[1].regLong);
							createIrArSuimm(ppcXoris, dReg, dReg, 0);
						}
					} else if (sReg2 < 0) {
						int immVal = ((StdConstant)opds[1].constant).valueL;
						createIrArSuimm(ppcXori, dReg, sReg1, (int)immVal);
						if (immVal < 0) {
							createIrArSuimm(ppcXoris, dReg, dReg, 0xffff);
							createIrArSuimm(ppcXori, res.regLong, opds[0].regLong, 0xffff);
							createIrArSuimm(ppcXoris, res.regLong, res.regLong, 0xffff);
						} else {
							createIrArSrB(ppcOr, res.regLong, opds[0].regLong, opds[0].regLong);
							createIrArSuimm(ppcXoris, dReg, dReg, 0);
						}
					} else {
						createIrArSrB(ppcXor, res.regLong, opds[0].regLong, opds[1].regLong);
						createIrArSrB(ppcXor, dReg, sReg1, sReg2);
					}
				} else {
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCxor
			case sCconvInt:	{// int -> other type
				opds = instr.getOperands();
				int sReg1 = opds[0].reg;
				int dReg = res.reg;
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tByte:
					createIrArS(ppcExtsb, dReg, sReg1);
					break;
				case tChar: 
					createIrArSSHMBME(ppcRlwinm, dReg, sReg1, 0, 16, 31);
					break;
				case tShort: 
					createIrArS(ppcExtsh, dReg, sReg1);
					break;
				case tLong:
					createIrArSrB(ppcOr, dReg, sReg1, sReg1);
					createIrArSSH(ppcSrawi, res.regLong, sReg1, 31);
					break;
				case tFloat:
					Item item = int2floatConst1;	// ref to 2^52+2^31;					
					createIrDrAsimm(ppcAddis, 0, 0, 0x4330);	// preload 2^52
					createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset);
					createIrArSuimm(ppcXoris, 0, sReg1, 0x8000);
					createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset+4);
					loadConstantAndFixup(res.regGPR1, item);
					createIrDrAd(ppcLfd, dReg, res.regGPR1, 0);
					createIrDrAd(ppcLfd, 0, stackPtr, tempStorageOffset);
					createIrDrArB(ppcFsub, dReg, 0, dReg);
					createIrDrB(ppcFrsp, dReg, dReg);
					break;
				case tDouble:
//					instructions[iCount] = ppcMtfsfi | (7 << 23) | (4  << 12);
//					incInstructionNum();
					item = int2floatConst1;	// ref to 2^52+2^31;					
					createIrDrAsimm(ppcAddis, 0, 0, 0x4330);	// preload 2^52
					createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset);
					createIrArSuimm(ppcXoris, 0, sReg1, 0x8000);
					createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset+4);
					loadConstantAndFixup(res.regGPR1, item);
					createIrDrAd(ppcLfd, dReg, res.regGPR1, 0);
					createIrDrAd(ppcLfd, 0, stackPtr, tempStorageOffset);
					createIrDrArB(ppcFsub, dReg, 0, dReg);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}	// sCconvInt
			case sCconvLong: {	// long -> other type
				opds = instr.getOperands();
				int sReg1 = opds[0].reg;
				int dReg = res.reg;
				switch (res.type & ~(1<<ssaTaFitIntoInt)){
				case tByte:
					createIrArS(ppcExtsb, dReg, sReg1);
					break;
				case tChar: 
					createIrArSSHMBME(ppcRlwinm, dReg, sReg1, 0, 16, 31);
					break;
				case tShort: 
					createIrArS(ppcExtsh, dReg, sReg1);
					break;
				case tInteger:
					createIrArSrB(ppcOr, dReg, sReg1, sReg1);
					break;
				case tFloat:
					createIrSrAd(ppcStmw, 29, stackPtr, tempStorageOffset + 8);
					Method m = Method.getCompSpecSubroutine("longToDouble");
					copyParametersSubroutine(opds[0].regLong, sReg1, 0, 0);
					loadConstantAndFixup(29, m);	// use a register which contains no operand 
					createIrSspr(ppcMtspr, LR, 29);
					createIBOBILK(ppcBclr, BOalways, 0, true);
					createIrDrAd(ppcLmw, 29, stackPtr, tempStorageOffset + 8);
					createIrDrB(ppcFmr, dReg, 0);	// get result
					createIrDrB(ppcFrsp, dReg, dReg);
					break;
				case tDouble:
					createIrSrAd(ppcStmw, 29, stackPtr, tempStorageOffset + 8);
					m = Method.getCompSpecSubroutine("longToDouble");
					copyParametersSubroutine(opds[0].regLong, sReg1, 0, 0);
					loadConstantAndFixup(29, m);	// use a register which contains no operand 
					createIrSspr(ppcMtspr, LR, 29);
					createIBOBILK(ppcBclr, BOalways, 0, true);
					createIrDrAd(ppcLmw, 29, stackPtr, tempStorageOffset + 8);
					createIrDrB(ppcFmr, dReg, 0);	// get result
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}
			case sCconvFloat: {	// float -> other type
				opds = instr.getOperands();
				int sReg1 = opds[0].reg;
				int dReg = res.reg;
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tByte:
					createIrDrB(ppcFctiw, 0, sReg1);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, 0, stackPtr, tempStorageOffset + 4);
					createIrArS(ppcExtsb, dReg, 0);
					break;
				case tChar: 
					createIrDrB(ppcFctiw, 0, sReg1);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, 0, stackPtr, tempStorageOffset + 4);
					createIrArSSHMBME(ppcRlwinm, dReg, 0, 0, 16, 31);
					break;
				case tShort: 
					createIrDrB(ppcFctiw, 0, sReg1);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, 0, stackPtr, tempStorageOffset + 4);
					createIrArS(ppcExtsh, dReg, 0);
					break;
				case tInteger:
					createIrDrB(ppcFctiwz, 0, sReg1);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, dReg, stackPtr, tempStorageOffset + 4);
					break;
				case tLong:	
					createIrSrAd(ppcStmw, 28, stackPtr, tempStorageOffset + 8);
					Method m = Method.getCompSpecSubroutine("doubleToLong");
					createIrDrB(ppcFmr, 0, sReg1);
					loadConstantAndFixup(29, m);	// use a register which contains no operand 
					createIrSspr(ppcMtspr, LR, 29);
					createIBOBILK(ppcBclr, BOalways, 0, true);
					createIrDrAd(ppcLmw, 29, stackPtr, tempStorageOffset + 8 + 4); // restore
					createIrArSrB(ppcOr, dReg, 28, 28);
					if (dReg != 28) // restore last register if not destination register
						createIrDrAd(ppcLwz, 28, stackPtr, tempStorageOffset + 8);
					createIrArSrB(ppcOr, res.regLong, 0, 0);
					break;
				case tDouble:
					createIrDrB(ppcFmr, dReg, sReg1);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}
			case sCconvDouble: {	// double -> other type
				opds = instr.getOperands();
				int sReg1 = opds[0].reg;
				int dReg = res.reg;
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tByte:
					createIrDrB(ppcFctiw, 0, sReg1);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, 0, stackPtr, tempStorageOffset + 4);
					createIrArS(ppcExtsb, dReg, 0);
					break;
				case tChar: 
					createIrDrB(ppcFctiw, 0, sReg1);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, 0, stackPtr, tempStorageOffset + 4);
					createIrArSSHMBME(ppcRlwinm, dReg, 0, 0, 16, 31);
					break;
				case tShort: 
					createIrDrB(ppcFctiw, 0, sReg1);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, 0, stackPtr, tempStorageOffset + 4);
					createIrArS(ppcExtsh, dReg, 0);
					break;
				case tInteger:
					createIrDrB(ppcFctiwz, 0, sReg1);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, dReg, stackPtr, tempStorageOffset + 4);
					break;
				case tLong:	
					createIrSrAd(ppcStmw, 28, stackPtr, tempStorageOffset + 8);
					Method m = Method.getCompSpecSubroutine("doubleToLong");
					createIrDrB(ppcFmr, 0, sReg1);
					loadConstantAndFixup(29, m);	// use a register which contains no operand 
					createIrSspr(ppcMtspr, LR, 29);
					createIBOBILK(ppcBclr, BOalways, 0, true);
					createIrDrAd(ppcLmw, 29, stackPtr, tempStorageOffset + 8 + 4); // restore
					createIrArSrB(ppcOr, dReg, 28, 28);
					if (dReg != 28) // restore last register if not destination register
						createIrDrAd(ppcLwz, 28, stackPtr, tempStorageOffset + 8);
					createIrArSrB(ppcOr, res.regLong, 0, 0);
					break;
				case tFloat:
					createIrDrB(ppcFrsp, dReg, sReg1);
					break;
				default:
					ErrorReporter.reporter.error(610);
					assert false : "result of SSA instruction has wrong type";
					return;
				}
				break;}
			case sCcmpl: case sCcmpg: {
				opds = instr.getOperands();
				int sReg1 = opds[0].reg;
				int sReg2 = opds[1].reg;
				int type = opds[0].type & ~(1<<ssaTaFitIntoInt);
				if (type == tLong) {
					int sReg1L = opds[0].regLong;
					int sReg2L = opds[1].regLong;
					createICRFrArB(ppcCmp, CRF0, sReg1L, sReg2L);
					createICRFrArB(ppcCmpl, CRF1, sReg1, sReg2);
					instr = node.instructions[i+1];
					if (instr.ssaOpcode == sCregMove) {i++; instr = node.instructions[i+1]; assert false;}
					assert instr.ssaOpcode == sCbranch : "sCcompl or sCcompg is not followed by branch instruction";
					int bci = ssa.cfg.code[node.lastBCA] & 0xff;
					if (bci == bCifeq) {
						createIcrbDcrbAcrbB(ppcCrand, CRF0EQ, CRF0EQ, CRF1EQ);
						createIBOBIBD(ppcBc, BOtrue, CRF0EQ, 0);
					} else if (bci == bCifne) {
						createIcrbDcrbAcrbB(ppcCrand, CRF0EQ, CRF0EQ, CRF1EQ);
						createIBOBIBD(ppcBc, BOfalse, CRF0EQ, 0);
					} else if (bci == bCiflt) {
						createIcrbDcrbAcrbB(ppcCrand, CRF1LT, CRF0EQ, CRF1LT);
						createIcrbDcrbAcrbB(ppcCror, CRF0LT, CRF1LT, CRF0LT);
						createIBOBIBD(ppcBc, BOtrue, CRF0LT, 0);
					} else if (bci == bCifge) {
						createIcrbDcrbAcrbB(ppcCrand, CRF1LT, CRF0EQ, CRF1LT);
						createIcrbDcrbAcrbB(ppcCror, CRF0LT, CRF1LT, CRF0LT);
						createIBOBIBD(ppcBc, BOfalse, CRF0LT, 0);
					} else if (bci == bCifgt) {
						createIcrbDcrbAcrbB(ppcCrand, CRF1LT, CRF0EQ, CRF1GT);
						createIcrbDcrbAcrbB(ppcCror, CRF0LT, CRF1LT, CRF0GT);
						createIBOBIBD(ppcBc, BOtrue, CRF0LT, 0);
					} else if (bci == bCifle) {
						createIcrbDcrbAcrbB(ppcCrand, CRF1LT, CRF0EQ, CRF1GT);
						createIcrbDcrbAcrbB(ppcCror, CRF0LT, CRF1LT, CRF0GT);
						createIBOBIBD(ppcBc, BOfalse, CRF0LT, 0);
					} else {
						ErrorReporter.reporter.error(623);
						assert false : "sCcompl or sCcompg is not followed by branch instruction";
						return;
					}
				} else if (type == tFloat  || type == tDouble) {
					createICRFrArB(ppcFcmpu, CRF1, sReg1, sReg2);
					instr = node.instructions[i+1];
					assert instr.ssaOpcode == sCbranch : "sCcompl or sCcompg is not followed by branch instruction";
					int bci = ssa.cfg.code[node.lastBCA] & 0xff;
					if (bci == bCifeq) 
						createIBOBIBD(ppcBc, BOtrue, CRF1EQ, 0);
					else if (bci == bCifne)
						createIBOBIBD(ppcBc, BOfalse, CRF1EQ, 0);
					else if (bci == bCiflt)
						createIBOBIBD(ppcBc, BOtrue, CRF1LT, 0);
					else if (bci == bCifge)
						createIBOBIBD(ppcBc, BOfalse, CRF1LT, 0);
					else if (bci == bCifgt)
						createIBOBIBD(ppcBc, BOtrue, CRF1GT, 0);
					else if (bci == bCifle)
						createIBOBIBD(ppcBc, BOfalse, CRF1GT, 0);
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
				opds = instr.getOperands();
				int sReg1 = opds[0].reg;

				MonadicRef ref = (MonadicRef)instr;
				Type t = (Type)ref.item;
				if (t.category == tcRef) {	// object (to test for) is regular class or interface
					if ((t.accAndPropFlags & (1<<apfInterface)) != 0) {	// object is interface
						createICRFrAsimm(ppcCmpi, CRF0, sReg1, 0);	// is null?
						createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 3);	// jump to label 2
						// label 1
						createIrDrAsimm(ppcAddi, res.reg, 0, 0);
						createIBOBIBD(ppcBc, BOalways, 4*CRF0, 13);	// jump to end
						// label 2
						createIrDrAd(ppcLbz, res.regGPR1, sReg1, -7);	// get array bit
						createICRFrAsimm(ppcCmpi, CRF0, res.regGPR1, 0);	// is array?
						createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, -4);	// jump to label 1
						createIrDrAd(ppcLwz, res.regGPR1, sReg1, -4);	// get tag
						createIrDrAd(ppcLwz, 0, res.regGPR1, Linker32.tdIntfTypeChkTableOffset);
						createIrDrArB(ppcAdd, res.regGPR1, res.regGPR1, 0);
						// label 3
						createIrDrAd(ppcLhzu, 0, res.regGPR1, 0);
						createICRFrAsimm(ppcCmpi, CRF0, 0, ((Class)t).chkId);	// is interface chkId
						createIrDrAsimm(ppcAddi, res.regGPR1, res.regGPR1, 2);
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+GT, -3);	// jump to label 3			
						createIrD(ppcMfcr, res.reg);
						createIrArSSHMBME(ppcRlwinm, res.reg, res.reg, 3, 31, 31);
					} else {	// regular class
						int offset = ((Class)t).extensionLevel;
						if (t.name.equals(HString.getHString("java/lang/Object"))) {
							createICRFrAsimm(ppcCmpi, CRF0, sReg1, 0);	// is null?
							createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 3);	// jump to label 1
							createIrDrAsimm(ppcAddi, res.reg, 0, 0);
							createIBOBIBD(ppcBc, BOalways, 4*CRF0, 2);	// jump to end
							// label 1
							createIrDrAsimm(ppcAddi, res.reg, 0, 1);
						} else { // regular class but not java/lang/Object
							createICRFrAsimm(ppcCmpi, CRF0, sReg1, 0);	// is null?
							createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 3);	// jump to label 2
							// label 1
							createIrDrAsimm(ppcAddi, res.reg, 0, 0);
							createIBOBIBD(ppcBc, BOalways, 4*CRF0, 11);	// jump to end
							// label 2
							createIrDrAd(ppcLbz, res.regGPR1, sReg1, -7);	// get array bit
							createICRFrAsimm(ppcCmpi, CRF0, res.regGPR1, 0);	// is array?
							createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, -4);	// jump to label 1
							createIrDrAd(ppcLwz, res.regGPR1, sReg1, -4);	// get tag
							createIrDrAd(ppcLwz, 0, res.regGPR1, Linker32.tdBaseClass0Offset + offset * 4);
							loadConstantAndFixup(res.regGPR1, t);	// addr of type
							createICRFrArB(ppcCmpl, CRF0, 0, res.regGPR1);
							createIrD(ppcMfcr, res.reg);
							createIrArSSHMBME(ppcRlwinm, res.reg, res.reg, 3, 31, 31);
						}
					}
				} else {	// object is an array
					if (((Array)t).componentType.category == tcPrimitive) {  // array of base type
						// test if not null
						createICRFrAsimm(ppcCmpi, CRF0, sReg1, 0);	// is null?
						createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 3);	// jump to label 2
						// label 1
						createIrDrAsimm(ppcAddi, res.reg, 0, 0);
						createIBOBIBD(ppcBc, BOalways, 4*CRF0, 10);	// jump to end
						// label 2
						createIrDrAd(ppcLbz, res.regGPR1, sReg1, -7);	// get array bit
						createICRFrAsimm(ppcCmpi, CRF0, res.regGPR1, 0);	// is not array?
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, -4);	// jump to label 1
						createIrDrAd(ppcLwz, 0, sReg1, -4);	// get tag
						loadConstantAndFixup(res.regGPR1, t);	// addr of type
						createICRFrArB(ppcCmpl, CRF0, 0, res.regGPR1);
						createIrD(ppcMfcr, res.reg);
						createIrArSSHMBME(ppcRlwinm, res.reg, res.reg, 3, 31, 31);
					} else {	// array of regular classes or interfaces
						int nofDim = ((Array)t).dimension;
						Item compType = RefType.refTypeList.getItemByName(((Array)t).componentType.name.toString());
						int offset = ((Class)(((Array)t).componentType)).extensionLevel;
						if (((Array)t).componentType.name.equals(HString.getHString("java/lang/Object"))) {
							// test if not null
							createICRFrAsimm(ppcCmpi, CRF0, sReg1, 0);	// is null?
							createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 3);	// jump to label 2
							// label 1
							createIrDrAsimm(ppcAddi, res.reg, 0, 0);
							createIBOBIBD(ppcBc, BOalways, 4*CRF0, 16);	// jump to end
							// label 2
							createIrDrAd(ppcLbz, res.regGPR1, sReg1, -7);	// get array bit
							createICRFrAsimm(ppcCmpi, CRF0, res.regGPR1, 0);	// is not array?
							createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, -4);	// jump to label 1

							createIrDrAd(ppcLwz, res.regGPR1, sReg1, -4);	// get tag
							createIrDrAd(ppcLwz, 0, res.regGPR1, 0);	
							createIrArSSHMBME(ppcRlwinm, res.regGPR1, 0, 16, 17, 31);	// get dim
							createICRFrAsimm(ppcCmpi, CRF0, 0, 0);	// check if array of primitive type
							createIBOBIBD(ppcBc, BOtrue, 4*CRF0+LT, 5);	// jump to label 3					
							createICRFrAsimm(ppcCmpi, CRF0, res.regGPR1, nofDim);
							createIBOBIBD(ppcBc, BOtrue, 4*CRF0+LT, -11);	// jump to label 1	
							createIrDrAsimm(ppcAddi, res.reg, 0, 1);
							createIBOBIBD(ppcBc, BOalways, 4*CRF0, 4);	// jump to end
							// label 3, is array of primitive type
							createICRFrAsimm(ppcCmpi, CRF0, res.regGPR1, nofDim);
							createIBOBIBD(ppcBc, BOfalse, 4*CRF0+GT, -15);	// jump to label 1	
							createIrDrAsimm(ppcAddi, res.reg, 0, 1);
						} else {	// array of regular classes or interfaces but not java/lang/Object
							if ((compType.accAndPropFlags & (1<<apfInterface)) != 0) {	// array of interfaces
								createICRFrAsimm(ppcCmpi, CRF0, sReg1, 0);	// is null?
								createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 3);	// jump to label 2
								// label 1
								createIrDrAsimm(ppcAddi, res.reg, 0, 0);
								createIBOBIBD(ppcBc, BOalways, 4*CRF0, 20);	// jump to end
								// label 2
								createIrDrAd(ppcLbz, res.regGPR1, sReg1, -7);	// get array bit
								createICRFrAsimm(ppcCmpi, CRF0, res.regGPR1, 0);	// is not array?
								createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, -4);	// jump to label 1

								createIrDrAd(ppcLwz, res.regGPR1, sReg1, -4);	// get tag
								createIrDrAd(ppcLwz, 0, res.regGPR1, 0);			
								createIrArSSHMBME(ppcRlwinm, 0, 0, 16, 17, 31);	// get dim
								createICRFrAsimm(ppcCmpi, CRF0, 0, nofDim);
								createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, -9);	// jump to label 1					

								createIrDrAd(ppcLwz, res.regGPR1, res.regGPR1, 8 + nofDim * 4);	// get component type
								createICRFrAsimm(ppcCmpi, CRF0, res.regGPR1, 0);	// is 0?
								createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, -12);	// jump to label 1					

								createIrDrAd(ppcLwz, 0, res.regGPR1, Linker32.tdIntfTypeChkTableOffset);
								createIrDrArB(ppcAdd, res.regGPR1, res.regGPR1, 0);
								// label 3
								createIrDrAd(ppcLhzu, 0, res.regGPR1, 0);
								createICRFrAsimm(ppcCmpi, CRF0, 0, ((Class)compType).chkId);	// is interface chkId
								createIrDrAsimm(ppcAddi, res.regGPR1, res.regGPR1, 2);
								createIBOBIBD(ppcBc, BOtrue, 4*CRF0+GT, -3);	// jump to label 3			
								createIrD(ppcMfcr, res.reg);	
								createIrArSSHMBME(ppcRlwinm, res.reg, res.reg, 3, 31, 31);			
							} else {	// array of regular classes
								// test if not null
								createICRFrAsimm(ppcCmpi, CRF0, sReg1, 0);	// is null?
								createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 3);	// jump to label 2
								// label 1
								createIrDrAsimm(ppcAddi, res.reg, 0, 0);
								createIBOBIBD(ppcBc, BOalways, 4*CRF0, 18);	// jump to end
								// label 2
								createIrDrAd(ppcLbz, res.regGPR1, sReg1, -7);	// get array bit
								createICRFrAsimm(ppcCmpi, CRF0, res.regGPR1, 0);	// is not array?
								createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, -4);	// jump to label 1

								createIrDrAd(ppcLwz, res.regGPR1, sReg1, -4);	// get tag
								createIrDrAd(ppcLwz, 0, res.regGPR1, 0);	
								createIrArSSHMBME(ppcRlwinm, 0, 0, 16, 17, 31);	// get dim
								createICRFrAsimm(ppcCmpi, CRF0, 0, nofDim);
								createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, -9);	// jump to label 1					

								createIrDrAd(ppcLwz, res.regGPR1, res.regGPR1, 8 + nofDim * 4);	// get component type
								createICRFrAsimm(ppcCmpi, CRF0, res.regGPR1, 0);	// is 0?
								createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, -12);	// jump to label 1					

								createIrDrAd(ppcLwz, 0, res.regGPR1, Linker32.tdBaseClass0Offset + offset * 4);
								loadConstantAndFixup(res.regGPR1, compType);	// addr of component type
								createICRFrArB(ppcCmpl, CRF0, 0, res.regGPR1);
								createIrD(ppcMfcr, res.reg);
								createIrArSSHMBME(ppcRlwinm, res.reg, res.reg, 3, 31, 31);
							}
						}
					}
				}
				break;}
			case sCcheckcast: {
				// this ssa instruction must be translated, so that only "twi, TOifnequal" is used
				// this enables the trap handler to throw a ClassCastException
				opds = instr.getOperands();
				int sReg1 = opds[0].reg;
				MonadicRef ref = (MonadicRef)instr;
				Type t = (Type)ref.item;
				if (t.category == tcRef) {	// object (to test for) is regular class or interface
					if ((t.accAndPropFlags & (1<<apfInterface)) != 0) {	// object is interface
						createICRFrAsimm(ppcCmpi, CRF0, sReg1, 0);	// is null?
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 11);	// jump to end
						createIrDrAd(ppcLbz, res.regGPR1, sReg1, -7);	// get array bit
						createItrapSimm(ppcTwi, TOifnequal, res.regGPR1, 0);	// is not array?
						createIrDrAd(ppcLwz, res.regGPR1, sReg1, -4);	// get tag
						createIrDrAd(ppcLwz, 0, res.regGPR1, Linker32.tdIntfTypeChkTableOffset);
						createIrDrArB(ppcAdd, res.regGPR1, res.regGPR1, 0);
						// label 1
						createIrDrAd(ppcLhzu, 0, res.regGPR1, 0);
						createICRFrAsimm(ppcCmpi, CRF0, 0, ((Class)t).chkId);	// is interface chkId
						createIrDrAsimm(ppcAddi, res.regGPR1, res.regGPR1, 2);
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+GT, -3);	// jump to label 1			
						createItrapSimm(ppcTwi, TOifnequal, 0, ((Class)t).chkId);	// chkId is not equal
					} else {	// object is regular class
						int offset = ((Class)t).extensionLevel;
						createICRFrAsimm(ppcCmpi, CRF0, sReg1, 0);	// is null?
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 8);	// jump to end
						createIrDrAd(ppcLbz, res.regGPR1, sReg1, -7);	// get array bit
						createItrapSimm(ppcTwi, TOifnequal, res.regGPR1, 0);	// is not array?
						createIrDrAd(ppcLwz, res.regGPR1, sReg1, -4);	// get tag
						createIrDrAd(ppcLwz, 0, res.regGPR1, Linker32.tdBaseClass0Offset + offset * 4);
						loadConstantAndFixup(res.regGPR1, t);	// addr of type
						createItrap(ppcTw, TOifnequal, res.regGPR1, 0);
					}
				} else {	// object (to test for) is an array
					if (((Array)t).componentType.category == tcPrimitive) {  // array of base type
						createICRFrAsimm(ppcCmpi, CRF0, sReg1, 0);	// is null?
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 8);	// jump to end
						createIrDrAd(ppcLbz, res.regGPR1, sReg1, -7);	// get array bit
						createIrArSuimm(ppcAndi, res.regGPR1, res.regGPR1, 0x80);
						createItrapSimm(ppcTwi, TOifnequal, res.regGPR1, 0x80);	// is array?
						createIrDrAd(ppcLwz, 0, sReg1, -4);	// get tag
						loadConstantAndFixup(res.regGPR1, t);	// addr of type
						createItrap(ppcTw, TOifnequal, res.regGPR1, 0);
					} else {	// array of regular classes or interfaces
						int nofDim = ((Array)t).dimension;
						Item compType = RefType.refTypeList.getItemByName(((Array)t).componentType.name.toString());
						if (((Array)t).componentType.name.equals(HString.getHString("java/lang/Object"))) {
							createICRFrAsimm(ppcCmpi, CRF0, sReg1, 0);	// is null?
							createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 15);	// jump to end
							createIrDrAd(ppcLbz, res.regGPR1, sReg1, -7);	// get array bit
							createIrArSuimm(ppcAndi, res.regGPR1, res.regGPR1, 0x80);
							createItrapSimm(ppcTwi, TOifnequal, res.regGPR1, 0x80);	// is array?

							createIrDrAd(ppcLwz, res.regGPR1, sReg1, -4);	// get tag
							createIrDrAd(ppcLwz, 0, res.regGPR1, 0);	
							createIrArSSHMBME(ppcRlwinm, res.regGPR1, 0, 16, 17, 31);	// get dim
							createICRFrAsimm(ppcCmpi, CRF0, 0, 0);	// check if array of primitive type
							createIBOBIBD(ppcBc, BOtrue, 4*CRF0+LT, 4);	// jump to label 3	
							
							createICRFrAsimm(ppcCmpi, CRF0, res.regGPR1, nofDim);
							createIBOBIBD(ppcBc, BOfalse, 4*CRF0+LT, 5);	// jump to end	
							createItrap(ppcTwi, TOifnequal, res.regGPR1, -1);	// trap always
							// label 3, is array of primitive type
							createICRFrAsimm(ppcCmpi, CRF0, res.regGPR1, nofDim);
							createIBOBIBD(ppcBc, BOtrue, 4*CRF0+GT, 2);	// jump to end	
							createItrap(ppcTwi, TOifnequal, res.regGPR1, -1);	// trap always
						} else {	// array of regular classes or interfaces but not java/lang/Object
							if ((compType.accAndPropFlags & (1<<apfInterface)) != 0) {	// array of interfaces
								createICRFrAsimm(ppcCmpi, CRF0, sReg1, 0);	// is null?
								createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 19);	// jump to end
								createIrDrAd(ppcLbz, res.regGPR1, sReg1, -7);	// get array bit
								createIrArSuimm(ppcAndi, res.regGPR1, res.regGPR1, 0x80);
								createItrapSimm(ppcTwi, TOifnequal, res.regGPR1, 0x80);	// is array?

								createIrDrAd(ppcLwz, res.regGPR1, sReg1, -4);	// get tag
								createIrDrAd(ppcLwz, 0, res.regGPR1, 0);	
								createIrArSSHMBME(ppcRlwinm, 0, 0, 16, 17, 31);	// get dim
								createItrapSimm(ppcTwi, TOifnequal, 0, nofDim);	// check dim

								createIrDrAd(ppcLwz, res.regGPR1, res.regGPR1, 8 + nofDim * 4);	// get component type
								createICRFrAsimm(ppcCmpi, CRF0, sReg1, 0);	// is null?
								createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 2);
								createItrapSimm(ppcTwi, TOifnequal, sReg1, -1);	// is 0?
								createIrDrAd(ppcLwz, 0, res.regGPR1, Linker32.tdIntfTypeChkTableOffset);
								createIrDrArB(ppcAdd, res.regGPR1, res.regGPR1, 0);
								// label 1
								createIrDrAd(ppcLhzu, 0, res.regGPR1, 0);
								createICRFrAsimm(ppcCmpi, CRF0, 0, ((Class)compType).chkId);	// is interface chkId
								createIrDrAsimm(ppcAddi, res.regGPR1, res.regGPR1, 2);
								createIBOBIBD(ppcBc, BOtrue, 4*CRF0+GT, -3);	// jump to label 1			
								createItrapSimm(ppcTwi, TOifnequal, 0, ((Class)compType).chkId);	// chkId is not equal
							} else {	// array of regular classes
								int offset = ((Class)(((Array)t).componentType)).extensionLevel;
								createICRFrAsimm(ppcCmpi, CRF0, sReg1, 0);	// is null?
								createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 16);	// jump to end
								createIrDrAd(ppcLbz, res.regGPR1, sReg1, -7);	// get array bit
								createIrArSuimm(ppcAndi, res.regGPR1, res.regGPR1, 0x80);
								createItrapSimm(ppcTwi, TOifnequal, res.regGPR1, 0x80);	// is array?

								createIrDrAd(ppcLwz, res.regGPR1, sReg1, -4);	// get tag
								createIrDrAd(ppcLwz, 0, res.regGPR1, 0);	
								createIrArSSHMBME(ppcRlwinm, 0, 0, 16, 17, 31);	// get dim
								createItrapSimm(ppcTwi, TOifnequal, 0, nofDim);	// check dim

								createIrDrAd(ppcLwz, res.regGPR1, res.regGPR1, 8 + nofDim * 4);	// get component type
								createICRFrAsimm(ppcCmpi, CRF0, sReg1, 0);	// is null?
								createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 2);
								createItrapSimm(ppcTwi, TOifnequal, sReg1, -1);	// is 0?

								createIrDrAd(ppcLwz, 0, res.regGPR1, Linker32.tdBaseClass0Offset + offset * 4);
								loadConstantAndFixup(res.regGPR1, compType);	// addr of component type
								createItrap(ppcTw, TOifnequal, res.regGPR1, 0);
							}
						}
					}
				}
				break;}
			case sCthrow: {
				opds = instr.getOperands();
				createIrArSrB(ppcOr, paramStartGPR, opds[0].reg, opds[0].reg);	// put exception into parameter register
				createItrap(ppcTw, TOalways, 0, 0);
				break;}
			case sCalength: {
				opds = instr.getOperands();
				int refReg = opds[0].reg;
				createItrap(ppcTwi, TOifequal, refReg, 0);
				createIrDrAd(ppcLha, res.reg , refReg, -arrayLenOffset);
				break;}
			case sCcall: {
				opds = instr.getOperands();
				Call call = (Call)instr;
				Method m = (Method)call.item;
				if ((m.accAndPropFlags & (1 << dpfSynthetic)) != 0) {
					if (m.id == idGET1) {	// GET1
						createIrDrAd(ppcLbz, res.reg, opds[0].reg, 0);
						createIrArS(ppcExtsb, res.reg, res.reg);
					} else if (m.id == idGET2) { // GET2
						createIrDrAd(ppcLha, res.reg, opds[0].reg, 0);
					} else if (m.id == idGET4) { // GET4
						createIrDrAd(ppcLwz, res.reg, opds[0].reg, 0);
					} else if (m.id == idGET8) { // GET8
						createIrDrAd(ppcLwz, res.regLong, opds[0].reg, 0);
						createIrDrAd(ppcLwz, res.reg, opds[0].reg, 4);
					} else if (m.id == idPUT1) { // PUT1
						createIrSrAd(ppcStb, opds[1].reg, opds[0].reg, 0);
					} else if (m.id == idPUT2) { // PUT2
						createIrSrAd(ppcSth, opds[1].reg, opds[0].reg, 0);
					} else if (m.id == idPUT4) { // PUT4
						createIrSrAd(ppcStw, opds[1].reg, opds[0].reg, 0);
					} else if (m.id == idPUT8) { // PUT8
						createIrSrAd(ppcStw, opds[1].regLong, opds[0].reg, 0);
						createIrSrAd(ppcStw, opds[1].reg, opds[0].reg, 4);
					} else if (m.id == idBIT) { // BIT
						createIrDrAd(ppcLbz, res.reg, opds[0].reg, 0);
						createIrDrAsimm(ppcSubfic, 0, opds[1].reg, 32);
						createIrArSrBMBME(ppcRlwnm, res.reg, res.reg, 0, 31, 31);
					} else if (m.id == idGETGPR) { // GETGPR
						int gpr = ((StdConstant)opds[0].constant).valueH;
						createIrArSrB(ppcOr, res.reg, gpr, gpr);
					} else if (m.id == idGETFPR) { // GETFPR
						int fpr = ((StdConstant)opds[0].constant).valueH;
						createIrDrB(ppcFmr, res.reg, fpr);
					} else if (m.id == idGETSPR) { // GETSPR
						int spr = ((StdConstant)opds[0].constant).valueH;
						createIrSspr(ppcMfspr, spr, res.reg);
					} else if (m.id == idPUTGPR) { // PUTGPR
						int gpr = ((StdConstant)opds[0].constant).valueH;
						createIrArSrB(ppcOr, gpr, opds[1].reg, opds[1].reg);
					} else if (m.id == idPUTFPR) { // PUTFPR
						int fpr = ((StdConstant)opds[0].constant).valueH;
						createIrDrB(ppcFmr, fpr, opds[1].reg);
					} else if (m.id == idPUTSPR) { // PUTSPR
						createIrArSrB(ppcOr, 0, opds[1].reg, opds[1].reg);
						int spr = ((StdConstant)opds[0].constant).valueH;
						createIrSspr(ppcMtspr, spr, 0);
					} else if (m.id == idHALT) { // HALT	// TODO
						createItrap(ppcTw, TOalways, 0, 0);
					} else if (m.id == idASM) { // ASM
						instructions[iCount] = InstructionDecoder.getCode(((StringLiteral)opds[0].constant).string.toString());
						iCount++;
						int len = instructions.length;
						if (iCount == len) {
							int[] newInstructions = new int[2 * len];
							for (int k = 0; k < len; k++)
								newInstructions[k] = instructions[k];
							instructions = newInstructions;
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
							Item method = clazz.methods.getItemByName(methName.toString());
							loadConstantAndFixup(res.reg, method);	// addr of method
						}
					} else if (m.id == idREF) { // REF
						createIrArSrB(ppcOr, res.reg, opds[0].reg, opds[0].reg);
					} else if (m.id == idDoubleToBits) { // DoubleToBits
						createIrSrAd(ppcStfd, opds[0].reg, stackPtr, tempStorageOffset);
						createIrDrAd(ppcLwz, res.regLong, stackPtr, tempStorageOffset);
						createIrDrAd(ppcLwz, res.reg, stackPtr, tempStorageOffset + 4);
					} else if (m.id == idBitsToDouble) { // BitsToDouble
						createIrSrAd(ppcStw, opds[0].regLong, stackPtr, tempStorageOffset);
						createIrSrAd(ppcStw, opds[0].reg, stackPtr, tempStorageOffset+4);
						createIrDrAd(ppcLfd, res.reg, stackPtr, tempStorageOffset);
					} else if (m.id == idFloatToBits) { // FloatToBits
						createIrSrAd(ppcStfs, opds[0].reg, stackPtr, tempStorageOffset);
						createIrDrAd(ppcLwz, res.reg, stackPtr, tempStorageOffset);
					} else if (m.id == idBitsToFloat) { // BitsToFloat
						createIrSrAd(ppcStw, opds[0].reg, stackPtr, tempStorageOffset);
						createIrDrAd(ppcLfs, 0, stackPtr, tempStorageOffset);
						createIrDrB(ppcFmr, res.reg, 0);
					}
				} else {	// real method (not synthetic)
					if ((m.accAndPropFlags & (1<<apfStatic)) != 0 ||
							m.name.equals(HString.getHString("newPrimTypeArray")) ||
							m.name.equals(HString.getHString("newRefArray"))
							) {	// invokestatic
						if (m == stringNewstringMethod) {	// replace newstring stub with Heap.newstring
							m = heapNewstringMethod;
							loadConstantAndFixup(res.regGPR1, m);	
							createIrSspr(ppcMtspr, LR, res.regGPR1); 
						} else {
							loadConstantAndFixup(res.regGPR1, m);	// addr of method
							createIrSspr(ppcMtspr, LR, res.regGPR1);
						}
					} else if ((m.accAndPropFlags & (1<<dpfInterfCall)) != 0) {	// invokeinterface
						int refReg = opds[0].reg;
						int offset = (Class.maxExtensionLevelStdClasses + 1) * Linker32.slotSize + Linker32.tdBaseClass0Offset;
						createItrap(ppcTwi, TOifequal, refReg, 0);
						createIrDrAd(ppcLwz, res.regGPR1, refReg, -4);
						createIrDrAd(ppcLwz, res.regGPR1, res.regGPR1, offset);	// delegate method
						createIrSspr(ppcMtspr, LR, res.regGPR1);
					} else if (call.invokespecial) {	// invokespecial
						if (newString) {	// special treatment for strings
							if (m == strInitC) m = strAllocC;
							else if (m == strInitCII) m = strAllocCII;	// addr of corresponding allocate method
							else if (m == strInitCII) m = strAllocCII;
							loadConstantAndFixup(res.regGPR1, m);	
							createIrSspr(ppcMtspr, LR, res.regGPR1);
						} else {
							int refReg = opds[0].reg;
							createItrap(ppcTwi, TOifequal, refReg, 0);
							loadConstantAndFixup(res.regGPR1, m);	// addr of init method
							createIrSspr(ppcMtspr, LR, res.regGPR1);
						}
					} else {	// invokevirtual 
						int refReg = opds[0].reg;
						int offset = Linker32.tdMethTabOffset;
						offset -= m.index * Linker32.slotSize; 
						createItrap(ppcTwi, TOifequal, refReg, 0);
						createIrDrAd(ppcLwz, res.regGPR1, refReg, -4);
						createIrDrAd(ppcLwz, res.regGPR1, res.regGPR1, offset);
						createIrSspr(ppcMtspr, LR, res.regGPR1);
					}
					
					// copy parameters into registers and to stack if not enough registers
					if (dbg) StdStreams.vrb.println("call to " + m.name + ": copy parameters");
					copyParameters(opds);
					
					if ((m.accAndPropFlags & (1<<dpfInterfCall)) != 0) {	// invokeinterface
						// interface info goes into last parameter register
						loadConstant(paramEndGPR, m.owner.index << 16 | m.index * 4);	// interface id and method offset						// check if param = maxParam in reg -2
					}
					
					if (newString) {
						int sizeOfObject = Type.wktObject.objectSize;
						createIrDrAsimm(ppcAddi, paramStartGPR+opds.length, 0, sizeOfObject); // reg after last parameter
					}
					createIBOBILK(ppcBclr, BOalways, 0, true);
					
					// get result
					int type = res.type & ~(1<<ssaTaFitIntoInt);
					if (type == tLong) {
						if (res.regLong == returnGPR2) {
							if (res.reg == returnGPR1) {	// returnGPR2 -> r0, returnGPR1 -> r3, r0 -> r2
								createIrArSrB(ppcOr, 0, returnGPR2, returnGPR2);
								createIrArSrB(ppcOr, res.regLong, returnGPR1, returnGPR1);
								createIrArSrB(ppcOr, res.reg, 0, 0);
							} else {	// returnGPR2 -> reg, returnGPR1 -> r3
								createIrArSrB(ppcOr, res.reg, returnGPR2, returnGPR2);
								createIrArSrB(ppcOr, res.regLong, returnGPR1, returnGPR1);
							}
						} else { // returnGPR1 -> regLong, returnGPR2 -> reg
							createIrArSrB(ppcOr, res.regLong, returnGPR1, returnGPR1);
							createIrArSrB(ppcOr, res.reg, returnGPR2, returnGPR2);
						}
					} else if (type == tFloat || type == tDouble) {
						createIrDrB(ppcFmr, res.reg, returnFPR);
					} else if (type == tVoid) {
						if (newString) {
							newString = false;
							createIrArSrB(ppcOr, stringReg, returnGPR1, returnGPR1); // stringReg was set by preceding sCnew
						}
					} else
						createIrArSrB(ppcOr, res.reg, returnGPR1, returnGPR1);
					
				}
				break;}	//sCcall
			case sCnew: {
				opds = instr.getOperands();
				Item item = ((Call)instr).item;	// item = ref
				Item method;
				if (opds == null) {	// bCnew
					if (item == Type.wktString) {
						newString = true;	// allocation of strings is postponed
						stringReg = res.reg;
						loadConstantAndFixup(res.reg, item);	// ref to string
					} else {
						method = CFR.getNewMemoryMethod(bCnew);
						loadConstantAndFixup(paramStartGPR, method);	// addr of new
						createIrSspr(ppcMtspr, LR, paramStartGPR);
						loadConstantAndFixup(paramStartGPR, item);	// ref
						createIBOBILK(ppcBclr, BOalways, 0, true);
						createIrArSrB(ppcOr, res.reg, returnGPR1, returnGPR1);
					}
				} else if (opds.length == 1) {
					switch (res.type  & ~(1<<ssaTaFitIntoInt)) {
					case tAboolean: case tAchar: case tAfloat: case tAdouble:
					case tAbyte: case tAshort: case tAinteger: case tAlong:	// bCnewarray
						method = CFR.getNewMemoryMethod(bCnewarray);
						loadConstantAndFixup(res.regGPR1, method);	// addr of newarray
						createIrSspr(ppcMtspr, LR, res.regGPR1);
						createIrArSrB(ppcOr, paramStartGPR, opds[0].reg, opds[0].reg);	// nof elems
						createIrDrAsimm(ppcAddi, paramStartGPR + 1, 0, (instr.result.type & 0x7fffffff) - 10);	// type
						loadConstantAndFixup(paramStartGPR + 2, item);	// ref to type descriptor
						createIBOBILK(ppcBclr, BOalways, 0, true);
						createIrArSrB(ppcOr, res.reg, returnGPR1, returnGPR1);
						break;
					case tAref:	// bCanewarray
						method = CFR.getNewMemoryMethod(bCanewarray);
						loadConstantAndFixup(res.regGPR1, method);	// addr of anewarray
						createIrSspr(ppcMtspr, LR, res.regGPR1);
						createIrArSrB(ppcOr, paramStartGPR, opds[0].reg, opds[0].reg);	// nof elems
						loadConstantAndFixup(paramStartGPR + 1, item);	// ref to type descriptor
						createIBOBILK(ppcBclr, BOalways, 0, true);
						createIrArSrB(ppcOr, res.reg, returnGPR1, returnGPR1);
						break;
					default:
						ErrorReporter.reporter.error(612);
						assert false : "operand of new instruction has wrong type";
						return;
					}
				} else { // bCmultianewarray:
					method = CFR.getNewMemoryMethod(bCmultianewarray);
					loadConstantAndFixup(res.regGPR1, method);	// addr of multianewarray
					createIrSspr(ppcMtspr, LR, res.regGPR1);
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
								createIrArSrB(ppcOr, cnt, srcGPR[cnt], srcGPR[cnt]);
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
								createIrArSrB(ppcOr, 0, srcGPR[cnt], srcGPR[cnt]);
								srcGPRcount[srcGPR[cnt]]--;
								done = false;
							}
							boolean done1 = false;
							while (!done1) {
								int k = paramStartGPR + 2; done1 = true;
								while (srcGPR[k] != 0) {
									if (srcGPRcount[k] == 0 && k != src) {
										createIrArSrB(ppcOr, k, srcGPR[k], srcGPR[k]);
										srcGPRcount[k]--; srcGPRcount[srcGPR[k]]--; 
										done1 = false;
									}
									k++; 
								}
							}
							if (src != 0) {
								createIrArSrB(ppcOr, src, 0, 0);
								srcGPRcount[src]--;
							}
							cnt++;
						}
					}
					loadConstantAndFixup(paramStartGPR, item);	// ref to type descriptor
					createIrDrAsimm(ppcAddi, paramStartGPR+1, 0, opds.length);	// nofDimensions
					createIBOBILK(ppcBclr, BOalways, 0, true);
					createIrArSrB(ppcOr, res.reg, returnGPR1, returnGPR1);
				}
				break;}
			case sCreturn: {
				opds = instr.getOperands();
				int bci = ssa.cfg.code[node.lastBCA] & 0xff;
				switch (bci) {
				case bCreturn:
					break;
				case bCireturn:
				case bCareturn:
					createIrArSrB(ppcOr, returnGPR1, opds[0].reg, opds[0].reg);
					break;
				case bClreturn:
					createIrArSrB(ppcOr, returnGPR1, opds[0].regLong, opds[0].regLong);
					createIrArSrB(ppcOr, returnGPR2, opds[0].reg, opds[0].reg);
					break;
				case bCfreturn:
				case bCdreturn:
					createIrDrB(ppcFmr, returnFPR, opds[0].reg);
					break;
				default:
					ErrorReporter.reporter.error(620);
					assert false : "return instruction not implemented";
					return;
				}
				if (node.next != null)	// last node needs no branch
					createIli(ppcB, 0, false);
				break;}
			case sCbranch:
			case sCswitch: {
				int bci = ssa.cfg.code[node.lastBCA] & 0xff;
				switch (bci) {
				case bCgoto:
					createIli(ppcB, 0, false);
					break;
				case bCif_acmpeq:
				case bCif_acmpne:
					opds = instr.getOperands();
					int sReg1 = opds[0].reg;
					int sReg2 = opds[1].reg;
					createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);
					if (bci == bCif_acmpeq)
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 0);
					else
						createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 0);
					break;
				case bCif_icmpeq:
				case bCif_icmpne:
				case bCif_icmplt:
				case bCif_icmpge:
				case bCif_icmpgt:
				case bCif_icmple:
					boolean inverted = false;
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					sReg2 = opds[1].reg;
					if (sReg1 < 0) {
						if (opds[0].constant != null) {
							int immVal = ((StdConstant)opds[0].constant).valueH;
							if ((immVal >= -32768) && (immVal <= 32767))
								createICRFrAsimm(ppcCmpi, CRF0, sReg2, immVal);
							else
								createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);
						} else
								createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);					
					} else if (sReg2 < 0) {
						if (opds[1].constant != null) {
							int immVal = ((StdConstant)opds[1].constant).valueH;
							if ((immVal >= -32768) && (immVal <= 32767)) {
								inverted = true;
								createICRFrAsimm(ppcCmpi, CRF0, sReg1, immVal);
							} else
								createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);
						} else
							createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);					
					} else {
						createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);
					}
					if (!inverted) {
						if (bci == bCif_icmpeq) 
							createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 0);
						else if (bci == bCif_icmpne)
							createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 0);
						else if (bci == bCif_icmplt)
							createIBOBIBD(ppcBc, BOtrue, 4*CRF0+LT, 0);
						else if (bci == bCif_icmpge)
							createIBOBIBD(ppcBc, BOfalse, 4*CRF0+LT, 0);
						else if (bci == bCif_icmpgt)
							createIBOBIBD(ppcBc, BOtrue, 4*CRF0+GT, 0);
						else if (bci == bCif_icmple)
							createIBOBIBD(ppcBc, BOfalse, 4*CRF0+GT, 0);
					} else {
						if (bci == bCif_icmpeq) 
							createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 0);
						else if (bci == bCif_icmpne)
							createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 0);
						else if (bci == bCif_icmplt)
							createIBOBIBD(ppcBc, BOtrue, 4*CRF0+GT, 0);
						else if (bci == bCif_icmpge)
							createIBOBIBD(ppcBc, BOfalse, 4*CRF0+GT, 0);
						else if (bci == bCif_icmpgt)
							createIBOBIBD(ppcBc, BOtrue, 4*CRF0+LT, 0);
						else if (bci == bCif_icmple)
							createIBOBIBD(ppcBc, BOfalse, 4*CRF0+LT, 0);
					}
					break; 
				case bCifeq:
				case bCifne:
				case bCiflt:
				case bCifge:
				case bCifgt:
				case bCifle: 
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					createICRFrAsimm(ppcCmpi, CRF0, sReg1, 0);
					if (bci == bCifeq) 
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 0);
					else if (bci == bCifne)
						createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 0);
					else if (bci == bCiflt)
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+LT, 0);
					else if (bci == bCifge)
						createIBOBIBD(ppcBc, BOfalse, 4*CRF0+LT, 0);
					else if (bci == bCifgt)
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+GT, 0);
					else if (bci == bCifle)
						createIBOBIBD(ppcBc, BOfalse, 4*CRF0+GT, 0);
					break;
				case bCifnonnull:
				case bCifnull: 
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					createICRFrAsimm(ppcCmpi, CRF0, sReg1, 0);
					if (bci == bCifnonnull)
						createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 0);
					else
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 0);
					break;
				case bCtableswitch:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					int addr = node.lastBCA + 1;
					addr = (addr + 3) & -4; // round to the next multiple of 4
					addr += 4; // skip default offset
					int low = getInt(ssa.cfg.code, addr);
					int high = getInt(ssa.cfg.code, addr + 4);
					int nofCases = high - low + 1;
					for (int k = 0; k < nofCases; k++) {
						createICRFrAsimm(ppcCmpi, CRF0, sReg1, low + k);
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 0);
					}
					createIli(ppcB, nofCases, false);
					break;
				case bClookupswitch:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					addr = node.lastBCA + 1;
					addr = (addr + 3) & -4; // round to the next multiple of 4
					addr += 4; // skip default offset
					int nofPairs = getInt(ssa.cfg.code, addr);
					for (int k = 0; k < nofPairs; k++) {
						int key = getInt(ssa.cfg.code, addr + 4 + k * 8);
						createICRFrAsimm(ppcCmpi, CRF0, sReg1, key);
						createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 0);
					}
					createIli(ppcB, nofPairs, true);
					break;
				default:
					ErrorReporter.reporter.error(621);
					assert false : "branch instruction not implemented";
					return;
				}
				break;}
			case sCregMove: {
				opds = instr.getOperands();
				switch (res.type & ~(1<<ssaTaFitIntoInt)) {
				case tInteger: case tChar: case tShort: case tByte: 
				case tBoolean: case tRef: case tAref: case tAboolean:
				case tAchar: case tAfloat: case tAdouble: case tAbyte: 
				case tAshort: case tAinteger: case tAlong:
					createIrArSrB(ppcOr, res.reg, opds[0].reg, opds[0].reg);
					break;
				case tLong:
					createIrArSrB(ppcOr, res.regLong, opds[0].regLong, opds[0].regLong);
					createIrArSrB(ppcOr, res.reg, opds[0].reg, opds[0].reg);
					break;
				case tFloat: case tDouble:
					createIrDrB(ppcFmr, res.reg, opds[0].reg);
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
				assert false : "SSA instruction not implemented" + SSAInstructionMnemonics.scMnemonics[instr.ssaOpcode] + " function";
				return;
			}
		}
	}

	private static void correctJmpAddr(int[] instructions, int count1, int count2) {
		instructions[count1] |= ((count2 - count1) << 2) & 0xffff;
	}

	private void copyParameters(SSAValue[] opds) {
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
		
		// count register usage
		int i = paramStartGPR;

		if (dbg) {
			StdStreams.vrb.print("srcGPR = ");
			for (int k = paramStartGPR; srcGPR[k] != 0; k++) StdStreams.vrb.print(srcGPR[k] + ","); 
			StdStreams.vrb.println();
			StdStreams.vrb.print("srcGPRcount = ");
			for (int n = paramStartGPR; srcGPR[n] != 0; n++) StdStreams.vrb.print(srcGPRcount[n] + ","); 
			StdStreams.vrb.println();
		}

		while (srcGPR[i] != 0) srcGPRcount[srcGPR[i++]]++;
		i = paramStartFPR;
		while (srcFPR[i] != 0) srcFPRcount[srcFPR[i++]]++;
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
			if (srcGPR[i] == i) {
//				if (dbg) StdStreams.vrb.println("move to itself");
				if (i <= paramEndGPR) srcGPRcount[i]--;
				else srcGPRcount[i]--;	// copy to stack
			}
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
		i = paramStartFPR;
		while (srcFPR[i] != 0) {
			if (srcFPR[i] == i) {
				if (i <= paramEndFPR) srcFPRcount[i]--;
				else srcFPRcount[i]--;	// copy to stack
			}
			i++;
		}

		// move registers 
		boolean done = false;
		while (!done) {
			i = paramStartGPR; done = true;
			while (srcGPR[i] != 0) {
				if (i > paramEndGPR) {	// copy to stack
					if (srcGPRcount[i] >= 0) { // check if not done yet
						if (dbg) StdStreams.vrb.println("\tGPR: parameter " + (i-paramStartGPR) + " from register " + srcGPR[i] + " to stack slot");
						createIrSrAsimm(ppcStw, srcGPR[i], stackPtr, paramOffset + offset);
						offset += 4;
						srcGPRcount[i]=-1; srcGPRcount[srcGPR[i]]--; 
						done = false;
					}
				} else {
					if (srcGPRcount[i] == 0) { // check if register no longer used for parameter
						if (dbg) StdStreams.vrb.println("\tGPR: parameter " + (i-paramStartGPR) + " from register " + srcGPR[i] + " to " + i);
						createIrArSrB(ppcOr, i, srcGPR[i], srcGPR[i]);
						srcGPRcount[i]--; srcGPRcount[srcGPR[i]]--; 
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
						createIrSrAd(ppcStfd, srcFPR[i], stackPtr, paramOffset + offset);
						offset += 8;
						srcFPRcount[i]=-1; srcFPRcount[srcFPR[i]]--; 
						done = false;
					}
				} else {
					if (srcFPRcount[i] == 0) { // check if register no longer used for parameter
						if (dbg) StdStreams.vrb.println("\tFPR: parameter " + (i-paramStartFPR) + " from register " + srcFPR[i] + " to " + i);
						createIrDrB(ppcFmr, i, srcFPR[i]);
						srcFPRcount[i]--; srcFPRcount[srcFPR[i]]--; 
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
					createIrArSrB(ppcOr, 0, srcGPR[i], srcGPR[i]);
					srcGPRcount[srcGPR[i]]--;
					done = false;
				}
				boolean done1 = false;
				while (!done1) {
					int k = paramStartGPR; done1 = true;
					while (srcGPR[k] != 0) {
						if (srcGPRcount[k] == 0 && k != src) {
							createIrArSrB(ppcOr, k, srcGPR[k], srcGPR[k]);
							srcGPRcount[k]--; srcGPRcount[srcGPR[k]]--; 
							done1 = false;
						}
						k++; 
					}
				}
				if (src != 0) {
					createIrArSrB(ppcOr, src, 0, 0);
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
					createIrDrB(ppcFmr, 0, srcFPR[i]);
					srcFPRcount[srcFPR[i]]--;
					done = false;
				}
				boolean done1 = false;
				while (!done1) {
					int k = paramStartFPR; done1 = true;
					while (srcFPR[k] != 0) {
						if (srcFPRcount[k] == 0 && k != src) {
							createIrDrB(ppcFmr, k, srcFPR[k]);
							srcFPRcount[k]--; srcFPRcount[srcFPR[k]]--; 
							done1 = false;
						}
						k++; 
					}
				}
				if (src != 0) {
					createIrDrB(ppcFmr, src, 0);
					srcFPRcount[src]--;
				}
				i++;
			}
		}
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
					createIrArSrB(ppcOr, i, srcGPR[i], srcGPR[i]);
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
					createIrArSrB(ppcOr, 0, srcGPR[i], srcGPR[i]);
					srcGPRcount[srcGPR[i]]--;
					done = false;
				}
				boolean done1 = false;
				while (!done1) {
					int k = topGPR; done1 = true;
					while (srcGPR[k] != 0) {
						if (srcGPRcount[k] == 0 && k != src) {
							createIrArSrB(ppcOr, k, srcGPR[k], srcGPR[k]);
							srcGPRcount[k]--; srcGPRcount[srcGPR[k]]--; 
							done1 = false;
						}
						k--; 
					}
				}
				if (src != 0) {
					createIrArSrB(ppcOr, src, 0, 0);
					srcGPRcount[src]--;
				}
				i--;
			}
		}
	}	

	private static int getInt(byte[] bytes, int index){
		return (((bytes[index]<<8) | (bytes[index+1]&0xFF))<<8 | (bytes[index+2]&0xFF))<<8 | (bytes[index+3]&0xFF);
	}

	private void createIrArS(int opCode, int rA, int rS) {
		instructions[iCount] = opCode | (rS << 21) | (rA << 16);
		incInstructionNum();
	}

	private void createIrD(int opCode, int rD) {
		instructions[iCount] = opCode | (rD << 21);
		incInstructionNum();
	}

	private void createIrS(int opCode, int rD) {
		instructions[iCount] = opCode | (rD << 21);
		incInstructionNum();
	}

	private void createIrDrArB(int opCode, int rD, int rA, int rB) {
		instructions[iCount] = opCode | (rD << 21) | (rA << 16) | (rB << 11);
		incInstructionNum();
	}

	private void createIrDrArC(int opCode, int rD, int rA, int rC) {
		instructions[iCount] = opCode | (rD << 21) | (rA << 16) | (rC << 6);
		incInstructionNum();
	}

	private void createIrDrArCrB(int opCode, int rD, int rA, int rC, int rB) {
		instructions[iCount] = opCode | (rD << 21) | (rA << 16) | (rC << 6) | (rB << 11);
		incInstructionNum();
	}
	
	private void createIrSrArB(int opCode, int rS, int rA, int rB) {
		instructions[iCount] = opCode | (rS << 21) | (rA << 16) | (rB << 11);
		incInstructionNum();
	}

	private void createIrArSrB(int opCode, int rA, int rS, int rB) {
		if ((opCode == ppcOr) && (rA == rS) && (rA == rB)) return; 	// lr x,x makes no sense
		instructions[iCount] = opCode | (rS << 21) | (rA << 16) | (rB << 11);
		incInstructionNum();
	}

	private void createIrDrAd(int opCode, int rD, int rA, int d) {
		instructions[iCount] = opCode | (rD << 21) | (rA << 16) | (d  & 0xffff);
		incInstructionNum();
	}

	private void createIrDrAsimm(int opCode, int rD, int rA, int simm) {
		instructions[iCount] = opCode | (rD << 21) | (rA << 16) | (simm  & 0xffff);
		incInstructionNum();
	}

	private void createIrArSuimm(int opCode, int rA, int rS, int uimm) {
		instructions[iCount] = opCode | (rA << 16) | (rS << 21) | (uimm  & 0xffff);
		incInstructionNum();
	}

	private void createIrSrAsimm(int opCode, int rS, int rA, int simm) {
		instructions[iCount] = opCode | (rS << 21) | (rA << 16) | (simm  & 0xffff);
		incInstructionNum();
	}

	private void createIrSrAd(int opCode, int rS, int rA, int d) {
		instructions[iCount] = opCode | (rS << 21) | (rA << 16) | (d  & 0xffff);
		incInstructionNum();
	}

	private void createIrArSSH(int opCode, int rA, int rS, int SH) {
		instructions[iCount] = opCode | (rS << 21) | (rA << 16) | (SH << 11);
		incInstructionNum();
	}

	private void createIrArSSHMBME(int opCode, int rA, int rS, int SH, int MB, int ME) {
		instructions[iCount] = opCode | (rS << 21) | (rA << 16) | (SH << 11) | (MB << 6) | (ME << 1);
		incInstructionNum();
	}

	private void createIrArSrBMBME(int opCode, int rA, int rS, int rB, int MB, int ME) {
		instructions[iCount] = opCode | (rS << 21) | (rA << 16) | (rB << 11) | (MB << 6) | (ME << 1);
		incInstructionNum();
	}

	private void createItrap(int opCode, int TO, int rA, int rB) {
		instructions[iCount] = opCode | (TO << 21) | (rA << 16) | (rB << 11);
		incInstructionNum();
	}

	private void createItrapSimm(int opCode, int TO, int rA, int imm) {
		instructions[iCount] = opCode | (TO << 21) | (rA << 16) | (imm & 0xffff);
		incInstructionNum();
	}

	private void createIli(int opCode, int LI, boolean link) {
		instructions[iCount] = opCode | (LI << 2 | (link ? 1 : 0));
		incInstructionNum();
	}

	private void createIBOBIBD(int opCode, int BO, int BI, int BD) {
		instructions[iCount] = opCode | (BO << 21) | (BI << 16) | ((BD << 2)&0xffff);
		incInstructionNum();
	}

	private void createIBOBILK(int opCode, int BO, int BI, boolean link) {
		instructions[iCount] = opCode | (BO << 21) | (BI << 16) | (link?1:0);
		incInstructionNum();
	}

	private void createICRFrArB(int opCode, int crfD, int rA, int rB) {
		instructions[iCount] = opCode | (crfD << 23) | (rA << 16) | (rB << 11);
		incInstructionNum();
	}

	private void createICRFrAsimm(int opCode, int crfD, int rA, int simm) {
		instructions[iCount] = opCode | (crfD << 23) | (rA << 16) | (simm & 0xffff);
		incInstructionNum();
	}

	private void createIcrbDcrbAcrbB(int opCode, int crbD, int crbA, int crbB) {
		instructions[iCount] = opCode | (crbD << 21) | (crbA << 16) | (crbB << 11);
		incInstructionNum();
	}
	
	private void createICRMrS(int opCode, int CRM, int rS) {
		instructions[iCount] = opCode | (rS << 21) | (CRM << 12);
		incInstructionNum();
	}
	
	private void createIFMrB(int opCode, int FM, int rB) {
		instructions[iCount] = opCode | (FM << 17) | (rB << 11);
		incInstructionNum();
	}
	
	private void createIrDrA(int opCode, int rD, int rA) {
		instructions[iCount] = opCode | (rD << 21) | (rA << 16);
		incInstructionNum();
	}

	private void createIrDrB(int opCode, int rD, int rB) {
		if ((opCode == ppcFmr) && (rD == rB)) return; 	// fmr x,x makes no sense
		instructions[iCount] = opCode | (rD << 21) | (rB << 11);
		incInstructionNum();
	}

	private void createIrSspr(int opCode, int spr, int rS) {
		int temp = ((spr & 0x1F) << 5) | ((spr & 0x3E0) >> 5);
		if (spr == 268 || spr == 269) opCode = ppcMftb;
		instructions[iCount] = opCode | (temp << 11) | (rS << 21);
		incInstructionNum();
	}

	private void createIrfi(int opCode) {
		instructions[iCount] = opCode;
		incInstructionNum();
	}

	private void createIpat(int pat) {
		instructions[iCount] = pat;
		incInstructionNum();
	}

	private void loadConstant(int reg, int val) {
		assert(reg != 0);
		int low = val & 0xffff;
		int high = (val >> 16) & 0xffff;
		if ((low >> 15) == 0) {
			if (low != 0 && high != 0) {
				createIrDrAsimm(ppcAddi, reg, 0, low);
				createIrDrAsimm(ppcAddis, reg, reg, high);
			} else if (low == 0 && high != 0) {
				createIrDrAsimm(ppcAddis, reg, 0, high);		
			} else if (low != 0 && high == 0) {
				createIrDrAsimm(ppcAddi, reg, 0, low);
			} else createIrDrAsimm(ppcAddi, reg, 0, 0);
		} else {
			createIrDrAsimm(ppcAddi, reg, 0, low);
			if (((high + 1) & 0xffff) != 0) createIrDrAsimm(ppcAddis, reg, reg, high + 1);
		}
	}
	
	private void loadConstantAndFixup(int reg, Item item) {
		assert(reg != 0);
		if (lastFixup < 0 || lastFixup > 32768) {ErrorReporter.reporter.error(602); return;}
		createIrDrAsimm(ppcAddi, reg, 0, lastFixup);
		createIrDrAsimm(ppcAddis, reg, reg, 0);
		lastFixup = iCount - 2;
		fixups[fCount] = item;
		fCount++;
		int len = fixups.length;
		if (fCount == len) {
			Item[] newFixups = new Item[2 * len];
			for (int k = 0; k < len; k++)
				newFixups[k] = fixups[k];
			fixups = newFixups;
		}		
	}
	
	public void doFixups() {
		int currInstr = lastFixup;
		int currFixup = fCount - 1;
		while (currFixup >= 0) {
			Item item = fixups[currFixup];
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
			int nextInstr = instructions[currInstr] & 0xffff;
			instructions[currInstr] = (instructions[currInstr] & 0xffff0000) | (low & 0xffff);
			instructions[currInstr+1] = (instructions[currInstr+1] & 0xffff0000) | (high & 0xffff);
			currInstr = nextInstr;
			currFixup--;
		}
		// fix addresses of exception information
		if (ssa == null) return;	// compiler specific subroutines have no unwinding or exception table
		if ((ssa.cfg.method.accAndPropFlags & (1 << dpfExcHnd)) != 0) return;	// exception methods have no unwinding or exception table
		if (dbg) StdStreams.vrb.print("\n\tFixup of exception table for method: " + ssa.cfg.method.owner.name + "." + ssa.cfg.method.name +  ssa.cfg.method.methDescriptor + "\n");		
		currInstr = excTabCount;
		int count = 0;
		while (instructions[currInstr] != 0xffffffff) {
			SSAInstruction ssaInstr = ssa.searchBca(instructions[currInstr]);	
			assert ssaInstr != null;
			instructions[currInstr++] = ssa.cfg.method.address + ssaInstr.machineCodeOffset * 4;	// start
			
			ssaInstr = ssa.searchBca(instructions[currInstr]);	
			assert ssaInstr != null;
			instructions[currInstr++] = ssa.cfg.method.address + ssaInstr.machineCodeOffset * 4;	// end
			
			ExceptionTabEntry[] tab = ssa.cfg.method.exceptionTab;
			assert tab != null;
			ExceptionTabEntry entry = tab[count];
			assert entry != null;
			if (entry.catchType != null) instructions[currInstr++] = entry.catchType.address;	// type 
			else instructions[currInstr++] = 0;	// finally 
			
			ssaInstr = ssa.searchBca(instructions[currInstr] + 1);	// add 1, as first store is ommitted	
			assert ssaInstr != null;
			instructions[currInstr++] = ssa.cfg.method.address + ssaInstr.machineCodeOffset * 4;	// handler
			count++;
		}
	}

	private void incInstructionNum() {
		iCount++;
		int len = instructions.length;
		if (iCount == len) {
			int[] newInstructions = new int[2 * len];
			for (int k = 0; k < len; k++)
				newInstructions[k] = instructions[k];
			instructions = newInstructions;
		}
	}

	private void insertProlog() {
		iCount = 0;
		createIrSrAsimm(ppcStwu, stackPtr, stackPtr, -stackSize);
		createIrSspr(ppcMfspr, LR, 0);
		createIrSrAsimm(ppcStw, 0, stackPtr, LRoffset);
		if (nofNonVolGPR > 0) {
			createIrSrAd(ppcStmw, nofGPR-nofNonVolGPR, stackPtr, GPRoffset);
		}
		if (enFloatsInExc) {
			createIrD(ppcMfmsr, 0);
			createIrArSuimm(ppcOri, 0, 0, 0x2000);
			createIrS(ppcMtmsr, 0);
			createIrS(ppcIsync, 0);	// must context synchronize after setting of FP bit
		}
		int offset = FPRoffset;
		if (nofNonVolFPR > 0) {
			for (int i = 0; i < nofNonVolFPR; i++) {
				createIrSrAd(ppcStfd, topFPR-i, stackPtr, offset);
				offset += 8;
			}
		}
		if (enFloatsInExc) {
			for (int i = 0; i < nonVolStartFPR; i++) {
				createIrSrAd(ppcStfd, i, stackPtr, offset);
				offset += 8;
			}
			createIrD(ppcMffs, 0);
			createIrSrAd(ppcStfd, 0, stackPtr, offset);
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
			if (moveGPRsrc[i]+paramStartGPR <= paramEndGPR) // copy from parameter register
				createIrArSrB(ppcOr, moveGPRdst[i], moveGPRsrc[i]+paramStartGPR, moveGPRsrc[i]+paramStartGPR);
			else { // copy from stack slot
				if (dbg) StdStreams.vrb.println("Prolog: copy parameter " + moveGPRsrc[i] + " from stack slot into GPR" + moveGPRdst[i]);
				createIrDrAd(ppcLwz, moveGPRdst[i], stackPtr, stackSize + paramOffset + offset);
				offset += 4;
			}
		}
		for (int i = 0; i < nofMoveFPR; i++) {
			if (moveFPRsrc[i]+paramStartFPR <= paramEndFPR) // copy from parameter register
				createIrDrB(ppcFmr, moveFPRdst[i], moveFPRsrc[i]+paramStartFPR);
			else { // copy from stack slot
				if (dbg) StdStreams.vrb.println("Prolog: copy parameter " + moveFPRsrc[i] + " from stack slot into FPR" + moveFPRdst[i]);
				createIrDrAd(ppcLfd, moveFPRdst[i], stackPtr, stackSize + paramOffset + offset);
				offset += 8;
			}
		}
	}

	private void insertEpilog(int stackSize) {
		int epilogStart = iCount;
		int offset = GPRoffset - 8;
		if (enFloatsInExc) {
			createIrDrAd(ppcLfd, 0, stackPtr, offset);
			createIFMrB(ppcMtfsf, 0xff, 0);
			offset -= 8;
			for (int i = nonVolStartFPR - 1; i >= 0; i--) {
				createIrDrAd(ppcLfd, i, stackPtr, offset);
				offset -= 8;
			}
		}
		if (nofNonVolFPR > 0) {
			for (int i = nofNonVolFPR - 1; i >= 0; i--) {
				createIrDrAd(ppcLfd, topFPR-i, stackPtr, offset);
				offset -= 8;
			}
		}
		if (nofNonVolGPR > 0)
			createIrDrAd(ppcLmw, nofGPR - nofNonVolGPR, stackPtr, GPRoffset);
		createIrDrAd(ppcLwz, 0, stackPtr, LRoffset);
		createIrSspr(ppcMtspr, LR, 0);
		createIrDrAsimm(ppcAddi, stackPtr, stackPtr, stackSize);
		createIBOBILK(ppcBclr, BOalways, 0, false);
		createIpat((-(iCount-epilogStart)*4) & 0xff);
		excTabCount = iCount;
		ExceptionTabEntry[] tab = ssa.cfg.method.exceptionTab;
		if (tab != null) {
			for (int i = 0; i < tab.length; i++) {
				ExceptionTabEntry entry = tab[i];
				createIpat(entry.startPc);
				createIpat(entry.endPc);
				if (entry.catchType != null) createIpat(entry.catchType.address); else createIpat(0);
				createIpat(entry.handlerPc);
			}
		}
		createIpat(0xffffffff);
	}

	private void insertPrologException() {
		iCount = 0;
		createIrSrAsimm(ppcStwu, stackPtr, stackPtr, -stackSize);
		createIrSrAsimm(ppcStw, 0, stackPtr, GPRoffset);
		createIrSspr(ppcMfspr, SRR0, 0);
		createIrSrAsimm(ppcStw, 0, stackPtr, SRR0offset);
		createIrSspr(ppcMfspr, SRR1, 0);
		createIrSrAsimm(ppcStw, 0, stackPtr, SRR1offset);
		createIrSspr(ppcMtspr, EID, 0);
		createIrSspr(ppcMfspr, LR, 0);
		createIrSrAsimm(ppcStw, 0, stackPtr, LRoffset);
		createIrSspr(ppcMfspr, XER, 0);
		createIrSrAsimm(ppcStw, 0, stackPtr, XERoffset);
		createIrSspr(ppcMfspr, CTR, 0);
		createIrSrAsimm(ppcStw, 0, stackPtr, CTRoffset);
		createIrD(ppcMfcr, 0);
		createIrSrAsimm(ppcStw, 0, stackPtr, CRoffset);
		createIrSrAd(ppcStmw, 2, stackPtr, GPRoffset + 8);
		if (enFloatsInExc) {
			createIrD(ppcMfmsr, 0);
			createIrArSuimm(ppcOri, 0, 0, 0x2000);
			createIrS(ppcMtmsr, 0);
			createIrS(ppcIsync, 0);	// must context synchronize after setting of FP bit
			int offset = FPRoffset;
			if (nofNonVolFPR > 0) {
				for (int i = 0; i < nofNonVolFPR; i++) {
					createIrSrAd(ppcStfd, topFPR-i, stackPtr, offset);
					offset += 8;
				}
			}
			for (int i = 0; i < nonVolStartFPR; i++) {
				createIrSrAd(ppcStfd, i, stackPtr, offset);
				offset += 8;
			}
			createIrD(ppcMffs, 0);
			createIrSrAd(ppcStfd, 0, stackPtr, offset);
		}
	}

	private void insertEpilogException(int stackSize) {
		int offset = GPRoffset - 8;
		if (enFloatsInExc) {
			createIrDrAd(ppcLfd, 0, stackPtr, offset);
			createIFMrB(ppcMtfsf, 0xff, 0);
			offset -= 8;
			for (int i = nonVolStartFPR - 1; i >= 0; i--) {
				createIrDrAd(ppcLfd, i, stackPtr, offset);
				offset -= 8;
			}
		}
		if (nofNonVolFPR > 0) {
			for (int i = nofNonVolFPR - 1; i >= 0; i--) {
				createIrDrAd(ppcLfd, topFPR-i, stackPtr, offset);
				offset -= 8;
			}
		}
		createIrDrAd(ppcLmw, 2, stackPtr, GPRoffset + 8);
		createIrDrAd(ppcLwz, 0, stackPtr, CRoffset);
		createICRMrS(ppcMtcrf, 0xff, 0);
		createIrDrAd(ppcLwz, 0, stackPtr, CTRoffset);
		createIrSspr(ppcMtspr, CTR, 0);
		createIrDrAd(ppcLwz, 0, stackPtr, XERoffset);
		createIrSspr(ppcMtspr, XER, 0);
		createIrDrAd(ppcLwz, 0, stackPtr, LRoffset);
		createIrSspr(ppcMtspr, LR, 0);
		createIrDrAd(ppcLwz, 0, stackPtr, SRR1offset);
		createIrSspr(ppcMtspr, SRR1, 0);
		createIrDrAd(ppcLwz, 0, stackPtr, SRR0offset);
		createIrSspr(ppcMtspr, SRR0, 0);
		createIrDrAd(ppcLwz, 0, stackPtr, GPRoffset);
		createIrDrAsimm(ppcAddi, stackPtr, stackPtr, stackSize);
		createIrfi(ppcRfi);
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		if (ssa != null)	// compiler specific subroutines have no ssa
			sb.append("Code for Method: " + ssa.cfg.method.owner.name + "." + ssa.cfg.method.name +  ssa.cfg.method.methDescriptor + "\n");
		int i;
		for (i = 0; i < iCount; i++) {
			if ((instructions[i] & 0xffffff00) == 0) break;
			sb.append("\t" + String.format("%08X", instructions[i]));
			sb.append("\t[0x");
			sb.append(Integer.toHexString(i * 4));
			sb.append("]\t");
			sb.append(InstructionDecoder.getMnemonic(instructions[i]));
			int opcode = (instructions[i] & 0xFC000000) >>> (31 - 5);
			if (opcode == 0x10) {
				int BD = (short) (instructions[i] & 0xFFFC);
				sb.append(", [0x" + Integer.toHexString(BD + 4 * i) + "]\t");
			} else if (opcode == 0x12) {
				int li = (instructions[i] & 0x3FFFFFC) << 6 >> 6;
				sb.append(", [0x" + Integer.toHexString(li + 4 * i) + "]\t");
			}
			sb.append("\n");
		}
		if (ssa != null) {	// compiler specific subroutines have no ssa
			if ((ssa.cfg.method.accAndPropFlags & (1 << dpfExcHnd)) != 0) return sb.toString();	// exception methods have no unwinding or exception table 
		
			sb.append("\t" + String.format("%08X", instructions[i]));
			sb.append("\t[0x");	sb.append(Integer.toHexString(i * 4)); sb.append("]\t");
			sb.append((byte)instructions[i++]); sb.append("  (offset to unwind code)\n");
			while (instructions[i] != 0xffffffff) {
				sb.append("\t" + String.format("%08X", instructions[i]));
				sb.append("\t[0x");	sb.append(Integer.toHexString(i * 4)); sb.append("]\t");
				sb.append("0x" + Integer.toHexString(instructions[i++])); sb.append("  (start address of try)\n");
				sb.append("\t" + String.format("%08X", instructions[i]));
				sb.append("\t[0x");	sb.append(Integer.toHexString(i * 4)); sb.append("]\t");
				sb.append("0x" + Integer.toHexString(instructions[i++])); sb.append("  (end address of try)\n");
				sb.append("\t" + String.format("%08X", instructions[i]));
				sb.append("\t[0x");	sb.append(Integer.toHexString(i * 4)); sb.append("]\t");
				sb.append("0x" + Integer.toHexString(instructions[i++])); sb.append("  (address of catch type)\n");
				sb.append("\t" + String.format("%08X", instructions[i]));
				sb.append("\t[0x");	sb.append(Integer.toHexString(i * 4)); sb.append("]\t");
				sb.append("0x" + Integer.toHexString(instructions[i++])); sb.append("  (address of catch)\n");
			}
			sb.append("\t" + String.format("%08X", instructions[i]));
			sb.append("\t[0x");	sb.append(Integer.toHexString(i * 4)); sb.append("]\t");
			sb.append("(end of method)\n");
		}
		return sb.toString();
	}

	public static void generateCompSpecSubroutines() {
		Method m = Method.getCompSpecSubroutine("longToDouble");
		// long is passed in r30/r31, r29 can be used for general purposes
		// faux1 and faux2 are used as general purpose FPR's, result is passed in f0 
		if (m != null) { 
			m.machineCode = new CodeGen();
			m.machineCode.instructions = new int[22];
			m.machineCode.fixups = new Item[defaultNofFixup];
			m.machineCode.iCount = 0;

			Item item = int2floatConst1;	// ref to 2^52+2^31;					
			m.machineCode.createIrDrAsimm(ppcAddis, 0, 0, 0x4330);	// preload 2^52
			m.machineCode.createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset);
			m.machineCode.createIrArSuimm(ppcXoris, 0, 30, 0x8000);	// op0.regLong
			m.machineCode.createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset+4);
			m.machineCode.loadConstantAndFixup(29, item); // r29 as auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, faux1, 29, 0);	// r29 as auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, 0, stackPtr, tempStorageOffset);
			m.machineCode.createIrDrArB(ppcFsub, faux2, 0, faux1);
			m.machineCode.createIrSrAd(ppcStw, 31, stackPtr, tempStorageOffset+4); // op0.reg
			item = int2floatConst3;	// ref to 2^52;
			m.machineCode.loadConstantAndFixup(29, item); // r29 as auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, faux1, 29, 0);	// r29 as auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, 0, stackPtr, tempStorageOffset);
			m.machineCode.createIrDrArB(ppcFsub, faux1, 0, faux1); 					
			item = int2floatConst2;	// ref to 2^32;
			m.machineCode.loadConstantAndFixup(29, item); // r29 as auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, 0, 29, 0); 	// r29 as auxGPR1
			m.machineCode.createIrDrArCrB(ppcFmadd, 0, faux2, 0, faux1);
			m.machineCode.createIBOBILK(ppcBclr, BOalways, 0, false);
		}

		m = Method.getCompSpecSubroutine("doubleToLong");
		// double is passed in f0, r29, r30 and r31 can be used for general purposes
		// result is returned in r0/r28 
		if (m != null) { 
			m.machineCode = new CodeGen();
			m.machineCode.instructions = new int[55];
			m.machineCode.iCount = 0;

			m.machineCode.createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset); // op0.reg
			m.machineCode.createIrDrAd(ppcLwz, 29, stackPtr, tempStorageOffset); // r29 as auxGPR1
			m.machineCode.createIrDrAd(ppcLwz, 28, stackPtr, tempStorageOffset+4); // r28 as res.reg
			m.machineCode.createIrArSSHMBME(ppcRlwinm, 30, 29, 12, 21, 31); // r29 as auxGPR1, r30 as auxGPR2	
			m.machineCode.createIrDrAsimm(ppcSubfic, 30, 30, 1075);	// r30 as auxGPR2
			m.machineCode.createICRFrAsimm(ppcCmpi, CRF2, 29, 0); // r29 as auxGPR1, check if negative
			m.machineCode.createIrDrAsimm(ppcAddis, 0, 0, 0xfff0);	
			m.machineCode.createIrArSrB(ppcAndc, 29, 29, 0); // r29 as auxGPR1	
			m.machineCode.createIrArSuimm(ppcOris, 29, 29, 0x10); // r29 as auxGPR1	
			m.machineCode.createICRFrAsimm(ppcCmpi, CRF0, 30, 52);	// r30 as auxGPR2
			int label1jmp1 = m.machineCode.iCount;
			m.machineCode.createIBOBIBD(ppcBc, BOfalse, 4*CRF0+GT, 0);	// jump to label 1
			// double is < 1
			m.machineCode.createIrDrAsimm(ppcAddi, 0, 0, 0); // r0 as res.regLong
			m.machineCode.createIrDrAsimm(ppcAddi, 28, 0, 0); // r28 as res.reg 
			m.machineCode.createIBOBILK(ppcBclr, BOalways, 0, false); // return
			//label 1
			correctJmpAddr(m.machineCode.instructions, label1jmp1, m.machineCode.iCount);
			m.machineCode.createICRFrAsimm(ppcCmpi, CRF0, 30, 0); // r30 as auxGPR2
			int label2jmp1 = m.machineCode.iCount;
			m.machineCode.createIBOBIBD(ppcBc, BOtrue, 4*CRF0+LT, 0);	// jump to label 2
			m.machineCode.createIrDrAsimm(ppcSubfic, 0, 30, 32);// r30 as auxGPR2
			m.machineCode.createIrArSrB(ppcSrw, 28, 28, 30); // r28 as res.reg, r30 as auxGPR2
			m.machineCode.createIrArSrB(ppcSlw, 0, 29, 0); // r29 as auxGPR1
			m.machineCode.createIrArSrB(ppcOr, 28, 28, 0); // r28 as res.reg
			m.machineCode.createIrDrAsimm(ppcAddi, 0, 30, -32);	// r30 as auxGPR2
			m.machineCode.createIrArSrB(ppcSrw, 0, 29, 0); // r29 as auxGPR1
			m.machineCode.createIrArSrB(ppcOr, 28, 28, 0); // r28 as res.reg
			m.machineCode.createIrArSrB(ppcSrw, 0, 29, 30); // r0 as res.regLong, r29 as auxGPR1, r30 as auxGPR2
			int label5jmp1 = m.machineCode.iCount;
			m.machineCode.createIBOBIBD(ppcBc, BOalways, 0, 0);	// jump to label 5
			//label 2
			correctJmpAddr(m.machineCode.instructions, label2jmp1, m.machineCode.iCount);
			m.machineCode.createIrDrA(ppcNeg, 30, 30); // r30 as auxGPR2
			m.machineCode.createICRFrAsimm(ppcCmpi, CRF0, 30, 11); // r30 as auxGPR2
			int label4jmp1 = m.machineCode.iCount;
			m.machineCode.createIBOBIBD(ppcBc, BOtrue, 4*CRF0+LT, 0);	// jump to label 4
			int label3jmp1 = m.machineCode.iCount;
			m.machineCode.createIBOBIBD(ppcBc, BOtrue, 4*CRF2+LT, 0);	// jump to label 3
			m.machineCode.createIrDrAsimm(ppcAddi, 28, 0, -1); // r28 as res.reg
			m.machineCode.createIrDrAsimm(ppcAddis, 0, 0, 0x7fff); // r0 as res.regLong
			m.machineCode.createIrArSuimm(ppcOri, 0, 0, 0xffff); // r0 as res.regLong
			m.machineCode.createIBOBILK(ppcBclr, BOalways, 0, false); // return
			//label 3
			correctJmpAddr(m.machineCode.instructions, label3jmp1, m.machineCode.iCount);
			m.machineCode.createIrDrAsimm(ppcAddi, 28, 0, 0); // r28 as res.reg
			m.machineCode.createIrDrAsimm(ppcAddis, 0, 0, 0x8000); // r0 as res.regLong
			m.machineCode.createIBOBILK(ppcBclr, BOalways, 0, false); // return
			//label 4
			correctJmpAddr(m.machineCode.instructions, label4jmp1, m.machineCode.iCount);
			m.machineCode.createIrDrAsimm(ppcSubfic, 0, 30, 32); // r30 as auxGPR2
			m.machineCode.createIrArSrB(ppcSlw, 31, 29, 30); // r31 as auxGPR3, r29 as auxGPR1, r30 as auxGPR2
			m.machineCode.createIrArSrB(ppcSrw, 0, 28, 0); // r28 as res.reg
			m.machineCode.createIrArSrB(ppcOr, 31, 31, 0); // r31 as auxGPR3
			m.machineCode.createIrDrAsimm(ppcAddi, 0, 30, -32); // r30 as auxGPR2
			m.machineCode.createIrArSrB(ppcSlw, 0, 28, 0); // r28 as res.reg
			m.machineCode.createIrArSrB(ppcOr, 0, 31, 0); // r0 as res.regLong, r31 as auxGPR3
			m.machineCode.createIrArSrB(ppcSlw, 28, 28, 30); // r28 as res.reg, r30 as auxGPR2
			//label 5
			correctJmpAddr(m.machineCode.instructions, label5jmp1, m.machineCode.iCount);
			m.machineCode.createIBOBILK(ppcBclr, BOfalse, 4*CRF2+LT, false); // return
			m.machineCode.createIrDrAsimm(ppcSubfic, 28, 28, 0); // r28 as res.reg
			m.machineCode.createIrDrA(ppcSubfze, 0, 0); // r0 as res.regLong
			m.machineCode.createIBOBILK(ppcBclr, BOalways, 0, false); // return
		}

		m = Method.getCompSpecSubroutine("divLong");
		// long op0 is passed in r30/r31 (dividend), long op1 is passed in r28/r29 (divisor) 
		// r27 can be used for general purposes
		// faux1, faux2, faux3 are used as general purpose FPR's 
		// result is returned in r0/r26 
		if (m != null) { 
			m.machineCode = new CodeGen();
			m.machineCode.instructions = new int[120];
			m.machineCode.fixups = new Item[defaultNofFixup];
			m.machineCode.iCount = 0;

			m.machineCode.createICRFrAsimm(ppcCmpi, CRF0, 28, 0);	// test if divisor < 2^32, op1.regLong
			int label1jmp1 = m.machineCode.iCount;
			m.machineCode.createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 0);	// jump to label 1
			m.machineCode.createICRFrAsimm(ppcCmpli, CRF0, 29, 0x7fff);	// test if divisor < 2^15, op1.reg
			int label1jmp2 = m.machineCode.iCount;
			m.machineCode.createIBOBIBD(ppcBc, BOfalse, 4*CRF0+LT, 0);	// jump to label 1
			
			// divisor is small, use GPR's, op1.regLong (r28) is 0 and can be used as aux register
			m.machineCode.createICRFrAsimm(ppcCmpi, CRF2, 30, 0); // is dividend negative?, op0.regLong
			m.machineCode.createIrDrArB(ppcDivw, 28, 30, 29); // auxGPR2, op0.regLong, op1.reg
			m.machineCode.createIrDrArB(ppcMullw, 0, 29, 28); // op1.reg, auxGPR2
			m.machineCode.createIrDrArB(ppcSubf, 0, 0, 30); // op0.regLong
			m.machineCode.createICRFrAsimm(ppcCmpi, CRF0, 0, 0); // is remainder negative?
			m.machineCode.createIBOBIBD(ppcBc, BOfalse, 4*CRF0+LT, 3);	
			m.machineCode.createIrDrArB(ppcAdd, 0, 0, 29);	// add divisor, op1.reg
			m.machineCode.createIrDrAsimm(ppcAddi, 28, 28, -1); // auxGPR2 	
			m.machineCode.createIrArSrB(ppcOr, 30, 28, 28); // (r30)res.regLong, auxGPR2
			m.machineCode.createIrArSSHMBME(ppcRlwinm, 27, 0, 16, 0, 15); // auxGPR1
			m.machineCode.createIrArSSHMBME(ppcRlwimi, 27, 31, 16, 16, 31); // auxGPR1, op0.reg
			m.machineCode.createIrDrArB(ppcDivwu, 28, 27, 29); // auxGPR2, auxGPR1, op1.reg
			m.machineCode.createIrDrArB(ppcMullw, 0, 29, 28); // op1.reg, auxGPR2
			m.machineCode.createIrDrArB(ppcSubf, 0, 0, 27); // auxGPR1
			m.machineCode.createIrArSSHMBME(ppcRlwinm, 27, 0, 16, 0, 15); // auxGPR1
			m.machineCode.createIrArSSHMBME(ppcRlwimi, 27, 31, 0, 16, 31); // auxGPR1, op0.reg 
			m.machineCode.createIrDrArB(ppcDivwu, 0, 27, 29); // auxGPR1, op1.reg
			m.machineCode.createIrArSSHMBME(ppcRlwinm, 26, 28, 16, 0, 15); // res.reg, auxGPR2
			m.machineCode.createIrArSSHMBME(ppcRlwimi, 26, 0, 0, 16, 31); // res.reg
			m.machineCode.createIrDrArB(ppcMullw, 0, 29, 0); // op1.reg
			m.machineCode.createIrDrArB(ppcSubf, 0, 0, 27); // auxGPR1
			m.machineCode.createICRFrAsimm(ppcCmpi, CRF0, 0, 0); // is remainder > 0?
			m.machineCode.createIcrbDcrbAcrbB(ppcCrand, CRF0EQ, CRF0GT, CRF2LT);
			m.machineCode.createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 2);	
			m.machineCode.createIrDrAsimm(ppcAddi, 26, 26, 1); // res.reg	
			m.machineCode.createIBOBIBD(ppcBc, BOtrue, 4*CRF1+GT, 3);	// was divisor negative? CRF1 set before subroutine
			m.machineCode.createIrDrAsimm(ppcSubfic, 26, 26, 0); // negate result, res.reg
			m.machineCode.createIrDrA(ppcSubfze, 30, 30);	// res.regLong
			m.machineCode.createIrArSrB(ppcOr, 0, 30, 30); // copy res.regLong
			m.machineCode.createIBOBILK(ppcBclr, BOalways, 0, false); // return

			//label 1, divisor is not small, use FPR's
			correctJmpAddr(m.machineCode.instructions, label1jmp1, m.machineCode.iCount);
			correctJmpAddr(m.machineCode.instructions, label1jmp2, m.machineCode.iCount);
			Item item = int2floatConst1;	// ref to 2^52+2^31;					
			m.machineCode.createIrDrAsimm(ppcAddis, 0, 0, 0x4330);	// preload 2^52
			m.machineCode.createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset);
			m.machineCode.createIrArSuimm(ppcXoris, 0, 30, 0x8000); // op0.regLong
			m.machineCode.createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset+4);
			m.machineCode.loadConstantAndFixup(27, item); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, faux2, 27, 0); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, 0, stackPtr, tempStorageOffset);
			m.machineCode.createIrDrArB(ppcFsub, faux2, 0, faux2);
			m.machineCode.createIrSrAd(ppcStw, 31, stackPtr, tempStorageOffset+4); // op0.reg
			item = int2floatConst3;	// ref to 2^52;
			m.machineCode.loadConstantAndFixup(27, item); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, faux1, 27, 0); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, 0, stackPtr, tempStorageOffset);
			m.machineCode.createIrDrArB(ppcFsub, faux1, 0, faux1);					
			item = int2floatConst2;	// ref to 2^32;
			m.machineCode.loadConstantAndFixup(27, item); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, 0, 27, 0); // auxGPR1
			m.machineCode.createIrDrArCrB(ppcFmadd, faux2, faux2, 0, faux1);

			item = int2floatConst1;	// ref to 2^52+2^31;					
			m.machineCode.createIrDrAsimm(ppcAddis, 0, 0, 0x4330);	// preload 2^52
			m.machineCode.createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset);
			m.machineCode.createIrArSuimm(ppcXoris, 0, 28, 0x8000); // op1.regLong
			m.machineCode.createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset+4);
			m.machineCode.loadConstantAndFixup(27, item); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, faux3, 27, 0); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, 0, stackPtr, tempStorageOffset);
			m.machineCode.createIrDrArB(ppcFsub, faux3, 0, faux3);
			m.machineCode.createIrSrAd(ppcStw, 29, stackPtr, tempStorageOffset+4); // op1.reg
			item = int2floatConst3;	// ref to 2^52;
			m.machineCode.loadConstantAndFixup(27, item); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, faux1, 27, 0); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, 0, stackPtr, tempStorageOffset);
			m.machineCode.createIrDrArB(ppcFsub, faux1, 0, faux1);					
			item = int2floatConst2;	// ref to 2^32;
			m.machineCode.loadConstantAndFixup(27, item); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, 0, 27, 0); // auxGPR1
			m.machineCode.createIrDrArCrB(ppcFmadd, faux3, faux3, 0, faux1);

			m.machineCode.createIrDrArB(ppcFdiv, faux1, faux2, faux3);

			m.machineCode.createIrSrAd(ppcStfd, faux1, stackPtr, tempStorageOffset);
			m.machineCode.createIrDrAd(ppcLwz, 27, stackPtr, tempStorageOffset); // auxGPR1
			m.machineCode.createIrDrAd(ppcLwz, 26, stackPtr, tempStorageOffset+4); // res.reg
			m.machineCode.createIrArSSHMBME(ppcRlwinm, 28, 27, 12, 21, 31); // auxGPR2, auxGPR1	
			m.machineCode.createIrDrAsimm(ppcSubfic, 28, 28, 1075);	
			m.machineCode.createICRFrAsimm(ppcCmpi, CRF2, 27, 0);
			m.machineCode.createIrDrAsimm(ppcAddis, 0, 0, 0xfff0);	
			m.machineCode.createIrArSrB(ppcAndc, 27, 27, 0);	
			m.machineCode.createIrArSuimm(ppcOris, 27, 27, 0x10);	
			m.machineCode.createICRFrAsimm(ppcCmpi, CRF0, 28, 52);
			// double is < 1
			m.machineCode.createIBOBIBD(ppcBc, BOfalse, 4*CRF0+GT, 4);
			m.machineCode.createIrDrAsimm(ppcAddi, 0, 0, 0); // res.regLong
			m.machineCode.createIrDrAsimm(ppcAddi, 26, 0, 0); // res.reg
			m.machineCode.createIBOBILK(ppcBclr, BOalways, 0, false); // return

			m.machineCode.createICRFrAsimm(ppcCmpi, CRF0, 28, 0);
			int label2jmp1 = m.machineCode.iCount;
			m.machineCode.createIBOBIBD(ppcBc, BOtrue, 4*CRF0+LT, 0);	// jump to label 2
			m.machineCode.createIrDrAsimm(ppcSubfic, 0, 28, 32);
			m.machineCode.createIrArSrB(ppcSrw, 26, 26, 28);
			m.machineCode.createIrArSrB(ppcSlw, 0, 27, 0);
			m.machineCode.createIrArSrB(ppcOr, 26, 26, 0);
			m.machineCode.createIrDrAsimm(ppcAddi, 0, 28, -32);
			m.machineCode.createIrArSrB(ppcSrw, 0, 27, 0);
			m.machineCode.createIrArSrB(ppcOr, 26, 26, 0); // res.reg
			m.machineCode.createIrArSrB(ppcSrw, 0, 27, 28); // res.regLong
			int label3jmp1 = m.machineCode.iCount;
			m.machineCode.createIBOBIBD(ppcBc, BOalways, 0, 0);	// jump to label 3
			//label 2
			correctJmpAddr(m.machineCode.instructions, label2jmp1, m.machineCode.iCount);
			m.machineCode.createIrDrAsimm(ppcSubfic, 0, 28, 32);
			m.machineCode.createIrArSrB(ppcSlw, 29, 27, 28); // (r29) res.regLong
			m.machineCode.createIrArSrB(ppcSrw, 0, 26, 0); // res.reg
			m.machineCode.createIrArSrB(ppcOr, 29, 29, 0);
			m.machineCode.createIrDrAsimm(ppcAddi, 0, 28, -32);
			m.machineCode.createIrArSrB(ppcSlw, 0, 26, 0); // res.reg
			m.machineCode.createIrArSrB(ppcOr, 0, 29, 0); // res.regLong is now in r0
			m.machineCode.createIrArSrB(ppcSlw, 26, 26, 28); // res.reg
			//label 3
			correctJmpAddr(m.machineCode.instructions, label3jmp1, m.machineCode.iCount);
			m.machineCode.createIBOBIBD(ppcBc, BOfalse, 4*CRF2+LT, 3);
			m.machineCode.createIrDrAsimm(ppcSubfic, 26, 26, 0); // res.reg
			m.machineCode.createIrDrA(ppcSubfze, 0, 0); // res.regLong
			m.machineCode.createIBOBIBD(ppcBc, BOtrue, 4*CRF1+GT, 3);	// jump to end
			m.machineCode.createIrDrAsimm(ppcSubfic, 26, 26, 0); // res.reg
			m.machineCode.createIrDrA(ppcSubfze, 0, 0); // res.regLong
			m.machineCode.createIBOBILK(ppcBclr, BOalways, 0, false); // return
		}


		m = Method.getCompSpecSubroutine("remLong");
		// long op0 is passed in r30/r31 (dividend), long op1 is passed in r28/r29 (divisor) 
		// r25, r26, r27 can be used for general purposes
		// faux1, faux2, faux3 are used as general purpose FPR's 
		// result is returned in r0/r24 
		if (m != null) { 
			m.machineCode = new CodeGen();
			m.machineCode.instructions = new int[130];
			m.machineCode.fixups = new Item[defaultNofFixup];
			m.machineCode.iCount = 0;

			m.machineCode.createICRFrAsimm(ppcCmpi, CRF0, 28, 0);	// test if divisor < 2^32, op1.regLong
			int label1jmp1 = m.machineCode.iCount;
			m.machineCode.createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 0);	// jump to label 1
			m.machineCode.createICRFrAsimm(ppcCmpli, CRF0, 29, 0x7fff);	// test if divisor < 2^15, op1.reg
			int label1jmp2 = m.machineCode.iCount;
			m.machineCode.createIBOBIBD(ppcBc, BOfalse, 4*CRF0+LT, 0);	// jump to label 1
			
			// divisor is small, use GPR's, op1.regLong (r28) is 0 but it must be preserved for multiplication at the end
			m.machineCode.createICRFrAsimm(ppcCmpi, CRF2, 30, 0); // is dividend negative?, op0.regLong
			m.machineCode.createIrDrAsimm(ppcAddi, 19, 0, 0x1234);	
			m.machineCode.createIrDrArB(ppcDivw, 25, 30, 29); // res.regLong, op0.regLong, op1.reg
			m.machineCode.createIrDrArB(ppcMullw, 0, 29, 25); // op1.reg, res.regLong
			m.machineCode.createIrDrArB(ppcSubf, 0, 0, 30); // op0.regLong
			m.machineCode.createICRFrAsimm(ppcCmpi, CRF0, 0, 0); // is remainder negative?
			m.machineCode.createIBOBIBD(ppcBc, BOfalse, 4*CRF0+LT, 3);	
			m.machineCode.createIrDrArB(ppcAdd, 0, 0, 29);	// add divisor, op1.reg
			m.machineCode.createIrDrAsimm(ppcAddi, 25, 25, -1); // res.regLong 	
			m.machineCode.createIrArSSHMBME(ppcRlwinm, 27, 0, 16, 0, 15); // auxGPR1
			m.machineCode.createIrArSSHMBME(ppcRlwimi, 27, 31, 16, 16, 31); // auxGPR1, op0.reg
			m.machineCode.createIrDrArB(ppcDivwu, 26, 27, 29); // auxGPR2, auxGPR1, op1.reg
			m.machineCode.createIrDrArB(ppcMullw, 0, 29, 26); // op1.reg, auxGPR2
			m.machineCode.createIrDrArB(ppcSubf, 0, 0, 27); // auxGPR1
			m.machineCode.createIrArSSHMBME(ppcRlwinm, 27, 0, 16, 0, 15); // auxGPR1
			m.machineCode.createIrArSSHMBME(ppcRlwimi, 27, 31, 0, 16, 31); // auxGPR1, op0.reg 
			m.machineCode.createIrDrArB(ppcDivwu, 0, 27, 29); // auxGPR1, op1.reg
			m.machineCode.createIrArSSHMBME(ppcRlwinm, 24, 26, 16, 0, 15); // res.reg, auxGPR2
			m.machineCode.createIrArSSHMBME(ppcRlwimi, 24, 0, 0, 16, 31); // res.reg
			m.machineCode.createIrDrArB(ppcMullw, 0, 29, 0); // op1.reg
			m.machineCode.createIrDrArB(ppcSubf, 0, 0, 27); // auxGPR1
			m.machineCode.createICRFrAsimm(ppcCmpi, CRF0, 0, 0); // is remainder > 0?
			m.machineCode.createIcrbDcrbAcrbB(ppcCrand, CRF0EQ, CRF0GT, CRF2LT);
			m.machineCode.createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 2);	
			m.machineCode.createIrDrAsimm(ppcAddi, 24, 24, 1); // res.reg	
			m.machineCode.createIBOBIBD(ppcBc, BOtrue, 4*CRF1+GT, 3);	// was divisor negative? CRF1 set before subroutine
			m.machineCode.createIrDrAsimm(ppcSubfic, 24, 24, 0); // negate result, div.reg
			m.machineCode.createIrDrA(ppcSubfze, 25, 25);	// div.regLong
			int label4jmp1 = m.machineCode.iCount;
			m.machineCode.createIBOBIBD(ppcBc, BOalways, 0, 0);	// jump to label 4

			//label 1, divisor is not small, use FPR's
			correctJmpAddr(m.machineCode.instructions, label1jmp1, m.machineCode.iCount);
			correctJmpAddr(m.machineCode.instructions, label1jmp2, m.machineCode.iCount);
			Item item = int2floatConst1;	// ref to 2^52+2^31;					
			m.machineCode.createIrDrAsimm(ppcAddis, 0, 0, 0x4330);	// preload 2^52
			m.machineCode.createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset);
			m.machineCode.createIrArSuimm(ppcXoris, 0, 30, 0x8000); // op0.regLong
			m.machineCode.createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset+4);
			m.machineCode.loadConstantAndFixup(27, item); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, faux2, 27, 0); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, 0, stackPtr, tempStorageOffset);
			m.machineCode.createIrDrArB(ppcFsub, faux2, 0, faux2);
			m.machineCode.createIrSrAd(ppcStw, 31, stackPtr, tempStorageOffset+4); // op0.reg
			item = int2floatConst3;	// ref to 2^52;
			m.machineCode.loadConstantAndFixup(27, item); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, faux1, 27, 0); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, 0, stackPtr, tempStorageOffset);
			m.machineCode.createIrDrArB(ppcFsub, faux1, 0, faux1);					
			item = int2floatConst2;	// ref to 2^32;
			m.machineCode.loadConstantAndFixup(27, item); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, 0, 27, 0); // auxGPR1
			m.machineCode.createIrDrArCrB(ppcFmadd, faux2, faux2, 0, faux1);

			item = int2floatConst1;	// ref to 2^52+2^31;					
			m.machineCode.createIrDrAsimm(ppcAddis, 0, 0, 0x4330);	// preload 2^52
			m.machineCode.createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset);
			m.machineCode.createIrArSuimm(ppcXoris, 0, 28, 0x8000); // op1.regLong
			m.machineCode.createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset+4);
			m.machineCode.loadConstantAndFixup(27, item); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, faux3, 27, 0); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, 0, stackPtr, tempStorageOffset);
			m.machineCode.createIrDrArB(ppcFsub, faux3, 0, faux3);
			m.machineCode.createIrSrAd(ppcStw, 29, stackPtr, tempStorageOffset+4); // op1.reg
			item = int2floatConst3;	// ref to 2^52;
			m.machineCode.loadConstantAndFixup(27, item); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, faux1, 27, 0); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, 0, stackPtr, tempStorageOffset);
			m.machineCode.createIrDrArB(ppcFsub, faux1, 0, faux1);					
			item = int2floatConst2;	// ref to 2^32;
			m.machineCode.loadConstantAndFixup(27, item); // auxGPR1
			m.machineCode.createIrDrAd(ppcLfd, 0, 27, 0); // auxGPR1
			m.machineCode.createIrDrArCrB(ppcFmadd, faux3, faux3, 0, faux1);

			m.machineCode.createIrDrArB(ppcFdiv, faux1, faux2, faux3);

			m.machineCode.createIrSrAd(ppcStfd, faux1, stackPtr, tempStorageOffset);
			m.machineCode.createIrDrAd(ppcLwz, 27, stackPtr, tempStorageOffset); // auxGPR1
			m.machineCode.createIrDrAd(ppcLwz, 24, stackPtr, tempStorageOffset+4); // div.reg
			m.machineCode.createIrArSSHMBME(ppcRlwinm, 26, 27, 12, 21, 31); // auxGPR2, auxGPR1	
			m.machineCode.createIrDrAsimm(ppcSubfic, 26, 26, 1075);	
			m.machineCode.createICRFrAsimm(ppcCmpi, CRF2, 27, 0);
			m.machineCode.createIrDrAsimm(ppcAddis, 0, 0, 0xfff0);	
			m.machineCode.createIrArSrB(ppcAndc, 27, 27, 0);	
			m.machineCode.createIrArSuimm(ppcOris, 27, 27, 0x10);	
			m.machineCode.createICRFrAsimm(ppcCmpi, CRF0, 26, 52);
			// double is < 1
			m.machineCode.createIBOBIBD(ppcBc, BOfalse, 4*CRF0+GT, 4);
			m.machineCode.createIrDrAsimm(ppcAddi, 25, 0, 0); // div.regLong
			m.machineCode.createIrDrAsimm(ppcAddi, 24, 0, 0); // div.reg
			int label4jmp2 = m.machineCode.iCount;
			m.machineCode.createIBOBIBD(ppcBc, BOalways, 0, 0);	// jump to label 4

			m.machineCode.createICRFrAsimm(ppcCmpi, CRF0, 26, 0);
			int label2jmp1 = m.machineCode.iCount;
			m.machineCode.createIBOBIBD(ppcBc, BOtrue, 4*CRF0+LT, 0);	// jump to label 2
			m.machineCode.createIrDrAsimm(ppcSubfic, 0, 26, 32);
			m.machineCode.createIrArSrB(ppcSrw, 24, 24, 26);
			m.machineCode.createIrArSrB(ppcSlw, 0, 27, 0);
			m.machineCode.createIrArSrB(ppcOr, 24, 24, 0);
			m.machineCode.createIrDrAsimm(ppcAddi, 0, 26, -32);
			m.machineCode.createIrArSrB(ppcSrw, 0, 27, 0);
			m.machineCode.createIrArSrB(ppcOr, 24, 24, 0); // div.reg
			m.machineCode.createIrArSrB(ppcSrw, 25, 27, 26); // div.regLong, aux1, aux2
			int label3jmp1 = m.machineCode.iCount;
			m.machineCode.createIBOBIBD(ppcBc, BOalways, 0, 0);	// jump to label 3
			//label 2
			correctJmpAddr(m.machineCode.instructions, label2jmp1, m.machineCode.iCount);
			m.machineCode.createIrDrAsimm(ppcSubfic, 0, 26, 32);
			m.machineCode.createIrArSrB(ppcSlw, 25, 27, 26); // div.regLong
			m.machineCode.createIrArSrB(ppcSrw, 0, 24, 0); // div.reg
			m.machineCode.createIrArSrB(ppcOr, 25, 25, 0);
			m.machineCode.createIrDrAsimm(ppcAddi, 0, 26, -32);
			m.machineCode.createIrArSrB(ppcSlw, 0, 24, 0); // div.reg
			m.machineCode.createIrArSrB(ppcOr, 25, 25, 0); // div.regLong
			m.machineCode.createIrArSrB(ppcSlw, 24, 24, 26); // div.reg, aux2
			//label 3
			correctJmpAddr(m.machineCode.instructions, label3jmp1, m.machineCode.iCount);
			m.machineCode.createIBOBIBD(ppcBc, BOfalse, 4*CRF2+LT, 3);
			m.machineCode.createIrDrAsimm(ppcSubfic, 24, 24, 0); // div.reg
			m.machineCode.createIrDrA(ppcSubfze, 25, 25); // div.regLong
			//label 4
			correctJmpAddr(m.machineCode.instructions, label4jmp1, m.machineCode.iCount);
			correctJmpAddr(m.machineCode.instructions, label4jmp2, m.machineCode.iCount);
			m.machineCode.createIrDrArB(ppcMullw, 27, 25, 29); // auxGPR1, div.regLong, op1.reg
			m.machineCode.createIrDrArB(ppcMullw, 26, 24, 28); // auxGPR2, div.reg, op1.regLong
			m.machineCode.createIrDrArB(ppcAdd, 27, 27, 26);
			m.machineCode.createIrDrArB(ppcMulhwu, 26, 24, 29); // auxGPR2, div.reg, op1.reg
			m.machineCode.createIrDrArB(ppcAdd, 25, 27, 26);
			m.machineCode.createIrDrArB(ppcMullw, 24, 24, 29); // res.reg, op1.reg

			m.machineCode.createIrDrArB(ppcSubfc, 24, 24, 31); // res.reg, div.reg, op0.reg
			m.machineCode.createIrDrArB(ppcSubfe, 0, 25, 30);	// res.regLong, div.regLong, op0.regLong
			m.machineCode.createIBOBILK(ppcBclr, BOalways, 0, false); // return
		}

		int regAux1 = paramEndGPR; // use parameter registers for interface delegation methods
		int regAux2 = paramEndGPR - 1; // use parameter registers
		int regAux3 = paramEndGPR - 2; // use parameter registers

		m = Method.getCompSpecSubroutine("imDelegI1Mm");
		if (m != null) { 
			m.machineCode = new CodeGen();
			// imDelegI1Mm
			m.machineCode.instructions = new int[16];
			m.machineCode.iCount = 0;
			m.machineCode.createIrDrAd(ppcLwz, regAux2, paramStartGPR, -4);	// get tag
			m.machineCode.createIrDrAd(ppcLwz, 0, regAux2, (Class.maxExtensionLevelStdClasses + 1) * 4 + Linker32.tdBaseClass0Offset + 4);	// get interface
			m.machineCode.createIrArS(ppcExtsh, 0, 0);
			m.machineCode.createIrArSuimm(ppcAndi, regAux1, regAux1, 0xffff);	// mask method number
			m.machineCode.createIrDrArB(ppcSubf, regAux1, regAux1, 0);	
			m.machineCode.createIrDrArB(ppcLwzx, 0, regAux2, regAux1);
			m.machineCode.createIrSspr(ppcMtspr, CTR, 0);
			m.machineCode.createIBOBILK(ppcBcctr, BOalways, 0, false);	// no linking
		}

		m = Method.getCompSpecSubroutine("imDelegIiMm");
		if (m != null) { 
			m.machineCode = new CodeGen();
			// imDelegIiMm
			m.machineCode.instructions = new int[16];
			m.machineCode.iCount = 0;
			m.machineCode.createIrArSuimm(ppcOri, regAux3, regAux1, 0xffff);	// interface id
			m.machineCode.createIrDrAd(ppcLwz, regAux2, paramStartGPR, -4);	// get tag
			m.machineCode.createIrDrAsimm(ppcAddi, regAux2, regAux2, (Class.maxExtensionLevelStdClasses + 1) * Linker32.slotSize + Linker32.tdBaseClass0Offset);	// set to address before first interface 
			m.machineCode.createIrDrAsimm(ppcAddi, regAux2, regAux2, 4);	// set to next interface
			m.machineCode.createIrDrAd(ppcLwz, 0, regAux2, 0);	// get interface
			m.machineCode.createICRFrArB(ppcCmpl, CRF0, 0, regAux3);
			m.machineCode.createIBOBIBD(ppcBc, BOtrue, 4*CRF0+GT, -3);
			m.machineCode.createIrArS(ppcExtsh, 0, 0);
			m.machineCode.createIrArSuimm(ppcAndi, regAux1, regAux1, 0xffff);	// mask method offset
			m.machineCode.createIrDrArB(ppcAdd, regAux1, regAux1, 0);	
			m.machineCode.createIrDrAd(ppcLwz, regAux2, paramStartGPR, -4);	// reload tag
			m.machineCode.createIrDrArB(ppcLwzx, 0, regAux2, regAux1);
			m.machineCode.createIrSspr(ppcMtspr, CTR, 0);
			m.machineCode.createIBOBILK(ppcBcctr, BOalways, 0, false);	// no linking
		}
		
		m = Method.getCompSpecSubroutine("handleException");
		if (m != null) { 
			m.machineCode = new CodeGen();
			// exceptionHandler
			m.machineCode.instructions = new int[64];
			m.machineCode.fixups = new Item[defaultNofFixup];
			m.machineCode.iCount = 0;
			// r2 contains reference to exception, r3 holds SRR0
			// r4 to r10 are used for auxiliary purposes

			// search end of method
			m.machineCode.createIrArSrB(ppcOr, 4, 3, 3);
			m.machineCode.createIrDrAd(ppcLwzu, 9, 4, 4);	
			m.machineCode.createICRFrAsimm(ppcCmpli, CRF0, 9, 0xff);
			m.machineCode.createIBOBIBD(ppcBc, BOtrue, 4*CRF0+GT, -2);
			m.machineCode.createIrArSrB(ppcOr, 10, 4, 4);	// keep for unwinding
			m.machineCode.createIrDrAd(ppcLwzu, 5, 4, 4);	//  R4 now points to first entry of exception table
		
			// search catch, label 1
			int label1 = m.machineCode.iCount;
			m.machineCode.createICRFrAsimm(ppcCmpi, CRF0, 5, -1);
			int label2 = m.machineCode.iCount;
			m.machineCode.createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 0);	// catch not found, goto label 2
			m.machineCode.createIrDrAd(ppcLwz, 5, 4, 0);	// start 
			m.machineCode.createICRFrArB(ppcCmp, CRF0, 3, 5);		
			m.machineCode.createIBOBIBD(ppcBc, BOtrue, 4*CRF0+LT, 17);
			m.machineCode.createIrDrAd(ppcLwz, 5, 4, 4);	// end 
			m.machineCode.createICRFrArB(ppcCmp, CRF0, 3, 5);		
			m.machineCode.createIBOBIBD(ppcBc, BOtrue, 4*CRF0+GT, 14);
			m.machineCode.createIrDrAd(ppcLwz, 5, 4, 8);	// type 
			
			m.machineCode.createICRFrAsimm(ppcCmpi, CRF0, 5, 0);	// check if type "any", caused by finally
			m.machineCode.createIBOBIBD(ppcBc, BOtrue, 4*CRF0+EQ, 8);
//			m.machineCode.createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 3);
//			m.machineCode.createIrDrAsimm(ppcAddi, 18, 18, 0x1000);	
//			m.machineCode.createIBOBIBD(ppcBc, BOalways, 4*CRF0+EQ, 8);
			
			m.machineCode.createIrDrAd(ppcLwz, 6, 5, -4);	// get extension level of exception 
			m.machineCode.createIrArSSHMBME(ppcRlwinm, 6, 6, 2, 0, 31);	// *4
			m.machineCode.createIrDrAsimm(ppcAddi, 6, 6, Linker32.tdBaseClass0Offset);	
			m.machineCode.createIrDrAd(ppcLwz, 7, 2, -4);	// get tag 
			m.machineCode.createIrDrArB(ppcLwzx, 8, 7, 6);	 
			m.machineCode.createICRFrArB(ppcCmp, CRF0, 8, 5);		
			m.machineCode.createIBOBIBD(ppcBc, BOfalse, 4*CRF0+EQ, 4);		
			m.machineCode.createIrDrAd(ppcLwz, 0, 4, 12);	// get handler address
			m.machineCode.createIrSspr(ppcMtspr, SRR0, 0);
			m.machineCode.createIrfi(ppcRfi);	// return to catch
			
			m.machineCode.createIrDrAd(ppcLwzu, 5, 4, 16);	
			m.machineCode.createIBOBIBD(ppcBc, BOalways, 0, 0);	// jump to label 1
			correctJmpAddr(m.machineCode.instructions, m.machineCode.iCount-1, label1);
			
			// catch not found, unwind, label 2
			correctJmpAddr(m.machineCode.instructions, label2, m.machineCode.iCount);
			m.machineCode.createIrDrAd(ppcLwz, 5, stackPtr, 0);	// get back pointer
			m.machineCode.createIrDrAd(ppcLwz, 3, 5, -4);	// get LR from stack
			m.machineCode.loadConstantAndFixup(6, m);
			m.machineCode.createIrSrAd(ppcStw, 6, 5, -4);	// put addr of handleException
			m.machineCode.createIrArS(ppcExtsb, 9, 9);
			m.machineCode.createIrDrArB(ppcAdd, 9, 10, 9);
			m.machineCode.createIrSspr(ppcMtspr, LR, 9);
//			m.machineCode.createIBOBIBD(ppcBc, BOalways, 4*CRF0+GT, 0);
			m.machineCode.createIBOBILK(ppcBclr, BOalways, 0, false); // branch to epilog
		}
	}

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


