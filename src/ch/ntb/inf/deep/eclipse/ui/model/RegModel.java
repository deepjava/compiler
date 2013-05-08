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

package ch.ntb.inf.deep.eclipse.ui.model;

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
	private boolean wasFreezeAsserted;
	
	private static RegModel model;
	
	private RegModel(){
	}
//	private Downloader getLoader(){
//		return module;
//	}
//	private void checkAndUpdateLoader(){
//		if(model.getLoader() == null){
//			model.setloader(UsbMpc555Loader.getInstance());
//		}
//	}
	
	public static RegModel getInstance(){
		if(model == null){
//			Downloader loader = UsbMpc555Loader.getInstance();
			model = new RegModel();
//			model.setloader(loader);
		}
//		model.checkAndUpdateLoader();
		return model;
	}
//	public void setloader(Downloader bdi){
//		module = bdi;
//	}
	
	public void creatDeSuSPRMod() {
		TargetConnection bdi = Launcher.getTargetConnection();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			devSupportLevelSPR = null;
			return;
		}
		devSupportLevelSPR = new Register[16];
		try {
			if(!bdi.isConnected()){
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			
			devSupportLevelSPR[0] = new Register("CMPA",bdi.getSprValue(144),1);
			devSupportLevelSPR[1] = new Register("CMPB",bdi.getSprValue(145),1);
			devSupportLevelSPR[2] = new Register("CMPC",bdi.getSprValue(146),1);
			devSupportLevelSPR[3] = new Register("CMPD",bdi.getSprValue(147),1);
			devSupportLevelSPR[4] = new Register("ECR",bdi.getSprValue(148),1);
			devSupportLevelSPR[5] = new Register("DER",bdi.getSprValue(149),1);
			devSupportLevelSPR[6] = new Register("COUNTA",bdi.getSprValue(150),1);
			devSupportLevelSPR[7] = new Register("COUNTB",bdi.getSprValue(151),1);
			devSupportLevelSPR[8] = new Register("CMPE",bdi.getSprValue(152),1);
			devSupportLevelSPR[9] = new Register("CMPF",bdi.getSprValue(153),1);
			devSupportLevelSPR[10] = new Register("CMPG",bdi.getSprValue(154),1);
			devSupportLevelSPR[11] = new Register("CMPH",bdi.getSprValue(155),1);
			devSupportLevelSPR[12] = new Register("LCTRL1",bdi.getSprValue(156),1);
			devSupportLevelSPR[13] = new Register("LCTRL2",bdi.getSprValue(157),1);
			devSupportLevelSPR[14] = new Register("ICTRL",bdi.getSprValue(158),1);
			devSupportLevelSPR[15] = new Register("BAR",bdi.getSprValue(159),1);

			/*the mcdp-lib dosent support access to Development Port Data Register
			deSuSPR[16] = new Register("DPDR",bdi.getSPR(630),0);*/

			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			devSupportLevelSPR = null;
		} 
	}

	public void creatSuLeSPRMod() {
		TargetConnection bdi = Launcher.getTargetConnection();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			supervisorLevelSPR = null;
			return;
		}
		supervisorLevelSPR = new Register[11];
		try {
			if(!bdi.isConnected()){
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			supervisorLevelSPR[0] = new Register("DSISR",bdi.getSprValue(18),1);
			supervisorLevelSPR[1]= new Register("DAR",bdi.getSprValue(19),1);
			supervisorLevelSPR[2]= new Register("DEC",bdi.getSprValue(22),1);
			supervisorLevelSPR[3]= new Register("SSR0",bdi.getSprValue(26),1);
			supervisorLevelSPR[4]= new Register("SSR1",bdi.getSprValue(27),1);
			
			/*results a software emulation exception, see Users Manuel p.3-26
			suLeSPR[]= new Register("EIE",bdi.getSPR(80),0);
			suLeSPR[]= new Register("EID",bdi.getSPR(81),0);
			suLeSPR[]= new Register("NRI",bdi.getSPR(82),0);*/
			
			supervisorLevelSPR[5]= new Register("SPRG0",bdi.getSprValue(272),1);
			supervisorLevelSPR[6]= new Register("SPRG1",bdi.getSprValue(273),1);
			supervisorLevelSPR[7]= new Register("SPRG2",bdi.getSprValue(274),1);
			supervisorLevelSPR[8]= new Register("SPRG3",bdi.getSprValue(275),1);
			supervisorLevelSPR[9]= new Register("PVR",bdi.getSprValue(287),1);
			supervisorLevelSPR[10]= new Register("FPECR",bdi.getSprValue(1022),1);

			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			supervisorLevelSPR = null;
		} 
	}

	public void createMSRModel() {
		TargetConnection bdi = Launcher.getTargetConnection();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			msr = null;
			return;
		}
		msr = new Register[1];
		try {
			if(!bdi.isConnected()){
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			
			msr[0] = new Register("MSR", bdi.getRegisterValue("MSR"), 1);
			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			msr = null;
		}  

	}

	public void creatUsLeSPRMod() {
		TargetConnection bdi = Launcher.getTargetConnection();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			userLevelSPR = null;
			return;
		}
		userLevelSPR = new Register[6];
		try {
			if(!bdi.isConnected()){
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			userLevelSPR[0] = new Register("XER",bdi.getSprValue(1),1);
			userLevelSPR[1] = new Register("LR",bdi.getSprValue(8),1);
			userLevelSPR[2] = new Register("CTR",bdi.getSprValue(9),1);
			userLevelSPR[3] = new Register("TBL",bdi.getSprValue(268),1);
			userLevelSPR[4] = new Register("TBU",bdi.getSprValue(269),1);
			userLevelSPR[5] = new Register("CR",bdi.getRegisterValue("CR"),1);

			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			userLevelSPR = null;
		} 

	}

	public void createFPRModel() {
		TargetConnection bdi = Launcher.getTargetConnection();
		if (bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			fprs = null;
			return;
		}
		fprs = new FPRegister[32];
		FPSCR = new Register[1];
		try {
			if(!bdi.isConnected()){
				bdi.openConnection();
			}
			wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			for (int i = 0; i < 32; i++) {
				fprs[i] = new FPRegister("FPR"+i,bdi.getFprValue(i),3);
			}
			FPSCR[0] = new Register("FPSCR",bdi.getRegisterValue("FPSCR"),1);
			
			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			fprs = null;
		} 
	}

	public void createGPRModel() {
		TargetConnection bdi = Launcher.getTargetConnection();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			gprs = null;
			return;
		}
		gprs = new Register[32];
		int[] temp = null;

		try {
			if(!bdi.isConnected()){
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			temp = readGPRs(bdi);
			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			gprs = null;
		} 

		// Load GPR data
		for (int i = 0; gprs != null && i < 32; i++) {
			gprs[i] = new Register("GPR"+i,temp[i],1);//Default Hex
		}

	}

	public void updateDeSuSPRMod() {
		if(devSupportLevelSPR == null){
			creatDeSuSPRMod();
			devSupportLevelSPR = null;
			return;
		}
		TargetConnection bdi = Launcher.getTargetConnection();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			return;
		}
		try {
			if(!bdi.isConnected()){//reopen
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			for (int j = 0; j < 16; j++) {
				devSupportLevelSPR[j].value = bdi.getSprValue(144 + j);
			}
			/*the mcdp-lib doesn't support access to the Development Port Data Register
			deSuSPR[16].value = bdi.getSPR(630);*/
			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			devSupportLevelSPR = null;
		} 
	}

	public void updateSuLeSPRMod() {
		if(supervisorLevelSPR == null){
			creatSuLeSPRMod();
			return;
		}
		TargetConnection bdi = Launcher.getTargetConnection();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			supervisorLevelSPR = null;
			return;
		}
		try {
			if(!bdi.isConnected()){//reopen
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			supervisorLevelSPR[0].value = bdi.getSprValue(18);
			supervisorLevelSPR[1].value  = bdi.getSprValue(19);
			supervisorLevelSPR[2].value  = bdi.getSprValue(22);
			supervisorLevelSPR[3].value  = bdi.getSprValue(26);
			supervisorLevelSPR[4].value  = bdi.getSprValue(27);
			
			/*results a software emulation exception, see Users Manuel p.3-26
			suLeSPR[].value = bdi.getSPR(80);
			suLeSPR[].value = bdi.getSPR(81);
			suLeSPR[].value = bdi.getSPR(82);*/
			
			supervisorLevelSPR[5].value  = bdi.getSprValue(272);
			supervisorLevelSPR[6].value  = bdi.getSprValue(273);
			supervisorLevelSPR[7].value  = bdi.getSprValue(274);
			supervisorLevelSPR[8].value  = bdi.getSprValue(275);
			supervisorLevelSPR[9].value  = bdi.getSprValue(287);
			supervisorLevelSPR[10].value  = bdi.getSprValue(1022);

			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			supervisorLevelSPR = null;
		} 

	}

	public void updateMSRModel() {
		if(msr == null){
			createMSRModel();
			return;
		}
		TargetConnection bdi = Launcher.getTargetConnection();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			msr = null;
			return;
		}
		try {
			if(!bdi.isConnected()){//reopen
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			msr[0].value  = bdi.getRegisterValue("MSR");

			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			msr = null;
		} 
	}

	public void updateUsLeSPRMod() {
		if(userLevelSPR == null){
			creatUsLeSPRMod();
			return;
		}
		TargetConnection bdi = Launcher.getTargetConnection();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			userLevelSPR = null;
			return;
		}
		try {
			if(!bdi.isConnected()){//reopen
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			userLevelSPR[0].value  = bdi.getSprValue(1);
			userLevelSPR[1].value  = bdi.getSprValue(8);
			userLevelSPR[2].value  = bdi.getSprValue(9);
			userLevelSPR[3].value  = bdi.getSprValue(268);
			userLevelSPR[4].value  = bdi.getSprValue(269);
			userLevelSPR[5].value  = bdi.getRegisterValue("CR");

			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			userLevelSPR = null;
		} 
	}

	public void updateFprMod() {
		if(fprs == null){
			createFPRModel();
			return;
		}
		TargetConnection bdi = Launcher.getTargetConnection();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			fprs = null;
			return;
		}
		try {
			if(!bdi.isConnected()){//reopen
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			for (int i = 0; i < 32; i++) {
				fprs[i].floatValue = bdi.getFprValue(i);
			}
			FPSCR[0].value = bdi.getRegisterValue("FPSCR");
			
			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			fprs = null;
		} 
	}

	public void updateGPRModel() {
		if(gprs == null){
			createGPRModel();
			return;
		}
		TargetConnection bdi = Launcher.getTargetConnection();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			gprs = null;
			return;
		}
		int[] temp = null;
		try {
			if(!bdi.isConnected()){//reopen
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.getTargetState() == TargetConnection.stateDebug;
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			temp = readGPRs(bdi);

			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (TargetConnectionException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			gprs = null;
		}
		// Load GPR data
		for (int i = 0; gprs != null && i < 32; i++) {
			gprs[i].value = temp[i];
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
				if(gprs == null)return errorReg;
			}
			return gprs;			
		case 1:
			if (fprs == null) {
				createFPRModel();
				if(fprs == null)return errorReg;
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
				if(userLevelSPR == null)return errorReg;
			}
			return userLevelSPR;
		case 3:
			if (msr == null) {
				createMSRModel();
				if(msr == null)return errorReg;
			}
			return msr;
		case 4:
			if (supervisorLevelSPR == null) {
				creatSuLeSPRMod();
				if(supervisorLevelSPR == null)return errorReg;
			}
			return supervisorLevelSPR;
		case 5:
			if (devSupportLevelSPR == null) {
				creatDeSuSPRMod();
				if(devSupportLevelSPR == null)return errorReg;
			}
			return devSupportLevelSPR;
		default:
			return null;
		}
	}
	/**
	 * Returns the chosen Registermodel
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
	
	private int[] readGPRs(TargetConnection bdi) throws TargetConnectionException {
		int nofGprs = bdi.getNofGpr();
		int gprs[] = new int[nofGprs];
		for(int i = 0; i < nofGprs; i++) {
			gprs[i] = bdi.getGprValue(i);
		}
		return gprs;
	}
}
