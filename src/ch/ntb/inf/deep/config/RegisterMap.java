/*
 * Copyright (c) 2011 NTB Interstate University of Applied Sciences of Technology Buchs.
 *
 * http://www.ntb.ch/inf
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Eclipse Public License for more details.
 * 
 * Contributors:
 *     NTB - initial implementation
 * 
 */

package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class RegisterMap implements ErrorCodes {
	private static RegisterMap regMap;
	int nofGprs = 0;
	Register gpr;
	int nofFprs = 0;
	Register fpr;
	int nofSprs = 0;
	Register spr;
	int nofIors = 0;
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
	
	public Register getRegister(HString registername){
		// search register
		Register current = spr;
		while (current != null) {
			if (current.name.equals(registername)) {
				return current;
			}
			current = current.next;
		}
		current = ior;
		while (current != null) {
			if (current.name.equals(registername)) {
				if (current.name.equals(registername)) {
					return current;
				}
			}
			current = current.next;
		}
		current = fpscr;
		while (current != null) {
			if (current.name.equals(registername)) {
				if (current.name.equals(registername)) {
					return current;
				}
			}
			current = current.next;
		}
		current = cr;
		while (current != null) {
			if (current.name.equals(registername)) {
				if (current.name.equals(registername)) {
					return current;
				}
			}
			current = current.next;
		}
		current = msr;
		while (current != null) {
			if (current.name.equals(registername)) {
				if (current.name.equals(registername)) {
					return current;
				}
			}
			current = current.next;
		}
		current = gpr;
		while (current != null) {
			if (current.name.equals(registername)) {
				ErrorReporter.reporter.error(errInitNotSupported, "for gpr register " + registername);
				Parser.nOfErrors++;
				return null;
			}
			current = current.next;
		}
		current = fpr;
		while (current != null) {
			if (current.name.equals(registername)) {
				ErrorReporter.reporter.error(errInitNotSupported, "for fpr register " + registername);
				Parser.nOfErrors++;
				return null;
			}
			current = current.next;
		}
		//register not found
		ErrorReporter.reporter.error(errNoSuchRegister, registername.toString());
		Parser.nOfErrors++;
		return null;
		
		
	}

//	public void addInitValueFor(HString registername, ValueAssignment init) {
//		// search register
//		Register current = spr;
//		while (current != null) {
//			if (current.name.equals(registername)) {
//				if (current.getInit() != null) {
//					current.setInit(init);
//					return;
//				}
//				current.setInit(init);
//				current.nextWithInitValue = regWithInitalValue;
//				regWithInitalValue = current;
//				return;
//			}
//			current = current.next;
//		}
//		current = ior;
//		while (current != null) {
//			if (current.name.equals(registername)) {
//				if (current.getInit() != null) {
//					current.setInit(init);
//					return;
//				}
//				current.setInit(init);
//				current.nextWithInitValue = regWithInitalValue;
//				regWithInitalValue = current;
//				return;
//			}
//			current = current.next;
//		}
//		current = fpscr;
//		while (current != null) {
//			if (current.name.equals(registername)) {
//				if (current.getInit() != null) {
//					current.setInit(init);
//					return;
//				}
//				current.setInit(init);
//				current.nextWithInitValue = regWithInitalValue;
//				regWithInitalValue = current;
//				return;
//			}
//			current = current.next;
//		}
//		current = cr;
//		while (current != null) {
//			if (current.name.equals(registername)) {
//				if (current.getInit() != null) {
//					current.setInit(init);
//					return;
//				}
//				current.setInit(init);
//				current.nextWithInitValue = regWithInitalValue;
//				regWithInitalValue = current;
//				return;
//			}
//			current = current.next;
//		}
//		current = msr;
//		while (current != null) {
//			if (current.name.equals(registername)) {
//				if (current.getInit() != null) {
//					current.setInit(init);
//					return;
//				}
//				current.setInit(init);
//				current.nextWithInitValue = regWithInitalValue;
//				regWithInitalValue = current;
//				return;
//			}
//			current = current.next;
//		}
//		current = gpr;
//		while (current != null) {
//			if (current.name.equals(registername)) {
//				ErrorReporter.reporter
//						.error("it is not allowed to set a init value for a gpr register");
//			}
//			current = current.next;
//		}
//		current = fpr;
//		while (current != null) {
//			if (current.name.equals(registername)) {
//				ErrorReporter.reporter
//						.error("it is not allowed to set a init value for a fpr register");
//			}
//			current = current.next;
//		}
//		// if Register doesn't exist yet create one;
//		Register reg = new Register(registername);
//		reg.setInit(init);
//		reg.nextWithInitValue = regWithInitalValue;
//		regWithInitalValue = reg;
//	}

	private void addGprRegister(Register reg) {
		nofGprs++;
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
		nofFprs++;
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
		nofSprs++;
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
		nofIors++;
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

	private void setMSRRegister(Register reg) {
		msr = reg;
	}

	private void setCRRegister(Register reg) {
		cr = reg;
	}

	private void setFPSCRRegister(Register reg) {
		fpscr = reg;
	}

	public void addRegister(Register reg) {
		// if a register init is set before all register was set we have to
		// check and merge
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
			ErrorReporter.reporter.error(errInvalidType, "register " + reg.getName().toString());
			Parser.nOfErrors++;
			return;
		}
	}

	public Register getGprRegister() {
		return gpr;
	}

	public Register getFprRegister() {
		return fpr;
	}

	public Register getSprRegister() {
		return spr;
	}
	
	public Register getIorRegister(){
		return ior;
	}
	
	public Register getMSR(){
		return msr;
	}
	
	public Register getCR(){
		return cr;
	}
	
	public Register getFpscr(){
		return fpscr;
	}
	
	public int getNofGprs(){
		return nofGprs;
	}

	public int getNofFprs(){
		return nofFprs;
	}
	
	public int getNofSprs(){
		return nofSprs;
	}
	
	public int getNofIors(){
		return nofIors;
	}
	public void println(int indentLevel) {
		for (int i = indentLevel; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("registermap {");

		msr.println(indentLevel + 1);

		cr.println(indentLevel + 1);

		Register current = gpr;
		while (current != null) {
			current.println(indentLevel + 1);
			current = current.next;
		}
		fpscr.println(indentLevel + 1);
		current = fpr;
		while (current != null) {
			current.println(indentLevel + 1);
			current = current.next;
		}
		current = spr;
		while (current != null) {
			current.println(indentLevel + 1);
			current = current.next;
		}
		current = ior;
		while (current != null) {
			current.println(indentLevel + 1);
			current = current.next;
		}

		for (int i = indentLevel; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("}");
	}

	public static void clear() {
		regMap = null;
	}

}
