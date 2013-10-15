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

package ch.ntb.inf.deep.eclipse.ui.properties;

import java.io.File;
import java.util.GregorianCalendar;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Parser;
import ch.ntb.inf.deep.eclipse.DeepPlugin;
import ch.ntb.inf.deep.eclipse.ui.preferences.PreferenceConstants;

public class DeepProjectPage extends PropertyPage implements IWorkbenchPropertyPage {
	
	private final String defaultPath = DeepPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.DEFAULT_LIBRARY_PATH);
	private Combo boardCombo, programmerCombo, osCombo;
	private Button check, browse;
	private Text path;
	private String lastChoice = "";
	private IEclipsePreferences pref;
	private Label libState;
	private String libPath, board, programmer, os, rootclasses;
	String[][] boards, programmers, osys;
	private DeepFileChanger dfc;
	
	@Override
	protected Control createContents(Composite parent) {
		// read deep project file
		IProject project = (IProject) getElement().getAdapter(IProject.class);
		dfc = new DeepFileChanger(project.getLocation()	+ "/" + project.getName() + ".deep");

		libPath = dfc.getContent("libpath");
		if (!libPath.equals("not available")) libPath = libPath.substring(1, libPath.length()-1);
		board = dfc.getContent("boardtype");
		programmer = dfc.getContent("programmertype");
		os = dfc.getContent("ostype");
		rootclasses = dfc.getContent("rootclasses");
//		System.out.println(dfc.fileContent.toString());		
//		System.out.println(libPath);
//		System.out.println(board);
//		System.out.println(programmer);
//		System.out.println(os);
//		System.out.println(rootclasses);

		// read project preferences
		pref = getPref();
		
		// build control
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
	
		Group groupLib = new Group(composite, SWT.NONE);
		groupLib.setText("Target Library");
		GridLayout gridLayout2 = new GridLayout(2, false);
		groupLib.setLayout(gridLayout2);
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 2;
		groupLib.setLayoutData(gridData);
		Label label1 = new Label(groupLib,SWT.NONE);
		label1.setText("Pleace specify the target library you want to use for this project.");
		label1.setLayoutData(gridData);
		Label dummy = new Label(groupLib, SWT.NONE);
		dummy.setLayoutData(gridData);
		check = new Button(groupLib, SWT.CHECK);
		check.setText("Use default library path");
		check.setSelection(libPath.equals(defaultPath));
		check.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (e.widget.equals(check)) {
					if (check.getSelection()) {
						path.setEnabled(false);
						path.setText(defaultPath);
					}
					else {
						path.setEnabled(true);
						path.setText(lastChoice);
					}
					libPath = path.getText();
					if (checkLibPath()) readLib();
				}
			}
		});
		check.setLayoutData(gridData);
		path = new Text(groupLib, SWT.SINGLE | SWT.BORDER);
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = SWT.FILL;
		gridData2.grabExcessHorizontalSpace = true;
		path.setLayoutData(gridData2);
		path.setText(libPath);
		path.setEnabled(false);
		path.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				libPath = path.getText();
				if (checkLibPath()) readLib();
			}
		});
		browse = new Button(groupLib, SWT.PUSH);
		browse.setText("Browse...");
		browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!check.getSelection()){	
					openDirectoryDialog();
					if (checkLibPath()) readLib();
				}
			}
		});
		libState = new Label(groupLib,SWT.NONE);
		libState.setLayoutData(gridData);

		Group groupBoard = new Group(composite, SWT.BOTTOM);
		groupBoard.setText("Board configuration");
		GridLayout groupLayout1 = new GridLayout(2, false);
		groupBoard.setLayout(groupLayout1);
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = SWT.FILL;
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.horizontalSpan = 2;
		groupBoard.setLayoutData(gridData3);
		Label boardLabel = new Label(groupBoard, SWT.NONE);
		boardLabel.setText("Select a board");
		boardCombo = new Combo(groupBoard, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		boardCombo.addSelectionListener(listener);
		Label progLabel = new Label(groupBoard, SWT.NONE);
		progLabel.setText("Select a programmer");
		programmerCombo = new Combo(groupBoard, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		programmerCombo.addSelectionListener(listener);

		Group groupOS = new Group(composite, SWT.BOTTOM);
		groupOS.setText("Runtime system");
		GridLayout groupLayout2 = new GridLayout(2, false);
		groupOS.setLayout(groupLayout2);
		Label osLabel = new Label(groupOS,SWT.NONE);
		osLabel.setText("Select a operating system");
		osCombo = new Combo(groupOS, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		osCombo.addSelectionListener(listener);

		if (checkLibPath()) readLib(); else libPath = "not available";
		return composite;
	}
	
	private SelectionAdapter listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e){
			if (e.widget.equals(boardCombo)) {
				if (boardCombo.getSelectionIndex() != boardCombo.getItemCount() - 1) board = boards[boardCombo.getSelectionIndex()][0];
				else board = "";
			}
			if (e.widget.equals(osCombo)) {
				if (osCombo.getSelectionIndex() != osCombo.getItemCount() - 1) os = osys[osCombo.getSelectionIndex()][0];
				else os = "";
			}
			if (e.widget.equals(programmerCombo)) {
				if (programmerCombo.getSelectionIndex() != programmerCombo.getItemCount() - 1) programmer = programmers[programmerCombo.getSelectionIndex()][0];
				else programmer = "";
			}
		}
	};

	private void readLib() {
		boards = Configuration.searchDescInConfig(new File(libPath + Configuration.boardsPath), Parser.sBoard);
		String[] str = new String[boards.length + 1];
		int index = boards.length;
		for (int i = 0; i < boards.length; i++) {
			str[i] = boards[i][1];
			//				if (pref.get("board", "").equals(boards[i][0])) index = i;
			if (board.equals(boards[i][0])) index = i;
		}
		str[str.length - 1] = "none";
		boardCombo.setItems(str);
		boardCombo.select(index);

		programmers = Configuration.searchDescInConfig(new File(libPath.toString() + Configuration.progPath), Parser.sProgrammer);
		str = new String[programmers.length + 1];
		index = programmers.length;
		for (int i = 0; i < programmers.length; i++) {
			str[i] = programmers[i][1];
			//				if (pref.get("programmer", "").equals(programmers[i][0])) index = i;
			if (programmer.equals(programmers[i][0])) index = i;
		}
		str[str.length - 1] = "none";
		programmerCombo.setItems(str);
		programmerCombo.select(index);

		osys = Configuration.searchDescInConfig(new File(libPath.toString() + Configuration.osPath), Parser.sOperatingSystem);
		str = new String[osys.length + 1];
		index = osys.length;
		for (int i = 0; i < osys.length; i++) {
			str[i] = osys[i][1];
			//				if (pref.get("os", "").equals(os[i][0])) index = i;
			if (os.equals(osys[i][0])) index = i;
		}
		str[str.length - 1] = "none";
		osCombo.setItems(str);
		osCombo.select(index);
	}

	private void openDirectoryDialog() {
		DirectoryDialog dlg = new DirectoryDialog(getShell());        
        dlg.setFilterPath(path.getText()); // Set the initial filter path according to anything they've selected or typed in
        dlg.setText("deep Library Path Selection");
        dlg.setMessage("Select a directory");
        String dir = dlg.open(); // Calling open() will open and run the dialog.
        if (dir != null) {
        	path.setText(dir);
        	libPath = dir;
        	lastChoice = dir;
        }
	}

	private boolean checkLibPath() {
		File lib = new File(libPath);
		if (!lib.exists()) {
			libState.setText("Given library path is NOT valid target library.");
			return false;		
		}
		String[][] boards = Configuration.searchDescInConfig(new File(lib.toString() + Configuration.boardsPath), Parser.sBoard);
		if (boards == null || boards[0][0].equals("not available")) {
			libState.setText("Given library path is NOT valid target library.");
			return false;			
		}
		libState.setText("");
		return true;
	}

	private IEclipsePreferences getPref() {
		IProject project =  (IProject) getElement().getAdapter(IProject.class);
		ProjectScope scope = new ProjectScope(project);
		return scope.getNode("deepStart");
	}

	protected void performApply() {
		saveProjectPreferences();
		saveFiles();
		super.performApply();
	}

	public boolean performOk() {
		saveProjectPreferences();
		saveFiles();
		return true;
	}
	
	public boolean performCancel() {
		return true;
	}

	private void saveProjectPreferences() {
		pref.put("board", boardCombo.getText());
		pref.put("programmer", programmerCombo.getText());
		pref.put("os", osCombo.getText());
		if (check.getSelection()) {
			pref.putBoolean("useDefault", true);
			pref.put("libPath", defaultPath);
		} else {
			pref.putBoolean("useDefault", false);
			pref.put("libPath", path.getText());
		}
		try {
			pref.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	private void saveFiles() {
		// change deep file
		IProject project = (IProject) getElement().getAdapter(IProject.class);
		GregorianCalendar cal = new GregorianCalendar();
		dfc.changeContent("version", "\"" + cal.getTime().toString() + "\"");
		dfc.changeContent("libpath", "\"" + libPath + "\"");
		dfc.changeContent("boardtype", board);
		dfc.changeContent("ostype", os);
		dfc.changeContent("programmertype", programmer);
		dfc.changeContent("rootclasses", rootclasses);
		dfc.save();

		// change classpath file
		DeepFileChanger cfc = new DeepFileChanger(project.getLocation() + "/.classpath");
		cfc.changeLibPath(libPath);
		cfc.save();

		lastChoice = path.getText();
		
		try { // refresh the package explorer
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
}


