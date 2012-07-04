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

import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.TargetMemorySegment;
import ch.ntb.inf.deep.strings.HString;

public class Device extends ConfigElement implements ErrorCodes{
	public Segment segments;
	public Memorysector sector;

	private HString memorytype;
	private int technology = -1; // 0 = RAM, 1 = FLASH
	private int attributes = 0;
	private int baseAddress = -1;
	private int size = 0;
	private int width = 0;

	public Device(String name, int baseAddress, int size, int width, int attributes, int technology) {
		this.name = HString.getRegisteredHString(name);
		this.baseAddress = baseAddress;
		this.size = size;
		this.width = width;
		this.attributes = attributes;
		this.technology = technology;
	}

	public void setMemoryType(String memType) {
		this.memorytype = HString.getRegisteredHString(memType);
	}
	
	public int getbaseAddress(){
		return baseAddress;
	}
	
	public int getSize(){
		return size;
	}

	public boolean addSegment(Segment s) {
		if(Configuration.dbg) StdStreams.vrb.println("[CONF] Device: adding new segment " + s.getName() + " to device " + this.getName());
		if(s.width == this.width) {
			if(segments == null) {
				segments = s;
			}
			else {
				segments.append(s);
			}
			return true;
		}
		ErrorReporter.reporter.error(errInconsistentattributes, "width form device " + this.getName() + " is not equal with the width from the segment" + s.getName() + "\n");
		return false;
	}
	
	public void addSector(Memorysector s) {
		if(sector == null) {
			sector = s;
		}
		else {
			sector.insertByAddress(s);
		}
	}

	public Segment getSegementByName(HString name) {
		return (Segment)segments.getElementByName(name);
	}
	
	public Segment getSegementByName(String jname) {
		return (Segment)segments.getElementByName(jname);
	}
	
	public HString getMemoryType(){
		return memorytype;
	}
	
	public int getTechnology(){
		return technology;
	}
	
	public int getAttributes() {
		return this.attributes;
	}

	public int getWidth() {
		return this.width;
	}
	
	public void markUsedSectors(TargetMemorySegment tms){
		if(tms == null)return;
		int tmsEnd = tms.startAddress + tms.data.length * 4;
		boolean marked = false; //only for mark time optimization
		if(tms != null){
			Memorysector current = sector;
			while(current != null){
				if((current.baseAddress < tms.startAddress && tms.startAddress < (current.baseAddress + size)) || (tms.startAddress < current.baseAddress && (current.baseAddress + current.size) < tmsEnd) || (current.baseAddress < tmsEnd && tmsEnd <(current.baseAddress + current.size))){
					current.used = true;
					marked = true;
				}else if(marked){
					return;
				}
				current = (Memorysector)current.next;
			}	
		}		
	}

	public int nofMarkedSectors(){
		int count = 0;
		Memorysector current = sector;
		while(current != null){
			if(current.used){
				count++;
			}
			current = (Memorysector)current.next;
		}
		return count;
	}

	public void println(int indentLevel) {
		for (int i = indentLevel; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("device " + name.toString() + " {");
		for (int i = indentLevel + 1; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.print("attributes: 0x" + Integer.toHexString(attributes)
				+ ", width: " + width);
		if (baseAddress != -1) {
			StdStreams.vrb.print(", base: 0x" + Integer.toHexString(baseAddress));
		}
		if (size > 0) {
			StdStreams.vrb.print(", size: 0x" + Integer.toHexString(size));
		}
		StdStreams.vrb.println(";");
		
		Memorysector cur = sector;
		while (cur != null){
			cur.println(indentLevel + 1);
			cur = (Memorysector)cur.next;
		}		
		
		Segment current = segments;
		while (current != null) {
			current.println(indentLevel + 1);
			current = (Segment)current.next;
		}
		for (int i = indentLevel; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.out.println("}");
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Name:        " + name.toString() + "\n");
		sb.append("Technology:  ");
		if(technology == 0){
			sb.append("Ram\n");
		}else if(technology == 1){
			sb.append("Flash\n");
		}else{
			sb.append("Unkown\n");
		}
		sb.append("Attributes:  0x" + Integer.toHexString(attributes) + "\n");
		sb.append("Width:       " + width + " Bytes\n");
		sb.append("Base:        0x" + Integer.toHexString(baseAddress) + "\n");
		sb.append("Size:        0x" + Integer.toHexString(size) + " (" + size + ")" + " Bytes\n");
		int usedSize = 0;
		Segment current = segments;
		while(current != null){
			usedSize += current.getUsedSize();
			current = (Segment)current.next;
		}
		sb.append("Used:        0x" + Integer.toHexString(usedSize) + " (" + usedSize + ")" + " Bytes" + " -> " + String.format("%.1f", ((float)(usedSize*100))/size) + "%\n");
		return sb.toString();
	}
}
