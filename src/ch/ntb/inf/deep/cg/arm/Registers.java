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
	final int nofFPR = 16;
	
	final int topGPR = 15;	// highest numbered GPR
	final int topFPR = 15;	// highest numbered FPR
	
	final int paramStartGPR = 0;	// GPR with first parameter
	final int paramStartFPR = 1;	// FPR with first parameter
	
	final int paramEndGPR = 6;	// GPR with last parameter, must be < nonVolStartGPR
	final int paramEndFPR = 6;	// FPR with last parameter, must be < nonVolStartFPR
	
	final int nonVolStartGPR = 7;	// first nonvolatile GPR
	final int nonVolStartFPR = 7;	// first nonvolatile FPR

	// volEndGPR must be 1 lower than nonVolStartGPR
	final int volEndGPR = 8;	// last volatile GPR
	// volEndFPR must be 1 lower than nonVolStartFPR
	final int volEndFPR = 8;	// last volatile FPR

	final int returnGPR1 = 0;	// GPR with return value
	final int returnGPR2 = 1;	// GPR with return value used for longs
	final int returnFPR = 1;	// FPR with return value
	
	final int stackPtr = 13;	// register for stack pointer
	final int LR = 14;			// register for link register
	final int PC = 15;			// register for program counter

	// initial mask for GPR's, '1' means register is free
	final int regsGPRinitial = -1 & (~((1 << nonVolStartGPR) - 1) | ((1 << (volEndGPR+1)) - 1)) & ~(1 << stackPtr) & ~(1 << LR) & ~(1 << PC);
	// initial mask for FPR's, '1' means register is free
	final int regsFPRinitial = -1 & (~((1 << nonVolStartFPR) - 1) | ((1 << (volEndFPR+1)) - 1));	

	final boolean gpr = true;
	final boolean fpr = false;
	
}
