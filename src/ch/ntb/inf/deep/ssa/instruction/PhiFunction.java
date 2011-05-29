package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.ssa.SSAValue;

public class PhiFunction extends SSAInstruction {
	public int nofOperands;
	public boolean deleted = false;
	public boolean visited = false;
	public boolean used = false;
	public int start;
	public int last;	

	public PhiFunction(int opCode) {
		this.ssaOpcode = opCode;
		operands = new SSAValue[2];
		nofOperands = 0;
	}

	@Override
	public SSAValue[] getOperands() {
		SSAValue[] opnd = new SSAValue[nofOperands];
		for(int i = 0; i < nofOperands; i++){
			opnd[i]=operands[i];
		}
		return opnd;
	}

	@Override
	public void setOperands(SSAValue[] operands) {
		this.operands = operands;
		nofOperands = operands.length;

	}
	public void addOperand(SSAValue operand) {
		int len = operands.length;
		if (len == nofOperands) {
			SSAValue[] newArray = new SSAValue[2 * len];
			for (int i = 0; i < len; i++) {
				newArray[i] = operands[i];
			}
			operands = newArray;
		}
		operands[nofOperands++] = operand;
		
	}
	
	public void insertOperand(SSAValue operand, int pos){
		if(pos >= nofOperands || pos < 0){
			throw new IndexOutOfBoundsException("invalid Position");
		}
		operands[pos] = operand;
	}
	@Override
	public void print(int level) {
		for (int i = 0; i < level*3; i++)StdStreams.vrb.print(" ");
		StdStreams.vrb.print(result.n + ": ");
		StdStreams.vrb.print("PhiFunction["+ scMnemonics[ssaOpcode]+"] {");
		for (int i=0;i<nofOperands-1;i++){
			if(operands[i] != null){
				StdStreams.vrb.print(operands[i].n + ", ");
			}else{
				StdStreams.vrb.print("null, ");
			}
		}
		if (nofOperands > 0) {
			if(operands[nofOperands-1] != null){
				StdStreams.vrb.print(operands[nofOperands-1].n + "}");
			} else {
				StdStreams.vrb.print("null)");
			}
		}	
		StdStreams.vrb.print(" (" + result.typeName() + ")");
		if (result.index != -1) StdStreams.vrb.print(", index=" + result.index);
		SSAValue res = result;
		if (res.join != null) {
			StdStreams.vrb.print(", join=[" + res.index + "]");
			res = result.join;
		} else {
			StdStreams.vrb.print(", end=" + res.end);
			if (res.reg != -1) {
				if (res.nonVol) StdStreams.vrb.print(", nonVol"); else StdStreams.vrb.print(", vol");
			}
			if (res.regLong != -1) StdStreams.vrb.print(", regLong=" + res.regLong);
			if (res.reg != -1) StdStreams.vrb.print(", reg=" + res.reg);
			if (res.regAux1 != -1) StdStreams.vrb.print(", regAux1=" + res.regAux1);
		}
		if (last != 0) StdStreams.vrb.print(", last=" + last);
		if (deleted) StdStreams.vrb.print(" del");
		if (used) StdStreams.vrb.print(" u");
		StdStreams.vrb.println();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(result.n + ": ");
		sb.append("PhiFunction["+ scMnemonics[ssaOpcode]+"] {");
		for (int i=0;i<nofOperands-1;i++){
			if(operands[i] != null){
				sb.append(operands[i].n + ", ");
			}else{
				sb.append("null, ");
			}
		}
		if (nofOperands > 0) {
			if(operands[nofOperands-1] != null){
				sb.append(operands[nofOperands-1].n + "}");
			} else {
				sb.append("null)");
			}
		}	
		sb.append(" (" + result.typeName() + ")");
		if (result.index != -1) sb.append(", index=" + result.index);
		SSAValue res = result;
		if (res.join != null) {
			sb.append(", join=[" + res.index + "]");
			res = result.join;
		} else {
			sb.append(", end=" + res.end);
			if (res.reg != -1) {
				if (res.nonVol) sb.append(", nonVol"); else sb.append(", vol");
			}
			if (res.regLong != -1) sb.append(", regLong=" + res.regLong);
			if (res.reg != -1) sb.append(", reg=" + res.reg);
			if (res.regAux1 != -1) sb.append(", regAux1=" + res.regAux1);
		}
		if (last != 0) sb.append(", last=" + last);
		if (deleted) sb.append(" del");
		if (used) sb.append(" u");
		return sb.toString();
	}
}
