package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.strings.HString;

public class NoOpndRef extends NoOpnd {
	HString fieldName;

	public NoOpndRef(int opcode){
		super(opcode);
	}
	
	public NoOpndRef(int opcode, HString fieldName) {
		super(opcode);
		this.fieldName = fieldName;
	}
	
	public void setArg(HString fieldName){
		this.fieldName = fieldName;
	}

	public HString getArg(){
		return fieldName;
	}
	
	@Override
	public void print(int level) {
		for (int i = 0; i < level*3; i++)System.out.print(" ");
		System.out.print("NoOpndRef["+ scMnemonics[ssaOpcode]+"]");
		if(fieldName != null){
			System.out.println("(" + fieldName + ")");
		}else{
			System.out.println();
		}
	}

}
