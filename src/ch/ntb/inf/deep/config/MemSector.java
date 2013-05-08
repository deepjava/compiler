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

package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.strings.HString;

public class MemSector extends Item {
	
	public boolean used = false;
	int size = 0;
	
	public MemSector(String jname) {
		this.name = HString.getRegisteredHString(jname);
	}
	
	public MemSector(String jname, int baseAddress, int size) {
		this.name = HString.getRegisteredHString(jname);
		this.address = baseAddress;
		this.size = size;
	}
	
	public void print(int indentLevel) {
		indent(indentLevel);
		vrb.print("sector = " + name.toString() + " {");
		vrb.print("base = 0x" + Integer.toHexString(address) + ", ");
		vrb.println("sectorsize = 0x" + Integer.toHexString(size) + "}");
	}

}
