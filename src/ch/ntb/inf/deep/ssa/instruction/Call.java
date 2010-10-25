package ch.ntb.inf.deep.ssa.instruction;

import ch.ntb.inf.deep.ssa.SSAValue;
import ch.ntb.inf.deep.strings.HString;

public class Call extends SSAInstruction {
	
	HString methodName;
	HString className;
	public boolean isStatic = false;
	public boolean isInterface = false;

	public Call(int opCode) {
		ssaOpcode = opCode;
	}
	
	public Call(int opCode, HString className , HString methodName){
		ssaOpcode = opCode;
		this.methodName = methodName;
		this.className = className;
	}
	
	
	public Call(int opCode,SSAValue[] operands){
		ssaOpcode = opCode;
		this.operands = operands;
	}
	
	public Call(int opCode, HString className , HString methodName, SSAValue[] operands){
		ssaOpcode = opCode;
		this.methodName = methodName;
		this.className = className;
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
	
	public void setArgs(HString className , HString methodName){
		this.methodName = methodName;
		this.className = className;
	}
	
	/**
	 * returns the ClassName at HString[0] and MethodeNamen at HString[1];
	 * @return HString[]
	 */
	public HString[] getArgs(){
		return new HString[]{this.className, this.methodName};
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
		if (className != null) {
			System.out.print(",   " + className);
			if (methodName != null) System.out.print("." + methodName);
		} else {
			System.out.print(",   " + operands[0].constant);			
		}
		System.out.print(",   end=" + result.end);
		if (result.index != -1) System.out.print(", index=" + result.index);
		if (result.reg != -1) System.out.print(", reg=" + result.reg);
		if (result.join != null) System.out.print(", join={" + result.join.n + "}");
		System.out.println();
	}

}
