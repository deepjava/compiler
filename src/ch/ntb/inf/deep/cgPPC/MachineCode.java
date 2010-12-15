package ch.ntb.inf.deep.cgPPC;

import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.cfg.*;
import ch.ntb.inf.deep.ssa.*;
import ch.ntb.inf.deep.ssa.instruction.*;
import ch.ntb.inf.deep.strings.HString;
import static org.junit.Assert.*;
import ch.ntb.inf.deep.classItems.*;

public class MachineCode implements SSAInstructionOpcs, SSAInstructionMnemonics, SSAValueType, InstructionOpcs, Registers, ICjvmInstructionOpcs, ICclassFileConsts {
	static final int maxNofParam = 32;
	private static final int defaultNofInstr = 16;
	private static final int defaultNofFixup = 8;
	private static final int arrayLenOffset = 6;	

	private static final int arrayFirstOffset = 12; // Linker.getSizeOfObject() benutzen
	private static final int cppOffset = 24;
	private static final int constForDoubleConv = 32;
	
	private static int LRoffset;	
	private static int CTRoffset;	
	private static int CRoffset;	
	private static int SRR0offset;	
	private static int SRR1offset;	
	private static int GPRoffset;	
	private static int FPRoffset;	
	private static int localVarOffset;
	private static int tempStorageOffset;	
	private static int paramOffset;
	private static int stackSize;
	static boolean tempStorage;
	
	private static int nofParam;
	private static int nofParamGPR, nofParamFPR;
	static int nofNonVolGPR, nofNonVolFPR;
	static int[] paramType = new int[maxNofParam];
	static boolean[] paramHasNonVolReg = new boolean[maxNofParam];
	static int[] paramRegNr = new int[maxNofParam];
	
	// information about into which registers parameters of this method go 
	private static int nofMoveGPR, nofMoveFPR;
	private static int[] moveGPR = new int[maxNofParam];
	private static int[] moveFPR = new int[maxNofParam];
	
	// information about into which registers parameters of a call to a method go 
	private static int[] destGPR = new int[nofGPR];
	private static int[] destFPR = new int[nofFPR];
	
	private static SSAValue[] lastExitSet;

	public SSA ssa;	// reference to the SSA of a method
	public int[] instructions;	//contains machine instructions for the ssa of a method
	public int iCount;	//nof instructions for this method
	
	Item[] fixups;	// contains all references whose address has to be fixed by the linker
	int fCount;	//nof fixups
	int lastFixup;	// instr number where the last fixup is found

	public MachineCode(SSA ssa) {
		this.ssa = ssa;
		instructions = new int[defaultNofInstr];
		fixups = new Item[defaultNofFixup];
		nofParamGPR = 0; nofParamFPR = 0;
		nofNonVolGPR = 0; nofNonVolFPR = 0;
		nofMoveGPR = 0; nofMoveFPR = 0;
		tempStorage = false;

		// make local copy 
		int maxStackSlots = ssa.cfg.method.maxStackSlots;
		int i = maxStackSlots;
		while ((i < ssa.isParam.length) && ssa.isParam[i]) {
			int type = ssa.paramType[i];
			paramType[i - maxStackSlots] = type;
			paramHasNonVolReg[i - maxStackSlots] = false;
			if (type == tLong || type == tDouble) i++;
			i++;
		}
		nofParam = i - maxStackSlots;
		assert nofParam <= maxNofParam : "method has too many parameters";
		
		System.out.println("build intervals for " + ssa.cfg.method.name);
//		ssa.print(0);
		RegAllocator.buildIntervals(ssa);
		
		System.out.println("assign registers to parameters, nofParam = " + nofParam);
		SSANode b = (SSANode) ssa.cfg.rootNode;
		while (b.next != null) {
			b = (SSANode) b.next;
		}	
		lastExitSet = b.exitSet;
		// determine, which parameters go into which register
		parseExitSet(lastExitSet, maxStackSlots);
		
		System.out.println("allocate registers");
		RegAllocator.assignRegisters(this);
//		ssa.print(0);
		
		if (ssa.cfg.method.name.equals(HString.getHString("reset"))) {	// no prolog
		} else if (ssa.cfg.method.name.equals(HString.getHString("interrupt"))) {
			stackSize = calcStackSizeException();
			insertPrologException();
		} else {
			stackSize = calcStackSize();
			iCount = calcPrologSize();
		}
		
		SSANode node = (SSANode)ssa.cfg.rootNode;
		while (node != null) {
			node.codeStartAddr = iCount;
			translateSSA(node);
			node.codeEndAddr = iCount-1;
			node = (SSANode) node.next;
		}
		node = (SSANode)ssa.cfg.rootNode;
		while (node != null) {	// resolve local branch targets
			if (node.nofInstr > 0) {
				if (node.instructions[node.nofInstr-1].ssaOpcode == sCbranch) {
					int code = this.instructions[node.codeEndAddr];
					//					System.out.println("target of branch instruction corrected: 0x" + Integer.toHexString(node.codeEndAddr*4));
					CFGNode[] successors = node.successors;
					switch (code & 0xfc000000) {
					case ppcB:			
						if ((code & 0xffff) != 0) {	// switch
							int nofCases = (code & 0xffff) >> 2;
							int k;
							for (k = 0; k < nofCases; k++) {
								int branchOffset = ((SSANode)successors[k]).codeStartAddr - (node.codeEndAddr+1-(nofCases-k)*2);
								this.instructions[node.codeEndAddr+1-(nofCases-k)*2] |= (branchOffset << 2) & 0x3ffffff;
							}
							int branchOffset = ((SSANode)successors[k]).codeStartAddr - node.codeEndAddr;
							this.instructions[node.codeEndAddr] &= 0xfc000000;
							this.instructions[node.codeEndAddr] |= (branchOffset << 2) & 0x3ffffff;
						} else {
							//							System.out.println("abs branch found");
							int branchOffset = ((SSANode)successors[0]).codeStartAddr - node.codeEndAddr;
							this.instructions[node.codeEndAddr] |= (branchOffset << 2) & 0x3ffffff;
						}
						break;
					case ppcBc:
						//						System.out.println("cond branch found");
						int branchOffset = ((SSANode)successors[1]).codeStartAddr - node.codeEndAddr;
						this.instructions[node.codeEndAddr] |= (branchOffset << 2) & 0xffff;
						break;
					}
				} else if (node.instructions[node.nofInstr-1].ssaOpcode == sCreturn) {
					if (node.next != null) {
						int branchOffset = iCount - node.codeEndAddr;
						this.instructions[node.codeEndAddr] |= (branchOffset << 2) & 0x3ffffff;
						//						System.out.println("return branch found, epilog start instr = " + iCount);
					}
				}
			}
			node = (SSANode) node.next;
		}
		if (ssa.cfg.method.name.equals(HString.getHString("reset"))) {	// reset needs no epilog
//		if ((ssa.cfg.method.accAndPropFlags & (1 << dpfExcHnd)) != 0) {	// exception
//System.out.println("is Exception");
		} else if (ssa.cfg.method.name.equals(HString.getHString("interrupt"))) {	// alle anderen excps
			insertEpilogException(stackSize);
		} else {
			insertEpilog(stackSize);
			insertProlog();
		}
//		print();
	}

	private static void parseExitSet(SSAValue[] exitSet, int maxStackSlots) {
		nofParamGPR = 0; nofParamFPR = 0;
		nofMoveGPR = 0; nofMoveFPR = 0;
		System.out.print("[");
		for (int i = 0; i < nofParam; i++) {
			int type = paramType[i];
			System.out.print("(" + svNames[type] + ")");
			if (type == tLong) {
				if (exitSet[i+maxStackSlots] != null) {	// if null -> parameter is never used
					System.out.print("r");
					if (paramHasNonVolReg[i]) {
						int reg = RegAllocator.reserveReg(gpr, true);
						int regLong = RegAllocator.reserveReg(gpr, true);
						moveGPR[nofMoveGPR] = nofParamGPR;
						moveGPR[nofMoveGPR+1] = nofParamGPR+1;
						nofMoveGPR += 2;
						paramRegNr[i] = reg;
						paramRegNr[i+1] = regLong;
						System.out.print(reg + ",r" + regLong);
					} else {
						RegAllocator.reserveReg(gpr, paramStartGPR + nofParamGPR);
						RegAllocator.reserveReg(gpr, paramStartGPR + nofParamGPR + 1);
						paramRegNr[i] = paramStartGPR + nofParamGPR;
						paramRegNr[i+1] = paramStartGPR + nofParamGPR + 1;
						System.out.print((paramStartGPR + nofParamGPR) + ",r" + (paramStartGPR + nofParamGPR + 1));
					}
				}
				nofParamGPR += 2;
				i++;
			} else if (type == tFloat || type == tDouble) {
				if (exitSet[i+maxStackSlots] != null) {	// if null -> parameter is never used
					System.out.print("fr");
					if (paramHasNonVolReg[i]) {
						int reg = RegAllocator.reserveReg(fpr, true);
						moveFPR[nofMoveFPR] = nofParamFPR;
						nofMoveFPR++;
						paramRegNr[i] = reg;
						System.out.print(reg);
					} else {
						RegAllocator.reserveReg(fpr, paramStartFPR + nofParamFPR);
						paramRegNr[i] = paramStartFPR + nofParamFPR;
						System.out.print(paramStartFPR + nofParamFPR);
					}
				}
				nofParamFPR++;
				if (type == tDouble) i++;
			} else {
				if (exitSet[i+maxStackSlots] != null) {	// if null -> parameter is never used
					System.out.print("r");
					if (paramHasNonVolReg[i]) {
						int reg = RegAllocator.reserveReg(gpr, true);
						moveGPR[nofMoveGPR] = nofParamGPR;
						nofMoveGPR++;
						paramRegNr[i] = reg;
						System.out.print(reg);
					} else {
						RegAllocator.reserveReg(gpr, paramStartGPR + nofParamGPR);
						paramRegNr[i] = paramStartGPR + nofParamGPR;
						System.out.print(paramStartGPR + nofParamGPR);
					}
				}
				nofParamGPR++;
			}
			if (i < nofParam - 1) System.out.print(", ");
		}
		System.out.println("]");
	}

	private static int calcStackSize() {
		int size = 16 + nofNonVolGPR * 4 + nofNonVolFPR * 8 + (tempStorage? 8 : 0);
		int padding = (16 - (size % 16)) % 16;
		size = size + padding;
		LRoffset = size - 4;
		GPRoffset = size - 12 - nofNonVolGPR * 4;
		FPRoffset = GPRoffset - nofNonVolFPR * 8;
		if (tempStorage) tempStorageOffset = FPRoffset - 8;
		else tempStorageOffset = FPRoffset;
		return size;
	}

	private static int calcStackSizeException() {
		int size = 24 + nofGPR * 4 + (tempStorage? 8 : 0);
		int padding = (16 - (size % 16)) % 16;
		size = size + padding;
		LRoffset = size - 4;
		CTRoffset = size - 8;
		CRoffset = size - 12;
		SRR0offset = size - 16;
		SRR0offset = size - 20;
		GPRoffset = size - 20 - nofGPR * 4;
		if (tempStorage) tempStorageOffset = GPRoffset - 8;
		else tempStorageOffset = GPRoffset;
		return size;
	}

	private static int calcPrologSize() {
		int size = 3;
		if (nofNonVolGPR > 0) size++;
		size = size + nofNonVolFPR + nofMoveGPR + nofMoveFPR;
		return size;
	}

	private void translateSSA (SSANode node) {
		SSAValue[] opds;
		int sReg1, sReg2, dReg, refReg, indexReg, valReg, bci, offset, type;
		for (int i = 0; i < node.nofInstr; i++) {
			SSAInstruction instr = node.instructions[i];
			SSAValue res = instr.result;
//			System.out.println("ssa opcode = " + instr.scMnemonics[instr.ssaOpcode]);
			switch (instr.ssaOpcode) { 
			case sCloadConst:
				opds = instr.getOperands();
				dReg = res.reg;
				if (dReg >= 0) {	// else immediate opd
//System.out.println("dReg = " + dReg);
					switch (res.type) {
					case tByte: case tShort: case tInteger:
						int immVal = ((Constant)res.constant).valueH;
						loadConstant(immVal, dReg);
					break;
					case tLong:	
						Constant constant = (Constant)res.constant;
						long immValLong = ((long)(constant.valueH)<<32) | (constant.valueL&0xFFFFFFFFL);
//						System.out.println("sdfsdge " + immValLong);
						loadConstant((int)(immValLong >> 32), res.regLong);
						loadConstant((int)immValLong, dReg);
						break;	
					case tFloat:	// load from const pool
						constant = (Constant)res.constant;
						if (constant.valueH == 0) {	// 0.0 must be loaded directly as it's not in the cp
							createIrDrAsimm(ppcAddi, res.regAux1, 0, 0);
							createIrSrAd(ppcStw, res.regAux1, stackPtr, tempStorageOffset);
							createIrDrAd(ppcLfs, res.reg, stackPtr, tempStorageOffset);
						} else if (constant.valueH == 0x3f80) {	// 1.0
							createIrDrAsimm(ppcAddis, res.regAux1, 0, 0x3f80);
							createIrSrAd(ppcStw, res.regAux1, stackPtr, tempStorageOffset);
							createIrDrAd(ppcLfs, res.reg, stackPtr, tempStorageOffset);
						} else if (constant.valueH == 0x4000) {	// 2.0
							createIrDrAsimm(ppcAddis, res.regAux1, 0, 0x4000);
							createIrSrAd(ppcStw, res.regAux1, stackPtr, tempStorageOffset);
							createIrDrAd(ppcLfs, res.reg, stackPtr, tempStorageOffset);
						} else {
							loadConstantAndFixup(res.regAux1, constant);
							createIrDrAd(ppcLfs, res.reg, res.regAux1, cppOffset);
						}
						break;
					case tDouble:
						constant = (Constant)res.constant;
						if (constant.valueH == 0) {	// 0.0 must be loaded directly as it's not in the cp
							createIrDrAsimm(ppcAddi, res.regAux1, 0, 0);
							createIrSrAd(ppcStw, res.regAux1, stackPtr, tempStorageOffset);
							createIrSrAd(ppcStw, res.regAux1, stackPtr, tempStorageOffset+4);
							createIrDrAd(ppcLfd, res.reg, stackPtr, tempStorageOffset);
						} else if (constant.valueH == 0x3ff0) {	// 1.0{
							createIrDrAsimm(ppcAddis, res.regAux1, 0, 0x3ff0);
							createIrSrAd(ppcStw, res.regAux1, stackPtr, tempStorageOffset);
							createIrDrAsimm(ppcAddis, res.regAux1, 0, 0);
							createIrSrAd(ppcStw, res.regAux1, stackPtr, tempStorageOffset+4);
							createIrDrAd(ppcLfd, res.reg, stackPtr, tempStorageOffset);
						} else {
							loadConstantAndFixup(res.regAux1, constant);
							createIrDrAd(ppcLfd, res.reg, res.regAux1, cppOffset);
						}
						break;
					case tRef:	// e.g. ref to const string
						loadConstantAndFixup(res.reg, res.constant);
						break;
					default:
						assert false : "cg: wrong type";
					}
				} else 
				break;	// sCloadConst
			case sCloadLocal:
				break;	// sCloadLocal
			case sCloadFromField:
				opds = instr.getOperands();
				if (opds == null) {	// getstatic
					sReg1 = instr.result.regAux1;
					Item field = ((NoOpndRef)instr).field;
					offset = field.offset;
					loadConstantAndFixup(sReg1, field);
				} else {	// getfield
					sReg1 = opds[0].reg;
					offset = ((MonadicRef)instr).item.offset;
					createItrap(ppcTwi, TOifequal, opds[0].reg, 0);
				}
				switch (res.type) {
				case tBoolean: case tByte:
					createIrDrAd(ppcLbz, res.reg, sReg1, offset);
					createIrArS(ppcExtsb, res.reg, res.reg);
					break;
				case tShort: 
					createIrDrAd(ppcLha, res.reg, sReg1, offset);
					break;
				case tInteger: case tRef: case tAref: case tAboolean:
				case tAchar: case tAfloat: case tAdouble: case tAbyte: 
				case tAshort: case tAinteger: case tAlong:
					createIrDrAd(ppcLwz, instr.result.reg, sReg1, offset);
					break;
				case tChar: 
					assert false : "cg: type not implemented";
					break;
				case tLong:
					createIrDrAd(ppcLwzu, res.reg, sReg1, offset);
					createIrDrAd(ppcLwz, res.regLong, sReg1, 4);
					break;
				case tFloat: 
					createIrDrAd(ppcLfs, res.reg, sReg1, offset);
					break;
				case tDouble: 
					createIrDrAd(ppcLfd, res.reg, sReg1, offset);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;	// sCloadFromField
			case sCloadFromArray:
				opds = instr.getOperands();
				refReg = opds[0].reg;
				indexReg = opds[1].reg;
				createItrap(ppcTwi, TOifequal, refReg, 0);
				createIrDrAd(ppcLha, res.regAux1, refReg, -arrayLenOffset);
				createItrap(ppcTw, TOifgeU, indexReg, res.regAux1);
				switch (res.type) {
				case tByte: case tBoolean:
					createIrDrAsimm(ppcAddi, res.regAux2, refReg, arrayFirstOffset);
					createIrDrArB(ppcLbzx, res.reg, res.regAux2, indexReg);
					createIrArS(ppcExtsb, res.reg, res.reg);
					break;
				case tShort: 
					createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 1, 0, 30);
					createIrDrAsimm(ppcAddi, res.regAux2, refReg, arrayFirstOffset);
					createIrDrArB(ppcLhax, res.reg, res.regAux1, res.regAux2);
					break;
				case tInteger: case tRef:
					createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 2, 0, 29);
					createIrDrAsimm(ppcAddi, res.regAux2, refReg, arrayFirstOffset);
					createIrDrArB(ppcLwzx, res.reg, res.regAux1, res.regAux2);
					break;
				case tLong: 
					createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 3, 0, 28);
					createIrDrAsimm(ppcAddi, res.regAux2, refReg, arrayFirstOffset);
					createIrDrArB(ppcLwzux, res.reg, res.regAux1, res.regAux2);
					createIrDrAd(ppcLwz, res.regLong, res.regAux1, 4);
					break;
				case tFloat:
					createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 2, 0, 29);
					createIrDrAsimm(ppcAddi, res.regAux2, refReg, arrayFirstOffset);
					createIrDrArB(ppcLfsx, res.reg, res.regAux1, res.regAux2);
					break;
				case tDouble: 
					createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 3, 0, 28);
					createIrDrAsimm(ppcAddi, res.regAux2, refReg, arrayFirstOffset);
					createIrDrArB(ppcLfdx, res.reg, res.regAux1, res.regAux2);
					break;
				case tChar: 
					assert false : "cg: type char not implemented";
					break;
				default:
					assert false : "cg: type not implemented";
				}
				break;	// sCloadFromArray
			case sCstoreToField:
				opds = instr.getOperands();
				if (opds.length == 1) {	// putstatic
					sReg1 = opds[0].reg;
					sReg2 = opds[0].regLong;
					refReg = res.regAux1;
					type = opds[0].type;
					Item item = ((MonadicRef)instr).item;
					offset = item.offset;
					loadConstantAndFixup(res.regAux1, item);
				} else {	// putfield
					refReg = opds[0].reg;
					sReg1 = opds[1].reg;
					sReg2 = opds[1].regLong;
					type = opds[1].type;
					offset = ((DyadicRef)instr).field.offset;
					createItrap(ppcTwi, TOifequal, refReg, 0);
				}
				switch (type) {
				case tBoolean: case tByte: 
					createIrSrAd(ppcStb, sReg1, refReg, offset);
					break;
				case tShort: 
					createIrSrAd(ppcSth, sReg1, refReg, offset);
					break;
				case tInteger: case tRef: case tAref: case tAboolean:
				case tAchar: case tAfloat: case tAdouble: case tAbyte: 
				case tAshort: case tAinteger: case tAlong:
					createIrSrAd(ppcStw, sReg1, refReg, offset);
					break;
				case tChar: 
					assert false : "cg: type not implemented";
					break;
				case tLong:
					createIrSrAd(ppcStwu, sReg1, refReg, offset);
					createIrSrAd(ppcStw, sReg2, refReg, 4);
					break;
				case tFloat: 
					createIrSrAd(ppcStfs, sReg1, refReg, offset);
					break;
				case tDouble: 
					createIrSrAd(ppcStfd, sReg1, refReg, offset);
					break;
				default:
//					System.out.println(instr.toString());
//					System.out.println(instr.result.n);
//					System.out.println(type);
					assert false : "cg: wrong type";
				}
				break;	// sCstoreToField
			case sCstoreToArray:
				opds = instr.getOperands();
				refReg = opds[0].reg;
				indexReg = opds[1].reg;
				valReg = opds[2].reg;
				createItrap(ppcTwi, TOifequal, refReg, 0);
				createIrDrAd(ppcLha, res.regAux1, refReg, -arrayLenOffset);
				createItrap(ppcTw, TOifgeU, indexReg, res.regAux1);
				switch (opds[2].type) {
				case tByte: 
					createIrDrAsimm(ppcAddi, res.regAux2, refReg, arrayFirstOffset);
					createIrSrArB(ppcStbx, valReg, indexReg, res.regAux2);
					break;
				case tShort: 
					createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 1, 0, 30);
					createIrDrAsimm(ppcAddi, res.regAux2, refReg, arrayFirstOffset);
					createIrSrArB(ppcSthx, valReg, res.regAux1, res.regAux2);
					break;
				case tInteger: case tRef:
				case tAref: case tAboolean: case tAchar: case tAfloat: case tAdouble:
				case tAbyte: case tAshort: case tAinteger: case tAlong:
					createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 2, 0, 29);
					createIrDrAsimm(ppcAddi, res.regAux2, refReg, arrayFirstOffset);
					createIrSrArB(ppcStwx, valReg, res.regAux1, res.regAux2);
					break;
				case tLong: 
					createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 3, 0, 28);
					createIrDrAsimm(ppcAddi, res.regAux2, refReg, arrayFirstOffset);
					createIrSrArB(ppcStwux, valReg, res.regAux1, res.regAux2);
					createIrSrAd(ppcStw, opds[2].regLong, res.regAux1, 4);
					break;
				case tFloat: 
					createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 2, 0, 29);
					createIrDrAsimm(ppcAddi, res.regAux2, refReg, arrayFirstOffset);
					createIrSrArB(ppcStfsx, valReg, res.regAux1, res.regAux2);
					break;
				case tDouble: 
					createIrArSSHMBME(ppcRlwinm, res.regAux1, indexReg, 3, 0, 28);
					createIrDrAsimm(ppcAddi, res.regAux2, refReg, arrayFirstOffset);
					createIrSrArB(ppcStfdx, valReg, res.regAux1, res.regAux2);
					break;
				case tChar: 
					assert false : "cg: type char not implemented";
				break;
				default:
					assert false : "cg: type not implemented";
				}
				break;	// sCstoreToArray
			case sCadd:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				switch (res.type) {
				case tInteger:
					if (sReg1 < 0) {
						int immVal = ((Constant)opds[0].constant).valueH;
						createIrDrAsimm(ppcAddi, dReg, sReg2, immVal);
					} else if (sReg2 < 0) {
						int immVal = ((Constant)opds[1].constant).valueH;
						createIrDrAsimm(ppcAddi, dReg, sReg1, immVal);
					} else {
						createIrDrArB(ppcAdd, dReg, sReg1, sReg2);
					}
					break;
				case tLong:
					createIrDrArB(ppcAddc, dReg, sReg1, sReg2);
					createIrDrArB(ppcAdde, res.regLong, opds[0].regLong, opds[1].regLong);
					break;
				case tFloat:
					createIrDrArB(ppcFadds, dReg, sReg1, sReg2);
					break;
				case tDouble:
					createIrDrArB(ppcFadd, dReg, sReg1, sReg2);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCsub:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				switch (res.type) {
				case tInteger:
					if (sReg1 < 0) {
						int immVal = ((Constant)opds[0].constant).valueH;
						createIrDrAsimm(ppcAddi, dReg, sReg2, -immVal);
					} else if (sReg2 < 0) {
						int immVal = ((Constant)opds[1].constant).valueH;
						createIrDrAsimm(ppcAddi, dReg, sReg1, -immVal);
					} else {
						createIrDrArB(ppcSubf, dReg, sReg2, sReg1);
					}
					break;
				case tLong:
					createIrDrArB(ppcSubfc, dReg, sReg2, sReg1);
					createIrDrArB(ppcSubfe, res.regLong, opds[1].regLong, opds[0].regLong);
					break;
				case tFloat:
					createIrDrArB(ppcFsubs, dReg, sReg1, sReg2);
					break;
				case tDouble:
					createIrDrArB(ppcFsub, dReg, sReg1, sReg2);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCmul:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				switch (res.type) {
				case tInteger:
					if (sReg1 < 0) {
						int immVal = ((Constant)opds[0].constant).valueH;
						createIrDrAsimm(ppcMulli, dReg, sReg2, immVal);
					} else if (sReg2 < 0) {
						int immVal = ((Constant)opds[1].constant).valueH;
						createIrDrAsimm(ppcMulli, dReg, sReg1, immVal);
					} else {
						createIrDrArB(ppcMullw, dReg, sReg1, sReg2);
					}
					break;
				case tLong:
					createIrDrArB(ppcMullw, res.regAux1, opds[0].regLong, sReg2);
					createIrDrArB(ppcMullw, res.regAux2, sReg1, opds[1].regLong);
					createIrDrArB(ppcAdd, res.regAux1, res.regAux1, res.regAux2);
					createIrDrArB(ppcMulhwu, res.regAux2, sReg1, sReg2);
					createIrDrArB(ppcMullw, res.reg, sReg1, sReg2);
					createIrDrArB(ppcAdd, res.regLong, res.regAux1, res.regAux2);
					break;
				case tFloat:
					createIrDrArC(ppcFmuls, dReg, sReg1, sReg2);
					break;
				case tDouble:
					createIrDrArC(ppcFmul, dReg, sReg1, sReg2);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCdiv:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				switch (res.type) {
				case tByte: case tShort: case tInteger:
					createItrap(ppcTwi, TOifequal, sReg2, 0);
					createIrDrArB(ppcDivw, dReg, sReg1, sReg2);
					break;
				case tLong:
					assert false : "cg: type not implemented";
					break;
				case tFloat:
					createIrDrArB(ppcFdivs, dReg, sReg1, sReg2);
					break;
				case tDouble:
					createIrDrArB(ppcFdiv, dReg, sReg1, sReg2);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCrem:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				switch (res.type) {
				case tByte: case tShort: case tInteger:
					createItrap(ppcTwi, TOifequal, sReg2, 0);
					createIrDrArB(ppcDivw, res.regAux1, sReg1, sReg2);
					createIrDrArB(ppcMullw, res.regAux1, res.regAux1, sReg2);
					createIrDrArB(ppcSubf, dReg, res.regAux1 ,sReg1);
					break;
				case tLong:
					assert false : "cg: type not implemented";
					break;
				case tFloat:
					assert false : "cg: type not implemented";
					break;
				case tDouble:
					assert false : "cg: type not implemented";
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCneg:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				dReg = res.reg;
				type = res.type;
				if (type == tInteger)
					createIrDrA(ppcNeg, dReg, sReg1);
				else if (type == tLong) {
					createIrDrAsimm(ppcSubfic, dReg, sReg1, 0);
					createIrDrA(ppcSubfze, res.regLong, opds[0].regLong);
				} else if (type == tFloat || type == tDouble)
					createIrDrB(ppcFneg, dReg, sReg1);
				else
					assert false : "cg: wrong type";
				break;
			case sCshl:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				type = res.type;
				if (type == tInteger) {
					if (sReg2 < 0) {
						int immVal = ((Constant)opds[1].constant).valueH % 32;
						createIrArSSHMBME(ppcRlwinm, dReg, sReg1, immVal, 0, 31-immVal);
					} else {
						createIrArSSHMBME(ppcRlwinm, 0, sReg2, 0, 27, 31);
						createIrArSrB(ppcSlw, dReg, sReg1, 0);
					}
				} else if (type == tLong) {
					if (sReg2 < 0) {	
						int immVal = ((Constant)opds[1].constant).valueH % 64;
						if (immVal < 32) {
							createIrArSSHMBME(ppcRlwinm, res.regLong, sReg1, immVal, 32-immVal, 31);
							createIrArSSHMBME(ppcRlwimi, res.regLong, opds[0].regLong, immVal, 0, 31-immVal);
							createIrArSSHMBME(ppcRlwinm, dReg, sReg1, immVal, 0, 31-immVal);
						} else {
							createIrDrAsimm(ppcAddi, dReg, 0, 0);
							createIrArSSHMBME(ppcRlwinm, res.regLong, sReg1, immVal-32, 0, 63-immVal);
						}
					} else { // gibt Problem wenn shift > 32
						createIrArSSHMBME(ppcRlwinm, 0, sReg2, 0, 26, 31);
						createIrArSrB(ppcSlw, res.regAux1, sReg1, 0);
						createIrArSrB(ppcSlw, res.regLong, opds[0].regLong, 0);
						createIrArSrB(ppcOr, res.regLong, res.regLong, res.regAux1);
						createIrArSrB(ppcSlw, dReg, sReg1, 0);
					}
				} else
					assert false : "cg: wrong type";
				break;
			case sCshr:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				type = res.type;
				if (type == tInteger) {
					if (sReg2 < 0) {
						int immVal = ((Constant)opds[1].constant).valueH % 32;
						createIrArSSH(ppcSrawi, dReg, sReg1, immVal);
					} else {
						createIrArSSHMBME(ppcRlwinm, 0, sReg2, 0, 27, 31);
						createIrArSrB(ppcSraw, dReg, sReg1, 0);
					}
				} else if (type == tLong) {
					if (sReg2 < 0) {	// gibt Problem wenn shift > 32
						int immVal = ((Constant)opds[1].constant).valueH % 64;
						createIrArSSHMBME(ppcRlwinm, dReg, sReg1, 32-immVal, immVal, 31);
						createIrArSSHMBME(ppcRlwimi, dReg, opds[0].regLong, 32-immVal, 0, immVal-1);
						createIrArSSH(ppcSrawi, res.regLong, opds[0].regLong, immVal);
					} else {
						createIrArSSHMBME(ppcRlwinm, 0, sReg2, 0, 27, 31);
						createIrArSrB(ppcSlw, res.regAux1, sReg1, 0);
						createIrArSrB(ppcSlw, res.regLong, opds[0].regLong, 0);
						createIrArSrB(ppcOr, res.regLong, res.regLong, res.regAux1);
						createIrArSrB(ppcSlw, dReg, sReg1, 0);
					}
				} else
					assert false : "cg: wrong type";
				break;
			case sCushr:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				type = res.type;
				if (type == tInteger) {
					if (sReg2 < 0) {
						int immVal = ((Constant)opds[1].constant).valueH % 32;
						createIrArSSHMBME(ppcRlwinm, dReg, sReg1, 32-immVal, immVal, 31);
					} else {
						createIrArSSHMBME(ppcRlwinm, 0, sReg2, 0, 27, 31);
						createIrArSrB(ppcSrw, dReg, sReg1, 0);
					}
				} else if (type == tLong) {
					if (sReg2 < 0) {	// gibt Problem wenn shift > 32
						int immVal = ((Constant)opds[1].constant).valueH % 64;
						createIrArSSHMBME(ppcRlwinm, dReg, sReg1, 32-immVal, immVal, 31);
						createIrArSSHMBME(ppcRlwimi, dReg, opds[0].regLong, 32-immVal, 0, immVal-1);
						createIrArSSH(ppcSrawi, res.regLong, opds[0].regLong, immVal);
					} else {
						createIrArSSHMBME(ppcRlwinm, 0, sReg2, 0, 27, 31);
						createIrArSrB(ppcSlw, res.regAux1, sReg1, 0);
						createIrArSrB(ppcSlw, res.regLong, opds[0].regLong, 0);
						createIrArSrB(ppcOr, res.regLong, res.regLong, res.regAux1);
						createIrArSrB(ppcSlw, dReg, sReg1, 0);
					}
				} else
					assert false : "cg: wrong type";
				break;
			case sCand:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				if (res.type == tInteger) {
					if (sReg1 < 0) {
						int immVal = ((Constant)opds[0].constant).valueH;
						createIrArSuimm(ppcAndi, dReg, sReg2, immVal);
					} else if (sReg2 < 0) {
						int immVal = ((Constant)opds[1].constant).valueH;
						createIrArSuimm(ppcAndi, dReg, sReg1, immVal);
					} else
						createIrArSrB(ppcAnd, dReg, sReg1, sReg2);
				} else if (res.type == tLong) {
					if (sReg1 < 0) {
						int immVal = ((Constant)opds[0].constant).valueL;
						createIrArSrB(ppcOr, res.regLong, opds[1].regLong, opds[1].regLong);
						createIrArSuimm(ppcAndi, dReg, sReg2, (int)immVal);
					} else if (sReg2 < 0) {
						int immVal = ((Constant)opds[1].constant).valueL;
						createIrArSrB(ppcOr, res.regLong, opds[0].regLong, opds[0].regLong);
						createIrArSuimm(ppcAndi, dReg, sReg1, (int)immVal);
					} else {
						createIrArSrB(ppcAnd, res.regLong, opds[0].regLong, opds[1].regLong);
						createIrArSrB(ppcAnd, dReg, sReg1, sReg2);
					}
				} else
					assert false : "cg: wrong type";
			break;
			case sCor:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				if (res.type == tInteger) {
					if (sReg1 < 0) {
						int immVal = ((Constant)opds[0].constant).valueH;
						createIrArSuimm(ppcOri, dReg, sReg2, immVal);
					} else if (sReg2 < 0) {
						int immVal = ((Constant)opds[1].constant).valueH;
						createIrArSuimm(ppcOri, dReg, sReg1, immVal);
					} else
						createIrArSrB(ppcOr, dReg, sReg1, sReg2);
				} else if (res.type == tLong) {
					if (sReg1 < 0) {
						int immVal = ((Constant)opds[0].constant).valueL;
						createIrArSrB(ppcOr, res.regLong, opds[1].regLong, opds[1].regLong);
						createIrArSuimm(ppcOri, dReg, sReg2, (int)immVal);
					} else if (sReg2 < 0) {
						int immVal = ((Constant)opds[1].constant).valueL;
						createIrArSrB(ppcOr, res.regLong, opds[0].regLong, opds[0].regLong);
						createIrArSuimm(ppcOri, dReg, sReg1, (int)immVal);
					} else {
						createIrArSrB(ppcOr, res.regLong, opds[0].regLong, opds[1].regLong);
						createIrArSrB(ppcOr, dReg, sReg1, sReg2);
					}
				} else
					assert false : "cg: wrong type";
				break;
			case sCxor:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				if (res.type == tInteger) {
					if (sReg1 < 0) {
						int immVal = ((Constant)opds[0].constant).valueH;
						createIrArSuimm(ppcXori, dReg, sReg2, immVal);
					} else if (sReg2 < 0) {
						int immVal = ((Constant)opds[1].constant).valueH;
						createIrArSuimm(ppcXori, dReg, sReg1, immVal);
					} else
						createIrArSrB(ppcXor, dReg, sReg1, sReg2);
				} else if (res.type == tLong) {
					if (sReg1 < 0) {
						int immVal = ((Constant)opds[0].constant).valueL;
						createIrArSrB(ppcOr, res.regLong, opds[1].regLong, opds[1].regLong);
						createIrArSuimm(ppcXori, dReg, sReg2, (int)immVal);
					} else if (sReg2 < 0) {
						int immVal = ((Constant)opds[1].constant).valueL;
						createIrArSrB(ppcOr, res.regLong, opds[0].regLong, opds[0].regLong);
						createIrArSuimm(ppcXori, dReg, sReg1, (int)immVal);
					} else {
						createIrArSrB(ppcXor, res.regLong, opds[0].regLong, opds[1].regLong);
						createIrArSrB(ppcXor, dReg, sReg1, sReg2);
					}
				} else
					assert false : "cg: wrong type";
				break;
			case sCconvInt:	// int -> other type
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				dReg = res.reg;
				switch (res.type) {
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
					createIrArSuimm(ppcXoris, 0, sReg1, 0x8000);
					createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset+4);
					Item item = null;	// hier ref auf 2^52;
					loadConstantAndFixup(res.regAux1, item);
					createIrDrAd(ppcLfd, dReg, res.regAux1, constForDoubleConv);
					createIrDrAd(ppcLfd, 0, stackPtr, tempStorageOffset);
					createIrDrArB(ppcFsub, dReg, 0, dReg);
					createIrDrB(ppcFrsp, dReg, dReg);
					break;
				case tDouble:
					item = null;
					createIrArSuimm(ppcXoris, 0, sReg1, 0x8000);
					createIrSrAd(ppcStw, 0, stackPtr, tempStorageOffset+4);
					loadConstantAndFixup(res.regAux1, item);
					createIrDrAd(ppcLfd, dReg, res.regAux1, constForDoubleConv);
					createIrDrAd(ppcLfd, 0, stackPtr, tempStorageOffset);
					createIrDrArB(ppcFsub, dReg, 0, dReg);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCconvLong:	// long -> other type
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				dReg = res.reg;
				switch (res.type) {
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
					// noch machen
					break;
				case tDouble:
					// noch machen
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCconvFloat:	// float -> other type
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				dReg = res.reg;
				switch (res.type) {
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
					createIrDrB(ppcFctiw, 0, sReg1);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, dReg, stackPtr, tempStorageOffset + 4);
					break;
				case tLong:	// noch machen
					break;
				case tDouble:
					createIrDrB(ppcFmr, dReg, sReg1);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCconvDouble:	// double -> other type
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				dReg = res.reg;
				switch (res.type) {
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
					createIrDrB(ppcFctiw, 0, sReg1);
					createIrSrAd(ppcStfd, 0, stackPtr, tempStorageOffset);
					createIrDrAd(ppcLwz, dReg, stackPtr, tempStorageOffset + 4);
					tempStorage = true;
					break;
				case tLong:	
					// noch machen
					break;
				case tFloat:
					createIrDrB(ppcFrsp, dReg, sReg1);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCcmpl:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = res.reg;
				type = opds[0].type;
				if (type == tLong) {
					int sReg1L = opds[0].regLong;
					int sReg2L = opds[1].regLong;
					createICRFrArB(ppcCmp, CRF0, sReg1L, sReg2L);
					createICRFrArB(ppcCmpl, CRF1, sReg1, sReg2);
					instr = node.instructions[i+1];
					assert instr.ssaOpcode == sCbranch : "sCcompl is not followed by branch instruction";
					bci = ssa.cfg.code[node.lastBCA] & 0xff;
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
					} else 
						assert false : "wrong branch instruction after sCcompl";
				} else if (type == tFloat  || type == tDouble) {
					createICRFrArB(ppcFcmpu, CRF1, sReg1, sReg2);
					instr = node.instructions[i+1];
					assert instr.ssaOpcode == sCbranch;
					opds = instr.getOperands();
					createICRFrASimm(ppcCmpi, CRF0, sReg1, 0);
					bci = ssa.cfg.code[node.lastBCA] & 0xff;
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
					else 
						assert false : "wrong branch instruction after sCcompl";
				} else 
					assert false : "cg: wrong type";
				i++;
				break;
			case sCcmpg:
				assert false : "ssa instruction not implemented";
			break;
			case sCinstanceof:
				break;
			case sCalength:
				opds = instr.getOperands();
				refReg = opds[0].reg;
				createItrap(ppcTwi, TOifequal, refReg, 0);
				createIrDrAd(ppcLha, res.reg , refReg, -arrayLenOffset);
				break;
			case sCcall:
				opds = instr.getOperands();
				Call call = (Call)instr;
				//				System.out.println("Call to " + call.item.name);
				//				System.out.printf("accAndPropFlags = 0x%1$x\n", call.item.accAndPropFlags);
				if ((call.item.accAndPropFlags & (1 << dpfSynthetic)) != 0) {
					if (call.item.name.equals(HString.getHString("GET1"))) {	//GET1
						createIrDrAd(ppcLbz, res.reg, opds[0].reg, 0);
						createIrArS(ppcExtsb, res.reg, res.reg);
					} else if (call.item.name.equals(HString.getHString("GET2"))) { // GET2
						createIrDrAd(ppcLha, res.reg, opds[0].reg, 0);
					} else if (call.item.name.equals(HString.getHString("GET4"))) { // GET4
						createIrDrAd(ppcLwz, res.reg, opds[0].reg, 0);
					} else if (call.item.name.equals(HString.getHString("GET8"))) {
						createIrDrAd(ppcLwz, res.reg, opds[0].reg, 0);
					} else if (call.item.name.equals(HString.getHString("PUT1"))) {
						createIrSrAd(ppcStb, opds[1].reg, opds[0].reg, 0);
					} else if (call.item.name.equals(HString.getHString("PUT2"))) {
						createIrSrAd(ppcSth, opds[1].reg, opds[0].reg, 0);
					} else if (call.item.name.equals(HString.getHString("PUT4"))) {
						createIrSrAd(ppcStw, opds[1].reg, opds[0].reg, 0);
					} else if (call.item.name.equals(HString.getHString("GETGPR"))) {
						createIrArSrB(ppcOr, res.reg, opds[0].reg, opds[0].reg);
					} else if (call.item.name.equals(HString.getHString("GETFPR"))) {
						createIrDrB(ppcFmr, res.reg, opds[0].reg);
					} else if (call.item.name.equals(HString.getHString("GETSPR"))) {
						int spr = ((Constant)opds[0].constant).valueH;
						//						System.out.println("spr = " + spr);
						createIrSspr(ppcMfspr, spr, res.reg);
					} else if (call.item.name.equals(HString.getHString("PUTGPR"))) {
						int gpr = ((Constant)opds[0].constant).valueH;
						createIrArSrB(ppcOr, gpr, opds[1].reg, opds[1].reg);
					} else if (call.item.name.equals(HString.getHString("PUTFPR"))) {
						createIrDrB(ppcFmr, opds[1].reg, opds[0].reg);
					} else if (call.item.name.equals(HString.getHString("PUTSPR"))) {
						createIrArSrB(ppcOr, 0, opds[1].reg, opds[1].reg);
						int spr = ((Constant)opds[0].constant).valueH;
						createIrSspr(ppcMtspr, spr, 0);
					} else if (call.item.name.equals(HString.getHString("ASM"))) {
						//	int code = InstructionDecoder.getCode(opds[0].toString());
						//						System.out.println("asm1 = " + ((StringLiteral)opds[0].constant).string);
						//						System.out.println("asm2 = " + InstructionDecoder.getCode(((StringLiteral)opds[0].constant).string.toString()));
						instructions[iCount] = InstructionDecoder.getCode(((StringLiteral)opds[0].constant).string.toString());
						iCount++;
						int len = instructions.length;
						if (iCount == len) {
							int[] newInstructions = new int[2 * len];
							for (int k = 0; k < len; k++)
								newInstructions[k] = instructions[k];
							instructions = newInstructions;
						}
					}
				} else {
					if ((call.item.accAndPropFlags & (1<<apfStatic)) != 0) {	// invokestatic

						loadConstantAndFixup(res.regAux1, call.item);	// addr of method
						createIrSspr(ppcMtspr, LR, res.regAux1);
					} else if ((call.item.accAndPropFlags & (1<<apfInterface)) != 0) {	// invokeinterface
						refReg = opds[0].reg;
						offset = call.item.offset;
						createItrap(ppcTwi, TOifequal, refReg, 0);
						createIrDrAd(ppcLwz, res.regAux1, refReg, -4);
						createIrDrAd(ppcLwz, res.regAux1, res.regAux1, -offset);
						createIrDrAd(ppcLwz, res.regAux1, res.regAux1, 0);
						createIrSspr(ppcMtspr, LR, res.regAux1);
					} else {	// invokevirtual and invokespecial
						refReg = opds[0].reg;
						offset = call.item.offset;
						createItrap(ppcTwi, TOifequal, refReg, 0);
						createIrDrAd(ppcLwz, res.regAux1, refReg, -4);
						createIrDrAd(ppcLwz, res.regAux1, res.regAux1, -offset);
						createIrSspr(ppcMtspr, LR, res.regAux1);
					}
					for (int k = 0; k < nofGPR; k++) destGPR[k] = 0;
					for (int k = 0; k < nofGPR; k++) destFPR[k] = 0;
					for (int k = 0; k < opds.length; k++) {
						type = opds[k].type;
						if (type == tLong) {
							destGPR[opds[k].regLong] = k + paramStartGPR;
							destGPR[opds[k].reg] = k + 1 + paramStartGPR;
						} else if (type == tFloat || type == tDouble) {
							destFPR[opds[k].reg] = k + paramStartFPR;
						} else
							destGPR[opds[k].reg] = k + paramStartGPR;
					}
					//				System.out.println("move before call");
					//				for (int k = 0; k < nofGPR; k++) System.out.println("destGPR["+k+"]="+destGPR[k]);
					for (int k = 0; k < nofGPR; k++) {
						if (destGPR[k] != 0 && destGPR[k] != k) {
							if (destGPR[destGPR[k]] == 0) {
								createIrArSrB(ppcOr, destGPR[k], k, k);
								destGPR[k] = 0;
							} else {
								createIrArSrB(ppcOr, 0, destGPR[destGPR[k]], destGPR[destGPR[k]]);
								createIrArSrB(ppcOr, destGPR[destGPR[k]], destGPR[k], destGPR[k]);
								createIrArSrB(ppcOr, destGPR[k], 0, 0);
								int temp = destGPR[k];
								destGPR[k] = destGPR[destGPR[k]];
								destGPR[destGPR[k]] = temp;
								//							k--;
							}
						}
					}
					for (int k = 0; k < nofFPR; k++) {
						if (destFPR[k] != 0 && destFPR[k] != k) {
							if (destFPR[destFPR[k]] == 0) {
								createIrDrB(ppcFmr, destFPR[k], k);
								destGPR[k] = 0;
							} else {
								createIrDrB(ppcFmr, 0, destFPR[destFPR[k]]);
								createIrDrB(ppcFmr, destFPR[destFPR[k]], destFPR[k]);
								createIrDrB(ppcFmr, destFPR[k], 0);
								int temp = destFPR[k];
								destFPR[k] = destFPR[destFPR[k]];
								destFPR[destFPR[k]] = temp;
								//							k--;
							}
						}
					}
					createIBOBI(ppcBclr, BOalways, 0);
					type = res.type;
					if (type == tLong) {
						createIrArSrB(ppcOr, res.reg, returnGPR1, returnGPR1);
						createIrArSrB(ppcOr, res.regLong, returnGPR1 + 1, returnGPR1 + 1);
					} else if (type == tFloat || type == tDouble) {
						createIrDrB(ppcFmr, res.reg, returnFPR);
					} else if (type == tVoid) {
					} else
						createIrArSrB(ppcOr, res.reg, returnGPR1, returnGPR1);
				}
				break;
			case sCnew:
				opds = instr.getOperands();
				if (opds.length == 1) {
					Item item = null;// item = ref
					switch (res.type) {
					case tRef:	// bCnew
						loadConstantAndFixup(paramStartGPR, item);	// addr of new
						createIrSspr(ppcMtspr, LR, paramStartGPR);
						loadConstantAndFixup(paramStartGPR, item);	// ref
						createIBOBI(ppcBclr, BOalways, 0);
						createIrArSrB(ppcOr, instr.result.reg, returnGPR1, returnGPR1);
						break;
					case tAboolean: case tAchar: case tAfloat: case tAdouble:
					case tAbyte: case tAshort: case tAinteger: case tAlong:	// bCnewarray
						opds = instr.getOperands();
						createIrArSrB(ppcOr, paramStartGPR, opds[0].reg, opds[0].reg);	// nof elems
						createItrapSimm(ppcTwi, TOifless, paramStartGPR, 0);
						loadConstantAndFixup(paramStartGPR + 1, item);	// addr of newarray
						createIrSspr(ppcMtspr, LR, paramStartGPR + 1);
						createIrDrAsimm(ppcAddi, paramStartGPR + 1, 0, instr.result.type - 10);	// type
						createIBOBI(ppcBclr, BOalways, 0);
						createIrArSrB(ppcOr, instr.result.reg, returnGPR1, returnGPR1);
						break;
					case tAref:	// bCanewarray
						opds = instr.getOperands();
						createIrArSrB(ppcOr, paramStartGPR, opds[0].reg, opds[0].reg);	// nof elems
						createItrapSimm(ppcTwi, TOifless, paramStartGPR, 0);
						loadConstantAndFixup(paramStartGPR + 1, item);	// addr of anewarray
						createIrSspr(ppcMtspr, LR, paramStartGPR + 1);
						loadConstantAndFixup(paramStartGPR + 1, item);	// ref
						createIBOBI(ppcBclr, BOalways, 0);
						createIrArSrB(ppcOr, instr.result.reg, returnGPR1, returnGPR1);
						break;
					default:
						assert false : "cg: instruction not implemented";
					}
				} else { // bCmultianewarray:
//					assert false : "cg: instruction not implemented";
				}
				break;
			case sCreturn:
				opds = instr.getOperands();
				bci = ssa.cfg.code[node.lastBCA] & 0xff;
				switch (bci) {
				case bCreturn:
					break;
				case bCireturn:
				case bCareturn:
					createIrArSrB(ppcOr, returnGPR1, opds[0].reg, opds[0].reg);
					break;
				case bClreturn:
					createIrArSrB(ppcOr, returnGPR1, opds[0].reg, opds[0].reg);
					createIrArSrB(ppcOr, returnGPR2, opds[0].regLong, opds[0].regLong);
					break;
				case bCfreturn:
				case bCdreturn:
					createIrDrB(ppcFmr, returnFPR, opds[0].reg);
					break;
				default:
					assert false : "cg: return instruction not implemented";
				}
				if (node.next != null)	// last node needs no branch
					createIli(ppcB, 0, false);
				break;
			case sCthrow:
/*				refReg = opds[0].reg;
				offset = call.item.offset;
				createItrap(ppcTwi, TOifequal, refReg, 0);
			createIrDrAd(ppcLwz, res.regAux1, refReg, -4);
			createIrDrAd(ppcLwz, res.regAux1, res.regAux1, -offset);
			createIrDrAd(ppcLwz, res.regAux1, res.regAux1, 0);
			createIrSspr(ppcMtspr, LR, res.regAux1);
				opds = instr.getOperands();
				createIrArSrB(ppcOr, instr.result.reg, opds[0].reg, opds[0].reg);*/
				break;
			case sCbranch:
				bci = ssa.cfg.code[node.lastBCA] & 0xff;
				switch (bci) {
				case bCgoto:
					createIli(ppcB, 0, false);
					break;
				case bCif_acmpeq:
				case bCif_acmpne:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					sReg2 = opds[1].reg;
					createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);
					if (bci == bCif_acmpeq)
						createIBOBIBD(ppcBc, BOtrue, (28-4*CRF0+EQ), 0);
					else
						createIBOBIBD(ppcBc, BOfalse, (28-4*CRF0+EQ), 0);
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
//					assertEquals("cg: wrong type", opds[0].type, tInteger);
//					assertEquals("cg: wrong type", opds[1].type, tInteger);
					if (sReg1 < 0) {
						if (opds[0].constant != null) {
//							System.out.println("constant is not null");
							int immVal = ((Constant)opds[0].constant).valueH;
							if ((immVal >= -32768) && (immVal <= 32767))
								createICRFrASimm(ppcCmpi, CRF0, sReg2, immVal);
							else
								createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);
						} else
								createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);					
					} else if (sReg2 < 0) {
						if (opds[1].constant != null) {
							int immVal = ((Constant)opds[1].constant).valueH;
							if ((immVal >= -32768) && (immVal <= 32767)) {
								inverted = true;
								createICRFrASimm(ppcCmpi, CRF0, sReg1, immVal);
							} else
								createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);
						} else
							createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);					
					} else {
						createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);
					}
					if (!inverted) {
						if (bci == bCif_icmpeq) 
							createIBOBIBD(ppcBc, BOtrue, (28-4*CRF0+EQ), 0);
						else if (bci == bCif_icmpne)
							createIBOBIBD(ppcBc, BOfalse, (28-4*CRF0+EQ), 0);
						else if (bci == bCif_icmplt)
							createIBOBIBD(ppcBc, BOtrue, (28-4*CRF0+LT), 0);
						else if (bci == bCif_icmpge)
							createIBOBIBD(ppcBc, BOfalse, (28-4*CRF0+LT), 0);
						else if (bci == bCif_icmpgt)
							createIBOBIBD(ppcBc, BOtrue, (28-4*CRF0+GT), 0);
						else if (bci == bCif_icmple)
							createIBOBIBD(ppcBc, BOfalse, (28-4*CRF0+GT), 0);
					} else {
						if (bci == bCif_icmpeq) 
							createIBOBIBD(ppcBc, BOtrue, (28-4*CRF0+EQ), 0);
						else if (bci == bCif_icmpne)
							createIBOBIBD(ppcBc, BOfalse, (28-4*CRF0+EQ), 0);
						else if (bci == bCif_icmplt)
							createIBOBIBD(ppcBc, BOtrue, (28-4*CRF0+GT), 0);
						else if (bci == bCif_icmpge)
							createIBOBIBD(ppcBc, BOfalse, (28-4*CRF0+GT), 0);
						else if (bci == bCif_icmpgt)
							createIBOBIBD(ppcBc, BOtrue, (28-4*CRF0+LT), 0);
						else if (bci == bCif_icmple)
							createIBOBIBD(ppcBc, BOfalse, (28-4*CRF0+LT), 0);
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
//					assertEquals("cg: wrong type", opds[0].type, tInteger);
					createICRFrASimm(ppcCmpi, CRF0, sReg1, 0);
					if (bci == bCifeq) 
						createIBOBIBD(ppcBc, BOtrue, (28-4*CRF0+EQ), 0);
					else if (bci == bCifne)
						createIBOBIBD(ppcBc, BOfalse, (28-4*CRF0+EQ), 0);
					else if (bci == bCiflt)
						createIBOBIBD(ppcBc, BOtrue, (28-4*CRF0+LT), 0);
					else if (bci == bCifge)
						createIBOBIBD(ppcBc, BOfalse, (28-4*CRF0+LT), 0);
					else if (bci == bCifgt)
						createIBOBIBD(ppcBc, BOtrue, (28-4*CRF0+GT), 0);
					else if (bci == bCifle)
						createIBOBIBD(ppcBc, BOfalse, (28-4*CRF0+GT), 0);
					break;
				case bCifnonnull:
				case bCifnull:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					createICRFrASimm(ppcCmpi, CRF0, sReg1, 0);
					if (bci == bCifnonnull)
						createIBOBIBD(ppcBc, BOfalse, (28-4*CRF0+EQ), 0);
					else
						createIBOBIBD(ppcBc, BOtrue, (28-4*CRF0+EQ), 0);
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
						createICRFrASimm(ppcCmpi, CRF0, sReg1, low + k);
						createIBOBIBD(ppcBc, BOtrue, (28-4*CRF0+EQ), 0);
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
						createICRFrASimm(ppcCmpi, CRF0, sReg1, key);
						createIBOBIBD(ppcBc, BOtrue, (28-4*CRF0+EQ), 0);
					}
					createIli(ppcB, nofPairs, true);
					break;
				default:
//					System.out.println(bci);
					assert false : "cg: no such branch instruction";
				}
				break;
			case sCregMove:
				opds = instr.getOperands();
				createIrArSrB(ppcOr, instr.result.reg, opds[0].reg, opds[0].reg);
				break;
			default:
				assert false : "cg: no code generated for " + instr.scMnemonics[instr.ssaOpcode] + " function";
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

	private void createIrDrArB(int opCode, int rD, int rA, int rB) {
		instructions[iCount] = opCode | (rD << 21) | (rA << 16) | (rB << 11);
		incInstructionNum();
	}

	private void createIrDrArC(int opCode, int rD, int rA, int rC) {
		instructions[iCount] = opCode | (rD << 21) | (rA << 16) | (rC << 6);
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

	private void createIrSrASimm(int opCode, int rS, int rA, int simm) {
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
		instructions[iCount] = opCode | (BO << 21) | (BI << 16) | (BD << 2);
		incInstructionNum();
	}

	private void createIBOBI(int opCode, int BO, int BI) {
		instructions[iCount] = opCode | (BO << 21) | (BI << 16);
		incInstructionNum();
	}

	private void createICRFrArB(int opCode, int crfD, int rA, int rB) {
		instructions[iCount] = opCode | (crfD << 23) | (rA << 16) | (rB << 11);
//		System.out.println(InstructionDecoder.getMnemonic(instructions[iCount]));
		incInstructionNum();
	}

	private void createICRFrASimm(int opCode, int crfD, int rA, int simm) {
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
	
	private void createIrDrA(int opCode, int rD, int rA) {
		instructions[iCount] = opCode | (rD << 21) | (rA << 16);
		incInstructionNum();
	}

	private void createIrDrB(int opCode, int rD, int rB) {
		instructions[iCount] = opCode | (rD << 21) | (rB << 11);
		incInstructionNum();
	}

	private void createIrSspr(int opCode, int spr, int rS) {
		int temp = ((spr & 0x1F) << 5) | ((spr & 0x3E0) >> 5);
		instructions[iCount] = opCode | (temp << 11) | (rS << 21);
		incInstructionNum();
	}

	private void createIrfi(int opCode) {
		instructions[iCount] = opCode;
		incInstructionNum();
	}

	private void loadConstant(int val, int reg) {
		int low = val & 0xffff;
		int high = (val >> 16) & 0xffff;
		if ((low >> 15) == 0) {
			if (low != 0) createIrDrAsimm(ppcAddi, reg, 0, low);
			if (high != 0) createIrDrAsimm(ppcAddis, reg, reg, high);
			if ((low == 0) && (high == 0)) createIrDrAsimm(ppcAddi, reg, 0, 0);
		} else {
			if (low != 0) createIrDrAsimm(ppcAddi, reg, 0, low);
			if (((high + 1) & 0xffff) != 0) createIrDrAsimm(ppcAddis, reg, reg, high + 1);
		}
	}
	
	private void loadConstantAndFixup(int reg, Item item) {
		assert lastFixup >= 0 && lastFixup <= 32768 : "fixup is out of range";
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
//		System.out.println("########## fCount = " + fCount);
		while (currFixup >= 0) {
//			if( fixups[currFixup] == null) System.out.println("########## fixups = null");
//			System.out.println("########## fixups.lenght = " + fixups.length);
//			System.out.println("########## currFixup = " + currFixup);
			Item item = fixups[currFixup];
			int addr;
			if (item == null) // item is null if constant is null is loaded (aconst_null) 
				addr = 0;
			else 
				addr = fixups[currFixup].address;
			int low = addr & 0xffff;
			int high = (addr >> 16) & 0xffff;
			if ((low >> 15) == 0) high++;
			int nextInstr = instructions[currInstr] & 0xffff;
			instructions[currInstr] = (instructions[currInstr] & 0xffff0000) | (low & 0xffff);
			instructions[currInstr+1] = (instructions[currInstr+1] & 0xffff0000) | (high & 0xffff);
			currInstr = nextInstr;
			currFixup--;
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
		int count = iCount;
		iCount = 0;
		createIrSrASimm(ppcStwu, stackPtr, stackPtr, -stackSize);
		createIrSspr(ppcMfspr, LR, 0);
		createIrSrASimm(ppcStw, 0, stackPtr, LRoffset);
		if (nofNonVolGPR > 0) {
			createIrSrAd(ppcStmw, nofGPR-nofNonVolGPR, stackPtr, GPRoffset);
		}
		if (nofNonVolFPR > 0) {
			for (int i = 0; i < nofNonVolFPR; i++)
				createIrSrAd(ppcStfd, topFPR-i, stackPtr, FPRoffset + i * 8);
		}
		for (int i = 0; i < nofMoveGPR; i++)
			createIrArSrB(ppcOr, topGPR-i, moveGPR[i]+paramStartGPR, moveGPR[i]+paramStartGPR);
		for (int i = 0; i < nofMoveFPR; i++)
			createIrDrB(ppcFmr, topFPR-i, moveFPR[i]+paramStartFPR);
		iCount = count;
	}

	private void insertPrologException() {
		iCount = 0;
		createIrSrASimm(ppcStwu, stackPtr, stackPtr, -stackSize);
		createIrSrASimm(ppcStw, 0, stackPtr, GPRoffset);
		createIrSspr(ppcMfspr, SRR0, 0);
		createIrSrASimm(ppcStw, 0, stackPtr, SRR0offset);
		createIrSspr(ppcMfspr, SRR1, 0);
		createIrSrASimm(ppcStw, 0, stackPtr, SRR1offset);
		createIrSspr(ppcMtspr, EID, 0);
		createIrSspr(ppcMfspr, LR, 0);
		createIrSrASimm(ppcStw, 0, stackPtr, LRoffset);
		createIrSspr(ppcMfspr, CTR, 0);
		createIrSrASimm(ppcStw, 0, stackPtr, CTRoffset);
		createIrD(ppcMfcr, 0);
		createIrSrASimm(ppcStw, 0, stackPtr, CRoffset);
		createIrSrAd(ppcStmw, 2, stackPtr, GPRoffset + 8);
	}

	private void insertEpilog(int stackSize) {
		if (nofNonVolFPR > 0) {
			for (int i = 0; i < nofNonVolFPR; i++)
				createIrDrAd(ppcLfd, topFPR-i, stackPtr, FPRoffset + i * 8);
		}
		if (nofNonVolGPR > 0)
			createIrDrAd(ppcLmw, nofGPR - nofNonVolGPR, stackPtr, GPRoffset);
		createIrDrAd(ppcLwz, 0, stackPtr, LRoffset);
		createIrSspr(ppcMtspr, LR, 0);
		createIrDrAsimm(ppcAddi, stackPtr, stackPtr, stackSize);
		createIBOBI(ppcBclr, BOalways, 0);
	}

	private void insertEpilogException(int stackSize) {
		createIrDrAd(ppcLmw, 2, stackPtr, GPRoffset + 8);
		createIrDrAd(ppcLwz, 0, stackPtr, CRoffset);
		createICRMrS(ppcMtcrf, 0xff, 0);
		createIrDrAd(ppcLwz, 0, stackPtr, CTRoffset);
		createIrSspr(ppcMtspr, CTR, 0);
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
	
	public void print(){
//		System.out.println("Method information for " + ssa.cfg.method.name);
		Method m = ssa.cfg.method;
//		System.out.println("Method has " + m.maxLocals + " locals where " + m.nofParams + " are parameters");
//		System.out.println("stackSize = " + stackSize);
//		System.out.println("Method uses " + nofNonVolGPR + " GPR and " + nofNonVolFPR + " FPR for locals where " + nofParamGPR + " GPR and " + nofParamFPR + " FPR are for parameters");
//		System.out.println();
		
		System.out.println("Code for Method: " + ssa.cfg.method.name);
		
		for (int i = 0; i < iCount; i++){
			System.out.print("\t" + Integer.toHexString(instructions[i]));
			System.out.print("\t[0x");
			System.out.print(Integer.toHexString(i*4));
			System.out.print("]\t" + InstructionDecoder.getMnemonic(instructions[i]));
			int opcode = (instructions[i] & 0xFC000000) >>> (31 - 5);
			if (opcode == 0x10) {
				int BD = (short)(instructions[i] & 0xFFFC);
				System.out.print(", [0x" + Integer.toHexString(BD + 4 * i) + "]\t");
			} else if (opcode == 0x12) {
				int li = (instructions[i] & 0x3FFFFFC) << 6 >> 6;
				System.out.print(", [0x" + Integer.toHexString(li + 4 * i) + "]\t");
			}
			System.out.println();
			}
	}

}