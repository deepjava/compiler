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

package ch.ntb.inf.deep.eclipse.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import ch.ntb.inf.deep.linker.Linker32;

public class ClasslistAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	private static boolean printMethods = false;
	private static boolean printFields = false;
	private static boolean printConstantFields = false;
	private static boolean printConstantBlock = false;
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public void run(IAction action) {
		String actionID = action.getId();
		if(actionID == null) return;
		if(actionID.equals("ch.ntb.inf.deep.eclipse.ui.actions.PrintClasslistAction")){
			Linker32.printClassList(printMethods, printFields, printConstantFields, printConstantBlock);
		}else if(actionID.equals("ch.ntb.inf.deep.eclipse.ui.actions.setPrintMethod")){
			printMethods = action.isChecked();
		}else if(actionID.equals("ch.ntb.inf.deep.eclipse.ui.actions.setPrintFields")){
			printFields = action.isChecked();
		}else if(actionID.equals("ch.ntb.inf.deep.eclipse.ui.actions.setPrintConstFields")){
			printConstantFields = action.isChecked();
		}else if(actionID.equals("ch.ntb.inf.deep.eclipse.ui.actions.setPrintConstantBlock")){
			printConstantBlock = action.isChecked();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {	
	}

}
