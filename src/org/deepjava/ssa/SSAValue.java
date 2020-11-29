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

package org.deepjava.ssa;

import org.deepjava.classItems.Item;
import org.deepjava.ssa.instruction.SSAInstruction;

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
	public SSAValue join;	// for resolving phi functions
	public SSAValue next;	// linked list for join values
	public boolean nonVol;	// value resides in volatile or nonvolatile register
	public SSAInstruction owner = null; //instruction which produces this value
	
	
	public SSAValue() {
	}
	
	public String typeName() {
		return svNames[type & 0x7fffffff];
	}
	
	@Override
	public String toString() {
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
