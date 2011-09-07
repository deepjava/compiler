package ch.ntb.inf.deep.launcher;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.MemoryMap;
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

		Launcher.buildAll("D:/work/Crosssystem/deep/ExampleProject.deep", "BootFromRam");

//		Launcher.buildAll("D:/work/JUnitTarget3/junitTarget/junitTarget.deep", "BootFromRam");
		//System.out.println(Configuration.getSystemMethodForID(0x1101));

		Launcher.downloadTargetImage();
		Launcher.startTarget();
		Launcher.saveCommandTableToFile("D:/work/JUnitTarget3/junitTarget/tct/commandTable.dtct");

//		Configuration.print();
//		Configuration.createInterfaceFile(HString.getHString("D:/work/Crosssystem/bsp/src/ch/ntb/inf/deep/runtime/mpc555/ntbMpc555HB.java"));
		//UsbMpc555Loader.getInstance();
//		Linker32.printClassList(false,true,false,true);
//		//Linker.printSystemTable();
//		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//		MemoryMap.getInstance().println(1);
//		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
//		Linker32.printTargetImage();
//		Linker32.printTargetImageSegmentList();
//		Launcher.saveTargetImageToFile("C:/temp/deep_example_target_image.dti");

		
	}

}
