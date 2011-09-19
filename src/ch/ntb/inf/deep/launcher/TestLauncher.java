package ch.ntb.inf.deep.launcher;

import ch.ntb.inf.deep.host.ErrorReporter;

/**
 * Launcher for tests only! Adept this file to your configuration, but
 * <strong>do not commit those changes to the SVN!</strong> Please store your
 * project file in the top folder of the deep-Project. You can find an example
 * project "ExampleProject.deep" in this folder which you may use as base for
 * your own test project.
 */
public class TestLauncher {
	public static void main(String[] args) {
		Launcher.buildAll("/PATH/TO/YOUR/PROJECT/ExampleProject.deep", "BootFromRam");

		if (ErrorReporter.reporter.nofErrors == 0) {
			Launcher.downloadTargetImage();
			Launcher.startTarget();
		}
	}
}
