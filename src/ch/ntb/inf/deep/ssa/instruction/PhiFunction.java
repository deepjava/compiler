package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;

public class PhiFunction extends SSAInstruction {
	public int nofOperands;
	public boolean deleted = false;
	public boolean visited = false;
	public int start;
	

	public PhiFunction(int opCode) {
		this.ssaOpcode = opCode;
		operands = new SSAValue[2];
		nofOperands = 0;
	}

	@Override
	public SSAValue[] getOperands() {
		SSAValue[] opnd = new SSAValue[nofOperands];
		for(int i = 0; i < nofOperands; i++){
			opnd[i]=operands[i];
		}
		return opnd;
	}

	@Override
	public void setOperands(SSAValue[] operands) {
		this.operands = operands;
		nofOperands = operands.length;

	}
	public void addOperand(SSAValue operand) {
		int len = operands.length;
		if (len == nofOperands) {
			SSAValue[] newArray = new SSAValue[2 * len];
			for (int i = 0; i < len; i++) {
				newArray[i] = operands[i];
			}
			operands = newArray;
		}
		operands[nofOperands++] = operand;
		
	}
	
	public void insertOperand(SSAValue operand, int pos){
		if(pos >= nofOperands || pos < 0){
			throw new IndexOutOfBoundsException("invalid Position");
		}
		operands[pos] = operand;
	}
	@Override
	public void print(int level) {
		for (int i = 0; i < level*3; i++)System.out.print(" ");
		System.out.print(result.n + ": ");
		System.out.print("PhiFunction["+ scMnemonics[ssaOpcode]+"] {");
		for (int i=0;i<nofOperands-1;i++){
			if(operands[i] != null){
				System.out.print(operands[i].n + ", ");
			}else{
				System.out.print("null, ");
			}
		}
		if(operands[nofOperands-1] != null){
			System.out.print(operands[nofOperands-1].n + "}");
		}else{
			System.out.print("null)");
		}	
		System.out.print(" (" + result.typeName() + ")");
		System.out.print(",   start=" + start + ", end=" + result.end);
		if (result.index != -1) System.out.print(", index=" + result.index);
		if (result.regLong != -1) System.out.print(", regLong=" + result.regLong);
		if (result.reg != -1) System.out.print(", reg=" + result.reg);
		if (result.join != null) System.out.print(", join={" + result.join.n + "}");
		if (deleted) System.out.print(" deleted");
		System.out.println();
	}
}
