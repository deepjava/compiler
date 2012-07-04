package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.strings.HString;

public class CPU extends ConfigElement {

	HString description;
	Constants sysConstants;
	MemoryMap memorymap;
	RegisterMap registermap;
	RegisterInitList reginit;
	HString arch;
	
	public CPU(String jname) {
		this.name = HString.getRegisteredHString(jname);
		sysConstants = new Constants(this.name + " sysConstants", true);
		reginit = new RegisterInitList(this);
	}
	
	public void setDescription(String desc) {
		this.description = HString.getRegisteredHString(desc);
	}
	
	public void setArch(String arch) {
		this.arch = HString.getRegisteredHString(arch);
	}
	
	public void setMemoryMap(MemoryMap mm) {
		this.memorymap = mm;
	}
	
	public RegisterMap getRegisterMap() {
		return this.registermap;
	}
	
	public int getInternalMemoryBase() {
		return 0x0; // TODO improve this
	}
	
	public MemoryMap getMemoryMap() {
		return this.memorymap;
	}

	public Constants getSysConstants() {
		return sysConstants;
	}
}
