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

import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class Memorysector extends ConfigElement {
	
	public boolean used = false;
	int baseAddress = -1;
	int size = 0;
	
	public Memorysector(String jname) {
		this.name = HString.getRegisteredHString(jname);
	}
	
	public Memorysector(String jname, int baseAddress) {
		this.name = HString.getRegisteredHString(jname);
		this.baseAddress = baseAddress;
	}
	
	public Memorysector(String jname, int baseAddress, int size) {
		this.name = HString.getRegisteredHString(jname);
		this.baseAddress = baseAddress;
		this.size = size;
	}
	
	public void setBaseAddress(int baseAddress) {
		this.baseAddress = baseAddress;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public int getBaseAddress() {
		return this.baseAddress;
	}
	
	public int getSize() {
		return this.size;
	}
	
	public HString getName(){
		return name;
	}

	public void insertByAddress(Memorysector newSector) {
		Memorysector sector = (Memorysector)this.getHead();
		while(sector.baseAddress + sector.size < newSector.baseAddress) {
			sector = (Memorysector)sector.next;
		}
		sector.insertAfter(newSector);
	}
	
	public void println(int indentLevel){
		for(int i = indentLevel; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("sector " + name.toString() + "{");
		
		for(int i = indentLevel + 1; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		if(baseAddress != -1){
			StdStreams.vrb.print(String.format("base: 0x%04x", baseAddress));
		}
		if(size > 0){
			StdStreams.vrb.print(String.format(", sectorsize: 0x%04x", size));
		}
		StdStreams.vrb.println(";");
		
		for(int i = indentLevel; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("}");
	}
}
