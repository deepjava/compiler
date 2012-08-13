/*
 * Copyright (c) 2011 NTB Interstate University of Applied Sciences of Technology Buchs.
 *
 * http://www.ntb.ch/inf
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Eclipse Public License for more details.
 * 
 * Contributors:
 *     NTB - initial implementation
 * 
 */

package ch.ntb.inf.deep.cgPPC;

interface Registers {
	final int nofGPR = 32;	
	final int nofFPR = 32;
	
	final int topGPR = 31;	// highest numbered GPR
	final int topFPR = 31;	// highest numbered FPR
	
	final int paramStartGPR = 2;	// GPR with first parameter
	final int paramStartFPR = 1;	// FPR with first parameter
	
	final int paramEndGPR = 10;	// GPR with last parameter, must be < nonVolStartGPR
	final int paramEndFPR = 8;	// FPR with last parameter, must be < nonVolStartFPR
	
	final int nonVolStartGPR = 13;	// first nonvolatile GPR
	final int nonVolStartFPR = 13;	// first nonvolatile FPR
	
	final int returnGPR1 = 2;	// GPR with return value
	final int returnGPR2 = 3;	// GPR with return value used for longs
	final int returnFPR = 1;	// FPR with return value
	
	final int faux1 = 20;	// FPR which is never assigned and can be freely used 
	final int faux2 = 21;	// FPR which is never assigned and can be freely used 
	final int faux3 = 22;	// FPR which is never assigned and can be freely used 

	final int stackPtr = 1;	// register for stack pointer

	// initial mask for GPR's, '1' means register is free
	final int regsGPRinitial = 0xfffffffc;	
	// initial mask for FPR's, '1' means register is free
	final int regsFPRinitial = 0xfffffffe & ~(1<<faux1 | 1<<faux2 | 1<<faux3);	

	final boolean gpr = true;
	final boolean fpr = false;
	
}
