package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.cgPPC.RegAllocator;
import ch.ntb.inf.deep.classItems.ClassMember;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.ssa.SSAValue;

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
		for (int i = 0; i < level*3; i++) StdStreams.vrb.print(" ");
		StdStreams.vrb.println(toString());
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
			sb.append(", join=[" + result.index + "(");
			SSAValue join = RegAllocator.joins[result.index];
			int i = 0;
			while (join != null && join != result.join) {
				i++;
				join = join.next;
			}
			sb.append(i + ")]");
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
