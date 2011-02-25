package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.ssa.SSAValue;

public class StoreToArray extends SSAInstruction {

	public StoreToArray(int opCode, SSAValue arrayref, SSAValue index, SSAValue value){
		ssaOpcode = opCode;
		operands = new SSAValue[]{arrayref, index, value};
	}

	public StoreToArray(int opCode){
		ssaOpcode = opCode;
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
	public void print(int level) {
		for (int i = 0; i < level*3; i++)StdStreams.vrb.print(" ");
		StdStreams.vrb.print(result.n + ": ");
		StdStreams.vrb.print("StoreToArray["+ scMnemonics[ssaOpcode]+"] {"+ operands[0].n + ", " + operands[1].n + ", " + operands[2].n+"}");
		StdStreams.vrb.print(" (" + result.typeName() + ")");
		StdStreams.vrb.print(",   end=" + result.end);
		if (result.index != -1) StdStreams.vrb.print(", index=" + result.index);
		if (result.regLong != -1) StdStreams.vrb.print(", regLong=" + result.regLong);
		if (result.reg != -1) StdStreams.vrb.print(", reg=" + result.reg);
		if (result.join != null) StdStreams.vrb.print(", join={" + result.join.n + "}");
		StdStreams.vrb.println();
	}

}
