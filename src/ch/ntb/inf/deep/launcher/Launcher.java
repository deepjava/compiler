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

package ch.ntb.inf.deep.launcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.ntb.inf.deep.cfg.CFG;
import ch.ntb.inf.deep.cgPPC.CodeGen;
import ch.ntb.inf.deep.classItems.Array;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.config.Board;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Programmer;
import ch.ntb.inf.deep.config.Project;
import ch.ntb.inf.deep.config.Register;
import ch.ntb.inf.deep.config.TargetConfiguration;
import ch.ntb.inf.deep.config.ValueAssignment;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.Linker32;
import ch.ntb.inf.deep.linker.TargetMemorySegment;
import ch.ntb.inf.deep.ssa.SSA;
import ch.ntb.inf.deep.strings.HString;
import ch.ntb.inf.deep.target.NtbMpc555UsbBdi;
import ch.ntb.inf.deep.target.TargetConnection;
import ch.ntb.inf.deep.target.TargetConnectionException;

public class Launcher implements ICclassFileConsts {
	
	private static final boolean dbg = false;
	
	private static ErrorReporter reporter = ErrorReporter.reporter;
	private static PrintStream out = StdStreams.out;
	private static PrintStream vrb = StdStreams.vrb;
	private static TargetConnection tc;

	public static void buildAll(String projectConfigFile, String targetConfiguration) {
		int attributes = (1 << atxCode) | (1 << atxLocalVariableTable) | (1 << atxExceptions) | (1 << atxLineNumberTable);
		
		Class clazz;
		Method method;
		Array array;
		
		// Reset error reporter
		reporter.clear();
		
		// Read configuration
		if(dbg) vrb.println("[Launcher] Loading Configuration");
		Project project = Configuration.addProject(projectConfigFile);
		Configuration.setActiveProject(project);
		project.setActiveTargetConfiguration(targetConfiguration);
		
		try {
			// Read required classes
			if(reporter.nofErrors <= 0) {
				if(dbg) vrb.println("[Launcher] Loading Classfiles");
				Class.buildSystem(Configuration.getRootClassNames(), Configuration.getSearchPaths(), Configuration.getSystemPrimitives(), attributes);
			}
			
			// Initialize compiler components
			if(reporter.nofErrors <= 0) {
				if(dbg) vrb.println("[Launcher] Initializing Linker");
				Linker32.init();
			}
			if(reporter.nofErrors <= 0) {
				if(dbg) vrb.println("[Launcher] Initializing Code Generator");
				CodeGen.init();
			}
			
			// Proceeding Arrays: Loop One
			if(dbg) vrb.println("[Launcher] Proceeding arrays (loop one):");
			array = Class.arrayClasses;
			while(array != null) {
				if(dbg) vrb.println("> Array: " + array.name);
				if(dbg) vrb.println("  creating type descritpor");
				Linker32.createTypeDescriptor(array);
				array = array.nextArray;
			}
			
			// Proceeding Classes: Loop One
			if(dbg) vrb.println("[Launcher] Proceeding classes (loop one):");
			if(reporter.nofErrors <= 0) {
				for(int extLevel = 0; extLevel <= Class.maxExtensionLevelStdClasses; extLevel++) {
					if(dbg) vrb.println("  Extentsion level " + extLevel + ":");
					clazz = Class.elOrdredClasses[extLevel];
					while(clazz != null && reporter.nofErrors <= 0) { // TODO verkettung beachten
						if(dbg) vrb.println("  > Class: " + clazz.name);
						
						// Create constant block
						if(reporter.nofErrors <= 0) {
							if(dbg) vrb.println("    creating constant block");
							Linker32.createConstantBlock(clazz);
						}
						
						method = (Method)clazz.methods;
						while (method != null && reporter.nofErrors <= 0) {
							if ((method.accAndPropFlags & ((1 << dpfSynthetic) | (1 << apfAbstract))) == 0) { // proceed only methods with code
								if(dbg) vrb.println("    > Method: " + method.name + method.methDescriptor + ", accAndPropFlags: " + Integer.toHexString(method.accAndPropFlags));
	
								// Create CFG
								if(reporter.nofErrors <= 0) {
									if(dbg) vrb.println("      building CFG");
									method.cfg = new CFG(method);
								}
	
								// Create SSA
								if(reporter.nofErrors <= 0) {
									if(dbg) vrb.println("      building SSA");
									method.ssa = new SSA(method.cfg);
									//if(dbg) method.ssa.print(0);
								}
	
								// Create machine code
								if(reporter.nofErrors <= 0) {
									if(dbg) vrb.println("      creating machine code");
									method.machineCode = new CodeGen(method.ssa); // TODO 
								}
	
							}
							method = (Method)method.next;
						}
	
						// Linker: calculate required size
						if(reporter.nofErrors <= 0) {
							if(dbg) vrb.println("    calculating code size and offsets");
							Linker32.calculateCodeSizeAndOffsets(clazz);
						}
						
						clazz = clazz.nextExtLevelClass;
					}
				}
			}

			CodeGen.generateCompSpecSubroutines();
			
			// calculate code size and offsets for compiler specific methods
			Linker32.calculateCodeSizeAndOffsetsForCompilerSpecSubroutines();
			
			// Linker: create system table and freeze memory map
			if(reporter.nofErrors <= 0) {
				if(dbg) vrb.println("[Launcher] Creating system table");
				Linker32.createSystemTable();
				if(dbg) vrb.println("[Launcher] Freezing memory map");
				Linker32.freezeMemoryMap();
			}
			
			// Proceeding Arrays: Loop Two
			if(dbg) vrb.println("[Launcher] Proceeding arrays (loop two):");
			array = Class.arrayClasses;
			while(array != null && reporter.nofErrors <= 0) {
				if(dbg) vrb.println("> Array: " + array.name);
				if(dbg) vrb.println("  calculating absoute addresses");
				Linker32.calculateAbsoluteAddresses(array);
				array = array.nextArray;
			}
			
			// Proceeding Classes: Loop Two
			if(dbg) vrb.println("[Launcher] Proceeding classes (loop two):");
			if(reporter.nofErrors <= 0) {
				for(int extLevel = 0; extLevel <= Class.maxExtensionLevelStdClasses; extLevel++) {
					if(dbg) vrb.println("  Extentsion level " + extLevel + ":");
					clazz = Class.elOrdredClasses[extLevel];
					while(clazz != null && reporter.nofErrors <= 0) { // TODO verkettung beachten
						if(dbg) vrb.println("  > Class: " + clazz.name);
						
						// Linker: calculate absolute addresses
						if(dbg) vrb.println("    calculating absolute addresses");
						Linker32.calculateAbsoluteAddresses(clazz);
						
						clazz = clazz.nextExtLevelClass;
					}
				}
			}

			Linker32.calculateAbsoluteAddressesForCompSpecSubroutines();
			
			// Create global constant table
			if(dbg) vrb.println("[Launcher] Creating global constant table");
			Linker32.createGlobalConstantTable();
			
			// Proceeding Classes: Loop Three
			if(dbg) vrb.println("[Launcher] Proceeding classes (loop three):");
			if(reporter.nofErrors <= 0) {
				for(int extLevel = 0; extLevel <= Class.maxExtensionLevelStdClasses; extLevel++) {
					if(dbg) vrb.println("  Extentsion level " + extLevel + ":");
					clazz = Class.elOrdredClasses[extLevel];
					while(clazz != null && reporter.nofErrors <= 0) { // TODO verkettung beachten
						if(dbg) vrb.println("  > Class: " + clazz.name);
		
						// Linker: update constant block
						if(dbg) vrb.println("    updating constant block");
						Linker32.updateConstantBlock(clazz);
						
						method = (Method)clazz.methods;
						while(method != null && reporter.nofErrors <= 0) {
							if((method.accAndPropFlags & ((1 << dpfSynthetic) | (1 << apfAbstract))) == 0) { // proceed only methods with code
								if(dbg) vrb.println("    > Method: " + method.name + method.methDescriptor + ", accAndPropFlags: " + Integer.toHexString(method.accAndPropFlags));
	
								// Code generator: fix up
								if(dbg) vrb.println("      doing fixups");
								method.machineCode.doFixups();
								//if(dbg) vrb.println(method.machineCode.toString());
							}
							method = (Method)method.next;
						}
						
						clazz = clazz.nextExtLevelClass;
					}
				}
			}
			Method m = Method.compSpecSubroutines;	// Code generator: fix up
			while(m != null) {
				if(dbg) vrb.println("    > Method: " + m.name + m.methDescriptor + ", accAndPropFlags: " + Integer.toHexString(m.accAndPropFlags));
				if(dbg) vrb.println("      doing fixups");
				m.machineCode.doFixups();
				m = (Method)m.next;
			}

			
			// Linker: Create target image
			if(reporter.nofErrors <= 0) {
				if(dbg) vrb.println("[Launcher] Generating target image");
				Linker32.generateTargetImage();
			}
			
			// Create target command table file if necessary
			if(reporter.nofErrors <= 0) {
				HString tctFileName = Configuration.getActiveProject().getTctFile();
				if(tctFileName != null) {
					saveCommandTableToFile(tctFileName.toString());
				}
			}
			
			if(reporter.nofErrors > 0) {
				out.println("Compilation failed with " + reporter.nofErrors + " error(s)");
				out.println();
			} else {
				out.println("Compilation and target image generation successfully finished");
				out.println();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void downloadTargetImage() {
		if(reporter.nofErrors <= 0) {
			Board b = Configuration.getBoard();
			TargetConfiguration targetConfig = Configuration.getActiveTargetConfiguration();
			TargetMemorySegment tms = Linker32.targetImage;
			if(b != null) {
				int c = 0;
				while(tc == null) {
					if(dbg) vrb.println("[Launcher] Opening target connection");
					openTargetConnection();
					c++;
					if(c > 4) {
						reporter.error(800, "Can't open target connection: tried 5 times");
						return;
					}
				}
				try {
					if(dbg) vrb.println("[Launcher] Initializing registers");
					tc.initRegisters(b.getCpuRegisterInits());
					tc.initRegisters(b.getBoardRegisterInits());
					tc.initRegisters(targetConfig.getRegisterInits());
					for(int i = 0; i < b.getCPU().getRegisterMap().getNofGprs(); i++) {
						tc.setGprValue(i, 0);
					}
					vrb.println("Downloading target image:");
					while(tms != null) {
						if(dbg) vrb.print("  Proceeding TMS #" + tms.id);
						if(tms.segment == null ){ // this should never happen
							// TODO add error message here
							if(dbg) vrb.println(" -> skipping (segment not defined)");
						}
						else {
							if(dbg) vrb.println(" -> writing " + tms.data.length * 4 + " bytes to address 0x" + Integer.toHexString(tms.startAddress) + " on device " + tms.segment.owner.getName());
							if(dbg) {
//								vrb.print(" [");
//								for(int i = 0; i < tms.data.length; i++) {
//									vrb.print(String.format("0x%08X", tms.data[i]));
//									vrb.print(' ');
//								}
//								vrb.println(" ]");
							}
							tc.writeTMS(tms);
						}
						tms = tms.next;
					}
				} catch(TargetConnectionException e) {
					reporter.error(TargetConnection.errDownloadFailed);
					reporter.nofErrors++;
				}
			}
			else {
				// TODO add error message here
			}
		}
		else {
			reporter.error(TargetConnection.errNoTargetImage);
			reporter.nofErrors++;
		}
	}
	
	public static void startTarget() {
		vrb.println("Starting target");
		if (reporter.nofErrors <= 0) {
			if(tc != null) {
				try {
					tc.startTarget();
				} catch (TargetConnectionException e) {
					reporter.error(TargetConnection.errStartTargetFailed);
					reporter.nofErrors++;
				}
			}
		}
	}
	
	public static void stopTarget() {
		try {
			tc.stopTarget();
		} catch (TargetConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void openTargetConnection() {
		if(dbg) vrb.println("[Launcher] Opening target connection");
		tc = getTargetConnection();
		if(tc != null) {
			if(dbg) vrb.println(" -> ok");
			try {
				if(dbg) vrb.println("  Initializing target connection");
				tc.init();
			} catch (TargetConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			System.out.println("[ERROR] Can't get a connection to the target"); // TODO improve this
		}
	}
	
	public static void reopenTargetConnection() {
		if(tc != null) {
			try {
				tc.closeConnection();
				tc.openConnection();
			} catch (TargetConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void closeTargetConnection() {
		if(dbg) vrb.println("[Launcher] Closing target connection");
		if(tc != null) tc.closeConnection();
	}

	public static void saveTargetImageToFile(String fileName, int format) {
		if(reporter.nofErrors <= 0){
			try {
				switch(format) {
				case Configuration.BIN:
					Linker32.writeTargetImageToBinFile(fileName);
					break;
				case Configuration.HEX:
					break;
				case Configuration.SREC: 
					break;
				case Configuration.DTIM:
					Linker32.writeTargetImageToDtimFile(fileName);
					break;
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void saveTargetImageToFile() {
		String timDirName = "tim";
		File timDir = new File(timDirName);
		if(!timDir.isDirectory()) {
			timDir.mkdir();
		}
		saveTargetImageToFile(timDirName + "/targetimage.dtim", Configuration.DTIM);
	}
	
	public static void saveCommandTableToFile(String fileName) {
		if(reporter.nofErrors <= 0){
			File path = new File(fileName.substring(0, fileName.lastIndexOf('/')));
			path.mkdirs(); // create directories if not existing
			try {
				Linker32.writeCommandTableToFile(fileName);
			} catch (IOException e) { 
				e.printStackTrace();
			}
		}
	}
	
	public static TargetConnection getTargetConnection() {
		if(tc == null) {
			Programmer programmer = Configuration.getProgrammer();
			if(programmer != null) {
				HString programmerName = programmer.getName();
				if(programmerName == HString.getRegisteredHString("ntbMpc555UsbBdi")) {
					if(dbg) vrb.println("  Getting instance of target connection for ntbMpc555UsbBdi");
					tc = NtbMpc555UsbBdi.getInstance();
				}
				else {
					System.out.println("ERROR: Programmer \"" + programmerName + "\" not found"); // TODO improve this
				}
			}
			else {
				System.out.println("ERROR: Programmer not defined"); // TODO improve this
			}
		}
		return tc;
	}
	
	public static void createInterfaceFile(String fileToCreate) {
		createInterfaceFile(fileToCreate, Configuration.getBoard());
	}
	
	public static void createInterfaceFile(String fileToCreate, Board b) {
		int indexOf;
		String pack;
		String className;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		try {
			indexOf = fileToCreate.lastIndexOf(File.separatorChar);
			if (indexOf != -1) {
				className = fileToCreate.substring(indexOf + 1).toString();
				pack = fileToCreate.substring(0, indexOf).toString();
			} else {
				className = fileToCreate.toString();
				pack = "";
			}
			// check if path exists
			File f = new File(fileToCreate.substring(0, indexOf).toString());
			if (!f.exists()) {
				f.mkdirs();
			}
			FileWriter fw = new FileWriter(fileToCreate.toString());
			indexOf = pack.lastIndexOf(File.separatorChar);
			if (indexOf != -1) {
				pack = pack.substring(indexOf + 1) + ";";
			}
			pack = pack.replace(File.separatorChar, '.');
			fw.write("package ch.ntb.inf.deep.runtime." + pack + "\n\n");
			fw.write("// Auto generated file (" + dateFormat.format(date) + ")\n\n");
			indexOf = className.indexOf(".");
			fw.write("public interface " + className.substring(0, indexOf) + " {\n");
			fw.write("\n\t// System constants of CPU " + b.getCPU().getName() + "\n");
			ValueAssignment current = b.getCPU().getSysConstants().getFirstConstant();
			while(current != null) {
				fw.write("\tpublic static final int " + current.getName().toString() + " = 0x" + Integer.toHexString(current.getValue()) + ";\n");
				current = (ValueAssignment)current.next;
			}
			fw.write("\n\t// System constants of board " + b.getName() + "\n");
			current = b.getSysConstants().getFirstConstant();
			while(current != null) {
				fw.write("\tpublic static final int " + current.getName().toString() + " = 0x" + Integer.toHexString(current.getValue()) + ";\n");
				current = (ValueAssignment)current.next;
			}
			fw.write("\n\t// Registermap GPR\n");
			Register reg = b.getCPU().getRegisterMap().getGprRegisters();
			while (reg != null) {
				fw.write("\tpublic static final int " + reg.getName() + " = 0x"
						+ Integer.toHexString(reg.getAddress()) + ";\n");
				reg = (Register)reg.next;
			}
			fw.write("\n\t// Registermap FPR\n");
			reg = b.getCPU().getRegisterMap().getFprRegisters();
			while (reg != null) {
				fw.write("\tpublic static final int " + reg.getName() + " = 0x"
						+ Integer.toHexString(reg.getAddress()) + ";\n");
				reg = (Register)reg.next;
			}
			fw.write("\n\t// Registermap SPR\n");
			reg = b.getCPU().getRegisterMap().getSprRegisters();
			while (reg != null) {
				fw.write("\tpublic static final int " + reg.getName() + " = 0x"
						+ Integer.toHexString(reg.getAddress()) + ";\n");
				reg = (Register)reg.next;
			}
			fw.write("\n\t// Registermap IOR\n");
			reg = b.getCPU().getRegisterMap().getIorRegisters();
			while (reg != null) {
				fw.write("\tpublic static final int " + reg.getName() + " = 0x"
						+ Integer.toHexString(reg.getAddress()) + ";\n");
				reg = (Register)reg.next;
			}
			fw.write("}");
			fw.flush();
			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
