package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;

public class StoreToArray extends SSAInstruction {

	public StoreToArray(int opCode, SSAValue arrayref, SSAValue index, SSAValue value){
		bytecodeIndex = opCode;
		operands = new SSAValue[]{arrayref, index, value};
	}

	public StoreToArray(int opCode){
		bytecodeIndex = opCode;
	}
	
	@Override
	public SSAValue[] getOperands() {
		return operands;
	}

	@Override
	public void setOperands(SSAValue[] operands) {
		if (operands.length == 3) {
			this.operands = operands;
		} else {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public String toString() {
		return result+" = "+ bcMnemonics[bytecodeIndex] + "(" + operands[0] + ", " + operands[1]+ ", " + operands[2]
				+ ")";
	}

}