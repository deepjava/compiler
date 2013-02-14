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

import ch.ntb.inf.deep.host.Dbg;
import ch.ntb.inf.deep.strings.HString;

public class Field extends ClassMember {

	Field(HString name, Type type){
		super(name, type);
	}
	
	//--- debug primitives
	public void printShort(int indentLevel){
		indent(indentLevel);
		vrb.printf("field %1$s %2$s, flags=", type.name, name);  Dbg.printAccAndPropertyFlags(accAndPropFlags, 'F');
	}

	public void print(int indentLevel){
		indent(indentLevel);
		Dbg.printJavaAccAndPropertyFlags(this.accAndPropFlags, 'F');
		type.printTypeCategory(); type.printName();
		vrb.printf(" %1$s; offset=%2$d, ", name.toString(), offset); type.printSize();
		vrb.print(", dFlags:");  Dbg.printDeepAccAndPropertyFlags(this.accAndPropFlags, 'F');
	}

	public void println(int indentLevel){
		print(indentLevel);  vrb.println();
	}
}
