package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.classItems.StdConstant;
import ch.ntb.inf.deep.classItems.StringLiteral;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.ssa.SSAValue;

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
		for (int i = 0; i < level*3; i++)StdStreams.vrb.print(" ");
		StdStreams.vrb.print(result.n + ": ");
		StdStreams.vrb.print("NoOpnd["+ scMnemonics[ssaOpcode]+"] ");
		if (ssaOpcode == sCloadConst) 
			if (result.constant instanceof StdConstant) {
				StdConstant constant = (StdConstant)result.constant;
				if (constant.name == null) {
					StdStreams.vrb.print(constant.valueH);
				} else {
					long value = ((long)(constant.valueH)<<32) | (constant.valueL&0xFFFFFFFFL);
					char category = constant.type.name.charAt(0);
					if (category == 'I') 
						StdStreams.vrb.print(constant.valueH);
					else if (category == 'J') 
						StdStreams.vrb.print(value);
					else if (category == 'F') 
						StdStreams.vrb.print(Float.toString(Float.intBitsToFloat(constant.valueH)));
					else
						StdStreams.vrb.print(Double.longBitsToDouble(value));
				}
				StdStreams.vrb.print(" (" + result.typeName() + ")");
			} else {
				StringLiteral str = (StringLiteral)result.constant;
				if (str != null) StdStreams.vrb.print("\"" + str.string + "\"");
				else StdStreams.vrb.print("null");
				StdStreams.vrb.print(" (" + result.typeName() + ")");
			}
		else
			StdStreams.vrb.print("(" + result.typeName() + ")");
		if (result.index != -1) StdStreams.vrb.print(", index=" + result.index);
		if (result.join != null) {
			StdStreams.vrb.print(", join=[" + result.join.index + "]");
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
		sb.append("NoOpnd["+ scMnemonics[ssaOpcode]+"] ");
		if (ssaOpcode == sCloadConst) 
			if (result.constant instanceof StdConstant) {
				StdConstant constant = (StdConstant)result.constant;
				if (constant.name == null) {
					sb.append(constant.valueH);
				} else {
					long value = ((long)(constant.valueH)<<32) | (constant.valueL&0xFFFFFFFFL);
					char category = constant.type.name.charAt(0);
					if (category == 'I') 
						sb.append(constant.valueH);
					else if (category == 'J') 
						sb.append(value);
					else if (category == 'F') 
						sb.append(Float.toString(Float.intBitsToFloat(constant.valueH)));
					else
						sb.append(Double.longBitsToDouble(value));
				}
				sb.append(" (" + result.typeName() + ")");
			} else {
				StringLiteral str = (StringLiteral)result.constant;
				if (str != null) sb.append("\"" + str.string + "\"");
				else sb.append("null");
				sb.append(" (" + result.typeName() + ")");
			}
		else
			sb.append("(" + result.typeName() + ")");
		if (result.index != -1) sb.append(", index=" + result.index);
		if (result.join != null) {
			sb.append(", join=[" + result.join.index + "]");
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
