package ch.ntb.inf.deep.ssa.instruction;

public class NoOpndRef extends NoOpnd {
	int ref;

	public NoOpndRef(int opcode){
		super(opcode);
	}
	
	public NoOpndRef(int opcode, int ref) {
		super(opcode);
		this.ref = ref;
	}
	
	public void setArg(int ref){
		this.ref=ref;
	}

	public int getArg(){
		return ref;
	}
	
	@Override
	public String toString(){
		return result+" = "+ bcMnemonics[ssaOpcode]+" "+ref;
	}

}
