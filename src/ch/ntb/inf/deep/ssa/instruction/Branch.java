package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;

public class Branch extends SSAInstruction {
	
	public Branch(int opCode, SSAValue operand1, SSAValue operand2){
		ssaOpcode = opCode;
		operands = new SSAValue[]{operand1,operand2};
	}

	public Branch(int opCode, SSAValue operand1){
		ssaOpcode = opCode;
		operands = new SSAValue[]{operand1};
	}

	public Branch(int opCode){
		ssaOpcode = opCode;
	}

	@Override
	public SSAValue[] getOperands() {
		return operands;
	}

	@Override
	public void setOperands(SSAValue[] operands) {
		if (operands.length > 0) {
			this.operands = operands;
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public void print(int level) {
		for (int i = 0; i < level*3; i++)System.out.print(" ");
		System.out.print(result.n + ": ");
		if (operands == null)
			System.out.print("Branch["+ scMnemonics[ssaOpcode]+"] ");
		else {
			if (operands.length == 2)
				System.out.print("Branch["+ scMnemonics[ssaOpcode]+"] ( "+ operands[0].n + ", " + operands[1].n + " )");
			else
				System.out.print("Branch["+ scMnemonics[ssaOpcode]+"] ( "+ operands[0].n + " )");
		}
		System.out.println();

	}
	
}
