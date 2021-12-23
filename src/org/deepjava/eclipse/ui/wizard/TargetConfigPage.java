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

package org.deepjava.eclipse.ui.wizard;

import java.io.File;

import org.deepjava.config.Configuration;
import org.deepjava.config.Parser;
import org.deepjava.eclipse.DeepPlugin;
import org.deepjava.eclipse.ui.preferences.PreferenceConstants;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

class TargetConfigPage extends WizardPage {
		
	private final String defaultBoard = DeepPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.DEFAULT_BOARD);
	private final String defaultOs = DeepPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.DEFAULT_OS);
	private final String defaultProgrammer = DeepPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.DEFAULT_PROGRAMMER);
	private final String defaultProgrammerOptions = DeepPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.DEFAULT_PROGRAMMER_OPTIONS);
	private Combo boardCombo, programmerCombo, osCombo;
	private Button checkImg, browseImg, downloadPL, browsePL;
	private Text programmerOpts, pathImg, pathPL;
	private final String defaultImgPath = "$PROJECT_LOCATION";
	private final String defaultPlPath = "$PROJECT_LOCATION";
	private String lastChoice = defaultImgPath;
	private String lastChoicePl = defaultPlPath;
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
		composite.setLayout(new GridLayout(1, true));
		composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		
		Group groupBoard = new Group(composite, SWT.NONE);
		groupBoard.setText("Board configuration");
		groupBoard.setLayout(new GridLayout(1, false));
		groupBoard.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Label boardLabel = new Label(groupBoard, SWT.NONE);
		boardLabel.setText("Select a board");
		boardCombo = new Combo(groupBoard, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		boardCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Label progLabel = new Label(groupBoard, SWT.NONE);
		progLabel.setText("Select a programmer");
		programmerCombo = new Combo(groupBoard, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		programmerCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Label progOptLabel = new Label(groupBoard, SWT.NONE);
		progOptLabel.setText("Select programmer options");
		programmerOpts = new Text(groupBoard, SWT.SINGLE | SWT.BORDER);
		programmerOpts.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		programmerOpts.setText(defaultProgrammerOptions);
		programmerOpts.setEnabled(true);
		programmerOpts.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage());
			}
		});
		
		Group groupOS = new Group(composite, SWT.NONE);
		groupOS.setText("Runtime system");
		groupOS.setLayout(new GridLayout(1, false));
		groupOS.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Label osLabel = new Label(groupOS,SWT.NONE);
		osLabel.setText("Select an operating system");
		osCombo = new Combo(groupOS, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		osCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Group groupImg = new Group(composite, SWT.NONE);
		groupImg.setText("Image file creation");
		groupImg.setLayout(new GridLayout(2, false));
		groupImg.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		checkImg = new Button(groupImg, SWT.CHECK);
		checkImg.setText("Create image file");
		checkImg.setSelection(false);
		checkImg.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(e.widget.equals(checkImg)) {
					if (checkImg.getSelection()) {
						pathImg.setEnabled(true);
						pathImg.setText(defaultImgPath);
						browseImg.setEnabled(true);
					} else {
						pathImg.setEnabled(false);
						pathImg.setText(lastChoice);
						browseImg.setEnabled(false);
					}
				}
				setPageComplete(validatePage());
			}
		});
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 2;		
		checkImg.setLayoutData(gridData);
		
		pathImg = new Text(groupImg, SWT.SINGLE | SWT.BORDER);
		pathImg.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		pathImg.setText(defaultImgPath);
		pathImg.setEnabled(false);
		pathImg.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage());
			}
		});
		browseImg = new Button(groupImg, SWT.PUSH);
		browseImg.setText("Browse...");
		browseImg.setEnabled(false);
		browseImg.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				if (checkImg.getSelection()) {	
					DirectoryDialog dlg = new DirectoryDialog(getShell());        
			        dlg.setFilterPath(pathImg.getText()); // Set the initial filter path according to anything they've selected or typed in
			        dlg.setText("Image file output location");
			        dlg.setMessage("Select a directory");
			        String dir = dlg.open(); // Calling open() will open and run the dialog.
			        if (dir != null) {
			        	pathImg.setText(dir);
			        	lastChoice = dir;
			        }
					setPageComplete(validatePage());
				}
			}
		});	
		Group groupPL = new Group(composite, SWT.NONE);
		groupPL.setText("Configuration file for PL");
		groupPL.setLayout(new GridLayout(2, false));
		groupPL.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		downloadPL = new Button(groupPL, SWT.CHECK);
		downloadPL.setText("Download PL file");
		downloadPL.setSelection(false);
		downloadPL.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(e.widget.equals(downloadPL)) {
					if(downloadPL.getSelection()) {
						pathPL.setEnabled(true);
						pathPL.setText(defaultPlPath);
						browsePL.setEnabled(true);
					} else {
						pathPL.setEnabled(false);
						pathPL.setText(lastChoicePl);
						browsePL.setEnabled(false);
					}
				}
				setPageComplete(validatePage());
			}
		});
		downloadPL.setLayoutData(gridData);
		pathPL = new Text(groupPL, SWT.SINGLE | SWT.BORDER);
		pathPL.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		pathPL.setText(defaultPlPath);
		pathPL.setEnabled(false);
		pathPL.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage());
			}
		});
		browsePL = new Button(groupPL, SWT.PUSH);
		browsePL.setText("Browse...");
		browsePL.setEnabled(false);
		browsePL.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				if (downloadPL.getSelection()) {	
					FileDialog dlg = new FileDialog(getShell());        
			        dlg.setFilterPath(pathImg.getText()); // Set the initial filter path according to anything they've selected or typed in
			        dlg.setText("Choose a bit-file for the PL");
			        String dir = dlg.open(); // Calling open() will open and run the dialog.
			        if (dir != null) {
			        	pathPL.setText(dir);
			        	lastChoicePl = dir;
			        }
					setPageComplete(validatePage());
				}
			}
		});	

		setControl(composite);
	}
		
	private void insertBoards() {
		if (((DeepProjectWizard)getWizard()).model.getLibrary() != null) {
			File lib = ((DeepProjectWizard)getWizard()).model.getLibrary();
			boards = Configuration.getDescInConfigDir(new File(lib.toString() + Configuration.boardsPath), Parser.sBoard);
			String[] str = new String[boards.length + 1];
			int index = str.length - 1;
			for (int i = 0; i < boards.length; i++) {
				str[i] = boards[i][1];
				if (boards[i][0].contains(defaultBoard)) index = i;
			}
			str[str.length - 1] = "none";
			boardCombo.setItems(str);
			boardCombo.select(index);
		}
	}
	
	private void insertOperatingSystems() {
		if (((DeepProjectWizard)getWizard()).model.getLibrary() != null) {
			File lib = ((DeepProjectWizard)getWizard()).model.getLibrary();
			operatingSystems = Configuration.getDescInConfigDir(new File(lib.toString() + Configuration.osPath), Parser.sOperatingSystem);
			String[] str = new String[operatingSystems.length + 1];
			int index = str.length - 1;
			for (int i = 0; i < operatingSystems.length; i++) {
				str[i] = operatingSystems[i][1];
				if (operatingSystems[i][0].contains(defaultOs)) index = i;
			}
			str[str.length - 1] = "none";
			osCombo.setItems(str);
			osCombo.select(index);
		}
	}
	
	private void insertProgrammers() {
		if (((DeepProjectWizard)getWizard()).model.getLibrary() != null) {
			File lib = ((DeepProjectWizard)getWizard()).model.getLibrary();
			programmers = Configuration.getDescInConfigDir(new File(lib.toString() + Configuration.progPath), Parser.sProgrammer);
			String[] str = new String[programmers.length + 1];
			int index = str.length - 1;
			for (int i = 0; i < programmers.length; i++) {
				str[i] = programmers[i][1];
				if (programmers[i][0].contains(defaultProgrammer)) index = i;
			}
			str[str.length - 1] = "none";
			programmerCombo.setItems(str);
			programmerCombo.select(index);
		}
	}
	
	private boolean validatePage() {
		if (boardCombo.getSelectionIndex() == -1 || osCombo.getSelectionIndex() == -1 || programmerCombo.getSelectionIndex() == -1) {
			return false;
		}
		DeepProjectWizard wiz = (DeepProjectWizard) getWizard();
		if (boardCombo.getSelectionIndex() != boardCombo.getItemCount() - 1) wiz.model.setBoard(boards[boardCombo.getSelectionIndex()]);
		else wiz.model.setBoard(null);
		if (osCombo.getSelectionIndex() != osCombo.getItemCount() - 1) wiz.model.setOs(operatingSystems[osCombo.getSelectionIndex()]);
		else wiz.model.setOs(null);
		if (programmerCombo.getSelectionIndex() != programmerCombo.getItemCount() - 1) wiz.model.setProgrammer(programmers[programmerCombo.getSelectionIndex()]);
		else wiz.model.setProgrammer(null);
		if (programmerOpts.getText() != null && programmerOpts.getText().length() > 0) wiz.model.setProgrammerOptions(programmerOpts.getText()); 
		else wiz.model.setProgrammerOptions(null);
		wiz.model.setCreateImgFile(checkImg.getSelection());
		if (lastChoice != defaultImgPath) wiz.model.setImgPath(new File(lastChoice));
		else wiz.model.setImgPath(null);
		wiz.model.setLoadPlFile(downloadPL.getSelection());
		if (lastChoicePl != defaultPlPath) wiz.model.setPlFilePath(new File(lastChoicePl));
		else wiz.model.setPlFilePath(null);
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
