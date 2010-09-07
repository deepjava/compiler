package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;

public class NoOpnd extends SSAInstruction {
	
	
	public NoOpnd(int opcode){
		ssaOpcode = opcode;		
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
	public void print(int level) {
		for (int i = 0; i < level*3; i++)System.out.print(" ");
		System.out.print(result.n + ": ");
		System.out.print("NoOpnd["+ scMnemonics[ssaOpcode]+"] " + result.constant);
		System.out.print(",   end=" + result.end + ", reg=" + result.reg + ", state array index=" + result.index );
		System.out.println();
	}
}
