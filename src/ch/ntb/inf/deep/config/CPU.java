package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.strings.HString;

public class CPU extends Item {

	HString description;
	public SystemConstant sysConstants;
	public MemMap memorymap;
	public Register regs;
	public RegisterInit regInits;
	public Arch arch;
	
	public CPU(String jname) {
		this.name = HString.getRegisteredHString(jname);
	}
	
}
