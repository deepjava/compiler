package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;

public class PhiFunction extends SSAInstruction {
	private int nofOperands;
	

	public PhiFunction(int opCode, int nofOperands) {
		this.ssaOpcode = opCode;
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
		if(operands.length >= pos){
			throw new ArrayIndexOutOfBoundsException();
		}
		operands[pos] = operand;
		nofOperands++;
	}
	@Override
	public void print(int level) {
		for (int i = 0; i < level*3; i++)System.out.print(" ");
		System.out.print("PhiFunction["+ scMnemonics[ssaOpcode]+"] (");
		for (int i=0;i<operands.length-1;i++){
			System.out.print(operands[i].typeName()+", ");
		}
		System.out.println(operands[operands.length-1].typeName()+")");		
	}
}
