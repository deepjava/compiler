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

import ch.ntb.inf.deep.classItems.ICdescAndTypeConsts;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.StdConstant;
import ch.ntb.inf.deep.classItems.Type;

public class ConstantItem extends BlockItem implements ICdescAndTypeConsts {
	
	Item ref;
	
	public ConstantItem(Item ref) {
		this.ref = ref;
		this.name = ref.name;
	}
	
	protected int getItemSize() {
		return ((Type)ref.type).sizeInBits / 8;
	}
	
	protected int insertIntoArray(int[] a, int offset) {
		int size = this.getItemSize();
		int index = offset / 4;
		int written = 0;
		if(offset + size <= a.length * 4) {
			switch(size) {
			case 8: // long/double 
				a[index] = ((StdConstant)ref).valueH;
				a[index + 1] = ((StdConstant)ref).valueL;
				written = size;
				break;
			case 4: // int/float 
				a[index] = ((StdConstant)ref).valueH;
				written = size;
				break;
			case 2: // short/char
				// TODO @Martin implement this
				break;
			case 1:	// byte/boolean 
				// TODO @Martin implement this
				break;
			default: // error
				break;
			}
		}
		return written;
	}
	
	public String toString() {
		int size = this.getItemSize();
		StringBuilder sb = new StringBuilder();
		switch(size) {
		case 8: // long/double
			sb.append(String.format("[%08X]", ((StdConstant)ref).valueH));
			sb.append(' ');
			sb.append(((StdConstant)ref));
			sb.append('\n');
			sb.append(String.format("[%08X]", ((StdConstant)ref).valueL));
			break;
		case 4: // int/float 
			sb.append(String.format("[%08X]", ((StdConstant)ref).valueH));
			sb.append(' ');
			sb.append(((StdConstant)ref));
			break;
		case 2: // short/char
			// TODO @Martin implement this
			break;
		case 1:	// byte/boolean 
			// TODO @Martin implement this
			break;
		default: // error
			break;
		}
		return sb.toString();
	}
	
	public void setIndex(int index) {
		ref.index = index;
	}
	
	public void setOffset(int offset) {
		ref.offset = offset;
	}
	
	public void setAddress(int address) {
		ref.address = address;
	}
}
