package ch.ntb.inf.deep.cgPPC;

interface Registers {
	final int nofGPR = 32;
	final int nofFPR = 32;
	
	final int topGPR = 31;
	final int topFPR = 31;
	
	final int paramStartGPR = 2;
	final int paramStartFPR = 1;
	
	final int paramEndGPR = 10;	// must be < nonVolStartGPR
	final int paramEndFPR = 8;	// must be < nonVolStartFPR
	
	final int nonVolStartGPR = 13;
	final int nonVolStartFPR = 13;
	
	final int returnGPR1 = 2;
	final int returnGPR2 = 3;
	final int returnFPR = 1;

	final int stackPtr = 1;	// register for stack pointer

	final int regsGPRinitial = 0xfffffffc;
	final int regsFPRinitial = 0xfffffffe;

	final boolean gpr = true;
	final boolean fpr = false;

}
