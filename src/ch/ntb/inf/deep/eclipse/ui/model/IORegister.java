package ch.ntb.inf.deep.eclipse.ui.model;

public class IORegister extends Register {
	/**
	 * Register size in bytes
	 */
	public int size;
	
	public IORegister(){	
	}
	public IORegister(String name, int value, int representation, int size){
		this.name = name;
		this.value = value;
		this.representation = representation;
		this.size = size;
	}

}
