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
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

import ch.ntb.inf.deep.classItems.*;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class Configuration implements ICclassFileConsts {
		
	public static final boolean dbg = false; // enable/disable debugging outputs for the configuration
	
	protected static final int maxNofRootClasses = 64;
	public static final int maxNofLibPaths = 8;
	protected static final int maxNumbersOfHeapSegments = 4;
	protected static final int maxNumbersOfStackSegments = 4;
	protected static final int maxNumbersOfSysTables = 4;
	
	protected static final String defaultTctFileName = "tct/commandTable.dtct";
	protected static final String basePath = "/config/base";
	protected static final String archPath = "/config/arch";	
	protected static final String cpuPath = "/config/cpus";
	public static final String boardsPath = "/config/boards";
	public static final String osPath = "/config/operatingsystems";
	public static final String progPath = "/config/programmers";
	
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
	public static final HString RESET = HString.getRegisteredHString("reset");
	public static final HString RESETCLASS = HString.getRegisteredHString("Reset");
	public static final HString SYSTEMTABLE = HString.getRegisteredHString("systemtable");
	public static final HString AM29LV160D = HString.getRegisteredHString("Am29LV160d");

	private static final ErrorReporter reporter = ErrorReporter.reporter;
	private static PrintStream vrb = StdStreams.vrb;
	
	private static Project project; // active project
	private static HString[] rootClassNames;
	private static SystemConstant compilerConstants;
	private static Board board;
	private static OperatingSystem os;
	private static Programmer programmer;
	private static RunConfiguration activeRunConfig;
	
	public static Project readProjectFile(String projectFile) {
		clear();

		if (dbg) vrb.println("[CONF] Configuration: reading project " + projectFile);
		project = new Project(projectFile);
		new Parser(project).parse();
		if (reporter.nofErrors > 0) return null;
		
		readConfigDir(basePath);
		if (reporter.nofErrors > 0) return null;
//		printCompilerConstants(1);
		
		readConfigFile(boardsPath, board);
		if (reporter.nofErrors > 0) return null;
//		printArchRegisters(1);
//		printCpuRegisters(1);
//		printCpuSysConstants(1);
//		printBoardSysConstants(1);
		
		readConfigFile(osPath, os);
		if (reporter.nofErrors > 0) return null;
		os.addSysClassesAndMethods();
//		printSystemClasses(2);
//		printSystemMethods(2);
		
		readConfigFile(progPath, programmer);
		if (reporter.nofErrors > 0) return null;
//		printArchRegisters(2);
//		printCpuRegisters(2);
//		printCpuRegsInit(2);
//		printBoardRegsInit(2);
//		printBoardMemMap(2);
//		printCpuMemMap(2);
//		printTargetConfigurations(2);
		return project;
	}
	
	public static void readProjectFileAndRunConfigs(String projectFile) {
		clear();
		if (dbg) vrb.println("[CONF] Configuration: reading project " + projectFile);
		project = new Project(projectFile);
		new Parser(project).parse();
		if (reporter.nofErrors > 0) return;
		readRunConfigs(boardsPath, board);
		if (reporter.nofErrors > 0) return;
	}

	public static void readConfigDir(String dir) {
		File[] configDir = new File[project.libPath.length];
		for (int i = 0; i < configDir.length; i++) configDir[i] = new File(project.libPath[i] + dir);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.startsWith(".") && name.endsWith(".deep");
			}
		};
		for (int i = 0; i < configDir.length; i++) {
			String[] configFileNames = configDir[i].list(filter);
			if (configFileNames != null) {
				for (int j = 0; j < configFileNames.length; j++) {
					if (dbg) StdStreams.vrb.println("[CONF] Reading config file from " + configDir[i].toString() + "\\" + configFileNames[j]);
					File cfgFile = new File(configDir[i] + "/" + configFileNames[j]);
					new Parser(cfgFile).parse();
				}
			}
		}
	}

	public static void readConfigFile(String path, Item itm) {
		File[] configDir = new File[project.libPath.length];
		for (int i = 0; i < configDir.length; i++) configDir[i] = new File(project.libPath[i] + path);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.startsWith(".") && name.endsWith(".deep");
			}
		};
		Boolean found = false;
		for (int i = 0; i < configDir.length; i++) {
			String[] configFileNames = configDir[i].list(filter);
			if (configFileNames != null) {
				for (int j = 0; j < configFileNames.length; j++) {
					int index = configFileNames[j].lastIndexOf('.');
					String name = configFileNames[j].substring(0, index);
					if (itm.name.equals(HString.getHString(name))) {
						if (dbg) StdStreams.vrb.println("[CONF] Reading config file from " + configDir[i].toString() + "\\" + configFileNames[j]);
						found = true;
						File cfgFile = new File(configDir[i] + "/" + configFileNames[j]);
						new Parser(cfgFile).parse();
					}
				}
			}
		}
		if (!found) reporter.error(250, "" + itm.name);
	}

	public static void readRunConfigs(String path, Item itm) {
		File[] configDir = new File[project.libPath.length];
		for (int i = 0; i < configDir.length; i++) configDir[i] = new File(project.libPath[i] + path);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.startsWith(".") && name.endsWith(".deep");
			}
		};
		Boolean found = false;
		for (int i = 0; i < configDir.length; i++) {
			String[] configFileNames = configDir[i].list(filter);
			if (configFileNames != null) {
				for (int j = 0; j < configFileNames.length; j++) {
					int index = configFileNames[j].lastIndexOf('.');
					String name = configFileNames[j].substring(0, index);
					if (itm.name.equals(HString.getHString(name))) {
						if (dbg) StdStreams.vrb.println("[CONF] Reading config file from " + configDir[i].toString() + "\\" + configFileNames[j]);
						found = true;
						File cfgFile = new File(configDir[i] + "/" + configFileNames[j]);
						new Parser(cfgFile).parseRunConfigs();
					}
				}
			}
		}
		if (!found) reporter.error(250, "" + itm.name);
	}

	public static String[][] searchDescInConfig(File configDir, short symbol) {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.startsWith(".") && name.endsWith(".deep");
			}
		};
		Boolean found = false;
		String[] configFileNames = configDir.list(filter);
		ArrayList<String[]> names = new ArrayList<String[]>();
		if (configFileNames != null) {
			for (int j = 0; j < configFileNames.length; j++) {
				found = true;
				File cfgFile = new File(configDir + "/" + configFileNames[j]);
				String[] s = new Parser(cfgFile).parse(symbol);
				names.add(s);
			}
		}
		if (found) {
			Object[] o = names.toArray();
			return (Arrays.copyOf(o, o.length, String[][].class)); 
		}
		return new String[][] {{"not available", "not available"}};
	}

	public static void setCompilerConstants(SystemConstant constants) {
		compilerConstants = constants;
	}

	public static SystemConstant getCompilerConstants() {
		return compilerConstants;
	}
	
	public static void setRootClasses(HString[] names) {
		rootClassNames = names;
	}

	public static HString[] getRootClasses() {
		return rootClassNames;
	}
	
	public static void setBoard(String jname) {
		board = new Board(jname);
	}

	public static Board getBoard() {
		return board;
	}

	public static void setOS(String jname) {
		os = new OperatingSystem(jname);
	}

	public static OperatingSystem getOS() {
		return os;
	}

	public static void setProgrammer(String jname) {
		programmer = new Programmer(jname);
	}

	public static Programmer getProgrammer() {
		return programmer;
	}

	public static Project getActiveProject() {
		return Configuration.project;
	}

	public static RunConfiguration getActiveTargetConfiguration() {
		return activeRunConfig;
	}

	public static void setActiveTargetConfig(String string) {
		RunConfiguration targetConfig = (RunConfiguration)board.getTargetConfigurationByName(string);
		if (targetConfig != null) activeRunConfig = targetConfig;
		else reporter.error(223, "Targetconfiguration \"" + string + "\" not found");
	}

	public static void clear() { 
		project = null;
		rootClassNames = null;
		compilerConstants = null;
		board = null;
		os = null;
		programmer = null;
	}
	
	public static Segment getCodeSegmentOf(Class cls) {
		return getSegmentOf(cls, CODE);
	}

	public static Segment getConstSegmentOf(Class cls) {
		return getSegmentOf(cls, CONST);
	}

	public static Segment getVarSegmentOf(Class cls) {
		return getSegmentOf(cls, VAR);
	}

	public static Segment getDefaultCodeSegment() {
		if (dbg) vrb.print("[CONF] looking for default " + CODE + " segment");
		Segment seg = null;
		seg = activeRunConfig.getModuleByName(DEFAULT).getSegment(CODE);		
		return seg;
	}
	
	public static Segment getDefaultConstSegment() {
		if (dbg) vrb.print("[CONF] looking for default " + CONST + " segment");
		Segment seg = null;
		seg = activeRunConfig.getModuleByName(DEFAULT).getSegment(CONST);		
		return seg;
	}
	
	public static Segment getDefaultVarSegment() {
		if (dbg) vrb.print("[CONF] looking for default " + VAR + " segment");
		Segment seg = null;
		seg = activeRunConfig.getModuleByName(DEFAULT).getSegment(VAR);		
		return seg;
	}
	
	private static Segment getSegmentOf(Class cls, HString attr) {
		if (dbg) vrb.print("[CONF] looking for " + attr + " segment for class " + cls.name);
		Segment seg = null;
		Module module;

			if (os.kernelClass == cls) {
				if (dbg) vrb.print("  -> KERNEL");
				module = activeRunConfig.getModuleByName(KERNEL);
				if (module != null) seg = module.getSegment(attr);
			} else if (os.heapClass == cls) {
				if (dbg) vrb.print("  -> HEAP");
				module = activeRunConfig.getModuleByName(HEAP);
				if (module != null) seg = module.getSegment(attr);
			} else if (os.exceptionBaseClass == cls) {
				if (dbg) vrb.print("  -> EXCEPTION BASE");
				module = activeRunConfig.getModuleByName(EXCEPTION);
				if (module != null) seg = module.getSegment(attr);
			} else if ((cls.accAndPropFlags & (1<<dpfExcHnd)) != 0) {
				if (dbg) vrb.print("  ->EXCEPTION ");
				int i = 0;
				Class tempCls = os.exceptions[i];
				while (i < os.nofExcClasses) {
					if (dbg) vrb.print(tempCls.name);
					if (tempCls.name == cls.name) {
						if (dbg) vrb.println(" -> found");
						seg = activeRunConfig.getModuleByName(EXCEPTION).getSegment(attr);
						break;
					} else if (dbg) vrb.print(" -> skiped, next: ");
					i++;
					tempCls = os.exceptions[i];
				}
			} 
			if (seg == null) {	// class without module assignment
				// Class is not a system class
				if (dbg) vrb.print(" -> is not a system class -> looking for module");
				Module mod = activeRunConfig.getModuleByName(cls.name);
				if (mod != null) {
					if(dbg) vrb.println(" -> found");
					seg = mod.getSegment(attr);
				}
				else {
					if(dbg) vrb.println(" -> not found -> using default segment");
					// module for Class not found, load default
					seg = activeRunConfig.getModuleByName(DEFAULT).getSegment(attr);
				}
			}
			return seg;
	}

	public static File[] getSearchPaths() {
		File[] libPaths = project.libs;
		File[] javaSearchPaths = new File[libPaths.length + 1];
		javaSearchPaths[0] = new File(project.getProjectDir().getAbsolutePath() + File.separator + "bin" + File.separator);
		for (int i = 0; i < libPaths.length; i++) {
			if (libPaths[i].isDirectory()) { // directory
				javaSearchPaths[i + 1] = new File(libPaths[i].getAbsolutePath() + File.separator + "bin" + File.separator);
			}
			else { // jar file
				javaSearchPaths[i + 1] = libPaths[i];
			}
		}
		if (dbg) {
			vrb.println("[CONF] javaSearchPath:");
			for (File f : javaSearchPaths) vrb.print("  " + f.getAbsolutePath() + "\n");
			vrb.println();
		}
		return javaSearchPaths;
	}

	public static int getNumberOfStacks() {
		return board.memorymap.nofStackSegments + board.cpu.memorymap.nofStackSegments;
	}

	public static int getNumberOfHeaps() {
		return board.memorymap.nofHeapSegments + board.cpu.memorymap.nofHeapSegments;
	}

	public static int getResetOffset() {
		Method m = os.getSystemMethodByName(os.resetClass, RESET);
		assert (m != null);
		return m.offset;
	}
	
	public static Segment[] getSysTabSegments() {
		if (dbg) vrb.println("[CONF] getSysTabSegments");
		Segment[] sysTabSegments = new Segment[1];
		Module mod = (Module)activeRunConfig.system.getItemByName(SYSTEMTABLE);
		if (mod == null) {reporter.error(237, SYSTEMTABLE.toString());	return null;}
		Segment seg = mod.getSegment(SYSTAB);
		sysTabSegments[0] = seg;
		return sysTabSegments;
	}
	
	public static Class[] getSystemClasses() {
		return os.sysClasses;
	}

	public static Register getRegisterByName(String regName) {
		assert board != null;
		return (Register) board.cpu.regs.getItemByName(regName);
	}
	
	public static int getValOfCompConstByName(String jname) {
		SystemConstant c = (SystemConstant) compilerConstants.getItemByName(jname);
		return c.val;
	}
	
	public static Device[] getAllDevices() {
		int nofdevs = board.memorymap.getNofDevices() + board.cpu.memorymap.getNofDevices();
		Device[] devs = new Device[nofdevs];
		Device d = board.memorymap.devs;
		int i = 0;
		while (d != null) {
			devs[i++] = d;
			d = (Device)d.next;
		}
		d = board.cpu.memorymap.devs;
		while (d != null) {
			devs[i++] = d;
			d = (Device)d.next;
		}
		return devs;
	}
	
	public static Device[] getDevicesByType(HString memoryType) {
		if (dbg) StdStreams.vrb.println("[Conf] Board: looking for devices of type " + memoryType);
		int nofDevs = 0;
		Device[] devs;
		Device d = board.memorymap.devs;
		while (d != null) { // board
			if (d.memorytype == memoryType) nofDevs++;
			d = (Device)d.next;
		}
		d = board.cpu.memorymap.devs;
		while (d != null) { // cpu
			if (d.memorytype == memoryType) nofDevs++;
			d = (Device)d.next;
		}
		devs = new Device[nofDevs];
		if (dbg) StdStreams.vrb.println("  Devices: " + nofDevs);
		nofDevs = 0;
		d = board.memorymap.devs;
		while (d != null) { // board
			if (d.memorytype == memoryType) {
				if (dbg) StdStreams.vrb.println("  - " + d.name);
				devs[nofDevs++] = d;
			}
			d = (Device)d.next;
		}
		d = board.cpu.memorymap.devs;
		while (d != null) { // c
			if (d.memorytype == memoryType) {
				if (dbg) StdStreams.vrb.println("  - " + d.name);
				devs[nofDevs++] = d;
			}
			d = (Device)d.next;
		}
		return devs;
	}
	
	public static Segment[] getHeapSegments() {
		int nofHeapSegments = getNumberOfHeaps();
		Segment[] heapSegments = new Segment[nofHeapSegments];
		int j = 0;
		for (int i = 0; i < board.memorymap.nofHeapSegments; i++) heapSegments[j++] = board.memorymap.getHeapSegment(i);
		for (int i = 0; i < board.cpu.memorymap.nofHeapSegments; i++) heapSegments[j++] = board.cpu.memorymap.getHeapSegment(i);
		return heapSegments;
	}

	public static Segment[] getStackSegments() {
		int nofStackSegments = getNumberOfStacks();
		Segment[] stackSegments = new Segment[nofStackSegments];
		int j = 0;
		for (int i = 0; i < board.memorymap.nofStackSegments; i++) stackSegments[j++] = board.memorymap.getStackSegment(i);
		for (int i = 0; i < board.cpu.memorymap.nofStackSegments; i++) stackSegments[j++] = board.cpu.memorymap.getStackSegment(i);
		return stackSegments;
	}
	
	private static void indent(int indentLevel) {
		StdStreams.vrbPrintIndent(indentLevel);
	}

	public static void printProject() {
		vrb.println(project.toString());
	}

	public static void printSystemClasses(int indentLevel){
		indent(indentLevel);
		vrb.println("system classes");
		Item[] items = os.sysClasses;
		for (Item itm : items) itm.print(indentLevel);
		vrb.println();	
	}

	public static void printSystemMethods(int indentLevel){
		indent(indentLevel);
		vrb.println("system methods");
		Item[] items = os.sysClasses;
		for (int i = 0; i < items.length; i++) {
			Item itm = items[i];
			indent(indentLevel+1); itm.printName(); vrb.println();
			Method[] meths = os.sysMethods[i];
			for (Method m : meths) {m.print(indentLevel+2); vrb.println();}
		}
		vrb.println();	
	}

	public static void printCpuMemMap(int indentLevel) {
		indent(indentLevel);
		vrb.println("memory map of cpu " + board.cpu.name + " {");
		board.cpu.memorymap.print(indentLevel+1);
		indent(indentLevel); vrb.println("}");
	}

	public static void printBoardMemMap(int indentLevel) {
		indent(indentLevel);
		vrb.println("memory map of board " + board.name + " {");
		board.memorymap.print(indentLevel+1);
		indent(indentLevel); vrb.println("}");
	}
	
	public static void printCompilerConstants(int indentLevel){
		indent(indentLevel);
		vrb.println("compiler constants {");
		printList(compilerConstants, indentLevel);
		indent(indentLevel); vrb.println("}");
	}
	
	public static void printCpuSysConstants(int indentLevel){
		indent(indentLevel);
		vrb.println("cpu system constants {");
		printList(board.cpu.sysConstants, indentLevel);
		indent(indentLevel); vrb.println("}");
	}

	public static void printBoardSysConstants(int indentLevel){
		indent(indentLevel);
		vrb.println("board system constants {");
		printList(board.sysConstants, indentLevel);
		indent(indentLevel); vrb.println("}");
	}

	public static void printCpuRegisters(int indentLevel){
		indent(indentLevel);
		vrb.println("cpu registers {");
		printList(board.cpu.regs, indentLevel);
		indent(indentLevel); vrb.println("}");
	}

	public static void printArchRegisters(int indentLevel){
		indent(indentLevel);
		vrb.println("architecture registers {");
		printList(board.cpu.arch.regs, indentLevel);
		indent(indentLevel); vrb.println("}");
	}

	public static void printCpuRegsInit(int indentLevel){
		indent(indentLevel);
		vrb.println("cpu initialised registers {");
		printList(board.cpu.regInits, indentLevel);
		indent(indentLevel); vrb.println("}");
	}

	public static void printBoardRegsInit(int indentLevel){
		indent(indentLevel);
		vrb.println("board initialised registers {");
		printList(board.regInits, indentLevel);
		indent(indentLevel); vrb.println("}");
	}

	private static void printList(Item itm, int indentLevel) {
		while (itm != null) {
			indent(indentLevel+1);
			vrb.println(itm.toString());
			itm = itm.next;
		}
	}

	public static void printTargetConfigurations(int indentLevel){
		indent(indentLevel);
		vrb.println("target configurations {");
		RunConfiguration tc = board.runConfig;
		while (tc != null) {
			indent(indentLevel+1);
			vrb.println(tc.name + " {");
			indent(indentLevel+2);
			vrb.println("description = " + tc.description);
			indent(indentLevel+2);
			vrb.println("system modules {");
			Module m = tc.system;
			while (m != null) {m.print(indentLevel+3); m = (Module) m.next;}
			indent(indentLevel+2); 
			vrb.println("}");
			indent(indentLevel+2);
			vrb.println("modules {");
			m = tc.modules;
			while (m != null) {m.print(indentLevel+3); m = (Module) m.next;}
			indent(indentLevel+2); 
			vrb.println("}");
			indent(indentLevel+2);
			vrb.println("initialised registers {");
			printList(tc.regInits, indentLevel+3);
			indent(indentLevel+2); 
			vrb.println("}");
			indent(indentLevel+1); vrb.println("}");
			tc = (RunConfiguration) tc.next;
		}
		indent(indentLevel); vrb.println("}");
	}

	public static void print() {
		vrb.println("project settings");
		project.print(1);
		printCompilerConstants(1);
		printCpuSysConstants(1);
		printCpuMemMap(1);
		printBoardMemMap(1);
		printTargetConfigurations(1);
		vrb.println("active target configuration = " + activeRunConfig.name);
		vrb.println();
	}

}
