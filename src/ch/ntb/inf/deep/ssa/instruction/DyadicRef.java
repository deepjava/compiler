package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;

public class DyadicRef extends Dyadic {
	int ref;

	public DyadicRef(int opCode) {
		super(opCode);
	}

	public DyadicRef(int opCode, int ref) {
		super(opCode);
		this.ref = ref;
	}

	public DyadicRef(int opCode, SSAValue operand1,	SSAValue operand2) {
		super(opCode, operand1, operand2);
	}

	public DyadicRef(int opCode, int ref, SSAValue operand1, SSAValue operand2) {
		super(opCode, operand1, operand2);
		this.ref = ref;
	}

	public void setArg(int ref) {
		this.ref = ref;
	}

	public int getArg() {
		return ref;
	}

	@Override
	public String toString() {
		return result + " = " + bcMnemonics[bytecodeIndex] + " " + ref+ " (" + operands[0] + ", " + operands[1] + ")";
	}

}
