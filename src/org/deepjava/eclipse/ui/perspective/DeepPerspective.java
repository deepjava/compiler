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

package org.deepjava.eclipse.ui.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.jdt.ui.JavaUI;

public class DeepPerspective implements IPerspectiveFactory {	
	private IPageLayout layout;

	@Override
	public void createInitialLayout(IPageLayout layout) {
		this.layout = layout;
		addViews();
		addActionSets();
		addNewWizardShortcuts();
		addPerspectiveShortcuts();
		addViewShortcuts();
	}
	
	private void addViews(){
		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, 0.15f, layout.getEditorArea());
		left.addView("org.eclipse.jdt.ui.PackageExplorer");
		
		IFolderLayout bot = layout.createFolder("bottom",IPageLayout.BOTTOM,0.76f,layout.getEditorArea());
		bot.addView("org.eclipse.pde.runtime.LogView");
		bot.addView("org.eclipse.ui.views.ProblemView");
		bot.addView("org.eclipse.ui.views.TaskList");
		bot.addView("org.eclipse.ui.console.ConsoleView");
		bot.addView("ch.ntb.inf.deep.eclipse.ui.view.ClassTreeView");
		bot.addView("ch.ntb.inf.deep.eclipse.ui.view.TargetOperationView");
		
		IFolderLayout rightTop = layout.createFolder("rightTop", IPageLayout.RIGHT, 0.80f, layout.getEditorArea());
		rightTop.addView(IPageLayout.ID_OUTLINE);
		
	}
	
	private void addActionSets() {
		layout.addActionSet("org.eclipse.debug.ui.launchActionSet"); 
		layout.addActionSet("org.eclipse.debug.ui.debugActionSet"); 
		layout.addActionSet("org.eclipse.debug.ui.profileActionSet"); 
		layout.addActionSet("org.eclipse.jdt.debug.ui.JDTDebugActionSet"); 
		layout.addActionSet("org.eclipse.jdt.junit.JUnitActionSet"); 
		layout.addActionSet("org.eclipse.team.ui.actionSet"); 
		layout.addActionSet("org.eclipse.team.cvs.ui.CVSActionSet"); 
		layout.addActionSet("org.eclipse.ant.ui.actionSet.presentation"); 
		layout.addActionSet(JavaUI.ID_ACTION_SET);
		layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET); 
	}
	
	private void addNewWizardShortcuts() {
		layout.addNewWizardShortcut("ch.ntb.inf.deep.ui.wizard.StartWizard");
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewPackageCreationWizard");
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewClassCreationWizard");
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewInterfaceCreationWizard");
		layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSourceFolderCreationWizard");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");
	}
	
	private void addPerspectiveShortcuts(){
		//TODO define
	}
	private void addViewShortcuts(){
		//TODO define
	}

}
