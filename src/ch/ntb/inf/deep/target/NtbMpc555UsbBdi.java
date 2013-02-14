package ch.ntb.inf.deep.target;

import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Parser;
import ch.ntb.inf.deep.config.Register;
import ch.ntb.inf.deep.config.RegisterInit;
import ch.ntb.inf.deep.config.RegisterInitList;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.TargetMemorySegment;
import ch.ntb.inf.libusbJava.USBException;
import ch.ntb.inf.mcdp.bdi.BDIException;
import ch.ntb.inf.mcdp.targets.mpc555.BDI;
import ch.ntb.inf.mcdp.usb.Device;
import ch.ntb.inf.mcdp.usb.DeviceFactory;

public class NtbMpc555UsbBdi extends TargetConnection {

	private static boolean dbg = false;

	private static final int nofGPRs = 32;
	private static final int defaultValue = 0;
	
	private static TargetConnection tc;
	private Device dev;
	private BDI bdi;
	private int memoryBaseAddress;
	private boolean flashErased = false;
	
	private NtbMpc555UsbBdi() {}
	
	public static TargetConnection getInstance() {
		if(tc != null && !tc.isConnected()){
			tc = null;
		}
		if (tc == null) {
			if(dbg) StdStreams.vrb.println("[TARGET] NtbMpc555UsbBdi: Creating new NtbMpc555UsbBdi... ");
			tc = new NtbMpc555UsbBdi();
			try {
				tc.openConnection();
			} catch (TargetConnectionException e) {
				tc = null;
			}
		}
		return tc;
	}
	
	@Override
	public synchronized void init() throws TargetConnectionException {
		memoryBaseAddress = Configuration.getMemoryBaseAddress();
		if(!isConnected()) {
			if(dbg) StdStreams.vrb.println("[TARGET] Connecting... ");
			openConnection();
		}
		if(dbg) StdStreams.vrb.println("[TARGET] Reseting... ");
		resetTarget();
	}

	@Override
	public synchronized void openConnection() throws TargetConnectionException {
		dev = DeviceFactory.getDevice();
		try {
			bdi = new BDI(dev);
			dev.open();
		} catch (USBException e) {
			if(dbg) StdStreams.vrb.println("FAILED");
			try {
				dev.close();
			} catch (USBException e1) {
				// nothing to do
			}
			throw new TargetConnectionException(e.getMessage(), e);
		}
	
	}

	@Override
	public synchronized void closeConnection() {
		try {
			dev.close();
		} catch (USBException e) {
			// do nothing
		}
	}

	@Override
	public synchronized boolean isConnected() {
		return dev.isOpen();
	}

	@Override
	public synchronized int getTargetState() {
		try {
			if(bdi.isFreezeAsserted()) return TargetConnection.stateDebug;
		} catch (BDIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return TargetConnection.stateUnknown;
	}

	@Override
	public synchronized void startTarget() throws TargetConnectionException {
		try {
			bdi.go();
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
	}

	@Override
	public synchronized void stopTarget() throws TargetConnectionException{
		try {
			bdi.break_();
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
	}

	@Override
	public synchronized void resetTarget() throws TargetConnectionException {
		try {
			// make a hard reset
			bdi.reset_target();
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
	}
	
	@Override
	public synchronized void initRegisters(RegisterInitList regInitList) throws TargetConnectionException {
		RegisterInit regInit = regInitList.getFirstRegInit();
		while(regInit != null) {
			initRegister(regInit);
			regInit = (RegisterInit)regInit.next;
		}
	}
	
	@Override
	public synchronized void initRegister(RegisterInit regInit) throws TargetConnectionException {
		setRegisterValue(regInit.getRegister(), regInit.getInitValue());
	}

	@Override
	public synchronized void setRegisterValue(String regName, int value) throws TargetConnectionException {
		Register reg = Configuration.getRegiststerByName(regName);
		if(reg != null) {
			setRegisterValue(reg, value);
		}
		// TODO add error message here
	}

	@Override
	public synchronized void setRegisterValue(Register reg, int value) throws TargetConnectionException {
		if(dbg) StdStreams.vrb.println("  Setting register " + reg.getName() + " to 0x" + Integer.toHexString(value));
		switch(reg.getType()) {
			case Parser.sGPR:
				setGprValue(reg.getAddress(), value);
				break;
			case Parser.sFPR:
				setFprValue(reg.getAddress(), value);
				break;
			case Parser.sSPR:
				setSprValue(reg.getAddress(), value);
				break;
			case Parser.sIOR:
				setIorValue(reg.getAddress(), value);
				break;
			case Parser.sMSR:
				setMsrValue(value);
				break;
			case Parser.sCR:
				setCrValue(value);
				break;
			case Parser.sFPSCR:
				setFpscrValue(value);
				break;
			default:
				// TODO add error message here
				break;
		}
	}

	@Override
	public synchronized void setGprValue(int gpr, int value) throws TargetConnectionException {
		try {
			bdi.writeGPR(gpr, value);
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
	}

	@Override
	public synchronized void setFprValue(int fpr, double value) throws TargetConnectionException {
		setFprValue(fpr, Double.doubleToLongBits(value));
	}

	@Override
	public synchronized void setFprValue(int fpr, long value) throws TargetConnectionException {
		int temp;
		try {
			temp = bdi.readMem(memoryBaseAddress, 4);
			bdi.writeFPR(fpr, memoryBaseAddress, value);
			bdi.writeMem(memoryBaseAddress, temp, 4);
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
	}

	@Override
	public synchronized void setSprValue(int spr, int value) throws TargetConnectionException {
		try {
			bdi.writeSPR(spr, value);
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
	}

	@Override
	public synchronized void setIorValue(int address, int value) throws TargetConnectionException {
		try {
			bdi.writeMem(address, value, 4);
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
	}

	@Override
	public synchronized int getRegisterValue(String regName) throws TargetConnectionException {
		Register reg = Configuration.getRegiststerByName(regName);
		if(reg != null) {
			return getRegisterValue(reg);
		}
		// TODO add error msg here
		return defaultValue;
	}

	@Override
	public synchronized int getRegisterValue(Register reg) throws TargetConnectionException {
		switch(reg.getType()) {
			case Parser.sGPR:
				return getGprValue(reg.getAddress());
			case Parser.sFPR:
				return (int)getFprValue(reg.getAddress());
			case Parser.sSPR:
				return getSprValue(reg.getAddress());
			case Parser.sIOR:
				return getIorValue(reg.getAddress());
			case Parser.sMSR:
				return getMsrValue();
			case Parser.sCR:
				return getCrValue();
			case Parser.sFPSCR:
				return getFpscrValue();
			default:
				// TODO add error msg here
				return defaultValue;
		}
	}

	@Override
	public synchronized long getRegisterValue64(String regName) throws TargetConnectionException {
		Register reg = Configuration.getRegiststerByName(regName);
		if(reg != null) {
			return getRegisterValue(reg);
		}
		// TODO add error msg here
		return defaultValue;
	}

	@Override
	public synchronized long getRegisterValue64(Register reg) throws TargetConnectionException {
		switch(reg.getType()) {
			case Parser.sGPR:
				return getGprValue(reg.getAddress());
			case Parser.sFPR:
				return getFprValue(reg.getAddress());
			case Parser.sSPR:
				return getSprValue(reg.getAddress());
			case Parser.sIOR:
				return getIorValue(reg.getAddress());
			case Parser.sMSR:
				return getMsrValue();
			case Parser.sCR:
				return getCrValue();
			case Parser.sFPSCR:
				return getFpscrValue();
			default:
				// TODO add error msg here
				return defaultValue;
		}
	}

	@Override
	public synchronized int getGprValue(int gpr) throws TargetConnectionException {
		int value = defaultValue;
		try {
			value = bdi.readGPR(gpr);
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
		return value;
	}

	@Override
	public synchronized long getFprValue(int fpr) throws TargetConnectionException {
		long value = 0;
		int temp;
		try {
			temp = bdi.readMem(memoryBaseAddress, 4);
			value = bdi.readFPR(fpr, memoryBaseAddress);
			bdi.writeMem(memoryBaseAddress, temp, 4);
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
		return value;
	}

	@Override
	public synchronized int getSprValue(int spr) throws TargetConnectionException {
		int value = defaultValue;
		try {
			value = bdi.readSPR(spr);
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
		return value;
	}

	@Override
	public synchronized int getIorValue(int address) throws TargetConnectionException {
		return readWord(address);
	}

	@Override
	public synchronized byte readByte(int address) throws TargetConnectionException {
		byte value = defaultValue;
		try {
			value = (byte)bdi.readMem(address, 1);
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
		return value;
	}
	
	@Override
	public synchronized short readHalfWord(int address) throws TargetConnectionException {
		short value = defaultValue;
		try {
			value = (short)bdi.readMem(address, 2);
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
		return value;
	}

	@Override
	public synchronized int readWord(int address) throws TargetConnectionException {
		int value = defaultValue;
		try {
			value = bdi.readMem(address, 4);
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
		return value;
	}

	@Override
	public synchronized void readFromAddress(int[] data, int address, int length) throws TargetConnectionException {
		try {
			if(length <= data.length) {
				for(int i = 0; i < length; i++) {
					data[i] = bdi.readMem(address + 4 * i, 4);
				}
			}
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
	}

	@Override
	public synchronized void writeByte(int address, byte data) throws TargetConnectionException {
		try {
			bdi.writeMem(address, data, 1);
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
	}
	
	@Override
	public synchronized void writeHalfWord(int address, short data) throws TargetConnectionException {
		try {
			bdi.writeMem(address, data, 2);
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
	}

	@Override
	public synchronized void writeWord(int address, int data) throws TargetConnectionException {
		try {
			bdi.writeMem(address, data, 4);
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
	}

	@Override
	public synchronized void writeToAddress(int[] data, int address, int length) throws TargetConnectionException {
		try {
			if(length <= data.length) {
				for(int i = 0; i < length; i++) {
					bdi.writeMem(address, data[i], 4);
				}
			}
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
	}
	
	@Override
	public synchronized void writeTMS(TargetMemorySegment tms) throws TargetConnectionException {
		Am29LV160dFlashWriter flashWriter = null;
		int count = 0;
		if(tms.segment.owner.getTechnology() == 0) { // RAM device
			int dataSizeToTransfer = tms.data.length;
			int startAddr = tms.startAddress;
			int index = 0;
			if(count%5 == 0){
				StdStreams.vrb.print('.');
			}
			while(dataSizeToTransfer > 0) {
				// limitation for fast download is 101 words
				int[] data;
				if(dataSizeToTransfer < 101) {
					data = new int[dataSizeToTransfer];
				}
				else {
					data = new int[100];
				}
				//StdStreams.vrb.print("data.length=" + data.length);
				//StdStreams.vrb.print("; data = {");
				for (int i = 0; i < data.length; i++) {
					data[i] = tms.data[index++];
					//StdStreams.vrb.print(String.format("0x%08X, ", data[i]));
				}
				//StdStreams.vrb.print("}; ");
				try {
					bdi.startFastDownload(startAddr);
					bdi.fastDownload(data, data.length);
					bdi.stopFastDownload();
//					int addr = startAddr;
//					for(int i = 0; i < data.length; i++) {
//						bdi.writeMem(addr, data[i], 4);
//						addr += 4;
//					}
				} catch (BDIException e) {
					e.printStackTrace();
				}
				dataSizeToTransfer -= data.length;
				startAddr += data.length * 4;
				//if(dataSizeToTransfer <= 0) StdStreams.vrb.println();
			}
			count++;
			if(tms.next == null || tms.next.segment.owner.getTechnology() != 0){
				StdStreams.vrb.println();
			}
		}
		else if(tms.segment.owner.getTechnology() == 1) { // Flash device
			if(tms.segment.owner.getMemoryType() == Configuration.AM29LV160D){
				flashWriter = new Am29LV160dFlashWriter(this);
				if(!flashErased){ // erase all used sectors
					TargetMemorySegment current = tms;
					// first mark all used sectors
					while(current != null && current.segment.owner.getMemoryType() == Configuration.AM29LV160D){
						current.segment.owner.markUsedSectors(current);
						current = current.next;
					}
					// second erase all marked sectors
					ch.ntb.inf.deep.config.Device[] devs = Configuration.getDevicesByType(Configuration.AM29LV160D);
					for(int i = 0; i < devs.length; i++) {
						if(devs[i] == null) System.out.println("ERROR: devs[" + i + "] == null");
						else flashWriter.eraseMarkedSectors(devs[i]);
					}
					flashErased = true;
					StdStreams.log.println("Programming flash");
				}
				// Programming flash
				if(!flashWriter.unlocked){
					flashWriter.unlockBypass(tms.segment.owner, true);
				}
				flashWriter.writeSequence(tms);
				if(tms.next == null || tms.next.segment.owner != tms.segment.owner && flashWriter.unlocked){
					flashWriter.unlockBypass(tms.segment.owner, false);
					StdStreams.log.println();						
				}
			}
			else{ // other memory type
					ErrorReporter.reporter.error(errMemWriterNotImplemented, "for Device " + tms.segment.owner.getName().toString());
					return;
			}
		}
		else { // unsupported
			// TODO add error message here
		}
	}

	@Override
	public synchronized void setBreakPoint(int address) throws TargetConnectionException {
		//1. Disable Instruction breakpoint interrupt
		Register der = Configuration.getRegiststerByName("DER");
		if(der == null)return;
		int derValue = getSprValue(der.getAddress()) & ~0x4;
		setSprValue(der.getAddress(), derValue);
		
		// 2. read ICTRL
		Register ictrl = Configuration.getRegiststerByName("ICTRL");
		if(ictrl == null)return;
		int ictrlValue = getSprValue(ictrl.getAddress());
	
		//3. decide which CMP is free an set it appropriate
		if((ictrlValue & 0x80000) == 0){//CMPA is free
			Register cmpa = Configuration.getRegiststerByName("CMPA");
			if(cmpa == null)return;
			setSprValue(cmpa.getAddress(), address);
			setSprValue(ictrl.getAddress(), ictrlValue | 0x80080800);//CTA = equals, IWP0 = match from CMPA, SIWP0EN = trap enabled
		}else if((ictrlValue & 0x20000) == 0){//CMPB is free
			Register cmpb = Configuration.getRegiststerByName("CMPB");
			if(cmpb == null)return;
			setSprValue(cmpb.getAddress(), address);
			setSprValue(ictrl.getAddress(), ictrlValue | 0x10020400);//CTB = equals, IWP1 = match from CMPB, SIWP1EN = trap enabled			
		}else if((ictrlValue & 0x8000) == 0){//CMPC is free
			Register cmpc = Configuration.getRegiststerByName("CMPC");
			if(cmpc == null)return;
			setSprValue(cmpc.getAddress(), address);
			setSprValue(ictrl.getAddress(), ictrlValue | 0x2008200);//CTC = equals, IWP2 = match from CMPC, SIWP2EN = trap enabled
		}else if((ictrlValue & 0x2000) == 0){//CMPD is free
			Register cmpd = Configuration.getRegiststerByName("CMPD");
			if(cmpd == null)return;
			setSprValue(cmpd.getAddress(), address);
			setSprValue(ictrl.getAddress(), ictrlValue | 0x402100);//CTD = equals, IWP3 = match from CMPD, SIWP3EN = trap enabled
		}
		
		//4. Enable instruction breakpoint interrupt
		setSprValue(der.getAddress(), derValue | 0x4);	
	}

	@Override
	public synchronized void removeBreakPoint(int address) throws TargetConnectionException {
		//1. Disable Instruction breakpoint interrupt
		Register der = Configuration.getRegiststerByName("DER");
		if(der == null)return;
		int derValue = getSprValue(der.getAddress()) & ~0x4;
		setSprValue(der.getAddress(), derValue);
		
		// 2. read ICTRL
		Register ictrl = Configuration.getRegiststerByName("ICTRL");
		if(ictrl == null)return;
		int ictrlValue = getSprValue(ictrl.getAddress());
		
		//3. find correct CMP
		if((ictrlValue & 0x80000) != 0){//CMPA is used
			Register cmpa = Configuration.getRegiststerByName("CMPA");
			if(cmpa == null)return;
			int cmpaValue = getSprValue(cmpa.getAddress());
			if(cmpaValue == address){
				setSprValue(ictrl.getAddress(), ictrlValue & ~0xE00C0800);
				//4. Enable instruction breakpoint interrupt
				setSprValue(der.getAddress(), derValue | 0x4);
				return;
			}
		}
		if((ictrlValue & 0x20000) != 0){//CMPB is used
			Register cmpb = Configuration.getRegiststerByName("CMPB");
			if(cmpb == null)return;
			int cmpbValue = getSprValue(cmpb.getAddress());
			if(cmpbValue == address){
				setSprValue(ictrl.getAddress(), ictrlValue & ~0x1C030400);
				//4. Enable instruction breakpoint interrupt
				setSprValue(der.getAddress(), derValue | 0x4);
				return;
			}
			
		}
		if((ictrlValue & 0x8000) != 0){//CMPC is used
			Register cmpc = Configuration.getRegiststerByName("CMPC");
			if(cmpc == null)return;
			int cmpcValue = getSprValue(cmpc.getAddress());
			if(cmpcValue == address){
				setSprValue(ictrl.getAddress(), ictrlValue & ~0x38C200);
				//4. Enable instruction breakpoint interrupt
				setSprValue(der.getAddress(), derValue | 0x4);
				return;
			}
			
		}
		if((ictrlValue & 0x2000) != 0){//CMPD is used
			Register cmpd = Configuration.getRegiststerByName("CMPD");
			if(cmpd == null)return;
			int cmpdValue = getSprValue(cmpd.getAddress());
			if(cmpdValue == address){
				setSprValue(ictrl.getAddress(), ictrlValue & ~0x73100);
				//4. Enable instruction breakpoint interrupt
				setSprValue(der.getAddress(), derValue | 0x4);
				return;
			}
		}
		
		//4. Enable instruction breakpoint interrupt
		setSprValue(der.getAddress(), derValue | 0x4);	
	}

	@Override
	public synchronized void confirmBreakPoint(int address) throws TargetConnectionException {
		//1. Disable Instruction breakpoint interrupt
		Register der = Configuration.getRegiststerByName("DER");
		if(der == null)return;
		int derValue = getSprValue(der.getAddress()) & ~0x4;
		setSprValue(der.getAddress(), derValue);
		
		// 2. read ICTRL
		Register ictrl = Configuration.getRegiststerByName("ICTRL");
		if(ictrl == null)return;
		int ictrlValue = getSprValue(ictrl.getAddress());
		
		// 3. set ignore first match on i-bus
		setSprValue(ictrl.getAddress(), ictrlValue | 0x8);		
		
		//4. Enable instruction breakpoint interrupt
		setSprValue(der.getAddress(), derValue | 0x4);	
	}

	
	/* private methods */
	
	private void clearGPRs() throws TargetConnectionException {
		for(int i = 0; i < nofGPRs; i++) {
			setGprValue(i, 0);
		}
	}

	private synchronized int getMsrValue() throws TargetConnectionException {
		int value = 0;
		try {
			value = bdi.readMSR();
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
		return value;
	}
	
	private synchronized int getFpscrValue() throws TargetConnectionException {
		int value = 0;
		try {
			value = bdi.readFPSCR();
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
		return value;
	}
	
	private synchronized int getCrValue() throws TargetConnectionException {
		int value = 0;
		try {
			value = bdi.readCR();
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
		return value;
	}

	private synchronized int setMsrValue(int value) throws TargetConnectionException {
		try {
			bdi.writeMSR(value);
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
		return value;
	}
	
	private synchronized int setFpscrValue(int value) throws TargetConnectionException {
		try {
			bdi.writeFPSCR(value);
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
		return value;
	}
	
	private synchronized int setCrValue(int value) throws TargetConnectionException {
		try {
			bdi.writeCR(value);
		} catch (BDIException e) {
			throw new TargetConnectionException(e.getMessage(), e);
		}
		return value;
	}

	@Override
	public int getNofGpr() {
		return nofGPRs;
	}
}
