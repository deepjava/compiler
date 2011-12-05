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
