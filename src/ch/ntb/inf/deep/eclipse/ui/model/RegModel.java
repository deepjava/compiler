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
	private boolean wasFreezeAsserted;

	private Downloader module;
	private static RegModel model;
	
	private RegModel(){
	}
	public static RegModel getInstance(){
		if(model == null){
			Downloader loader = UsbMpc555Loader.getInstance();
			model = new RegModel();
			model.setloader(loader);
		}
		return model;
	}
	public void setloader(Downloader bdi){
		module = bdi;
	}
	
	public void creatDeSuSPRMod() {
		deSuSPR = new Register[16];
	
		try {
			if(!module.isConnected()){
				module.openConnection();
			}
			
			wasFreezeAsserted = module.isFreezeAsserted();
			if(!wasFreezeAsserted){
				module.stopTarget();
			}
			
			deSuSPR[0] = new Register("CMPA",module.getSPR(144),1);
			deSuSPR[1] = new Register("CMPB",module.getSPR(145),1);
			deSuSPR[2] = new Register("CMPC",module.getSPR(146),1);
			deSuSPR[3] = new Register("CMPD",module.getSPR(147),1);
			deSuSPR[4] = new Register("ECR",module.getSPR(148),1);
			deSuSPR[5] = new Register("DER",module.getSPR(149),1);
			deSuSPR[6] = new Register("COUNTA",module.getSPR(150),1);
			deSuSPR[7] = new Register("COUNTB",module.getSPR(151),1);
			deSuSPR[8] = new Register("CMPE",module.getSPR(152),1);
			deSuSPR[9] = new Register("CMPF",module.getSPR(153),1);
			deSuSPR[10] = new Register("CMPG",module.getSPR(154),1);
			deSuSPR[11] = new Register("CMPH",module.getSPR(155),1);
			deSuSPR[12] = new Register("LCTRL1",module.getSPR(156),1);
			deSuSPR[13] = new Register("LCTRL2",module.getSPR(157),1);
			deSuSPR[14] = new Register("ICTRL",module.getSPR(158),1);
			deSuSPR[15] = new Register("BAR",module.getSPR(159),1);

			/*the mcdp-lib dosent support access to Development Port Data Register
			deSuSPR[16] = new Register("DPDR",module.getSPR(630),0);*/

			if(!wasFreezeAsserted){
				module.startTarget();
			}
		} catch (DownloaderException e) {
			System.out.println(e);
			System.out.println("create DeSuSPR");
		} 
	}

	public void creatSuLeSPRMod() {
		suLeSPR = new Register[11];
		try {
			if(!module.isConnected()){
				module.openConnection();
			}
			
			wasFreezeAsserted = module.isFreezeAsserted();
			if(!wasFreezeAsserted){
				module.stopTarget();
			}
			suLeSPR[0] = new Register("DSISR",module.getSPR(18),1);
			suLeSPR[1]= new Register("DAR",module.getSPR(19),1);
			suLeSPR[2]= new Register("DEC",module.getSPR(22),1);
			suLeSPR[3]= new Register("SSR0",module.getSPR(26),1);
			suLeSPR[4]= new Register("SSR1",module.getSPR(27),1);
			
			/*results a software emulation exception, see Users Manuel p.3-26
			suLeSPR[]= new Register("EIE",module.getSPR(80),0);
			suLeSPR[]= new Register("EID",module.getSPR(81),0);
			suLeSPR[]= new Register("NRI",module.getSPR(82),0);*/
			
			suLeSPR[5]= new Register("SPRG0",module.getSPR(272),1);
			suLeSPR[6]= new Register("SPRG1",module.getSPR(273),1);
			suLeSPR[7]= new Register("SPRG2",module.getSPR(274),1);
			suLeSPR[8]= new Register("SPRG3",module.getSPR(275),1);
			suLeSPR[9]= new Register("PVR",module.getSPR(287),1);
			suLeSPR[10]= new Register("FPECR",module.getSPR(1022),1);

			if(!wasFreezeAsserted){
				module.startTarget();
			}
		} catch (DownloaderException e) {
			System.out.println(e);
			System.out.println("create SuLeSPR");
		} 
	}

	public void creatMaStRegMod() {
		maStReg = new Register[1];
		try {
			if(!module.isConnected()){
				module.openConnection();
			}
			
			wasFreezeAsserted = module.isFreezeAsserted();
			if(!wasFreezeAsserted){
				module.stopTarget();
			}
			
			maStReg[0] = new Register("MSR",module.getMSR(),1);
			if(!wasFreezeAsserted){
				module.startTarget();
			}
		} catch (DownloaderException e) {
			System.out.println(e);
			System.out.println("create MaStReg");
		}  

	}

	public void creatUsLeSPRMod() {
		usLeSPR = new Register[6];
		try {
			if(!module.isConnected()){
				module.openConnection();
			}
			
			wasFreezeAsserted = module.isFreezeAsserted();
			if(!wasFreezeAsserted){
				module.stopTarget();
			}
			usLeSPR[0] = new Register("XER",module.getSPR(1),1);
			usLeSPR[1] = new Register("LR",module.getSPR(8),1);
			usLeSPR[2] = new Register("CTR",module.getSPR(9),1);
			usLeSPR[3] = new Register("TBL",module.getSPR(268),1);
			usLeSPR[4] = new Register("TBU",module.getSPR(269),1);
			usLeSPR[5] = new Register("CR",module.getCR(),1);

			if(!wasFreezeAsserted){
				module.startTarget();
			}
		} catch (DownloaderException e) {
			System.out.println(e);
			System.out.println("create UsLeSPR");
		} 

	}

	public void creatFprMod() {
		floPoiReg = new FloRegister[32];
		FPSCR = new Register[1];
		try {
			if(!module.isConnected()){
				module.openConnection();
			}
			wasFreezeAsserted = module.isFreezeAsserted();
			if(!wasFreezeAsserted){
				module.stopTarget();
			}
			for (int i = 0; i < 32; i++) {
				floPoiReg[i] = new FloRegister("FPR"+i,module.getFPR(i),3);
			}
			FPSCR[0] = new Register("FPSCR",module.getFPSCR(),1);
			
			if(!wasFreezeAsserted){
				module.startTarget();
			}
		} catch (DownloaderException e) {
			System.out.println(e);
			System.out.println("create Fpr");
		} 
	}

	public void creatGprMod() {
		genPurReg = new Register[32];
		int[] temp = null;

		try {
			if(!module.isConnected()){
				module.openConnection();
			}
			
			wasFreezeAsserted = module.isFreezeAsserted();
			if(!wasFreezeAsserted){
				module.stopTarget();
			}
			temp = module.readGPRs();
			if(!wasFreezeAsserted){
				module.startTarget();
			}
		} catch (DownloaderException e) {
			System.out.println(e);
			System.out.println("create GPR");
		} 

		// Load GPR data
		for (int i = 0; i < 32; i++) {
			genPurReg[i] = new Register("GPR"+i,temp[i],1);//Default Hex
		}

	}

	public void updateDeSuSPRMod() {
		try {
			if(!module.isConnected()){//reopen
				module.openConnection();
			}
			
			wasFreezeAsserted = module.isFreezeAsserted();
			if(!wasFreezeAsserted){
				module.stopTarget();
			}
			for (int j = 0; j < 16; j++) {
				deSuSPR[j].value = module.getSPR(144 + j);
			}
			/*the mcdp-lib doesn't support access to the Development Port Data Register
			deSuSPR[16].value = module.getSPR(630);*/
			if(!wasFreezeAsserted){
				module.startTarget();
			}
		} catch (DownloaderException e) {
			System.out.println(e);
			System.out.println("update DeSuSPR");
		} 
	}

	public void updateSuLeSPRMod() {
		try {
			if(!module.isConnected()){//reopen
				module.openConnection();
			}
			
			wasFreezeAsserted = module.isFreezeAsserted();
			if(!wasFreezeAsserted){
				module.stopTarget();
			}
			suLeSPR[0].value = module.getSPR(18);
			suLeSPR[1].value  = module.getSPR(19);
			suLeSPR[2].value  = module.getSPR(22);
			suLeSPR[3].value  = module.getSPR(26);
			suLeSPR[4].value  = module.getSPR(27);
			
			/*results a software emulation exception, see Users Manuel p.3-26
			suLeSPR[].value = module.getSPR(80);
			suLeSPR[].value = module.getSPR(81);
			suLeSPR[].value = module.getSPR(82);*/
			
			suLeSPR[5].value  = module.getSPR(272);
			suLeSPR[6].value  = module.getSPR(273);
			suLeSPR[7].value  = module.getSPR(274);
			suLeSPR[8].value  = module.getSPR(275);
			suLeSPR[9].value  = module.getSPR(287);
			suLeSPR[10].value  = module.getSPR(1022);

			if(!wasFreezeAsserted){
				module.startTarget();
			}
		} catch (DownloaderException e) {
			System.out.println(e);
			System.out.println("update SuLeSPR");
		} 

	}

	public void updateMaStRegMod() {

		try {
			if(!module.isConnected()){//reopen
				module.openConnection();
			}
			
			wasFreezeAsserted = module.isFreezeAsserted();
			if(!wasFreezeAsserted){
				module.stopTarget();
			}
			maStReg[0].value  = module.getMSR();

			if(!wasFreezeAsserted){
				module.startTarget();
			}
		} catch (DownloaderException e) {
			System.out.println(e);
			System.out.println("update MaStReg");
		} 
	}

	public void updateUsLeSPRMod() {
		try {
			if(!module.isConnected()){//reopen
				module.openConnection();
			}
			
			wasFreezeAsserted = module.isFreezeAsserted();
			if(!wasFreezeAsserted){
				module.stopTarget();
			}
			usLeSPR[0].value  = module.getSPR(1);
			usLeSPR[1].value  = module.getSPR(8);
			usLeSPR[2].value  = module.getSPR(9);
			usLeSPR[3].value  = module.getSPR(268);
			usLeSPR[4].value  = module.getSPR(269);
			usLeSPR[5].value  = module.getCR();

			if(!wasFreezeAsserted){
				module.startTarget();
			}
		} catch (DownloaderException e) {
			System.out.println(e);
			System.out.println("update UsLeSPR");
		} 
	}

	public void updateFprMod() {

		try {
			if(!module.isConnected()){//reopen
				module.openConnection();
			}
			
			wasFreezeAsserted = module.isFreezeAsserted();
			if(!wasFreezeAsserted){
				module.stopTarget();
			}
			for (int i = 0; i < 32; i++) {
				floPoiReg[i].floatValue = module.getFPR(i);
			}
			FPSCR[0].value = module.getFPSCR();
			
			if(!wasFreezeAsserted){
				module.startTarget();
			}
		} catch (DownloaderException e) {
			System.out.println(e);
			System.out.println("update Fpr");
		} 
	}

	public void updateGprMod() {
		int[] temp = null;
		try {
			if(!module.isConnected()){//reopen
				module.openConnection();
			}
			
			wasFreezeAsserted = module.isFreezeAsserted();
			if(!wasFreezeAsserted){
				module.stopTarget();
			}
			temp = module.readGPRs();

			if(!wasFreezeAsserted){
				module.startTarget();
			}
		} catch (DownloaderException e) {
			System.out.println(e);
			System.out.println("update Gpr");
		}
		// Load GPR data
		for (int i = 0; i < 32; i++) {
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
			}
			return genPurReg;			
		case 1:
			if (floPoiReg == null) {
				creatFprMod();
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
			}
			return usLeSPR;
		case 3:
			if (maStReg == null) {
				creatMaStRegMod();
			}
			return maStReg;
		case 4:
			if (suLeSPR == null) {
				creatSuLeSPRMod();
			}
			return suLeSPR;
		case 5:
			if (deSuSPR == null) {
				creatDeSuSPRMod();
			}
			return deSuSPR;
		default:
			return null;
		}
	}
}
