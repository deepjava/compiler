package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.strings.HString;

public class CPU extends ConfigElement {

	HString description;
	Constants sysConstants;
	MemoryMap memorymap;
	RegisterMap registermap;
	RegisterInitList reginit;
	Arch arch;
	
	public CPU(String jname) {
		this.name = HString.getRegisteredHString(jname);
		sysConstants = new Constants(this.name + " sysConstants", true);
		reginit = new RegisterInitList(this);
	}
	
	public void setDescription(String desc) {
		this.description = HString.getRegisteredHString(desc);
	}
	
	public void setArch(Arch arch) {
		this.arch = arch;
	}
	
	public void setMemoryMap(MemoryMap mm) {
		this.memorymap = mm;
	}
	
	@Deprecated
	public RegisterMap getRegisterMap() {
		return this.registermap;
	}
	
	public Register getRegisterByName(HString name) {
		Register r;
		// look for register in arch
		r = arch.getRegisterMap().getRegisterByName(name);
				
		// if not found, look for register in cpu
		if(r == null) {
			r = registermap.getRegisterByName(name);
		}
		return r;
	}
	
	public Register getRegisterByName(String jname) {
		return getRegisterByName(HString.getRegisteredHString(jname));
	}
	
	public Register[] getAllGPRs() {
		Register[] regs = new Register[getNofGPRs()];
		Register r;
		int c = 0;
		
		// 1. architecture specific registers
		r = arch.registermap.gpr;
		while(r != null & c < regs.length) {
			regs[c++] = r;
			r = (Register)r.next;
		}

		// 2. CPU specific registers
		r = registermap.gpr;
		while(r != null & c < regs.length) {
			regs[c++] = r;
			r = (Register)r.next;
		}	
		return regs;
	}
	
	public Register[] getAllFPRs() {
		Register[] regs = new Register[getNofFPRs()];
		Register r;
		int c = 0;
		
		// 1. architecture specific registers
		r = arch.registermap.fpr;
		while(r != null & c < regs.length) {
			regs[c++] = r;
			r = (Register)r.next;
		}

		// 2. CPU specific registers
		r = registermap.fpr;
		while(r != null & c < regs.length) {
			regs[c++] = r;
			r = (Register)r.next;
		}	
		return regs;
	}
	
	public Register[] getAllSPRs() {
		Register[] regs = new Register[getNofSPRs()];
		Register r;
		int c = 0;
		
		// 1. architecture specific registers
		r = arch.registermap.spr;
		while(r != null & c < regs.length) {
			regs[c++] = r;
			r = (Register)r.next;
		}

		// 2. CPU specific registers
		r = registermap.spr;
		while(r != null & c < regs.length) {
			regs[c++] = r;
			r = (Register)r.next;
		}	
		return regs;
	}
	
	public Register[] getAllIORs() {
		Register[] regs = new Register[getNofIORs()];
		Register r;
		int c = 0;
		
		// 1. architecture specific registers
		r = arch.registermap.ior;
		while(r != null & c < regs.length) {
			regs[c++] = r;
			r = (Register)r.next;
		}

		// 2. CPU specific registers
		r = registermap.ior;
		while(r != null & c < regs.length) {
			regs[c++] = r;
			r = (Register)r.next;
		}	
		return regs;
	}
	
	public int getNofGPRs() {
		return arch.registermap.getNofGprs() + registermap.getNofGprs();
	}
	
	public int getNofFPRs() {
		return arch.registermap.getNofFprs() + registermap.getNofFprs();
	}
	
	public int getNofSPRs() {
		return arch.registermap.getNofSprs() + registermap.getNofSprs();
	}
	
	public int getNofIORs() {
		return arch.registermap.getNofIors() + registermap.getNofIors();
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
