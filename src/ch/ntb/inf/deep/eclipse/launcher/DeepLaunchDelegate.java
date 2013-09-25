/*
 * Copyright (c) 2011 NTB Interstate University of Applied Sciences of Technology Buchs.
 *
 * http://www.ntb.ch/inf
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Eclipse Public License for more details.
 * 
 * Contributors:
 *     NTB - initial implementation
 * 
 */

package ch.ntb.inf.deep.eclipse.launcher;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.osgi.framework.Bundle;

import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Programmer;
import ch.ntb.inf.deep.eclipse.DeepPlugin;
import ch.ntb.inf.deep.eclipse.ui.view.ConsoleDisplayMgr;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.launcher.Launcher;
import ch.ntb.inf.deep.strings.HString;
import ch.ntb.inf.deep.target.TargetConnection;


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
		if (cdm != null) cdm.clear();
	
		monitor.worked(10);
		if(monitor.isCanceled()) {
			monitor.done();
			return;
		}
		
		if(location.charAt(0) == IPath.SEPARATOR ){			
			Launcher.buildAll(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + /*IPath.SEPARATOR +*/ location + IPath.SEPARATOR + program, targetConfig);			
		}
		else {
			Launcher.buildAll(location + IPath.SEPARATOR + program, targetConfig);
		}
		
		monitor.worked(50);
		if(monitor.isCanceled()) {
			monitor.done();
			return;
		}
		
		if(ErrorReporter.reporter.nofErrors == 0 ) {
			Launcher.saveTargetImageToFile();
		}
		
		if(ErrorReporter.reporter.nofErrors == 0 ) {
			monitor.worked(60);
			Programmer programmer = Configuration.getProgrammer();
			if (programmer != null) {
				java.lang.Class<?> cls;
				try {
					Bundle bundle = Platform.getBundle(programmer.getPluginId().toString());
					if (bundle != null) {
//						System.out.println(bundle.getSymbolicName() + " 1");
						cls = bundle.loadClass(programmer.getClassName().toString());
//						System.out.println(cls.getName() + " 2");
						//					cls = java.lang.Class.forName(programmer.getClassName().toString());
						java.lang.reflect.Method m;
						m = cls.getDeclaredMethod("getInstance");
						TargetConnection tc = (TargetConnection) m.invoke(cls);
						Launcher.setTargetConnection(tc);
						Launcher.openTargetConnection();
						Launcher.downloadTargetImage();
						Launcher.startTarget();
					} else ErrorReporter.reporter.error(812, programmer.getClassName().toString());
				} catch (ClassNotFoundException e) {
					ErrorReporter.reporter.error(811, programmer.getClassName().toString());
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		monitor.worked(100);
		monitor.done();

		IDebugTarget target = new DummyDebugTarget(launch);
		launch.addDebugTarget(target);
		target.terminate();
	}
}
