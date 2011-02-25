package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.classItems.ClassMember;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.host.StdStreams;
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
		for (int i = 0; i < level*3; i++)StdStreams.vrb.print(" ");
		StdStreams.vrb.print(result.n + ": ");
		StdStreams.vrb.print("MonadicRef["+ scMnemonics[ssaOpcode]+"] {"+ operands[0].n + "}");
		if (item != null) StdStreams.vrb.print(" <" + item.name + "(" + item.type.name + ")>");
		if(item instanceof ClassMember) {StdStreams.vrb.print("Owner: "); ((ClassMember)item).printOwner();}
		StdStreams.vrb.print(" (" + result.typeName() + ")");
		StdStreams.vrb.print(",   end=" + result.end);
		if (result.index != -1) StdStreams.vrb.print(", index=" + result.index);
		if (result.regLong != -1) StdStreams.vrb.print(", regLong=" + result.regLong);
		if (result.reg != -1) StdStreams.vrb.print(", reg=" + result.reg);
		if (result.join != null) StdStreams.vrb.print(", join={" + result.join.n + "}");
		StdStreams.vrb.println();
	}
	

}
