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
		saveProjectPreferences();
		dispose();
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("NTB deep Project Wizard");
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
			} else return;
			description = project.getDescription();		
			description.setNatureIds(new String[] {"ch.ntb.inf.deep.nature.DeepNature",	"org.eclipse.jdt.core.javanature" });
			project.setDescription(description, new SubProgressMonitor(monitor, 10));
		} catch (CoreException e) {e.printStackTrace();
		} finally {
			// create folders
			IFolder scrFolder = project.getFolder("src");
			IFolder binFolder = project.getFolder("bin");
			try {
				scrFolder.create(true, true, null);
				binFolder.create(true, true, null);
			} catch (CoreException e) {e.printStackTrace();}
			
			// create classpath file
			IFile file = project.getFile(".classpath");
			String libpath = model.getLibrary().getAbsolutePath();
			StringBuffer sb = new StringBuffer();
			File srcFolder = new File(libpath + "/src");
			sb.append("<?xml version=\"1.0\" encoding =\"UTF-8\"?>\n");
			sb.append("<classpath>\n");
			sb.append("\t<classpathentry kind=\"src\" path=\"src\"/>\n");
			if (srcFolder.exists()) sb.append("\t<classpathentry kind=\"lib\" path=\"" + libpath + "/bin\" sourcepath=\"" + libpath + "/src\"/>\n");
			else sb.append("\t<classpathentry kind=\"lib\" path=\"" + libpath + "/bin\"/>\n");
//			sb.append("\t<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n");
			sb.append("\t<classpathentry kind=\"output\" path=\"bin\"/>\n");
			sb.append("</classpath>\n");
			InputStream in = new ByteArrayInputStream(sb.toString().getBytes());
			try {
				file.create(in, true, null);
			} catch (CoreException e) {e.printStackTrace();}

			// create deep file
			file = project.getFile(project.getName() +".deep");
			GregorianCalendar cal = new GregorianCalendar();
			sb = new StringBuffer();
			sb.append("#deep-1\n\nmeta {\n\tversion = \"" + cal.getTime() +"\";\n");
			sb.append("\tdescription = \"deep project file for " + project.getName() + "\";\n");
			sb.append("}\n\n");
			sb.append("project " + project.getName() + " {\n\tlibpath = ");
			String str = model.getLibrary().getAbsolutePath();
			str = str.replace('/', '\\');			
			sb.append("\"" + str + "\";\n");
			sb.append("\tboardtype = ");
			if (model != null && model.getBoard() != null) sb.append(model.getBoard()[0]);
			sb.append(";\n");
			sb.append("\tostype = ");
			if (model != null && model.getOs() != null) sb.append(model.getOs()[0]);
			sb.append(";\n");
			sb.append("\tprogrammertype = ");
			if (model != null && model.getProgrammer() != null) sb.append(model.getProgrammer()[0]);
			sb.append(";\n\n#\tenter names of rootclasses, e.g.");
			sb.append("\n#\trootclasses = \"test.MyFirstTestClass\",\"other.MySecondTestClass\";");
			sb.append("\n\trootclasses = \"\";\n}\n");
			in = new ByteArrayInputStream(sb.toString().getBytes());
			try {
				file.create(in, false, null);
			} catch(CoreException e) {e.printStackTrace();}
			monitor.done();
		}
	}
	
	private void saveProjectPreferences(){
		ProjectScope scope = new ProjectScope(project);
		IEclipsePreferences pref = scope.getNode("deepStart");
		if (pref != null) {
			String[] name = model.getBoard();
			if (name != null) pref.put("board", name[0]);
			name = model.getOs();
			if (name != null) pref.put("os", name[0]);
			name = model.getProgrammer();
			if (name != null) pref.put("programmer", name[0]);
			pref.put("libPath", model.getLibrary().getAbsolutePath());
		}
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
