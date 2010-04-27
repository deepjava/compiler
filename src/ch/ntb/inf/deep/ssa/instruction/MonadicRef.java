package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;

public class MonadicRef extends Monadic {
	
	int ref;
	
	
	public MonadicRef(int opCode) {
		super(opCode);		
	}
	
	public MonadicRef(int opCode, int ref){
		super(opCode);
		this.ref = ref;
	}
	
	public MonadicRef(int opCode, SSAValue operand){
		super(opCode,operand);
	}
	
	public MonadicRef(int opCode, int ref, SSAValue operand){
		super(opCode,operand);
		this.ref = ref;
	}
	
	public void setArg(int ref){
		this.ref =ref;
	}
	
	public int getArg(){
		return ref;
	}
	
	@Override
	public String toString(){
		return result+" = "+ bcMnemonics[bytecodeIndex]+" "+ref+" ("+operands[0]+")";
	}
	

}
