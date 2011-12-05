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

public interface SSAValueType {
	public static int //Type of SSAValue
	tVoid = 0, tPhiFunc = 2, tRef = 3, tBoolean = 4, tChar = 5,
	tFloat = 6, tDouble = 7, tByte = 8, tShort = 9,
	tInteger = 10, tLong = 11, tAref = 13, tAboolean =14,
	tAchar = 15, tAfloat = 16, tAdouble = 17, tAbyte = 18,
	tAshort = 19, tAinteger = 20, tAlong = 21;
	
	public static byte //attributes of types
	ssaTaFitIntoInt = 31;
	
	
	public static String[] svNames = {
		
		"Void",
		"",
		"Phi-Function",
		"Ref",
		"Boolean",
		"Char",
		"Float",
		"Double",
		"Byte",
		"Short",
		"Integer",
		"Long",
		"",
		"Ref-Array",
		"Boolean-Array",
		"Char-Array",
		"Float-Array",
		"Double-Array",
		"Byte-Array",
		"Short-Array",
		"Integer-Array",
		"Long-Array"		
	};

}
