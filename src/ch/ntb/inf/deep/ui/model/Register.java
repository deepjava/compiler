package ch.ntb.inf.deep.ui.model;

public class Register {
	public int value;
	public String name;
	/** 
	 * Kind of Representations:	Binary = 0, Hexadecimal = 1, Decimal = 2,Double = 3
	 */
	public int representation;
	public Register(){	
	}
	public Register(String name, int value, int representation){
		this.name = name;
		this.value = value;
		this.representation = representation;
	}

}
