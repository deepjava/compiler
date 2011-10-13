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
		reporter.clear();
		
		// 1) Read configuration
		Configuration.parseAndCreateConfig(projectConfigFile, targetConfiguration);
		
		try {
			// 2) Read requiered classes
			if (reporter.nofErrors <= 0)
				Class.buildSystem(Configuration.getRootClassNames(), Configuration.getSearchPaths(), Configuration.getSystemPrimitives(), attributes);
	
			
			// 3) Initialize linker
			if (reporter.nofErrors <= 0) {
				Linker32.init();
				CodeGen.init();
			}
			
			// 4) Loop One
			clearVisitedFlagsForAllClasses();
			Item item = Type.classList;
			Method method;
			if (reporter.nofErrors <= 0) {				
				out.println("Loaded classes");
			}
			while (item != null && reporter.nofErrors <= 0) {
				if(item instanceof Class && ((item.accAndPropFlags & (1 << apfInterface)) == 0)) {
					
					Class clazz = (Class) item;

					// 3.1) Linker: calculate offsets
					if (reporter.nofErrors <= 0)
						Linker32.prepareConstantBlock(clazz);

					out.printf("Class: %1$s\n", clazz.name);
					
					method = (Method) clazz.methods;
					while (method != null && reporter.nofErrors <= 0) {
						if ((method.accAndPropFlags & ((1 << dpfSynthetic) | (1 << apfAbstract))) == 0) {
							if(dbg)vrb.println(">>>> Method: " + method.name + method.methDescriptor + ", accAndPropFlags: " + Integer.toHexString(method.accAndPropFlags));

							// 3.2) Create CFG
							if (reporter.nofErrors <= 0)
								method.cfg = new CFG(method);
							// method.cfg.printToLog();

							// 3.3) Create SSA
							if (reporter.nofErrors <= 0)
								method.ssa = new SSA(method.cfg);

							// 3.4) Create machine code
							if (reporter.nofErrors <= 0)
								method.machineCode = new CodeGen(method.ssa);

						}
						method = (Method) method.next;
					}

					// 3.5) Linker: calculate required size
					if (reporter.nofErrors <= 0)
						Linker32.calculateCodeSizeAndOffsets(clazz);
				}
				item = item.next;
			}
			out.println();

			// 5) Linker: freeze memory map
			if (reporter.nofErrors <= 0) {
				Linker32.calculateSystemTableSize();
				Linker32.calculateGlobalConstantTableSize();
				Linker32.freezeMemoryMap();
			}
			
			// 6) Loop Two 
			item = Type.classList;
			while (item != null && reporter.nofErrors <= 0) {
				if (item instanceof Class && ((item.accAndPropFlags & (1 << apfInterface)) == 0)) {
					Class clazz = (Class) item;
					// 6.1) Linker: calculate absolute addresses
					Linker32.calculateAbsoluteAddresses(clazz);
					// 6.2) Linker: arrange constant
					Linker32.updateConstantBlock(clazz);					
				}
				else if(item instanceof Array) {
					// 6.1) Linker: calculate absolute addresses
					Linker32.calculateAbsoluteAddresses((Array)item);
				}	
				item = item.next;
			}
			// 7) create constant table
			Linker32.createGlobalConstantTable();
			
			
			clearVisitedFlagsForAllClasses();
			// 8) Loop Three
			item = Type.classList;
			while (item != null && reporter.nofErrors <= 0) {//before we do the fix up all addresses must be calculated
				if (item instanceof Class && ((item.accAndPropFlags & (1 << apfInterface)) == 0)) {
					Class clazz = (Class) item;
					method = (Method) clazz.methods;
					while (method != null && reporter.nofErrors <= 0) {
						if ((method.accAndPropFlags & ((1 << dpfSynthetic) | (1 << apfAbstract))) == 0) {
							// 8.1) Code generator: fix up
							method.machineCode.doFixups();
						}
						method = (Method) method.next;
					}
				}
				else if(item instanceof Array) {
					// 8.2) Linker: create type descriptor for Arrays
					Linker32.createTypeDescriptor((Array)item);
				}
				item = item.next;
			}

			// 9) Linker: Create system table
			if (reporter.nofErrors <= 0)
				Linker32.createSystemTable();

			// 10) Linker: Create target image
			if (reporter.nofErrors <= 0)
				Linker32.generateTargetImage();
			if (reporter.nofErrors > 0) {
				out.println("Compilation failed with " + reporter.nofErrors + " error(s)");
				out.println();
			} else {
				out.println("Compilation and target image generation successfully finished");
				out.println();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void downloadTargetImage() {
		if (reporter.nofErrors <= 0) {
			Downloader bdi = UsbMpc555Loader.getInstance();
			try {
				if(bdi != null){
					bdi.init();
				}else{
					reporter.error(Downloader.errTargetNotFound);
					reporter.nofErrors++;
				}
			} catch (DownloaderException e) {
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
			if (bdi != null) {
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
		if (reporter.nofErrors <= 0){
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
		if (reporter.nofErrors <= 0){
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
