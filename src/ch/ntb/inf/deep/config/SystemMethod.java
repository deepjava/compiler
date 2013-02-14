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

import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class SystemMethod extends ConfigElement implements ICclassFileConsts{
	public int attributes; 	// e.g. (1<<dpfNew) 
	public int id;	// unique id for system methods		
	public int offset = -1;

	public SystemMethod(HString name) {
		this.name = name;
	}
	
	//--- debug primitives
	public void print(int indentLevel){
		PrintStream vrb = StdStreams.vrb;
		StdStreams.vrbPrintIndent(indentLevel);
		vrb.println("method "+name+" {");
		StdStreams.vrbPrintIndent(indentLevel+1);
		vrb.printf("attributes: 0x%1$x\n", attributes);
		StdStreams.vrbPrintIndent(indentLevel);
		vrb.printf("id: 0x%1$x\n", id);
		StdStreams.vrbPrintIndent(indentLevel);
		vrb.println("}");
	}
}
