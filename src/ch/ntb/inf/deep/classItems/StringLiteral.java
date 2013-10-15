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
