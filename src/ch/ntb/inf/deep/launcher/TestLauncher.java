package ch.ntb.inf.deep.launcher;

import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.linker.Linker32;

/**
 * Launcher for tests only! Adapt this file to your configuration, but don't commit
 * those changes to the SVN! Please store your project file in the top folder of
 * the deep-Project. You can find an example project "ExampleProject.deep" in
 * this folder which you may use as base for your own test project.
 */
public class TestLauncher {
	public static void main(String[] args) {
		Launcher.buildAll("M:/EUser/JCC/deep/555ExampleProject.deep", "BootFromRam");
//		Launcher.buildAll("M:/EUser/JCC/deep/555junitTarget.deep", "BootFromRam");
//		Launcher.buildAll("M:/EUser/JCC/deep/5200ExampleProject.deep", "BootFromRam");
		
		if (ErrorReporter.reporter.nofErrors == 0) {
			Launcher.openTargetConnection();
			Launcher.downloadTargetImage();
			Launcher.startTarget();
			Launcher.closeTargetConnection();
		}

//		Launcher.saveTargetImageToFile("W:/phycorempc5200b/test.bin", Configuration.BIN); System.out.println("image file created");

//		Launcher.createInterfaceFile("M:/EUser\\JCC\\bsp\\src\\ch\\ntb\\inf\\deep\\runtime\\mpc5200\\phyCoreMpc5200tiny.java");

		/* DEBUG OUTPRINTS */
//		System.out.println("%%%%%%%%%%%%%%% Class List %%%%%%%%%%%%%%%"); Linker32.printClassList(true, false, false, true);
//		System.out.println("%%%%%%%%%%%%%%% System Table %%%%%%%%%%%%%%%"); Linker32.printSystemTable();
//		System.out.println("%%%%%%%%%%%%%%% Global Constants %%%%%%%%%%%%%%%"); Linker32.printGlobalConstantTable();		
//		System.out.println("%%%%%%%%%%%%%%% Target Image (Full image) %%%%%%%%%%%%%%%"); Linker32.printTargetImage();
//		System.out.println("%%%%%%%%%%%%%%% Target Image (Segment List) %%%%%%%%%%%%%%%"); Linker32.printTargetImageSegmentList();	
//		System.out.println("%%%%%%%%%%%%%%% Memory Map %%%%%%%%%%%%%%%"); MemoryMap.getInstance().println(1);
//		System.out.println("%%%%%%%%%%%%%%% Configuration %%%%%%%%%%%%%%%"); Configuration.print();
//		System.out.println("%%%%%%%%%%%%%%% Compiler specific subroutines %%%%%%%%%%%%%%%"); Method.printCompSpecificSubroutines();
	}
}
