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

import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.strings.HString;

public class InterfaceItem extends BlockItem {
	
	private static final int size = 4;
	
	int offset;
	Item iface;
		
	public InterfaceItem(HString name, Item iface, int offset) {
		this.name = name;
		this.iface = iface;
		this.offset = offset;
	}
	
	public InterfaceItem(Item iface, int offset) {
		if(iface != null) this.name = iface.name;
		else this.name = HString.getHString("???");
		this.iface = iface;
		this.offset = offset;
	}
	
	public int getOffset() {
		return this.offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	protected int getItemSize() {
		return size;
	}
	
	protected int insertIntoArray(int[] a, int offset) {
		int index = offset / 4;
		int written = 0;
		if(offset + size <= a.length * 4) {
			a[index] = offset << 16 & (short)-1;
			written = size;
		}
		return written;
	}
	
	public String toString() {
		return String.format("[%08X]", offset << 16 & (short)-1) + " (" + name + ")";
	}

}
