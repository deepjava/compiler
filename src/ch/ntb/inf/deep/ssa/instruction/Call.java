package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.cgPPC.RegAllocator;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.ssa.SSAValue;

public class Call extends SSAInstruction {
	public Item item;
	public boolean invokespecial;

	public Call(int opCode) {
		ssaOpcode = opCode;
	}
	
	public Call(int opCode, Item item){
		ssaOpcode = opCode;
		this.item = item;
	}
	
	public Call(int opCode,SSAValue[] operands){
		ssaOpcode = opCode;
		this.operands = operands;
	}
	
	public Call(int opCode, Item item, SSAValue[] operands){
		ssaOpcode = opCode;
		this.item = item;
		this.operands = operands;
	}

	@Override
	public SSAValue[] getOperands() {
		return operands;
	}

	@Override
	public void setOperands(SSAValue[] operands) {
		this.operands = operands;
	}
	
	public void setArgs(Item item){
		this.item = item;
	}
	
	@Override
	public void print(int level) {
		for (int i = 0; i < level*3; i++) StdStreams.vrb.print(" ");
		StdStreams.vrb.println(toString());
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(result.n + ": ");
		sb.append("Call["+ scMnemonics[ssaOpcode]+"]");
		if (operands != null) {
			sb.append(" {");
			for (int i=0;i<operands.length-1;i++){
				sb.append(operands[i].n + ", ");
			}
			if (operands.length > 0){
				sb.append(operands[operands.length-1].n + "}");
			} else {
				sb.append("}");
			}
		}
		sb.append(" (" + result.typeName() + ") ");
		if (item != null) { 
			if (item instanceof Method) {
				sb.append(((Method)item).owner.name);
				sb.append("." + item.name + ((Method)item).methDescriptor);
			} else
				sb.append(item.name);
		}
		if (result.index != -1) sb.append(", index=" + result.index);
		if (result.join != null) {
			sb.append(", join=[" + result.index + "(");
			SSAValue join = RegAllocator.joins[result.index];
			int i = 0;
			while (join != result.join) {
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
