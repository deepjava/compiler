package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.ssa.SSAValue;

public class Call extends SSAInstruction {
	public Item item;
	public boolean isStatic = false;
	public boolean isInterface = false;

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
		for (int i = 0; i < level*3; i++)System.out.print(" ");
		System.out.print(result.n + ": ");
		System.out.print("Call["+ scMnemonics[ssaOpcode]+"] {");
		for (int i=0;i<operands.length-1;i++){
			System.out.print(operands[i].n + ", ");
		}
		if (operands.length > 0){
			System.out.print(operands[operands.length-1].n + "}");
		} else {
			System.out.print("}");
		}
		System.out.print(" (" + result.typeName() + ")");
		if (item != null) System.out.print(",   " + item.name);
//			System.out.print(",   " + operands[0].constant);			
		System.out.print(",   end=" + result.end);
		if (result.index != -1) System.out.print(", index=" + result.index);
		if (result.regLong != -1) System.out.print(", regLong=" + result.regLong);
		if (result.reg != -1) System.out.print(", reg=" + result.reg);
		if (result.join != null) System.out.print(", join={" + result.join.n + "}");
		System.out.println();
	}

}
