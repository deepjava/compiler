package ch.ntb.inf.deep.eclipse.launcher;

import java.io.PrintStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import ch.ntb.inf.deep.eclipse.DeepPlugin;
import ch.ntb.inf.deep.eclipse.ui.view.ConsoleDisplayMgr;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.launcher.Launcher;


public class DeepLaunchDelegate extends JavaLaunchDelegate{


	@Override
	public final void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)throws CoreException{
		ConsoleDisplayMgr cdm = ConsoleDisplayMgr.getDefault();

		String targetConfig = configuration.getAttribute(DeepPlugin.ATTR_TARGET_CONFIG, "");
		String location = configuration.getAttribute(DeepPlugin.ATTR_DEEP_LOCATION, "");
		String program = configuration.getAttribute(DeepPlugin.ATTR_DEEP_PROGRAM, "");

		monitor.beginTask("Download Target Image", 100);

		//Init Console
		if(cdm != null){
			cdm.clear();
			PrintStream out = new PrintStream(cdm.getNewIOConsoleOutputStream(ConsoleDisplayMgr.MSG_INFORMATION));
			PrintStream err = new PrintStream(cdm.getNewIOConsoleOutputStream(ConsoleDisplayMgr.MSG_ERROR));
			StdStreams.vrb = out;
			//StdStreams.log = out;
			StdStreams.out = out;
			StdStreams.err = err;
		}
		
	
		monitor.worked(10);
		if (monitor.isCanceled()) {
			monitor.done();
			return;
		}		
		
		Launcher.buildAll(location +"/" +program, targetConfig);
		
		monitor.worked(60);
		if (monitor.isCanceled()) {
			monitor.done();
			return;
		}
		if(ErrorReporter.reporter.nofErrors == 0 ){
			Launcher.downloadTargetImage();
		}
		monitor.worked(100);
		monitor.done();
		
		IDebugTarget target = new DummyDebugTarget(launch);
		launch.addDebugTarget(target);
		target.terminate();
		
		
		
	}
}
