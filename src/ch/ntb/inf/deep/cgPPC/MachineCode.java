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
	private static final int LRoffset = 0;	
	
	private static final int arrayLenOffset = 8;	
	private static final int arrayFirstOffset = 12;
	private static final int stringAddr = 16;
	
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

	public MachineCode(SSA ssa) {
		this.ssa = ssa;
		instructions = new int[defaultNofInstr];
		int nofLocals = ssa.cfg.method.maxLocals;
		stackSize = (32 + nofLocals) / 16 * 16;
		boolean isStatic = (ssa.cfg.method.accAndPropFlags & (1 << apfStatic)) != 0;
		insertProlog(stackSize, nofLocals, isStatic);
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
						System.out.println("abs branch found");
						int targAddr = ((SSANode)successors[0]).codeStartAddr;
						this.instructions[node.codeEndAddr] |= (targAddr << 2) & 0x3ffffff;
						break;
					case ppcBc:
						System.out.println("cond branch found");
						targAddr = ((SSANode)successors[1]).codeStartAddr;
						this.instructions[node.codeEndAddr] |= (targAddr << 2) & 0xffff;
						break;
					}
				} else if (node.instructions[node.nofInstr-1].ssaOpcode == sCreturn) {
					if (node.next != null) {
						this.instructions[node.codeEndAddr] |= (iCount << 2) & 0x3ffffff;
						System.out.println("return branch found, epilog start instr = " + iCount);
					}
				}
			}
			node = (SSANode) node.next;
		}
		insertEpilog(stackSize);
	}

	public void translateSSA (SSANode node) {
		SSAValue[] opds;
		int sReg1, sReg2, dReg, refReg, reg1, reg2, indexReg, valReg, prevVolRegsGPR;
		for (int i = 0; i < node.nofInstr; i++) {
			SSAInstruction instr = node.instructions[i];
//			System.out.println("ssa opcode =" + instr.scMnemonics[instr.ssaOpcode]);
			switch (instr.ssaOpcode) { 
			case sCloadConst:
				int high, low, addr;
				opds = instr.getOperands();
				dReg = instr.result.reg;
				switch (instr.result.type) {
				case tByte: case tShort: case tInteger:
					int immVal = (Integer)instr.result.constant;
					if ((immVal < -32768) || (immVal > 32767)) {
						high = (immVal >> 16) + (immVal >> 15) - ((immVal&0xffff0000) >> 15);
						low = (immVal - (high >> 16)) & 0xffff;
						if (immVal < 0) low--;
//						System.out.println("new instr, low = " + low);
						if (low != 0) {
							int volReg = RegAllocator.getVolatileGPR();
							createInstructionSDI(ppcAddis, 0, volReg, high);
							createInstructionSDI(ppcAddi, volReg, dReg, low);
						} else {
//						System.out.println("low = 0");
							createInstructionSDI(ppcAddis, 0, dReg, high);
						}
					} else {
						if (instr.result.index >= 0) { // local variable
							createInstructionSDI(ppcAddi, 0, dReg, immVal);
						} else { // constant used for ssa instruction
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
//								System.out.println("ssa opcode of immediate instr =" + instr1.scMnemonics[instr1.ssaOpcode]);
								createInstructionSDI(ppcAddi, 0, dReg, immVal);
							}
						}
					}
					break;
				case tLong:
					// will use constant pool later
					long immValLong = (Long)instr.result.constant;					
					int volReg = RegAllocator.getVolatileGPR();
					createInstructionSDI(ppcAddis, 0, volReg, (int)(immValLong >> 48));
					createInstructionSDI(ppcAddis, volReg, volReg, (int)(immValLong >> 32));
					createInstructionSDI(ppcAddis, volReg, volReg, (int)(immValLong >> 16));
					createInstructionSDI(ppcAddi, volReg, dReg, (int)immValLong);
					assert false : "not yet implemented";
					break;
				case tFloat:
					// will use constant pool later
					float immValFloat = (Float)instr.result.constant;		
					assert false : "not yet implemented";
					break;
				case tDouble:
					double immValDouble = (Double)instr.result.constant;					
//					assert false : "not yet implemented";
					break;
				case tRef:
					prevVolRegsGPR = RegAllocator.volRegsGPR;
					RegAllocator.volRegsGPR = instr.result.volRegs;
					reg1 = RegAllocator.reserveVolatileGPR();
					
					System.out.println("cfg = " + ssa.cfg.method.name);
					System.out.println("instr = " + scMnemonics[instr.ssaOpcode]);
					System.out.println("type = " + svNames[instr.result.type]);
					System.out.println("vol reg = " + reg1);
					System.out.println("result reg = " + instr.result.reg);
					System.out.println("volRegs = " + Integer.toHexString(RegAllocator.volRegsGPR));
					addr = stringAddr;
					high = (addr >> 16) + (addr >> 15) - ((addr&0xffff0000) >> 15);
					low = (addr - (high >> 16)) & 0xffff;
					createIrDrAImm(ppcAddis, reg1, 0, high);
					createIrDrAImm(ppcAddi, reg1, reg1, low);
					RegAllocator.volRegsGPR = prevVolRegsGPR;
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
					break;
				case tLong:
					createInstructionSSD(ppcAddc, sReg1, sReg2, dReg);
					createInstructionSSD(ppcAdde, opds[0].regAux2, opds[1].regAux2, instr.result.regAux2);
					break;
				case tFloat:
					createInstructionSSD(ppcFadds, sReg1, sReg2, dReg);
					break;
				case tDouble:
					createInstructionSSD(ppcFadd, sReg1, sReg2, dReg);
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
					assert false : "cg: type not implemented";
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
			case sCbranch:
				int bci = ssa.cfg.code[node.lastBCA] & 0xff;
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
			case sCreturn:
				opds = instr.getOperands();
				bci = ssa.cfg.code[node.lastBCA] & 0xff;
				switch (bci) {
				case bCreturn:
					break;
				case bCireturn:
				case bCareturn:
					createInstructionSSD(ppcOr, returnGPR1, opds[0].reg, opds[0].reg);
					break;
				case bClreturn:
					createInstructionSSD(ppcOr, returnGPR1, opds[0].reg, opds[0].reg);
					createInstructionSSD(ppcOr, returnGPR2, opds[0].regAux2, opds[0].regAux2);
					break;
				case bCfreturn:
				case bCdreturn:
					createInstructionSSD(ppcFmr, returnFPR, 0, opds[0].reg);
					break;
				default:
					assert false : "cg: return instruction not implemented";
				}
				if (node.next != null)	// last node needs no branch
					createInstructionB(ppcB, 0, false);
				break;
			case sCcall:
				opds = instr.getOperands();
				for (int k = 0; k < opds.length; k++) {
//					assert opds[k].type == tInteger : "type not implemented";
//					assert opds[k].type == tObject : "type not implemented : " + opds[k].type;
					int reg = opds[k].reg;
					createInstructionSSD(ppcOr, 2 + k, reg, reg);
				}
				createInstructionSPR(ppcMtspr, LR, 0);
				createInstructionBCLR(ppcBclr, BOalways, 0);
				if (instr.result.type != tVoid)
					createInstructionSSD(ppcOr, instr.result.reg, 2, 2);
				break;
			case sCnew:
				opds = instr.getOperands();
				if (opds.length == 1) {
					switch (instr.result.type) {
					case tRef:	// bCnew
//						assert false : "cg: instruction not implemented";
						break;
					case tAboolean: case tAchar: case tAfloat: case tAdouble:
					case tAbyte: case tAshort: case tAinteger: case tAlong:	// bCnewarray
						opds = instr.getOperands();
						createIrArSrB(ppcOr, 2, opds[0].reg, opds[0].reg);
						createItrapImm(ppcTwi, TOifless, 2, 0);
						createIrDrAImm(ppcAddi, 3, 0, instr.result.type - 10);
						createInstructionB(ppcBl, 0, true);
						createIrArSrB(ppcOr, instr.result.reg, 2, 2);
						break;
					case tAref:	// bCanewarray
						opds = instr.getOperands();
						createIrArSrB(ppcOr, 2, opds[0].reg, opds[0].reg);
						createItrapImm(ppcTwi, TOifless, 2, 0);
						createIrDrAImm(ppcAddi, 3, 0, 20);
						createInstructionB(ppcBl, 0, true);
						createIrArSrB(ppcOr, instr.result.reg, 2, 2);
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
				prevVolRegsGPR = RegAllocator.volRegsGPR;
				RegAllocator.volRegsGPR = instr.result.volRegs;
				reg1 = RegAllocator.reserveVolatileGPR();
				createIrDrAImm(ppcLha, reg1, refReg, arrayLenOffset);
				createItrap(ppcTw, TOifgeU, indexReg, reg1);
				reg2 = RegAllocator.reserveVolatileGPR();
				switch (instr.result.type) {
				case tByte: 
					createIrDrAImm(ppcAddi, reg2, refReg, arrayFirstOffset);
					createIrDrArB(ppcLbzx, instr.result.reg, reg2, indexReg);
					createIrArS(ppcExtsb, instr.result.reg, instr.result.reg);
					break;
				case tShort: 
					createIrArRotateMask(ppcRlwinm, reg1, indexReg, 1, 0, 30);
					createIrDrAImm(ppcAddi, reg2, refReg, arrayFirstOffset);
					createIrDrArB(ppcLhax, instr.result.reg, reg1, reg2);
					break;
				case tInteger: 
				case tRef:
					createIrArRotateMask(ppcRlwinm, reg1, indexReg, 2, 0, 29);
					createIrDrAImm(ppcAddi, reg2, refReg, arrayFirstOffset);
					createIrDrArB(ppcLwzx, instr.result.reg, reg1, reg2);
					break;
				case tLong: 
					createIrArRotateMask(ppcRlwinm, reg1, indexReg, 3, 0, 28);
					createIrDrAImm(ppcAddi, reg2, refReg, arrayFirstOffset);
					createIrDrArB(ppcLwzux, instr.result.reg, reg1, reg2);
					createIrDrAImm(ppcLwz, instr.result.regAux2, reg1, 4);
					break;
				case tFloat:
					createIrArRotateMask(ppcRlwinm, reg1, indexReg, 2, 0, 29);
					createIrDrAImm(ppcAddi, reg2, refReg, arrayFirstOffset);
					createIrDrArB(ppcLfsx, instr.result.reg, reg1, reg2);
					break;
				case tDouble: 
					createIrArRotateMask(ppcRlwinm, reg1, indexReg, 3, 0, 28);
					createIrDrAImm(ppcAddi, reg2, refReg, arrayFirstOffset);
					createIrDrArB(ppcLfdx, instr.result.reg, reg1, reg2);
					break;
				case tChar: 
					assert false : "cg: type char not implemented";
					break;
				default:
					assert false : "cg: type not implemented";
				}
				RegAllocator.volRegsGPR = prevVolRegsGPR;
				break;
			case sCstoreToArray:
				opds = instr.getOperands();
				refReg = opds[0].reg;
				indexReg = opds[1].reg;
				valReg = opds[2].reg;
				createItrap(ppcTwi, TOifequal, refReg, 0);
				prevVolRegsGPR = RegAllocator.volRegsGPR;
//				System.out.println("prevVolRegsGPR = " + Integer.toHexString(prevVolRegsGPR));
//				System.out.println("instr.result.volRegs = " + Integer.toHexString(instr.result.volRegs));
				RegAllocator.volRegsGPR = instr.result.volRegs;
				reg1 = RegAllocator.reserveVolatileGPR();
//				System.out.println("reg1 = " + reg1);
				createIrDrAImm(ppcLha, reg1, refReg, arrayLenOffset);
				createItrap(ppcTw, TOifgeU, indexReg, reg1);
				reg2 = RegAllocator.reserveVolatileGPR();
				switch (opds[2].type) {
				case tByte: 
					createIrDrAImm(ppcAddi, reg2, refReg, arrayFirstOffset);
					createIrSrArB(ppcStbx, valReg, indexReg, reg2);
					break;
				case tShort: 
					createIrArRotateMask(ppcRlwinm, reg1, indexReg, 1, 0, 30);
					createIrDrAImm(ppcAddi, reg2, refReg, arrayFirstOffset);
					createIrSrArB(ppcSthx, valReg, reg1, reg2);
					break;
				case tInteger: case tRef:
				case tAref: case tAboolean: case tAchar: case tAfloat: case tAdouble:
				case tAbyte: case tAshort: case tAinteger: case tAlong:
					createIrArRotateMask(ppcRlwinm, reg1, indexReg, 2, 0, 29);
					createIrDrAImm(ppcAddi, reg2, refReg, arrayFirstOffset);
					createIrSrArB(ppcStwx, valReg, reg1, reg2);
					break;
				case tLong: 
					createIrArRotateMask(ppcRlwinm, reg1, indexReg, 3, 0, 28);
					createIrDrAImm(ppcAddi, reg2, refReg, arrayFirstOffset);
					createIrSrArB(ppcStwux, valReg, reg1, reg2);
					createIrSrAImm(ppcStw, opds[2].regAux2, reg1, 4);
					break;
				case tFloat: 
					createIrArRotateMask(ppcRlwinm, reg1, indexReg, 2, 0, 29);
					createIrDrAImm(ppcAddi, reg2, refReg, arrayFirstOffset);
					createIrSrArB(ppcStfsx, valReg, reg1, reg2);
					break;
				case tDouble: 
					createIrArRotateMask(ppcRlwinm, reg1, indexReg, 3, 0, 28);
					createIrDrAImm(ppcAddi, reg2, refReg, arrayFirstOffset);
					createIrSrArB(ppcStfdx, valReg, reg1, reg2);
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
				RegAllocator.volRegsGPR = prevVolRegsGPR;
				break;
			case sCalength:
				opds = instr.getOperands();
				refReg = opds[0].reg;
				createItrap(ppcTwi, TOifequal, refReg, 0);
				createInstructionSDI(ppcLha, refReg, instr.result.reg , arrayLenOffset);
				break;
			case sCloadLocal:
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

	private void createIrDrAImm(int opCode, int rD, int rA, int imm) {
		instructions[iCount] = opCode | (rD << 21) | (rA << 16) | (imm  & 0xffff);
		incInstructionNum();
	}

	private void createIrSrAImm(int opCode, int rS, int rA, int imm) {
		instructions[iCount] = opCode | (rS << 21) | (rA << 16) | (imm  & 0xffff);
		incInstructionNum();
	}

	private void createIrArRotateMask(int opCode, int rA, int rS, int SH, int MB, int ME) {
		instructions[iCount] = opCode | (rS << 21) | (rA << 16) | (SH << 11) | (MB << 6) | (ME << 1);
		incInstructionNum();
	}

	private void createItrap(int opCode, int TO, int rA, int rB) {
		instructions[iCount] = opCode | (TO << 21) | (rA << 16) | (rB << 11);
		incInstructionNum();
	}

	private void createItrapImm(int opCode, int TO, int rA, int imm) {
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

	private void createInstructionBC(int opCode, int BO, int BI, int BD) {
		instructions[iCount] = opCode | (BO << 21) | (BI << 16) | (BD << 2);
//		System.out.println(InstructionDecoder.getMnemonic(instructions[iCount]));
		incInstructionNum();
	}

	private void createInstructionBCLR(int opCode, int BO, int BI) {
		instructions[iCount] = opCode | (BO << 21) | (BI << 16);
//		System.out.println(InstructionDecoder.getMnemonic(instructions[iCount]));
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

	private void createInstructionRotateI(int opCode, int sReg, int dReg, int shift, int start, int stop) {
		instructions[iCount] = opCode | (dReg << 21) | (sReg << 16) | (shift << 11) | (start << 6) | (stop << 1);
//		System.out.println(InstructionDecoder.getMnemonic(instructions[iCount]));
//		System.out.println(Integer.toHexString((instructions[iCount])));
//		System.out.println(Integer.toHexString(InstructionDecoder.getCode("rlwinm  r3, r29, 2, 0, 29")));
		incInstructionNum();
	}

	private void createInstructionSPR(int opCode, int spr, int reg) {
		instructions[iCount] = opCode | (spr << 11) | (reg << 21);
//		System.out.println(InstructionDecoder.getMnemonic(instructions[iCount]));
		incInstructionNum();
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

	private void insertProlog(int stackSize, int nofLocals, boolean isStatic) {
		Method m = ssa.cfg.method;
		nofParamGPR = 0; nofParamFPR = 0;
		for (int i = m.maxStackSlots; i < m.maxStackSlots + m.maxLocals; i++) {
//			System.out.print(ssa.isParam[i] + ", ");
			if (ssa.isParam[i]) {
				switch (ssa.paramType[i]) {
				case tInteger:
				case tRef:
				case tBoolean:
					nofParamGPR++;
					System.out.println("type is integer");
					break;
				case tLong:
					nofParamGPR += 2;
					i++;
					System.out.println("type is long");
					break;
				case tFloat:
				case tDouble:
					nofParamFPR++;
					System.out.println("type is float");
					break;
				default:
					System.out.println("type " + ssa.paramType[i]);
					assert false : "type not implemented";
				}
			}
		}
		createInstructionSDI(ppcStwu, stackPtr, stackPtr, -stackSize);
		createInstructionSPR(ppcMfspr, LR, 0);
		createInstructionSDI(ppcStw, stackPtr, 0, stackSize - LRoffset);
		if (ssa.nofGPR > 0) {
			createInstructionSDI(ppcStmw, stackPtr, 32-ssa.nofGPR, stackSize - GPRoffset);
//		int count = ssa.cfg.method.nofParams + (isStatic? 0 : 1);
			for (int i = 0; i < nofParamGPR; i++) {
				createInstructionSSD(ppcOr, 31 - i, 2 + i, 2 + i);
			}
		}
	}

	private void insertEpilog(int stackSize) {
		epilogStartInstr = iCount;
		if (ssa.nofGPR > 0)
			createInstructionSDI(ppcLmw, stackPtr, 32-ssa.nofGPR, stackSize - GPRoffset);
		createInstructionSDI(ppcLwz, stackPtr, 0, stackSize - LRoffset);
		createInstructionSPR(ppcMtspr, LR, 0);
		createInstructionSDI(ppcAddi, stackPtr, stackPtr, stackSize);
		createInstructionBCLR(ppcBclr, BOalways, 0);
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
			System.out.println();
			}
	}

}