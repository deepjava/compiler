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

package ch.ntb.inf.deep.linker;

import ch.ntb.inf.deep.config.Segment;

public class TargetMemorySegment {
	private static int tmsCounter = 0;
	
	public int id;
	public int startAddress;
	public int[] data;
	public TargetMemorySegment next;
	public Segment segment;
	public int size;	// size of TMS in bytes
	
	public TargetMemorySegment(Segment segment, int startAddress, int[] data, int length) {
		this.id = tmsCounter++;
		this.segment = segment;
		this.startAddress = startAddress;
		assert startAddress != -1;
		this.data = new int[length];
		for(int i = 0; i < length; i++) {
			this.data[i] = data[i];
		}
		this.size = length * 4;
	}
	
	public TargetMemorySegment(Segment segment, int startAddress, ConstBlkEntry item) {
		this.id = tmsCounter++;
		this.segment = segment;
		this.startAddress = startAddress;
		assert startAddress != -1;
		this.data = new int[Linker32.getBlockSize(item) / 4];
		int offset = 0;	// offset in bytes
		while (item != null) {
			item.insertIntoArray(data, offset);
			offset += item.getItemSize();
			item = (ConstBlkEntry)item.next;
		}
		this.size = offset;
	}

	public void addData(int addr, int[] d, int length) {
		if(d != null && 
				d.length > 0 && 
				length > 0 && 
				length <= d.length && 
				addr >= this.startAddress && 
				addr + length * 4 <= this.startAddress + this.data.length * 4) {
			
			for(int i = 0; i < length; i++) this.data[(addr - this.startAddress) / 4 + i] = d[i];
		}
	}
	
	public void addData(int addr, int[] d) {
		addData(addr, d, d.length);
	}
	
	public static void clearCounter() {
		tmsCounter = 0;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("Target Memory Segment #" + this.id + ":\n  Base Address: 0x" + Integer.toHexString(startAddress) + "\n  Size: " + data.length * 4 + " byte\n  Content:\n");
		for(int i = 0; i < data.length; i++) {
			sb.append("    0x" + Integer.toHexString((startAddress + i * 4)) + " [" + Integer.toHexString(data[i]) + "]\n");
		}
		return sb.toString();
	}
}
