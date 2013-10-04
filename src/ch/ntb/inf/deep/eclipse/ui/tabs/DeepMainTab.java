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
package ch.ntb.inf.deep.eclipse.ui.tabs;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;

import ch.ntb.inf.deep.config.Board;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.RunConfiguration;
import ch.ntb.inf.deep.eclipse.DeepPlugin;

public class DeepMainTab extends AbstractLaunchConfigurationTab {

	private Text fProgramText;	// text box for deep file name
	private String program;	// deep file name
	private String locationPath;	// project name
	private String runConf;
	private Button[] runConfigs;
	private Group group2;
	private Composite comp;
	
	private SelectionAdapter selectionListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			for (int i = 0; i < runConfigs.length; i++) {
				if (e.widget.equals(runConfigs[i])) {
					String conf = runConfigs[i].getText();
					RunConfiguration rc = Configuration.getBoard().runConfig;
					while (rc != null) {
						if (rc.description.equals(conf)) runConf = rc.name.toString();
						rc = (RunConfiguration) rc.next;
					}

				}
			}
			updateLaunchConfigurationDialog();
		}
	};

	public void createControl(Composite parent) {
		Font font = parent.getFont();

		comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout topLayout = new GridLayout();
		topLayout.verticalSpacing = 0;
		topLayout.numColumns = 2;
		comp.setLayout(topLayout);
		comp.setFont(font);

		createVerticalSpacer(comp, 3);

		Group group1 = new Group(comp, SWT.NONE);
		group1.setText("Main deep file");
		GridLayout gridLayout1 = new GridLayout(2, false);
		group1.setLayout(gridLayout1);
		GridData gd = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gd.horizontalSpan = 2;
		group1.setLayoutData(gd);
		fProgramText = new Text(group1, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fProgramText.setLayoutData(gd);
		fProgramText.setFont(font);
		fProgramText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		Button deepFileBrowse = createPushButton(group1, "&Browse...", null);
		deepFileBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ResourceListSelectionDialog dialog = new ResourceListSelectionDialog( getShell(), ResourcesPlugin.getWorkspace().getRoot(), IResource.FILE);
				dialog.setTitle("Deep Program");
				dialog.setMessage("Select deep project file");
				if (dialog.open() == Window.OK) {
					Object[] files = dialog.getResult();
					IFile file = (IFile) files[0];
					locationPath = file.getProject().getFullPath().toString();
					fProgramText.setText(file.getProjectRelativePath().toString());
					program = file.getProjectRelativePath().toString();
				}
			}
		});
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			program = configuration.getAttribute(DeepPlugin.ATTR_DEEP_PROGRAM,	(String) null);
			if (program != null) {
				locationPath = configuration.getAttribute(DeepPlugin.ATTR_DEEP_LOCATION, "");
				runConf = configuration.getAttribute(DeepPlugin.ATTR_TARGET_CONFIG, "");
				fProgramText.setText(program);
				if (group2 != null) { 
					if (group2.getChildren().length > 0) for (Control ctrl : group2.getChildren()) ctrl.dispose();
					group2.dispose();
				}
				group2 = new Group(comp, SWT.NONE);
				group2.setText("Target configuration");
				GridLayout gridLayout2 = new GridLayout(2, false);
				group2.setLayout(gridLayout2);
				GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
				gridData.horizontalSpan = 2;
				group2.setLayoutData(gridData);
				Label label = new Label(group2,SWT.NONE);
				label.setText("Select an available target configuration from the list");
				label.setLayoutData(gridData);
				Configuration.readProjectFileAndRunConfigs(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + locationPath + IPath.SEPARATOR + program);
				Board b = Configuration.getBoard();
				if (b == null) {
					label.setText("No configuration available");
					Configuration.readProjectFile(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + locationPath + IPath.SEPARATOR + program);
				}
				else label.setText("Select an available configuration");
				RunConfiguration rc = null;
				if (Configuration.getBoard() != null) rc = Configuration.getBoard().runConfig;
				int nof = 0;
				while (rc != null) {nof++; rc = (RunConfiguration) rc.next;}
				runConfigs = new Button[nof];
				int i = 0;
				if (Configuration.getBoard() != null) rc = Configuration.getBoard().runConfig;
				while (rc != null) {
					runConfigs[i] = new Button(group2, SWT.RADIO);
					runConfigs[i].setText(rc.description);
					if (rc.name.toString().equals(runConf)) runConfigs[i].setSelection(true);
					else runConfigs[i].setSelection(false);
					runConfigs[i].addSelectionListener(selectionListener);
					runConfigs[i].setLayoutData(gridData);
					i++;
					rc = (RunConfiguration) rc.next;
				}
			}
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(DeepPlugin.ATTR_DEEP_PROGRAM, program);
		configuration.setAttribute(DeepPlugin.ATTR_DEEP_LOCATION, locationPath);
		configuration.setAttribute(DeepPlugin.ATTR_TARGET_CONFIG , runConf);
		Configuration.setActiveTargetConfig(runConf);
		
		// perform resource mapping for contextual launch
		IResource[] resources = null;
		if (program != null) {
			IPath path = new Path(locationPath + "/" + program);
			IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			if (res != null) {
				resources = new IResource[] { res };
			}
		}
		configuration.setMappedResources(resources);
	}
	
	public String getName() {
		return "Main";
	}

	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		setMessage(null);
		String text = fProgramText.getText();
		if (text.length() > 0) {
			int lastIndex = locationPath.lastIndexOf("/");
			String projectName;
			if(lastIndex > -1){
				projectName = locationPath.substring(lastIndex +1);
			}else{
				projectName = "";
			}
			IPath path = new Path(projectName +"/" +text);
			if (ResourcesPlugin.getWorkspace().getRoot().findMember(path) == null) {
				setErrorMessage(text + " does not exist");
				return false;
			}
			if(!text.endsWith(".deep")){
				setErrorMessage("chose a deep project file");
				return false;
			}
		} else {
			setMessage("Specify a program");
		}
		return true;
	}

	public Image getImage() {
		return null; // DebugUIPlugin.getDefault().getImageRegistry().get(DebugUIPlugin.IMG_OBJ_PDA);
	}
}
