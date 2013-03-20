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

package ch.ntb.inf.deep.classItems;

public class LocalVar extends Item {

	//--- instance fields
	int startPc, length; // life range: [startPc, startPc+length]
//	int index; // starts at this slot, long and double occupy this slot and next slot (index+1)

	//--- debug primitives
	
	public void print(int indentLevel){
		indent(indentLevel);
		vrb.printf("[%1$2d] (%2$c)%3$s %4$s  [%5$d,%6$d]", index, (char)((Type)type).category, type.name, name, startPc, startPc+length);
	}
}
