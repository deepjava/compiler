package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;

public class PhiFunction extends SSAInstruction {
	private int nofOperands;
	

	public PhiFunction(int opCode, int nofOperands) {
		this.nofOperands = nofOperands;
		operands = new SSAValue[nofOperands];
	}

	@Override
	public SSAValue[] getOperands() {	
		return operands;
	}

	@Override
	public void setOperands(SSAValue[] operands) {
		this.operands = operands;
		nofOperands = operands.length;

	}
	public void addOperand(SSAValue operand, int pos) {
		int len = operands.length;
		if (len == nofOperands) {
			SSAValue[] newArray = new SSAValue[2 * len];
			for (int i = 0; i < len; i++) {
				newArray[i] = operands[i];
			}
			operands = newArray;
		}
		operands[nofOperands] = operand;
		nofOperands++;
	}
	@Override
	public String toString(){
		return "PhiFunction";
	}
}
