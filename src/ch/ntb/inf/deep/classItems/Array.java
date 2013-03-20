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

package ch.ntb.inf.deep.classItems;

import ch.ntb.inf.deep.config.Segment;
import ch.ntb.inf.deep.host.Dbg;
import ch.ntb.inf.deep.linker.FixedValueEntry;
import ch.ntb.inf.deep.strings.HString;

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
