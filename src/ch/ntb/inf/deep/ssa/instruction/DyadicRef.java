package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.classItems.Item;
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
		for (int i = 0; i < level*3; i++)System.out.print(" ");
		System.out.print(result.n + ": ");
		System.out.print("DyadicRef["+ scMnemonics[ssaOpcode]+"] {"+ operands[0].n + ", " + operands[1].n + "}");
		if (field.name != null) System.out.print(" <" + field.name + "(" + field.type.name + ")>");
		System.out.print(" (" + result.typeName() + ")");
		System.out.print(",   end=" + result.end);
		if (result.index != -1) System.out.print(", index=" + result.index);
		if (result.regLong != -1) System.out.print(", regLong=" + result.regLong);
		if (result.reg != -1) System.out.print(", reg=" + result.reg);
		if (result.join != null) System.out.print(", join={" + result.join.n + "}");
		System.out.println();
	}
}
