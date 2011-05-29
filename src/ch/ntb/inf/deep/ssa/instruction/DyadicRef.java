package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.ssa.SSAValue;

public class DyadicRef extends Dyadic {
	public Item field;

	public DyadicRef(int opCode) {
		super(opCode);
	}

	public DyadicRef(int opCode, Item field) {
		super(opCode);
		this.field = field;
	}

	public DyadicRef(int opCode, SSAValue operand1,	SSAValue operand2) {
		super(opCode, operand1, operand2);
	}

	public DyadicRef(int opCode, Item field, SSAValue operand1, SSAValue operand2) {
		super(opCode, operand1, operand2);
		this.field = field;
	}

	public void setArg(Item field) {
		this.field = field;
	}

	public Item getArg() {
		return field;
	}

	@Override
	public void print(int level) {
		for (int i = 0; i < level*3; i++)StdStreams.vrb.print(" ");
		StdStreams.vrb.print(result.n + ": ");
		StdStreams.vrb.print("DyadicRef["+ scMnemonics[ssaOpcode]+"] {"+ operands[0].n + ", " + operands[1].n + "}");
		if (field.name != null) StdStreams.vrb.print(" <" + field.name + "(" + field.type.name + ")>");
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
		sb.append("DyadicRef["+ scMnemonics[ssaOpcode]+"] {"+ operands[0].n + ", " + operands[1].n + "}");
		if (field.name != null) sb.append(" <" + field.name + "(" + field.type.name + ")>");
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
