/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     NTB/Roger Millischer - Adaption for deep
 *******************************************************************************/

package ch.ntb.inf.deep.eclipse.launcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import ch.ntb.inf.deep.eclipse.DeepPlugin;

public class DeepLaunchShortcut implements ILaunchShortcut2 {
	public static final String ID = "ch.ntb.inf.deep.launcher.DeepLaunchShortcut";

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut2#getLaunchConfigurations(org.eclipse.jface.viewers.ISelection)
	 */
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		return getConfigurations();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut2#getLaunchConfigurations(org.eclipse.ui.IEditorPart)
	 */
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		return getConfigurations();
	}

	/**
	 * Returns all of the launch configurations of type <code>org.eclipse.jdt.debug.tests.testConfigType
	 * @return all of the launch configurations of type <code>org.eclipse.jdt.debug.tests.testConfigType
	 */
	protected ILaunchConfiguration[] getConfigurations() {
		try {
			ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = lm.getLaunchConfigurationType("deep.launchType");
			return lm.getLaunchConfigurations(type);
		}
		catch(CoreException ce) {}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut2#getLaunchableResource(org.eclipse.jface.viewers.ISelection)
	 */
	public IResource getLaunchableResource(ISelection selection) {
		return null;//ResourcesPlugin.getWorkspace().getRoot();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut2#getLaunchableResource(org.eclipse.ui.IEditorPart)
	 */
	public IResource getLaunchableResource(IEditorPart editorpart) {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.jface.viewers.ISelection, java.lang.String)
	 */
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			performLaunch(((IStructuredSelection) selection).toArray(), mode);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.ui.IEditorPart, java.lang.String)
	 */
	public void launch(IEditorPart editor, String mode) {
		IEditorInput input = editor.getEditorInput();
		if (input.getName().endsWith(".deep")) {
			IFile file = (IFile) input.getAdapter(IFile.class);
			if (file != null) {
				performLaunch(new Object[] { file }, mode);
			}
		}
	}

	protected void performLaunch(Object[] search, String mode) {
		// first try to find a candidate
		Object candidate = null;
		boolean found = false;
		if (search != null) {
			for (int i = 0; i < search.length && !found; i++) {
				candidate = search[i];
				if (!(candidate instanceof IFile)
						&& candidate instanceof IAdaptable) {
					candidate = ((IAdaptable) candidate)
							.getAdapter(IFile.class);
				}
				if (candidate instanceof IFile) {
					IFile element = (IFile) candidate;
					if ("deep".equals(element.getFileExtension())) {
						found = true;
					}
				}
			}
			if (found) {
				// second try to find a config
				try {
					String[] attributeToCompare = new String[]{ DeepPlugin.ATTR_DEEP_PROGRAM, DeepPlugin.ATTR_DEEP_LOCATION, DeepPlugin.ATTR_TARGET_CONFIG};
					ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
					ILaunchConfigurationType type = lm.getLaunchConfigurationType("deep.launchType");
					ILaunchConfiguration config = null;
					if (type != null) {
						ILaunchConfiguration[] configs = lm	.getLaunchConfigurations(type);
						ILaunchConfigurationWorkingCopy copy = createConfiguration((IFile)candidate, configs);
						for(int i = 0; i < configs.length; i++) {
							if(hasSameAttributes(configs[i], copy, attributeToCompare)){
								config = configs[i];
								break;
							}
						}
						if (config == null) {
							// create a new one
							config = copy.doSave();
						}
						if (config != null) {
							DebugUITools.launch(config, mode);
						}
					}
				} catch (CoreException ce) {
					DebugPlugin.log(ce);
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	protected ILaunchConfigurationWorkingCopy createConfiguration(IFile fileToLaunch, ILaunchConfiguration[] configs) {
		ILaunchConfigurationWorkingCopy wc = null;
		String name = fileToLaunch.getName();
		for(int i = 0; i < configs.length; i++){
			if(name.equals(configs[i].getName())){
				name = DebugPlugin.getDefault().getLaunchManager().generateUniqueLaunchConfigurationNameFrom(fileToLaunch.getName());
				break;
			}
		}
		try {
			ILaunchConfigurationType configType = getConfigurationType();
			wc = configType.newInstance(null, name);
			wc.setAttribute(DeepPlugin.ATTR_DEEP_PROGRAM, fileToLaunch.getProjectRelativePath().toString());
			wc.setAttribute(DeepPlugin.ATTR_DEEP_LOCATION, fileToLaunch.getProject().getFullPath().toString());
			wc.setAttribute(DeepPlugin.ATTR_TARGET_CONFIG, "BootFromRam");
			// CONTEXTLAUNCHING
			wc.setMappedResources(new IResource[] { fileToLaunch });
		} catch (CoreException ce) {
			// reportErorr(ce);
		}
		return wc;
	}
	
	protected ILaunchConfigurationType getConfigurationType() {
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		return lm.getLaunchConfigurationType("deep.launchType");
	}
	
	private boolean hasSameAttributes(ILaunchConfiguration config, ILaunchConfigurationWorkingCopy temporary ,String[] attributeToCompare){
		for (int i = 0; i < attributeToCompare.length; i++){
			try {
				if(!config.getAttribute(attributeToCompare[i], "").equals(temporary.getAttribute(attributeToCompare[i], ""))){
					return false;
				}
			} catch (CoreException e) {
				return false;
			}
		}
		return true;
	}
}
