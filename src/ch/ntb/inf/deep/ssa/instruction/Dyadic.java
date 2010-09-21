package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;

public class Dyadic extends SSAInstruction {
	
	public Dyadic(int opCode, SSAValue operand1, SSAValue operand2){
		ssaOpcode = opCode;
		operands = new SSAValue[]{operand1,operand2};
	}

	public Dyadic(int opCode){
		ssaOpcode = opCode;
	}
	
	@Override
	public SSAValue[] getOperands() {
		return operands;
	}

	@Override
	public void setOperands(SSAValue[] operands) {
		if (operands.length == 2) {
			this.operands = operands;
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public void print(int level) {
		for (int i = 0; i < level*3; i++)System.out.print(" ");
		System.out.print(result.n + ": ");
		System.out.print("Dyadic["+ scMnemonics[ssaOpcode]+"] {"+ operands[0].n + ", " + operands[1].n + "}");
		System.out.print(",   end=" + result.end + ", index=" + result.index + ", reg=" + result.reg);
		System.out.println();

	}
	
}
