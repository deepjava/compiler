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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.GregorianCalendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.osgi.service.prefs.BackingStoreException;

import ch.ntb.inf.deep.eclipse.DeepPlugin;


public class DeepProjectWizard extends Wizard implements INewWizard{
	private LibPathPage libPathPage;
	private TargetConfigPage targetConfigPage;
	private ProjectConfigPage projectConfigPage;

	private IProject project;
	protected DeepProjectModel model = new DeepProjectModel();

	@Override
	public boolean performFinish() {
		try {
			getContainer().run(false, true, new WorkspaceModifyOperation() {
				@Override
				protected void execute(IProgressMonitor monitor) {
					createProject(monitor != null ? monitor : new NullProgressMonitor());
				}
			});
		} catch (InvocationTargetException x) {
			reportError(x);
			return false;
		} catch (InterruptedException x) {
			reportError(x);
			return false;
		}
		save();
		dispose();
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("NTB Project Wizard");
		setDefaultPageImageDescriptor(getImageDescriptor("deep.gif"));
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		libPathPage = new LibPathPage("First Page");
		libPathPage.setTitle("Target Library");
		libPathPage.setDescription("Please choose the target library to use for this project");
		addPage(libPathPage);
		targetConfigPage = new TargetConfigPage("Second Page");
		targetConfigPage.setTitle("Target configuration");
		targetConfigPage.setDescription("Please choose the board and operating system for this project");
		addPage(targetConfigPage);
		projectConfigPage = new ProjectConfigPage("Third Page");
		projectConfigPage.setTitle("Projectname");
		projectConfigPage.setDescription("Please define your projectname");
		addPage(projectConfigPage);
	}

	private ImageDescriptor getImageDescriptor(String relativePath) {
		String iconPath = "icons/full/obj16/";

		try {
			DeepPlugin plugin = DeepPlugin.getDefault();
			URL installURL = plugin.getBundle().getEntry("/");
			URL url;
			url = new URL(installURL, iconPath + relativePath);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private InputStream getClassPathFileContent() {
		String libpath = model.getLibrary().getPathAsString();
		StringBuffer sb = new StringBuffer();
		File srcFolder = new File(libpath + "/src");
		sb.append("<?xml version=\"1.0\" encoding =\"UTF-8\"?>\n");
		sb.append("<classpath>\n");
		sb.append("\t<classpathentry kind=\"src\" path=\"src\"/>\n");
		if(srcFolder.exists()) {			
			sb.append("\t<classpathentry kind=\"lib\" path=\"" + libpath + "/bin\" sourcepath=\"" + libpath + "/src\"/>\n");
		}
		else {			
			sb.append("\t<classpathentry kind=\"lib\" path=\"" + libpath + "/bin\"/>\n");
		}
		sb.append("\t<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n");
		sb.append("\t<classpathentry kind=\"output\" path=\"bin\"/>\n");
		sb.append("</classpath>\n");
		return new ByteArrayInputStream(sb.toString().getBytes());
	}

	private void createClassPath(IProject project) {
		IFile file = project.getFile(".classpath");
		try {
			file.create(getClassPathFileContent(), true, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	private void createDeepFile(){
		IFile file = project.getFile(project.getName() +".deep");
		   try{
			   file.create(getDeepFileContent(), false, null);
		   } catch(CoreException e){
			   e.printStackTrace();
		   }
	}
	
	private InputStream getDeepFileContent(){
		GregorianCalendar cal = new GregorianCalendar();
		StringBuffer sb = new StringBuffer();
		sb.append("#deep-1\n\nmeta {\n\tversion = \"" + cal.getTime() +"\";\n");
		sb.append("\tdescription = \"deep project file for " + project.getName() + "\";\n");
		sb.append("}\n\n");
		sb.append("project " + project.getName() + "{\n\tlibpath = ");
		sb.append("\"" + model.getLibrary().getPathAsString() + "\";\n");
		sb.append("\tboardtype = ");
		if(model != null && model.getBoard() != null) sb.append(model.getBoard().getName().toString());
		sb.append(";\n");
		sb.append("\tostype = ");
		if(model != null && model.getOs() != null) sb.append(model.getOs().getName().toString());
		sb.append(";\n");
		sb.append("\tprogrammertype = ");
		if(model != null && model.getProgrammer() != null) sb.append(model.getProgrammer().getName().toString());
		sb.append(";\n");
		sb.append("\trootclasses = \"\";\n}\n");
		
		return new ByteArrayInputStream(sb.toString().getBytes());
	}

	private void createFolders(IProject project) {
		IFolder scrFolder = project.getFolder("src");
		IFolder binFolder = project.getFolder("bin");
		try {
			scrFolder.create(true, true, null);
			binFolder.create(true, true, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void createProject(IProgressMonitor monitor) {
		monitor.beginTask("Creating project", 20);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		project = projectConfigPage.getProjectHandle();
		IProjectDescription description = workspace.newProjectDescription(project.getName());
		
		if (!Platform.getLocation().equals(projectConfigPage.getLocationPath())) {
			description.setLocation(projectConfigPage.getLocationPath());
		}					
		
		
		try {
			project.create(description, monitor);
			monitor.worked(10);
			if (project.exists()) {
				project.open(monitor);
				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			} else {
				return;
			}
			description = project.getDescription();		
			description.setNatureIds(new String[] {"ch.ntb.inf.deep.nature.DeepNature",	"org.eclipse.jdt.core.javanature" });
			project.setDescription(description, new SubProgressMonitor(monitor, 10));
		} catch (CoreException e) {
			System.out.println("CoreException: " + e.getLocalizedMessage());
			e.printStackTrace();
		}finally{
			createFolders(project);
			createClassPath(project);
			createDeepFile();
			monitor.done();
		}
	}
	
	private void save(){
		ProjectScope scope = new ProjectScope(project);
		IEclipsePreferences pref = scope.getNode("deepStart");//TODO implement check if this node exists...
		pref.put("board", model.getBoard().getName().toString());
		pref.put("rts", model.getOs().getName().toString());
		pref.put("libPath", model.getLibrary().getPathAsString());
		try {
			pref.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
	
	private void reportError(Exception x) {
		ErrorDialog.openError(getShell(), "Error", "Project creation error", makeStatus(x));
	}
	
	public static IStatus makeStatus(Exception x) {

		if (x instanceof CoreException) {
			return ((CoreException) x).getStatus();
		} else {
			return new Status(IStatus.ERROR, "ch.ntb.inf.deep", IStatus.ERROR, x.getMessage() != null ? x.getMessage() : x.toString(), x);
		}
	}
}
