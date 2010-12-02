package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.strings.HString;

public class RegisterMap implements ErrorCodes {
	private static RegisterMap regMap;
	Register regWithInitalValue;
	Register gpr;
	Register fpr;
	Register spr;
	Register ior;
	Register msr;
	Register cr;
	Register fpscr;

	private RegisterMap() {
	}

	public static RegisterMap getInstance() {
		if (regMap == null) {
			regMap = new RegisterMap();
		}
		return regMap;
	}
	public void addInitValueFor(HString registername, ValueAssignment init){
		//search register
		Register current = spr;
		while(current != null){
			if(current.name.equals(registername)){
				current.setInit(init);
				current.nextWithInitValue = regWithInitalValue;
				regWithInitalValue = current;
				return;
			}
			current = current.next;
		}
		current = ior;
		while(current != null){
			if(current.name.equals(registername)){
				current.setInit(init);
				current.nextWithInitValue = regWithInitalValue;
				regWithInitalValue = current;
				return;
			}
			current = current.next;
		}
		current = gpr;
		while(current != null){
			if(current.name.equals(registername)){
				current.setInit(init);
				current.nextWithInitValue = regWithInitalValue;
				regWithInitalValue = current;
				return;
			}
			current = current.next;
		}
		current = fpr;
		while(current != null){
			if(current.name.equals(registername)){
				current.setInit(init);
				current.nextWithInitValue = regWithInitalValue;
				regWithInitalValue = current;
				return;
			}
			current = current.next;
		}
		current = fpscr;
		while(current != null){
			if(current.name.equals(registername)){
				current.setInit(init);
				current.nextWithInitValue = regWithInitalValue;
				regWithInitalValue = current;
				return;
			}
			current = current.next;
		}
		current = cr;
		while(current != null){
			if(current.name.equals(registername)){
				current.setInit(init);
				current.nextWithInitValue = regWithInitalValue;
				regWithInitalValue = current;
				return;
			}
			current = current.next;
		}
		current = msr;
		while(current != null){
			if(current.name.equals(registername)){
				current.setInit(init);
				current.nextWithInitValue = regWithInitalValue;
				regWithInitalValue = current;
				return;
			}
			current = current.next;
		}
		//if Register doesn't exist yet create one;
		Register reg = new Register(registername);
		reg.setInit(init);
		reg.nextWithInitValue = regWithInitalValue;
		regWithInitalValue = reg;
	}

	private void addGprRegister(Register reg) {
		if (gpr == null) {
			gpr = reg;
			return;
		}
		Register current = gpr;
		Register prev = null;
		int regHash = reg.name.hashCode();
		while (current != null) {
			if (current.name.hashCode() == regHash) {
				if (current.name.equals(reg.name)) {
					reg.next = current.next;
					if (prev != null) {
						prev.next = reg;
					} else {
						gpr = reg;
					}
					return;
				}
			}
			prev = current;
			current = current.next;
		}
		// if no match prev shows the tail of the list
		prev.next = reg;
	}

	private void addFprRegister(Register reg) {
		if (fpr == null) {
			fpr = reg;
			return;
		}
		Register current = fpr;
		Register prev = null;
		int regHash = reg.name.hashCode();
		while (current != null) {
			if (current.name.hashCode() == regHash) {
				if (current.name.equals(reg.name)) {
					reg.next = current.next;
					if (prev != null) {
						prev.next = reg;
					} else {
						fpr = reg;
					}
					return;
				}
			}
			prev = current;
			current = current.next;
		}
		// if no match prev shows the tail of the list
		prev.next = reg;
	}

	private void addSprRegister(Register reg) {
		if (spr == null) {
			spr = reg;
			return;
		}
		Register current = spr;
		Register prev = null;
		int regHash = reg.name.hashCode();
		while (current != null) {
			if (current.name.hashCode() == regHash) {
				if (current.name.equals(reg.name)) {
					reg.next = current.next;
					if (prev != null) {
						prev.next = reg;
					} else {
						spr = reg;
					}
					return;
				}
			}
			prev = current;
			current = current.next;
		}
		// if no match prev shows the tail of the list
		prev.next = reg;
	}
	
	private void addIorRegister(Register reg) {
		if (ior == null) {
			ior = reg;
			return;
		}
		Register current = ior;
		Register prev = null;
		int regHash = reg.name.hashCode();
		while (current != null) {
			if (current.name.hashCode() == regHash) {
				if (current.name.equals(reg.name)) {
					reg.next = current.next;
					if (prev != null) {
						prev.next = reg;
					} else {
						ior = reg;
					}
					return;
				}
			}
			prev = current;
			current = current.next;
		}
		// if no match prev shows the tail of the list
		prev.next = reg;
	}
	private void setMSRRegister(Register reg){
		msr = reg;
	}
	private void setCRRegister(Register reg){
		cr = reg;
	}
	private void setFPSCRRegister(Register reg){
		fpscr = reg;
	}

	public void addRegister(Register reg) {
		//if a register init is set before all register was set we have to check and merge
		if(regWithInitalValue != null){
			Register current = regWithInitalValue;
			while(current != null){
				if(current.name.equals(reg.name)){
					//copy content of reg into current
					current.name = reg.name;
					current.next = reg.next;
					current.type = reg.type;
					current.addr = reg.addr;
					current.size = reg.size;
					current.repr = reg.repr;
					
					//replace reg with current;
					reg = current;
					break;
				}
			}
		}		
		if (reg.type == Parser.sGPR) {
			addGprRegister(reg);
		} else if (reg.type == Parser.sFPR) {
			addFprRegister(reg);
		} else if (reg.type == Parser.sSPR) {
			addSprRegister(reg);
		} else if (reg.type == Parser.sIOR) {
			addIorRegister(reg);
		} else if (reg.type == Parser.sMSR) {
			setMSRRegister(reg);
		} else if (reg.type == Parser.sCR) {
			setCRRegister(reg);
		} else if (reg.type == Parser.sFPSCR) {
			setFPSCRRegister(reg);
		} else {
			ErrorReporter.reporter.error(errInvalidType,
					"Invalide register type in register "
							+ reg.getName().toString() + "\n");
			return;
		}
	}
	
	public Register getGprRegister(){
		return gpr;
	}
	
	public Register getFprRegister(){
		return fpr;
	}
	
	public Register getSprRegister(){
		return spr;
	}
	
	public void println(int indentLevel){
		for(int i = indentLevel; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("registermap {");
		
		msr.println(indentLevel + 1);
		
		cr.println(indentLevel + 1);
				
		Register current = gpr;
		while(current != null){
			current.println(indentLevel + 1);
			current = current.next;
		}
		fpscr.println(indentLevel + 1);
		current = fpr;
		while(current != null){
			current.println(indentLevel + 1);
			current = current.next;
		}
		current = spr;
		while(current != null){
			current.println(indentLevel + 1);
			current = current.next;
		}
		current = ior;
		while(current != null){
			current.println(indentLevel + 1);
			current = current.next;
		}
				
		for(int i = indentLevel; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("}");
	}

	public static void clear() {
		regMap = null;		
	}

}
