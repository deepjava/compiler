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

package ch.ntb.inf.deep.eclipse.ui.wizard;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Parser;
import ch.ntb.inf.deep.eclipse.DeepPlugin;
import ch.ntb.inf.deep.eclipse.ui.preferences.PreferenceConstants;

class TargetConfigPage extends WizardPage {
		
	private final String defaultBoard = DeepPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.DEFAULT_BOARD);
	private final String defaultOs = DeepPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.DEFAULT_OS);
	private final String defaultProgrammer = DeepPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.DEFAULT_PROGRAMMER);
	private final String defaultImgFormat = PreferenceConstants.DEFAULT_IMG_FORMAT;	 
	private Combo boardCombo, programmerCombo, osCombo, imgFormatCombo;
	private Button check, browse;
	private Text path;
	private final String defaultImgPath = "$PROJECT_LOCATION";
	private String lastChoice = defaultImgPath;
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
		osLabel.setText("Select an operating system");
		osCombo = new Combo(groupOS, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		setControl(composite);
		
		Group groupImg = new Group(composite, SWT.NONE);
		groupImg.setText("Image file creation");
		GridLayout gridLayout = new GridLayout(2,false);
		groupImg.setLayout(gridLayout);
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 2;		
		check = new Button(groupImg, SWT.CHECK);
		check.setText("Create image file");
		check.setSelection(false);
		check.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(e.widget.equals(check)) {
					if(check.getSelection()) {
						path.setEnabled(true);
						path.setText(defaultImgPath);
						browse.setEnabled(true);
						imgFormatCombo.setEnabled(true);
					} else {
						path.setEnabled(false);
						path.setText(lastChoice);
						browse.setEnabled(false);
						imgFormatCombo.setEnabled(false);
					}
				}
				setPageComplete(validatePage());
			}
		});
		check.setLayoutData(gridData);
		
		GridData gridData2 = new GridData(SWT.FILL, SWT.FILL, true, false);
		path = new Text(groupImg, SWT.SINGLE | SWT.BORDER);
		path.setLayoutData(gridData2);
		path.setText(defaultImgPath);
		path.setEnabled(false);
		path.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage());
			}
		});
		browse = new Button(groupImg, SWT.PUSH);
		browse.setText("Browse...");
		browse.setEnabled(false);
		browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				if (check.getSelection()) {	
					openDirectoryDialog();
					setPageComplete(validatePage());
				}
			}
		});	
		GridData gridData3 = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData3.horizontalSpan = 2;
		Label imgFormatLabel = new Label(groupImg,SWT.NONE);
		imgFormatLabel.setText("Select image file format");
		imgFormatCombo = new Combo(groupImg, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		imgFormatCombo.setEnabled(false);
		imgFormatCombo.setLayoutData(gridData3);
		setControl(composite);
	}
		
	private void openDirectoryDialog() {
		DirectoryDialog dlg = new DirectoryDialog(getShell());        
        dlg.setFilterPath(path.getText()); // Set the initial filter path according to anything they've selected or typed in
        dlg.setText("Image file output location");
        dlg.setMessage("Select a directory");
        String dir = dlg.open(); // Calling open() will open and run the dialog.
        if (dir != null) {
        	path.setText(dir);
        	lastChoice = dir;
        }
	}
	
	private void insertBoards() {
		if (((DeepProjectWizard)getWizard()).model.getLibrary() != null) {
			File lib = ((DeepProjectWizard)getWizard()).model.getLibrary();
			boards = Configuration.searchDescInConfig(new File(lib.toString() + Configuration.boardsPath), Parser.sBoard);
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
			operatingSystems = Configuration.searchDescInConfig(new File(lib.toString() + Configuration.osPath), Parser.sOperatingSystem);
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
			programmers = Configuration.searchDescInConfig(new File(lib.toString() + Configuration.progPath), Parser.sProgrammer);
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
	
	private void insertImgFormats(){
		String[] str = new String[Configuration.formatMnemonics.length];
		int index = str.length - 1;
		for (int i = 0; i < Configuration.formatMnemonics.length; i++) {
			str[i] = Configuration.formatMnemonics[i];
			if (str[i].equalsIgnoreCase(defaultImgFormat)) index = i;
		}
		imgFormatCombo.setItems(str);
		imgFormatCombo.select(index);
	}
	
	private boolean validatePage() {
		if (boardCombo.getSelectionIndex() == -1 || osCombo.getSelectionIndex() == -1 || programmerCombo.getSelectionIndex() == -1){
			return false;
		}
		if (boardCombo.getSelectionIndex() != boardCombo.getItemCount() - 1) ((DeepProjectWizard)getWizard()).model.setBoard(boards[boardCombo.getSelectionIndex()]);
		else ((DeepProjectWizard)getWizard()).model.setBoard(null);
		if (osCombo.getSelectionIndex() != osCombo.getItemCount() - 1) ((DeepProjectWizard)getWizard()).model.setOs(operatingSystems[osCombo.getSelectionIndex()]);
		else ((DeepProjectWizard)getWizard()).model.setOs(null);
		if (programmerCombo.getSelectionIndex() != programmerCombo.getItemCount() - 1) ((DeepProjectWizard)getWizard()).model.setProgrammer(programmers[programmerCombo.getSelectionIndex()]);
		else ((DeepProjectWizard)getWizard()).model.setProgrammer(null);
		((DeepProjectWizard)getWizard()).model.setCreateImgFile(check.getSelection());
		((DeepProjectWizard)getWizard()).model.setImgFormat(Configuration.formatMnemonics[imgFormatCombo.getSelectionIndex()]);
		if (lastChoice != defaultImgPath) ((DeepProjectWizard)getWizard()).model.setImgPath(new File(lastChoice));
		else ((DeepProjectWizard)getWizard()).model.setImgPath(null);
		return true;
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			insertBoards();
			insertOperatingSystems();
			insertProgrammers();
			insertImgFormats();
		}
        getControl().setVisible(visible);
        validatePage();	// must happen to store settings to model in case that no combo is changed
    }
}
