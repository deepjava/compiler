package ch.ntb.inf.deep.ssa;

public class Local extends SSAValue {
	/**
	 * Index in the localvariable-table
	 */
	int index;

	
	public Local(int index){
		super();
		this.index = index;
	}

}
