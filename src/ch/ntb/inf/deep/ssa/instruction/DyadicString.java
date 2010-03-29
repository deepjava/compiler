package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;

public class DyadicString extends Dyadic {
	String symRef;

	public DyadicString(int opCode) {
		super(opCode);
	}

	public DyadicString(int opCode, String symRef) {
		super(opCode);
		this.symRef = symRef;
	}

	public DyadicString(int opCode, SSAValue operand1,	SSAValue operand2) {
		super(opCode, operand1, operand2);
	}

	public DyadicString(int opCode, String symRef, SSAValue operand1, SSAValue operand2) {
		super(opCode, operand1, operand2);
		this.symRef = symRef;
	}

	public void setArg(String symRef) {
		this.symRef = symRef;
	}

	public String getArg() {
		return symRef;
	}

	@Override
	public String toString() {
		return result + " = " + bcMnemonics[bytecodeIndex] + " " + symRef+ " (" + operands[0] + ", " + operands[1] + ")";
	}

}
