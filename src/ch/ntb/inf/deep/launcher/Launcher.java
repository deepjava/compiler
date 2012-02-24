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
import java.io.IOException;
import java.io.PrintStream;

import ch.ntb.inf.deep.cfg.CFG;
import ch.ntb.inf.deep.cgPPC.CodeGen;
import ch.ntb.inf.deep.classItems.Array;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linker.Linker32;
import ch.ntb.inf.deep.loader.Downloader;
import ch.ntb.inf.deep.loader.DownloaderException;
import ch.ntb.inf.deep.loader.UsbMpc555Loader;
import ch.ntb.inf.deep.ssa.SSA;

public class Launcher implements ICclassFileConsts {
	private static ErrorReporter reporter = ErrorReporter.reporter;
	private static final boolean dbg = false;
	private static PrintStream out = StdStreams.out;
	private static PrintStream vrb = StdStreams.vrb;

	public static void buildAll(String projectConfigFile, String targetConfiguration) {
		int attributes = (1 << atxCode) | (1 << atxLocalVariableTable) | (1 << atxExceptions) | (1 << atxLineNumberTable);
		
		Class clazz;
		Method method;
		Array array;
		
		// Reset error reporter
		reporter.clear();
		
		// Read configuration
		if(dbg) vrb.println("Loading Configuration");
		Configuration.parseAndCreateConfig(projectConfigFile, targetConfiguration);
		
		try {
			// Read requiered classes
			if(reporter.nofErrors <= 0) {
				if(dbg) vrb.println("Loading Classfiles");
				Class.buildSystem(Configuration.getRootClassNames(), Configuration.getSearchPaths(),
						Configuration.getSystemPrimitives(), attributes);
			}
			
			// Initialize compiler components
			if(reporter.nofErrors <= 0) {
				if(dbg) vrb.println("Initializing Linker");
				Linker32.init();
				if(dbg) vrb.println("Initializing Code Generator");
				CodeGen.init();
			}
			
			// Proceeding Arrays: Loop One
			if(dbg) vrb.println("Proceeding arrays (loop one):");
			array = Class.arrayClasses;
			while(array != null) {
				if(dbg) vrb.println("> Array: " + array.name);
				if(dbg) vrb.println("  creating type descritpor");
				Linker32.createTypeDescriptor(array);
				array = array.nextArray;
			}
			
			// Proceeding Classes: Loop One
			if(dbg) vrb.println("Proceeding classes (loop one):");
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
							}

							// Create machine code
							if(reporter.nofErrors <= 0) {
								if(dbg) vrb.println("      creating machine code");
								method.machineCode = new CodeGen(method.ssa);
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

			// calculate code size and offsets for compiler specific methods
			Linker32.calculateCodeSizeAndOffsetsForCompilerSpecSubroutines();
			
			// Linker: create system table and freeze memory map
			if(reporter.nofErrors <= 0) {
				if(dbg) vrb.println("Creating system table");
				Linker32.createSystemTable();
				if(dbg) vrb.println("Freezing memory map");
				Linker32.freezeMemoryMap();
			}
			
			// Proceeding Arrays: Loop Two
			if(dbg) vrb.println("Proceeding arrays (loop one):");
			array = Class.arrayClasses;
			while(array != null) {
				if(dbg) vrb.println("> Array: " + array.name);
				if(dbg) vrb.println("  calculating absoute addresses");
				Linker32.calculateAbsoluteAddresses(array);
				array = array.nextArray;
			}
			
			// Proceeding Classes: Loop Two
			if(dbg) vrb.println("Proceeding classes (loop two):");
			for(int extLevel = 0; extLevel <= Class.maxExtensionLevelStdClasses; extLevel++) {
				if(dbg) vrb.println("  Extentsion level " + extLevel + ":");
				clazz = Class.elOrdredClasses[extLevel];
				while(clazz != null && reporter.nofErrors <= 0) { // TODO verkettung beachten
					if(dbg) vrb.println("  > Class: " + clazz.name);
					
					// Linker: calculate absolute addresses
					if(dbg) vrb.println("    calculating absolute addresses");
					Linker32.calculateAbsoluteAddresses(clazz);
					
					// Linker: arrange constant
					if(dbg) vrb.println("    updating constant block");
					Linker32.updateConstantBlock(clazz);
					clazz = clazz.nextExtLevelClass;
				}
			}

			Linker32.calculateAbsoluteAddressesForCompSpecSubroutines();
			
			// Create global constant table
			if(dbg) vrb.println("Creating global constant table");
			Linker32.createGlobalConstantTable();
			
			// Proceeding Classes: Loop Three
			if(dbg) vrb.println("Proceeding classes (loop two):");
			for(int extLevel = 0; extLevel <= Class.maxExtensionLevelStdClasses; extLevel++) {
				if(dbg) vrb.println("  Extentsion level " + extLevel + ":");
				clazz = Class.elOrdredClasses[extLevel];
				while(clazz != null && reporter.nofErrors <= 0) { // TODO verkettung beachten
					if(dbg) vrb.println("  > Class: " + clazz.name);
					
					method = (Method)clazz.methods;
					while(method != null && reporter.nofErrors <= 0) {
						if((method.accAndPropFlags & ((1 << dpfSynthetic) | (1 << apfAbstract))) == 0) { // proceed only methods with code
							if(dbg) vrb.println("    > Method: " + method.name + method.methDescriptor + ", accAndPropFlags: " + Integer.toHexString(method.accAndPropFlags));

							// Code generator: fix up
							if(dbg) vrb.println("      doing fixups");
							method.machineCode.doFixups();
						}
						method = (Method)method.next;
					}
					
					clazz = clazz.nextExtLevelClass;
				}
			}
			
			// Linker: Create target image
			if(reporter.nofErrors <= 0) {
				if(dbg) vrb.println("Generating target image");
				Linker32.generateTargetImage();
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
			Downloader bdi = UsbMpc555Loader.getInstance();
			try {
				if(bdi != null){
					bdi.init();
				} else {
					reporter.error(Downloader.errTargetNotFound);
					reporter.nofErrors++;
				}
			} catch(DownloaderException e) {
				reporter.error(Downloader.errDownloadFailed);
				reporter.nofErrors++;
			}
		} else {
			reporter.error(Downloader.errNoTargetImage);
			reporter.nofErrors++;
		}
	}
	
	public static void startTarget() {
		if (reporter.nofErrors <= 0) {
			Downloader bdi = UsbMpc555Loader.getInstance();
			if(bdi != null) {
				try {
					bdi.startTarget();
				} catch (DownloaderException e) {
					reporter.error(Downloader.errStartTargetFailed);
					reporter.nofErrors++;
				}
			}
		}
	}

	public static void saveTargetImageToFile(String fileName) {
		if(reporter.nofErrors <= 0){
			try {
				Linker32.writeTargetImageToFile(fileName);
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
		saveTargetImageToFile(timDirName + "/targetimage.dtim");
	}
	
	public static void saveCommandTableToFile(String fileName) {
		if(reporter.nofErrors <= 0){
			try {
				Linker32.writeCommandTableToFile(fileName);
			} catch (IOException e) { 
				e.printStackTrace();
			}
		}
		
	}

	public static void saveCommandTableToFile() {
		String tctDirName = "tct";
		File tctDir = new File(tctDirName);
		if(!tctDir.isDirectory()) {
			tctDir.mkdir();
		}
		saveCommandTableToFile(tctDirName + "/commandTable.dtct");
	}
	
	private static void clearVisitedFlagsForAllClasses() {
		Item item = Type.classList;
		while(item != null) {
			if((item.accAndPropFlags & 1<<dpfClassMark) != 0 ) { // if visited flag is set
				item.accAndPropFlags &= ~(1<<dpfClassMark);		 // delete it
			}
			item = item.next;
		}
	}
}
