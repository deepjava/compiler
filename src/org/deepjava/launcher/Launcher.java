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

package org.deepjava.launcher;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.deepjava.cfg.CFG;
import org.deepjava.cg.Code32;
import org.deepjava.cg.CodeGen;
import org.deepjava.cg.InstructionDecoder;
import org.deepjava.cg.arm.CodeGenARM;
import org.deepjava.cg.arm.InstructionDecoderARM;
import org.deepjava.cg.ppc.CodeGenPPC;
import org.deepjava.cg.ppc.InstructionDecoderPPC;
import org.deepjava.classItems.Array;
import org.deepjava.classItems.CFR;
import org.deepjava.classItems.Class;
import org.deepjava.classItems.ICclassFileConsts;
import org.deepjava.classItems.Method;
import org.deepjava.config.Arch;
import org.deepjava.config.Board;
import org.deepjava.config.CPU;
import org.deepjava.config.Configuration;
import org.deepjava.config.Parser;
import org.deepjava.config.Programmer;
import org.deepjava.config.Register;
import org.deepjava.config.RegisterInit;
import org.deepjava.config.RunConfiguration;
import org.deepjava.config.SystemConstant;
import org.deepjava.host.Dbg;
import org.deepjava.host.ErrorReporter;
import org.deepjava.host.StdStreams;
import org.deepjava.linker.Linker32;
import org.deepjava.linker.TargetMemorySegment;
import org.deepjava.ssa.SSA;
import org.deepjava.strings.HString;
import org.deepjava.target.Am29LV160dFlashWriter;
import org.deepjava.target.TargetConnection;
import org.deepjava.target.TargetConnectionException;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class Launcher implements ICclassFileConsts {

	private static final boolean dbg = false;
	private static final boolean dbgProflg = false;
	
	private static ErrorReporter reporter = ErrorReporter.reporter;
	private static PrintStream log = StdStreams.log;
	private static PrintStream vrb = StdStreams.vrb;
	private static TargetConnection tc;
	private static long time;
	
	public static int buildAll(String deepProjectFileName, String targetConfigurationName, boolean checkVersion) {
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
		Configuration.readProjectFile(deepProjectFileName);
		if (reporter.nofErrors <= 0) Configuration.setActiveTargetConfig(targetConfigurationName);
		if (dbgProflg) {vrb.println("duration for reading configuration = " + ((System.nanoTime() - time) / 1000) + "us"); time = System.nanoTime();}

		if ((reporter.nofErrors <= 0) && checkVersion) {
			Bundle deepBundle = Platform.getBundle("org.deepjava");
			if (deepBundle != null) {
				Version deepCompilerVersion = deepBundle.getVersion();
				File deepLibVersionFile = null;
				FileReader fr = null;
				char[] deepLibVersion = {'0','.','0'};
				boolean identVersion = false;
				int major = 0;
				int minor = 0;
				
				for (HString h : Configuration.getLibPaths()) {
					deepLibVersionFile = new File(h.toString() + "VERSION");
					try {
						fr = new FileReader(deepLibVersionFile);
						fr.read(deepLibVersion, 0, 3);
					} catch (FileNotFoundException e) {
						ErrorReporter.reporter.error(310, "failed to open file " + deepLibVersionFile);
					} catch (IOException e) {
						ErrorReporter.reporter.error(310, "could not read from " + deepLibVersionFile);
					}
					major = deepLibVersion[0] - '0';
					minor = deepLibVersion[2] - '0';
					identVersion = deepCompilerVersion.getMajor() == major && deepCompilerVersion.getMinor() == minor;
				}
				if (reporter.nofErrors <= 0 && !identVersion) {
					if (compareVersions(deepCompilerVersion, major, minor) == -1) {
						ErrorReporter.reporter.error(311);
					} else if(compareVersions(deepCompilerVersion, major, minor) == 1) {
						ErrorReporter.reporter.error(312);
					}
				}
			}
		}
		
		HString[] rootClassNames = Configuration.getRootClasses();
		if (reporter.nofErrors <= 0) {
			int nof = rootClassNames.length;
			if (nof == 0 || (nof == 1 && rootClassNames[0].equals(HString.getHString("")))) reporter.error(305);
			if (rootClassNames != null && reporter.nofErrors <= 0) {
				log.println("Root classes in project \"" + Configuration.getProjectName() + "\":");
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
			Arch arch = Configuration.getBoard().cpu.arch;
			Code32.arch = arch;
			if (arch.name.equals(HString.getHString("ppc32"))) {
				cg = new CodeGenPPC();
				InstructionDecoder.dec = new InstructionDecoderPPC();
				Linker32.bigEndian = true;
			}
			if (arch.name.equals(HString.getHString("arm32"))) {
				cg = new CodeGenARM();
				InstructionDecoder.dec = new InstructionDecoderARM();
				Linker32.bigEndian = false;
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
			if (m.machineCode != null) {
				if (dbg) vrb.println("    > Method: " + m.name + m.methDescriptor + ", accAndPropFlags: " + Integer.toHexString(m.accAndPropFlags));
				if (dbg) vrb.println("      doing fixups");
				cg.doFixups(m.machineCode);
			}
			m = (Method)m.next;
		}

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
			HString tctFileName = Configuration.getTctFileName();
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
			HString fname = Configuration.getImgFileName();
			if (fname != null && !fname.equals(HString.getHString(""))) 
				saveTargetImageToFile(fname.toString(), Configuration.getImgFileFormat());
		}	
		
		return reporter.nofErrors;
	}

	public static void downloadTargetImage() {
		boolean flashErased = false;
		Board b = Configuration.getBoard();
		RunConfiguration targetConfig = Configuration.getActiveTargetConfiguration();
		if (b != null) {
			if (tc != null) {
				Programmer programmer = Configuration.getProgrammer();
				if (programmer.name.equals(HString.getHString("abatronBDI"))) {
					try {
						log.println("Downloading target image");
						initTarget(b, targetConfig);
						tc.downloadImageFile(Configuration.getImgFileName().toString());
					} catch (TargetConnectionException e) {
						if (e.getMessage().equals("no such file")) {
							reporter.error(821);
						} else if (e.getMessage().equals("target not connected")) {
							reporter.error(800);
						} else {
							reporter.error(801);
						}
					}
				} else if (programmer.name.equals(HString.getHString("openOCD"))) {
					TargetMemorySegment tms = Linker32.targetImage;
					if (tms != null && reporter.nofErrors <= 0) {
						if (tms.segment.owner.technology == 1) {	// flash device
							log.print("Creating flash file: ");
							createZynqFlashFile(tms);
						} else {	// ram device
							try {
								log.println("Downloading target image");
								initTarget(b, targetConfig);
								HString str = Configuration.getImgFileName();
								if (str == null) reporter.error(820);
								tc.downloadImageFile(str.toString());
							} catch (TargetConnectionException e) {
								if (e.getMessage().equals("no such file")) {
									reporter.error(821);
								} else if (e.getMessage().equals("target not connected")) {
									reporter.error(800);
								} else {
									reporter.error(801);
								}
							}
						}
					}
				} else {	// USB BDI
					try {
						TargetMemorySegment tms = Linker32.targetImage;
						initTarget(b, targetConfig);
						while (tms != null && reporter.nofErrors <= 0) {
							if (tms.segment.owner.technology == 1) { // flash device
								if (tms.segment.owner.memorytype == Configuration.AM29LV160D) {
									Am29LV160dFlashWriter flashWriter = new Am29LV160dFlashWriter(tc);
									if (!flashErased) { // erase all used sectors
										TargetMemorySegment current = tms;
										// first mark all used sectors
										while (current != null && current.segment.owner.memorytype == Configuration.AM29LV160D) {
											current.segment.owner.markUsedSectors(current);
											current = current.next;
										}
										// second erase all marked sectors
										org.deepjava.config.Device[] devs = Configuration.getDevicesByType(Configuration.AM29LV160D);
										for (int i = 0; i < devs.length; i++) {
											if(devs[i] == null) StdStreams.err.println("ERROR: devs[" + i + "] == null");
											else flashWriter.eraseMarkedSectors(devs[i]);
										}
										flashErased = true;
										log.println("Programming flash");
									}

									if (!flashWriter.unlocked) flashWriter.unlockBypass(tms.segment.owner, true);
									flashWriter.writeSequence(tms);
									if (tms.next == null || tms.next.segment.owner != tms.segment.owner && flashWriter.unlocked) {
										flashWriter.unlockBypass(tms.segment.owner, false);
										StdStreams.log.println();						
									}
								} else { // other memory type
									ErrorReporter.reporter.error(807, "for Device " + tms.segment.owner.name.toString());
									return;
								}
							} else {	// RAM device 
								if (dbg) vrb.print("  processing TMS #" + tms.id);
								if (tms.segment == null) { // this should never happen
									if (dbg) vrb.println(" -> skipping (segment not defined)");
								} else {
									if (dbg) vrb.println(" -> writing " + tms.data.length * 4 + " bytes to address 0x" + Integer.toHexString(tms.startAddress) + " on device " + tms.segment.owner.name);
									tc.writeTMS(tms);
								}
							}
							tms = tms.next;
						}
					} catch (TargetConnectionException e) {
						if (e.getCause().getClass().getName() ==  "ch.ntb.inf.usbbdi.bdi.PacketWrongException") {
							reporter.error(813);
						} else if (e.getCause().getClass().getName() == "ch.ntb.inf.usbbdi.bdi.ReadyBitNotSetException") {
							reporter.error(814);
						} else if (e.getMessage().equals("no such file")) {
							reporter.error(821);
						} else if (e.getMessage().equals("target not connected")) {
							reporter.error(800);
						} else {
							reporter.error(801);
						}
					}
				}
			} else 	reporter.error(800);
		} else	reporter.error(238);
	}
	
	private static void createZynqFlashFile(TargetMemorySegment tms) {
		while (tms != null && reporter.nofErrors <= 0) {
			if (tms.segment.owner.technology != 1) {
				StdStreams.vrb.println("other than flash segment found");
			}
			tms = tms.next;
		}
		try {
			String fname = Configuration.getImgFileName().toString();
			// open bin file for reading
			File binFile = new File(fname.substring(0, fname.lastIndexOf('.') + 1) + Linker32.targetImage.segment.owner.name + fname.substring(fname.lastIndexOf('.'), fname.length()));
			System.out.println(fname.substring(0, fname.lastIndexOf('.') + 1) + Linker32.targetImage.segment.owner.name + fname.substring(fname.lastIndexOf('.'), fname.length()));
			if (!binFile.exists()) {
				ErrorReporter.reporter.error(822, "check if proper target file format used");
				return;
			}
			BufferedInputStream readerBin = new BufferedInputStream(new FileInputStream(binFile));
			// open mcs file for writing
			fname = fname.substring(0, fname.lastIndexOf('.') + 1) + Linker32.targetImage.segment.owner.name + ".mcs";
			BufferedWriter writerMcs = new BufferedWriter(new FileWriter(fname));
			System.out.println(fname);
			log.println(fname);
			// open predefined Xilinx mcs file 
			String plFile = Configuration.getPlFile();
			if (plFile == null) {
				ErrorReporter.reporter.error(823, "add PL file to deep configuration");
				readerBin.close();
				writerMcs.close();
				return;
			}
			HString[] libs = Configuration.getLibPaths();
			boolean found = false;
			for (HString lib : libs) {
				fname = lib + "rsc/BOOT" + plFile.substring(plFile.indexOf("flink"), plFile.length() - 4) + "App.mcs";
				File f = new File(fname);
				if (f.exists()) {
					found = true;
					break;
				}
			}
			if (!found) {
				ErrorReporter.reporter.error(824, "check if library path set correctly");
				readerBin.close();
				writerMcs.close();
				return;
			}
			System.out.println(fname);
			BufferedReader readerMcs = new BufferedReader(new FileReader(fname));
			StringBuffer buf = new StringBuffer();
			String lineMcs;
			byte[] lineBin = new byte[128];
			int line = 1;
			
			// determine application size in bytes, this size must be written into header file info section (see below)
			int size = 0, k;
			final int len = 16;
			while ((k = readerBin.read(lineBin, 0, len)) == len) size += 16;
			if (k > 0) size += k;
			readerBin.close();
//			StdStreams.vrb.println("nof bytes in bin file = 0x" + Integer.toHexString(size));
			
			readerBin = new BufferedInputStream(new FileInputStream(binFile));
			while (line < 160 && ((lineMcs = readerMcs.readLine()) != null)) {
				buf.append(lineMcs);
				buf.append('\n');
				line++;
			}
			int checksum = 3 * size;
			// change header file info for application
			lineMcs = readerMcs.readLine();
			String newLine = lineMcs.substring(0, 9) + String.format("%08X", Integer.reverseBytes(size)) + String.format("%08X", Integer.reverseBytes(size)) + String.format("%08X", Integer.reverseBytes(size)) + lineMcs.substring(33);
			checksum += getHeaderInfoEntry(lineMcs.substring(33, 41));
			buf.append(newLine);
			buf.append('\n');
			line++;
			lineMcs = readerMcs.readLine();
			checksum += getHeaderInfoEntry(lineMcs.substring(9, 17));
			checksum += getHeaderInfoEntry(lineMcs.substring(17, 25));
			checksum += getHeaderInfoEntry(lineMcs.substring(25, 33));
			checksum += getHeaderInfoEntry(lineMcs.substring(33, 41));
			buf.append(lineMcs);
			buf.append('\n');
			line++;
			lineMcs = readerMcs.readLine();
			checksum += getHeaderInfoEntry(lineMcs.substring(9, 17));
			checksum += getHeaderInfoEntry(lineMcs.substring(17, 25));
			checksum += getHeaderInfoEntry(lineMcs.substring(25, 33));
			checksum += getHeaderInfoEntry(lineMcs.substring(33, 41));
			buf.append(lineMcs);
			buf.append('\n');
			line++;
			lineMcs = readerMcs.readLine();
			checksum += getHeaderInfoEntry(lineMcs.substring(9, 17));
			checksum += getHeaderInfoEntry(lineMcs.substring(17, 25));
			checksum += getHeaderInfoEntry(lineMcs.substring(25, 33));
			checksum = ~checksum;
//			StdStreams.vrb.println("checksum = 0x" + Integer.toHexString(checksum));
			newLine = lineMcs.substring(0, 33) + String.format("%08X", Integer.reverseBytes(checksum)) + lineMcs.substring(41);
			buf.append(newLine);
			buf.append('\n');
			line++;

			while (line < 137604 && ((lineMcs = readerMcs.readLine()) != null)) {
				buf.append(lineMcs);
				buf.append('\n');
				line++;
			}
			int offset = 0xa300;
			int highAddr = 0x22;
			while ((k = readerBin.read(lineBin, 0, len)) == len) {
//				StdStreams.vrb.print("reading bin = " + count + ": ");
//				for (int i = 0; i < len; i++) StdStreams.vrb.print(String.format("%02X", lineBin[i]));        
//				StdStreams.vrb.println();
				buf.append(":10" + String.format("%04X", offset) + "00");
				for (int i = 0; i < len; i++) {
					buf.append(String.format("%02X", lineBin[i]));
				}
				buf.append("AB\n");
				if (offset == 0xfff0) {	// high address tag, MCS files need such tags every 64k block
					buf.append(":02" + String.format("%04X", 0) + "04" + String.format("%04X", highAddr) + "AB\n");
					highAddr++;
					offset = 0;
				} else offset += 0x10;
			} 
			if (k > 0) {	// end unfinished lines
//				StdStreams.vrb.print("reading bin = " + count + ": ");
//				for (int i = 0; i < k; i++) StdStreams.vrb.print(String.format("%02X", lineBin[i]));        
//				StdStreams.vrb.println();
				buf.append(":" + String.format("%02X", k) + String.format("%04X", offset) + "00");
				for (int i = 0; i < k; i++) {
					buf.append(String.format("%02X", (byte)lineBin[i]));
				}
				buf.append("AB\n");
			}
			buf.append(":00000001FF\n");	// eof tag
			readerMcs.close();
			readerBin.close();
			writerMcs.write(buf.toString());
			writerMcs.close();
		} catch (IOException e) {
			ErrorReporter.reporter.error(822, "check if proper target file format used");
			return;
		}
	}

	private static void initTarget(Board b, RunConfiguration targetConfig) throws TargetConnectionException {
		log.println("Initializing target");
		if (dbg) vrb.println("[Launcher] Reseting target");
		tc.resetTarget();
		if (dbg) vrb.println("[Launcher] Initializing registers");
		RegisterInit r = b.cpu.regInits;
		if (dbg) vrb.println("[Launcher] Initializing cpu registers");
		while (r != null) {
			tc.setRegisterValue(r.reg, r.val);
			r = (RegisterInit) r.next;
		}
		r = b.regInits;
		if (dbg) vrb.println("[Launcher] Initializing board registers");
		while (r != null) {
			tc.setRegisterValue(r.reg, r.val);
			r = (RegisterInit) r.next;
		}
		if (dbg) vrb.println("[Launcher] Initializing target configuration registers");
		r = targetConfig.regInits;
		while (r != null) {
			if (r.poll) {
				long val;
				do {
					val = tc.getRegisterValue(r.reg);
					if (dbg) vrb.println("[Launcher] poll for target configuration register " + r.toString() + ", current value = " + val);
				} while ((val & r.val) != r.val);
			} else tc.setRegisterValue(r.reg, r.val);
			r = (RegisterInit) r.next;
		}
		for (int i = 0; i < b.cpu.arch.getNofGPRs(); i++) tc.setRegisterValue("R" + i, 0);
	}

	private static int getHeaderInfoEntry(String str) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i <= str.length() - 2; i += 2) {
			result.append(new StringBuilder(str.substring(i, i + 2)).reverse());
		}
		return (int)Long.parseLong(result.reverse().toString(), 16);   
	}

	public static void startTarget(int address) {
		if (reporter.nofErrors <= 0) {
			if (tc != null) {
				log.println("Starting target");
				try {
					tc.startTarget(address);
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
			reporter.error(803);
		}
	}

	public static void openTargetConnection() {
		if (dbg) vrb.println("[Launcher] Opening target connection");
		if (tc != null) {
			try {
				if (!tc.isConnected()) tc.openConnection();
			} catch (TargetConnectionException e) {
				reporter.error(815);
			}
		} else reporter.error(803);
	}

	public static void reopenTargetConnection() {
		if (dbg) vrb.println("[Launcher] Reopening target connection");
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
		if (dbg) vrb.println("[Launcher] Closing target connection");
		if (tc != null) tc.closeConnection();
	}

	public static TargetConnection getTargetConnection() {
		return tc;
	}

	public static void setTargetConnection(TargetConnection targConn) {
		if (dbg) vrb.println("[Launcher] set target connection");
		tc = targConn;
		Programmer programmer = Configuration.getProgrammer();
		if (programmer != null) {
			HString opts = programmer.getOpts();
			if (opts != null) {
				tc.setOptions(opts);
				if (dbg) vrb.println("[Launcher] Setting target connection options to " + opts.toString());
			}
		}
	}

	protected static long saveTargetImageToFile(String fileName, int format) {
		long bytesWritten = -1;
		try {
			switch(format) {
			case Configuration.BIN:
				log.print("Writing target image in binary format to: " + fileName);
				bytesWritten = Linker32.writeTargetImageToBinFile(fileName);
				log.println(" (" + bytesWritten / 1024 + ("kB)"));
				break;
			case Configuration.HEX:
				reporter.error(10, "Writing hex image");
				break;
			case Configuration.SREC:
				reporter.error(10, "Writing srec image");
				break;
			case Configuration.DTIM:
				bytesWritten = Linker32.writeTargetImageToDtimFile(fileName);
				log.println(" (" + bytesWritten / 1024 + ("kB)"));
				break;
			case Configuration.ELF:
				log.println("Writing target image in ELF format to " + fileName);
				bytesWritten = Linker32.writeTargetImageToElfFile(fileName);
				log.println(" (" + bytesWritten / 1024 + ("kB)"));
				break;
			default: 
				reporter.error(10, "Writing " + Configuration.formatMnemonics[format] + " image");
			}
			log.println("Image file generated");
		} catch(IOException e) {
			e.printStackTrace();
			bytesWritten = -1;
		}
		return bytesWritten;	
	}
	
	protected static void saveCommandTableToFile(String fileName) {
		if (reporter.nofErrors <= 0) {
			File path = new File(fileName.substring(0, fileName.lastIndexOf('/')));
			path.mkdirs(); // create directories if not existing
			try {
				Linker32.writeCommandTableToFile(fileName);
			} catch (IOException e) { 
				e.printStackTrace();
			}
		}
	}

	public static int compareVersions(Version compilerVersion, int deepMajor, int deepMinor) {
		int compilerMajor = compilerVersion.getMajor();
		int compilerMinor = compilerVersion.getMinor();
		
		if(compilerMajor > deepMajor) return 1;
		if(compilerMajor < deepMajor) return -1;
		// major versions must be equal from here on out
		if(compilerMinor > deepMinor) return 1;
		if(compilerMinor < deepMinor) return -1;
		return 0;
	}

	protected static void createInterfaceFiles(String libraryPath) {
		String basePath = libraryPath + File.separatorChar + "src" + File.separatorChar +
				"ch"  + File.separatorChar + "ntb"  + File.separatorChar + "inf"  + 
				File.separatorChar + "deep"  + File.separatorChar + "runtime";
		
		createCompilerInterfaceFile(basePath);
		
		Configuration.clear();
		String[][] boards = Configuration.getDescInConfigDir(new File(libraryPath.toString() + Configuration.boardsPath), Parser.sBoard);
		for (int i = 0; i < boards.length; i++) {
			vrb.println("\nCreating interface files for board: " + boards[i][1]);
			Board b = new Board(boards[i][0]);
			Configuration.setBoard(b);
			Configuration.readConfigFile(Configuration.boardsPath, b);
			createArchInterfaceFile(b.cpu.arch, basePath);
			createProcInterfaceFile(b.cpu, basePath);
			createBoardInterfaceFile(b, basePath);
		}
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
			vrb.println("\nCreating " + f.getAbsolutePath());
			fw.write("package org.deepjava.runtime;\n\n");
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
			fw.write("package org.deepjava.runtime." + archName + ";\n\n");
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
			fw.write("package org.deepjava.runtime." + cpuName + ";\n\n");
			fw.write("import org.deepjava.runtime." + archName + ".I" + archName + ";\n\n");
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
		if (cpuName.equals("zynq7000"))
			basePath = basePath + File.separatorChar + cpuName + File.separatorChar + boardName;
		else 
			basePath = basePath + File.separatorChar + cpuName;
		String fileName = "I" + boardName + ".java";
		try {
			File dir = new File(basePath);
			if(!dir.exists()) dir.mkdirs();			
			File f = new File(dir.getAbsolutePath() + File.separatorChar + fileName);
			FileWriter fw = new FileWriter(f);
			vrb.println("Creating " + f.getAbsolutePath());
			if (cpuName.equals("zynq7000")) {
				fw.write("package org.deepjava.runtime." + cpuName + "." + boardName.toLowerCase() + ";\n\n");
				fw.write("import org.deepjava.runtime." + cpuName + ".I" + cpuName + ";\n\n");
			} else 
				fw.write("package org.deepjava.runtime." + cpuName + ";\n\n");
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
