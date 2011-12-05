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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.ntb.inf.deep.eclipse.DeepPlugin;
import ch.ntb.inf.deep.eclipse.ui.preferences.PreferenceConstants;

class WizPage1 extends WizardPage {
	
	private Combo processor, board, rts;
	private Button check, browse;
	private Text path;
	private final String defaultPath = DeepPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.DEFAULT_LIBRARY_PATH);
	private String lastChoise = "";
	
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

	protected WizPage1(String pageName) {
		super(pageName);
		setPageComplete(true);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		composite.setLayout(gridLayout);

		Group group1 = new Group(composite, SWT.NONE);
		group1.setText("Processor");
		RowLayout rowLayout2 = new RowLayout(SWT.VERTICAL);
		group1.setLayout(rowLayout2);
		Label label = new Label(group1, SWT.NONE);
		label.setText("Select a processor");
		processor = new Combo(group1, SWT.BORDER);
		processor.setItems(new String[]{"MPC555"});
		processor.select(0);
		processor.addSelectionListener(selectionListener);
		
		Group group2 = new Group(composite, SWT.NONE);
		group2.setText("Board configuration");
		RowLayout rowLayout3 = new RowLayout(SWT.VERTICAL);
		group2.setLayout(rowLayout3);
		Label label1 = new Label(group2, SWT.NONE);
		label1.setText("Select a configuration");
		board = new Combo(group2, SWT.BORDER);
		board.setItems(new String[]{"NTB MPC555 Headerboard", "phyCORE-mpc555"});
		board.select(0);
		board.addSelectionListener(selectionListener);
		
		
		Group group3 = new Group(composite, SWT.NONE);
		group3.setText("Runtime system");
		GridLayout gridLayout2 = new GridLayout(2, false);
		group3.setLayout(gridLayout2);
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 2;
		group3.setLayoutData(gridData);
		Label label2 = new Label(group3,SWT.NONE);
		label2.setText("Select a runtime system");
		label2.setLayoutData(gridData);
		rts = new Combo(group3, SWT.BORDER);
		rts.setItems(new String[]{"Simple tasking system", "uCos"});
		rts.select(0);
		rts.addSelectionListener(selectionListener);
		rts.setLayoutData(gridData);
		Label dummy = new Label(group3, SWT.NONE);
		dummy.setLayoutData(gridData);
		check = new Button(group3, SWT.CHECK);
		check.setText("use default library path");
		check.setSelection(true);
		check.addSelectionListener(selectionListener);
		check.setLayoutData(gridData);
		path = new Text(group3, SWT.SINGLE | SWT.BORDER);
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = SWT.FILL;
		gridData2.grabExcessHorizontalSpace = true;
		path.setLayoutData(gridData2);
		path.setText(defaultPath);
		path.setEnabled(false);
		browse = new Button(group3, SWT.PUSH);
		browse.setText("Browse...");
		browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
			if(!check.getSelection()){	
				openDirectoryDialog();
			}
		}});
	
		setControl(composite);

	}
	public String getProcessorValue(){
		return processor.getItem(processor.getSelectionIndex());
	}
	public String getBoardValue(){
		return board.getItem(board.getSelectionIndex());
	}
	public String getRunTimeSystemValue(){
		return rts.getItem(rts.getSelectionIndex());
	}
	
	public boolean useDefaultLibPath(){
		return check.getSelection();
	}
	
	public String getDefaultLibPath(){
		return defaultPath;
	}
	
	public String getChosenLibPath(){
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
		if(processor.getSelectionIndex() == -1 || board.getSelectionIndex() == -1 || rts.getSelectionIndex() == -1){
			return false;
		}
		return true;
	}

}
