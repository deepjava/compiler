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
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

class LibPathPage extends WizardPage {
		
	private Button check, browse;
	private Label libState;
	private Text path;
	private final String defaultPath = DeepPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.DEFAULT_LIBRARY_PATH);
	private String lastChoice = defaultPath;
	
	protected LibPathPage(String pageName) {
		super(pageName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		composite.setLayout(gridLayout);	
		Group group = new Group(composite, SWT.NONE);
		group.setText("Target Library");
		GridLayout gridLayout2 = new GridLayout(2, false);
		group.setLayout(gridLayout2);
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 2;
		group.setLayoutData(gridData);
		Label label2 = new Label(group,SWT.NONE);
		label2.setText("Pleace specify the target library you want to use for this project.");
		label2.setLayoutData(gridData);
		Label dummy = new Label(group, SWT.NONE);
		dummy.setLayoutData(gridData);
		check = new Button(group, SWT.CHECK);
		check.setText("Use default library path");
		check.setSelection(true);
		check.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(e.widget.equals(check)) {
					if(check.getSelection()) {
						path.setEnabled(false);
						path.setText(defaultPath);
					} else {
						path.setEnabled(true);
						path.setText(lastChoice);
					}
				}
				setPageComplete(validatePage());
			}
		});
		check.setLayoutData(gridData);
		path = new Text(group, SWT.SINGLE | SWT.BORDER);
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = SWT.FILL;
		gridData2.grabExcessHorizontalSpace = true;
		path.setLayoutData(gridData2);
		path.setText(defaultPath);
		path.setEnabled(false);
		path.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage());
			}
		});
		browse = new Button(group, SWT.PUSH);
		browse.setText("Browse...");
		browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				if (!check.getSelection()) {	
					openDirectoryDialog();
					setPageComplete(validatePage());
				}
			}
		});
		libState = new Label(group,SWT.NONE);
		libState.setLayoutData(gridData);
		libState.setText("");
		setControl(composite);
		setPageComplete(validatePage());
	}
		
	private void openDirectoryDialog() {
		DirectoryDialog dlg = new DirectoryDialog(getShell());        
        dlg.setFilterPath(path.getText()); // Set the initial filter path according to anything they've selected or typed in
        dlg.setText("deep Library Path Selection");
        dlg.setMessage("Select a directory");
        String dir = dlg.open(); // Calling open() will open and run the dialog.
        if (dir != null) {
        	path.setText(dir);
        	lastChoice = dir;
        }
	}
	
	private boolean validatePage() {
		File lib = new File(path.getText());
		if (!lib.exists()) {
			libState.setText("Given library path is NOT valid target library.");
			return false;		
		}
		((DeepProjectWizard)getWizard()).model.setLibrary(lib);
		String[][] boards = Configuration.getDescInConfigDir(new File(lib.toString() + Configuration.boardsPath), Parser.sBoard);
		if (boards == null || boards[0][0].equals("not available")) {
			libState.setText("Given library path is NOT valid target library.");
			return false;			
		}
		libState.setText("");
		return true;
	}

	public IWizardPage getNextPage() {
        if (getWizard() == null) {
			return null;
		}
        TargetConfigPage nextPage = (TargetConfigPage)getWizard().getNextPage(this);
        //nextPage.onEnterPage();
        return nextPage;
    }
	
}
