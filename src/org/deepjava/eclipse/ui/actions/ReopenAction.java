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

package org.deepjava.eclipse.ui.actions;

import org.deepjava.host.ErrorReporter;
import org.deepjava.host.StdStreams;
import org.deepjava.launcher.Launcher;
import org.deepjava.target.TargetConnection;
import org.deepjava.target.TargetConnectionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class ReopenAction implements IWorkbenchWindowActionDelegate {

	@SuppressWarnings("unused")
	private IWorkbenchWindow window;
    public static final String ID = "org.deepjava.eclipse.ui.actions.ReopenAction";
	
	@Override
	public void dispose() {}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public void run(IAction action) {
		TargetConnection tc = Launcher.getTargetConnection();
		if (tc == null){
			ErrorReporter.reporter.error(100);
			return;
		}
		tc.closeConnection();
		try {
			Thread.sleep(500);//Give OS time 
			tc.openConnection();
			StdStreams.log.println("Device successfully reopened");
		} catch (TargetConnectionException e) {
			tc.closeConnection();
			ErrorReporter.reporter.error(804);
		} catch (InterruptedException e) {
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {}

}
