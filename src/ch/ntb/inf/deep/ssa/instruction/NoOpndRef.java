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
	public void print(int level) {
		for (int i = 0; i < level; i++)System.out.print("\t");
		System.out.println("NoOpndRef["+ scMnemonics[ssaOpcode]+"]");
	}

}
