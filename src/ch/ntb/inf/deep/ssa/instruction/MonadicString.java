package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;

public class MonadicString extends Monadic {
	
	String symRef;
	
	
	public MonadicString(int opCode) {
		super(opCode);		
	}
	
	public MonadicString(int opCode, String symRef){
		super(opCode);
		this.symRef = symRef;
	}
	
	public MonadicString(int opCode, SSAValue operand){
		super(opCode,operand);
	}
	
	public MonadicString(int opCode, String symRef, SSAValue operand){
		super(opCode,operand);
		this.symRef = symRef;
	}
	
	public void setArg(String symRef){
		this.symRef =symRef;
	}
	
	public String getArg(){
		return symRef;
	}
	
	@Override
	public String toString(){
		return result+" = "+ bcMnemonics[bytecodeIndex]+" "+symRef+" ("+operands[0]+")";
	}
	

}
