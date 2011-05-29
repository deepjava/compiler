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
		if (item != null) {
			StdStreams.vrb.print(" <" + item.name);
			if (item.type != null) StdStreams.vrb.print("(" + item.type.name + ")");
			StdStreams.vrb.print(">");
		}
		if(item instanceof ClassMember) {StdStreams.vrb.print("Owner: "); ((ClassMember)item).printOwner();}
		StdStreams.vrb.print(" (" + result.typeName() + ")");
		if (result.index != -1) StdStreams.vrb.print(", index=" + result.index);
		if (result.join != null) {
			StdStreams.vrb.print(", join=[" + result.index + "]");
		} else {
			StdStreams.vrb.print(", end=" + result.end);
			if (result.reg != -1) {
				if (result.nonVol) StdStreams.vrb.print(", nonVol"); else StdStreams.vrb.print(", vol");
			}
			if (result.regLong != -1) StdStreams.vrb.print(", regLong=" + result.regLong);
			if (result.reg != -1) StdStreams.vrb.print(", reg=" + result.reg);
			if (result.regAux1 != -1) StdStreams.vrb.print(", regAux1=" + result.regAux1);
		}
		StdStreams.vrb.println();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(result.n + ": ");
		sb.append("MonadicRef["+ scMnemonics[ssaOpcode]+"] {"+ operands[0].n + "}");
		if (item != null) {
			sb.append(" <" + item.name);
			if (item.type != null) sb.append("(" + item.type.name + ")");
			sb.append(">");
		}
		if(item instanceof ClassMember) {sb.append("Owner: " + ((ClassMember)item).owner.name.toString());}
		sb.append(" (" + result.typeName() + ")");
		if (result.index != -1) sb.append(", index=" + result.index);
		if (result.join != null) {
			sb.append(", join=[" + result.index + "]");
		} else {
			sb.append(", end=" + result.end);
			if (result.reg != -1) {
				if (result.nonVol) sb.append(", nonVol"); else sb.append(", vol");
			}
			if (result.regLong != -1) sb.append(", regLong=" + result.regLong);
			if (result.reg != -1) sb.append(", reg=" + result.reg);
			if (result.regAux1 != -1) sb.append(", regAux1=" + result.regAux1);
		}
		return sb.toString();
	}
	

}
