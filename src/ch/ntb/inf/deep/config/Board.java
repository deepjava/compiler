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
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.strings.HString;

public class Board extends Item {
	public HString description;
	public CPU cpu;
	public SystemConstant sysConstants;
	public MemMap memorymap;
	public RegisterInit regInits;
	public RunConfiguration runConfig;
	
	public Board(String jname) {
		this.name = HString.getRegisteredHString(jname);
	}
	
	public Device getDeviceByName(HString name) {
		Device dev = memorymap.getDeviceByName(name); // search in board
		if (dev == null) dev = cpu.memorymap.getDeviceByName(name); // search in cpu
		return dev;
	}
	
	public Device getDeviceByName(String devName) {
		return getDeviceByName(HString.getRegisteredHString(devName));
	}
	
	public void addRunConfiguration(RunConfiguration newTC) {
		if (runConfig == null) runConfig = newTC;
		else {
			// Check if there is already a target configuration with the same name
			RunConfiguration tc = runConfig;
			while (tc != null && tc.name != newTC.name) tc = (RunConfiguration)tc.next;
			if (tc == null) runConfig.appendTail(newTC);
			else ErrorReporter.reporter.error(253);
		}
	}
	
	public RunConfiguration getTargetConfigurationByName(String tcName) {
		return (RunConfiguration)runConfig.getItemByName(tcName);
	}
	
}
