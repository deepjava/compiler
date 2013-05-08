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
