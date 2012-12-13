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

import java.io.PrintStream;

import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class SystemClass extends ConfigElement {
	public SystemMethod methods;
	public int attributes;
	private Arch archCondition = null;
	private CPU cpuCondition = null;
	
	public SystemClass(String jname) {
		this.name = HString.getRegisteredHString(jname);
	}
	
	public void addAttributes(int attributes){
		this.attributes |= attributes;
	}
	
	public void addMethod(SystemMethod method) {
		method.next = methods;
		methods = method;
	}
	
	public void addCondition(Arch archCond) {
		archCondition = archCond;
	}
	
	public void addCondition(CPU cpuCond) {
		cpuCondition = cpuCond;
		archCondition = cpuCond.arch;
	}
	
	public boolean checkCondition(CPU cpu) {
		if(cpuCondition == cpu || (archCondition == cpu.arch && cpuCondition == null) || (cpuCondition == null && archCondition == null)) return true;
		return false;
	}
	
	//--- debug primitives
	public void print(int indentLevel){
		PrintStream vrb = StdStreams.vrb;
		StdStreams.vrbPrintIndent(indentLevel);
		vrb.println("class = "+name);
		SystemMethod current = methods;
		while(current != null){
			current.print(indentLevel);
			current = (SystemMethod)current.next;
		}
	}
}
