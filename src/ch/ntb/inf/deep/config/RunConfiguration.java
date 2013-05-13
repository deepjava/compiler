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
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

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
