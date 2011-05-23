package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.host.StdStreams;

public class RegisterInit {
	
	public Register register;
	public int initValue;
	public RegisterInit next;
	
	public RegisterInit(Register register, int initValue){
		this.register = register;
		this.initValue = initValue;
	}
	
	
	public void print(int indentLevel){
		StdStreams.vrb.print(register.getName().toString() + String.format(" = 0x%08X", initValue));	
	}
	
	public void println(int indentLevel){
		while(indentLevel > 0){
			StdStreams.vrb.print("  ");
			indentLevel--;
		}
		StdStreams.vrb.println(register.getName().toString() + String.format(" = 0x%08X", initValue));	
	}
	
	@Override
	public String toString(){
		return register.getName().toString() + String.format(" = 0x%08X", initValue);
	}


}
