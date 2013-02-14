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

public class StringLiteral extends Constant {
	public HString string;
	
	StringLiteral(HString name, HString string) {
		super(name, Type.wellKnownTypes[txString]);
		this.string = string;
		this.accAndPropFlags |= (1<<apfFinal);
	}

	//--- debug primitives
	public void printShort(int indentLevel) {
		vrb.printf("string \"%1$s\", flags=", string);  Dbg.printAccAndPropertyFlags(accAndPropFlags, 'F');
	}

	public void print(int indentLevel){
		super.print(indentLevel);
		vrb.print(" value = \"" + string + '\"'); 
	}

	public void println(int indentLevel) {
		print(indentLevel); vrb.println();
	}
	
	public String toString() {
		return string.toString();
	}
}
