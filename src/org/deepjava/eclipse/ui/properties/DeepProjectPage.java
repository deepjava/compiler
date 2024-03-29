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

package org.deepjava.eclipse.ui.properties;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;

import org.deepjava.config.Configuration;
import org.deepjava.config.Parser;
import org.deepjava.eclipse.DeepPlugin;
import org.deepjava.eclipse.ui.preferences.PreferenceConstants;
import org.deepjava.host.ErrorReporter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class DeepProjectPage extends PropertyPage implements IWorkbenchPropertyPage {
	
	private final String defaultPath = DeepPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.DEFAULT_LIBRARY_PATH);
	private Combo boardCombo, programmerCombo, osCombo;
	private Button checkLib, browseLib, checkImg, browseImg, downloadPL, browsePL;
	private Text pathLib, programmerOpts, pathImg, pathPL;
	private final String defaultImgPath = "./";
	private final String defaultPlPath = "./";
	private String lastChoiceImg = defaultImgPath;
	private String lastChoicePl = defaultPlPath;
	private boolean createImgFile = false, loadPL = false;
	private Label libState;
	private String libPath, absLibPath, board, programmer, programmerOpt, os, imgLocation, plfile;
	String[][] boards;
	private String[][] operatingSystems;
	private String[][] programmers;
	private DeepFileChanger dfc;
	
	@Override
	protected Control createContents(Composite parent) {
		// read deep project file
		IProject project = (IProject) getElement().getAdapter(IProject.class);
		try {
			dfc = new DeepFileChanger(project.getLocation()	+ "/" + project.getName() + ".deep");
		} catch (Exception e) {
			ErrorDialog.openError(getShell(), "Error", "Project name must be identical to project directory name", makeStatus(e));
			return null;
		}

		libPath = dfc.getContent("libpath", false);
		libPath = libPath.replace("\"", "");
		if (!(new File(libPath).isAbsolute())) {
			String s = project.getLocation().toString() + IPath.SEPARATOR + libPath;
			try {
				absLibPath = new File(s).getCanonicalPath();
			} catch (IOException e) {
				ErrorReporter.reporter.error(255, "path=" + libPath);
				return parent;
			}
		} else absLibPath = libPath;
		board = dfc.getContent("boardtype", false);
		os = dfc.getContent("ostype", false);
		programmer = dfc.getContent("programmertype", false);
		programmerOpt = dfc.getContent("programmeropts", true);
		if (programmerOpt.startsWith("#")) programmerOpt = programmerOpt.substring(1);
		imgLocation = dfc.getContent("imgfile", true);
		if (imgLocation.equalsIgnoreCase("none")) {
			createImgFile = false;
		} else if (imgLocation.startsWith("#")) {
			lastChoiceImg = imgLocation.replace('\\', '/');
			lastChoiceImg = lastChoiceImg.replace("\"", "");
			lastChoiceImg = lastChoiceImg.substring(1);
		} else {
			createImgFile = true;
			lastChoiceImg = imgLocation.replace('\\', '/');
			lastChoiceImg = lastChoiceImg.replace("\"", "");
		}
		plfile = dfc.getContent("pl_file", true);
		if (plfile.equalsIgnoreCase("none")) loadPL = false;
		else if (plfile.startsWith("#")) {
			loadPL = false;
			lastChoicePl = plfile.replace('\\', '/');
			lastChoicePl = lastChoicePl.replace("\"", "");
			lastChoicePl = lastChoicePl.substring(1);
		} else {
			loadPL = true;
			lastChoicePl = plfile.replace('\\', '/');
			lastChoicePl = lastChoicePl.replace("\"", "");
		}
		
		// build control
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
	
		Group groupLib = new Group(composite, SWT.NONE);
		groupLib.setText("Target library");
		groupLib.setLayout(new GridLayout(2, false));
		groupLib.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gridData.horizontalSpan = 2;
		checkLib = new Button(groupLib, SWT.CHECK);
		checkLib.setText("Use default library path");
		checkLib.setSelection(absLibPath.equals(defaultPath.replace('\\', '/')));
		checkLib.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) { // checkbox event
				if (e.widget.equals(checkLib)) {
					if (checkLib.getSelection()) {
						pathLib.setEnabled(false);
						browseLib.setEnabled(false);
						pathLib.setText(defaultPath);
					} else {
						pathLib.setEnabled(true);
						browseLib.setEnabled(true);
						pathLib.setText(absLibPath);
					}
					absLibPath = pathLib.getText();
					if (checkLibPath()) readConfig();
				}
			}
		});
		checkLib.setLayoutData(gridData);
		pathLib = new Text(groupLib, SWT.SINGLE | SWT.BORDER);
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = SWT.FILL;
		gridData2.grabExcessHorizontalSpace = true;
		pathLib.setLayoutData(gridData2);
		pathLib.setText(libPath);
		pathLib.setEnabled(!checkLib.getSelection());
		pathLib.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) { // path changed event
				libPath = pathLib.getText();
				if (!(new File(libPath).isAbsolute())) {
					String s = project.getLocation().toString() + IPath.SEPARATOR + libPath;
					try {
						absLibPath = new File(s).getCanonicalPath();
					} catch (IOException e1) {
						ErrorReporter.reporter.error(255, "path=" + libPath);
						return;
					}
				} else absLibPath = libPath;
				if (checkLibPath()) readConfig();
			}
		});
		browseLib = new Button(groupLib, SWT.PUSH);
		browseLib.setText("Browse...");
		browseLib.setEnabled(!checkLib.getSelection());
		browseLib.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) { // browse event
				if (!checkLib.getSelection()){	
					DirectoryDialog dlg = new DirectoryDialog(getShell());        
			        dlg.setFilterPath(pathLib.getText()); // Set the initial filter path according to anything they've selected or typed in
			        dlg.setText("deep Library Path Selection");
			        dlg.setMessage("Select a directory");
			        String dir = dlg.open(); // Calling open() will open and run the dialog.
			        if (dir != null) {
			        	pathLib.setText(dir);
			        	libPath = dir;
						if (!(new File(libPath).isAbsolute())) {
							String s = project.getLocation().toString() + IPath.SEPARATOR + libPath;
							try {
								absLibPath = new File(s).getCanonicalPath();
							} catch (IOException e1) {
								ErrorReporter.reporter.error(255, "path=" + libPath);
								return;
							}
						} else absLibPath = libPath;
			        }
					if (checkLibPath()) readConfig();
				}
			}
		});
		libState = new Label(groupLib,SWT.NONE);
		libState.setLayoutData(gridData);

		Group groupBoard = new Group(composite, SWT.NONE);
		groupBoard.setText("Board configuration");
		groupBoard.setLayout(new GridLayout(1, false));
		groupBoard.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Label boardLabel = new Label(groupBoard, SWT.NONE);
		boardLabel.setText("Select a board");
		boardCombo = new Combo(groupBoard, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		boardCombo.addSelectionListener(listener);
		Label progLabel = new Label(groupBoard, SWT.NONE);
		progLabel.setText("Select a programmer");
		programmerCombo = new Combo(groupBoard, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		programmerCombo.addSelectionListener(listener);
		Label progOptLabel = new Label(groupBoard, SWT.NONE);
		progOptLabel.setText("Select programmer options");
		programmerOpts = new Text(groupBoard, SWT.SINGLE | SWT.BORDER);
		programmerOpts.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		programmerOpts.setEnabled(true);
		programmerOpts.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
			}
		});

		Group groupOS = new Group(composite, SWT.NONE);
		groupOS.setText("Runtime system");
		groupOS.setLayout(new GridLayout(1, false));
		groupOS.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Label osLabel = new Label(groupOS,SWT.NONE);
		osLabel.setText("Select an operating system");
		osCombo = new Combo(groupOS, SWT.VERTICAL | SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		osCombo.addSelectionListener(listener);

		Group groupImg = new Group(composite, SWT.NONE);
		groupImg.setText("Image file creation");
		groupImg.setLayout(new GridLayout(2, false));
		groupImg.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));	
		checkImg = new Button(groupImg, SWT.CHECK);
		checkImg.setText("Create image file in directory");
		checkImg.setSelection(createImgFile);
		checkImg.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(e.widget.equals(checkImg)) {
					if (checkImg.getSelection()) {
						pathImg.setEnabled(true);
						browseImg.setEnabled(true);
						createImgFile = true;
					} else {
						pathImg.setEnabled(false);
						browseImg.setEnabled(false);
						createImgFile = false;
					}
					pathImg.setText(lastChoiceImg);
				}
			}
		});
		checkImg.setLayoutData(gridData);
		
		pathImg = new Text(groupImg, SWT.SINGLE | SWT.BORDER);
		pathImg.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		pathImg.setText(lastChoiceImg);
		pathImg.setEnabled(createImgFile);
		pathImg.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				lastChoiceImg = imgLocation = pathImg.getText().replace('\\', '/');
				if (checkLibPath()) readConfig();
			}
		});
		browseImg = new Button(groupImg, SWT.PUSH);
		browseImg.setText("Browse...");
		browseImg.setEnabled(createImgFile);
		browseImg.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				if (checkImg.getSelection()) {	
					DirectoryDialog dlg = new DirectoryDialog(getShell());        
			        dlg.setFilterPath(pathImg.getText()); // Set the initial filter path according to anything they've selected or typed in
			        dlg.setText("Image File Save Location");
			        dlg.setMessage("Select a directory");
			        String dir = dlg.open(); // Calling open() will open and run the dialog.
			        if (dir != null) {
			        	pathImg.setText(dir);
			        	lastChoiceImg = dir;
			        }
				}
			}
		});	
		GridData gridDataImg3 = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridDataImg3.horizontalSpan = 2;
		
		Group groupPL = new Group(composite, SWT.NONE);
		groupPL.setText("Configuration file for PL");
		groupPL.setLayout(new GridLayout(2, false));
		groupPL.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		downloadPL = new Button(groupPL, SWT.CHECK);
		downloadPL.setText("Download PL file");
		downloadPL.setSelection(loadPL);
		downloadPL.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(e.widget.equals(downloadPL)) {
					if (downloadPL.getSelection()) {
						pathPL.setEnabled(true);
						browsePL.setEnabled(true);
						loadPL = true;
					} else {
						pathPL.setEnabled(false);
						browsePL.setEnabled(false);
						loadPL = false;
					}
					pathPL.setText(lastChoicePl);
				}
			}
		});
		downloadPL.setLayoutData(gridData);
		pathPL = new Text(groupPL, SWT.SINGLE | SWT.BORDER);
		pathPL.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		pathPL.setText(lastChoicePl);
		pathPL.setEnabled(loadPL);
		pathPL.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				lastChoicePl = plfile = pathPL.getText().replace('\\', '/');
			}
		});
		browsePL = new Button(groupPL, SWT.PUSH);
		browsePL.setText("Browse...");
		browsePL.setEnabled(loadPL);
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
				}
			}
		});	
		if (checkLibPath()) readConfig(); else libPath = "not available";
		return composite;
	}
	
	private SelectionAdapter listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			if (e.widget.equals(boardCombo)) {
				if (boardCombo.getSelectionIndex() != boardCombo.getItemCount() - 1) board = boards[boardCombo.getSelectionIndex()][0];
				else board = "";
			}
			if (e.widget.equals(osCombo)) {
				if (osCombo.getSelectionIndex() != osCombo.getItemCount() - 1) os = operatingSystems[osCombo.getSelectionIndex()][0];
				else os = "";
			}
			if (e.widget.equals(programmerCombo)) {
				if (programmerCombo.getSelectionIndex() != programmerCombo.getItemCount() - 1) programmer = programmers[programmerCombo.getSelectionIndex()][0];
				else programmer = "";
			}
		}
	};

	private void readConfig() {
		boards = Configuration.getDescInConfigDir(new File(absLibPath + Configuration.boardsPath), Parser.sBoard);
		String[] str = new String[boards.length + 1];
		int index = boards.length;
		for (int i = 0; i < boards.length; i++) {
			str[i] = boards[i][1];
			if (board.equals(boards[i][0])) index = i;
		}
		str[str.length - 1] = "none";
		boardCombo.setItems(str);
		boardCombo.select(index);

		programmers = Configuration.getDescInConfigDir(new File(absLibPath.toString() + Configuration.progPath), Parser.sProgrammer);
		str = new String[programmers.length + 1];
		index = programmers.length;
		for (int i = 0; i < programmers.length; i++) {
			str[i] = programmers[i][1];
			if (programmer.equals(programmers[i][0])) index = i;
		}
		str[str.length - 1] = "none";
		programmerCombo.setItems(str);
		programmerCombo.select(index);

		programmerOpts.setText(programmerOpt);
		
		operatingSystems = Configuration.getDescInConfigDir(new File(absLibPath.toString() + Configuration.osPath), Parser.sOperatingSystem);
		str = new String[operatingSystems.length + 1];
		index = operatingSystems.length;
		for (int i = 0; i < operatingSystems.length; i++) {
			str[i] = operatingSystems[i][1];
			if (os.equals(operatingSystems[i][0])) index = i;
		}
		str[str.length - 1] = "none";
		osCombo.setItems(str);
		osCombo.select(index);
	}

	private boolean checkLibPath() {
		File lib = new File(absLibPath);
		if (!lib.exists()) {
			libState.setText("Given library path is NOT valid target library.");
			return false;		
		}
		String[][] boards = Configuration.getDescInConfigDir(new File(lib.toString() + Configuration.boardsPath), Parser.sBoard);
		if (boards == null || boards[0][0].equals("none")) {
			libState.setText("Given library path is NOT valid target library.");
			return false;			
		}
		libState.setText("");
		return true;
	}

	protected void performApply() {
		saveFiles();
		super.performApply();
	}

	public boolean performOk() {
		saveFiles();
		return true;
	}
	
	public boolean performCancel() {
		return true;
	}

	private void saveFiles() {
		// change deep file
		IProject project = (IProject) getElement().getAdapter(IProject.class);
		GregorianCalendar cal = new GregorianCalendar();
		dfc.changeContent("version", '\"' + cal.getTime().toString() + '\"');
		dfc.changeContent("libpath", '\"' + libPath.replace('\\', '/') + '\"');
		dfc.changeContent("boardtype", board);
		dfc.changeContent("ostype", os);
		if (programmerCombo.getText().equals("none")) {
			dfc.commentContent("programmertype");
		} else {
			if (dfc.changeContent("programmertype", programmer) != 0)
				dfc.changeContent("programmertype", programmer);
		}
		if (programmerOpts.getText() != null && programmerOpts.getText().length() > 0) {
			if (dfc.changeContent("programmeropts", programmerOpts.getText()) != 0)
				dfc.addContent("programmeropts", programmerOpt);
		} else {
			dfc.commentContent("programmeropts");
		}
		if (createImgFile) {  
			if (dfc.changeContent("imgfile", '\"' + lastChoiceImg + (lastChoiceImg.endsWith("/")?'"':"/\"")) != 0)
				dfc.addContent("imgfile", '\"' + lastChoiceImg + (lastChoiceImg.endsWith("/")?'"':"/\""));
		} else {
			dfc.commentContent("imgfile");
		}
		if (loadPL) {  
			if (dfc.changeContent("pl_file", "\"" + lastChoicePl + '\"') != 0)
				dfc.addContent("pl_file", "\"" + lastChoicePl + '\"');
		} else {
			dfc.commentContent("pl_file");
		}
		dfc.save();

		// change classpath file
		try {
			DeepFileChanger cfc = new DeepFileChanger(project.getLocation() + "/.classpath");
			cfc.changeLibPath(libPath);
			cfc.save();
		} catch (Exception e) {
			ErrorDialog.openError(getShell(), "Error", "Project name must be identical to project directory name", makeStatus(e));
			return;
		}

		try { // refresh the package explorer
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static IStatus makeStatus(Exception x) {
		if (x instanceof CoreException) {
			return ((CoreException) x).getStatus();
		} else {
			return new Status(IStatus.ERROR, "org.deepjava", IStatus.ERROR, x.getMessage() != null ? x.getMessage() : x.toString(), x);
		}
	}

}


