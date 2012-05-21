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

package ch.ntb.inf.deep.linker;

import java.util.zip.CRC32;

import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class BlockItem {
		
	public BlockItem next;
	public BlockItem prev;
	
	public HString name;
	
	protected void insertAfter(BlockItem itm) {
		if(this.next != null) {
			itm.next = this.next;
			this.next.prev = itm;
		}
		this.next = itm;
		itm.prev = this;
	}
	
	protected void insertBefore(BlockItem itm) {
		if(this.prev != null) {
			itm.prev = this.prev;
			this.prev.next = itm;
		}
		this.prev = itm;
		itm.next = this;
	}
	
	protected void append(BlockItem itm) {
		this.getTail().insertAfter(itm);
	}
		
	protected BlockItem getTail() {
		BlockItem tail = this;
		while(tail.next != null) tail = tail.next;
		return tail;
	}
	
	protected BlockItem getHead() {
		BlockItem head = this;
		while(head.prev != null) head = head.prev;
		return head;
	}
		
	protected int getBlockSize() {
		BlockItem itm = this.getHead();
		int size = 0;
		while(itm != null) {
			size += itm.getItemSize();
			itm = itm.next;
		}
		return size;
	}
	
	protected int getItemSize() {
		return -1;
	}
	
	protected int insertIntoArray(int[] a, int offset) {
		return -1;
	}
	
	public byte[] getBytes() {
		return null;
	}
	
	/**
	 * Calculates the CRC32 checksum of the list for all
	 * elements from the beginning to the given item and
	 * saves the result in the given item. 
	 * 
	 * @param fcsItem item to write the checksum in.
	 * @return the CRC32 checksum
	 */
	public static int setCRC32(FixedValueItem fcsItem) {
		CRC32 checksum = new CRC32();
		BlockItem i = fcsItem.getHead();
		if(Linker32.dbg) {System.out.println("> Calculating CRC32:"); System.out.print("  ");}
		while(i != fcsItem) {
			checksum.update(i.getBytes());
			if(Linker32.dbg) printByteArray(i.getBytes());
			i = i.next;
		}
		if(Linker32.dbg) System.out.println();
		int fcs = (int)checksum.getValue();
		// change endianess and complement
		fcs = ((((byte)fcs)<<24) | ((((byte)(fcs>>8))<<16)&0xff0000) | ((((byte)(fcs>>16))<<8)&0xff00) | (((byte)(fcs>>24))&0xff)) ^ 0xffffffff;
		fcsItem.setValue(fcs);
		return fcs;
	}
	
	
	public String toString(){
		return new String("empty Block");
	}
	
	public void printList() {
		BlockItem i = getHead();
		while(i != null) {
			StdStreams.vrb.println(i);
			i = i.next;
		}
	}
	
	public void printListRaw() {
		int[] a = new int[this.getBlockSize()/4];
		int offset = 0;
		BlockItem item = this.getHead();
		while(item != null) {
			item.insertIntoArray(a, offset);
			offset += item.getItemSize();
			item = item.next;
		}
		for(int i = 0; i < a.length; i++) {
			StdStreams.vrb.printf("[%8x]\n", a[i]);
		}
	}
	
	protected void inserteBytes(byte[] bytes, int offset, int val) {
		for (int i = 0; i < 4; ++i) {
		    int shift = i << 3; // i * 8
		    bytes[offset + 3 - i] = (byte)((val & (0xff << shift)) >>> shift);
		}
	}
	
	// debug primitives
	private static void printByteArray(byte[] bytes) {
		final byte[] hexchars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
		StringBuilder s = new StringBuilder(3 * bytes.length);
		 for (int i = 0; i < bytes.length; i++) {
			 int v = bytes[i] & 0xff;
			 s.append((char)hexchars[v >> 4]);
			 s.append((char)hexchars[v & 0xf]);
			 s.append(" ");
		 }
		 System.out.print(s);
	}
}
