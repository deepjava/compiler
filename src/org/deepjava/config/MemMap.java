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
import org.deepjava.host.StdStreams;
import org.deepjava.strings.HString;

public class MemMap extends Item {
	
	public Device devs;
	int nofHeapSegments = 0;
	int nofStackSegments = 0;
	private Segment[] heapSegments = new Segment[Configuration.maxNumbersOfHeapSegments];
	private Segment[] stackSegments = new Segment[Configuration.maxNumbersOfStackSegments];

	public MemMap(String str) {
		name = HString.getHString(str);
	}

	protected void registerHeapSegment(Segment heapSegment) {
		if (nofHeapSegments < Configuration.maxNumbersOfHeapSegments) {
			heapSegments[nofHeapSegments++] = heapSegment;
		}
	}
	
	protected void registerStackSegment(Segment stackSegment) {
		if (nofStackSegments < Configuration.maxNumbersOfStackSegments) {
			stackSegments[nofStackSegments++] = stackSegment;
		}
	}
	
	protected void addDevice(Device dev) {
		if (Configuration.dbg) StdStreams.vrb.println("[CONF] MemoryMap: adding new device " + dev.name);
		if (devs == null) this.devs = dev;
		else devs.appendTail(dev);
	}

	public Segment getHeapSegment(int i) {
		if (i >= 0 && i < nofHeapSegments) return heapSegments[i];
		return null;
	}
	
	public Segment getStackSegment(int i) {
		if (i >= 0 && i < nofStackSegments) return stackSegments[i];
		return null;
	}
	
	public int getNofDevices() {
		int nof = 0;
		Item itm = devs;
		while (itm != null) {
			nof++;
			itm = itm.next;
		}
		return nof;
	}

	public Device getDeviceByName(HString name) {
		return (Device)devs.getItemByName(name);
	}
	
	public Device getDeviceByName(String jname) {
		return (Device)devs.getItemByName(jname);
	}

	public Segment getSegmentByName(String fullQualifiedName) {
		if (Configuration.dbg) StdStreams.vrb.println("[CONF] MemoryMap: getSegmentByFullName");
		String[] name = fullQualifiedName.split("\\.");
		if (Configuration.dbg) StdStreams.vrb.println("  Looking for: " + fullQualifiedName);
		int i = 0;
		Device dev = getDeviceByName(name[i++]);
		Segment seg = null;
		if(dev != null && name.length > 1) {
			if (Configuration.dbg) StdStreams.vrb.println("  Device found: " + dev.name);
			seg = dev.getSegmentByName(name[i++]);
			if (Configuration.dbg) {
				if (seg != null) StdStreams.vrb.println("  Segment found: " + seg.name);
				else StdStreams.vrb.println("  Segment not found: " + name[i - 1]);
			}
			if (i != name.length) {
				if(Configuration.dbg) StdStreams.vrb.println("  i != name.length -> returning null");
				seg = null;
			}
		}
		return seg;
	}
	
	public void print(int indentLevel) {
		indent(indentLevel);
		vrb.println("memorymap {");
		Device current = devs;
		while (current != null) {
			current.print(indentLevel+1);
			current = (Device)current.next;
		}
		indent(indentLevel); vrb.println("}");

	}

}
