/**
 * Copyright (c) 2010 NTB Interstaatliche Hochschule für Technick Buchs
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * 
 * Contributors:
 *	   NTB - initial implementation
 *	   Roger Millischer - initial implementation
 */
package ch.ntb.inf.deep.eclipse.ui.perspective;

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
