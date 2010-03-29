package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;
import ch.ntb.inf.deep.cfg.*;

/**
 * @author  millischer
 */
public abstract class SSAInstruction implements JvmInstructionMnemonics {
	protected SSAValue[] operands;
	protected SSAValue result;
	protected int bytecodeIndex;
	
	public void setResult(SSAValue result){
		this.result = result;
	}
	public SSAValue getResult(){
		return result;
	}
	abstract void setOperands(SSAValue[] operands);
	abstract SSAValue[] getOperands();
	public String toString(){
		return "SSAInstruction";
	}

}
