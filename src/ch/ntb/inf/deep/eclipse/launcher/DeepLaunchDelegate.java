package ch.ntb.inf.deep.eclipse.launcher;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import ch.ntb.inf.deep.eclipse.DeepPlugin;
import ch.ntb.inf.deep.eclipse.ui.view.ConsoleDisplayMgr;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.launcher.Launcher;


public class DeepLaunchDelegate extends JavaLaunchDelegate{


	@Override
	public final void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)throws CoreException{
		if(!mode.equals(ILaunchManager.RUN_MODE)){
			return;
		}
		
		//terminate all other DebugTargets
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches =lm.getLaunches();
		for(int i = 0; i < launches.length; i++){
			if(launches[i].getDebugTarget() != null){
				launches[i].getDebugTarget().terminate();
			}
		}
		
		ConsoleDisplayMgr cdm = ConsoleDisplayMgr.getDefault();

		String targetConfig = configuration.getAttribute(DeepPlugin.ATTR_TARGET_CONFIG, "");
		String location = configuration.getAttribute(DeepPlugin.ATTR_DEEP_LOCATION, "");
		String program = configuration.getAttribute(DeepPlugin.ATTR_DEEP_PROGRAM, "");

		monitor.beginTask("Download Target Image", 100);

		//clear Console
		if(cdm != null){
			cdm.clear();
		}
		
	
		monitor.worked(10);
		if (monitor.isCanceled()) {
			monitor.done();
			return;
		}		
		if(location.charAt(0) == IPath.SEPARATOR ){			
			Launcher.buildAll(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + IPath.SEPARATOR + location + IPath.SEPARATOR +program, targetConfig);			
		}else{
			Launcher.buildAll(location + IPath.SEPARATOR + program, targetConfig);
		}
		
		monitor.worked(60);
		if (monitor.isCanceled()) {
			monitor.done();
			return;
		}
		if(ErrorReporter.reporter.nofErrors == 0 ){
			Launcher.downloadTargetImage();
			Launcher.startTarget();
		}
		monitor.worked(100);
		monitor.done();
		
		IDebugTarget target = new DummyDebugTarget(launch);
		launch.addDebugTarget(target);
		target.terminate();
		
		
		
	}
}
