package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.host.StdStreams;
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
		StdStreams.vrb.print(name.toString() + " = 0x" + Integer.toHexString(value));	
	}
	
	public void println(int indentLevel){
		while(indentLevel > 0){
			StdStreams.vrb.print("  ");
			indentLevel--;
		}
		StdStreams.vrb.println(name.toString() + " = 0x" + Integer.toHexString(value));	
	}

}
