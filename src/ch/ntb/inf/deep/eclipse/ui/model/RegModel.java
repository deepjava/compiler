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

package ch.ntb.inf.deep.eclipse.ui.model;

import ch.ntb.inf.deep.config.Board;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.launcher.Launcher;
import ch.ntb.inf.deep.target.TargetConnection;
import ch.ntb.inf.deep.target.TargetConnectionException;

public class RegModel  {

	private Register[] gprs; 
	private FPRegister[] fprs;
	private Register[] FPSCR;
	private Register[] userLevelSPR;
	private Register[] msr;
	private Register[] supervisorLevelSPR;
	private Register[] devSupportLevelSPR;
	private Register[] errorReg;
	
	private static RegModel model;
	
	private RegModel() {}
	
	public static RegModel getInstance() {
		if (model == null) {
			model = new RegModel();
		}
		return model;
	}
	
	public void creatDeSuSPRMod() {
		TargetConnection tc = Launcher.getTargetConnection();
		if (tc == null) {
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			devSupportLevelSPR = null;
			return;
		}
		devSupportLevelSPR = new Register[16];
		try {
			if (!tc.isConnected())	tc.openConnection();
			boolean wasFreezeAsserted = tc.getTargetState() == TargetConnection.stateDebug;
			if (!wasFreezeAsserted) tc.stopTarget();
			
			devSupportLevelSPR[0] = new Register("CMPA", (int)tc.getRegisterValue("CMPA"), 1);
			devSupportLevelSPR[1] = new Register("CMPB", (int)tc.getRegisterValue("CMPB"), 1);
			devSupportLevelSPR[2] = new Register("CMPC", (int)tc.getRegisterValue("CMPC"), 1);
			devSupportLevelSPR[3] = new Register("CMPD", (int)tc.getRegisterValue("CMPD"), 1);
			devSupportLevelSPR[4] = new Register("ECR", (int)tc.getRegisterValue("ECR"), 1);
			devSupportLevelSPR[5] = new Register("DER", (int)tc.getRegisterValue("DER"), 1);
			devSupportLevelSPR[6] = new Register("COUNTA", (int)tc.getRegisterValue("COUNTA"), 1);
			devSupportLevelSPR[7] = new Register("COUNTB", (int)tc.getRegisterValue("COUNTB"), 1);
			devSupportLevelSPR[8] = new Register("CMPE", (int)tc.getRegisterValue("CMPE"), 1);
			devSupportLevelSPR[9] = new Register("CMPF", (int)tc.getRegisterValue("CMPF"), 1);
			devSupportLevelSPR[10] = new Register("CMPG", (int)tc.getRegisterValue("CMPG"), 1);
			devSupportLevelSPR[11] = new Register("CMPH", (int)tc.getRegisterValue("CMPH"), 1);
			devSupportLevelSPR[12] = new Register("LCTRL1", (int)tc.getRegisterValue("LCTRL1"), 1);
			devSupportLevelSPR[13] = new Register("LCTRL2", (int)tc.getRegisterValue("LCTRL2"), 1);
			devSupportLevelSPR[14] = new Register("ICTRL", (int)tc.getRegisterValue("ICTRL"), 1);
			devSupportLevelSPR[15] = new Register("BAR", (int)tc.getRegisterValue("BAR"), 1);

			/*the mcdp-lib dosent support access to Development Port Data Register
			deSuSPR[16] = new Register("DPDR",bdi.getSPR(630),0);*/

			if (!wasFreezeAsserted)	tc.startTarget();
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			devSupportLevelSPR = null;
		} 
	}

	public void creatSuLeSPRMod() {
		TargetConnection tc = Launcher.getTargetConnection();
		if (tc == null) {
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			supervisorLevelSPR = null;
			return;
		}
		supervisorLevelSPR = new Register[11];
		try {
			if (!tc.isConnected()) tc.openConnection();
			boolean wasFreezeAsserted = tc.getTargetState() == TargetConnection.stateDebug;
			if (!wasFreezeAsserted) tc.stopTarget();

			supervisorLevelSPR[0] = new Register("DSISR", (int)tc.getRegisterValue("DSISR"), 1);
			supervisorLevelSPR[1]= new Register("DAR", (int)tc.getRegisterValue("DAR"), 1);
			supervisorLevelSPR[2]= new Register("DEC", (int)tc.getRegisterValue("DEC"), 1);
			supervisorLevelSPR[3]= new Register("SSR0", (int)tc.getRegisterValue("SSR0"), 1);
			supervisorLevelSPR[4]= new Register("SSR1", (int)tc.getRegisterValue("SSR1"), 1);
			
			/*results in a software emulation exception, see Users Manuel p.3-26
			suLeSPR[]= new Register("EIE",bdi.getSPR(80),0);
			suLeSPR[]= new Register("EID",bdi.getSPR(81),0);
			suLeSPR[]= new Register("NRI",bdi.getSPR(82),0);*/
			
			supervisorLevelSPR[5]= new Register("SPRG0", (int)tc.getRegisterValue("SPRG0"), 1);
			supervisorLevelSPR[6]= new Register("SPRG1", (int)tc.getRegisterValue("SPRG1"), 1);
			supervisorLevelSPR[7]= new Register("SPRG2", (int)tc.getRegisterValue("SPRG2"), 1);
			supervisorLevelSPR[8]= new Register("SPRG3", (int)tc.getRegisterValue("SPRG3"), 1);
			supervisorLevelSPR[9]= new Register("PVR", (int)tc.getRegisterValue("PVR"), 1);
			supervisorLevelSPR[10]= new Register("FPECR", (int)tc.getRegisterValue("FPECR"), 1);

			if (!wasFreezeAsserted)	tc.startTarget();
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			supervisorLevelSPR = null;
		} 
	}

	public void createMSRModel() {
		TargetConnection tc = Launcher.getTargetConnection();
		if (tc == null) {
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			msr = null;
			return;
		}
		msr = new Register[1];
		try {
			if (!tc.isConnected())	tc.openConnection();
			boolean wasFreezeAsserted = tc.getTargetState() == TargetConnection.stateDebug;
			if(!wasFreezeAsserted) tc.stopTarget();
			
			msr[0] = new Register("MSR", (int)tc.getRegisterValue("MSR"), 1);
			if (!wasFreezeAsserted)	tc.startTarget();
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			msr = null;
		}  
	}

	public void creatUsLeSPRMod() {
		TargetConnection tc = Launcher.getTargetConnection();
		if (tc == null) {
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			userLevelSPR = null;
			return;
		}
		userLevelSPR = new Register[6];
		try {
			if (!tc.isConnected()) tc.openConnection();
			boolean wasFreezeAsserted = tc.getTargetState() == TargetConnection.stateDebug;
			if(!wasFreezeAsserted) tc.stopTarget();

			userLevelSPR[0] = new Register("XER", (int)tc.getRegisterValue("XER"),1);
			userLevelSPR[1] = new Register("LR", (int)tc.getRegisterValue("LR"), 1);
			userLevelSPR[2] = new Register("CTR", (int)tc.getRegisterValue("CTR"), 1);
			userLevelSPR[3] = new Register("TBL", (int)tc.getRegisterValue("TBLread"), 1);
			userLevelSPR[4] = new Register("TBU", (int)tc.getRegisterValue("TBUread"), 1);
			userLevelSPR[5] = new Register("CR", (int)tc.getRegisterValue("CR"), 1);

			if(!wasFreezeAsserted) tc.startTarget();
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			userLevelSPR = null;
		} 

	}

	public void createFPRModel() {
		TargetConnection tc = Launcher.getTargetConnection();
		if (tc == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			fprs = null;
			return;
		}
		fprs = new FPRegister[32];
		FPSCR = new Register[1];
		try {
			if (!tc.isConnected()) tc.openConnection();
			boolean wasFreezeAsserted = tc.getTargetState() == TargetConnection.stateDebug;
			if (!wasFreezeAsserted) tc.stopTarget();
			for (int i = 0; i < 32; i++) {
				fprs[i] = new FPRegister("FPR"+i, tc.getRegisterValue("FPR" + i), 3);
			}
			FPSCR[0] = new Register("FPSCR", (int)tc.getRegisterValue("FPSCR"),1);
			
			if (!wasFreezeAsserted)	tc.startTarget();
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			fprs = null;
		} 
	}

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

		try {
			if (!tc.isConnected()) tc.openConnection();
			boolean wasFreezeAsserted = tc.getTargetState() == TargetConnection.stateDebug;
			if (!wasFreezeAsserted) tc.stopTarget();
			
			
			for (int i = 0; i < nof; i++) {
				gprs[i] = new Register("GPR"+i, (int) tc.getRegisterValue("R" + i), 1);
			}
			
			if (!wasFreezeAsserted) tc.startTarget();
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			gprs = null;
		} 
	}

	public void updateDeSuSPRMod() {
		if (devSupportLevelSPR == null){
			creatDeSuSPRMod();
			devSupportLevelSPR = null;
			return;
		}
		TargetConnection tc = Launcher.getTargetConnection();
		if (tc == null) {
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			return;
		}
		try {
			if (!tc.isConnected()) tc.openConnection();
			boolean wasFreezeAsserted = tc.getTargetState() == TargetConnection.stateDebug;
			if (!wasFreezeAsserted)	tc.stopTarget();

			devSupportLevelSPR[0].value = (int)tc.getRegisterValue("CMPA");
			devSupportLevelSPR[1].value = (int)tc.getRegisterValue("CMPB");
			devSupportLevelSPR[2].value = (int)tc.getRegisterValue("CMPC");
			devSupportLevelSPR[3].value = (int)tc.getRegisterValue("CMPD");
			devSupportLevelSPR[4].value = (int)tc.getRegisterValue("ECR");
			devSupportLevelSPR[5].value = (int)tc.getRegisterValue("DER");
			devSupportLevelSPR[6].value = (int)tc.getRegisterValue("COUNTA");
			devSupportLevelSPR[7].value = (int)tc.getRegisterValue("COUNTB");
			devSupportLevelSPR[8].value = (int)tc.getRegisterValue("CMPE");
			devSupportLevelSPR[9].value = (int)tc.getRegisterValue("CMPF");
			devSupportLevelSPR[10].value = (int)tc.getRegisterValue("CMPG");
			devSupportLevelSPR[11].value = (int)tc.getRegisterValue("CMPH");
			devSupportLevelSPR[12].value = (int)tc.getRegisterValue("LCTRL1");
			devSupportLevelSPR[13].value = (int)tc.getRegisterValue("LCTRL2");
			devSupportLevelSPR[14].value = (int)tc.getRegisterValue("ICTRL");
			devSupportLevelSPR[15].value = (int)tc.getRegisterValue("BAR");
			
			/*the mcdp-lib doesn't support access to the Development Port Data Register
			deSuSPR[16].value = bdi.getSPR(630);*/
			if (!wasFreezeAsserted)	tc.startTarget();
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			devSupportLevelSPR = null;
		} 
	}

	public void updateSuLeSPRMod() {
		if (supervisorLevelSPR == null) {
			creatSuLeSPRMod();
			return;
		}
		TargetConnection tc = Launcher.getTargetConnection();
		if (tc == null) {
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			supervisorLevelSPR = null;
			return;
		}
		try {
			if (!tc.isConnected()) tc.openConnection();
			boolean wasFreezeAsserted = tc.getTargetState() == TargetConnection.stateDebug;
			if (!wasFreezeAsserted)	tc.stopTarget();
			
			supervisorLevelSPR[0].value = (int)tc.getRegisterValue("DSISR");
			supervisorLevelSPR[1].value = (int)tc.getRegisterValue("DAR");
			supervisorLevelSPR[2].value = (int)tc.getRegisterValue("DEC");
			supervisorLevelSPR[3].value = (int)tc.getRegisterValue("SSR0");
			supervisorLevelSPR[4].value = (int)tc.getRegisterValue("SSR1");
			
			/*results in a software emulation exception, see Users Manuel p.3-26
			suLeSPR[].value = bdi.getSPR(80);
			suLeSPR[].value = bdi.getSPR(81);
			suLeSPR[].value = bdi.getSPR(82);*/
			
			supervisorLevelSPR[5].value  = (int)tc.getRegisterValue("SPRG0");
			supervisorLevelSPR[6].value  = (int)tc.getRegisterValue("SPRG1");
			supervisorLevelSPR[7].value  = (int)tc.getRegisterValue("SPRG2");
			supervisorLevelSPR[8].value  = (int)tc.getRegisterValue("SPRG3");
			supervisorLevelSPR[9].value  = (int)tc.getRegisterValue("PVR");
			supervisorLevelSPR[10].value  = (int)tc.getRegisterValue("FPECR");

			if (!wasFreezeAsserted)	tc.startTarget();
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			supervisorLevelSPR = null;
		} 
	}

	public void updateMSRModel() {
		if (msr == null){
			createMSRModel();
			return;
		}
		TargetConnection tc = Launcher.getTargetConnection();
		if (tc == null) {
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			msr = null;
			return;
		}
		try {
			if (!tc.isConnected()) tc.openConnection();
			boolean wasFreezeAsserted = tc.getTargetState() == TargetConnection.stateDebug;
			if (!wasFreezeAsserted) tc.stopTarget();
			
			msr[0].value  = (int)tc.getRegisterValue("MSR");

			if (!wasFreezeAsserted)	tc.startTarget();
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			msr = null;
		} 
	}

	public void updateUsLeSPRMod() {
		if (userLevelSPR == null) {
			creatUsLeSPRMod();
			return;
		}
		TargetConnection tc = Launcher.getTargetConnection();
		if (tc == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			userLevelSPR = null;
			return;
		}
		try {
			if (!tc.isConnected()) tc.openConnection();
			boolean wasFreezeAsserted = tc.getTargetState() == TargetConnection.stateDebug;
			if (!wasFreezeAsserted) tc.stopTarget();
			
			userLevelSPR[0].value  = (int)tc.getRegisterValue("XER");
			userLevelSPR[1].value  = (int)tc.getRegisterValue("LR");
			userLevelSPR[2].value  = (int)tc.getRegisterValue("CTR");
			userLevelSPR[3].value  = (int)tc.getRegisterValue("TBLread");
			userLevelSPR[4].value  = (int)tc.getRegisterValue("TBUread");
			userLevelSPR[5].value  = (int)tc.getRegisterValue("CR");

			if (!wasFreezeAsserted) tc.startTarget();
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			userLevelSPR = null;
		} 
	}

	public void updateFprMod() {
		if (fprs == null){
			createFPRModel();
			return;
		}
		TargetConnection tc = Launcher.getTargetConnection();
		if (tc == null) {
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			fprs = null;
			return;
		}
		try {
			if (!tc.isConnected()) tc.openConnection();
			boolean wasFreezeAsserted = tc.getTargetState() == TargetConnection.stateDebug;
			if (!wasFreezeAsserted)	tc.stopTarget();
			
			for (int i = 0; i < 32; i++) {
				fprs[i].floatValue = tc.getRegisterValue("FPR" + i);
			}
			FPSCR[0].value = (int)tc.getRegisterValue("FPSCR");
			
			if (!wasFreezeAsserted)	tc.startTarget();
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			fprs = null;
		} 
	}

	public void updateGPRModel() {
		if (gprs == null){
			createGPRModel();
			return;
		}
		TargetConnection tc = Launcher.getTargetConnection();
		if (tc == null) {
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			gprs = null;
			return;
		}
		Board b = Configuration.getBoard();
		if (b == null) return;
		int nof = b.cpu.arch.getNofGPRs();

		try {
			if (!tc.isConnected()) tc.openConnection();
			boolean wasFreezeAsserted = tc.getTargetState() == TargetConnection.stateDebug;
			if (!wasFreezeAsserted)	tc.stopTarget();
			
			for (int i = 0; i < nof; i++) {
				gprs[i].value = (int) tc.getRegisterValue("R" + i);
			}

			if (!wasFreezeAsserted)	tc.startTarget();
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			gprs = null;
		}
	}

	/**
	 * Returns the chosen Registermodel
	 * 
	 * @param RegType  GPR = 0, FPR = 1, UsLeSPR = 2, MaStReg = 3, SuLeSPR = 4, DeSuSPR = 5
	 * @return The Registers
	 */
	public Register[] getMod(int RegType) {
		// Check if Model exist, if not create
		switch (RegType) {
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
			Register[] floReg = new Register[33];
			for(int i = 0;i < 32; i++){
				floReg[i]=fprs[i];
			}
			floReg[32] = FPSCR[0];
			return floReg;
		case 2:
			if (userLevelSPR == null) {
				creatUsLeSPRMod();
				if(userLevelSPR == null) return errorReg;
			}
			return userLevelSPR;
		case 3:
			if (msr == null) {
				createMSRModel();
				if(msr == null) return errorReg;
			}
			return msr;
		case 4:
			if (supervisorLevelSPR == null) {
				creatSuLeSPRMod();
				if(supervisorLevelSPR == null) return errorReg;
			}
			return supervisorLevelSPR;
		case 5:
			if (devSupportLevelSPR == null) {
				creatDeSuSPRMod();
				if(devSupportLevelSPR == null) return errorReg;
			}
			return devSupportLevelSPR;
		default:
			return null;
		}
	}
	
	/**
	 * Returns the chosen register model
	 * 
	 * @param RegType  GPR = 0, FPR = 1, UsLeSPR = 2, MaStReg = 3, SuLeSPR = 4, DeSuSPR = 5
	 * @return The Registers
	 */
	public void clearMod(int RegType) {
		// Check if Model exist, if not create
		switch (RegType) {
		case 0:
			gprs = null;
			break;
		case 1:
			fprs = null;
			FPSCR = null;
			break;
		case 2:
			userLevelSPR = null;
			break;
		case 3:
			msr = null;
			break;
		case 4:
			supervisorLevelSPR = null;
			break;
		case 5:
			devSupportLevelSPR = null;
			break;
		default:
			return;
		}
	}
	
//	private int[] readGPRs(TargetConnection tc) throws TargetConnectionException {
//		int nofGprs = 32;
//		int gprs[] = new int[nofGprs];
//		for(int i = 0; i < nofGprs; i++) {
//			gprs[i] = (int)tc.getRegisterValue("R" + i);
//		}
//		return gprs;
//	}
}
