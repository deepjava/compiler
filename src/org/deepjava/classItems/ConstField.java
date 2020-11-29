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

package org.deepjava.classItems;

import org.deepjava.host.Dbg;
import org.deepjava.strings.HString;

public class ConstField extends Field {
	Constant constant;

	ConstField(HString name, Type type, Item constant){
		super(name, type);
		this.constant = (Constant)constant;
	}

	public Constant getConstantItem() {
		return constant;
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
	
}
