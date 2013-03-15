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

import ch.ntb.inf.deep.strings.HString;

public class InterfaceTabEntry extends Method {
	public static final HString interfTabEntryName = Item.stab.insertCondAndGetEntry("#");
	public static final int lastInterfTabEntryId = 0;

	/**
	 * Generate a stub to define the end entry of an interface table.
	 * @param interfaceId
	 * @param baseIndex index of the base method of the interface
	 */
	public InterfaceTabEntry( Class interf, int baseIndex ){
		super( interfTabEntryName ); // name = interfTabEntryName, type = null, methDescriptor = null;
		this.owner = interf;
		this.index = (interf.index<<16) | baseIndex;
	}

	public void setLastTabEntry(){
		index = (index & (1<<16)-1) | (lastInterfTabEntryId<<16);
	}
	
	//--- debug primitives
	public void printHeaderX(int indentLevel){
		indent(indentLevel);
		vrb.printf("T<%1$d|%2$d> %3$s.%4$s", index>>>16, index&(1<<16)-1, owner.name, name);  
	}
}
