/*
 * Copyright 2011 - 2013 NTB University of Applied Sciences in Technology
 * Buchs, Switzerland, http://www.ntb.ch/inf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package ch.ntb.inf.deep.cg.arm;

interface Registers {
	final int nofGPR = 16;	
	final int nofEXTR = 32;
	
	final int topGPR = 12;	// highest numbered GPR except PC, LR, SP
	final int topEXTR = 31;	// highest numbered EXTR
	
	final int paramStartGPR = 0;	// GPR with first parameter
	final int paramStartEXTR = 0;	// EXTR with first parameter
	
	final int paramEndGPR = 5;	// GPR with last parameter, must be < nonVolStartGPR
	final int paramEndEXTR = 6;	// EXTR with last parameter, must be < nonVolStartEXTR
	
	final int nonVolStartGPR = 7;	// first nonvolatile GPR
	final int nonVolStartEXTR = 11;	// first nonvolatile EXTR

	final int volEndGPR = 5;	// last volatile GPR
	final int volEndEXTR = 10;	// last volatile EXTR

	final int returnGPR1 = 0;	// GPR with return value
	final int returnGPR2 = 1;	// GPR with return value used for longs
	final int returnEXTR = 1;	// EXTR with return value
	
	final int scratchReg = 6;		// scratch register
	final int stackPtr = 13;	// register for stack pointer
	final int LR = 14;			// register for link register
	final int PC = 15;			// register for program counter
	
	final int scratchRegEXTR = 0;		// scratch register

	// initial mask for GPR's, '1' means register is free
	final int regsGPRinitial = 0xffff & (~((1 << nonVolStartGPR) - 1) | ((1 << (volEndGPR+1)) - 1)) & ~(1 << stackPtr) & ~(1 << LR) & ~(1 << PC);
	// initial mask for EXTR's, double precision, '1' means register is free
	final int regsEXTRDinitial = 0xffffffff & (~((1 << nonVolStartEXTR) - 1) | ((1 << (volEndEXTR + 1)) - 1)) & ~(1 << scratchRegEXTR);	
	// initial mask for EXTR's, single precision, '1' means register is free
	final int regsEXTRSinitial = 0xffffffff & (~((1 << nonVolStartEXTR) - 1) | ((1 << (volEndEXTR + 1)) - 1)) & ~(3 << scratchRegEXTR);	

	final boolean gpr = true;
	final boolean extr = false;
	
}
