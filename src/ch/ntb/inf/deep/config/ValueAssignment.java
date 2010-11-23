package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.strings.HString;

public class ValueAssignment {
	
	HString name;
	int value;
	public ValueAssignment next;
	
	public ValueAssignment(HString name, int value){
		this.name = name;
		this.value = value;
	}
	
	public HString getName(){
		return name;
	}
	
	public int getValue(){
		return value;
	}
	
	public void setValue(int value){
		this.value = value;
	}
	
	public void print(int indentLevel){
		System.out.print(name.toString() + " = 0x" + Integer.toHexString(value));	
	}
	
	public void println(int indentLevel){
		while(indentLevel > 0){
			System.out.print("  ");
			indentLevel--;
		}
		System.out.println(name.toString() + " = 0x" + Integer.toHexString(value));	
	}

}
