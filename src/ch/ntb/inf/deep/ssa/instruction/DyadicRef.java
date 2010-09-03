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
		System.out.println("DyadicRef["+ scMnemonics[ssaOpcode]+"] ( "+ operands[0].typeName() + ", " + operands[1].typeName() + " )");
	}
}
