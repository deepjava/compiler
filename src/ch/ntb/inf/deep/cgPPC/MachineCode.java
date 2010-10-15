package ch.ntb.inf.deep.cgPPC;

import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.cfg.*;
import ch.ntb.inf.deep.ssa.*;
import ch.ntb.inf.deep.ssa.instruction.*;
import static org.junit.Assert.*;
import ch.ntb.inf.deep.classItems.*;

public class MachineCode implements SSAInstructionOpcs, SSAInstructionMnemonics, SSAValueType, InstructionOpcs, Registers, ICjvmInstructionOpcs, IClassFileConsts {
	private static final int defaultNofInstr = 16;
	private static final int stackPtr = 1;	// register for stack pointer
	private static final int GPRoffset = 12;	
	private static final int FPRoffset = 32;	
	private static final int LRoffset = 0;	
	
	private static final int arrayLenOffset = 8;	
	private static final int arrayFirstOffset = 12;
	private static final int refAddr = 16;
	private static final int refCD = 20;
	private static final int methodOffset = 24;
	private static final int cppOffset = 24;
	private static final int classVarOffset = 4;
	private static final int varOffset = 8;
	
	/**
	 * reference to the SSA of a method
	 */
	SSA ssa;
	int epilogStartInstr;
	int stackSize;
	int nofParamGPR = 0, nofParamFPR = 0;
	
	/**
	 * contains machine instructions for the ssa of a method
	 */
	int[] instructions;
	int iCount;
	int fixup;

	public MachineCode(SSA ssa) {
		System.out.println("start generating code for " + ssa.cfg.method.name);
		this.ssa = ssa;
		instructions = new int[defaultNofInstr];
		stackSize = (32 + ssa.nofGPR * 4 + ssa.nofFPR * 8) / 16 * 16;
		boolean isStatic = (ssa.cfg.method.accAndPropFlags & (1 << apfStatic)) != 0;
		insertProlog(stackSize, isStatic);
		SSANode node = (SSANode)this.ssa.cfg.rootNode;
		while (node != null) {
			node.codeStartAddr = iCount;
			translateSSA(node);
			node.codeEndAddr = iCount-1;
			node = (SSANode) node.next;
		}
		node = (SSANode)this.ssa.cfg.rootNode;
		while (node != null) {	// resolve local branch targets
			if (node.nofInstr > 0) {
				if (node.instructions[node.nofInstr-1].ssaOpcode == sCbranch) {
					int code = this.instructions[node.codeEndAddr];
					System.out.println("target of branch instruction corrected: 0x" + Integer.toHexString(node.codeEndAddr*4));
					CFGNode[] successors = node.successors;
					switch (code & 0xfc000000) {
					case ppcB:			
//						System.out.println("abs branch found");
						int branchOffset = ((SSANode)successors[0]).codeStartAddr - node.codeEndAddr;
						this.instructions[node.codeEndAddr] |= (branchOffset << 2) & 0x3ffffff;
						break;
					case ppcBc:
//						System.out.println("cond branch found");
						branchOffset = ((SSANode)successors[1]).codeStartAddr - node.codeEndAddr;
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

	public void translateSSA (SSANode node) {
		SSAValue[] opds;
		int sReg1, sReg2, dReg, refReg, indexReg, valReg, bci;
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
						if (instr.result.index >= 0) { // local variable
							createIrDrASimm(ppcAddi, dReg, 0, immVal);
						} else { // constant used for ssa instruction further down
							SSAValue val = instr.result;
							SSAValue startVal = node.instructions[0].result;
							SSAInstruction instr1 = node.instructions[val.end - startVal.n];
							switch (instr1.ssaOpcode) {	// instruction, where the const is used
							case sCadd:
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
					loadConstant((int)(immValLong >> 32), instr.result.reg);
					loadConstant((int)immValLong, instr.result.regLong);
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
					} else if ((opds[1].index < 0) && (opds[1].constant != null)) {
						int immVal = (Integer)opds[1].constant;
						if ((immVal >= -32768) && (immVal <= 32767))
							createIrDrASimm(ppcAddi, dReg, sReg1, immVal);
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
			case sCconvInt:
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
					loadConstantAndFixup(fixup, paramStartGPR);	// addr of method
					createIrSspr(ppcMtspr, LR, paramStartGPR);
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
					createIrArSrB(ppcOr, returnGPR1, instr.result.reg, instr.result.reg);
					break;
				case tLong:
					createIrArSrB(ppcOr, returnGPR1, instr.result.reg, instr.result.reg);
					createIrArSrB(ppcOr, returnGPR1 + 1, instr.result.regLong, instr.result.regLong);
					break;
				case tFloat: case tDouble:
					createIrDrB(ppcFmr, returnFPR, instr.result.reg);
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
						loadConstantAndFixup(refCD, paramStartGPR + 1);	// ref
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
				if (opds.length == 1) {	// putstatic
					sReg1 = opds[0].reg;
					loadConstantAndFixup(fixup, instr.result.regAux1);
					switch (opds[0].type) {
					case tBoolean: case tByte: case tShort: 
					case tInteger: case tRef: case tAref: case tAboolean:
					case tAchar: case tAfloat: case tAdouble: case tAbyte: 
					case tAshort: case tAinteger: case tAlong:
						/*	unterscheiden zwischen Typen der Felder
						 * if (ref.dataItem.type == tBoolean or case tByte:
						createIrSrAd(ppcStb, sReg1, instr.result.regAux1, classVarOffset);
					else if == tShort: 
						createIrSrAd(ppcSth, sReg1, instr.result.regAux1, classVarOffset);
					else*/
						createIrSrAd(ppcStw, sReg1, instr.result.regAux1, classVarOffset);
						break;
					case tChar: 
						assert false : "cg: type not implemented";
					break;
					case tLong:
						createIrSrAd(ppcStwu, sReg1, instr.result.regAux1, classVarOffset);
						createIrSrAd(ppcStw, opds[0].regLong, instr.result.regAux1, 4);
						break;
					case tFloat: 
						createIrSrAd(ppcStfs, sReg1, instr.result.regAux1, classVarOffset);
						break;
					case tDouble: 
						createIrSrAd(ppcStfd, sReg1, instr.result.regAux1, classVarOffset);
						break;
					default:
						assert false : "cg: wrong type";
					}
				} else {	// putfield
					refReg = opds[0].reg;
					sReg1 = opds[1].reg;
					createItrap(ppcTwi, TOifequal, refReg, 0);
					switch (opds[1].type) {
					case tBoolean: case tByte: case tShort: 
					case tInteger: case tRef: case tAref: case tAboolean:
					case tAchar: case tAfloat: case tAdouble: case tAbyte: 
					case tAshort: case tAinteger: case tAlong:
						/*	unterscheiden zwischen Typen der Felder
						 * if (ref.dataItem.type == tBoolean or case tByte:
						createIrSrAd(ppcStb, sReg1, instr.result.regAux1, classVarOffset);
					else if == tShort: 
						createIrSrAd(ppcSth, sReg1, instr.result.regAux1, classVarOffset);
					else*/
						createIrSrAd(ppcStw, sReg1, refReg, varOffset);
						break;
					case tChar: 
						assert false : "cg: type not implemented";
					break;
					case tLong:
						createIrSrAd(ppcStwu, sReg1, refReg, varOffset);
						createIrSrAd(ppcStw, opds[1].regLong, refReg, 4);
						break;
					case tFloat: 
						createIrSrAd(ppcStfs, sReg1, refReg, varOffset);
						break;
					case tDouble: 
						createIrSrAd(ppcStfd, sReg1, refReg, varOffset);
						break;
					default:
						assert false : "cg: wrong type";
					}
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
					createInstructionB(ppcB, 0, false);
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
					assertEquals("cg: wrong type", opds[0].type, tInteger);
					assertEquals("cg: wrong type", opds[1].type, tInteger);
					if (opds[0].index < 0) {
						if (opds[0].constant != null) {
							System.out.println("constant is not null");
							int immVal = (Integer)opds[0].constant;
							if ((immVal >= -32768) && (immVal <= 32767))
								createInstructionCMPI(ppcCmpi, CRF0, sReg2, immVal);
							else
								createInstructionCMP(ppcCmp, CRF0, sReg2, sReg1);
						} else
								createInstructionCMP(ppcCmp, CRF0, sReg2, sReg1);					
					} else if (opds[1].index < 0) {
						if (opds[1].constant != null) {
							int immVal = (Integer)opds[1].constant;
							if ((immVal >= -32768) && (immVal <= 32767)) {
								inverted = true;
								createInstructionCMPI(ppcCmpi, CRF0, sReg1, immVal);
							} else
								createInstructionCMP(ppcCmp, CRF0, sReg2, sReg1);
						} else
							createInstructionCMP(ppcCmp, CRF0, sReg2, sReg1);					
					} else {
						createInstructionCMP(ppcCmp, CRF0, sReg2, sReg1);
					}
					if (!inverted) {
						if (bci == bCif_icmpeq) 
							createInstructionBC(ppcBc, BOtrue, (28-4*CRF0+EQ), 0);
						else if (bci == bCif_icmpne)
							createInstructionBC(ppcBc, BOfalse, (28-4*CRF0+EQ), 0);
						else if (bci == bCif_icmplt)
							createInstructionBC(ppcBc, BOtrue, (28-4*CRF0+LT), 0);
						else if (bci == bCif_icmpge)
							createInstructionBC(ppcBc, BOfalse, (28-4*CRF0+LT), 0);
						else if (bci == bCif_icmpgt)
							createInstructionBC(ppcBc, BOtrue, (28-4*CRF0+GT), 0);
						else if (bci == bCif_icmple)
							createInstructionBC(ppcBc, BOfalse, (28-4*CRF0+GT), 0);
					} else {
						if (bci == bCif_icmpeq) 
							createInstructionBC(ppcBc, BOtrue, (28-4*CRF0+EQ), 0);
						else if (bci == bCif_icmpne)
							createInstructionBC(ppcBc, BOfalse, (28-4*CRF0+EQ), 0);
						else if (bci == bCif_icmplt)
							createInstructionBC(ppcBc, BOtrue, (28-4*CRF0+GT), 0);
						else if (bci == bCif_icmpge)
							createInstructionBC(ppcBc, BOfalse, (28-4*CRF0+GT), 0);
						else if (bci == bCif_icmpgt)
							createInstructionBC(ppcBc, BOtrue, (28-4*CRF0+LT), 0);
						else if (bci == bCif_icmple)
							createInstructionBC(ppcBc, BOfalse, (28-4*CRF0+LT), 0);
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
					assertEquals("cg: wrong type", opds[0].type, tInteger);
					createInstructionCMPI(ppcCmpi, 0, sReg1, 0);
					if (bci == bCifeq) 
						createInstructionBC(ppcBc, BOtrue, (28-4*CRF0+EQ), 0);
					else if (bci == bCifne)
						createInstructionBC(ppcBc, BOfalse, (28-4*CRF0+EQ), 0);
					else if (bci == bCiflt)
						createInstructionBC(ppcBc, BOtrue, (28-4*CRF0+LT), 0);
					else if (bci == bCifge)
						createInstructionBC(ppcBc, BOfalse, (28-4*CRF0+LT), 0);
					else if (bci == bCifgt)
						createInstructionBC(ppcBc, BOtrue, (28-4*CRF0+GT), 0);
					else if (bci == bCifle)
						createInstructionBC(ppcBc, BOfalse, (28-4*CRF0+GT), 0);
					break;
				case bCifnonnull:
					assert false : "cg: branch not implemented";
					break;
				case bCifnull:
					assert false : "cg: branch not implemented";
					break;
				default:
					System.out.println(bci);
					assert false : "cg: no such branch instruction";
				}
				break;
			case sCloadFromField:
				loadConstantAndFixup(fixup, instr.result.regAux1);
				switch (instr.result.type) {
				case tBoolean: case tByte:
					createIrDrAd(ppcLbz, instr.result.reg, instr.result.regAux1, classVarOffset);
					createIrArS(ppcExtsb, instr.result.reg, instr.result.reg);
					break;
				case tShort: 
					createIrDrAd(ppcLha, instr.result.reg, instr.result.regAux1, classVarOffset);
					break;
				case tInteger: case tRef: case tAref: case tAboolean:
				case tAchar: case tAfloat: case tAdouble: case tAbyte: 
				case tAshort: case tAinteger: case tAlong:
					createIrDrAd(ppcLwz, instr.result.reg, instr.result.regAux1, classVarOffset);
					break;
				case tChar: 
					assert false : "cg: type not implemented";
					break;
				case tLong:
					createIrDrAd(ppcLwzu, instr.result.reg, instr.result.regAux1, classVarOffset);
					createIrDrAd(ppcLwz, instr.result.regLong, instr.result.regAux1, 4);
					break;
				case tFloat: 
					createIrDrAd(ppcLfs, instr.result.reg, instr.result.regAux1, classVarOffset);
					break;
				case tDouble: 
					createIrDrAd(ppcLfd, instr.result.reg, instr.result.regAux1, classVarOffset);
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

	private void createInstructionB(int opCode, int LI, boolean link) {
		instructions[iCount] = opCode | (LI << 2 | (link ? 1 : 0));
//		System.out.println(InstructionDecoder.getMnemonic(instructions[iCount]));
		incInstructionNum();
	}

	private void createIli(int opCode, int LI, boolean link) {
		instructions[iCount] = opCode | (LI << 2 | (link ? 1 : 0));
		incInstructionNum();
	}

	private void createInstructionBC(int opCode, int BO, int BI, int BD) {
		instructions[iCount] = opCode | (BO << 21) | (BI << 16) | (BD << 2);
//		System.out.println(InstructionDecoder.getMnemonic(instructions[iCount]));
		incInstructionNum();
	}

	private void createIBOBI(int opCode, int BO, int BI) {
		instructions[iCount] = opCode | (BO << 21) | (BI << 16);
		incInstructionNum();
	}

	private void createInstructionCMP(int opCode, int crfD, int sReg1, int sReg2) {
		instructions[iCount] = opCode | (crfD << 23) | (sReg1 << 16) | (sReg2 << 11);
//		System.out.println(InstructionDecoder.getMnemonic(instructions[iCount]));
		incInstructionNum();
	}

	private void createInstructionCMPI(int opCode, int crfD, int sReg, int SIMM) {
		instructions[iCount] = opCode | (crfD << 23) | (sReg << 16) | (SIMM & 0xffff);
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
		int high = (val >> 16) + (val >> 15) - ((val&0xffff0000) >> 15);
		int low = (val - (high >> 16)) & 0xffff;
		if (val < 0) low--;
		if (low != 0) {
			createIrDrASimm(ppcAddis, reg, 0, high);
			createIrDrASimm(ppcAddi, reg, reg, low);
		} else {
			createIrDrASimm(ppcAddis, reg, 0, high);
		}
	}
	
	private void loadConstantAndFixup(int val, int reg) {
		assert fixup >= 0 && fixup <= 32768 : "fixup is out of range";
		createIrDrASimm(ppcAddis, reg, 0, 0);
		createIrDrASimm(ppcAddi, reg, reg, fixup);
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

	private void insertProlog(int stackSize, boolean isStatic) {
		createIrSrASimm(ppcStwu, stackPtr, stackPtr, -stackSize);
		createIrSspr(ppcMfspr, LR, 0);
		createIrSrASimm(ppcStw, 0, stackPtr, stackSize - LRoffset);
		if (ssa.nofGPR > 0) {
			createIrSrAd(ppcStmw, nofGPR-ssa.nofGPR, stackPtr, stackSize - GPRoffset);
		}
		if (ssa.nofFPR > 0) {
			for (int i = 0; i < ssa.nofFPR; i++)
				createIrSrAd(ppcStfd, topFPR-i, stackPtr, stackSize - FPRoffset);
		}
		if (ssa.nofGPR > 0 || ssa.nofFPR > 0) {
			Method m = ssa.cfg.method;
			nofParamGPR = 0; nofParamFPR = 0;
			for (int i = m.maxStackSlots; i < m.maxStackSlots + m.maxLocals; i++) {
				if (ssa.isParam[i]) {
					switch (ssa.paramType[i]) {
					case tInteger:
					case tRef:
					case tBoolean:
						nofParamGPR++;
						System.out.println("param type is integer");
						if (RegAllocator.lastExitSet[i] != null)
							createIrArSrB(ppcOr, RegAllocator.lastExitSet[i].reg, nofParamGPR+paramStartGPR-1, nofParamGPR+paramStartGPR-1);
						break;
					case tLong:
						nofParamGPR += 2;
						i++;
						System.out.println("param type is long");
						break;
					case tFloat:
					case tDouble:
						nofParamFPR++;
						System.out.println("param type is float");
						break;
					default:
						System.out.println("type " + ssa.paramType[i]);
						assert false : "type not implemented";
					}
				}
			}
		}
	}

	private void insertEpilog(int stackSize) {
		epilogStartInstr = iCount;
		if (ssa.nofGPR > 0)
			createIrDrAd(ppcLmw, 32-ssa.nofGPR, stackPtr, stackSize - GPRoffset);
		createIrDrAd(ppcLwz, 0, stackPtr, stackSize - LRoffset);
		createIrSspr(ppcMtspr, LR, 0);
		createIrDrASimm(ppcAddi, stackPtr, stackPtr, stackSize);
		createIBOBI(ppcBclr, BOalways, 0);
	}

	public void print(){
		System.out.println("Method information for " + ssa.cfg.method.name);
		Method m = ssa.cfg.method;
		System.out.println("Method has " + m.maxLocals + " locals where " + m.nofParams + " are parameters");
		System.out.println("stackSize = " + stackSize);
		System.out.println("Method uses " + ssa.nofGPR + " GPR and " + ssa.nofFPR + " FPR for locals where " + nofParamGPR + " GPR and " + nofParamFPR + " FPR are for parameters");
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