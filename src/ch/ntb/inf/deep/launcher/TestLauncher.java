package ch.ntb.inf.deep.launcher;

import ch.ntb.inf.deep.linker.Linker32;

/**
 * Launcher for tests only! Adept this file to your configuration, but don't commit
 * those changes to the SVN! Please store your project file in the top folder of
 * the deep-Project. You can find an example project "ExampleProject.deep" in
 * this folder which you may use as base for your own test project.
 */
public class TestLauncher {
	public static void main(String[] args) {

		Launcher.buildAll("M:/EUser/JCC/Deep/ExampleProject.deep", "BootFromRam");
//		Linker32.printClassList(true, false, false, true);
		Launcher.downloadTargetImage();
		Launcher.startTarget();
		Launcher.saveCommandTableToFile("M:/EUser/JCC/junitTarget/tct/commandTable.dtct");

		
	}

}
