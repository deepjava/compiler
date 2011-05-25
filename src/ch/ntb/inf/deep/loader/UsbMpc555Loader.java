package ch.ntb.inf.deep.loader;

import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.MemoryMap;
import ch.ntb.inf.deep.config.Parser;
import ch.ntb.inf.deep.config.RegisterInit;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.Linker32;
import ch.ntb.inf.deep.linker.TargetMemorySegment;
import ch.ntb.inf.deep.strings.HString;
import ch.ntb.inf.libusbJava.USBException;
import ch.ntb.mcdp.bdi.BDIException;
import ch.ntb.mcdp.targets.mpc555.BDI;
import ch.ntb.mcdp.usb.Device;
import ch.ntb.mcdp.usb.DeviceFactory;

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
			ErrorReporter.reporter.error("Bdi is not in Debug mode!");
			ErrorReporter.reporter.println();
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
						ErrorReporter.reporter.error("MemoryWriter for Device " + image.segment.owner.getName().toString() + " isn't implemented yet!");
						ErrorReporter.reporter.println();
						return;
					
					
				}
			}else{ // other technologies not implemented yet
				nofErrors++;
				ErrorReporter.reporter.error("MemoryWriter for Device " + image.segment.owner.getName().toString() + " isn't implemented yet!");
				ErrorReporter.reporter.println();
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

}
