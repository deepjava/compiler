package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.ssa.SSAValue;

public class MonadicRef extends Monadic {
	public Item item;
	
	public MonadicRef(int opCode) {
		super(opCode);		
	}
	
	public MonadicRef(int opCode, Item item){
		super(opCode);
		this.item = item;
	}
	
	public MonadicRef(int opCode, SSAValue operand){
		super(opCode,operand);
	}
	
	public MonadicRef(int opCode, Item item, SSAValue operand){
		super(opCode,operand);
		this.item = item;
	}
	
	public void setArgs(Item item){
		this.item = item;
	}
	
	@Override
	public void print(int level) {
		for (int i = 0; i < level*3; i++)System.out.print(" ");
		System.out.print(result.n + ": ");
		System.out.print("MonadicRef["+ scMnemonics[ssaOpcode]+"] {"+ operands[0].n + "}");
		if (item != null) System.out.print(" {" + item.name + "}");
		System.out.print(" (" + result.typeName() + ")");
		System.out.print(",   end=" + result.end);
		if (result.index != -1) System.out.print(", index=" + result.index);
		if (result.regLong != -1) System.out.print(", regLong=" + result.regLong);
		if (result.reg != -1) System.out.print(", reg=" + result.reg);
		if (result.join != null) System.out.print(", join={" + result.join.n + "}");
		System.out.println();
	}
	

}
