/**
 * Copyright (c) 2010 NTB Interstaatliche Hochschule für Technick Buchs
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * 
 * Contributors:
 *	   NTB - initial implementation
 *	   Roger Millischer - initial implementation
 */
package ch.ntb.inf.deep.eclipse.ui.model;


import ch.ntb.inf.deep.loader.Downloader;
import ch.ntb.inf.deep.loader.DownloaderException;
import ch.ntb.inf.deep.loader.UsbMpc555Loader;


public class RegModel  {


	private Register[] genPurReg; 
	private FloRegister[] floPoiReg;
	private Register[] FPSCR;
	private Register[] usLeSPR;
	private Register[] maStReg;
	private Register[] suLeSPR;
	private Register[] deSuSPR;
	private Register[] errorReg;
	private boolean wasFreezeAsserted;

//	private Downloader module;
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
		Downloader bdi = UsbMpc555Loader.getInstance();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			deSuSPR = null;
			return;
		}
		deSuSPR = new Register[16];
		try {
			if(!bdi.isConnected()){
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.isFreezeAsserted();
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			
			deSuSPR[0] = new Register("CMPA",bdi.getSPR(144),1);
			deSuSPR[1] = new Register("CMPB",bdi.getSPR(145),1);
			deSuSPR[2] = new Register("CMPC",bdi.getSPR(146),1);
			deSuSPR[3] = new Register("CMPD",bdi.getSPR(147),1);
			deSuSPR[4] = new Register("ECR",bdi.getSPR(148),1);
			deSuSPR[5] = new Register("DER",bdi.getSPR(149),1);
			deSuSPR[6] = new Register("COUNTA",bdi.getSPR(150),1);
			deSuSPR[7] = new Register("COUNTB",bdi.getSPR(151),1);
			deSuSPR[8] = new Register("CMPE",bdi.getSPR(152),1);
			deSuSPR[9] = new Register("CMPF",bdi.getSPR(153),1);
			deSuSPR[10] = new Register("CMPG",bdi.getSPR(154),1);
			deSuSPR[11] = new Register("CMPH",bdi.getSPR(155),1);
			deSuSPR[12] = new Register("LCTRL1",bdi.getSPR(156),1);
			deSuSPR[13] = new Register("LCTRL2",bdi.getSPR(157),1);
			deSuSPR[14] = new Register("ICTRL",bdi.getSPR(158),1);
			deSuSPR[15] = new Register("BAR",bdi.getSPR(159),1);

			/*the mcdp-lib dosent support access to Development Port Data Register
			deSuSPR[16] = new Register("DPDR",bdi.getSPR(630),0);*/

			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (DownloaderException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			deSuSPR = null;
		} 
	}

	public void creatSuLeSPRMod() {
		Downloader bdi = UsbMpc555Loader.getInstance();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			suLeSPR = null;
			return;
		}
		suLeSPR = new Register[11];
		try {
			if(!bdi.isConnected()){
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.isFreezeAsserted();
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			suLeSPR[0] = new Register("DSISR",bdi.getSPR(18),1);
			suLeSPR[1]= new Register("DAR",bdi.getSPR(19),1);
			suLeSPR[2]= new Register("DEC",bdi.getSPR(22),1);
			suLeSPR[3]= new Register("SSR0",bdi.getSPR(26),1);
			suLeSPR[4]= new Register("SSR1",bdi.getSPR(27),1);
			
			/*results a software emulation exception, see Users Manuel p.3-26
			suLeSPR[]= new Register("EIE",bdi.getSPR(80),0);
			suLeSPR[]= new Register("EID",bdi.getSPR(81),0);
			suLeSPR[]= new Register("NRI",bdi.getSPR(82),0);*/
			
			suLeSPR[5]= new Register("SPRG0",bdi.getSPR(272),1);
			suLeSPR[6]= new Register("SPRG1",bdi.getSPR(273),1);
			suLeSPR[7]= new Register("SPRG2",bdi.getSPR(274),1);
			suLeSPR[8]= new Register("SPRG3",bdi.getSPR(275),1);
			suLeSPR[9]= new Register("PVR",bdi.getSPR(287),1);
			suLeSPR[10]= new Register("FPECR",bdi.getSPR(1022),1);

			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (DownloaderException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			suLeSPR = null;
		} 
	}

	public void creatMaStRegMod() {
		Downloader bdi = UsbMpc555Loader.getInstance();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			maStReg = null;
			return;
		}
		maStReg = new Register[1];
		try {
			if(!bdi.isConnected()){
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.isFreezeAsserted();
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			
			maStReg[0] = new Register("MSR",bdi.getMSR(),1);
			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (DownloaderException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			maStReg = null;
		}  

	}

	public void creatUsLeSPRMod() {
		Downloader bdi = UsbMpc555Loader.getInstance();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			usLeSPR = null;
			return;
		}
		usLeSPR = new Register[6];
		try {
			if(!bdi.isConnected()){
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.isFreezeAsserted();
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			usLeSPR[0] = new Register("XER",bdi.getSPR(1),1);
			usLeSPR[1] = new Register("LR",bdi.getSPR(8),1);
			usLeSPR[2] = new Register("CTR",bdi.getSPR(9),1);
			usLeSPR[3] = new Register("TBL",bdi.getSPR(268),1);
			usLeSPR[4] = new Register("TBU",bdi.getSPR(269),1);
			usLeSPR[5] = new Register("CR",bdi.getCR(),1);

			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (DownloaderException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			usLeSPR = null;
		} 

	}

	public void creatFprMod() {
		Downloader bdi = UsbMpc555Loader.getInstance();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			floPoiReg = null;
			return;
		}
		floPoiReg = new FloRegister[32];
		FPSCR = new Register[1];
		try {
			if(!bdi.isConnected()){
				bdi.openConnection();
			}
			wasFreezeAsserted = bdi.isFreezeAsserted();
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			for (int i = 0; i < 32; i++) {
				floPoiReg[i] = new FloRegister("FPR"+i,bdi.getFPR(i),3);
			}
			FPSCR[0] = new Register("FPSCR",bdi.getFPSCR(),1);
			
			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (DownloaderException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			floPoiReg = null;
		} 
	}

	public void creatGprMod() {
		Downloader bdi = UsbMpc555Loader.getInstance();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			genPurReg = null;
			return;
		}
		genPurReg = new Register[32];
		int[] temp = null;

		try {
			if(!bdi.isConnected()){
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.isFreezeAsserted();
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			temp = bdi.readGPRs();
			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (DownloaderException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			genPurReg = null;
		} 

		// Load GPR data
		for (int i = 0; genPurReg != null && i < 32; i++) {
			genPurReg[i] = new Register("GPR"+i,temp[i],1);//Default Hex
		}

	}

	public void updateDeSuSPRMod() {
		if(deSuSPR == null){
			creatDeSuSPRMod();
			deSuSPR = null;
			return;
		}
		Downloader bdi = UsbMpc555Loader.getInstance();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			return;
		}
		try {
			if(!bdi.isConnected()){//reopen
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.isFreezeAsserted();
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			for (int j = 0; j < 16; j++) {
				deSuSPR[j].value = bdi.getSPR(144 + j);
			}
			/*the mcdp-lib doesn't support access to the Development Port Data Register
			deSuSPR[16].value = bdi.getSPR(630);*/
			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (DownloaderException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			deSuSPR = null;
		} 
	}

	public void updateSuLeSPRMod() {
		if(suLeSPR == null){
			creatSuLeSPRMod();
			return;
		}
		Downloader bdi = UsbMpc555Loader.getInstance();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			suLeSPR = null;
			return;
		}
		try {
			if(!bdi.isConnected()){//reopen
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.isFreezeAsserted();
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			suLeSPR[0].value = bdi.getSPR(18);
			suLeSPR[1].value  = bdi.getSPR(19);
			suLeSPR[2].value  = bdi.getSPR(22);
			suLeSPR[3].value  = bdi.getSPR(26);
			suLeSPR[4].value  = bdi.getSPR(27);
			
			/*results a software emulation exception, see Users Manuel p.3-26
			suLeSPR[].value = bdi.getSPR(80);
			suLeSPR[].value = bdi.getSPR(81);
			suLeSPR[].value = bdi.getSPR(82);*/
			
			suLeSPR[5].value  = bdi.getSPR(272);
			suLeSPR[6].value  = bdi.getSPR(273);
			suLeSPR[7].value  = bdi.getSPR(274);
			suLeSPR[8].value  = bdi.getSPR(275);
			suLeSPR[9].value  = bdi.getSPR(287);
			suLeSPR[10].value  = bdi.getSPR(1022);

			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (DownloaderException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			suLeSPR = null;
		} 

	}

	public void updateMaStRegMod() {
		if(maStReg == null){
			creatMaStRegMod();
			return;
		}
		Downloader bdi = UsbMpc555Loader.getInstance();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			maStReg = null;
			return;
		}
		try {
			if(!bdi.isConnected()){//reopen
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.isFreezeAsserted();
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			maStReg[0].value  = bdi.getMSR();

			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (DownloaderException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			maStReg = null;
		} 
	}

	public void updateUsLeSPRMod() {
		if(usLeSPR == null){
			creatUsLeSPRMod();
			return;
		}
		Downloader bdi = UsbMpc555Loader.getInstance();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			usLeSPR = null;
			return;
		}
		try {
			if(!bdi.isConnected()){//reopen
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.isFreezeAsserted();
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			usLeSPR[0].value  = bdi.getSPR(1);
			usLeSPR[1].value  = bdi.getSPR(8);
			usLeSPR[2].value  = bdi.getSPR(9);
			usLeSPR[3].value  = bdi.getSPR(268);
			usLeSPR[4].value  = bdi.getSPR(269);
			usLeSPR[5].value  = bdi.getCR();

			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (DownloaderException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			usLeSPR = null;
		} 
	}

	public void updateFprMod() {
		if(floPoiReg == null){
			creatFprMod();
			return;
		}
		Downloader bdi = UsbMpc555Loader.getInstance();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			floPoiReg = null;
			return;
		}
		try {
			if(!bdi.isConnected()){//reopen
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.isFreezeAsserted();
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			for (int i = 0; i < 32; i++) {
				floPoiReg[i].floatValue = bdi.getFPR(i);
			}
			FPSCR[0].value = bdi.getFPSCR();
			
			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (DownloaderException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			floPoiReg = null;
		} 
	}

	public void updateGprMod() {
		if(genPurReg == null){
			creatGprMod();
			return;
		}
		Downloader bdi = UsbMpc555Loader.getInstance();
		if(bdi == null){
			errorReg = new Register[]{new Register("target not connected",-1,1)};
			genPurReg = null;
			return;
		}
		int[] temp = null;
		try {
			if(!bdi.isConnected()){//reopen
				bdi.openConnection();
			}
			
			wasFreezeAsserted = bdi.isFreezeAsserted();
			if(!wasFreezeAsserted){
				bdi.stopTarget();
			}
			temp = bdi.readGPRs();

			if(!wasFreezeAsserted){
				bdi.startTarget();
			}
		} catch (DownloaderException e) {
			errorReg = new Register[]{new Register("target is not initialized",-1,1)};
			genPurReg = null;
		}
		// Load GPR data
		for (int i = 0; genPurReg != null && i < 32; i++) {
			genPurReg[i].value = temp[i];
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
			if (genPurReg == null) {
				creatGprMod();
				if(genPurReg == null)return errorReg;
			}
			return genPurReg;			
		case 1:
			if (floPoiReg == null) {
				creatFprMod();
				if(floPoiReg == null)return errorReg;
			}
			Register[] floReg = new Register[33];
			for(int i = 0;i < 32; i++){
				floReg[i]=floPoiReg[i];
			}
			floReg[32] = FPSCR[0];
			return floReg;
		case 2:
			if (usLeSPR == null) {
				creatUsLeSPRMod();
				if(usLeSPR == null)return errorReg;
			}
			return usLeSPR;
		case 3:
			if (maStReg == null) {
				creatMaStRegMod();
				if(maStReg == null)return errorReg;
			}
			return maStReg;
		case 4:
			if (suLeSPR == null) {
				creatSuLeSPRMod();
				if(suLeSPR == null)return errorReg;
			}
			return suLeSPR;
		case 5:
			if (deSuSPR == null) {
				creatDeSuSPRMod();
				if(deSuSPR == null)return errorReg;
			}
			return deSuSPR;
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
			genPurReg = null;
			break;
		case 1:
			floPoiReg = null;
			FPSCR = null;
			break;
		case 2:
			usLeSPR = null;
			break;
		case 3:
			maStReg = null;
			break;
		case 4:
			suLeSPR = null;
			break;
		case 5:
			deSuSPR = null;
			break;
		default:
			return;
		}
	}
}
