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

package org.deepjava.eclipse.ui.refactoring;

import java.io.File;

import org.deepjava.eclipse.ui.properties.DeepFileChanger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

public class RenameDeepProject extends RenameParticipant {
	private IProject p;
	private DeepFileChanger dfc;
	
	@Override
	public RefactoringStatus checkConditions(IProgressMonitor arg0,
			CheckConditionsContext arg1) throws OperationCanceledException {
		return null;
	}

	@Override
	public Change createChange(IProgressMonitor arg0) throws CoreException,
			OperationCanceledException {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	protected boolean initialize(Object element) {
		String newName = getArguments().getNewName();
		p = (IProject) element;
		try {
			dfc = new DeepFileChanger(p.getLocation()	+ "/" + p.getName() + ".deep");
			dfc.changeContent("description", "\"deep project file for " + newName + "\"");
			dfc.changeProjectName(newName);
			dfc.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		File oldFile = new File(p.getLocation()	+ "/" + p.getName() + ".deep"); 
		oldFile.renameTo(new File(p.getLocation()	+ "/" + newName + ".deep"));
		return true;
	}

}
