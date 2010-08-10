package ch.ntb.inf.deep.ssa;

/**
 * @author   millischer
 */
public class SSAValue implements SSAValueType {
	public int type;
//	public SSAValue reference;
	public Object constant;
	public int n = -1;	// each ssa-instruction is numbered for the register allocation 
	public int end;	// indicates the end number of the live range for this value
	public SSAValue join = this;	// representative, used for joining values during register allocation
	public int reg = -1;	// register or memory slot number
	public int memorySlot = -1;
	
	public SSAValue(){
	}

}
