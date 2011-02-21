package ch.ntb.inf.deep.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import ch.ntb.inf.deep.DeepPlugin;

public class DeepLaunchDelegate extends JavaLaunchDelegate{


	@Override
	public final void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)throws CoreException{
		monitor.beginTask("Download Target Image", 100);

		String targetConfig = configuration.getAttribute(DeepPlugin.ATTR_TARGET_CONFIG, (String)null);
		String program = configuration.getAttribute(DeepPlugin.ATTR_DEEP_PROGRAM, (String)null);
	
		monitor.worked(10);
		if (monitor.isCanceled()) {
			return;
		}		
		
		Launcher.buildAll(program, targetConfig);
		
		monitor.worked(60);
		if (monitor.isCanceled()) {
			return;
		}
		
		Launcher.downloadTargetImage();
		monitor.worked(100);
	}
}
