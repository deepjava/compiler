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

package ch.ntb.inf.deep.launcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.ntb.inf.deep.cfg.CFG;
import ch.ntb.inf.deep.cg.Code32;
import ch.ntb.inf.deep.cg.CodeGen;
import ch.ntb.inf.deep.cg.InstructionDecoder;
//import ch.ntb.inf.deep.cg.arm.CodeGenARM;
//import ch.ntb.inf.deep.cg.arm.InstructionDecoderARM;
import ch.ntb.inf.deep.cg.ppc.CodeGenPPC;
import ch.ntb.inf.deep.cg.ppc.InstructionDecoderPPC;
import ch.ntb.inf.deep.classItems.Array;
import ch.ntb.inf.deep.classItems.CFR;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.config.Arch;
import ch.ntb.inf.deep.config.Board;
import ch.ntb.inf.deep.config.CPU;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Project;
import ch.ntb.inf.deep.config.Register;
import ch.ntb.inf.deep.config.RegisterInit;
import ch.ntb.inf.deep.config.RunConfiguration;
import ch.ntb.inf.deep.config.SystemConstant;
import ch.ntb.inf.deep.host.Dbg;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.Linker32;
import ch.ntb.inf.deep.linker.TargetMemorySegment;
import ch.ntb.inf.deep.ssa.SSA;
import ch.ntb.inf.deep.strings.HString;
import ch.ntb.inf.deep.target.TargetConnection;
import ch.ntb.inf.deep.target.TargetConnectionException;

public class Launcher implements ICclassFileConsts {
	
	private static final boolean dbg = false;
	private static final boolean dbgProflg = false;
	
	private static ErrorReporter reporter = ErrorReporter.reporter;
	private static PrintStream log = StdStreams.log;
	private static PrintStream vrb = StdStreams.vrb;
	private static TargetConnection tc;
	private static long time;

	public static int buildAll(String deepProjectFileName, String targetConfigurationName) {
		// choose the attributes which should be read from the class file
		int attributes = (1 << atxCode) | (1 << atxLocalVariableTable) | (1 << atxExceptions) | (1 << atxLineNumberTable);

		Class clazz;
		Method method;
		Array array;
		
		// Reset error reporter
		reporter.clear();
		CFR.initBuildSystem();
		
		// Read configuration
		if (dbgProflg) time = System.nanoTime();
		if (dbg) vrb.println("[Launcher] Loading Configuration");
		log.println("Loading deep project file \"" + deepProjectFileName + "\"");
		Project project = Configuration.readProjectFile(deepProjectFileName);
		if (reporter.nofErrors <= 0) Configuration.setActiveTargetConfig(targetConfigurationName);
		if (dbgProflg) {vrb.println("duration for reading configuration = " + ((System.nanoTime() - time) / 1000) + "us"); time = System.nanoTime();}

		HString[] rootClassNames = Configuration.getRootClasses();
		if (reporter.nofErrors <= 0) {
			int nof = rootClassNames.length;
			if (nof == 0 || (nof == 1 && rootClassNames[0].equals(HString.getHString("")))) reporter.error(305);
			if (rootClassNames != null && reporter.nofErrors <= 0) {
				log.println("Root classes in project \"" + project.getProjectName() + "\":");
				for (int i = 0; i < rootClassNames.length; i++) {
					log.println("   " + rootClassNames[i]);
				}
			}
		}

		// Read required classes
		if (reporter.nofErrors <= 0) {
			if (dbg) vrb.println("[Launcher] Loading Classfiles");
			CFR.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemClasses(), attributes);
			//				CFR.buildSystem(rootClassNames, Configuration.getSearchPaths(), null, attributes);
			if (dbgProflg) {vrb.println("duration for reading class files = " + ((System.nanoTime() - time) / 1000) + "us"); time = System.nanoTime();}
		}

		// Initialize compiler components
		if (reporter.nofErrors <= 0) {
			if (dbg) vrb.println("[Launcher] Initializing Linker");
			Linker32.init();
		}
		CodeGen cg = null;
		if (reporter.nofErrors <= 0) {
			if (Configuration.getBoard().cpu.arch.name.equals(HString.getHString("ppc32"))) {
				cg = new CodeGenPPC();
				InstructionDecoder.dec = new InstructionDecoderPPC();
			}
			if (Configuration.getBoard().cpu.arch.name.equals(HString.getHString("arm32"))) {
//				cg = new CodeGenARM();
//				InstructionDecoder.dec = new InstructionDecoderARM();
			}
			if (dbg) vrb.println("[Launcher] Initializing Code Generator");
			cg.init();
		}

		// creating type descriptors for arrays
		if (dbg) vrb.println("[Launcher] creating type descriptors for arrays:");
		array = Class.arrayClasses;
		while (array != null) {
			if (dbg) vrb.println("> Array: " + array.name);
			if (dbg) vrb.println("  creating type descriptor");
			Linker32.createTypeDescriptor(array);
			array = array.nextArray;
		}

		// processing interfaces, creating constant block 
		if (dbg) vrb.println("[Launcher] creating constant block for interfaces:");
		Class intf = Class.constBlockInterfaces;	// handle interfaces list
		while (intf != null) {
			if (dbg) vrb.println("> Interface: " + intf.name + " creating type descriptor");
			Linker32.createConstantBlock(intf);
			intf = intf.nextInterface;
		}

		// loop one: processing standard classes, creating constant block, translating code, calculating code size
		if (dbg) vrb.println("[Launcher] (loop one) processing classes:");
		if (reporter.nofErrors <= 0) {
			for (int extLevel = 0; extLevel <= Class.maxExtensionLevelStdClasses; extLevel++) {
				if (dbg) vrb.println("  Extension level " + extLevel + ":");
				clazz = Class.extLevelOrdClasses[extLevel];
				while (clazz != null && reporter.nofErrors <= 0) {
					if (dbg) vrb.println("  > Class: " + clazz.name);
					if ((clazz.accAndPropFlags & (1 << dpfSynthetic)) != 0) {
						if (dbg) vrb.println("   is synthetic, omit");
					} else {

						// Create constant block
						if (reporter.nofErrors <= 0) {
							if(dbg) vrb.println("[LAUNCHER] creating constant block for " + clazz.name);
							Linker32.createConstantBlock(clazz);
						}

						method = (Method)clazz.methods;
						while (method != null && reporter.nofErrors <= 0) {
							// handle native methods differently 
							if ((method.accAndPropFlags & (1 << apfNative)) != 0) {
								vrb.println("No implementation for " + method.name);
							} else if ((method.accAndPropFlags & ((1 << dpfSynthetic) | (1 << apfAbstract))) == 0) { // proceed only methods with code
								if (dbg) {vrb.print("    > Method: " + method.name + method.methDescriptor + ", accAndPropFlags: "); Dbg.printAccAndPropertyFlags(method.accAndPropFlags); vrb.println();}
								// Create CFG
								if (reporter.nofErrors <= 0) {
									method.cfg = new CFG(method);
								}
								// Create SSA
								if (reporter.nofErrors <= 0) {
									method.ssa = new SSA(method.cfg);
								}
								// Create machine code
								if (reporter.nofErrors <= 0) {
									method.machineCode = new Code32(method.ssa);
									cg.translateMethod(method);
								}
							} else if (dbg) vrb.print("method " + method.name + " is synthetic or abstract");
							method = (Method)method.next;
						}

						// calculate required code size
						if (reporter.nofErrors <= 0) {
							if(dbg) vrb.println("[LAUNCHER] calculating code size and offsets");
							Linker32.calculateCodeSizeAndMethodOffsets(clazz);
						}
					}

					clazz = clazz.nextExtLevelClass;
				}
			}
		}

		if (reporter.nofErrors > 0) return reporter.nofErrors;;
		
		// handle interfaces with class constructor, translating code, calculating code size
		if(dbg) vrb.println("[LAUNCHER] handle interfaces with class constructor");
		intf = Class.constBlockInterfaces;	
		while (intf != null) {
			method = (Method)intf.methods;
			while (method != null && reporter.nofErrors <= 0) {
				if ((method.accAndPropFlags & ((1 << dpfSynthetic) | (1 << apfAbstract))) == 0) { // proceed only with class constructors
					if (dbg) vrb.println("    > Method: " + method.name + method.methDescriptor + ", accAndPropFlags: " + Integer.toHexString(method.accAndPropFlags));
					method.cfg = new CFG(method);
					method.ssa = new SSA(method.cfg);
					method.machineCode = new Code32(method.ssa); 
					cg.translateMethod(method);
				}
				method = (Method)method.next;
			}
			// calculate required code size
			Linker32.calculateCodeSizeAndMethodOffsets(intf);
			if (dbg) vrb.println("    > Interface: " + intf.name + " calculating code size");
			intf = intf.nextInterface;
		}

		// handle compiler specific methods
		if(dbg) vrb.println("[LAUNCHER] compiler specific methods");
		cg.generateCompSpecSubroutines();
		Linker32.calculateCodeSizeAndOffsetsForCompilerSpecSubroutines();

		if (reporter.nofErrors <= 0) {
			Linker32.createSystemTable();	// create system table

			// freeze memory map (arrange in memory segments) for std classes, arrays, interfaces, compiler specific methods, system table
			Linker32.freezeMemoryMap();
		}

		// calculate absolute addresses for arrays
		if (dbg) vrb.println("[Launcher] calculate absolute addresses for arrays:");
		array = Class.arrayClasses;
		while (array != null && reporter.nofErrors <= 0) {
			Linker32.calculateAbsoluteAddresses(array);
			if (dbg) vrb.println(" > Array: " + array.name + ", addr:0x" + Integer.toHexString(array.address));
			array = array.nextArray;
		}

		// calculate absolute addresses for interfaces
		if(dbg) vrb.println("[Launcher] calculate absolute addresses for interfaces:");
		intf = Class.constBlockInterfaces;
		while (intf != null && reporter.nofErrors <= 0) {
			Linker32.calculateAbsoluteAddresses(intf);
			if (dbg) vrb.println(" > Interface: " + intf.name + ", addr:0x" + Integer.toHexString(intf.address));
			intf = intf.nextInterface;
		}

		// loop two: processing std classes, calculate absolute addresses
		if (dbg) vrb.println("[Launcher] (loop two) processing classes:");
		if (reporter.nofErrors <= 0) {
			for (int extLevel = 0; extLevel <= Class.maxExtensionLevelStdClasses; extLevel++) {
				clazz = Class.extLevelOrdClasses[extLevel];
				while (clazz != null && reporter.nofErrors <= 0) {
					if ((clazz.accAndPropFlags & (1 << dpfSynthetic)) != 0) {
						if (dbg) vrb.println("   is synthetic, omit");
					} else Linker32.calculateAbsoluteAddresses(clazz);
					clazz = clazz.nextExtLevelClass;
				}
			}
		}

		// handle compiler specific methods
		if (reporter.nofErrors <= 0) Linker32.calculateAbsoluteAddressesForCompSpecSubroutines();

		// create global constant table
		if (dbg) vrb.println("[Launcher] Creating global constant table");
		if (reporter.nofErrors <= 0) Linker32.createGlobalConstantTable();

		// loop three: processing standard classes, updating constant blocks, method fix ups 
		if (dbg) vrb.println("[Launcher] (loop three) processing classes:");
		if (reporter.nofErrors <= 0) {
			for (int extLevel = 0; extLevel <= Class.maxExtensionLevelStdClasses; extLevel++) {
				if (dbg) vrb.println("  Extension level " + extLevel + ":");
				clazz = Class.extLevelOrdClasses[extLevel];
				while (clazz != null && reporter.nofErrors <= 0) { 
					if (dbg) vrb.println("  > Class: " + clazz.name);
					if ((clazz.accAndPropFlags & (1 << dpfSynthetic)) != 0) {
						if (dbg) vrb.println("   is synthetic, omit");
					} else {

						// Linker: update constant block
						if (dbg) vrb.println("    updating constant block");
						Linker32.updateConstantBlock(clazz);

						method = (Method)clazz.methods;
						while (method != null && reporter.nofErrors <= 0) {
							if ((method.accAndPropFlags & ((1 << dpfSynthetic) | (1 << apfAbstract) | (1 << apfNative))) == 0) { // proceed only methods with code
								if (dbg) vrb.println("    > Method: " + method.name + method.methDescriptor + ", accAndPropFlags: " + Integer.toHexString(method.accAndPropFlags));
								if (dbg) vrb.println("      doing fixups");
								cg.doFixups(method.machineCode);
							}
							method = (Method)method.next;
						}
					}
					clazz = clazz.nextExtLevelClass;
				}
			}
		}

		// processing interfaces, updating constant blocks, method fix ups
		if (dbg) vrb.println("[Launcher] (loop three) processing interfaces:");
		intf = Class.constBlockInterfaces;
		while (intf != null && reporter.nofErrors <= 0) {
			if (dbg) vrb.println("> Interface: " + intf.name);
			Linker32.updateConstantBlock(intf);
			method = (Method)intf.methods;
			while (method != null && reporter.nofErrors <= 0) {
				if ((method.accAndPropFlags & ((1 << dpfSynthetic) | (1 << apfAbstract))) == 0) { // proceed only methods with code
					if (dbg) vrb.println("    > Method: " + method.name + method.methDescriptor + ", accAndPropFlags: " + Integer.toHexString(method.accAndPropFlags));
					if (dbg) vrb.println("      doing fixups");
					cg.doFixups(method.machineCode);

				}
				method = (Method)method.next;
			}
			intf = intf.nextInterface;
		}

		// handle compiler specific methods
		if (dbg) vrb.println("[Launcher] (loop three) processing compiler specific subroutines:");
		Method m = Method.compSpecSubroutines;	// Code generator: fix up
		while (m != null) {
			if (dbg) vrb.println("    > Method: " + m.name + m.methDescriptor + ", accAndPropFlags: " + Integer.toHexString(m.accAndPropFlags));
			if (dbg) vrb.println("      doing fixups");
			cg.doFixups(m.machineCode);
			m = (Method)m.next;
		}
//		Method.printCompSpecificSubroutines();

		// Linker: update system table, determine size of code
		if (reporter.nofErrors <= 0) {
			Linker32.updateSystemTable();
			if (dbgProflg) {vrb.println("duration for generating ssa, code and linking = " + ((System.nanoTime() - time) / 1000) + "us"); time = System.nanoTime();}
		}

		// Linker: Create target image
		if (reporter.nofErrors <= 0) {
			if (dbg) vrb.println("[Launcher] Generating target image");
			Linker32.generateTargetImage();
		}

		// Create target command table file if file name defined in the configuration
		if (reporter.nofErrors <= 0) {
			HString tctFileName = Configuration.getActiveProject().getTctFileName();
			if (tctFileName != null) saveCommandTableToFile(tctFileName.toString());
			if (dbgProflg) {vrb.println("duration for generating target file = " + ((System.nanoTime() - time) / 1000) + "us"); time = System.nanoTime();}
		}

		if (reporter.nofErrors > 0) {
			log.println("Compilation failed with " + reporter.nofErrors + " error(s)");
			log.println();
		} else {
			log.println("Compilation and target image generation successfully finished");
			log.println();
		}
		
		// Create target image file if file name defined in the configuration
		if (reporter.nofErrors <= 0) {
			HString fname = Configuration.getActiveProject().getImgFileName();
			if (fname != null && !fname.equals(HString.getHString(""))) 
				saveTargetImageToFile(fname.toString(), Configuration.getActiveProject().getImgFileFormat());
		}	
		
		return reporter.nofErrors;
	}
	
	public static void downloadTargetImage() {
		Board b = Configuration.getBoard();
		RunConfiguration targetConfig = Configuration.getActiveTargetConfiguration();
		TargetMemorySegment tms = Linker32.targetImage;
		if (b != null) {
			if (tc != null) {
				try {
					if (dbg) vrb.println("[Launcher] Initializing registers");
					RegisterInit r = b.regInits;
					while (r != null) {
						tc.initRegister(r);
						r = (RegisterInit) r.next;
					}
					r = targetConfig.regInits;
					while (r != null) {
						tc.initRegister(r);
						r = (RegisterInit) r.next;
					}
					for (int i = 0; i < b.cpu.arch.getNofGPRs(); i++) tc.setGprValue(i, 0);
					log.println("Downloading target image:");
					while (tms != null && reporter.nofErrors <= 0) {
						if (dbg) vrb.print("  processing TMS #" + tms.id);
						if (tms.segment == null) { // this should never happen
							// TODO add error message here
							if (dbg) vrb.println(" -> skipping (segment not defined)");
						} else {
							if (dbg) vrb.println(" -> writing " + tms.data.length * 4 + " bytes to address 0x" + Integer.toHexString(tms.startAddress) + " on device " + tms.segment.owner.name);
							tc.writeTMS(tms);
						}
						tms = tms.next;
					}
					tc.resetErasedFlag();
				} 
				catch (TargetConnectionException e) {
					if(e.getCause().getClass().getName() ==  "ch.ntb.inf.usbbdi.bdi.PacketWrongException"){
						reporter.error(813);
					}
					else if(e.getCause().getClass().getName() == "ch.ntb.inf.usbbdi.bdi.ReadyBitNotSetException"){
						reporter.error(814);
					}
					else{
						reporter.error(801);
					}
				}
			} else 	reporter.error(800);
		} else	reporter.error(238);
	}

	public static void startTarget() {
		if (reporter.nofErrors <= 0) {
			if (tc != null) {
				log.println("Starting target");
				try {
					tc.startTarget();
				} catch (TargetConnectionException e) {
					reporter.error(805);
					reporter.nofErrors++;
				}
			}
		}
	}

	public static void stopTarget() {
		try {
			if(tc != null) tc.stopTarget();
		} catch (TargetConnectionException e) {
			e.printStackTrace();
		}
	}

	public static void openTargetConnection() {
		if (dbg) vrb.println("[Launcher] Opening target connection");
		if (tc != null) {
			if (dbg) vrb.println(" -> ok");
			try {
				if(dbg) vrb.println("  Initializing target connection");
				tc.init();
			} catch (TargetConnectionException e) {
				e.printStackTrace();
			}
		} else reporter.error(803);
	}

	public static void reopenTargetConnection() {
		if (tc != null) {
			try {
				tc.closeConnection();
				tc.openConnection();
			} catch (TargetConnectionException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void closeTargetConnection() {
		if(dbg) vrb.println("[Launcher] Closing target connection");
		if(tc != null) tc.closeConnection();
	}

	public static TargetConnection getTargetConnection() {
		return tc;
	}

	public static void setTargetConnection(TargetConnection tc2) {
		tc = tc2;
	}

	protected static long saveTargetImageToFile(String fileName, int format) {
		long bytesWritten = -1;
		try {
			switch(format) {
			case Configuration.BIN:
				log.println("Writing target image in binary format to: " + fileName);
				bytesWritten = Linker32.writeTargetImageToBinFile(fileName);
				break;
			case Configuration.HEX:
				reporter.error(10, "Writing hex image");
				break;
			case Configuration.SREC:
				reporter.error(10, "Writing srec image");
				break;
			case Configuration.DTIM:
				bytesWritten = Linker32.writeTargetImageToDtimFile(fileName);
				break;
			}
		} catch(IOException e) {
			e.printStackTrace();
			bytesWritten = -1;
		}
		return bytesWritten;	
	}
	
	protected static void saveCommandTableToFile(String fileName) {
		if (reporter.nofErrors <= 0){
			File path = new File(fileName.substring(0, fileName.lastIndexOf('/')));
			path.mkdirs(); // create directories if not existing
			try {
				Linker32.writeCommandTableToFile(fileName);
			} catch (IOException e) { 
				e.printStackTrace();
			}
		}
	}
	
	protected static void createInterfaceFiles(String libraryPath) {
		String basePath = libraryPath + File.separatorChar + "src" + File.separatorChar +
				"ch"  + File.separatorChar + "ntb"  + File.separatorChar + "inf"  + 
				File.separatorChar + "deep"  + File.separatorChar + "runtime";
		
		createCompilerInterfaceFile(basePath);
		
		Board b = Configuration.getBoard();
		createArchInterfaceFile(b.cpu.arch, basePath);
		createProcInterfaceFile(b.cpu, basePath);
		createBoardInterfaceFile(b, basePath);
	}
		
	private static void createCompilerInterfaceFile(String basePath) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		String fileName = "IdeepCompilerConstants.java";
		try {
			File dir = new File(basePath);
			if (!dir.exists()) dir.mkdirs();
			File f = new File(dir.getAbsolutePath() + File.separatorChar + fileName);
			FileWriter fw = new FileWriter(f);
			vrb.println("Creating " + f.getAbsolutePath());
			fw.write("package ch.ntb.inf.deep.runtime;\n\n");
			fw.write("// Auto generated file (" + dateFormat.format(date) + ")\n\n");
			fw.write("public interface IdeepCompilerConstants {\n");
			fw.write("\n\t// Compiler constants\n");
			SystemConstant curr = Configuration.getCompilerConstants();
			while (curr != null) {
				fw.write("\tpublic static final int " + curr.name.toString() + " = 0x" + Integer.toHexString(curr.val) + ";\n");
				curr = (SystemConstant)curr.next;
			}
			fw.write("}");
			fw.flush();
			fw.close();

		} catch (IOException e) {e.printStackTrace();}
	}
	
	private static void createArchInterfaceFile(Arch a, String basePath) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		String archName = a.name.toString();
		basePath = basePath + File.separatorChar + archName;
		String fileName = "I" + archName + ".java";
		try {
			File dir = new File(basePath);
			if(!dir.exists()) dir.mkdirs();
			File f = new File(dir.getAbsolutePath() + File.separatorChar + fileName);
			FileWriter fw = new FileWriter(f);
			vrb.println("Creating " + f.getAbsolutePath());
			fw.write("package ch.ntb.inf.deep.runtime." + archName + ";\n\n");
			fw.write("// Auto generated file (" + dateFormat.format(date) + ")\n\n");
			fw.write("public interface I" + archName + " {\n");
			Register reg;
			fw.write("\n\t// Registermap for architecture \"" + archName + "\"\n");
			reg = a.regs;
			while (reg != null) {
				fw.write("\tpublic static final int " + reg.name + " = 0x" + Integer.toHexString(reg.address) + ";\n");
				reg = (Register) reg.next;
			}
			fw.write("}");
			fw.flush();
			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void createProcInterfaceFile(CPU cpu, String basePath) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		String cpuName = cpu.name.toString();
		String archName = cpu.arch.name.toString();
		basePath = basePath + File.separatorChar + cpuName;
		String fileName = "I" + cpuName + ".java";
		try {
			File dir = new File(basePath);
			if(!dir.exists()) dir.mkdirs();
			File f = new File(dir.getAbsolutePath() + File.separatorChar + fileName);
			FileWriter fw = new FileWriter(f);
			vrb.println("Creating " + f.getAbsolutePath());
			fw.write("package ch.ntb.inf.deep.runtime." + cpuName + ";\n\n");
			fw.write("// Auto generated file (" + dateFormat.format(date) + ")\n\n");
			fw.write("public interface I" + cpuName + " extends I" + archName + " {\n");
			
			fw.write("\n\t// System constants of CPU " + cpuName + "\n");
			SystemConstant curr = cpu.sysConstants;
			while (curr != null && curr != Configuration.getCompilerConstants()) {
				fw.write("\tpublic static final int " + curr.name.toString() + " = 0x" + Integer.toHexString(curr.val) + ";\n");
				curr = (SystemConstant) curr.next;
			}
			
			fw.write("\n\t// Specific registers of CPU " + cpuName + "\n");
			Register reg = cpu.regs;
			while (reg != null && reg != cpu.arch.regs) {
				fw.write("\tpublic static final int " + reg.name + " = 0x" + Integer.toHexString(reg.address) + ";\n");
				reg = (Register) reg.next;
			}
			
			fw.write("}");
			fw.flush();
			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void createBoardInterfaceFile(Board b, String basePath) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		String boardName = b.name.toString();
		String cpuName = b.cpu.name.toString();
		basePath = basePath + File.separatorChar + cpuName;
		String fileName = "I" + boardName + ".java";
		try {
			File dir = new File(basePath);
			if(!dir.exists()) dir.mkdirs();			
			File f = new File(dir.getAbsolutePath() + File.separatorChar + fileName);
			FileWriter fw = new FileWriter(f);
			vrb.println("Creating " + f.getAbsolutePath());
			fw.write("package ch.ntb.inf.deep.runtime." + cpuName + ";\n\n");
			fw.write("// Auto generated file (" + dateFormat.format(date) + ")\n\n");
			fw.write("public interface I" + boardName + " extends I" + cpuName + " {\n");
			
			fw.write("\n\t// System constants of board " + boardName + "\n");
			SystemConstant curr = b.sysConstants;
			while (curr != null && curr != b.cpu.sysConstants) {
				fw.write("\tpublic static final int " + curr.name.toString() + " = 0x" + Integer.toHexString(curr.val) + ";\n");
				curr = (SystemConstant) curr.next;
			}
			
			fw.write("}");
			fw.flush();
			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
