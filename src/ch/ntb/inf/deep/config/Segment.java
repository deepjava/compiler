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
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class Segment extends Item {
	public Device owner;
	public Segment parent;
	public int attributes = 0;
	public int size = 0;
	public int usedSize = 0;
	public int width = 0;
	
	public Segment(String jname, Device owner){
		this.name = HString.getRegisteredHString(jname);
		this.owner = owner;
	}
	
	public void addAttributes(int attributes) {
		this.attributes |= attributes;
	}
	
	public void setEndAddress(int endAddress) {
		if (this.address >= 0) {
			this.size = endAddress - this.address;
		}
	}
		
	public HString getFullName() {
		String name = this.name.toString();
		Segment ps = this.parent;
		Segment ts = this;
		while(ps != null) {
			name = ps.name + "." + name;
			if(ps.parent == null) ts = ps;
			ps = ps.parent;
		}
		name = ts.owner.name + "." + name;
		return HString.getRegisteredHString(name);
	}
	
	public void addToUsedSize(int size){
		usedSize += size;
	}
	
	public void print(int indentLevel) {
		indent(indentLevel);
		vrb.print("segment = " + name.toString() + " {");
		vrb.print("attributes = 0x" + Integer.toHexString(attributes) + ", ");
		vrb.print("width = " + width + ", ");
		vrb.print("base = 0x" + Integer.toHexString(address) + ", ");
		vrb.println("size = 0x" + Integer.toHexString(size) + "}");
	}

}
