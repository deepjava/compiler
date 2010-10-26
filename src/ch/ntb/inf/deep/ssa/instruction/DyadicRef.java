package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;
import ch.ntb.inf.deep.strings.HString;

public class DyadicRef extends Dyadic {
	HString fieldName;

	public DyadicRef(int opCode) {
		super(opCode);
	}

	public DyadicRef(int opCode, HString fieldName) {
		super(opCode);
		this.fieldName = fieldName;
	}

	public DyadicRef(int opCode, SSAValue operand1,	SSAValue operand2) {
		super(opCode, operand1, operand2);
	}

	public DyadicRef(int opCode, HString fieldName, SSAValue operand1, SSAValue operand2) {
		super(opCode, operand1, operand2);
		this.fieldName = fieldName;
	}

	public void setArg(HString fieldName) {
		this.fieldName = fieldName;
	}

	public HString getArg() {
		return fieldName;
	}

	@Override
	public void print(int level) {
		for (int i = 0; i < level*3; i++)System.out.print(" ");
		System.out.print(result.n + ": ");
		System.out.print("DyadicRef["+ scMnemonics[ssaOpcode]+"] {"+ operands[0].n + ", " + operands[1].n + "}");
		System.out.print(" (" + result.typeName() + ")");
		System.out.print(",   end=" + result.end);
		if (result.index != -1) System.out.print(", index=" + result.index);
		if (result.regLong != -1) System.out.print(", regLong=" + result.regLong);
		if (result.reg != -1) System.out.print(", reg=" + result.reg);
		if (result.join != null) System.out.print(", join={" + result.join.n + "}");
		System.out.println();
	}
}
