﻿/*
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

import ch.ntb.inf.deep.strings.HString;

public class FixedValueItem extends BlockItem {
	
	private static final int size = 4;
	
	int val;
		
	public FixedValueItem(HString name, int val) {
		this.name = name;
		this.val = val;
	}
	
	public FixedValueItem(int val) {
		this(HString.getHString("???"), val);
	}
	
	public FixedValueItem(HString name) {
		this(name, -1);
	}
	
	public FixedValueItem(String name) {
		this(HString.getHString(name), -1);
	}
	
	public FixedValueItem(String name, int val) {
		this(HString.getHString(name), val);
	}
	
	public void setValue(int val) {
		this.val = val;
	}
	
	protected int getItemSize() {
		return size;
	}
	
	protected int insertIntoArray(int[] a, int offset) {
		int index = offset / 4;
		int written = 0;
		if(offset + size <= a.length * 4) {
			a[index] = val;
			written = size;
		}
		return written;
	}
	
	public String toString() {
		return String.format("[%08X]", val) + " (" + name + ")";
	}
	
	public int getValue() {
		return val;
	}

}
