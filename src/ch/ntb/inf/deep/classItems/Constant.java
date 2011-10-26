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

public class Constant extends Item {

	Constant(HString name, Type type){
		super(name,  type);
		accAndPropFlags |= 1<<dpfConst;
	}

	//--- debug primitives
	public void printShort(int indentLevel){
		indent(indentLevel);
		vrb.print("field ");
		super.printShort(0);
	}

	public void print(int indentLevel){
		indent(indentLevel);
		Dbg.printJavaAccAndPropertyFlags(this.accAndPropFlags);
		type.printTypeCategory(); type.printName();
		vrb.print(' '); printName();
		vrb.print(" //dFlags:");  Dbg.printDeepAccAndPropertyFlags(this.accAndPropFlags);
	}
}
