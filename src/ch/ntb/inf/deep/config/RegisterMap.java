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

	
	public Register getRegister(HString registername){
		Register r;
		
		r = (Register)spr.getElementByName(registername);
		if(r != null) return r;
		
		r = (Register)ior.getElementByName(registername);
		if(r != null) return r;
		
		r = (Register)fpscr.getElementByName(registername);
		if(r != null) return r;
		
		r = (Register)cr.getElementByName(registername);
		if(r != null) return r;
		
		r = (Register)msr.getElementByName(registername);
		if(r != null) return r;
		
		r = (Register)gpr.getElementByName(registername);
		if(r != null) return r;

		r = (Register)fpr.getElementByName(registername);
		if(r != null) return r;
		
		//register not found
		return null;
	}
	
	public Register getRegister(String jname) {
		return getRegister(HString.getRegisteredHString(jname));
	}

	private boolean addGprRegister(Register reg) {
		nofGprs++;
		if (gpr == null) {
			gpr = reg;
			return true;
		}
		if((Register)gpr.getElementByName(reg.name) != null) {
			return false;
		}
		gpr.append(reg);
		return true;
	}

	private boolean addFprRegister(Register reg) {
		nofFprs++;
		if (fpr == null) {
			fpr = reg;
			return true;
		}
		if((Register)fpr.getElementByName(reg.name) != null) {
			return false;
		}
		fpr.append(reg);
		return true;
	}

	private boolean addSprRegister(Register reg) {
		nofSprs++;
		if (spr == null) {
			spr = reg;
			return true;
		}
		if((Register)spr.getElementByName(reg.name) != null) {
			return false;
		}
		spr.append(reg);
		return true;
	}

	private boolean addIorRegister(Register reg) {
		nofIors++;
		if (ior == null) {
			ior = reg;
			return true;
		}
		if((Register)ior.getElementByName(reg.name) != null) {
			return false;
		}
		ior.append(reg);
		return true;
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

	public boolean addRegister(Register reg) {
		// TODO if a register init is set before all register was set we have to
		// check and merge
		if (reg.type == Parser.sGPR) {
			return addGprRegister(reg);
		}
		else if (reg.type == Parser.sFPR) {
			return addFprRegister(reg);
		}
		else if (reg.type == Parser.sSPR) {
			return addSprRegister(reg);
		}
		else if (reg.type == Parser.sIOR) {
			return addIorRegister(reg);
		}
		else if (reg.type == Parser.sMSR) {
			setMSRRegister(reg);
			return true;
		}
		else if (reg.type == Parser.sCR) {
			setCRRegister(reg);
			return true;
		}
		else if (reg.type == Parser.sFPSCR) {
			setFPSCRRegister(reg);
			return true;
		}
		else { // TODO improve error handling
			ErrorReporter.reporter.error(errInvalidType, "register " + reg.getName().toString());
			Parser.nOfErrors++;
			return false;
		}
	}

	public Register getGprRegisters() {
		return gpr;
	}

	public Register getFprRegisters() {
		return fpr;
	}

	public Register getSprRegisters() {
		return spr;
	}
	
	public Register getIorRegisters() {
		return ior;
	}
	
	public Register getMSR() {
		return msr;
	}
	
	public Register getCR() {
		return cr;
	}
	
	public Register getFpscr() {
		return fpscr;
	}
	
	public int getNofGprs() {
		return nofGprs;
	}

	public int getNofFprs() {
		return nofFprs;
	}
	
	public int getNofSprs() {
		return nofSprs;
	}
	
	public int getNofIors() {
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
			current = (Register)current.next;
		}
		fpscr.println(indentLevel + 1);
		current = fpr;
		while (current != null) {
			current.println(indentLevel + 1);
			current = (Register)current.next;
		}
		current = spr;
		while (current != null) {
			current.println(indentLevel + 1);
			current = (Register)current.next;
		}
		current = ior;
		while (current != null) {
			current.println(indentLevel + 1);
			current = (Register)current.next;
		}

		for (int i = indentLevel; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("}");
	}
}
