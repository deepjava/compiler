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

package ch.ntb.inf.deep.config;

import java.io.File;
import ch.ntb.inf.deep.classItems.Item;
import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.strings.HString;
import ch.ntb.inf.deep.strings.StringTable;

public class Project extends Item {
	HString[] libPath;
	File[] libs;
	File projectFile;
	HString deepProjectFileName;	// deep project file name
	HString deepProjectName;		// deep project name as specified in the project file
	HString tctFileName;			// target command file name
	HString imgFileName;			// image file name
	
	
	public Project(String deepFileName) {
		Item.stab = StringTable.getInstance();
		this.projectFile = new File(deepFileName);
		this.deepProjectFileName = HString.getRegisteredHString(deepFileName);
	}
	
	public void createLibs(HString[] libPath) {
		this.libPath = libPath;
		libs = new File[libPath.length];
		for (int i = 0; i < libPath.length; i++) {			
			libs[i] = new File(libPath[i].toString());
			if (!libs[i].exists()) ErrorReporter.reporter.error(255, "path=" + libs[i].toString()); 
		}
	}
	
	public void setProjectName(String projectName) {	
		this.deepProjectName = HString.getHString(projectName);
	}

	public HString getProjectName() {	
		return deepProjectName;
	}
	
	public void setTctFileName(String name) {
		tctFileName = HString.getHString(name);
	}

	public HString getTctFileName() {
		return tctFileName;
	}
	
	public void setImgFileName(String name) {
		imgFileName = HString.getHString(name);
	}
	
	public HString getImgFileName() {
		return imgFileName;
	}

	public File getProjectDir() {
		return projectFile.getParentFile();
	}
	
	/* debug primitives */
	public void print(int indentLevel) {
		indent(indentLevel);
		vrb.println("project " + deepProjectName + " {");
		indent(indentLevel+1);
		vrb.print("libpath = ");
		for (HString path : libPath) vrb.print("\"" + path + "\"   ");
		vrb.println();
		if (tctFileName != null) {indent(indentLevel+1); vrb.println("tctFile = " + tctFileName);}
		if (imgFileName != null) {indent(indentLevel+1); vrb.println("imgFile = " + imgFileName);}
		indent(indentLevel+1);
		vrb.println("board = " + Configuration.getBoard().name + " (" + Configuration.getBoard().description + ")");
		indent(indentLevel+1);
		vrb.println("cpu = " + Configuration.getBoard().cpu.name + " (" + Configuration.getBoard().cpu.description + ")");
		indent(indentLevel+1);
		vrb.println("architecture = " + Configuration.getBoard().cpu.arch.name);
		indent(indentLevel+1);
		vrb.println("operating system = " + Configuration.getOS().name);
		indent(indentLevel+1);
		vrb.println("programmertype = " + Configuration.getProgrammer().name);		
		indent(indentLevel+1);
		vrb.println("root classes = {");
		for (HString name : Configuration.getRootClasses()) {indent(indentLevel+2); vrb.println("\"" + name + "\"");}
		indent(indentLevel+1);
		vrb.println("}");
		indent(indentLevel); vrb.println("}");
	}
	
}
