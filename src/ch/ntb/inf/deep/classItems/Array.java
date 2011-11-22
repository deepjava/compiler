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
import ch.ntb.inf.deep.debug.Dbg;
import ch.ntb.inf.deep.strings.HString;

public class Array extends Type {
	public Array nextArray;

	public Type componentType;
	public byte dimension; // array dimension

	public Segment segment;
	public int[] typeDescriptor;
	
	Array(HString regName){
		super(regName, wktObject); // base type is java/lang/Object
		category = tcArray;
		sizeInBits = 32;

		dimension = 1;
		while(regName.charAt(dimension) == tcArray) dimension++;
		HString sname = regName.substring(dimension);
		sname = stab.insertCondAndGetEntry(sname);
		componentType = getTypeByDescriptor(sname);
		
		if( regName.length() == 2 ){
			assert dimension == 1;
			int typeIndex = getPrimitiveTypeIndex(sname.charAt(0));
			if( typeIndex >= txBoolean){// if (one dimensional array of primitve type) register it in primTypeArrays
				primTypeArrays[typeIndex] = this;
			}
		}
	}

	protected void selectAndMoveInitClasses(){
	}

	//--- debug primitives
	
	public void printShort(int indentLevel){
		indent(indentLevel);
		vrb.printf("array %1$s, flags=", name);  Dbg.printAccAndPropertyFlags(accAndPropFlags, 'C');
	}
	
	public void printFields(int indentLevel){
		indent(indentLevel);
		vrb.printf("array: (dim=%1$d, compType=%2$s, baseType=%3$s)\n", dimension, componentType.name, type.name);
	}

	public void printMethods(int indentLevel){
		indent(indentLevel);
		vrb.println("methods: none");
	}
}
