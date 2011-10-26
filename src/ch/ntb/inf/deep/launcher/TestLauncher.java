package ch.ntb.inf.deep.launcher;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.linker.Linker32;
import ch.ntb.inf.deep.strings.HString;

/**
 * Launcher for tests only! Adept this file to your configuration, but don't commit
 * those changes to the SVN! Please store your project file in the top folder of
 * the deep-Project. You can find an example project "ExampleProject.deep" in
 * this folder which you may use as base for your own test project.
 */
public class TestLauncher {
	public static void main(String[] args) {

		Launcher.buildAll("D:/work/Project_deep/deep/ExampleProject.deep", "BootFromRam");
//		Launcher.buildAll("D:/work/Project_deep/deep/ExampleProject.deep", "BootFromFlash");
//		Launcher.buildAll("D:/work/Project_deep/deep/jUnitTargetTests.deep", "BootFromFlash");

	//	Launcher.saveTargetImage2File("C:/temp/deep_example_target_image.dti");
		
	//	System.out.println("%%%%%%%%%%%%%%% Class List %%%%%%%%%%%%%%%"); Linker.printClassList();
		System.out.println("%%%%%%%%%%%%%%% Class List %%%%%%%%%%%%%%%"); Linker32.printClassList(true, true, true, true);
		
	//	System.out.println("%%%%%%%%%%%%%%% System Table %%%%%%%%%%%%%%%"); Linker32.printSystemTable();
		
	//	Class rc = (Class)Type.classList.getItemByName("java/io/PrintStream");
	//	Method m = (Method)rc.methods.getItemByName("length");
		
	//	m.machineCode.print();
		
	//	System.out.println("%%%%%%%%%%%%%%% Global Constants %%%%%%%%%%%%%%%"); Linker.printGlobalConstantTable();
		
	//	System.out.println("%%%%%%%%%%%%%%% Target Image %%%%%%%%%%%%%%%"); Linker32.printTargetImage();
	//	System.out.println("%%%%%%%%%%%%%%% Target Image %%%%%%%%%%%%%%%"); Linker32.printTargetImageSegmentList();
		
	//	System.out.println("%%%%%%%%%%%%%%% MEMORY MAP %%%%%%%%%%%%%%%"); MemoryMap.getInstance().println(1);
		
	//	System.out.println("%%%%%%%%%%%%%%% CONFIGURATION %%%%%%%%%%%%%%%"); Configuration.print();
		
	//	Launcher.downloadTargetImage();
		
	//	Launcher.saveTargetImageToFile();
		
	//	Launcher.saveCommandTableToFile("D:/work/Project_deep/junitTarget/tct/commandTable.dtct");

//		System.out.println("Size of object: " + Linker.getSizeOfObject() + " byte");
//		System.out.println("f = " + Integer.toHexString(Float.floatToIntBits(ch.ntb.inf.deep.testClasses.T10Constants.f)));
//		System.out.println("d = " + Long.toHexString(Double.doubleToLongBits(ch.ntb.inf.deep.testClasses.T10Constants.d)));

//		Configuration.createInterfaceFile(HString.getHString("D:/work/Project_deep/RuntimeSystem/src/ch/ntb/inf/deep/runtime/mpc555/ntbMpc555HB.java"));
		
	}

}
