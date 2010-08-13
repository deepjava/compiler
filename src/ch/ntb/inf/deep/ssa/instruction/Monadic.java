package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;

public class Monadic extends SSAInstruction {

	public Monadic(int opCode,SSAValue operand){
		ssaOpcode = opCode;
		operands = new SSAValue[]{operand};
	}
	
	public Monadic(int opCode){
		ssaOpcode = opCode;
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
	public void print(int level) {
		for (int i = 0; i < level*3; i++)System.out.print(" ");
		System.out.println("Monadic["+ scMnemonics[ssaOpcode]+"] ( "+ operands[0].typeName() + " )");
	}
	

}
