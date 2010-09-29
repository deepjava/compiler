package ch.ntb.inf.deep.cgPPC;

interface Registers {
	final int TopGPR = 31;
	final int TopFPR = 31;
	
	final int returnGPR1 = 2;
	final int returnGPR2 = 3;
	final int returnFPR = 2;
	
	final int volRegsGPRinitial = 0x00001ffc;
	final int nonVolRegsGPRinitial = 0xffffe000;
	final int volRegsFPRinitial = 0x000001fe;
	final int nonVolRegsFPRinitial = 0xfffffe00;

}
