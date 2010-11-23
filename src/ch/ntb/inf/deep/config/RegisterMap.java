package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.host.ErrorReporter;

public class RegisterMap implements ErrorCodes {
	private static RegisterMap regMap;
	Register gpr;
	Register fpr;
	Register spr;

	private RegisterMap() {
	}

	public static RegisterMap getInstance() {
		if (regMap == null) {
			regMap = new RegisterMap();
		}
		return regMap;
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

	public void addRegister(Register reg) {
		if (reg.type == Parser.sGPR) {
			addGprRegister(reg);
		} else if (reg.type == Parser.sFPR) {
			addFprRegister(reg);
		} else if (reg.type == Parser.sSPR) {
			addSprRegister(reg);
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
		System.out.println("RegisterMap {");
		Register current = gpr;
		while(current != null){
			current.println(indentLevel + 1);
			current = current.next;
		}
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
		
		for(int i = indentLevel; i > 0; i--){
			System.out.print("  ");
		}
		System.out.println("}");
	}

}
