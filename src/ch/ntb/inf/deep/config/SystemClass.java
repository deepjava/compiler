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

public class SystemClass {
	public SystemClass next;
	public String name;
	public SystemMethod methods;
	public int attributes;

	public SystemClass(String name) {
		this.name = name;
	}
	
	public void addAttributes(int attributes){
		this.attributes |= attributes;
	}
	

	public void addMethod(SystemMethod method) {
		method.next = methods;
		methods = method;
	}
	
	//--- debug primitives
	public void print(int indentLevel){
		PrintStream vrb = StdStreams.vrb;
		StdStreams.vrbPrintIndent(indentLevel);
		vrb.println("class = "+name);
		SystemMethod current = methods;
		while(current != null){
			current.print(indentLevel);
			current = current.next;
		}
	}
}
