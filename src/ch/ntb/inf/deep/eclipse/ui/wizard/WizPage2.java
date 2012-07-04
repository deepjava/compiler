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

import ch.ntb.inf.deep.config.Board;
import ch.ntb.inf.deep.config.Library;
import ch.ntb.inf.deep.eclipse.DeepPlugin;
import ch.ntb.inf.deep.eclipse.ui.preferences.PreferenceConstants;

class WizPage2 extends WizardPage {
	
	private Combo boardCombo, rtsCombo;
	private Button check;
	private Text path;
	private final String defaultPath = DeepPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.DEFAULT_LIBRARY_PATH);
	private String lastChoise = "";
	private Library lib;
	private Board[] boards;
	
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

	protected WizPage2(String pageName, Library lib) {
		super(pageName);
		this.lib = lib;
		setPageComplete(true);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, true);
		composite.setLayout(gridLayout);

		Group groupBoard = new Group(composite, SWT.NONE);
		groupBoard.setText("Board configuration");
		RowLayout rowLayout3 = new RowLayout(SWT.VERTICAL);
		groupBoard.setLayout(rowLayout3);
		Label label1 = new Label(groupBoard, SWT.NONE);
		label1.setText("Select a board");
		boardCombo = new Combo(groupBoard, SWT.BORDER);
//		board.setItems(new String[]{"NTB MPC555 Headerboard", "Phytec phyCORE-MPC555", "Phytec phyCORE-MPC5200/tiny", "Phytec phyCORE-MPC5200/IO"});
//		board.setItems(boardDescs);
//		board.select(0);
		boardCombo.addSelectionListener(selectionListener);
		Group groupOS = new Group(composite, SWT.NONE);
		groupOS.setText("Runtime system");
		GridLayout gridLayout2 = new GridLayout(2, false);
		groupOS.setLayout(gridLayout2);
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 2;
		groupOS.setLayoutData(gridData);
		Label label2 = new Label(groupOS,SWT.NONE);
		label2.setText("Select a operating system");
		label2.setLayoutData(gridData);
		rtsCombo = new Combo(groupOS, SWT.BORDER);
		rtsCombo.setItems(new String[]{"NTB Simple tasking system", "Java uCos"});
		rtsCombo.select(0);
		rtsCombo.addSelectionListener(selectionListener);
		rtsCombo.setLayoutData(gridData);
		setControl(composite);
	}
	
	public void insertBoards() {
		this.boards = lib.getBoards();
		String[] boardDescs = new String[boards.length];
		for(int i = 0; i < boardDescs.length; i++) {
			boardDescs[i] = boards[i].getDescription().toString();
		}
		boardCombo.setItems(boardDescs);
		boardCombo.select(0);
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
		if(boardCombo.getSelectionIndex() == -1 || rtsCombo.getSelectionIndex() == -1){
			return false;
		}
		return true;
	}

}
