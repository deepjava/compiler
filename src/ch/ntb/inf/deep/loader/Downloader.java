package ch.ntb.inf.deep.loader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * Download Module<br>
 * This class is the basis for all download modules.
 */
public abstract class Downloader {

	/**
	 * Hold the the start address for the program
	 */
	
	protected static int baseAddress;

		
	/**
	 * Filename of a bin file, if not NULL the code will be read directly from
	 * this file.
	 */
	protected String filename;

	/**
	 * File handle if bin file is opened
	 */
	private FileInputStream fis;

//	/**
//	 * Buffer for reading from a open bin file
//	 */
//	private final byte[] binInstr;
//
//
//	/**
//	 * Constructor
//	 */
//	public Downloader() {
//		binInstr = new byte[4];
//	}


	/**
	 * Initialization
	 * 
	 * @param filename Filename of the bin file
	 * @throws DownloaderException
	 */
	public void init(String filename) throws DownloaderException {
		this.filename = filename;
		init();
	}


	/**
	 * Initialization
	 * 
	 * @throws DownloaderException
	 */
	public abstract void init() throws DownloaderException;


	/**
	 * Write program code to the target
	 * @throws DownloaderException
	 */
	protected abstract void writeCode() throws DownloaderException;


	/**
	 * Initialized the memory.
	 * @throws DownloaderException
	 */
	protected abstract  void initRegisters() throws DownloaderException;


	/**
	 * Reset target
	 * @throws DownloaderException
	 */
	public abstract void resetTarget() throws DownloaderException;


	/**
	 * Start target
	 * @throws DownloaderException
	 */
	public abstract void startTarget() throws DownloaderException;


	/**
	 * Stop target
	 * @throws DownloaderException
	 */
	public abstract void stopTarget() throws DownloaderException;


	/**
	 * Does some actions after an internal breakpoint is detected
	 * 
	 * @throws DownloaderException
	 */
	public abstract void internalBreakpoint() throws DownloaderException;


	/**
	 * Close connection to target
	 */
	public abstract void closeConnection();


	/**
	 * Returns true if the downloader is connected.
	 * 
	 * @return True if the downloader is connected
	 */
	public abstract boolean isConnected();


	/**
	 * Open a connection to the target
	 * 
	 * @throws DownloaderException
	 */
	public abstract void openConnection() throws DownloaderException;


	/**
	 * Check if target is freezed
	 * 
	 * @return State of the freeze signal
	 * @throws DownloaderException
	 */
	public abstract boolean isFreezeAsserted() throws DownloaderException;


	/**
	 * Read the GPRs
	 * 
	 * @return List of GPRs and its values
	 * @throws DownloaderException
	 */
	public abstract int[] readGPRs() throws DownloaderException;


	/**
	 * Set the GPRs
	 * 
	 * @param gprs Map with register numbers and values
	 * @throws DownloaderException
	 */
	public abstract void setGPRs(int[][] gprs) throws DownloaderException;


	/**
	 * Set the value of one GPR
	 * 
	 * @param no Number
	 * @param value Value
	 * @throws DownloaderException
	 */
	public abstract void setGPR(int no, int value) throws DownloaderException;


	/**
	 * Get the value of one GPR
	 * 
	 * @param no Number
	 * @return Value
	 * @throws DownloaderException
	 */
	public abstract int getGPR(int no) throws DownloaderException;


	/**
	 * Read the CTR
	 * 
	 * @return CTR value
	 * @throws DownloaderException
	 */
	public abstract int getCR() throws DownloaderException;


	/**
	 * Set the CTR
	 * 
	 * @param value CTR
	 * @throws DownloaderException
	 */
	public abstract void setCR(int value) throws DownloaderException;


	/**
	 * Set a SPR
	 * 
	 * @param no Number
	 * @param value Value
	 * @throws DownloaderException
	 */
	public abstract void setSPR(int no, int value) throws DownloaderException;


	/**
	 * Get the value of a SPR
	 * 
	 * @param no Number
	 * @return Value
	 * @throws DownloaderException
	 */
	public abstract int getSPR(int no) throws DownloaderException;


	/**
	 * Set a memory address
	 * 
	 * @param addr Address
	 * @param value Value
	 * @param size Number of bytes
	 * @throws DownloaderException
	 */
	public abstract void setMem(int addr, int value, int size) throws DownloaderException;


	/**
	 * Get the value of a memory address
	 * 
	 * @param addr Address
	 * @param size Number of bytes
	 * @return Value
	 * @throws DownloaderException
	 */
	public abstract int getMem(int addr, int size) throws DownloaderException;

	/**
	 * Get the machine state register
	 * 
	 * @return Value
	 * @throws DownloaderException
	 */
	public abstract int getMSR() throws DownloaderException;

	/**
	 * Set the machine state register to the given value
	 * 
	 * @param value set to
	 * @throws DownloaderException
	 */
	public abstract void setMSR(int value) throws DownloaderException;
	
	/**
	 * Get the value of the floatingpointregister
	 * @param no register number
	 * @return value
	 * @throws DownloaderException
	 */
	public abstract long getFPR(int no) throws DownloaderException;
	
	/**
	 * Set the value of the floatingpointregister
	 * @param no registernumber
	 * @param value value to set
	 * @throws DownloaderException
	 */
	public abstract void setFPR(int no, long value) throws DownloaderException;
	
	/**
	 * Get the value of the Floatingpoint state an condition register
	 * @return value
	 * @throws DownloaderException
	 */
	public abstract int getFPSCR() throws DownloaderException;
	
	/**
	 * set Breakpoint at given address
	 * @param memAddr
	 */
	public abstract void setBreakpoint(int memAddr) throws DownloaderException;
	
	/**
	 * confirms the Breakpoint at given address
	 * @param memAddr
	 */
	public abstract void confirmBreakpoint() throws DownloaderException;
	

	/**
	 * removes Breakpoint from given address
	 * @param memAddr
	 */
	public abstract void removeBreakpoint(int memAddr) throws DownloaderException;
	
	/**
	 * Get the Value of the Exception Cause Register
	 * @return
	 * @throws DownloaderException 
	 */
	public abstract int getECR() throws DownloaderException; 

	/**
	 * Get the Value of the Program Counter
	 * @return
	 * @throws DownloaderException 
	 */
	public abstract int getPC() throws DownloaderException; 

	/**
	 * Get the Value of the Stackpointer
	 * @return
	 * @throws DownloaderException 
	 */
	public abstract int getSP() throws DownloaderException; 
	
	/**
	 * set the stepping mode
	 * @param mode 0 = off, 1 = Branch trace, 2 = single step, 3 = both modes
	 * @throws DownloaderException 
	 */
	public abstract void setStepping(int mode) throws DownloaderException; 
	
//	/**
//	 * Opens the bin file for reading
//	 * 
//	 * @throws FileNotFoundException
//	 */
//	protected void openBinFile() throws FileNotFoundException {
//		if(filename != null) {
//			fis = new FileInputStream(filename);
//		}
//	}
//
//
//	/**
//	 * Closes the bin file
//	 * 
//	 * @throws IOException
//	 */
//	protected void closeBinFile() throws IOException {
//		if(fis != null) {
//			fis.close();
//		}
//	}


//	/**
//	 * Reads an instruction directly from the bin file
//	 * 
//	 * @return Instruction
//	 * @throws IOException
//	 */
//	protected Integer readFromBinFile() throws IOException {
//		if(fis != null && fis.available() >= binInstr.length) {
//			fis.read(binInstr);
//			return ((binInstr[0] << 24) & 0xff000000) + ((binInstr[1] << 16) & 0xff0000) + ((binInstr[2] << 8) & 0xff00) + (binInstr[3] & 0xff);
//		}
//		return null;
//	}
}
