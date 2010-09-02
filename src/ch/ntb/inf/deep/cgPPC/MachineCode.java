package ch.ntb.inf.deep.cgPPC;
import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.ssa.*;
import ch.ntb.inf.deep.ssa.instruction.SSAInstruction;

public class MachineCode implements SSAInstructionOpcs, SSAValueType, InstructionOpcs {
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
		traverse();
		createInstructionSDI(ppcAddi, stackPtr, stackPtr, stackSize);
	}

	
	public void traverse () {
		SSANode node = (SSANode)this.ssa.cfg.rootNode;
		SSAValue[] opds;
		int sReg1, sReg2, dReg;
		for (int i = 0; i < node.nofInstr; i++) {
			SSAInstruction instr = node.instructions[i];
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
				
			}
		}
	}

	public void createInstructionSSD(int opCode, int sReg1, int sReg2, int dReg) {
		instructions[iCount] = opCode | (sReg1 << 16) | (sReg2 << 11) | (dReg << 21);
		incInstructionNum();
	}

	public void createInstructionSDI(int opCode, int sReg, int dReg, int immVal) {
//		System.out.println(Integer.toHexString(opCode | (sReg << 16) | (dReg << 21) | (immVal & 0xffff)));
		instructions[iCount] =  opCode | (sReg << 16) | (dReg << 21) | (immVal & 0xffff);
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

	public void print(int level, int SSANr){
		int count = 0;
		
		for (int i = 0; i < level; i++)System.out.print("\t");
		System.out.println("Code "+ SSANr +":");
		
		for (int i = 0; i < iCount; i++){
		//	System.out.println(instructions[i]);
			System.out.println(InstructionDecoder.getMnemonic(instructions[i]));
		}
	}

}