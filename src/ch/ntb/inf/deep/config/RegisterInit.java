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

public class RegisterInit extends ConfigElement {
	
	private Register register;
	private int initValue;
	
	public RegisterInit(Register register, int initValue){
		this.name = HString.getRegisteredHString(register.getName() + "_init");
		this.register = register;
		this.initValue = initValue;
	}
	
	
	public void print(int indentLevel){
		StdStreams.vrb.print(register.getName().toString() + String.format(" = 0x%08X", initValue));	
	}
	
	public void println(int indentLevel){
		while(indentLevel > 0){
			StdStreams.vrb.print("  ");
			indentLevel--;
		}
		StdStreams.vrb.println(register.getName().toString() + String.format(" = 0x%08X", initValue));	
	}
	
	public RegisterInit getRegisterInitByRegister(Register reg) {
		RegisterInit ri = (RegisterInit)this.getHead();
		while(ri != null && ri.register != reg) {
			ri = (RegisterInit)ri.next;
		}
		return ri;
	}
	
	public void setInitValue(int val) {
		this.initValue = val;
	}
	
	@Override
	public String toString(){
		return register.getName().toString() + String.format(" = 0x%08X", initValue);
	}

	public Register getRegister() {
		return register;
	}
	
	public int getInitValue() {
		return initValue;
	}

}
