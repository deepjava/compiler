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
	
	final int paramStartGPR = 2;	// GPR with first parameter
	final int paramStartFPR = 1;	// FPR with first parameter
	
	final int paramEndGPR = 7;	// GPR with last parameter, must be < nonVolStartGPR
	final int paramEndFPR = 6;	// FPR with last parameter, must be < nonVolStartFPR
	
	final int nonVolStartGPR = 8;	// first nonvolatile GPR
	final int nonVolStartFPR = 8;	// first nonvolatile FPR
	
	final int returnGPR1 = 2;	// GPR with return value
	final int returnGPR2 = 3;	// GPR with return value used for longs
	final int returnFPR = 1;	// FPR with return value
	
	final int faux1 = 8;	// FPR which is never assigned and can be freely used 
	final int faux2 = 9;	// FPR which is never assigned and can be freely used 
	final int faux3 = 10;	// FPR which is never assigned and can be freely used 

	final int stackPtr = 14;	// register for stack pointer

	// initial mask for GPR's, '1' means register is free
	final int regsGPRinitial = 0xfffffffc;	
	// initial mask for FPR's, '1' means register is free
	final int regsFPRinitial = 0xfffffffe & ~(1<<faux1 | 1<<faux2 | 1<<faux3);	

	final boolean gpr = true;
	final boolean fpr = false;
	
}
