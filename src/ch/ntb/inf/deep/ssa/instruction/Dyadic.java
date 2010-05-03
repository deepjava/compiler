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
	public String toString() {
		return result+" = "+ bcMnemonics[ssaOpcode] + "(" + operands[0] + ", " + operands[1]
				+ ")";
	}

}
