package ch.ntb.inf.deep.launcher;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import ch.ntb.inf.deep.cfg.CFG;
import ch.ntb.inf.deep.cgPPC.MachineCode;
import ch.ntb.inf.deep.classItems.Array;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.linkerPPC.Linker;
import ch.ntb.inf.deep.loader.Downloader;
import ch.ntb.inf.deep.loader.DownloaderException;
import ch.ntb.inf.deep.loader.UsbMpc555Loader;
import ch.ntb.inf.deep.ssa.SSA;

public class Launcher implements ICclassFileConsts {
	private static ErrorReporter reporter = ErrorReporter.reporter;
	private static PrintStream out = StdStreams.out;
	private static PrintStream vrb = StdStreams.vrb;

	public static void buildAll(String projectConfigFile,
			String targetConfiguration) {

		int attributes = (1 << atxCode) | (1 << atxLocalVariableTable)
				| (1 << atxExceptions) | (1 << atxLineNumberTable);
		reporter.clear();
		
		// 1) Read configuration
		Configuration.parseAndCreateConfig(projectConfigFile,
				targetConfiguration);
		
		try {
			// 2) Read requiered classes
			if (reporter.nofErrors <= 0)
				Class.buildSystem(Configuration.getRootClassNames(),
						Configuration.getSearchPaths(), Configuration
								.getSystemPrimitives(), attributes);

//			Type t = Type.classList;
//            while (t != null) {
//                  if (t.next.name.equals(HString.getHString("java/lang/String"))) {
//                       Item strClass = t.next;
//                       t.next = t.next.next;
//                       strClass.next = Type.classList;
//                       Type.classList = (Type)strClass;
//                       break;
//                  }
//                  t = (Type)t.next;
//            }
//            t = Type.classList;
//            System.out.println("class list ++++++++++++++++");
//            while (t != null) {
//                  System.out.println(t.name);
//                  t = (Type)t.next;
//            }

			
			
			// 2a) Initialize linker
			if (reporter.nofErrors <= 0) {
				Linker.init();
				MachineCode.init();
			}
			
			// 3) Loop One
			clearVisitedFlagsForAllClasses();
			Item item = Type.classList;
			Method method;
			while (item != null && reporter.nofErrors <= 0) {
				if (item instanceof Class) {
					Class clazz = (Class) item;

					// 3.1) Linker: calculate offsets
					if (reporter.nofErrors <= 0)
						Linker.prepareConstantBlock(clazz);

					StdStreams.vrb.println(">>>> Class: " + clazz.name + ", accAndPropFlags: " + Integer.toHexString(clazz.accAndPropFlags));
					
					method = (Method) clazz.methods;
					while (method != null && reporter.nofErrors <= 0) {
						if ((method.accAndPropFlags & ((1 << dpfSynthetic) | (1 << apfAbstract))) == 0) {
							StdStreams.vrb.println(">>>> Method: " + method.name + method.methDescriptor + ", accAndPropFlags: " + Integer.toHexString(method.accAndPropFlags));

							// 3.2) Create CFG
							if (reporter.nofErrors <= 0)
								method.cfg = new CFG(method);
							// method.cfg.printToLog();

							// 3.3) Create SSA
							if (reporter.nofErrors <= 0)
								method.ssa = new SSA(method.cfg);

							// 3.4) Create machine code
							if (reporter.nofErrors <= 0)
								method.machineCode = new MachineCode(method.ssa);

						}
						method = (Method) method.next;
					}

					// 3.5) Linker: calculate required size
					if (reporter.nofErrors <= 0)
						Linker.calculateCodeSizeAndOffsets(clazz);
				}
				item = item.next;
			}

			// 4) Linker: freeze memory map
			if (reporter.nofErrors <= 0) {
				Linker.calculateSystemTableSize();
				Linker.freezeMemoryMap();
			}

			// 5) Loop Two 
			item = Type.classList;
			while (item != null && reporter.nofErrors <= 0) {
				// 5.1) Linker: calculate absolute addresses
				if (item instanceof Class) {
					Linker.calculateAbsoluteAddresses((Class)item);
				}
				else if(item instanceof Array) {
					Linker.calculateAbsoluteAddresses((Array)item);
				}
				
				item = item.next;
			}
			
			clearVisitedFlagsForAllClasses();
			item = Type.classList;
			while (item != null && reporter.nofErrors <= 0) { // TODO: why is here another loop??? -> move to loop two?
				// 5.3) Linker: Create constant block
				if(item instanceof Class) {
					Class clazz = (Class) item;
					Linker.createConstantBlock(clazz);

					method = (Method) clazz.methods;
					while (method != null && reporter.nofErrors <= 0) {
						if ((method.accAndPropFlags & ((1 << dpfSynthetic) | (1 << apfAbstract))) == 0) {
							// 5.2) Code generator: fix up
							method.machineCode.doFixups();
						}
						method = (Method) method.next;
					}
				}
				else if(item instanceof Array) {
					Linker.createTypeDescriptor((Array)item);
				}

				item = item.next;
			}

			// 6) Linker: Create system table
			if (reporter.nofErrors <= 0)
				Linker.createSystemTable();

			// 7) Linker: Create target image
			if (reporter.nofErrors <= 0)
				Linker.generateTargetImage();
			if (reporter.nofErrors > 0) {
				out.println("Compilation failed with " + reporter.nofErrors + " error(s)");
			} else {
				out.println("Compilation and target image generation successfully finished");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void downloadTargetImage() {
		if (reporter.nofErrors <= 0) {
			Downloader bdi = UsbMpc555Loader.getInstance();
			try {
				bdi.init();
				// System.out.println("++++++++ Start Target!+++++++++");
				bdi.startTarget();
			} catch (DownloaderException e) {
				e.printStackTrace();
			}
		} else {
			out.println("No target image to load");
		}
	}

	public static void saveTargetImageToFile(String fileName) {
		if (reporter.nofErrors <= 0){
			try {
				Linker.writeTargetImageToFile(fileName);
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
				Linker.writeCommandTableToFile(fileName);
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
