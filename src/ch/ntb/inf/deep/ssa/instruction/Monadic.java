package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;

public class Monadic extends SSAInstruction {

	public Monadic(int opCode,SSAValue operand){
		bytecodeIndex = opCode;
		operands = new SSAValue[]{operand};
	}
	
	public Monadic(int opCode){
		bytecodeIndex = opCode;
	}
	
	@Override
	public SSAValue[] getOperands() {
		return operands;
	}

	@Override
	public void setOperands(SSAValue[] operands) {
		if(operands.length == 1){
			this.operands = operands;
		}
		else{
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public String toString(){
		return result+" = "+ bcMnemonics[bytecodeIndex]+"("+operands[0]+")";
		}
	

}
