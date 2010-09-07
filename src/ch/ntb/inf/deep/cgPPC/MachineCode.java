package ch.ntb.inf.deep.cgPPC;
import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.cfg.JvmInstructionMnemonics;
import ch.ntb.inf.deep.ssa.*;
import ch.ntb.inf.deep.ssa.instruction.*;

public class MachineCode implements SSAInstructionOpcs, SSAValueType, InstructionOpcs, JvmInstructionMnemonics {
	private static final int defaultNofInstr = 16;
	private static final int stackPtr = 1;	// register for stack pointer
	
	/**
	 * reference to the SSA of a method
	 */
	SSA ssa;
	
	/**
	 * contains machine instructions for the ssa of a method
	 */
	int[] instructions;
	int iCount;

	public MachineCode(SSA ssa) {
		this.ssa = ssa;
		instructions = new int[defaultNofInstr];
		int stackSize = 64;
		createInstructionSDI(ppcStwu, stackPtr, stackPtr, -stackSize);
		SSANode node = (SSANode)this.ssa.cfg.rootNode;
		while (node != null) {
			node.codeStartAddr = iCount;
			translateSSA(node);
			node.codeEndAddr = iCount-1;
			node = (SSANode) node.next;
		}
		node = (SSANode)this.ssa.cfg.rootNode;
		while (node != null) {
			if ((node.nofInstr > 0) && (node.instructions[node.nofInstr-1].ssaOpcode == sCBranch)) {
				int code = this.instructions[node.codeEndAddr];
//				System.out.println("target of branch instruction corrected: 0x" + Integer.toHexString(node.codeEndAddr*4));
				switch (code & 0xfc000000) {
				case ppcB:
					SSANode[] successors = (SSANode[])(node).successors;
					int targAddr = successors[0].codeStartAddr;
					this.instructions[node.codeEndAddr] |= (targAddr << 2) & 0x3ffffff;
					break;
				case ppcBc:
					successors = (SSANode[])(node).successors;
					targAddr = successors[1].codeStartAddr;
					this.instructions[node.codeEndAddr] |= (targAddr << 2) & 0xffff;
					break;
				}
			}
			node = (SSANode) node.next;
		}
		createInstructionSDI(ppcAddi, stackPtr, stackPtr, stackSize);
	}

	public void translateSSA (SSANode node) {
		SSAValue[] opds;
		int sReg1, sReg2, dReg;
		for (int i = 0; i < node.nofInstr; i++) {
			SSAInstruction instr = node.instructions[i];
			System.out.println("ssa opcode =" + instr.scMnemonics[instr.ssaOpcode]);
			switch (instr.ssaOpcode) { 
			case sCloadConst:
				opds = instr.getOperands();
				dReg = instr.result.reg;
				switch (instr.result.type) {
				case tByte: case tShort: case tInteger:
					int immVal = (Integer)instr.result.constant;	
					if ((immVal >= -32768) && (immVal <= 32767)) {
						createInstructionSDI(ppcAddi, 0, dReg, immVal);
					} else {
						int volReg = RegAllocator.getVolatile();
						createInstructionSDI(ppcAddis, 0, volReg, immVal >> 16);
						createInstructionSDI(ppcAddi, volReg, dReg, immVal);
					}
					break;
				case tLong:
					// will use constant pool later
					long immValLong = (Long)instr.result.constant;					
					int volReg = RegAllocator.getVolatile();
					createInstructionSDI(ppcAddis, 0, volReg, (int)(immValLong >> 48));
					createInstructionSDI(ppcAddis, volReg, volReg, (int)(immValLong >> 32));
					createInstructionSDI(ppcAddis, volReg, volReg, (int)(immValLong >> 16));
					createInstructionSDI(ppcAddi, volReg, dReg, (int)immValLong);
					break;
				case tFloat:
					// will use constant pool later
					float immValFloat = (Float)instr.result.constant;		
					assert false : "not yet implemented";
					break;
				case tDouble:
					double immValDouble = (Double)instr.result.constant;					
					assert false : "not yet implemented";
					break;
				}
				break;
			case sCadd:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = instr.result.reg;
				switch (instr.result.type) {
				case tByte: case tShort: case tInteger:
					createInstructionSSD(ppcAdd, sReg1, sReg2, dReg);
					break;
				case tLong:
					assert false : "not yet implemented";
					break;
				case tFloat:
					createInstructionSSD(ppcFadds, sReg1, sReg2, dReg);
					break;
				case tDouble:
					createInstructionSSD(ppcFadd, sReg1, sReg2, dReg);
					break;
				}
				break;
			case sCsub:
				opds = instr.getOperands();
				sReg1 = opds[0].reg;
				sReg2 = opds[1].reg;
				dReg = instr.result.reg;
				switch (instr.result.type) {
				case tByte: case tShort: case tInteger:
					createInstructionSSD(ppcSubf, sReg1, sReg2, dReg);
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
				}
				break;
			case sCBranch:
				int bci = ssa.cfg.code[node.lastBCA] & 0xff;
				switch (bci) {
				case bCgoto:
					createInstructionB(ppcB, 0);
					break;
				case bCif_acmpeq:
					break;
				case bCif_acmpne:
					break;
				case bCif_icmpeq:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					sReg2 = opds[1].reg;
					createInstructionCMP(ppcCmp, 0, sReg1, sReg2);
					createInstructionBC(ppcBc, 0xc, 31, 0);
					break;
				case bCif_icmpne:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					sReg2 = opds[1].reg;
					createInstructionCMP(ppcCmp, 0, sReg1, sReg2);
					createInstructionBC(ppcBc, 0xc, 31, 0);
					break;
				case bCif_icmplt:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					sReg2 = opds[1].reg;
					createInstructionCMP(ppcCmp, 0, sReg1, sReg2);
					createInstructionBC(ppcBc, 0xc, 31, 0);
					break;
				case bCif_icmpge:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					sReg2 = opds[1].reg;
					createInstructionCMP(ppcCmp, 0, sReg1, sReg2);
					createInstructionBC(ppcBc, 0xc, 31, 0);
					break;
				case bCif_icmpgt:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					sReg2 = opds[1].reg;
					createInstructionCMP(ppcCmp, 0, sReg1, sReg2);
					createInstructionBC(ppcBc, 0xc, 31, 0);
					break;
				case bCif_icmple:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					sReg2 = opds[1].reg;
					createInstructionCMP(ppcCmp, 0, sReg1, sReg2);
					createInstructionBC(ppcBc, 0xc, 31, 0);
					break;
				case bCifeq:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					createInstructionCMPI(ppcCmpi, 0, sReg1, 0);
					createInstructionBC(ppcBc, 0xc, 31, 0);
					break;
				case bCifne:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					createInstructionCMPI(ppcCmpi, 0, sReg1, 0);
					createInstructionBC(ppcBc, 0xc, 31, 0);
					break;
				case bCiflt:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					createInstructionCMPI(ppcCmpi, 0, sReg1, 0);
					createInstructionBC(ppcBc, 0xc, 31, 0);
					break;
				case bCifge:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					createInstructionCMPI(ppcCmpi, 0, sReg1, 0);
					createInstructionBC(ppcBc, 0xc, 31, 0);
					break;
				case bCifgt:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					createInstructionCMPI(ppcCmpi, 0, sReg1, 0);
					createInstructionBC(ppcBc, 0xc, 31, 0);
					break;
				case bCifle:
					opds = instr.getOperands();
					sReg1 = opds[0].reg;
					createInstructionCMPI(ppcCmpi, 0, sReg1, 0);
					createInstructionBC(ppcBc, 0xc, 31, 0);
					break;
				case bCifnonnull:
					break;
				case bCifnull:
					break;
				default:
					System.out.println(bci);
					assert false : "no such branch instruction";
				}
/*			default:
				assert false : "no code generated for this ssa function";*/
			}
		}
	}

	private void createInstructionSSD(int opCode, int sReg1, int sReg2, int dReg) {
		instructions[iCount] = opCode | (sReg1 << 16) | (sReg2 << 11) | (dReg << 21);
		incInstructionNum();
	}

	private void createInstructionSDI(int opCode, int sReg, int dReg, int immVal) {
//		System.out.println(Integer.toHexString(opCode | (sReg << 16) | (dReg << 21) | (immVal & 0xffff)));
		instructions[iCount] =  opCode | (sReg << 16) | (dReg << 21) | (immVal & 0xffff);
		incInstructionNum();
	}

	private void createInstructionB(int opCode, int LI) {
		instructions[iCount] = opCode | (LI << 2);
		incInstructionNum();
	}

	private void createInstructionBC(int opCode, int BO, int BI, int BD) {
		instructions[iCount] = opCode | (BO << 21) | (BI << 16) | (BD << 2);
		incInstructionNum();
	}

	private void createInstructionCMP(int opCode, int crfD, int sReg1, int sReg2) {
		instructions[iCount] = opCode | (crfD << 23) | (sReg1 << 16) | (sReg2 << 11);
		incInstructionNum();
	}

	private void createInstructionCMPI(int opCode, int crfD, int sReg, int SIMM) {
		instructions[iCount] = opCode | (crfD << 23) | (sReg << 16) | (SIMM & 0xffff);
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

	public void print(){
		System.out.println("Code for Method:" + ssa.cfg.method.name);
		
		for (int i = 0; i < iCount; i++){
		//	System.out.println(instructions[i]);
			System.out.print("\t[0x");
			System.out.print(Integer.toHexString(i*4));
			System.out.println("] " + InstructionDecoder.getMnemonic(instructions[i]));
		}
	}

}