package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.classItems.Constant;
import ch.ntb.inf.deep.classItems.StringLiteral;
import ch.ntb.inf.deep.ssa.SSAValue;
import ch.ntb.inf.deep.strings.HString;

public class NoOpnd extends SSAInstruction {
	
	public NoOpnd(int opcode){
		ssaOpcode = opcode;		
	}

	@Override
	public SSAValue[] getOperands() {
		return null;
	}

	@Override
	public void setOperands(SSAValue[] operands) {
		//return immediately
	}
	@Override
	public void print(int level) {
		for (int i = 0; i < level*3; i++)System.out.print(" ");
		System.out.print(result.n + ": ");
		System.out.print("NoOpnd["+ scMnemonics[ssaOpcode]+"] ");
		if (ssaOpcode == sCloadConst) 
			if (result.constant instanceof Constant) {
				Constant constant = (Constant)result.constant;
				if (constant.name == null) {
					System.out.print(constant.valueH);
				} else {
					long value = ((long)(constant.valueH)<<32) | (constant.valueL&0xFFFFFFFFL);
					char category = constant.type.name.charAt(0);
					if (category == 'I') 
						System.out.print(constant.valueH);
					else if (category == 'J') 
						System.out.print(value);
					else if (category == 'F') 
						System.out.print(Float.toString(Float.intBitsToFloat(constant.valueH)));
					else
						System.out.print(Double.longBitsToDouble(value));
				}
				System.out.print(" (" + result.typeName() + ")");
			} else {
				StringLiteral str = (StringLiteral)result.constant;
				if (str != null) System.out.print(str.string);
				System.out.print(" (" + result.typeName() + ")");
			}
		else
			System.out.print("(" + result.typeName() + ")");
		System.out.print(",   end=" + result.end);
		if (result.index != -1) System.out.print(", index=" + result.index);
		if (result.regLong != -1) System.out.print(", regLong=" + result.regLong);
		if (result.reg != -1) System.out.print(", reg=" + result.reg);
		if (result.join != null) System.out.print(", join={" + result.join.n + "}");
		System.out.println();
	}
}
