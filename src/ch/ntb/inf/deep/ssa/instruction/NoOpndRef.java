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
		System.out.print(result.n + ": ");
		System.out.print("NoOpndRef["+ scMnemonics[ssaOpcode]+"]");
		if(fieldName != null){
			System.out.print("{" + fieldName + "}");
		}
		System.out.print(" (" + result.typeName() + ")");
		System.out.print(",   end=" + result.end);
		if (result.index != -1) System.out.print(", index=" + result.index);
		if (result.regLong != -1) System.out.print(", regLong=" + result.regLong);
		if (result.reg != -1) System.out.print(", reg=" + result.reg);
		if (result.join != null) System.out.print(", join={" + result.join.n + "}");
		System.out.println();
	}

}
