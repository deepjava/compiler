package ch.ntb.inf.deep.cgPPC;

import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.cfg.*;
import ch.ntb.inf.deep.ssa.*;
import ch.ntb.inf.deep.ssa.instruction.*;
import static org.junit.Assert.*;
import ch.ntb.inf.deep.classItems.*;

public class MachineCode implements SSAInstructionOpcs, SSAInstructionMnemonics, SSAValueType, InstructionOpcs, Registers, ICjvmInstructionOpcs, IClassFileConsts {
	private static final int maxNofParam = 32;
	private static final int defaultNofInstr = 16;
	private static final int stackPtr = 1;	// register for stack pointer
	private static int GPRoffset;	
	private static int FPRoffset;	
	private static int LRoffset;	
	
	private static final int arrayLenOffset = 8;	
	private static final int arrayFirstOffset = 12;
	private static final int refAddr = 16;
	private static final int refCD = 20;
	private static final int methodOffset = 24;
	private static final int cppOffset = 24;
	private static final int classVarOffset = 4;
	private static final int objVarOffset = 6;
	private static final int varOffset = 8;
	
	private static SSA ssa;	// reference to the SSA of a method
	int[] instructions;	//contains machine instructions for the ssa of a method
	int iCount;	//nof instructions for this method
	
	private static int nofParam;
	private static int nofParamGPR, nofParamFPR;
	static int nofNonVolGPR, nofNonVolFPR;
	private static int nofMoveGPR, nofMoveFPR;
	static int[] paramType = new int[maxNofParam];
	static boolean[] paramHasNonVolReg = new boolean[maxNofParam];
	static int[] paramRegNr = new int[maxNofParam];
	private static int[] moveGPR = new int[maxNofParam];
	private static int[] moveFPR = new int[maxNofParam];
	private static SSAValue[] lastExitSet;

	private int epilogStartInstr;
	private static int stackSize;
	private static boolean tempStorage;
	
	int fixup;

	public MachineCode(SSA ssa) {
		MachineCode.ssa = ssa;
		instructions = new int[defaultNofInstr];

		// make local copy
		int maxStackSlots = ssa.cfg.method.maxStackSlots;
		int i = maxStackSlots;
		while ((i < ssa.isParam.length) && ssa.isParam[i]) {
			paramType[i - maxStackSlots] = ssa.paramType[i];
			paramHasNonVolReg[i] = false;
			i++;
		}
		nofParam = i - maxStackSlots;
		assert nofParam <= maxNofParam : "method has too many parameters";
		
		System.out.println("build intervals for " + ssa.cfg.method.name);
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
		ssa.print(0);
		
		stackSize = calcStackSize();
		insertProlog();
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
		insertEpilog(stackSize);
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
			} else if (type == tFloat) {
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
			} else if (type == tDouble) {
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
				i++;
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
		FPRoffset = size - 12 - nofNonVolGPR * 4 - nofNonVolFPR * 8;
		return size;
	}

	private void translateSSA (SSANode node) {
		SSAValue[] opds;
		int sReg1, sReg2, dReg, refReg, indexReg, valReg, bci, offset;
		for (int i = 0; i < node.nofPhiFunc; i++) {
			SSAInstruction instr = node.phiFunctions[i];
			opds = instr.getOperands(); 
			if (instr.result.reg != opds[0].reg) {
//				System.out.println("insert move register");
//				createIrArSrB(ppcOr, instr.result.reg, opds[0].reg, opds[0].reg);
			}
			break;
			
		}
		for (int i = 0; i < node.nofInstr; i++) {
			SSAInstruction instr = node.instructions[i];
			System.out.println("ssa opcode = " + instr.scMnemonics[instr.ssaOpcode]);
			switch (instr.ssaOpcode) { 
			case sCloadConst:
				opds = instr.getOperands();
				dReg = instr.result.reg;
				switch (instr.result.type) {
				case tByte: case tShort: case tInteger:
					int immVal = (Integer)instr.result.constant;
					if ((immVal < -32768) || (immVal > 32767)) {
						loadConstant(immVal, dReg);
					} else {
						if ((instr.result.index >= 0) || (instr.result.join != null)) { // local variable
							createIrDrASimm(ppcAddi, dReg, 0, immVal);
						} else { // constant used for ssa instruction further down
							SSAValue val = instr.result;
							SSAValue startVal = node.instructions[0].result;
							SSAInstruction instr1 = node.instructions[val.end - startVal.n];
			System.out.println("val.end = " + val.end);
			System.out.println("startVal.n = " + startVal.n);
							switch (instr1.ssaOpcode) {	// instruction, where the const is used
							case sCadd:
							case sCand:
							case sCor:
							case sCxor:
							case sCsub:
							case sCmul:
							case sCbranch:
								break;
							default:
								createIrDrASimm(ppcAddi, dReg, 0, immVal);
							}
						}
					}
					break;
				case tLong:	
					Object obj = instr.result.constant;
					long immValLong;
					if (obj instanceof Long) immValLong = (Long)obj;	
					else immValLong = (Integer)obj;
					if ((immValLong < -32768) || (immValLong > 32767)) {
						loadConstant((int)(immValLong >> 32), instr.result.reg);
						loadConstant((int)immValLong, instr.result.regLong);
					} else {
						if (instr.result.index >= 0) { // local variable
							createIrDrASimm(ppcAddi, dReg, 0, (int)immValLong);
						} else { // constant used for ssa instruction further down
							SSAValue val = instr.result;
							SSAValue startVal = node.instructions[0].result;
							SSAInstruction instr1 = node.instructions[val.end - startVal.n];
							switch (instr1.ssaOpcode) {	// instruction, where the const is used
							case sCadd:
							case sCand:
							case sCor:
							case sCxor:
							case sCsub:
							case sCmul:
							case sCbranch:
								break;
							default:
								createIrDrASimm(ppcAddi, dReg, 0, (int)immValLong);
							}
						}
					}
					break;
				case tFloat:	// load from const pool
					loadConstantAndFixup(fixup, instr.result.regAux1);
					createIrDrAd(ppcLfs, instr.result.reg, instr.result.regAux1, cppOffset);
					break;
				case tDouble:
					loadConstantAndFixup(fixup, instr.result.regAux1);
					createIrDrAd(ppcLfd, instr.result.reg, instr.result.regAux1, cppOffset);
					break;
				case tRef:
					loadConstantAndFixup(fixup ,instr.result.reg);
					break;
				default:
					System.out.println("cfg = " + ssa.cfg.method.name);
					System.out.println("instr = " + scMnemonics[instr.ssaOpcode]);
					System.out.println("type = " + svNames[instr.result.type]);
					assert false : "cg: wrong type";
				}
				break;
			case sCadd:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = instr.result.reg;
				switch (instr.result.type) {
				case tByte: case tShort: case tInteger:
					if ((opds[0].index < 0) && (opds[0].constant != null)) {
						int immVal = (Integer)opds[0].constant;
						if ((immVal >= -32768) && (immVal <= 32767))
							createIrDrASimm(ppcAddi, dReg, sReg2, immVal);
						else
							createIrDrArB(ppcAdd, dReg, sReg1, sReg2);
					} else if ((opds[1].index < 0) && (opds[1].constant != null)) {
						int immVal = (Integer)opds[1].constant;
						if ((immVal >= -32768) && (immVal <= 32767))
							createIrDrASimm(ppcAddi, dReg, sReg1, immVal);
						else
							createIrDrArB(ppcAdd, dReg, sReg1, sReg2);
					} else {
						createIrDrArB(ppcAdd, dReg, sReg1, sReg2);
					}
					break;
				case tLong:
					createIrDrArB(ppcAddc, dReg, sReg1, sReg2);
					createIrDrArB(ppcAdde, instr.result.regLong, opds[0].regLong, opds[1].regLong);
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
				dReg = instr.result.reg;
				switch (instr.result.type) {
				case tByte: case tShort: case tInteger:
					if (opds[0].index < 0) {
						int immVal = (Integer)opds[0].constant;
						if ((immVal >= -32768) && (immVal <= 32767))
							createInstructionSDI(ppcAddi, sReg2, dReg, -immVal);
					} else if (opds[1].index < 0) {
						int immVal = (Integer)opds[1].constant;
						if ((immVal >= -32768) && (immVal <= 32767))
							createInstructionSDI(ppcAddi, sReg1, dReg, -immVal);
					} else {
						createInstructionSSD(ppcSubf, sReg2, sReg1, dReg);
					}
					break;
				case tLong:
					assert false : "not yet implemented";
					break;
				case tFloat:
					createInstructionSSD(ppcFsubs, sReg1, sReg2, dReg);
					break;
				case tDouble:
					createInstructionSSD(ppcFsub, sReg1, sReg2, dReg);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCmul:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = instr.result.reg;
				switch (instr.result.type) {
				case tByte: case tShort: case tInteger:
					if (opds[0].index < 0) {
						int immVal = (Integer)opds[0].constant;
						if ((immVal >= -32768) && (immVal <= 32767))
							createInstructionSDI(ppcMulli, sReg2, dReg, immVal);
					} else if (opds[1].index < 0) {
						int immVal = (Integer)opds[1].constant;
						if ((immVal >= -32768) && (immVal <= 32767))
							createInstructionSDI(ppcMulli, sReg1, dReg, immVal);
					} else {
						createInstructionSSD(ppcMullw, sReg1, sReg2, dReg);
					}
					break;
				case tLong:
//					assert false : "cg: type not implemented";
					break;
				case tFloat:
					createInstructionSSD(ppcFmuls, sReg1, sReg2, dReg);
					break;
				case tDouble:
					createInstructionSSD(ppcFmul, sReg1, sReg2, dReg);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCdiv:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = instr.result.reg;
				switch (instr.result.type) {
				case tByte: case tShort: case tInteger:
					if (opds[0].index < 0) {
						int immVal = (Integer)opds[0].constant;
						if ((immVal >= -32768) && (immVal <= 32767))
							createInstructionSDI(ppcAddi, sReg2, dReg, immVal);
					} else if (opds[1].index < 0) {
						int immVal = (Integer)opds[1].constant;
						if ((immVal >= -32768) && (immVal <= 32767))
							createInstructionSDI(ppcAddi, sReg1, dReg, immVal);
					} else {
						createInstructionSSD(ppcAdd, sReg1, sReg2, dReg);
					}
					assert false : "cg: type not implemented";
					break;
				case tLong:
					assert false : "cg: type not implemented";
					break;
				case tFloat:
					createInstructionSSD(ppcFdivs, sReg1, sReg2, dReg);
					break;
				case tDouble:
					createInstructionSSD(ppcFdiv, sReg1, sReg2, dReg);
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCrem:
				break;
			case sCand:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = instr.result.reg;
				if (instr.result.type == tInteger) {
					if ((opds[0].index < 0) && (opds[0].constant != null)) {
						int immVal = (Integer)opds[0].constant;
						if ((immVal >= -32768) && (immVal <= 32767))
							createIrArSuimm(ppcAndi, dReg, sReg2, immVal);
						else
							createIrArSrB(ppcAnd, dReg, sReg1, sReg2);
					} else if ((opds[1].index < 0) && (opds[1].constant != null)) {
						int immVal = (Integer)opds[1].constant;
						if ((immVal >= -32768) && (immVal <= 32767))
							createIrArSuimm(ppcAndi, dReg, sReg1, immVal);
						else
							createIrArSrB(ppcAnd, dReg, sReg1, sReg2);
					} else
						createIrArSrB(ppcAnd, dReg, sReg1, sReg2);
				} else if (instr.result.type == tLong) {
					if ((opds[0].index < 0) && (opds[0].constant != null)) {
						long immVal = (Long)opds[0].constant;
						if ((immVal >= -32768) && (immVal <= 32767))
							createIrArSuimm(ppcAndi, dReg, sReg2, (int)immVal);
						else {
							createIrArSrB(ppcAnd, instr.result.regLong, opds[0].regLong, opds[1].regLong);
							createIrArSrB(ppcAnd, dReg, sReg1, sReg2);
						}
					} else if ((opds[1].index < 0) && (opds[1].constant != null)) {
						long immVal = (Integer)opds[1].constant;
						if ((immVal >= -32768) && (immVal <= 32767))
							createIrArSuimm(ppcAndi, dReg, sReg1, (int)immVal);
						else {
							createIrArSrB(ppcAnd, instr.result.regLong, opds[0].regLong, opds[1].regLong);
							createIrArSrB(ppcAnd, dReg, sReg1, sReg2);
						}
					} else {
						createIrArSrB(ppcAnd, instr.result.regLong, opds[0].regLong, opds[1].regLong);
						createIrArSrB(ppcAnd, dReg, sReg1, sReg2);
					}
				} else
					assert false : "cg: wrong type";
			break;
			case sCor:
				break;
			case sCxor:
				break;
			case sCconvInt:	// int -> other type
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				dReg = instr.result.reg;
				switch (instr.result.type) {
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
					createIrArSSH(ppcSrawi, 0, sReg1, 31);
					createIrArSrB(ppcOr, instr.result.regLong, 0, 0);
					break;
				case tFloat:
					break;
				case tDouble:
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCconvLong:
				break;
			case sCconvFloat:
				break;
			case sCconvDouble:
				break;
			case sCcmpl:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = instr.result.reg;
				switch (instr.result.type) {
				case tByte:
					break;
				case tChar: 
					break;
				case tInteger: 
					break;
				case tLong:
					break;
				case tFloat:
					break;
				case tDouble:
					break;
				default:
					assert false : "cg: wrong type";
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
					createIrArSrB(ppcFmr, returnFPR, 0, opds[0].reg);
					break;
				default:
					assert false : "cg: return instruction not implemented";
				}
				if (node.next != null)	// last node needs no branch
					createIli(ppcB, 0, false);
				break;
			case sCcall:
				opds = instr.getOperands();
				if (((Call)instr).isStatic) {	// invokestatic
					loadConstantAndFixup(fixup, instr.result.regAux1);	// addr of method
					createIrSspr(ppcMtspr, LR, instr.result.regAux1);
				} else {	// invokevirtual and invokespecial and invokeinterface
					refReg = opds[0].reg;
					createItrap(ppcTwi, TOifequal, refReg, 0);
					createIrDrAd(ppcLwz, instr.result.regAux1, refReg, -4);
					createIrDrAd(ppcLwz, instr.result.regAux1, instr.result.regAux1, -methodOffset);
					createIrSspr(ppcMtspr, LR, instr.result.regAux1);
				}
				for (int k = 0; k < opds.length; k++) {
					sReg1 = opds[k].reg;
					switch (opds[k].type) {
					case tRef: case tBoolean: case tChar: case tByte:
					case tShort: case tInteger: case tAref: case tAboolean:
					case tAchar: case tAfloat: case tAdouble: case tAbyte: 
					case tAshort: case tAinteger: case tAlong:
						sReg1 = opds[k].reg;
						createIrArSrB(ppcOr, paramStartGPR + k, sReg1, sReg1);
						break;
					case tLong:
						sReg2 = opds[k].regLong;
						createIrArSrB(ppcOr, paramStartGPR + k, sReg1, sReg1);
						createIrArSrB(ppcOr, paramStartGPR + k + 1, sReg2, sReg2);
						break;
					case tFloat: case tDouble:
						createIrDrB(ppcFmr, paramStartFPR, sReg1);
						break;
					default:
						assert false : "cg: wrong type";
					}
				}
				createIBOBI(ppcBclr, BOalways, 0);
				switch (instr.result.type) {
				case tRef: case tBoolean: case tChar: case tByte:
				case tShort: case tInteger: case tAref: case tAboolean:
				case tAchar: case tAfloat: case tAdouble: case tAbyte: 
				case tAshort: case tAinteger: case tAlong:
					createIrArSrB(ppcOr, instr.result.reg, returnGPR1, returnGPR1);
					break;
				case tLong:
					createIrArSrB(ppcOr, instr.result.reg, returnGPR1, returnGPR1);
					createIrArSrB(ppcOr, instr.result.regLong, returnGPR1 + 1, returnGPR1 + 1);
					break;
				case tFloat: case tDouble:
					createIrDrB(ppcFmr, instr.result.reg, returnFPR);
					break;
				case tVoid:	// no return value
					break;
				default:
					assert false : "cg: wrong type";
				}
				break;
			case sCnew:
				opds = instr.getOperands();
				if (opds.length == 1) {
					switch (instr.result.type) {
					case tRef:	// bCnew
						loadConstantAndFixup(fixup, paramStartGPR);	// addr of new
						createIrSspr(ppcMtspr, LR, paramStartGPR);
						loadConstantAndFixup(fixup, paramStartGPR);	// ref
						createIBOBI(ppcBclr, BOalways, 0);
						createIrArSrB(ppcOr, instr.result.reg, returnGPR1, returnGPR1);
						break;
					case tAboolean: case tAchar: case tAfloat: case tAdouble:
					case tAbyte: case tAshort: case tAinteger: case tAlong:	// bCnewarray
						opds = instr.getOperands();
						createIrArSrB(ppcOr, paramStartGPR, opds[0].reg, opds[0].reg);	// nof elems
						createItrapSimm(ppcTwi, TOifless, paramStartGPR, 0);
						loadConstantAndFixup(fixup, paramStartGPR + 1);	// addr of newarray
						createIrSspr(ppcMtspr, LR, paramStartGPR + 1);
						createIrDrASimm(ppcAddi, paramStartGPR + 1, 0, instr.result.type - 10);	// type
						createIBOBI(ppcBclr, BOalways, 0);
						createIrArSrB(ppcOr, instr.result.reg, returnGPR1, returnGPR1);
						break;
					case tAref:	// bCanewarray
						opds = instr.getOperands();
						createIrArSrB(ppcOr, paramStartGPR, opds[0].reg, opds[0].reg);	// nof elems
						createItrapSimm(ppcTwi, TOifless, paramStartGPR, 0);
						loadConstantAndFixup(fixup, paramStartGPR + 1);	// addr of anewarray
						createIrSspr(ppcMtspr, LR, paramStartGPR + 1);
						loadConstantAndFixup(fixup, paramStartGPR + 1);	// ref
						createIBOBI(ppcBclr, BOalways, 0);
						createIrArSrB(ppcOr, instr.result.reg, returnGPR1, returnGPR1);
						break;
					default:
						System.out.println("cfg = " + ssa.cfg.method.name);
						System.out.println("instr = " + scMnemonics[instr.ssaOpcode]);
						System.out.println("type = " + instr.result.type);
						System.out.println("type = " + svNames[instr.result.type]);
						assert false : "cg: instruction not implemented";
					}
				} else { // bCmultianewarray:
//					assert false : "cg: instruction not implemented";
				}
				break;
			case sCloadFromArray:
				opds = instr.getOperands();
				refReg = opds[0].reg;
				indexReg = opds[1].reg;
				createItrap(ppcTwi, TOifequal, refReg, 0);
				createIrDrAd(ppcLha, instr.result.regAux1, refReg, arrayLenOffset);
				createItrap(ppcTw, TOifgeU, indexReg, instr.result.regAux1);
				switch (instr.result.type) {
				case tByte: case tBoolean:
					createIrDrASimm(ppcAddi, instr.result.regAux2, refReg, arrayFirstOffset);
					createIrDrArB(ppcLbzx, instr.result.reg, instr.result.regAux2, indexReg);
					createIrArS(ppcExtsb, instr.result.reg, instr.result.reg);
					break;
				case tShort: 
					createIrArSSHMBME(ppcRlwinm, instr.result.regAux1, indexReg, 1, 0, 30);
					createIrDrASimm(ppcAddi, instr.result.regAux2, refReg, arrayFirstOffset);
					createIrDrArB(ppcLhax, instr.result.reg, instr.result.regAux1, instr.result.regAux2);
					break;
				case tInteger: case tRef:
					createIrArSSHMBME(ppcRlwinm, instr.result.regAux1, indexReg, 2, 0, 29);
					createIrDrASimm(ppcAddi, instr.result.regAux2, refReg, arrayFirstOffset);
					createIrDrArB(ppcLwzx, instr.result.reg, instr.result.regAux1, instr.result.regAux2);
					break;
				case tLong: 
					createIrArSSHMBME(ppcRlwinm, instr.result.regAux1, indexReg, 3, 0, 28);
					createIrDrASimm(ppcAddi, instr.result.regAux2, refReg, arrayFirstOffset);
					createIrDrArB(ppcLwzux, instr.result.reg, instr.result.regAux1, instr.result.regAux2);
					createIrDrAd(ppcLwz, instr.result.regLong, instr.result.regAux1, 4);
					break;
				case tFloat:
					createIrArSSHMBME(ppcRlwinm, instr.result.regAux1, indexReg, 2, 0, 29);
					createIrDrASimm(ppcAddi, instr.result.regAux2, refReg, arrayFirstOffset);
					createIrDrArB(ppcLfsx, instr.result.reg, instr.result.regAux1, instr.result.regAux2);
					break;
				case tDouble: 
					createIrArSSHMBME(ppcRlwinm, instr.result.regAux1, indexReg, 3, 0, 28);
					createIrDrASimm(ppcAddi, instr.result.regAux2, refReg, arrayFirstOffset);
					createIrDrArB(ppcLfdx, instr.result.reg, instr.result.regAux1, instr.result.regAux2);
					break;
				case tChar: 
					assert false : "cg: type char not implemented";
					break;
				default:
					assert false : "cg: type not implemented";
				}
				break;
			case sCstoreToArray:
				opds = instr.getOperands();
				refReg = opds[0].reg;
				indexReg = opds[1].reg;
				valReg = opds[2].reg;
				createItrap(ppcTwi, TOifequal, refReg, 0);
				createIrDrAd(ppcLha, instr.result.regAux1, refReg, arrayLenOffset);
				createItrap(ppcTw, TOifgeU, indexReg, instr.result.regAux1);
				switch (opds[2].type) {
				case tByte: 
					createIrDrASimm(ppcAddi, instr.result.regAux2, refReg, arrayFirstOffset);
					createIrSrArB(ppcStbx, valReg, indexReg, instr.result.regAux2);
					break;
				case tShort: 
					createIrArSSHMBME(ppcRlwinm, instr.result.regAux1, indexReg, 1, 0, 30);
					createIrDrASimm(ppcAddi, instr.result.regAux2, refReg, arrayFirstOffset);
					createIrSrArB(ppcSthx, valReg, instr.result.regAux1, instr.result.regAux2);
					break;
				case tInteger: case tRef:
				case tAref: case tAboolean: case tAchar: case tAfloat: case tAdouble:
				case tAbyte: case tAshort: case tAinteger: case tAlong:
					createIrArSSHMBME(ppcRlwinm, instr.result.regAux1, indexReg, 2, 0, 29);
					createIrDrASimm(ppcAddi, instr.result.regAux2, refReg, arrayFirstOffset);
					createIrSrArB(ppcStwx, valReg, instr.result.regAux1, instr.result.regAux2);
					break;
				case tLong: 
					createIrArSSHMBME(ppcRlwinm, instr.result.regAux1, indexReg, 3, 0, 28);
					createIrDrASimm(ppcAddi, instr.result.regAux2, refReg, arrayFirstOffset);
					createIrSrArB(ppcStwux, valReg, instr.result.regAux1, instr.result.regAux2);
					createIrSrAd(ppcStw, opds[2].regLong, instr.result.regAux1, 4);
					break;
				case tFloat: 
					createIrArSSHMBME(ppcRlwinm, instr.result.regAux1, indexReg, 2, 0, 29);
					createIrDrASimm(ppcAddi, instr.result.regAux2, refReg, arrayFirstOffset);
					createIrSrArB(ppcStfsx, valReg, instr.result.regAux1, instr.result.regAux2);
					break;
				case tDouble: 
					createIrArSSHMBME(ppcRlwinm, instr.result.regAux1, indexReg, 3, 0, 28);
					createIrDrASimm(ppcAddi, instr.result.regAux2, refReg, arrayFirstOffset);
					createIrSrArB(ppcStfdx, valReg, instr.result.regAux1, instr.result.regAux2);
					break;
				case tChar: 
					assert false : "cg: type char not implemented";
				break;
				default:
					System.out.println("cfg = " + ssa.cfg.method.name);
					System.out.println("instr = " + scMnemonics[instr.ssaOpcode]);
					System.out.println("type = " + svNames[opds[2].type]);
					assert false : "cg: type not implemented";
				}
				break;
			case sCstoreToField:
				opds = instr.getOperands();
				int type;
				if (opds.length == 1) {	// putstatic
					sReg1 = opds[0].reg;
					sReg2 = opds[0].regLong;
					refReg = instr.result.regAux1;
					type = opds[0].type;
					offset = classVarOffset;
					loadConstantAndFixup(fixup, instr.result.regAux1);
				} else {	// putfield
					refReg = opds[0].reg;
					sReg1 = opds[1].reg;
					sReg2 = opds[1].regLong;
					type = opds[1].type;
					offset = objVarOffset;
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
					assert false : "cg: wrong type";
				}
				break;
			case sCalength:
				opds = instr.getOperands();
				refReg = opds[0].reg;
				createItrap(ppcTwi, TOifequal, refReg, 0);
				createInstructionSDI(ppcLha, refReg, instr.result.reg , arrayLenOffset);
				break;
			case sCloadLocal:
				break;
			case sCregMove:
				opds = instr.getOperands();
				createIrArSrB(ppcOr, instr.result.reg, opds[0].reg, opds[0].reg);
				break;
			case sCbranch:
				bci = ssa.cfg.code[node.lastBCA] & 0xff;
				switch (bci) {
				case bCgoto:
					createIli(ppcB, 0, false);
					break;
				case bCif_acmpeq:
					break;
				case bCif_acmpne:
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
					if (opds[0].index < 0) {
						if (opds[0].constant != null) {
							System.out.println("constant is not null");
							int immVal = (Integer)opds[0].constant;
							if ((immVal >= -32768) && (immVal <= 32767))
								createICRFrASimm(ppcCmpi, CRF0, sReg2, immVal);
							else
								createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);
						} else
								createICRFrArB(ppcCmp, CRF0, sReg2, sReg1);					
					} else if (opds[1].index < 0) {
						if (opds[1].constant != null) {
							int immVal = (Integer)opds[1].constant;
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
					createICRFrASimm(ppcCmpi, 0, sReg1, 0);
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
					assert false : "cg: branch not implemented";
					break;
				case bCifnull:
					assert false : "cg: branch not implemented";
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
						createICRFrASimm(ppcCmpi, 0, sReg1, low + k);
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
						createICRFrASimm(ppcCmpi, 0, sReg1, key);
						createIBOBIBD(ppcBc, BOtrue, (28-4*CRF0+EQ), 0);
					}
					createIli(ppcB, nofPairs, true);
					break;
				default:
					System.out.println(bci);
					assert false : "cg: no such branch instruction";
				}
				break;
			case sCloadFromField:
				opds = instr.getOperands();
				if (opds == null) {	// getstatic
					sReg1 = instr.result.regAux1;
					offset = classVarOffset;
					loadConstantAndFixup(fixup, sReg1);
				} else {	// getfield
					sReg1 = opds[0].reg;
					offset = objVarOffset;
					createItrap(ppcTwi, TOifequal, opds[0].reg, 0);
				}
				switch (instr.result.type) {
				case tBoolean: case tByte:
					createIrDrAd(ppcLbz, instr.result.reg, sReg1, offset);
					createIrArS(ppcExtsb, instr.result.reg, instr.result.reg);
					break;
				case tShort: 
					createIrDrAd(ppcLha, instr.result.reg, sReg1, offset);
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
					createIrDrAd(ppcLwzu, instr.result.reg, sReg1, offset);
					createIrDrAd(ppcLwz, instr.result.regLong, sReg1, 4);
					break;
				case tFloat: 
					createIrDrAd(ppcLfs, instr.result.reg, sReg1, offset);
					break;
				case tDouble: 
					createIrDrAd(ppcLfd, instr.result.reg, sReg1, offset);
					break;
				default:
					assert false : "cg: wrong type";
				}
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

	private void createIrDrArB(int opCode, int rD, int rA, int rB) {
		instructions[iCount] = opCode | (rD << 21) | (rA << 16) | (rB << 11);
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

	private void createIrDrASimm(int opCode, int rD, int rA, int simm) {
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

	private void createInstructionSSD(int opCode, int sReg1, int sReg2, int dReg) {
		if ((opCode == ppcOr) && (sReg1 == sReg2) && (sReg1 == dReg)) return; 	// lr x,x makes no sense
		instructions[iCount] = opCode | (sReg1 << 16) | (sReg2 << 11) | (dReg << 21);
		incInstructionNum();
	}

	private void createInstructionSDI(int opCode, int sReg, int dReg, int immVal) {
//		System.out.println(Integer.toHexString(opCode | (sReg << 16) | (dReg << 21) | (immVal & 0xffff)));
		instructions[iCount] =  opCode | (sReg << 16) | (dReg << 21) | (immVal & 0xffff);
//		System.out.println(InstructionDecoder.getMnemonic(instructions[iCount]));
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

	private void createIrDrB(int opCode, int rD, int rB) {
		instructions[iCount] = opCode | (rD << 21) | (rB << 11);
		incInstructionNum();
	}

	private void createIrSspr(int opCode, int spr, int rS) {
		instructions[iCount] = opCode | (spr << 11) | (rS << 21);
		incInstructionNum();
	}

	private void loadConstant(int val, int reg) {
		int low = val & 0xffff;
		int high = (val >> 16) & 0xffff;
		if ((low >> 15) == 0) {
			if (low != 0) createIrDrASimm(ppcAddi, reg, 0, low);
			if (high != 0) createIrDrASimm(ppcAddis, reg, reg, high);
			if ((low == 0) && (high == 0)) createIrDrASimm(ppcAddi, reg, 0, 0);
		} else {
			if (low != 0) createIrDrASimm(ppcAddi, reg, 0, low);
			if (((high + 1) & 0xffff) != 0) createIrDrASimm(ppcAddis, reg, reg, high + 1);
		}
	}
	
	private void loadConstantAndFixup(int val, int reg) {
		assert val >= 0 && val <= 32768 : "fixup is out of range";
		createIrDrASimm(ppcAddi, reg, 0, val);
		createIrDrASimm(ppcAddis, reg, reg, 0);
		fixup = iCount - 2;
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
			createIrArSrB(ppcOr, topGPR - 1, moveGPR[i], moveGPR[i]);
		for (int i = 0; i < nofMoveFPR; i++)
			createIrDrB(ppcFmr, topFPR - 1, moveFPR[i]);	// stimmt instruktion rDrB ?????
	}

	private void insertEpilog(int stackSize) {
		epilogStartInstr = iCount;
		if (nofNonVolFPR > 0) {
			for (int i = 0; i < nofNonVolFPR; i++)
				createIrDrAd(ppcLfd, topFPR-i, stackPtr, FPRoffset + i * 8);
		}
		if (nofNonVolGPR > 0)
			createIrDrAd(ppcLmw, nofGPR - nofNonVolGPR, stackPtr, GPRoffset);
		createIrDrAd(ppcLwz, 0, stackPtr, LRoffset);
		createIrSspr(ppcMtspr, LR, 0);
		createIrDrASimm(ppcAddi, stackPtr, stackPtr, stackSize);
		createIBOBI(ppcBclr, BOalways, 0);
	}

	public void print(){
		System.out.println("Method information for " + ssa.cfg.method.name);
		Method m = ssa.cfg.method;
		System.out.println("Method has " + m.maxLocals + " locals where " + m.nofParams + " are parameters");
		System.out.println("stackSize = " + stackSize);
		System.out.println("Method uses " + nofNonVolGPR + " GPR and " + nofNonVolFPR + " FPR for locals where " + nofParamGPR + " GPR and " + nofParamFPR + " FPR are for parameters");
		System.out.println();
		
		System.out.println("Code for Method:" + ssa.cfg.method.name);
		
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