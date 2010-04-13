package ch.ntb.inf.deep.ssa;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.ssa.instruction.SSAInstruction;

public class SSANode extends CFGNode {
	public SSAValue entrySet;
	public SSAValue exitSet;
	public SSAValue stateArray[];
	public SSAInstruction instructions[];
	public PhiFunction phiFunctions[];	

}
