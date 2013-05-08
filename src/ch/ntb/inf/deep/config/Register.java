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

import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.strings.HString;

public class Register extends Item {
	public int regType = -1;
	public int size = -1;
	int repr = Parser.sHex;
	
	public Register(HString name) {
		this.name = name;
//		if (Configuration.dbg) StdStreams.vrb.println("[CONF] adding register " + name);
	}
	
	public HString getRegTypeName() {
		if (regType == Parser.sGPR) return HString.getRegisteredHString("GPR");
		if (regType == Parser.sFPR) return HString.getRegisteredHString("FPR");
		if (regType == Parser.sSPR) return HString.getRegisteredHString("SPR");
		if (regType == Parser.sIOR) return HString.getRegisteredHString("IOR");
		if (regType == Parser.sMSR) return HString.getRegisteredHString("MSR");
		if (regType == Parser.sCR) return HString.getRegisteredHString("CR");
		if (regType == Parser.sFPSCR) return HString.getRegisteredHString("FPSCR");
		return HString.getRegisteredHString("Undefined Type");
	}
	
	public HString getReprName() {
		if (repr == Parser.sDez) return HString.getRegisteredHString("Dez");
		if (repr == Parser.sBin) return HString.getRegisteredHString("Bin");
		if (repr == Parser.sHex) return HString.getRegisteredHString("Hex");
		return HString.getRegisteredHString("Undefined Representation");
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("register " + name.toString() + " {");
		sb.append("type: " + regType + ", " + "address: " + address + ", " + "size: " + size + ", " + "repr: " + getReprName());
		sb.append("}");
		return sb.toString();
	}
}
