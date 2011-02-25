package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.host.StdStreams;
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
		for (int i = 0; i < level*3; i++)StdStreams.vrb.print(" ");
		StdStreams.vrb.print(result.n + ": ");
		StdStreams.vrb.print("Monadic["+ scMnemonics[ssaOpcode]+"] {"+ operands[0].n + "}");
		StdStreams.vrb.print(" (" + result.typeName() + ")");
		StdStreams.vrb.print(",   end=" + result.end);
		if (result.index != -1) StdStreams.vrb.print(", index=" + result.index);
		if (result.regLong != -1) StdStreams.vrb.print(", regLong=" + result.regLong);
		if (result.reg != -1) StdStreams.vrb.print(", reg=" + result.reg);
		if (result.join != null) StdStreams.vrb.print(", join={" + result.join.n + "}");
		StdStreams.vrb.println();
	}
	

}
