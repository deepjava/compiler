package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;

public class NoOpnd extends SSAInstruction {
	
	
	public NoOpnd(int opcode){
		bytecodeIndex = opcode;		
	}

	@Override
	public SSAValue[] getOperands() {
		return null;
	}

	@Override
	public void setOperands(SSAValue[] operands) {
		//return immediately
	}
	@Override
	public String toString(){
		return result+" = "+ bcMnemonics[bytecodeIndex];
	}

}