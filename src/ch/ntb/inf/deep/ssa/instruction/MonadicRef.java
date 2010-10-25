package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;
import ch.ntb.inf.deep.strings.HString;

public class MonadicRef extends Monadic {
	
	HString className;
	HString fieldName;
	
	
	public MonadicRef(int opCode) {
		super(opCode);		
	}
	
	public MonadicRef(int opCode, HString className, HString fieldName){
		super(opCode);
		this.className = className;
		this.fieldName = fieldName;
	}
	
	public MonadicRef(int opCode, SSAValue operand){
		super(opCode,operand);
	}
	
	public MonadicRef(int opCode, HString className, HString fieldName, SSAValue operand){
		super(opCode,operand);
		this.className = className;
		this.fieldName = fieldName;
	}
	
	public void setArgs(HString className, HString fieldName){
		this.className = className;
		this.fieldName = fieldName;
	}
	
	public HString[] getArgs(){
		return new HString[]{className, fieldName};
	}
	
	@Override
	public void print(int level) {
		for (int i = 0; i < level*3; i++)System.out.print(" ");
		System.out.print(result.n + ": ");
		System.out.print("MonadicRef["+ scMnemonics[ssaOpcode]+"] {"+ operands[0].n + "}");
		System.out.print(" (" + result.typeName() + ")");
		System.out.print(",   end=" + result.end);
		if (result.index != -1) System.out.print(", index=" + result.index);
		if (result.reg != -1) System.out.print(", reg=" + result.reg);
		if (result.regLong != -1) System.out.print(", regLong=" + result.regLong);
		if (result.join != null) System.out.print(", join={" + result.join.n + "}");
		System.out.println();
	}
	

}
