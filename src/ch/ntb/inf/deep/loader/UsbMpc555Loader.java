package ch.ntb.inf.deep.loader;

import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Parser;
import ch.ntb.inf.deep.config.Register;
import ch.ntb.inf.deep.linkerPPC.Linker;
import ch.ntb.inf.deep.linkerPPC.TargetMemorySegment;
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

	/**
	 * Target
	 */
	private BDI mpc;

	/**
	 * USB Device
	 */
	private Device dev;
	
	private UsbMpc555Loader(){		
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see ch.ntb.mpc555.register.controller.javaEnv.crosscompiler.download.Downloader#init()
	 */
	@Override
	public synchronized void init() throws DownloaderException {
		baseAddress = Configuration.getValueFor(HString.getHString("IMB"));
		System.out.println("++++++++ Open Device!+++++++++");
		
		//check if connection is open
		if(!this.isConnected()){
			this.openConnection();
		}
		System.out.println("++++++++ Reset Target!+++++++++");
		this.resetTarget();
	
		System.out.println("++++++++ init Registers and write Code!+++++++++");
		// initialize Memory
		initRegisters();

		// clear the GPRs
		clearGPRs();

		/*//TODO remove this its only for testing*****************
		long b = Long.valueOf(0x4004000000000000l); //2.5 
		setFPR(0, b);
		setFPR(1, b);
		setFPR(2, b);
		setFPR(3, b);
		setFPR(4, b);
		setFPR(5, b);
		setFPR(6, b);
		setFPR(7, b);
		setFPR(8, b);		
		//********************************************************
		*/
		// Write the code down
		writeCode();
		System.out.println("++++++++ Download finished!+++++++++");
		
		

	}
	
	public static UsbMpc555Loader getInstance(){
		if(loader == null){
			loader = new UsbMpc555Loader();
			try {
				// open Usb-Connection
				loader.openConnection();
				
				// Make a reset on target
				loader.resetTarget();
			} catch (DownloaderException e) {
				e.printStackTrace();
			}
		}
		return loader;
	}
	
	// /**
	// * Parse a Ramimage and write the code into the memory
	// *
	// * @param path
	// * @throws FileNotFoundException
	// */
	// protected synchronized void parseAndWriteCode(String path) {
	// String s;
	// int count =0;
	// int memAddr=0;
	//		
	// try {
	// FileReader fr = new FileReader(path);
	// BufferedReader br = new BufferedReader(fr);
	// while ((s = br.readLine()) != null) {
	//				
	// if(s.charAt(0)!='\t'){
	// s = s.substring(1);
	// memAddr= Integer.decode(s);
	// }else{
	// s=s.substring(2);
	// setMem(memAddr, Integer.decode(s), 4);
	// count++;
	// memAddr=memAddr+4;
	// }
	// }
	// System.out.println(count);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// // while (dis.available() > 0) {
	// // code.add(dis.readInt());
	// // }
	// catch (NumberFormatException e) {
	// System.err.println("Count "+ count);
	// e.printStackTrace();
	// } catch (DownloaderException e) {
	// e.printStackTrace();
	// }
	//
	// }

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
		// get code from the Linker
		if (!isFreezeAsserted()) {
			System.out.println("Bdi is not in Debug mode!");
		}
		TargetMemorySegment image = Linker.targetImage;
		while (image != null) {
			// TODO remove Hack, solve it proper!!!!!
			int dataSizeToTransfer = image.data.length;
			int startAddr = image.startAddress;
			int index = 0;
			while (dataSizeToTransfer > 0) {
				//limitation for fast downlod is 101 Words
				int[] data = new int[100];
				if(dataSizeToTransfer < 101){
					data = new int[dataSizeToTransfer];
				}
				for(int i = 0; i < data.length; i++){
					data[i]= image.data[index++];
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
			Register root = Configuration.getInitializedRegisters();
			Register current = root;
			while (current != null) {
				switch (current.getType()) {
				case Parser.sSPR:
					mpc.writeSPR(current.getAddress(), current.getInit()
							.getValue());
					break;
				case Parser.sIOR:
					mpc.writeMem(current.getAddress(), current.getInit()
							.getValue(), current.getSize());
					break;
				case Parser.sMSR:
					mpc.writeMSR(current.getInit().getValue());
					break;
				case Parser.sCR:
					mpc.writeCR(current.getInit().getValue());
					break;
				case Parser.sFPSCR:
					mpc.writeFPSCR(current.getInit().getValue());
					break;
				default:
					break;
				}
				current = current.nextWithInitValue;
			}

			// Check if all is set fine
			current = root;
			while (current != null) {
				switch (current.getType()) {
				case Parser.sSPR:
					checkValue(mpc.readSPR(current.getAddress()), current
							.getInit().getValue());
					break;
				case Parser.sIOR:
					if(!current.getName().equals(HString.getHString("RSR")))
					checkValue(mpc.readMem(current.getAddress(), current
							.getSize()), current.getInit().getValue());
					break;
				case Parser.sMSR:
					checkValue(mpc.readMSR(), current.getInit().getValue());
					break;
				case Parser.sCR:
					checkValue(mpc.readCR(), current.getInit().getValue());
					break;
				case Parser.sFPSCR:
					checkValue(mpc.readFPSCR(), current.getInit().getValue());
					break;
				default:
					break;
				}
				current = current.nextWithInitValue;

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
			/*
			 * Do not check the freeze signal on the target because it is set
			 * only after the following memory writes
			 */
			boolean cf = mpc.isCheckFreezeOnTarget();
			mpc.setCheckFreezeOnTarget(false);
			// assign pin to Freeze output
			mpc.writeMem(0x02FC000, 0x40000, 4);
			// enable bus monitor,disable watchdog timer
			mpc.writeMem(0x02FC004, 0x0FFFFFF83, 4);
			// SCCR, switch off EECLK for download
			mpc.writeMem(0x02FC280, 0x08121C100, 4);
			mpc.setCheckFreezeOnTarget(cf);
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
		System.out.println("++++++++ Start Target!+++++++++");
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
		//RegisterMap regMap = Configuration.getRegisterMap();
		//int[] gprs = new int[regMap.getNofGprs()]; TODO this it the orignal line
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
		//for (int i = 0; i < Configuration.getRegisterMap().getNofGprs(); i++) {TODO this it the orignal line
		for (int i = 0; i < 32; i++){
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
			// System.out.println("USB dev.open()");
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
