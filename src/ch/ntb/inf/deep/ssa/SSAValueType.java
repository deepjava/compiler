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
