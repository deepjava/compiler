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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.ntb.inf.deep.config.Board;
import ch.ntb.inf.deep.config.OperatingSystem;
import ch.ntb.inf.deep.config.Programmer;
import ch.ntb.inf.deep.eclipse.DeepPlugin;
import ch.ntb.inf.deep.eclipse.ui.preferences.PreferenceConstants;

class TargetConfigPage extends WizardPage {
		
	private Composite composite;
	private Combo boardCombo, rtsCombo, programmerCombo;
	private Button check;
	private Text path;
	private final String defaultPath = DeepPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.DEFAULT_LIBRARY_PATH);
	private String lastChoise = "";
	private Board[] boards;
	private OperatingSystem[] operatingsystems;
	private Programmer[] programmers;
	
	private SelectionAdapter selectionListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e){
			if (e.widget.equals(check)){
				if(check.getSelection()){
					path.setEnabled(false);
					path.setText(defaultPath);
				}else{
					path.setEnabled(true);
					path.setText(lastChoise);
				}
			}
			setPageComplete((ValidatePage()));
		}
	};

	protected TargetConfigPage(String pageName) {
		super(pageName);
		setPageComplete(true);
	}

	@Override
	public void createControl(Composite parent) {		
		composite = new Composite(parent, SWT.NONE);
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		composite.setLayout(layout);
		Group groupBoard = new Group(composite, SWT.NONE);
		groupBoard.setText("Board configuration");
		FillLayout groupLayout1 = new FillLayout(SWT.VERTICAL);
		groupBoard.setLayout(groupLayout1);
		Label boardLabel = new Label(groupBoard, SWT.NONE);
		boardLabel.setText("Select a board");
		boardCombo = new Combo(groupBoard, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		boardCombo.addSelectionListener(selectionListener);
		Label labelProgrammer = new Label(groupBoard, SWT.NONE);
		labelProgrammer.setText("Select a programmer");
		programmerCombo = new Combo(groupBoard, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		programmerCombo.addSelectionListener(selectionListener);		
		Group groupOS = new Group(composite, SWT.NONE);
		groupOS.setText("Runtime system");
		FillLayout groupLayout2 = new FillLayout(SWT.VERTICAL);
		groupOS.setLayout(groupLayout2);
		Label osLabel = new Label(groupOS,SWT.NONE);
		osLabel.setText("Select a operating system");
		rtsCombo = new Combo(groupOS, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		rtsCombo.addSelectionListener(selectionListener);
		setControl(composite);
	}
		
	private void insertBoards() {
		if(((DeepProjectWizard)getWizard()).model.getLibrary() != null) {
			this.boards = ((DeepProjectWizard)getWizard()).model.getLibrary().getBoards();
			String[] boardDescs = new String[boards.length];
			for(int i = 0; i < boardDescs.length; i++) {
				boardDescs[i] = boards[i].getDescription().toString();
			}
			boardCombo.setItems(boardDescs);
			boardCombo.select(0);
		}
	}
	
	private void insertOperatingSystems() {
		if(((DeepProjectWizard)getWizard()).model.getLibrary() != null) {
			this.operatingsystems = ((DeepProjectWizard)getWizard()).model.getLibrary().getOperatingSystems();
			String[] osDescs = new String[operatingsystems.length];
			for(int i = 0; i < osDescs.length; i++) {
				osDescs[i] = operatingsystems[i].getDescription().toString();
			}
			rtsCombo.setItems(osDescs);
			rtsCombo.select(0);
		}
	}
	
	private void insertProgrammers() {
		if(((DeepProjectWizard)getWizard()).model.getLibrary() != null) {
			this.programmers = ((DeepProjectWizard)getWizard()).model.getLibrary().getProgrammers();
			String[] progDescs = new String[programmers.length];
			for(int i = 0; i < progDescs.length; i++) {
				progDescs[i] = programmers[i].getDescription().toString();
			}
			programmerCombo.setItems(progDescs);
			programmerCombo.select(0);
		}
	}
	
	public String getBoardValue(){
		return boardCombo.getItem(boardCombo.getSelectionIndex());
	}
	public String getRunTimeSystemValue(){
		return rtsCombo.getItem(rtsCombo.getSelectionIndex());
	}
	
	/**
	 * Open a resource chooser to select a program
	 */
	protected void openDirectoryDialog() {
		DirectoryDialog dlg = new DirectoryDialog(getShell());

        // Set the initial filter path according
        // to anything they've selected or typed in
        dlg.setFilterPath(path.getText());

        // Change the title bar text
        dlg.setText("Deep Library Path Selection");

        // Customizable message displayed in the dialog
        dlg.setMessage("Select a directory");

        // Calling open() will open and run the dialog.
        // It will return the selected directory, or
        // null if user cancels
        String dir = dlg.open();
        if (dir != null) {
          // Set the text box to the new selection
        	path.setText(dir);
        	lastChoise = dir;
        }

	}
	
	private boolean ValidatePage() {
		if(boardCombo.getSelectionIndex() == -1 || rtsCombo.getSelectionIndex() == -1 || programmerCombo.getSelectionIndex() == -1){
			return false;
		}
		((DeepProjectWizard)getWizard()).model.setBoard(boards[boardCombo.getSelectionIndex()]);
		((DeepProjectWizard)getWizard()).model.setOs(operatingsystems[rtsCombo.getSelectionIndex()]);
		((DeepProjectWizard)getWizard()).model.setProgrammer(programmers[programmerCombo.getSelectionIndex()]);
		return true;
	}
	
	@Override
	public void setVisible(boolean visible) {
		if(visible) {
			insertBoards();
			insertOperatingSystems();
			insertProgrammers();
		}
        getControl().setVisible(visible);
    }
}
