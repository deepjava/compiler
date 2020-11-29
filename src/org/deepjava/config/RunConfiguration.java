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

package org.deepjava.config;

import org.deepjava.classItems.Item;
import org.deepjava.strings.HString;

public class RunConfiguration extends Item {
	public String description;
	Module modules;
	Module system;
	public RegisterInit regInits;
	
	public RunConfiguration(String jname) {
		this.name = HString.getRegisteredHString(jname);
	}
	
	public void addModule(Module mod) {
		if (dbg) vrb.println("[CONF] TargetConfiguration: Adding new module: " + mod.name);
		if (modules == null) modules = mod;
		else {
			if (Configuration.dbg) vrb.print("  Looking for module with the same name");
			Item m = modules.getItemByName(mod.name);
			if (m == null) {
				if (Configuration.dbg) vrb.println(" -> not found -> adding new module");
				modules.appendTail(mod);
				return;
			} else if (Configuration.dbg) {vrb.println(" -> found -> nothing to do");}
		}
	}
			
	public void addSystemModule(Module mod){
		if (Configuration.dbg) vrb.println("[CONF] TargetConfiguration: Adding new system module: " + mod.name);
		if (system == null) system = mod;
		else {
			mod.next = system;
			system = mod;
		}		
	}

	public Module getModuleByName(String moduleName){
		return (Module)modules.getItemByName(moduleName);
	}

	public Module getModuleByName(HString moduleName){
		return (Module)modules.getItemByName(moduleName);
	}
	
}
