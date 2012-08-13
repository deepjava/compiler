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

package ch.ntb.inf.deep.ssa;

import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.ssa.instruction.SSAInstruction;

/**
 * @author   millischer
 */
public class SSAValue implements SSAValueType {
	public int type;
	public int index = -1;	// index into state array	
	public Item constant;	// contains reference to constant data
	public int n = -1;	// each ssa-instruction is numbered for the register allocation 
	public int end;	// indicates the end number of the live range for this value
	public int start = Integer.MAX_VALUE;	// indicates the start number of the live range for this value
	public int reg = -1;	// register or memory slot number
	public int regLong = -1;	// 2nd register or memory slot number for longs, contains upper 4 bytes
	public int regGPR1 = -1;	// auxiliary general purpose register 1, used for translating complex SSA instructions
	public int regGPR2 = -1;	// auxiliary general purpose register 2, used for translating complex SSA instructions 
	public int volRegs;	// stores information about volatiles which are used to produce this value
	public int memorySlot = -1;
	public SSAValue join;	// for resolving phi functions
	public SSAValue next;
	public boolean nonVol;	// value resides in volatile or nonvolatile register
	public SSAInstruction owner = null; //instruction which produce this value
	
	
	public SSAValue(){
	}
	
	public String typeName(){
		return svNames[type & 0x7fffffff];
	}
	
	@Override
	public String toString(){
		String r = svNames[type & 0x7fffffff];
		
		switch(type & 0x7fffffff){
		case tVoid://void
			r = n + " (" + r + ")";
			break;
		case tPhiFunc://PhiFunc
			r = r + "(" + n + ")";
			break;
		case tRef://ref
			r = n + " (" + r + ")";
			break;
		case tBoolean://Boolean
		case tChar://Char
		case tFloat://Float
		case tDouble://Double
		case tByte://Byte
		case tShort://Short
		case tInteger://Integer
		case tLong://Long
		case tAref://Aref
		case tAboolean://Aboolean
		case tAchar://Achar
		case tAfloat://Afloat
		case tAdouble://Adouble
		case tAbyte://Abyte
		case tAshort://Ashort
		case tAinteger://Ainteger
		case tAlong://Along
			r = n + " (" + r + ")";
			break;
		default:
			break;		
		
		}		
		return r;
	}
	
}
