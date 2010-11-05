package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.classItems.Item;

public class NoOpndRef extends NoOpnd {
	public Item field;

	public NoOpndRef(int opcode){
		super(opcode);
	}
	
	public NoOpndRef(int opcode, Item field) {
		super(opcode);
		this.field = field;
	}
	
	public void setArg(Item field){
		this.field = field;
	}

	@Override
	public void print(int level) {
		for (int i = 0; i < level*3; i++)System.out.print(" ");
		System.out.print(result.n + ": ");
		System.out.print("NoOpndRef["+ scMnemonics[ssaOpcode]+"]");
		if(field.name != null) System.out.print(" {" + field.name + "}");
		System.out.print(" (" + result.typeName() + ")");
		System.out.print(",   end=" + result.end);
		if (result.index != -1) System.out.print(", index=" + result.index);
		if (result.regLong != -1) System.out.print(", regLong=" + result.regLong);
		if (result.reg != -1) System.out.print(", reg=" + result.reg);
		if (result.join != null) System.out.print(", join={" + result.join.n + "}");
		System.out.println();
	}

}
