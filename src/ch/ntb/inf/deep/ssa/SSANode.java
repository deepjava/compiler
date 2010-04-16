package ch.ntb.inf.deep.ssa;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.ssa.instruction.SSAInstruction;

public class SSANode extends CFGNode {
	private int nofInstr = 4;
	private int nofPhiFunc = 2;
	public SSAValue entrySet[];
	public SSAValue exitSet[];
	public SSAValue operandStack[];
	public SSAInstruction instructions[];
	public PhiFunction phiFunctions[];	



	public SSANode(int maxLocals, int maxStack){
		super();
		entrySet = new SSAValue[maxLocals];
		exitSet = new SSAValue[maxLocals];
		operandStack = new SSAValue[maxStack];
		instructions = new SSAInstruction[nofInstr];
		phiFunctions = new PhiFunction[nofPhiFunc];
		
	}
	
}