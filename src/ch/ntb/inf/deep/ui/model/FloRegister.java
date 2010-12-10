package ch.ntb.inf.deep.ui.model;

public class FloRegister extends Register {
	public long floatValue;
	
	public FloRegister() {	
	}

	public FloRegister(String name, long value, int representation) {
		this.name = name;
		this.floatValue = value;
		this.representation =representation;		
	}

}
