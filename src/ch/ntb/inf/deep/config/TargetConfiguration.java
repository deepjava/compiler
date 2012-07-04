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

public class TargetConfiguration extends ConfigElement {
	Board board;
	Module modules;
	Module system;
	RegisterInitList reginits;
	
	public TargetConfiguration(String jname, Board board) {
		this.name = HString.getRegisteredHString(jname);
		this.board = board;
		this.reginits = new RegisterInitList(board.cpu);
	}
	
	public void addModule(Module newModule) {
		if(Configuration.dbg) StdStreams.vrb.println("[CONF] TargetConfiguration: Adding new module: " + newModule.getName());
		if (modules == null) {
			if(Configuration.dbg) StdStreams.vrb.println("  Adding first module");
			modules = newModule;
		}
		else {
			if(Configuration.dbg) StdStreams.vrb.print("  Looking for module with the same name");
			ConfigElement m = modules.getElementByName(newModule.name);
			if(m == null) {
				if(Configuration.dbg) StdStreams.vrb.println(" -> not found -> adding new module");
				modules.append(newModule);
				return;
			}
			else {
				if(Configuration.dbg) StdStreams.vrb.println(" -> found -> nothing to do");
			}
		}
	}
			
	public void addSystemModule(Module mod){ // TODO improve this!
		if(system == null){
			system = mod;
		}else{
			mod.next = system;
			system = mod;
		}		
	}

	public Module getModules() {
		return modules;
	}
	
	public Module getSystemModules() {
		return (Module)system.getHead();
	}
	
	public Module getModuleByName(String moduleName){
		return (Module)modules.getElementByName(moduleName);
	}

	public Module getModuleByName(HString moduleName){
		return (Module)modules.getElementByName(moduleName);
	}
	
	public RegisterInitList getRegisterInits() {
		return reginits;
	}
	
//	public void print(int indentLevel){
//		for(int i = indentLevel; i > 0; i--){
//			StdStreams.vrb.print("  ");
//		}
//		StdStreams.vrb.println("targetconfiguration " + name.toString() + " {");
//		
//		if (reginits != null) {
//			StdStreams.vrb.println("  reginit{");
//			RegisterInit initReg = reginits;
//			while (initReg != null) {
//				for(int i = indentLevel+1; i > 0; i--){
//					StdStreams.vrb.print("  ");
//				}
//				StdStreams.vrb.println(initReg.register.getName().toString() + String.format(" = 0x%X", initReg.initValue));
//				initReg = (RegisterInit)initReg.next;
//			}
//			StdStreams.vrb.println("  }");
//		}
//		
//		
//		for(int i = indentLevel+1; i > 0; i--){
//			StdStreams.vrb.print("  ");
//		}
//		StdStreams.vrb.println("system {");
//		
//		Module current = system;
//		while(current != null){
//			current.println(indentLevel + 2);
//			current = (Module)current.next;
//		}
//		
//		for(int i = indentLevel+1; i > 0; i--){
//			StdStreams.vrb.print("  ");
//		}
//		StdStreams.vrb.println("}");
//		
//		for(int i = indentLevel+1; i > 0; i--){
//			StdStreams.vrb.print("  ");
//		}
//		StdStreams.vrb.println("modules {");
//				
//		current = modules;
//		while(current != null){
//			current.println(indentLevel + 2);
//			current = (Module)current.next;
//		}
//		
//		for(int i = indentLevel+1; i > 0; i--){
//			StdStreams.vrb.print("  ");
//		}
//		StdStreams.vrb.println("}");
//		
//		for(int i = indentLevel; i > 0; i--){
//			StdStreams.vrb.print("  ");
//		}
//		StdStreams.vrb.println("}");
//	}
}
