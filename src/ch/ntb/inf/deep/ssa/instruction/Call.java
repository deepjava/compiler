package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;

public class Call extends SSAInstruction {
	
	int ref;

	public Call(int opCode) {
		ssaOpcode = opCode;
	}
	
	public Call(int opCode, int ref){
		ssaOpcode = opCode;
		this.ref = ref;
	}
	
	
	public Call(int opCode,SSAValue[] operands){
		ssaOpcode = opCode;
		this.operands = operands;
	}
	
	public Call(int opCode, int ref, SSAValue[] operands){
		ssaOpcode = opCode;
		this.ref = ref;
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
	
	public void setArg(int ref){
		this.ref = ref;
	}
	
	public int getArg(){
		return ref;
	}
	
	@Override
	public String toString() {
		String r = result+" = "+ bcMnemonics[ssaOpcode]+" " + ref+ " (";
		for (int i=0;i<operands.length;i++){
			r= r+ operands[i];
		}
		r = r+ ")";
		return r;
	}

}
