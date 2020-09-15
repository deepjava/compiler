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
	int imgFileFormat;				// image file format
	HString plFileName;				// pl file name (programmable logic)
	
	public Project(String deepFileName) {
		Item.stab = StringTable.getInstance();
		File rel = new File(deepFileName);
		String path = rel.getAbsolutePath();
		this.projectFile = new File(path);
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
	
	public HString[] getLibPaths() {
		return libPath;
	}
	
	public void setProjectName(String projectName) {	
		this.deepProjectName = HString.getHString(projectName);
	}

	public HString getProjectName() {	
		return deepProjectName;
	}
	
	public HString getProjectFileName() {
		return deepProjectFileName;
	}
	
	public void setTctFileName(String name) {
		tctFileName = HString.getHString(name);
	}

	public HString getTctFileName() {
		return tctFileName;
	}
	
	public HString getImgFileName() {
		return imgFileName;
	}

	public void setImgFileName(String name) {
		imgFileName = HString.getHString(name);
	}
	
	public int getImgFileFormat() {
		return imgFileFormat;
	}

	public void setImgFileFormat(int format) {
		imgFileFormat = format;
	}
	
	public HString getPlFileName() {
		return plFileName;
	}

	public void setPlFileName(String name) {
		plFileName = HString.getHString(name);
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
		if (plFileName != null) {indent(indentLevel+1); vrb.println("pl_file = " + plFileName);}
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
