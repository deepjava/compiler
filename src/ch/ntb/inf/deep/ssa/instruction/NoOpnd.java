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
		System.out.print("NoOpnd["+ scMnemonics[ssaOpcode]+"] ");
		if (ssaOpcode == sCloadConst) 
			System.out.print(result.constant + " (" + result.typeName() + ")");
		else
			System.out.print("(" + result.typeName() + ")");
		System.out.print(",   end=" + result.end + ", index=" + result.index + ", reg=" + result.reg);
		System.out.println();
	}
}
