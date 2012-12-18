package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.strings.HString;

public class Arch extends ConfigElement {
	
	RegisterMap registermap;

	public Arch(String jname) {
		this.name = HString.getRegisteredHString(jname);
	}
	
	public RegisterMap getRegisterMap() {
		return this.registermap;
	}
	
	public Register[] getAllRegisters() {
		return registermap.getAllRegisters();
	}
	
}
