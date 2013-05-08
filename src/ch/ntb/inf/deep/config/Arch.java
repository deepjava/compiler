package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.strings.HString;

public class Arch extends Item {
	
	public Register regs;

	public Arch(String jname) {
		this.name = HString.getRegisteredHString(jname);
	}

	public int getNofGPRs() {
		int nof = 0;
		Register reg = regs;
		while (reg != null) {
			if (reg.regType == Parser.sGPR) nof++;
			reg = (Register) reg.next;
		}
		return nof;
	}
		
}
