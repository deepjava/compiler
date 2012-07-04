package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class Board extends ConfigElement {
	HString description;
	CPU cpu;
	Constants sysConstants;
	MemoryMap memorymap;
	RegisterInitList reginit;
	TargetConfiguration targetConfigs;
	
	public Board(String jname) {
		this.name = HString.getRegisteredHString(jname);
		sysConstants = new Constants(this.name + " sysConstants", true);
		reginit = new RegisterInitList();
	}
	
	public void setDescription(String desc) {
		this.description = HString.getRegisteredHString(desc);
	}
	
	public void setCpu(CPU cpu) {
		this.cpu = cpu;
		this.reginit.setCpu(cpu);
	}
	
	public CPU getCPU() {
		return cpu;
	}
	
	public HString getDescription() {
		return this.description;
	}
	
	public Device getDeviceByName(HString name) {
		Device dev = memorymap.getDeviceByName(name);
		if(dev == null) { // if not found in board config, look in CPU config
			dev = cpu.memorymap.getDeviceByName(name);
		}
		return dev;
	}
	
	public Device getDeviceByName(String devName) {
		return getDeviceByName(HString.getRegisteredHString(devName));
	}
	
	public Device[] getAllDevices() {
		int nofdevs = memorymap.getNofDevices() + cpu.memorymap.getNofDevices();
		Device[] devs = new Device[nofdevs];
		Device d = memorymap.getDevices();
		int i = 0;
		while(d != null) {
			devs[i++] = d;
			d = (Device)d.next;
		}
		d = cpu.memorymap.getDevices();
		while(d != null) {
			devs[i++] = d;
			d = (Device)d.next;
		}
		return devs;
	}

	public Device[] getDevicesByType(HString memoryType) {
		if(Configuration.dbg) StdStreams.vrb.println("[Conf] Board: looking for devices of type " + memoryType);
		int nofDevs = 0;
		Device[] devs;
		Device d = memorymap.getDevices();
		while(d != null) { // board
			if(d.getMemoryType() == memoryType) nofDevs++;
			d = (Device)d.next;
		}
		d = cpu.memorymap.getDevices();
		while(d != null) { // cpu
			if(d.getMemoryType() == memoryType) nofDevs++;
			d = (Device)d.next;
		}
		devs = new Device[nofDevs];
		if(Configuration.dbg) StdStreams.vrb.println("  Devices: " + nofDevs);
		nofDevs = 0;
		d = memorymap.getDevices();
		while(d != null) { // board
			if(d.getMemoryType() == memoryType) {
				if(Configuration.dbg) StdStreams.vrb.println("  - " + d.getName());
				devs[nofDevs++] = d;
			}
			d = (Device)d.next;
		}
		d = cpu.memorymap.getDevices();
		while(d != null) { // c
			if(d.getMemoryType() == memoryType) {
				if(Configuration.dbg) StdStreams.vrb.println("  - " + d.getName());
				devs[nofDevs++] = d;
			}
			d = (Device)d.next;
		}
		return devs;
	}
	
	public RegisterInitList getCpuRegisterInits() {
		return cpu.reginit;
	}
	
	public RegisterInitList getBoardRegisterInits() {
		return reginit;
	}
	
	public void addTargetConfiguration(TargetConfiguration newTC) {
		if(targetConfigs == null) {
			targetConfigs = newTC;
		}
		else {
			// Check if there is already a target configuration with the same name
			TargetConfiguration tc = targetConfigs;
			while(tc != null && tc.name != newTC.name) {
				tc = (TargetConfiguration)tc.next;
			}
			if(tc == null) {
				targetConfigs.append(newTC);
			}
			else {
				System.out.println("WARNING: TargetConfig with name " + newTC.name + " exists already!"); // TODO add proper warning here!!!
			}
		}
	}
	
	public TargetConfiguration getTargetConfigurationByName(String tcName) {
		return (TargetConfiguration)targetConfigs.getElementByName(tcName);
	}
	
	public int getValueFor(String constName) {
		int val = sysConstants.getValueOfConstant(constName);
		if(val == Integer.MIN_VALUE) val = cpu.sysConstants.getValueOfConstant(constName);
		return val;
	}
	
	public Constants getSysConstants() {
		return sysConstants;
	}
	
	public MemoryMap getMemoryMap() {
		return memorymap;
	}
	
}
