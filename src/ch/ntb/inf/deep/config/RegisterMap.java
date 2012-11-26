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

	
	protected Register getRegisterByName(HString registername){
		Register r;
		
		if(spr != null) {
			r = (Register)spr.getElementByName(registername);
			if(r != null) return r;
		}
		
		if(ior != null) {
			r = (Register)ior.getElementByName(registername);
			if(r != null) return r;
		}
		
		if(fpscr != null) {
			r = (Register)fpscr.getElementByName(registername);
			if(r != null) return r;
		}
		
		if(cr != null) {
			r = (Register)cr.getElementByName(registername);
			if(r != null) return r;
		}
		
		if(msr != null) {
			r = (Register)msr.getElementByName(registername);
			if(r != null) return r;
		}
		
		if(gpr != null) {
			r = (Register)gpr.getElementByName(registername);
			if(r != null) return r;
		}

		if(fpr != null) {
			r = (Register)fpr.getElementByName(registername);
			if(r != null) return r;
		}
		
		//register not found
		return null;
	}
	
	protected Register getRegisterByName(String jname) {
		return getRegisterByName(HString.getRegisteredHString(jname));
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

	protected boolean addRegister(Register reg) {
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

	protected Register getGprRegisters() {
		return gpr;
	}

	protected Register getFprRegisters() {
		return fpr;
	}

	protected Register getSprRegisters() {
		return spr;
	}
	
	protected Register getIorRegisters() {
		return ior;
	}
	
	protected Register getMSR() {
		return msr;
	}
	
	protected Register getCR() {
		return cr;
	}
	
	protected Register getFpscr() {
		return fpscr;
	}
	
	protected int getNofGprs() {
		return nofGprs;
	}

	protected int getNofFprs() {
		return nofFprs;
	}
	
	protected int getNofSprs() {
		return nofSprs;
	}
	
	protected int getNofIors() {
		return nofIors;
	}
}
