package ch.ntb.inf.deep.launcher;


/**
 * Launcher for tests only! Adept this file to your configuration, but don't commit
 * those changes to the SVN! Please store your project file in the top folder of
 * the deep-Project. You can find an example project "ExampleProject.deep" in
 * this folder which you may use as base for your own test project.
 */
public class TestLauncher {
	public static void main(String[] args) {
		Launcher.buildAll("D:/work/Crosssystem/deep/ExampleProject.deep", "BootFromRam");
		//Configuration.print();
		Launcher.downloadTargetImage();
		//UsbMpc555Loader.getInstance();
		//Linker.printClassList();
		//Linker.printSystemTable();
		//Linker.printTargetImage();
		//Launcher.saveTargetImage2File("C:/temp/deep_example_target_image.dti");
		
		
		
//		UsbMpc555Loader.getInstance().closeConnection();
//		USBLog log = new USBLog("UsbLog");
//		log.addJobChangeListener(new JobChangeAdapter(){
//			public void done(IJobChangeEvent event){
//				if(event.getResult().isOK())
//					System.out.println("Job completed successfully");
//				else
//					System.out.println("Job did not complete successfully");
//			}
//		});
//		log.setSystem(true);
//		log.schedule();
//		while(true);

	}

}
