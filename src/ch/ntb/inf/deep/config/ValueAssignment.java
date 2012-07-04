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

package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class ValueAssignment extends ConfigElement {
	
	int value;
	
	public ValueAssignment(String jname, int value){
		this.name = HString.getRegisteredHString(jname);
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	public void print(int indentLevel){
		StdStreams.vrb.print(name.toString() + " = 0x" + Integer.toHexString(value));	
	}
	
	public void println(int indentLevel){
		while(indentLevel > 0){
			StdStreams.vrb.print("  ");
			indentLevel--;
		}
		StdStreams.vrb.println(name.toString() + " = 0x" + Integer.toHexString(value));	
	}

}
