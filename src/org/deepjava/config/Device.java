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
import org.deepjava.host.ErrorReporter;
import org.deepjava.host.StdStreams;
import org.deepjava.linker.TargetMemorySegment;
import org.deepjava.strings.HString;

public class Device extends Item {
	public Segment segments;
	public MemSector sector;

	public HString memorytype;
	public int technology = -1; // 0 = RAM, 1 = FLASH
	public int attributes = 0;
	public int size = 0;
	public int width = 0;

	public Device(String name, int baseAddress, int size, int width, int attributes, int technology, String memType) {
		this.name = HString.getRegisteredHString(name);
		this.address = baseAddress;
		this.size = size;
		this.width = width;
		this.attributes = attributes;
		this.technology = technology;
		this.memorytype = HString.getRegisteredHString(memType);
	}

	public void addSegment(Segment s) {
		if (Configuration.dbg) StdStreams.vrb.println("[CONF] Device: adding new segment " + s.name + " to device " + name);
		if (s.width == this.width) {
			if (segments == null) segments = s;
			else segments.appendTail(s);
		} else ErrorReporter.reporter.error(223, "width of device " + name + " is not equal with the width of the segment" + s.name + "\n");
	}
	
	public void addSector(MemSector newSec) {	// add to list, list is sorted by increasing addresses
		if (sector == null) sector = newSec;
		else {
			MemSector s = sector, prev = null;
			while (newSec.address >= s.address && s.next != null) {prev = s; s = (MemSector) s.next;}
			if (prev == null) {
				if (newSec.address >= s.address) {s.next = newSec;} // last item
				else {sector = newSec; newSec.next = s;}	// first item
			} else {
				if (newSec.address >= s.address) {s.next = newSec;} // last item
				else {prev.next = newSec; newSec.next = s;}	// insert
			}		
		}
	}

	public Segment getSegmentByName(String jname) {
		return (Segment)segments.getItemByName(jname);
	}
	
	public void markUsedSectors(TargetMemorySegment tms) {
		if (tms == null) return;
		int tmsEnd = tms.startAddress + tms.data.length * 4;
		boolean marked = false; //only for mark time optimization
		if (tms != null) {
			MemSector current = sector;
			while(current != null){
				if((current.address < tms.startAddress && tms.startAddress < (current.address + size)) || (tms.startAddress < current.address && (current.address + current.size) < tmsEnd) || (current.address < tmsEnd && tmsEnd <(current.address + current.size))){
					current.used = true;
					marked = true;
				} else if(marked) return;
				current = (MemSector)current.next;
			}	
		}		
	}

	public int nofMarkedSectors() {
		int count = 0;
		MemSector current = sector;
		while(current != null){
			if(current.used){
				count++;
			}
			current = (MemSector)current.next;
		}
		return count;
	}

	public void print(int indentLevel) {
		indent(indentLevel);
		vrb.println("device = " + name.toString() + " {");
		indent(indentLevel+1);
		vrb.print("technology = ");
		if (technology == 0) vrb.print("Ram, ");
		else if (technology == 1) vrb.print("Flash, ");
		else vrb.print("Unkown, ");
		vrb.print("attributes = 0x" + Integer.toHexString(attributes) + ", ");
		vrb.print("width = " + width + ", ");
		vrb.print("base = 0x" + Integer.toHexString(address) + ", ");
		vrb.println("size = 0x" + Integer.toHexString(size));
		Item curr = sector;
		while (curr != null) {
			curr.print(indentLevel+1);
			curr = (MemSector)curr.next;
		}	
		curr = segments;
		while (curr != null) {
			curr.print(indentLevel+1);
			curr = (Segment)curr.next;
		}
		indent(indentLevel);
		vrb.println("}");
	}
}
