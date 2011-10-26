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

import ch.ntb.inf.deep.debug.Dbg;
import ch.ntb.inf.deep.strings.HString;

public class NamedConst extends DataItem {
	Constant constant;

	NamedConst(HString name, Type type, Item constant){
		super(name, type);
		this.constant = (Constant)constant;
	}

	//--- debug primitives
	public void print(int indentLevel){
		super.print(indentLevel);
		vrb.print("nconst "); constant.print(0);
	}

	public void printShort(int indentLevel){
		indent(indentLevel);
		vrb.printf("nconst %1$s %2$s, flags=", type.name, name);  Dbg.printAccAndPropertyFlags(accAndPropFlags, 'F');
		vrb.print(", ");  constant.printShort(0);
	}
	
	public Constant getConstantItem() {
		return constant;
	}
}
