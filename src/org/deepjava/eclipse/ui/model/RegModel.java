/*
 * Copyright 2011 - 2013 NTB University of Applied Sciences in Technology
 * Buchs, Switzerland, http://www.ntb.ch/inf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.deepjava.eclipse.ui.model;

import org.deepjava.config.Board;
import org.deepjava.config.CPU;
import org.deepjava.config.Configuration;
import org.deepjava.config.Parser;
import org.deepjava.launcher.Launcher;
import org.deepjava.strings.HString;
import org.deepjava.target.TargetConnection;
import org.deepjava.target.TargetConnectionException;

public class RegModel  {

	private Register[] gprs; 
	private FPRegister[] fprs;
	private int nofSinglePrecRegs, nofDoublePrecRegs;
	private Register[] fpscr;
	private Register[] msr;
	private Register[] sprs;
	private Register[] errorReg;
	
	private static RegModel model;
	
	private RegModel() {}
	
	public static RegModel getInstance() {
		if (model == null) {
			model = new RegModel();
		}
		return model;
	}
	
	/**
	 * Create instances for all defined GPRs and get their values from the target
	 */
	public void createGPRModel() {
		TargetConnection tc = Launcher.getTargetConnection();
		if (tc == null) {
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			gprs = null;
			return;
		}
		Board b = Configuration.getBoard();
		if (b == null) return;
		int nof = b.cpu.arch.getNofGPRs();
		gprs = new Register[nof];
		org.deepjava.config.Register regs = b.cpu.arch.regs;
		int i = 0;
		while (regs != null) {
			if (regs != null && regs.regType == Parser.sGPR) {
				String name = regs.name.toString();
				gprs[i++] = new Register(name, 0, 1);
			}
			regs = (org.deepjava.config.Register) regs.next;
		}
		updateGPRModel();
	}

	public void createFPRModel() {
		TargetConnection tc = Launcher.getTargetConnection();
		if (tc == null) {
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			fprs = null;
			return;
		}
		Board b = Configuration.getBoard();
		if (b == null) return;
		int nof = b.cpu.arch.getNofFPRs();
		boolean arm = b.cpu.arch.name.equals(HString.getHString("arm32"));
		fprs = new FPRegister[nof];
		nofSinglePrecRegs = 0;
		nofDoublePrecRegs = 0;
		fpscr = new Register[1];
		org.deepjava.config.Register regs = b.cpu.arch.regs;
		int i = 0;
		while (regs != null) {
			if (regs != null && regs.regType == Parser.sFPR) {
				String name = regs.name.toString();
				fprs[i++] = new FPRegister(name, 0, 3);
				if (arm && (regs.name.charAt(0) == 'S')) nofSinglePrecRegs++;
				else nofDoublePrecRegs++;
			}
			regs = (org.deepjava.config.Register) regs.next;
		}
		fpscr[0] = new Register("FPSCR", 0, 1);
		updateFPRModel();
	}

	public void createSPRModel() {
		TargetConnection tc = Launcher.getTargetConnection();
		if (tc == null) {
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			sprs = null;
			return;
		}
		Board b = Configuration.getBoard();
		if (b == null) return;
		CPU cpu = b.cpu;
		int nof = cpu.getNofSPRs();
		sprs = new Register[nof];
		msr = new Register[1];
		org.deepjava.config.Register reg = b.cpu.regs;
		int i = 0;
		while (reg != null) {
			if (reg != null && reg.regType == Parser.sSPR) {
				String name = reg.name.toString();
				sprs[i++] = new Register(name, 0, 1);
			}
			reg = (org.deepjava.config.Register) reg.next;
		}
		msr[0] = new Register("MSR", 0, 1);
		updateSPRModel();
	}

	/**
	 * Get the values of all GPRs from the target
	 */
	public void updateGPRModel() {
		if (gprs == null) createGPRModel();
		
		TargetConnection tc = Launcher.getTargetConnection();
		if (tc == null) {
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			gprs = null;
			return;
		}
		try {
			if (!tc.isConnected()) tc.openConnection();
			boolean wasFreezeAsserted = tc.getTargetState() == TargetConnection.stateDebug;
			if (!wasFreezeAsserted)	tc.stopTarget();
			
			for (int i = 0; i < gprs.length; i++) {
				gprs[i].value = (int) tc.getRegisterValue(gprs[i].name);
			}

			if (!wasFreezeAsserted)	tc.startTarget(-1);
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			gprs = null;
		}
	}

	/**
	 * Get the values of all FPRs and the FPSCR from the target
	 */
	public void updateFPRModel() {
		if (fprs == null) createFPRModel();
		
		TargetConnection tc = Launcher.getTargetConnection();
		if (tc == null) {
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			fprs = null;
			return;
		}
		Board b = Configuration.getBoard();
		if (b == null) return;
		boolean arm = b.cpu.arch.name.equals(HString.getHString("arm32"));
		try {
			if (!tc.isConnected()) tc.openConnection();
			boolean wasFreezeAsserted = tc.getTargetState() == TargetConnection.stateDebug;
			if (!wasFreezeAsserted)	tc.stopTarget();
			for (int i = 0; i < nofDoublePrecRegs / 2; i++) {
				long val = tc.getRegisterValue(fprs[i].name); 
				fprs[i].floatValue = val;
				if (arm) {
					fprs[nofDoublePrecRegs+2*i].floatValue = val & 0xffffffffL;
					fprs[nofDoublePrecRegs+2*i+1].floatValue = (val >> 32) & 0xffffffffL;
				}
			}
			for (int i = nofDoublePrecRegs / 2; i < nofDoublePrecRegs; i++) {
				long val = tc.getRegisterValue(fprs[i].name); 
				fprs[i].floatValue = val;
			}
			fpscr[0].value = (int)tc.getRegisterValue(fpscr[0].name);
			
			if (!wasFreezeAsserted)	tc.startTarget(-1);
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			fprs = null;
		} 
	}

	/**
	 * Get the values of all GPRs from the target
	 */
	public void updateSPRModel() {
		if (sprs == null) createSPRModel();
		
		TargetConnection tc = Launcher.getTargetConnection();
		if (tc == null) {
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			sprs = null;
			return;
		}
		try {
			if (!tc.isConnected()) tc.openConnection();
			boolean wasFreezeAsserted = tc.getTargetState() == TargetConnection.stateDebug;
			if (!wasFreezeAsserted)	tc.stopTarget();
			
			if (sprs == null) System.out.println("sprs is null");
			for (int i = 0; i < sprs.length; i++) {
				sprs[i].value = (int) tc.getRegisterValue(sprs[i].name);
			}
			msr[0].value = (int)tc.getRegisterValue(msr[0].name);

			if (!wasFreezeAsserted)	tc.startTarget(-1);
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			sprs = null;
		}
	}

	/**
	 * Returns the chosen register model, if model does not yet exist -> create
	 * 
	 * @param type  GPR = 0, FPR+FPSCR = 1, SPR+MSR = 2
	 * @return The registers
	 */
	public Register[] getMod(int type) {
		switch (type) {
		case 0:
			if (gprs == null) {
				createGPRModel();
				if (gprs == null) return errorReg;
			}
			return gprs;			
		case 1:
			if (fprs == null) {
				createFPRModel();
				if(fprs == null) return errorReg;
			}
			Register[] regs = new Register[fprs.length + fpscr.length];
			for(int i = 0; i < fprs.length; i++){
				regs[i] = fprs[i];
			}
			for(int i = 0; i < fpscr.length; i++){
				regs[fprs.length + i] = fpscr[i];
			}
			return regs;
		case 2:
			if (sprs == null) {
				createSPRModel();
				if(sprs == null) return errorReg;
			}
			regs = new Register[sprs.length + msr.length];
			for(int i = 0; i < msr.length; i++){
				regs[i] = msr[i];
			}
			for(int i = 0; i < sprs.length; i++){
				regs[msr.length + i] = sprs[i];
			}
			return regs;
		default:
			return null;
		}
	}
	
	/**
	 * Clears the chosen register model
	 * 
	 * @param type  GPR = 0, FPR = 1, SPR = 2
	 */
	public void clearMod(int type) {
		switch (type) {
		case 0:
			gprs = null;
			break;
		case 1:
			fprs = null;
			fpscr = null;
			break;
		case 2:
			System.out.println("clear model");
			sprs = null;
			msr = null;
			break;
		default:
			return;
		}
	}

}
