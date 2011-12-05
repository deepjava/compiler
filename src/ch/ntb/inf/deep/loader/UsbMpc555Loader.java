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

package ch.ntb.inf.deep.loader;

import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.MemoryMap;
import ch.ntb.inf.deep.config.Parser;
import ch.ntb.inf.deep.config.Register;
import ch.ntb.inf.deep.config.RegisterInit;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.Linker32;
import ch.ntb.inf.deep.linker.TargetMemorySegment;
import ch.ntb.inf.deep.strings.HString;
import ch.ntb.inf.libusbJava.USBException;
import ch.ntb.inf.mcdp.bdi.BDIException;
import ch.ntb.inf.mcdp.targets.mpc555.BDI;
import ch.ntb.inf.mcdp.usb.Device;
import ch.ntb.inf.mcdp.usb.DeviceFactory;

/**
 * Creates an USB connection to the target.<br>
 * The USB and MCDP projects have to be in the class path!!
 */
public class UsbMpc555Loader extends Downloader {

	private static UsbMpc555Loader loader;
	private static boolean dbg = false;
	private static int nofErrors = 0;
	private static HString Am29LV160d = HString.getHString("Am29LV160d");

	/**
	 * Target
	 */
	private BDI mpc;

	/**
	 * USB Device
	 */
	private Device dev;

	private UsbMpc555Loader() {
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#init()
	 */
	@Override
	public synchronized void init() throws DownloaderException {
		nofErrors = 0;
		baseAddress = Configuration.getValueFor(HString.getHString("IMB"));
		if(dbg) StdStreams.vrb.println("++++++++ Open Device!+++++++++");

		// check connection is open
		if (!this.isConnected()) {
			this.openConnection();
		}
		
		if(dbg) StdStreams.vrb.println("++++++++ Reset Target!+++++++++");
		this.resetTarget();

		if(dbg) StdStreams.vrb.println("++++++++ init Registers!+++++++++");

		// initialize Memory
		initRegisters();

		// clear the GPRs
		clearGPRs();
		StdStreams.out.println("Download");

		// Write the code down
		writeCode();
		
		if(nofErrors == 0){
			StdStreams.out.println("successfully finished");
		}else{
			StdStreams.out.println("failed");
		}

	}

	public static UsbMpc555Loader getInstance() {
		if(loader != null && !loader.isConnected()){
			loader = null;
		}
		if (loader == null) {
			loader = new UsbMpc555Loader();
			try {
				// open Usb-Connection
				loader.openConnection();
			} catch (DownloaderException e) {
				loader = null;
			}
		}
		
		return loader;
	}

	/**
	 * Set program counter to the base address
	 * 
	 * @throws DownloaderException
	 */
	protected synchronized void resetProgramCtr(int address)
			throws DownloaderException {
		try {
			// set SRR0 to start of instructions
			mpc.writeSPR(26, address);
			checkValue(mpc.readSPR(26), address);
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#writeCode()
	 */
	@Override
	protected synchronized void writeCode() throws DownloaderException {
		// get code from the Devices
		if (!isFreezeAsserted()) {
			nofErrors++;
			ErrorReporter.reporter.error(errTargetNotInDebugMode);
			return;
		}
		TargetMemorySegment image = Linker32.targetImage;
		boolean flashErased = false;
		MPC555HBFlashWriter memWriter = null;
		int count  = 0;
		while (image != null){
			if(image.segment == null ){//this should not happen
				image = image.next;
				continue;
			}
			if(image.segment.owner.getTechnology() == 0){//RAM device
				int dataSizeToTransfer = image.data.length;
				int startAddr = image.startAddress;
				int index = 0;
				
				if(count%5 == 0){
					StdStreams.out.print('.');
				}
				
				while (dataSizeToTransfer > 0) {
					// limitation for fast download is 101 Words
					int[] data = new int[100];
					if (dataSizeToTransfer < 101) {
						data = new int[dataSizeToTransfer];
					}
					for (int i = 0; i < data.length; i++) {
						data[i] = image.data[index++];
					}
					try {
						mpc.startFastDownload(startAddr);
						mpc.fastDownload(data, data.length);
						mpc.stopFastDownload();
					} catch (BDIException e) {
						e.printStackTrace();
					}
					dataSizeToTransfer -= data.length;
					startAddr += data.length * 4;
				}
				count++;
				if(image.next == null || image.next.segment.owner.getTechnology() != 0){
					StdStreams.out.println();
				}
			}else if (image.segment.owner.getTechnology() == 1){ //Flash device
				if(image.segment.owner.getMemoryType().equals(Am29LV160d)){
					if(!flashErased){//erase all used sectors
						memWriter = new MPC555HBFlashWriter(this);
						TargetMemorySegment current = image;
						//first mark all used sectors
						while(current != null && current.segment.owner.getMemoryType().equals(Am29LV160d)){
							current.segment.owner.markUsedSectors(current);
							current = current.next;
						}
						//second erase all marked sectors
						MemoryMap memMap = MemoryMap.getInstance();
						ch.ntb.inf.deep.config.Device dev = memMap.getDevices();
						while(dev != null){
							if(dev.getTechnology() == 1 && dev.getMemoryType().equals(Am29LV160d)){
								memWriter.eraseMarkedSectors(dev);
							}
							dev = dev.next;
						}
						flashErased = true;
					}
					//Programm
					if(!memWriter.unlocked){
						StdStreams.out.println("programming flash");
						memWriter.unlockBypass(image.segment.owner, true);
					}
					memWriter.writeSequence(image);
					
					if(image.next == null || image.next.segment.owner != image.segment.owner && memWriter.unlocked){
						memWriter.unlockBypass(image.segment.owner, false);
						StdStreams.out.println();						
					}
				}else{// other devices not implemented yet
						nofErrors++;
						ErrorReporter.reporter.error(errMemWriterNotImplemented,"for Device " + image.segment.owner.getName().toString());
						return;
					
					
				}
			}else{ // other technologies not implemented yet
				nofErrors++;
				ErrorReporter.reporter.error(errMemWriterNotImplemented, "for Device " + image.segment.owner.getName().toString());
				return;
			}
			image = image.next;	
		}	
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#initRegisters()
	 */
	@Override
	protected synchronized void initRegisters() throws DownloaderException {
		try {
			RegisterInit[] regInitArray = Configuration.getInitializedRegisters();
			for(int i = 0; i < regInitArray.length; i++){
				RegisterInit current = regInitArray[i];
				while (current != null) {
					switch (current.register.getType()) {
					case Parser.sSPR:
						mpc.writeSPR(current.register.getAddress(), current.initValue);
						break;
					case Parser.sIOR:
						mpc.writeMem(current.register.getAddress(), current.initValue, current.register.getSize());
						break;
					case Parser.sMSR:
						mpc.writeMSR(current.initValue);
						break;
					case Parser.sCR:
						mpc.writeCR(current.initValue);
						break;
					case Parser.sFPSCR:
						mpc.writeFPSCR(current.initValue);
						break;
					default:
						break;
					}
					current = current.next;
				}
	
				// Check if all is set fine
				current = regInitArray[i];
				while (current != null) {
					switch (current.register.getType()) {
					case Parser.sSPR:
						checkValue(mpc.readSPR(current.register.getAddress()), current.initValue);
						break;
					case Parser.sIOR:
						if (!current.register.getName().equals(HString.getHString("RSR")))
							checkValue(mpc.readMem(current.register.getAddress(), current.register.getSize()), current.initValue);
						break;
					case Parser.sMSR:
						checkValue(mpc.readMSR(), current.initValue);
						break;
					case Parser.sCR:
						checkValue(mpc.readCR(), current.initValue);
						break;
					case Parser.sFPSCR:
						checkValue(mpc.readFPSCR(), current.initValue);
						break;
					default:
						break;
					}
					current = current.next;
	
				}
			}
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#resetTarget()
	 */
	@Override
	public synchronized void resetTarget() throws DownloaderException {
		try {
			// make a hard reset
			mpc.reset_target();
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#startTarget()
	 */
	@Override
	public synchronized void startTarget() throws DownloaderException {
		try {
			mpc.go();
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#stopTarget()
	 */
	@Override
	public synchronized void stopTarget() throws DownloaderException {
		try {
			mpc.break_();
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#internalBreakpoint()
	 */
	@Override
	public synchronized void internalBreakpoint() throws DownloaderException {
		try {
			mpc.prologue();
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#readGPRs()
	 */
	@Override
	public synchronized int[] readGPRs() throws DownloaderException {
		// RegisterMap regMap = Configuration.getRegisterMap();
		// int[] gprs = new int[regMap.getNofGprs()]; TODO this it the orignal
		// line
		int[] gprs = new int[32];
		for (int j = 0; j < gprs.length; j++) {
			gprs[j] = getGPR(j);
		}
		return gprs;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#setGPRs(java.util.Map)
	 */
	@Override
	public synchronized void setGPRs(int[][] gprs) throws DownloaderException {
		for (int i = 0; i < gprs.length; i++) {
			setGPR(gprs[i][0], gprs[i][1]);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#setGPR(int,
	 *      int)
	 */
	@Override
	public synchronized void setGPR(int no, int value)
			throws DownloaderException {
		try {
			mpc.writeGPR(no, value);
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#getGPR(int)
	 */
	@Override
	public synchronized int getGPR(int no) throws DownloaderException {
		int value = 0;
		try {
			value = mpc.readGPR(no);
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
		return value;
	}

	/**
	 * Sets all GPRs to 0
	 * 
	 * @throws DownloaderException
	 */
	public synchronized void clearGPRs() throws DownloaderException {
		// for (int i = 0; i < Configuration.getRegisterMap().getNofGprs(); i++)
		// {TODO this it the orignal line
		for (int i = 0; i < 32; i++) {
			setGPR(i, 0);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#getCR()
	 */
	@Override
	public synchronized int getCR() throws DownloaderException {
		int crReg = 0;
		try {
			crReg = mpc.readCR();
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
		return crReg;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#setCR(int)
	 */
	@Override
	public synchronized void setCR(int value) throws DownloaderException {
		try {
			mpc.writeCR(value);
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#isFreezeAsserted()
	 */
	@Override
	public synchronized boolean isFreezeAsserted() throws DownloaderException {
		boolean freeze = false;
		try {
			freeze = mpc.isFreezeAsserted();
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
		return freeze;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#setSPR(int,
	 *      int)
	 */
	@Override
	public synchronized void setSPR(int no, int value)
			throws DownloaderException {
		try {
			mpc.writeSPR(no, value);
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#getSPR(int)
	 */
	@Override
	public synchronized int getSPR(int no) throws DownloaderException {
		int value = 0;
		try {
			value = mpc.readSPR(no);
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
		return value;
	}

	public synchronized long getFPR(int no) throws DownloaderException {
		long value = 0;
		int temp;
		try {
			temp = mpc.readMem(baseAddress, 4);
			value = mpc.readFPR(no, baseAddress);
			mpc.writeMem(baseAddress, temp, 4);
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
		return value;
	}

	public synchronized void setFPR(int no, long value)
			throws DownloaderException {
		int temp;
		try {
			temp = mpc.readMem(baseAddress, 4);
			mpc.writeFPR(no, baseAddress, value);
			mpc.writeMem(baseAddress, temp, 4);
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
	}

	public synchronized int getFPSCR() throws DownloaderException {
		int value = 0;
		try {
			value = mpc.readFPSCR();
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
		return value;

	}

	public synchronized int getMSR() throws DownloaderException {
		int value = 0;
		try {
			value = mpc.readMSR();
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
		return value;

	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#getMem(int,
	 *      int)
	 */
	@Override
	public synchronized int getMem(int addr, int size)
			throws DownloaderException {
		int value = 0;
		try {
			value = mpc.readMem(addr, size);
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
		return value;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#setMem(int,
	 *      int, int)
	 */
	@Override
	public synchronized void setMem(int addr, int value, int size)
			throws DownloaderException {
		try {
			mpc.writeMem(addr, value, size);
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
	}

	/**
	 * Check if the register is written correct
	 * 
	 * @param i
	 *            Value 1
	 * @param j
	 *            Value 2
	 */
	private synchronized static void checkValue(int i, int j) {
		if (i != j) {
			throw new RuntimeException("i (" + i + ") != j (" + j + ")");
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#openConnection()
	 */
	@Override
	public synchronized void openConnection() throws DownloaderException {
		dev = DeviceFactory.getDevice();
		try {
			mpc = new BDI(dev);
			// StdStreams.vrb.println("USB dev.open()");
			dev.open();
		} catch (USBException e) {
			try {
				dev.close();
			} catch (USBException e1) {
				// nothing to do
			}
			throw new DownloaderException(e.getMessage(), e);
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#closeConnection()
	 */
	@Override
	public synchronized void closeConnection() {
		try {
			dev.close();
		} catch (USBException e) {
			// do nothing
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#isConnected()
	 */
	@Override
	public synchronized boolean isConnected() {
		return dev.isOpen();
	}

	@Override
	public synchronized void setBreakpoint(int memAddr) throws DownloaderException {
		//1. Disable Instruction breakpoint interrupt
		Register der = Configuration.getRegisterMap().getRegister(HString.getHString("DER"));
		if(der == null)return;
		int derValue = getSPR(der.getAddress()) & ~0x4;
		setSPR(der.getAddress(), derValue);
		
		// 2. read ICTRL
		Register ictrl = Configuration.getRegisterMap().getRegister(HString.getHString("ICTRL"));
		if(ictrl == null)return;
		int ictrlValue = getSPR(ictrl.getAddress());
	
		//3. decide which CMP is free an set it appropriate
		if((ictrlValue & 0x80000) == 0){//CMPA is free
			Register cmpa = Configuration.getRegisterMap().getRegister(HString.getHString("CMPA"));
			if(cmpa == null)return;
			setSPR(cmpa.getAddress(), memAddr);
			setSPR(ictrl.getAddress(), ictrlValue | 0x80080800);//CTA = equals, IWP0 = match from CMPA, SIWP0EN = trap enabled
		}else if((ictrlValue & 0x20000) == 0){//CMPB is free
			Register cmpb = Configuration.getRegisterMap().getRegister(HString.getHString("CMPB"));
			if(cmpb == null)return;
			setSPR(cmpb.getAddress(), memAddr);
			setSPR(ictrl.getAddress(), ictrlValue | 0x10020400);//CTB = equals, IWP1 = match from CMPB, SIWP1EN = trap enabled			
		}else if((ictrlValue & 0x8000) == 0){//CMPC is free
			Register cmpc = Configuration.getRegisterMap().getRegister(HString.getHString("CMPC"));
			if(cmpc == null)return;
			setSPR(cmpc.getAddress(), memAddr);
			setSPR(ictrl.getAddress(), ictrlValue | 0x2008200);//CTC = equals, IWP2 = match from CMPC, SIWP2EN = trap enabled
		}else if((ictrlValue & 0x2000) == 0){//CMPD is free
			Register cmpd = Configuration.getRegisterMap().getRegister(HString.getHString("CMPD"));
			if(cmpd == null)return;
			setSPR(cmpd.getAddress(), memAddr);
			setSPR(ictrl.getAddress(), ictrlValue | 0x402100);//CTD = equals, IWP3 = match from CMPD, SIWP3EN = trap enabled
		}
		
		//4. Enable instruction breakpoint interrupt
		setSPR(der.getAddress(), derValue | 0x4);		
	}
	
	@Override
	public synchronized void confirmBreakpoint() throws DownloaderException {
		//1. Disable Instruction breakpoint interrupt
		Register der = Configuration.getRegisterMap().getRegister(HString.getHString("DER"));
		if(der == null)return;
		int derValue = getSPR(der.getAddress()) & ~0x4;
		setSPR(der.getAddress(), derValue);
		
		// 2. read ICTRL
		Register ictrl = Configuration.getRegisterMap().getRegister(HString.getHString("ICTRL"));
		if(ictrl == null)return;
		int ictrlValue = getSPR(ictrl.getAddress());
		
		// 3. set ignore first match on i-bus
		setSPR(ictrl.getAddress(), ictrlValue | 0x8);		
		
		//4. Enable instruction breakpoint interrupt
		setSPR(der.getAddress(), derValue | 0x4);	
	
		
	}

	@Override
	public synchronized void removeBreakpoint(int memAddr) throws DownloaderException {
		//1. Disable Instruction breakpoint interrupt
		Register der = Configuration.getRegisterMap().getRegister(HString.getHString("DER"));
		if(der == null)return;
		int derValue = getSPR(der.getAddress()) & ~0x4;
		setSPR(der.getAddress(), derValue);
		
		// 2. read ICTRL
		Register ictrl = Configuration.getRegisterMap().getRegister(HString.getHString("ICTRL"));
		if(ictrl == null)return;
		int ictrlValue = getSPR(ictrl.getAddress());
		
		//3. find correct CMP
		if((ictrlValue & 0x80000) != 0){//CMPA is used
			Register cmpa = Configuration.getRegisterMap().getRegister(HString.getHString("CMPA"));
			if(cmpa == null)return;
			int cmpaValue = getSPR(cmpa.getAddress());
			if(cmpaValue == memAddr){
				setSPR(ictrl.getAddress(), ictrlValue & ~0xE00C0800);
				//4. Enable instruction breakpoint interrupt
				setSPR(der.getAddress(), derValue | 0x4);
				return;
			}
		}
		if((ictrlValue & 0x20000) != 0){//CMPB is used
			Register cmpb = Configuration.getRegisterMap().getRegister(HString.getHString("CMPB"));
			if(cmpb == null)return;
			int cmpbValue = getSPR(cmpb.getAddress());
			if(cmpbValue == memAddr){
				setSPR(ictrl.getAddress(), ictrlValue & ~0x1C030400);
				//4. Enable instruction breakpoint interrupt
				setSPR(der.getAddress(), derValue | 0x4);
				return;
			}
			
		}
		if((ictrlValue & 0x8000) != 0){//CMPC is used
			Register cmpc = Configuration.getRegisterMap().getRegister(HString.getHString("CMPC"));
			if(cmpc == null)return;
			int cmpcValue = getSPR(cmpc.getAddress());
			if(cmpcValue == memAddr){
				setSPR(ictrl.getAddress(), ictrlValue & ~0x38C200);
				//4. Enable instruction breakpoint interrupt
				setSPR(der.getAddress(), derValue | 0x4);
				return;
			}
			
		}
		if((ictrlValue & 0x2000) != 0){//CMPD is used
			Register cmpd = Configuration.getRegisterMap().getRegister(HString.getHString("CMPD"));
			if(cmpd == null)return;
			int cmpdValue = getSPR(cmpd.getAddress());
			if(cmpdValue == memAddr){
				setSPR(ictrl.getAddress(), ictrlValue & ~0x73100);
				//4. Enable instruction breakpoint interrupt
				setSPR(der.getAddress(), derValue | 0x4);
				return;
			}
		}
		
		//4. Enable instruction breakpoint interrupt
		setSPR(der.getAddress(), derValue | 0x4);	
	
		
	}

	@Override
	public synchronized int getECR() throws DownloaderException {
		Register ecr = Configuration.getRegisterMap().getRegister(HString.getHString("ECR"));
		if(ecr == null)throw new DownloaderException("ECR Regigster not found!");
		return getSPR(ecr.getAddress());
	}

	@Override
	public int getPC() throws DownloaderException {
		Register srr0 = Configuration.getRegisterMap().getRegister(HString.getHString("SRR0"));
		if(srr0 == null)throw new DownloaderException("PC Regigster not found!");
		return getSPR(srr0.getAddress());
	}

	@Override
	public int getSP() throws DownloaderException {
		return getGPR(1);
	}

	@Override
	public void setMSR(int value) throws DownloaderException {
		try {
			mpc.writeMSR(value);
		} catch (BDIException e) {
			throw new DownloaderException(e.getMessage(), e);
		}
	}

	@Override
	public void setStepping(int mode) throws DownloaderException {
//		int msrValue = getMSR(); //Clear BE and SE bit
		Register srr1 = Configuration.getRegisterMap().getRegister(HString.getHString("SRR1"));
		int srr1Value = getSPR(srr1.getAddress()) & ~0x600;
		switch(mode){
		case 1://branch trace
			srr1Value |= 0x200;
//			msrValue |= 0x200;
			setSPR(srr1.getAddress(), srr1Value & 0xFFFF); 
//			setMSR(msrValue);
			break;
		case 2://single step
			srr1Value |= 0x400;
//			msrValue |= 0x400;
			setSPR(srr1.getAddress(), srr1Value & 0xFFFF); 
//			setMSR(msrValue);
			break;
		case 3://both
			srr1Value |= 0x600;
//			msrValue |= 0x600;
			setSPR(srr1.getAddress(), srr1Value & 0xFFFF); 
//			setMSR(msrValue);
			break;
		default ://clear
			setSPR(srr1.getAddress(), srr1Value & 0xFFFF); 
//			setMSR(msrValue);
			break;
		}
		
	}

}
