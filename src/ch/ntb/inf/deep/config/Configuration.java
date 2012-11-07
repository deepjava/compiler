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

import java.io.File;
import java.io.PrintStream;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class Configuration implements ErrorCodes, IAttributes, ICclassFileConsts {
		
	public static final boolean dbg = false; // enable/disable debugging outputs for the configuration
	
	protected static final int maxNofRootClasses = 64;
	protected static final int maxNofLibPaths = 8;
	protected static final int maxNumbersOfHeaps = 4;
	protected static final int maxNumbersOfStacks = 4;
	protected static final int maxNumbersOfSystables = 4;
	
	protected static final String defaultTctFileName = "tct/commandTable.dtct";
	
	public static final int BIN = 0;
	public static final int HEX = 1;
	public static final int SREC = 2;
	public static final int DTIM = 3;
	
	public static final HString CODE = HString.getRegisteredHString("code");
	public static final HString CONST = HString.getRegisteredHString("const");
	public static final HString VAR = HString.getRegisteredHString("var");
	public static final HString SYSTAB = HString.getRegisteredHString("systab");
	public static final HString DEFAULT = HString.getRegisteredHString("default");
	public static final HString KERNEL = HString.getRegisteredHString("kernel");
	public static final HString HEAP = HString.getRegisteredHString("heap");
	public static final HString STACK = HString.getRegisteredHString("stack");
	public static final HString EXCEPTION = HString.getRegisteredHString("exception");
	public static final HString SYSTEMTABLE = HString.getRegisteredHString("systemtable");
	public static final HString AM29LV160D = HString.getRegisteredHString("Am29LV160d");

	private static final ErrorReporter reporter = ErrorReporter.reporter;
	private static PrintStream vrb = StdStreams.vrb;
	
	public static Library libs; // list of all available libraries
	public static Project projects; // list of all open projects
	private static Project activeProject; // the currently active project
	
	public static Library addLibrary(HString path) {
		if(dbg) vrb.println("[CONF] Configuration: adding Library " + path);
		Library lib;
		boolean readConfigFiles = false;
		if(libs == null) {
			if(dbg) vrb.println("  Adding first lib");
			libs = new Library(path);
			lib = libs;
			readConfigFiles = true;
		}
		else {
			if(dbg) vrb.print("  Looking if library is already registered");
			lib = (Library)libs.getElementByName(path);
			if(lib == null) {
				if(dbg) vrb.println(" -> not found -> adding");
				lib = new Library(path);
				libs.append(lib);
				readConfigFiles = true;
			}
			else {
				if(dbg) vrb.println(" -> found -> updating"); // TODO improve this!
				ConfigElement prev = lib.prev;
				ConfigElement next = lib.prev;
				lib = new Library(path);
				lib.prev = prev;
				lib.next = next;
				readConfigFiles = true;
			}
		}
		if(readConfigFiles) {
			if(dbg) vrb.println("  Reading config files from " + lib.getPathAsString());
			lib.readConfig();
		}
		return lib;
	}
	
	public static Library addLibrary(String path) {
		return addLibrary(HString.getRegisteredHString(path));
	}
	
	public static Project addProject(String projectFile) {
		if(dbg) vrb.println("[CONF] Configuration: adding project " + projectFile);
		Project project;
		if(projects == null) {
			if(dbg) vrb.println("  Adding first project");
			projects = new Project(projectFile);
			project = projects;
		}
		else {
			if(dbg) vrb.print("  Checking if project is already registered");
			project = projects.getProjectByFileName(HString.getRegisteredHString(projectFile)); // 
			if(project == null) {
				if(dbg) vrb.println(" -> not found -> adding new project");
				project = new Project(projectFile);
				projects.append(project);
			}
			else {
				if(dbg) vrb.println(" -> found -> nothing to do");
			}
		}
		if(dbg) vrb.println("  Reading project file " + projectFile);
		project.readProjectFile();
		return project;
	}
	
	public static void clear() { // TODO check this
		Parser.clear();
		projects = null;
		activeProject = null;
		libs = null;
	}
	
	public static Segment getCodeSegmentOf(HString clazz) {
		return getSegmentOf(clazz, CODE);
	}

	public static Segment getConstSegmentOf(HString clazz) {
		return getSegmentOf(clazz, CONST);
	}

	public static Segment getVarSegmentOf(HString clazz) {
		return getSegmentOf(clazz, VAR);
	}

	public static Segment getDefaultCodeSegment() {
		return getDefaultSegment(CODE);
	}
	
	public static Segment getDefaultConstSegment() {
		return getDefaultSegment(CONST);
	}
	
	public static Segment getDefaultVarSegment() {
		return getDefaultSegment(VAR);
	}
	
	private static Segment getDefaultSegment(HString contentType){
		Segment seg;
		SegmentAssignment segAss = activeProject.getActiveTargetConfiguration().getModuleByName(DEFAULT).getSegmentAssignments();
		while(segAss != null) {
			if(segAss.contentAttribute.equals(contentType)) {
				String segDesignator = segAss.segmentDesignator.toString();
				int index = segDesignator.indexOf('.');
				// Determine Device name
				HString name = HString.getRegisteredHString(segDesignator.substring(0, index));
				segDesignator = segDesignator.substring(index + 1);
				Device dev = activeProject.board.getDeviceByName(name);
				if (dev == null) {
					ErrorReporter.reporter.error(errNoSuchDevice, name.toString() + "with segment for " + contentType + " not found");
					return null;
				}
				index = segDesignator.indexOf('.');
				if (index == -1) {
					return dev.getSegementByName(HString.getRegisteredHString(segDesignator));
				}
				name = HString.getRegisteredHString(segDesignator.substring(0, index));
				segDesignator = segDesignator.substring(index + 1);
				seg = dev.getSegementByName(name);
				index = segDesignator.indexOf('.');
				while (index != -1) {
					name = HString.getRegisteredHString(segDesignator.substring(0, index));
					segDesignator = segDesignator.substring(index + 1);
					seg = seg.getSubSegmentByName(name);
					index = segDesignator.indexOf('.');
				}
				return seg.getSubSegmentByName(HString.getRegisteredHString(segDesignator));
			}
			segAss = segAss.next;
		}

		// default segment for given content type not set
		return null;
	}
	
	private static Segment getSegmentOf(HString clazz, HString contentAttribute) {
		if(dbg) vrb.print("[CONF] looking for " + contentAttribute + " segment for class " + clazz);
		Segment seg;
		SegmentAssignment segAss = null;

		// first check if clazz is a system class
		Module module;
		if(activeProject.getOperatingSystem().getKernel().name.equals(clazz)) {
			if(dbg) vrb.print("  -> KERNEL");
			module = activeProject.activeTargetConf.getModuleByName(KERNEL);
			if(module != null) segAss = module.getSegmentAssignments();
		}
		else if (activeProject.getOperatingSystem().getHeap().name.equals(clazz)) {
			if(dbg) vrb.print("  -> HEAP");
			module = activeProject.activeTargetConf.getModuleByName(HEAP);
			if(module != null) segAss = module.getSegmentAssignments();
		}
		else if (activeProject.getOperatingSystem().getExceptionBaseClass().name.equals(clazz)) {
			if(dbg) vrb.print("  -> EXCEPTION BASE");
			module = activeProject.activeTargetConf.getModuleByName(EXCEPTION);
			if(module != null) segAss = module.getSegmentAssignments();
		}
		else {
			if(dbg) vrb.print("  -> Looking for exception: ");
			SystemClass tempCls = activeProject.getOperatingSystem().getExceptions();
			while (tempCls != null && (tempCls.attributes & (1 << dpfExcHnd)) != 0  && (tempCls != activeProject.getOperatingSystem().getExceptionBaseClass())) {
				if(dbg) vrb.print(tempCls.getName());
				if(tempCls.name.equals(clazz)) {
					if(dbg) vrb.println(" -> found");
					segAss = activeProject.activeTargetConf.getModuleByName(EXCEPTION).getSegmentAssignments();
					break;
				}
				else {
					if(dbg) vrb.print(" -> skiped, next: ");
				}
				tempCls = (SystemClass)tempCls.next;
			}
			if (segAss == null) {
				// Class is not a system class
				if(dbg) vrb.print(" -> class is not a system class -> looking for module");
				Module mod = activeProject.activeTargetConf.getModuleByName(clazz);
				if (mod != null) {
					if(dbg) vrb.println(" -> found");
					segAss = mod.getSegmentAssignments();
				}
				else {
					if(dbg) vrb.println(" -> not found -> using default segment");
					// module for Class not found load default
					segAss = activeProject.activeTargetConf.getModuleByName(DEFAULT).getSegmentAssignments();
				}
			}
		}
		while (segAss != null) {
			if (segAss.contentAttribute.equals(contentAttribute)) {
				String segDesignator = segAss.segmentDesignator.toString();
				int index = segDesignator.indexOf('.');
				// Determine Device name
				HString name = HString.getRegisteredHString(segDesignator.substring(0, index));
				segDesignator = segDesignator.substring(index + 1);
				Device dev = activeProject.board.getDeviceByName(name);
				if (dev == null) {
					ErrorReporter.reporter.error(errNoSuchDevice, name.toString() + "with segment for "	+ contentAttribute.toString());
					return null;
				}
				index = segDesignator.indexOf('.');
				if (index == -1) {
					return dev.getSegementByName(HString
							.getRegisteredHString(segDesignator));
				}
				name = HString.getRegisteredHString(segDesignator.substring(0, index));
				segDesignator = segDesignator.substring(index + 1);
				seg = dev.getSegementByName(name);
				index = segDesignator.indexOf('.');
				while (index != -1) {
					name = HString.getRegisteredHString(segDesignator.substring(0, index));
					segDesignator = segDesignator.substring(index + 1);
					seg = seg.getSubSegmentByName(name);
					index = segDesignator.indexOf('.');
				}
				return seg.getSubSegmentByName(HString.getRegisteredHString(segDesignator));
			}
			segAss = segAss.next;
		}

		// segment for content attribute not set
		return null;
	}

	public static Segment getSegmentByFullName(String fullQualifiedName) {
		if(dbg) vrb.println("[CONF] Configuration: getSegmentByFullName");
		String[] name = fullQualifiedName.split("\\.");
		if(dbg) vrb.println("  Looking for: " + name[0]);
		if(dbg) {
			for(int i = 1; i < name.length; i++) {
				indent(i + 1);
				vrb.println("  " + name[i]);
			}
		}
		int i = 0;
		Device dev = getDeviceByName(name[i++]);
		Segment seg = null;
		if(dev != null && name.length > 1) {
			if(dbg) vrb.println("  Device found: " + dev.getName());
			seg = dev.getSegementByName(name[i++]);
			if(dbg) {
				if(seg != null) vrb.println("  Segment found: " + seg.getName());
				else vrb.println("  Segment not found: " + name[i - 1]);
			}
			while(seg != null && i < name.length){
				seg = seg.getSubSegmentByName(name[i++]);
			}
			if(i != name.length) {
				vrb.println("  i != name.length -> returning null");
				seg = null;
			}
		}
		return seg;
	}
	
	public static Device getDeviceByName(String devName) {
		return activeProject.getBoard().getDeviceByName(devName);
	}
	
	public static Device getFirstDevice() {
		return (Device)activeProject.board.memorymap.getDevices().getHead();
	}

	public static File[] getSearchPaths() {
		File[] libPaths = activeProject.getLibPathAsFileArray();
		File[] javaSearchPaths = new File[libPaths.length + 1];
		javaSearchPaths[0] = new File(activeProject.getProjectDir().getAbsolutePath() + File.separator + "bin" + File.separator);
		for(int i = 0; i < libPaths.length; i++) {
			if(libPaths[i].isDirectory()){ // directory
				javaSearchPaths[i + 1] = new File(libPaths[i].getAbsolutePath() + File.separator + "bin" + File.separator);
			}
			else { // jar file
				javaSearchPaths[i + 1] = libPaths[i];
			}
		}
		return javaSearchPaths;
	}

	public static void setActiveProject(Project project) {
		Configuration.activeProject = project;

	}
	
	public static Project getActiveProject() {
		return Configuration.activeProject;
	}

	public static int getNumberOfStacks() {
		return activeProject.getNumberOfStacks();
	}

	public static int getNumberOfHeaps() {
		return activeProject.getNumberOfHeaps();
	}

	public static HString getHeapClassname() {
		if(activeProject.getOperatingSystem() != null) {
			if(activeProject.getOperatingSystem().getHeap() != null) {				
				return activeProject.getOperatingSystem().getHeap().getName();
			}
			else {
				reporter.error(999, "Heap not set for os " + activeProject.getOperatingSystem().getName());
			}
		}
		reporter.error(errOsNotFound, "Operationg system not set for project " + activeProject.getName());
		return null;
	}

	public static HString getKernelClassname() {
		if(activeProject != null) return activeProject.getOperatingSystem().getKernel().name;
		return null;
	}

	public static Board getBoard() {
		if(activeProject != null) return activeProject.getBoard();
		return null;
	}
	
	public static CPU getCpu() {
		if(activeProject != null) return activeProject.getBoard().getCPU();
		return null;
	}
	
	public static Programmer getProgrammer() {
		if(activeProject != null) return activeProject.getProgrammer();
		return null;
	}
	
	public static HString getExceptionClassname() {
		if(activeProject != null) return activeProject.getOperatingSystem().getExceptionBaseClass().name;
		return null;
	}

	public static int getResetOffset() { // TODO improve this!
		SystemMethod resetMethod = null;
		if(activeProject != null) resetMethod = activeProject.getOperatingSystem().getExceptionMethodByName("reset");
		if(resetMethod != null) return resetMethod.offset;
		return -1;
	}
	
	public static OperatingSystem getOperatingSystem() {
		if(activeProject != null) return activeProject.getOperatingSystem();
		return null;
	}

	public static TargetConfiguration getActiveTargetConfiguration() {
		return activeProject.getActiveTargetConfiguration();
	}

	public static Project getProjectByName(String projectName) {
		return getProjectByName(HString.getRegisteredHString(projectName));
	}

	public static Project getProjectByName(HString registeredProjectName) {
		return (Project)projects.getElementByName(registeredProjectName);
	}
	
	public static Segment[] getHeapSegments() {
		if(activeProject != null) return activeProject.getHeapSegments();
		return null;
	}

	public static Segment[] getStackSegments() {
		if(activeProject != null) return activeProject.getStackSegments();
		return null;
	}

	public static Segment[] getSysTabSegments() {
		if(dbg) vrb.println("[CONF] getSysTabSegments");
		Segment[] sysTabSegments = new Segment[maxNumbersOfSystables];
		int count = 0;
		Module sysTabModule = (Module)activeProject.getActiveTargetConfiguration().getSystemModules().getElementByName(SYSTEMTABLE);
		if(sysTabModule == null) {
			reporter.error(errSysModNotFound, SYSTEMTABLE.toString());
			return null;
		}
		if(dbg) vrb.println("  Looking for " + SYSTAB + " assignments in system module " + sysTabModule.getName() + ":");
		SegmentAssignment segAssignment = sysTabModule.getSegmentAssignments();
		while(segAssignment != null && count < sysTabSegments.length) {
			if(dbg) vrb.print("  Content attribute of segment assignment: " + segAssignment.contentAttribute);
			if(segAssignment.contentAttribute == SYSTAB) {
				if(dbg) vrb.print(" -> looking for segment " + segAssignment.segmentDesignator);
				sysTabSegments[count] = getSegmentByFullName(segAssignment.segmentDesignator.toString());
				if(sysTabSegments[count] == null) {
					if(dbg) vrb.println(" -> not found");
					reporter.error(errNoSuchSegment, segAssignment.segmentDesignator.toString());
					return null;
				}
				else {
					if(dbg) vrb.println(" -> found");
					count++;
				}
			}
			else {
				if(dbg) vrb.println(" -> skip");
			}
			segAssignment = segAssignment.next;
		}
		Segment[] availableSysTabSegments = new Segment[count];
		for(int i = 0; i < availableSysTabSegments.length; i++) {
			availableSysTabSegments[i] = sysTabSegments[i];
		}
		return availableSysTabSegments;
	}
	
	public static int getSystemMethodIdOf(String name){ // TODO improve this
		int hash = name.hashCode();
		SystemClass clazz = activeProject.getOperatingSystem().getClassList();
		while(clazz != null){
			SystemMethod meth = clazz.methods;
			while(meth != null){
				if(hash == meth.name.hashCode()){
					if(name.equals(meth.name)){
						return 0xFFF & meth.attributes;
					}
				}
				meth = meth.next;
			}
			clazz = (SystemClass)clazz.next;			
		}		
		return 0;
	}
	
	public static String getSystemMethodForID(int id){ // TODO improve this
		if((id & 0xFFFFF000) != 0){
			ErrorReporter.reporter.error(errInvalideParameter,
					"getSystemMethodForID parameter 0x" + Integer.toHexString(id) + " to large, only 12-bit numbers are allowed");
		}
		SystemClass clazz = activeProject.getOperatingSystem().getClassList();
		while(clazz != null){
			SystemMethod meth = clazz.methods;
			while(meth != null){
				if((meth.attributes & 0xFFF) == id){
					return meth.name;
				}
				meth = meth.next;
			}
			clazz = (SystemClass)clazz.next;
		}
		return null;
	}

	public static HString[] getRootClassNames() {
		return activeProject.getRootClasses();
	}

	public static String getTctFileName() {
		if(activeProject.getTctFile() != null) return activeProject.getTctFile().toString();
		return null;
	}
	
	public static SystemClass getSystemPrimitives() {
		return activeProject.getOperatingSystem().getClassList();
	}

	public static int getMemoryBaseAddress() {
		if(activeProject.getBoard() != null) {
			return activeProject.getBoard().getCPU().getInternalMemoryBase();
		}
		
		return -1;
	}
	
	public static Register getRegiststerByName(String regName) {
		if(activeProject.getBoard() != null) {
			return activeProject.getBoard().getCPU().getRegisterMap().getRegister(regName);
		}
		return null;
	}
	
	public static Device[] getDevicesByType(HString memoryType) {
		if(activeProject.getBoard() != null) {
			return activeProject.getBoard().getDevicesByType(memoryType);
		}
		return null;
	}
	
	protected static boolean setActiveTargetConfig(HString targetConfigName) {
		TargetConfiguration targetConfig = (TargetConfiguration)activeProject.board.targetConfigs.getElementByName(targetConfigName);
		if(targetConfig != null) {
			activeProject.setActiveTargetConfiguration(targetConfig);
		}
		ErrorReporter.reporter.error(errInconsistentattributes,	"Targetconfiguration \"" + targetConfigName + "\" not found");
		return false;
	}
	
	private static void indent(int indentLevel) {
		for (int i = indentLevel; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
	}

}
