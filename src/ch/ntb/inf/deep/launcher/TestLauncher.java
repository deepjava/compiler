package ch.ntb.inf.deep.launcher;

/**
 * Launcher for tests only! Adept this file to your configuration, but don't commit
 * those changes to the SVN! Please store your project file in the top folder of
 * the deep-Project. You can find an example project "ExampleProject.deep" in
 * this folder which you may use as base for your own test project.
 */
public class TestLauncher {
	public static void main(String[] args) {

		Launcher.buildAll("D:/path/to/deep/project/ExampleProject.deep", "BootFromRAM");

		Launcher.saveTargetImageToFile("C:/temp/deep_example_target_image.dti");

		
	}

}
