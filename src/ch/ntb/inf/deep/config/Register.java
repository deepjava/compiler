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

public class Register extends ConfigElement {
	public Register nextWithInitValue;
	int type = -1;
	int addr = -1;
	int size = -1;
	int repr;
	
	public Register(HString name){
		this.name = name;
	}
	
	public void setAddress(int addr){
		this.addr = addr;
	}
	
	public void setSize(int size){
		this.size = size;
	}
	
	public void setType(int type){
		this.type = type;
		if(repr == 0){
			repr = Parser.sHex;
		}
	}
		
	public void setRepresentation(int repr){
		this.repr = repr;
	}
	
	public int getAddress(){
		return addr;
	}
	
	public int getSize(){
		return size;
	}
	
	public int getType(){
		return type;
	}
	
	public int getRepresentation(){
		return repr;
	}
	
	public HString getTypeName(){
		if(type == Parser.sGPR){
			return HString.getRegisteredHString("GPR");
		}
		if(type == Parser.sFPR){
			return HString.getRegisteredHString("FPR");
		}
		if(type == Parser.sSPR){
			return HString.getRegisteredHString("SPR");
		}
		if(type == Parser.sIOR){
			return HString.getRegisteredHString("IOR");
		}
		if (type == Parser.sMSR){
			return HString.getRegisteredHString("MSR");
		}
		if (type == Parser.sCR){
			return HString.getRegisteredHString("CR");
		}
		if (type == Parser.sFPSCR){
			return HString.getRegisteredHString("FPSCR");
		}
		
		return HString.getRegisteredHString("Undefined Type");
	}
	
	public HString getReprName(){
		if(repr == Parser.sDez){
			return HString.getRegisteredHString("Dez");
		}
		if(repr == Parser.sBin){
			return HString.getRegisteredHString("Bin");
		}
		if(repr == Parser.sHex){
			return HString.getRegisteredHString("Hex");
		}
		return HString.getRegisteredHString("Undefined Representation");
	}
	
	public void println(int indentLevel){
		for(int i = indentLevel; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("register " + name.toString() + " {");
		
		for(int i = indentLevel + 1; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("type: " + getTypeName() + ";");
		
		for(int i = indentLevel + 1; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("address: " + addr + ";");
		
		for(int i = indentLevel + 1; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("size: " + size + ";");
		
		for(int i = indentLevel + 1; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("repr: " + getReprName() + ";");
		
		for(int i = indentLevel; i > 0; i--){
			StdStreams.vrb.print("  ");
		}
		StdStreams.out.println("}");
	}
}
