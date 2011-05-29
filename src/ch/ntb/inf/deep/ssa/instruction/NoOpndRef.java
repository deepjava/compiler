package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.classItems.ClassMember;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.host.StdStreams;

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
		for (int i = 0; i < level*3; i++)StdStreams.vrb.print(" ");
		StdStreams.vrb.print(result.n + ": ");
		StdStreams.vrb.print("NoOpndRef["+ scMnemonics[ssaOpcode]+"]");
		if (field.name != null) StdStreams.vrb.print(" <" + field.name + "(" + field.type.name + ")>");
		if(field instanceof ClassMember) {StdStreams.vrb.print("Owner: "); ((ClassMember)field).printOwner();}
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
		sb.append("NoOpndRef["+ scMnemonics[ssaOpcode]+"]");
		if (field.name != null) sb.append(" <" + field.name + "(" + field.type.name + ")>");
		if(field instanceof ClassMember) {sb.append("Owner: " + ((ClassMember)field).owner.name.toString());}
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
