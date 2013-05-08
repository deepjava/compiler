package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
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
