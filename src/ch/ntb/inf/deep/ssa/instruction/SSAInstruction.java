package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;
import ch.ntb.inf.deep.cfg.*;

/**
 * @author  millischer
 */
public abstract class SSAInstruction implements JvmInstructionMnemonics {
	protected SSAValue[] operands;
	public SSAValue result;
	public int ssaOpcode;
	
	public abstract void setOperands(SSAValue[] operands);
	public abstract SSAValue[] getOperands();
	public String toString(){
		return "SSAInstruction";
	}

}
