package ch.ntb.inf.deep.ssa;

/**
 * @author   millischer
 */
public class SSAValue implements SSAValueType {
	public int type;
	public SSAValue reference;
	public Object constant;
	/**
	 * Instruction Number for the Register-Allocation.
	 */
	public int n = -1;

	/**
	 * Register or Memory-Slot Number.
	 */
	public int reg = -1;
	public int memorySlot = -1;
	
	public SSAValue(){
		
	}
	//TODO add Methods
}
