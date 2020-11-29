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

package org.deepjava.classItems;

import org.deepjava.config.Segment;
import org.deepjava.host.Dbg;
import org.deepjava.linker.FixedValueEntry;
import org.deepjava.strings.HString;

public class Array extends RefType {
	public Array nextArray;	// all arrays are linked 
	public Array nextHigherDim;	// link to array with same type but next higher dimension
	public Array nextLowerDim;	// link to array with same type but next lower dimension

	public Type componentType;	// component type
	public byte dimension;	// array dimension

	public Segment segment;
	public FixedValueEntry typeDescriptor;	// reference to first item of array type descriptor (extension level)
	
	Array(HString regName, Type compType, int dimension) {
		super(regName, wktObject); // base type is java/lang/Object
		category = tcArray;
		sizeInBits = 32;	// array is of type reference type
		this.componentType = compType;
		this.dimension = (byte)dimension;
	}

	//--- debug primitives
	
	public void printShort(int indentLevel) {
		indent(indentLevel);
		vrb.printf("array %1$s, flags=", name);  Dbg.printAccAndPropertyFlags(accAndPropFlags, 'C');
	}
	
	public void printFields(int indentLevel) {
		indent(indentLevel);
		vrb.printf("array: (dim=%1$d, compType=%2$s, baseType=%3$s, nextHigherDim=%4$s, nextLowerDim=%5$s)\n", dimension, componentType.name, type.name, nextHigherDim!=null?nextHigherDim.name:"null", nextLowerDim!=null?nextLowerDim.name:"null");
	}

	public void printMethods(int indentLevel) {
		indent(indentLevel);
		vrb.println("methods: none");
	}

	public void print(int indentLevel) {
		printShort(indentLevel ); vrb.println();
		printFields(indentLevel+1 );
	}
}
