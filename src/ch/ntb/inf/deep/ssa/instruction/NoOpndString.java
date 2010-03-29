package ch.ntb.inf.deep.ssa.instruction;

public class NoOpndString extends NoOpnd {
	String symRef;

	public NoOpndString(int opcode){
		super(opcode);
	}
	
	public NoOpndString(int opcode, String symRef) {
		super(opcode);
		this.symRef = symRef;
	}
	
	public void setArg(String symRef){
		this.symRef=symRef;
	}

	public String getArg(){
		return symRef;
	}
	
	@Override
	public String toString(){
		return result+" = "+ bcMnemonics[bytecodeIndex]+" "+symRef;
	}

}
