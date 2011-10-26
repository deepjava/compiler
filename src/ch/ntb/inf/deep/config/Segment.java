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
import ch.ntb.inf.deep.linker.TargetMemorySegment;
import ch.ntb.inf.deep.strings.HString;

public class Segment implements IAttributes {
	public Device owner;
	public Segment subSegments;
	public Segment parent;
	public Segment lastSubSegment;
	public Segment next;
	public Segment prev;
	
	HString name;
	int attributes = 0;
	int baseAddress = -1;
	int size = 0;
	int usedSize = 0;
	int width = 0;
	
	public Segment(HString name, Device owner){
		this.name = name;
		this.owner = owner;
	}
	
	public Segment(HString name, Device owner, int baseAddress) {
		this.name = name;
		this.baseAddress = baseAddress;
		this.owner = owner;
	}
	
	public Segment(HString name, Device owner, int baseAddress, int size) {
		this.name = name;
		this.baseAddress = baseAddress;
		this.size = size;
		this.owner = owner;
	}
	
	public Segment(HString name, Device owner, int baseAddress, int size, int width) {
		this.name = name;
		this.baseAddress = baseAddress;
		this.size = size;
		this.width = width;
		this.owner = owner;
	}
	
	public Segment(HString name, Device owner, int baseAddress, int size, int width, int attributes) {
		this.name = name;
		this.baseAddress = baseAddress;
		this.size = size;
		this.width = width;
		this.attributes = attributes;
		this.owner = owner;
	}
		
	public void setAttribute(int attributes) {
		this.attributes = attributes;
	}
	
	public void addAttributes(int attributes) {
		this.attributes |= attributes;
	}
	
	public void setAttributes(int attributes) {
		this.attributes = attributes;
	}
	
	public void removeAttributes(int attributes) {
		this.attributes &= ~attributes;
	}
	
	public void setBaseAddress(int baseAddress) {
		this.baseAddress = baseAddress;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public void setEndAddress(int endAddress) {
		if(this.baseAddress >= 0) {
			this.size = endAddress - this.baseAddress;
		}
	}
	
	public int getBaseAddress() {
		return this.baseAddress;
	}
	
	public int getSize() {
		return this.size;
	}
	
	public int getWidth() {
		return width;
	}
	
	public HString getName(){
		return name;
	}
	
	public int getAttributes(){
		return attributes;
	}

	
	public boolean addSubSegment(Segment s) {
		s.parent = this;
		if(s.width == this.width) {
			if(subSegments == null) {
				subSegments = s;
				lastSubSegment = subSegments;
			}
			else {
				lastSubSegment.next = s;
				s.prev = lastSubSegment;
				lastSubSegment = lastSubSegment.next;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Add Size to usedSize.
	 * @param size
	 */
	public void addToUsedSize(int size){
		usedSize += size;
	}
	
	
	/**
	 * @return the used size of the segment.
	 */
	public int getUsedSize(){
		return usedSize;
	}
	
	public Segment getSubSegmentByName(HString name){
		int segHash = name.hashCode();
		Segment current = subSegments;
		while(current != null){
			if(current.name.hashCode() == segHash){
				if(current.name.equals(name)){
					return current;
				}
			}
			current = current.next;
		}
		return current;
	}
	
	public void println(int indentLevel){
		for(int i = indentLevel; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("segment " + name.toString() + "{");
		for(int i = indentLevel + 1; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.print("attributes: 0x" + Integer.toHexString(attributes) + ", width: " + width );
		if(baseAddress != -1){
			StdStreams.vrb.print(", base: 0x" + Integer.toHexString(baseAddress));
		}
		if(size > 0){
			StdStreams.vrb.print(", size: 0x" + Integer.toHexString(size));
		}
		StdStreams.vrb.println(";");
		Segment current = subSegments;
		while(current != null){
			current.println(indentLevel + 1);
			current = current.next;
		}
		for(int i = indentLevel; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("}");
	}
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Name:       " + name.toString() + "\n");
		sb.append("Owner:      " + owner.getName().toString() + "\n");
		sb.append("Attributes: 0x" + Integer.toHexString(attributes) + "\n");
		sb.append("Width:      " + width + " Bytes\n");
		sb.append("Base:       0x" + Integer.toHexString(baseAddress) + "\n");
		sb.append("Size:       0x" + Integer.toHexString(size) + " (" + size + ")" + " Bytes\n");
		if(size > 0){			
			sb.append("Used:       0x" + Integer.toHexString(usedSize) + " (" + usedSize + ")" + " Bytes" + " -> " + String.format("%.1f", ((float)(usedSize*100))/size) + "%\n");
		}
		return sb.toString();
	}
}
