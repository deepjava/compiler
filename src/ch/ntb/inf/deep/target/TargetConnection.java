package ch.ntb.inf.deep.target;

import ch.ntb.inf.deep.config.Register;
import ch.ntb.inf.deep.config.RegisterInit;
import ch.ntb.inf.deep.config.RegisterInitList;
import ch.ntb.inf.deep.linker.TargetMemorySegment;

public abstract class TargetConnection {
		
	public static final int errTargetNotFound = 800,
			errDownloadFailed = 801,
			errNoTargetImage = 802,
			errConnectionLost = 803,
			errReopenFailed = 804,
			errStartTargetFailed = 805,
			errTargetNotInDebugMode = 806,
			errMemWriterNotImplemented = 807,
			errBypassNotUnlocked = 808,
			errProgrammFailed = 809;
	public static final byte stateRunning = 0x01;
	public static final byte stateStopped = 0x02;
	public static final byte stateDebug = 0x03;
	public static final byte stateUnknown = -1; // 0xff
			
	public abstract void init() throws TargetConnectionException;
	
	public abstract void openConnection() throws TargetConnectionException;
	
	public abstract void closeConnection();
	
	public abstract boolean isConnected();

	public abstract int getTargetState() throws TargetConnectionException;
	
	public abstract void startTarget() throws TargetConnectionException;
	
	public abstract void stopTarget() throws TargetConnectionException;
	
	public abstract void resetTarget() throws TargetConnectionException;
	
	public abstract void initRegister(RegisterInit registerInit) throws TargetConnectionException;

	public abstract void initRegisters(RegisterInitList registerInitList) throws TargetConnectionException;
	
	public abstract void setRegisterValue(String regName, int value) throws TargetConnectionException;
	
	public abstract void setRegisterValue(Register reg, int value) throws TargetConnectionException;
	
	public abstract void setGprValue(int gpr, int value) throws TargetConnectionException;
	
	public abstract void setFprValue(int fpr, double value) throws TargetConnectionException;

	public abstract void setFprValue(int fpr, long value) throws TargetConnectionException;

	public abstract void setSprValue(int spr, int value) throws TargetConnectionException;
	
	public abstract void setIorValue(int ior, int value) throws TargetConnectionException;
	
	public abstract int getRegisterValue(String regName) throws TargetConnectionException;
	
	public abstract int getRegisterValue(Register reg) throws TargetConnectionException;
	
	public abstract long getRegisterValue64(String regName) throws TargetConnectionException;
	
	public abstract long getRegisterValue64(Register reg) throws TargetConnectionException;
	
	public abstract int getGprValue(int gpr) throws TargetConnectionException;
	
	public abstract long getFprValue(int gpr) throws TargetConnectionException;
	
	public abstract int getSprValue(int spr) throws TargetConnectionException;
	
	public abstract int getIorValue(int ior) throws TargetConnectionException;
		
	public abstract byte readByte(int address) throws TargetConnectionException;
	
	public abstract short readHalfWord(int address) throws TargetConnectionException;
	
	public abstract int readWord(int address) throws TargetConnectionException;
	
	public abstract void readFromAddress(int[] data, int address, int length) throws TargetConnectionException;
	
	public abstract void writeByte(int address, byte data) throws TargetConnectionException;
	
	public abstract void writeHalfWord(int address, short data) throws TargetConnectionException;
	
	public abstract void writeWord(int address, int data) throws TargetConnectionException;
	
	public abstract void writeToAddress(int[] data, int address, int length) throws TargetConnectionException;
	
	public abstract void writeTMS(TargetMemorySegment tms) throws TargetConnectionException;
	
	public abstract void setBreakPoint(int address) throws TargetConnectionException;
	
	public abstract void removeBreakPoint(int address) throws TargetConnectionException;
	
	public abstract void confirmBreakPoint(int address) throws TargetConnectionException;
	
	public abstract int getNofGpr();

}
