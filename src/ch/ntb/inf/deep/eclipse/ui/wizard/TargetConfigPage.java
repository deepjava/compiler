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

package ch.ntb.inf.deep.eclipse.ui.wizard;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Parser;

class TargetConfigPage extends WizardPage {
		
	private Combo boardCombo, programmerCombo, osCombo;
	private String[][] boards;
	private String[][] operatingSystems;
	private String[][] programmers;
	
	protected TargetConfigPage(String pageName) {
		super(pageName);
		setPageComplete(true);
	}

	@Override
	public void createControl(Composite parent) {		
		Composite composite = new Composite(parent, SWT.NONE);
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		composite.setLayout(layout);
		
		Group groupBoard = new Group(composite, SWT.NONE);
		groupBoard.setText("Board configuration");
		groupBoard.setLayout(layout);
		Label boardLabel = new Label(groupBoard, SWT.NONE);
		boardLabel.setText("Select a board");
		boardCombo = new Combo(groupBoard, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		Label progLabel = new Label(groupBoard, SWT.NONE);
		progLabel.setText("Select a programmer");
		programmerCombo = new Combo(groupBoard, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		
		Group groupOS = new Group(composite, SWT.NONE);
		groupOS.setText("Runtime system");
		groupOS.setLayout(layout);
		Label osLabel = new Label(groupOS,SWT.NONE);
		osLabel.setText("Select a operating system");
		osCombo = new Combo(groupOS, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		setControl(composite);
	}
		
	private void insertBoards() {
		if (((DeepProjectWizard)getWizard()).model.getLibrary() != null) {
			File lib = ((DeepProjectWizard)getWizard()).model.getLibrary();
			boards = Configuration.searchDescInConfig(new File(lib.toString() + Configuration.boardsPath), Parser.sBoard);
			String[] str = new String[boards.length];
			int index = 0;
			for (int i = 0; i < boards.length; i++) {
				str[i] = boards[i][1];
				if (boards[i][0].contains("NTB MPC555")) index = i;
			}
			boardCombo.setItems(str);
			boardCombo.select(index);
		}
	}
	
	private void insertOperatingSystems() {
		if (((DeepProjectWizard)getWizard()).model.getLibrary() != null) {
			File lib = ((DeepProjectWizard)getWizard()).model.getLibrary();
			operatingSystems = Configuration.searchDescInConfig(new File(lib.toString() + Configuration.osPath), Parser.sOperatingSystem);
			String[] str = new String[operatingSystems.length];
			for (int i = 0; i < operatingSystems.length; i++) str[i] = operatingSystems[i][1];
			osCombo.setItems(str);
			osCombo.select(0);
		}
	}
	
	private void insertProgrammers() {
		if (((DeepProjectWizard)getWizard()).model.getLibrary() != null) {
			File lib = ((DeepProjectWizard)getWizard()).model.getLibrary();
			programmers = Configuration.searchDescInConfig(new File(lib.toString() + Configuration.progPath), Parser.sProgrammer);
			String[] str = new String[programmers.length];
			int index = 0;
			for (int i = 0; i < programmers.length; i++) {
				str[i] = programmers[i][1];
				if (programmers[i][0].contains("555")) index = i;
			}
			programmerCombo.setItems(str);
			programmerCombo.select(index);
		}
	}
	
	private boolean validatePage() {
		if (boardCombo.getSelectionIndex() == -1 || osCombo.getSelectionIndex() == -1 || programmerCombo.getSelectionIndex() == -1){
			return false;
		}
		((DeepProjectWizard)getWizard()).model.setBoard(boards[boardCombo.getSelectionIndex()]);
		((DeepProjectWizard)getWizard()).model.setOs(operatingSystems[osCombo.getSelectionIndex()]);
		((DeepProjectWizard)getWizard()).model.setProgrammer(programmers[programmerCombo.getSelectionIndex()]);
		return true;
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			insertBoards();
			insertOperatingSystems();
			insertProgrammers();
		}
        getControl().setVisible(visible);
        validatePage();	// must happen to store settings to model in case that no combo is changed
    }
}
