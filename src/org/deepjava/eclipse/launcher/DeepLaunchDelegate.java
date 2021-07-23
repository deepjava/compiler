/*
 * Copyright 2011 - 2013 NTB University of Applied Sciences in Technology
 * Buchs, Switzerland, http://www.ntb.ch/inf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.deepjava.eclipse.launcher;

import java.lang.reflect.InvocationTargetException;

import org.deepjava.config.Configuration;
import org.deepjava.config.Programmer;
import org.deepjava.eclipse.DeepPlugin;
import org.deepjava.eclipse.ui.view.ConsoleDisplayMgr;
import org.deepjava.host.ErrorReporter;
import org.deepjava.launcher.Launcher;
import org.deepjava.target.TargetConnection;
import org.eclipse.core.resources.IProject;
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
		String projectName = configuration.getAttribute(DeepPlugin.ATTR_DEEP_LOCATION, "");	
		String launchFileName = configuration.getAttribute(DeepPlugin.ATTR_DEEP_PROGRAM, "");

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		IPath path = project.getRawLocation();

		monitor.beginTask("Download Target Image", 100);

		//clear Console
		if (cdm != null) cdm.clear();
	
		monitor.worked(10);
		if(monitor.isCanceled()) {
			monitor.done();
			return;
		}
		
		String deepProjectFile;
		if(path == null) // default project location (in workspace)
			deepProjectFile = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + projectName + IPath.SEPARATOR + launchFileName;
		else 	// in other location
			deepProjectFile = path.toString() + IPath.SEPARATOR + launchFileName;

		Launcher.buildAll(deepProjectFile, targetConfig, true);			

		monitor.worked(50);
		if(monitor.isCanceled()) {
			monitor.done();
			return;
		}

		if(ErrorReporter.reporter.nofErrors == 0 ) {
			monitor.worked(60);
			Programmer programmer = Configuration.getProgrammer();
			if (programmer != null) {
				java.lang.Class<?> cls;
				try {
					Bundle bundle = Platform.getBundle(programmer.getPluginId().toString());
					if (bundle != null) {
						cls = bundle.loadClass(programmer.getClassName().toString());
						java.lang.reflect.Method m;
						m = cls.getDeclaredMethod("getInstance");
						TargetConnection tc = (TargetConnection) m.invoke(cls);
						Launcher.setTargetConnection(tc);
						Launcher.openTargetConnection();
						if (ErrorReporter.reporter.nofErrors == 0) {
							Launcher.downloadTargetImage();
							Launcher.startTarget(Launcher.getResetAddr());
						}
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
