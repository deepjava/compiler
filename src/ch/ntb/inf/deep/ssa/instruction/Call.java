package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;

public class Call extends SSAInstruction {
	
	String symRef;

	public Call(int opCode) {
		bytecodeIndex = opCode;
	}
	
	public Call(int opCode, String symRef){
		bytecodeIndex = opCode;
		this.symRef = symRef;
	}
	
	
	public Call(int opCode,SSAValue[] operands){
		bytecodeIndex = opCode;
		this.operands = operands;
	}
	
	public Call(int opCode, String symRef, SSAValue[] operands){
		bytecodeIndex = opCode;
		this.symRef = symRef;
		this.operands = operands;
	}

	@Override
	public SSAValue[] getOperands() {
		return operands;
	}

	@Override
	public void setOperands(SSAValue[] operands) {
		this.operands = operands;
	}
	
	public void setStringArg(String symRef){
		this.symRef = symRef;
	}
	
	public String getStringArg(){
		return symRef;
	}
	
	@Override
	public String toString() {
		String r = result+" = "+ bcMnemonics[bytecodeIndex]+" " + symRef+ " (";
		for (int i=0;i<operands.length;i++){
			r= r+ operands[i];
		}
		r = r+ ")";
		return r;
	}

}
