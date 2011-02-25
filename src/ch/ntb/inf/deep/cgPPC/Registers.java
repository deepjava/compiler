package ch.ntb.inf.deep.cgPPC;

interface Registers {
	final int nofGPR = 32;
	final int nofFPR = 32;
	
	final int topGPR = 31;
	final int topFPR = 31;
	
	final int paramStartGPR = 2;
	final int paramStartFPR = 1;
	
	final int returnGPR1 = 2;
	final int returnGPR2 = 3;
	final int returnFPR = 1;

	final int stackPtr = 1;	// register for stack pointer

	final int volRegsGPRinitial = 0x0003fffc;
	final int nonVolRegsGPRinitial = ~volRegsGPRinitial & 0xfffffffc;
	final int volRegsFPRinitial = 0x00001ffe;
	final int nonVolRegsFPRinitial = ~volRegsFPRinitial & 0xfffffffe;

	final boolean gpr = true;
	final boolean fpr = false;

}
