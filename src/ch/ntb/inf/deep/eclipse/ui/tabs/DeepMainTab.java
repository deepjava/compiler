/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *     
 * changes: Millischer Roger  adapt from Example  PDAMainTab
 *******************************************************************************/
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;

import ch.ntb.inf.deep.eclipse.DeepPlugin;

/**
 * Tab to specify the Deep program to run/debug.
 */
public class DeepMainTab extends AbstractLaunchConfigurationTab {

	private Text fProgramText, targetConfig;
	private String program;
	private String locationPath;
	private Button fProgramButton, ram, flash, other;
	private String lastChoise = "";
	private String BOOT_FROM_RAM = "BootFromRam";
	private String BOOT_FROM_FLASH = "BootFromFlash";
	
	private SelectionAdapter selectionListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e){
			if (e.widget.equals(ram)){
				ram.setSelection(true);
				flash.setSelection(false);
				if(other.getSelection()){
					other.setSelection(false);
					targetConfig.setEnabled(false);
					targetConfig.setText("");
				}
			}else if (e.widget.equals(flash)){
				flash.setSelection(true);
				ram.setSelection(false);
				if(other.getSelection()){
					other.setSelection(false);
					targetConfig.setEnabled(false);
					targetConfig.setText("");
				}
			}else if (e.widget.equals(other)){
				ram.setSelection(false);
				flash.setSelection(false);
				other.setSelection(true);
				targetConfig.setEnabled(true);
				targetConfig.setText(lastChoise);
			}
			updateLaunchConfigurationDialog();
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse
	 * .swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();

		Composite comp = new Composite(parent, SWT.NONE);
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
		fProgramButton = createPushButton(group1, "&Browse...", null); //$NON-NLS-1$
		fProgramButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				browseFiles();
			}
		});
		
		Group group3 = new Group(comp, SWT.NONE);
		group3.setText("Target configuration");
		GridLayout gridLayout2 = new GridLayout(2, false);
		group3.setLayout(gridLayout2);
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 2;//TODO check
		group3.setLayoutData(gridData);
		Label label2 = new Label(group3,SWT.NONE);
		label2.setText("Select a targetConfiguration");
		label2.setLayoutData(gridData);
		
		ram = new Button(group3, SWT.RADIO);
		ram.setText("Boot from ram");
		ram.setSelection(false);
		ram.addSelectionListener(selectionListener);
		ram.setLayoutData(gridData);
		flash = new Button(group3, SWT.RADIO);
		flash.setText("Boot from flash");
		flash.setSelection(false);
		flash.addSelectionListener(selectionListener);
		flash.setLayoutData(gridData);
		other = new Button(group3, SWT.RADIO);
		other.setText("Other");
		other.setSelection(false);
		other.addSelectionListener(selectionListener);
		targetConfig = new Text(group3, SWT.SINGLE | SWT.BORDER);
		GridData gridData2 = new GridData();
//		gridData2.horizontalAlignment = SWT.FILL;
		gridData2.grabExcessHorizontalSpace = true;
		targetConfig.setLayoutData(gridData2);
		targetConfig.setEnabled(false);
	}

	/**
	 * Open a resource chooser to select a program
	 */
	protected void browseFiles() {
		ResourceListSelectionDialog dialog = new ResourceListSelectionDialog( getShell(), ResourcesPlugin.getWorkspace().getRoot(), IResource.FILE);
		dialog.setTitle("Deep Program");
		dialog.setMessage("Select deep project file");
		if (dialog.open() == Window.OK) {
			Object[] files = dialog.getResult();
			IFile file = (IFile) files[0];
			locationPath = file.getProject().getLocation().toString();
//			String temp = fProgramText.getText();
//			if (!temp.equals("")) {
//				temp = temp + ";";
//			}
			fProgramText.setText(file.getProjectRelativePath().toString());
			program = file.getProjectRelativePath().toString();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.
	 * debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse
	 * .debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			program = configuration.getAttribute(DeepPlugin.ATTR_DEEP_PROGRAM,	(String) null);
			if (program != null) {
				fProgramText.setText(program);
			}
			locationPath = configuration.getAttribute(	DeepPlugin.ATTR_DEEP_LOCATION, "");
			String targetConf = configuration.getAttribute(DeepPlugin.ATTR_TARGET_CONFIG, BOOT_FROM_RAM);
			if(targetConf.endsWith(BOOT_FROM_RAM)){
				ram.setSelection(true);
			}else if(targetConf.equals(BOOT_FROM_FLASH)){
				flash.setSelection(true);
			}else{
				other.setSelection(true);
				targetConfig.setEnabled(true);
				targetConfig.setText(targetConf);
			}
		
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse
	 * .debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		
		configuration.setAttribute(DeepPlugin.ATTR_DEEP_PROGRAM, program);
		configuration.setAttribute(DeepPlugin.ATTR_DEEP_LOCATION, locationPath);
		if(ram.getSelection()){
			configuration.setAttribute(DeepPlugin.ATTR_TARGET_CONFIG , BOOT_FROM_RAM);
		}else if(flash.getSelection()){
			configuration.setAttribute(DeepPlugin.ATTR_TARGET_CONFIG , BOOT_FROM_FLASH);
		}else{
			configuration.setAttribute(DeepPlugin.ATTR_TARGET_CONFIG , targetConfig.getText());
		}

		// perform resource mapping for contextual launch
		IResource[] resources = null;
		if (program != null) {
			IPath path = new Path(program);
			IResource res = ResourcesPlugin.getWorkspace().getRoot()
					.findMember(path);
			if (res != null) {
				resources = new IResource[] { res };
			}
		}
		configuration.setMappedResources(resources);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return "Main";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug
	 * .core.ILaunchConfiguration)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return null; // DebugUIPlugin.getDefault().getImageRegistry().get(DebugUIPlugin.IMG_OBJ_PDA);
	}
}
