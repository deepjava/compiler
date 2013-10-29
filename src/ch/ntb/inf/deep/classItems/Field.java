/*
 * Copyright 2011 - 2013 NTB University of Applied Sciences in Technology
 * Buchs, Switzerland, http://www.ntb.ch/inf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
	public void printShort(int indentLevel) {
		indent(indentLevel);
		vrb.printf("field %1$s %2$s, flags=", type.name, name); Dbg.printAccAndPropertyFlags(accAndPropFlags, 'F');
	}

	public void print(int indentLevel) {
		indent(indentLevel);
		Dbg.printJavaAccAndPropertyFlags(this.accAndPropFlags, 'F');
		type.printTypeCategory(); type.printName();
		vrb.printf(" %1$s; offset=%2$d, ", name.toString(), offset); type.printSize();
		vrb.print(", dFlags:"); Dbg.printDeepAccAndPropertyFlags(this.accAndPropFlags, 'F');
	}

	public void println(int indentLevel) {
		print(indentLevel); vrb.println();
	}
}
