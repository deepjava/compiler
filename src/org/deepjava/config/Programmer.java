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

public class Programmer extends Item {

	private HString description;
	private HString pluginId;
	private HString className;
	private HString opts;
	
	public Programmer(String jname) {
		this.name = HString.getRegisteredHString(jname);
	}
	
	public void setDescription(String desc) {
		this.description = HString.getRegisteredHString(desc);
	}
	
	public HString getDescription() {
		return this.description;
	}
	
	public void setClassName(String name) {
		this.className = HString.getRegisteredHString(name);
	}
	
	public HString getClassName() {
		return this.className;
	}
	
	public void setPluginId(String id) {
		this.pluginId = HString.getRegisteredHString(id);
	}
	
	public HString getPluginId() {
		return this.pluginId;
	}
	
	public void setOpts(String id) {
		opts = HString.getRegisteredHString(id);
	}
	
	public HString getOpts() {
		return opts;
	}
}
