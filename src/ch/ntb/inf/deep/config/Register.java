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
