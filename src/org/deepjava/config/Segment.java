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

package org.deepjava.config;

import org.deepjava.classItems.Item;
import org.deepjava.strings.HString;

public class Segment extends Item {
	public Device owner;
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
