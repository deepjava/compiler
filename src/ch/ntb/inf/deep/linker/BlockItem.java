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
}
