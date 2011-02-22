/**
 * Copyright (c) 2010 NTB Interstaatliche Hochschule für Technick Buchs
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * 
 * Contributors:
 *	   NTB - initial implementation
 *	   Roger Millischer - initial implementation
 */
package ch.ntb.inf.deep.ui.properties;

import java.io.IOException;
import java.util.GregorianCalendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
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

public class ProjectPage extends PropertyPage implements IWorkbenchPropertyPage {
	private Combo processor, board, rts;
	private Button check, browse;
	private Text path;
	private final String defaultPath = "I:/deep/lib";
	private String lastChoise = "";
	private IEclipsePreferences pref;

	@Override
	protected Control createContents(Composite parent) {
		pref = getPref();
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
		processor.setItems(new String[] { "MPC555" });
		processor.setText(pref.get("proc", ""));

		Group group2 = new Group(composite, SWT.NONE);
		group2.setText("Configuration");
		RowLayout rowLayout3 = new RowLayout(SWT.VERTICAL);
		group2.setLayout(rowLayout3);
		Label label1 = new Label(group2, SWT.NONE);
		label1.setText("Select a configuration");
		board = new Combo(group2, SWT.BORDER);
		board.setItems(new String[] { "NTB MPC555 Headerboard",
				"phyCORE-mpc555" });
		board.setText(pref.get("board", ""));

		Group group3 = new Group(composite, SWT.NONE);
		group3.setText("Runtime-System");
		GridLayout gridLayout2 = new GridLayout(2, false);
		group3.setLayout(gridLayout2);
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true,
				false);
		gridData.horizontalSpan = 2;
		group3.setLayoutData(gridData);
		Label label2 = new Label(group3, SWT.NONE);
		label2.setText("Select a runtime system");
		label2.setLayoutData(gridData);
		rts = new Combo(group3, SWT.BORDER);
		rts.setItems(new String[] { "Simple tasking system", "uCos" });
		rts.setText(pref.get("rts", ""));
		rts.setLayoutData(gridData);
		Label dummy = new Label(group3, SWT.NONE);
		dummy.setLayoutData(gridData);
		check = new Button(group3, SWT.CHECK);
		check.setText("use default library path");
		check.setSelection(pref.getBoolean("useDefault", true));
		check.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (check.getSelection()) {
					path.setEnabled(false);
					path.setText(defaultPath);
				} else {
					path.setEnabled(true);
					path.setText(lastChoise);
				}
			}
		});
		check.setLayoutData(gridData);
		path = new Text(group3, SWT.SINGLE | SWT.BORDER);
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = SWT.FILL;
		gridData2.grabExcessHorizontalSpace = true;
		path.setLayoutData(gridData2);
		path.setText(pref.get("libPath", defaultPath));
		path.setEnabled(!check.getSelection());
		if (!check.getSelection()) {
			lastChoise = path.getText();
		}
		browse = new Button(group3, SWT.PUSH);
		browse.setText("Browse...");
		browse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!check.getSelection()) {
					openDirectoryDialog();
				}
			}
		});

		return composite;

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

	private IEclipsePreferences getPref() {
		IProject project = (IProject) getElement();
		ProjectScope scope = new ProjectScope(project);
		return scope.getNode("deepStart");
	}

	protected void performApply() {
		save();
		super.performApply();
	}

	public boolean performOk() {
		save();
		return true;
	}

	@Override
	protected void performDefaults() {
		processor.setText("MPC555");
		board.setText("NTB MPC555 Headerboard");
		check.setSelection(true);
		path.setEnabled(false);
		path.setText(defaultPath);
		rts.setText("Simple tasking system");
	}

	private void save() {
		pref.put("proc", processor.getText());
		pref.put("board", board.getText());
		pref.put("rts", rts.getText());
		if (check.getSelection()) {
			pref.putBoolean("useDefault", true);
			pref.put("libPath", defaultPath);
		} else {
			pref.putBoolean("useDefault", false);
			pref.put("libPath", path.getText());
		}
		performChanges();
		try {
			pref.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	private void performChanges() {
		GregorianCalendar cal = new GregorianCalendar();
		IProject project = (IProject) getElement();
		ConfigFileChanger cfc = new ConfigFileChanger(project.getLocation()
				+ "/" + project.getName() + ".deep");
		try {
			cfc.changeContent("version", "\"" + cal.getTime().toString() + "\"");

			StringBuffer sb = new StringBuffer();
			if (processor.getText().equals("MPC555")) {
				if (board.getText().equals("NTB MPC555 Headerboard")) {
					sb.append("\"config/ntbMpc555HB.deep\"");
					if (rts.getText().equals("Simple tasking system")) {
						sb.append(", \"config/ntbMpc555STS.deep\"");
					} else if (rts.getText().equals("uCos")) {
						sb.append(", \"config/ntbMpc555uCOS.deep\"");
					}
				} else if (board.getText().equals("phyCORE-mpc555")) {
					sb.append("\"config/phyMpc555Core.deep\"");
					if (rts.getText().equals("Simple tasking system")) {
						sb.append(", \"config/ntbMpc555STS.deep\"");
					} else if (rts.getText().equals("uCos")) {
						sb.append(", \"config/ntbMpc555uCOS.deep\"");
					}
				}
			} else {
				sb.append("\"\"");
			}
			cfc.changeContent("import", sb.toString());
			cfc.changeContent("libpath", "\"" + path.getText() + "\"");
		} catch (IOException e) {
			e.printStackTrace();
		}

		cfc.save();
		cfc.close();
	}
}
