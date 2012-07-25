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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
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

import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Library;

class LibPathPage extends WizardPage {
		
	private Button check, browse;
	private Label libState;
	private Text path;
	//private final String defaultPath = DeepPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.DEFAULT_LIBRARY_PATH);
	private final String defaultPath = "D:\\work\\Project_deep\\trunk\\RuntimeSystem";
	private String lastChoise = defaultPath;
	
	private SelectionAdapter selectionListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			if(e.widget.equals(check)) {
				if(check.getSelection()) {
					path.setEnabled(false);
					path.setText(defaultPath);
				}
				else {
					path.setEnabled(true);
					path.setText(lastChoise);
				}
			}
			setPageComplete(validatePage());
		}
	};

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
		check.setText("use default library path");
		check.setSelection(true);
		check.addSelectionListener(selectionListener);
		check.setLayoutData(gridData);
		path = new Text(group, SWT.SINGLE | SWT.BORDER);
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = SWT.FILL;
		gridData2.grabExcessHorizontalSpace = true;
		path.setLayoutData(gridData2);
		path.setText(defaultPath);
		path.setEnabled(false);
		browse = new Button(group, SWT.PUSH);
		browse.setText("Browse...");
		browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				if(!check.getSelection()){	
					openDirectoryDialog();
				}
			}
		});
		libState = new Label(group,SWT.NONE);
		libState.setLayoutData(gridData);
		setControl(composite);
		setPageComplete(validatePage());
	}
		
	private String getChosenLibPath(){
		return path.getText();
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
        dlg.setText("deep Library Path Selection");

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
        	setPageComplete(validatePage());
        }
	}
	
	private boolean validatePage() {
		Library lib = Configuration.addLibrary(getChosenLibPath());
		if(lib != null && lib.getNofBoards() > 0 && lib.getNofOperatingSystems() > 0){
			((DeepProjectWizard)getWizard()).model.setLibrary(lib);
			libState.setText("Given library path is valid.");
			return true;
		}
		else {
			libState.setText("Given library path is NOT valid target library.");
		}
		return false;
	}

	public IWizardPage getNextPage() {
        if(getWizard() == null) {
			return null;
		}
        TargetConfigPage nextPage = (TargetConfigPage)getWizard().getNextPage(this);
        //nextPage.onEnterPage();
        return nextPage;
    }
	
}
