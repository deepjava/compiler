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

public class MemoryMap extends ConfigElement implements IAttributes, ErrorCodes {
	
	private Device devs;
	private Module modulesMap;
	
	private int nofHeaps = 0;
	private int nofStacks = 0;
	private Segment[] heapSegments = new Segment[Configuration.maxNumbersOfHeaps];
	private Segment[] stackSegments = new Segment[Configuration.maxNumbersOfStacks];

	protected void registerHeapSegment(Segment heapSegment) {
		if(nofHeaps < Configuration.maxNumbersOfHeaps) {
			this.heapSegments[nofHeaps++] = heapSegment;
		}
	}
	
	protected void registerStackSegment(Segment stackSegment) {
		if(nofStacks < Configuration.maxNumbersOfStacks) {
			this.stackSegments[nofStacks++] = stackSegment;
		}
	}
	
	protected void addDevice(Device dev) {
		if(Configuration.dbg) StdStreams.vrb.println("[CONF] MemoryMap: adding new device " + dev.getName());
		if (this.devs == null) {
			this.devs = dev;
		}
		else {
			this.devs.append(dev);
		}
	}

	public Device getDevices() {
		return devs;
	}
	
	public int getNofDevices(){
		if(devs != null) return devs.getNofElements();
		return 0;
	}

	public Module getModules() {
		return modulesMap;
	}

	public Device getDeviceByName(HString name) {
		return (Device)devs.getElementByName(name);
	}
	
	public Device getDeviceByName(String jname) {
		return (Device)devs.getElementByName(jname);
	}

	public Segment getSegmentByName(String fullQualifiedName) {
		if(Configuration.dbg) StdStreams.vrb.println("[CONF] MemoryMap: getSegmentByFullName");
		String[] name = fullQualifiedName.split("\\.");
		if(Configuration.dbg) StdStreams.vrb.println("  Looking for: " + fullQualifiedName);
		int i = 0;
		Device dev = getDeviceByName(name[i++]);
		Segment seg = null;
		if(dev != null && name.length > 1) {
			if(Configuration.dbg) StdStreams.vrb.println("  Device found: " + dev.getName());
			seg = dev.getSegementByName(name[i++]);
			if(Configuration.dbg) {
				if(seg != null) StdStreams.vrb.println("  Segment found: " + seg.getName());
				else StdStreams.vrb.println("  Segment not found: " + name[i - 1]);
			}
			while(seg != null && i < name.length){
				seg = seg.getSubSegmentByName(name[i++]);
			}
			if(i != name.length) {
				if(Configuration.dbg) StdStreams.vrb.println("  i != name.length -> returning null");
				seg = null;
			}
		}
		return seg;
	}
	
	public int getNofHeaps() {
		return nofHeaps;
	}
	
	public int getNofStacks() {
		return nofStacks;
	}
	
	public Segment getHeapSegment(int i) {
		if(i >= 0 && i < nofHeaps) return heapSegments[i];
		return null;
	}
	
	public Segment getStackSegment(int i) {
		if(i >= 0 && i < nofStacks) return stackSegments[i];
		return null;
	}
	
	public void println(int indentLevel) {
		for (int i = indentLevel; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("memorymap {");
		Device current = devs;
		while (current != null) {
			current.println(indentLevel + 1);
			current = (Device)current.next;
		}

		if (modulesMap != null) {
			for (int i = indentLevel + 1; i > 0; i--) {
				StdStreams.vrb.print("  ");
			}
			StdStreams.vrb.println("modules {");
			Module mod = modulesMap;
			while (mod != null) {
				mod.println(indentLevel + 2);
			}
			for (int i = indentLevel + 1; i > 0; i--) {
				StdStreams.vrb.print("  ");
			}
			StdStreams.vrb.println("}");
		}

		for (int i = indentLevel; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
		StdStreams.vrb.println("}");

	}

}
