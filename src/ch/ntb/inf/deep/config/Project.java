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

import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class Project extends ConfigElement {
	HString[] rootClasses;
	Library[] lib;
	HString tctFile;
	Board board;
	Programmer programmer;
	OperatingSystem os;
	TargetConfiguration activeTargetConf;
	File projectFile;
	HString projectFileName;
	
	public Project(String fileName) {
		this.projectFile = new File(fileName.toString());
		this.projectFileName = HString.getRegisteredHString(fileName);
	}
	
	public void setName(String projectName) {
		this.name = HString.getRegisteredHString(projectName);
	}

	public void setRootClasses(HString[] rootClasses) {
		this.rootClasses = rootClasses;
	}

	public void setLibPath(HString[] libPath) {
		this.lib = new Library[libPath.length];
		for(int i = 0; i < libPath.length; i++) {			
			this.lib[i] = Configuration.addLibrary(libPath[i]);
		}
	}
	
	public void setTctFile(String tctFile) {
		this.tctFile = HString.getRegisteredHString(tctFile);
	}

	public void setBoard(Board board) {
		this.board = board;
	}
	
	public Board setBoard(String jname) {
		HString boardName = HString.getRegisteredHString(jname);
		Board b = null;
		int i = 0;
		while(i < lib.length && b == null) {
			b = lib[i].getBoardByName(boardName);
			i++;
		}
		if(b != null) {
			this.board = b;
		}
		return b;
	}
	
	public void setProgrammer(Programmer programmer) {
		this.programmer = programmer;
	}
	
	public Programmer setProgrammer(String jname) {
		HString programmerName = HString.getRegisteredHString(jname);
		Programmer programmer = null;
		int i = 0;
		while(i < lib.length && programmer == null) {
			programmer = lib[i].getProgrammerByName(programmerName);
			i++;
		}
		if(programmer != null) {
			this.programmer = programmer;
		}
		return programmer;
	}
	
	public void setOperatingSystem(OperatingSystem os) {
		this.os = os;
	}
	
	public OperatingSystem setOperatingSystem(String jname) {
		HString osName = HString.getRegisteredHString(jname);
		OperatingSystem os = null;
		int i = 0;
		while(i < lib.length && os == null) {
			os = lib[i].getOperatingSystemByName(osName);
			i++;
		}
		if(os != null) {
			this.os = os;
		}
		return os;
	}
	
	public void setActiveTargetConfiguration(TargetConfiguration tc) {
		this.activeTargetConf = tc;
	}
	
	public boolean setActiveTargetConfiguration(String tcName) {
		if(this.board != null) {
			this.activeTargetConf = this.board.getTargetConfigurationByName(tcName);
			if(this.activeTargetConf != null) return true;
		}
		return false;
	}
	
	public HString[] getRootClasses() {
		return rootClasses;
	}

	public String getRootClassNames() {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < rootClasses.length; i++) {
			sb.append(rootClasses[i].toString());
			if(i < rootClasses.length - 1) sb.append(", ");
		}
		sb.append(";");
		return sb.toString();
	}
	
	public String getLibPathAsSingleString() {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < lib.length; i++) {
			sb.append(lib[i].getPath());
			sb.append(", ");
		}
		return sb.substring(0, sb.lastIndexOf(","));
	}
	
	public String[] getLibPathAsStringArray() {
		String[] paths = new String[lib.length];
		for(int i = 0; i < lib.length; i++) {
			paths[i] = lib[i].getPathAsString();
		}
		return paths;
	}
	
	public File[] getLibPathAsFileArray() {
		File[] paths = new File[lib.length];
		for(int i = 0; i < lib.length; i++) {
			paths[i] = lib[i].getPath();
		}
		return paths;
	}
	
	public HString getTctFile() {
		return tctFile;
	}
	
	public Board getBoard() {
		return board;
	}

	public Project getProjectByFileName(HString registeredFileName) {
		Project p = (Project)this.getHead();
		while(p != null && p.projectFileName != registeredFileName) {
			p = (Project)p.next;
		}
		return p;
	}
	
	public void readProjectFile() {
		Parser projectFileParser = new Parser(this.projectFile, this);
		projectFileParser.start();
	}
	
	public Programmer getProgrammer() {
		return programmer;
	}
	
	public OperatingSystem getOperatingSystem() {
		return os;
	}
	
	public TargetConfiguration getActiveTargetConfiguration() {
		return this.activeTargetConf;
	}
		
	public int getNumberOfStacks() {
		return board.memorymap.getNofStacks() + board.cpu.memorymap.getNofStacks();
	}
	
	public int getNumberOfHeaps() {
		return board.memorymap.getNofHeaps() + board.cpu.memorymap.getNofHeaps();
	}

	public Segment[] getHeapSegments() {
		int nofHeapSegmentsBoard = board.memorymap.getNofHeaps();
		int nofHeapSegmentsCpu = board.cpu.memorymap.getNofHeaps();
		Segment[] heapSegments = new Segment[nofHeapSegmentsBoard + nofHeapSegmentsCpu];
		int j = 0;
		for(int i = 0; i < nofHeapSegmentsBoard; i++){
			heapSegments[j++] = board.memorymap.getHeapSegment(i);
		}
		for(int i = 0; i < nofHeapSegmentsCpu; i++){
			heapSegments[j++] = board.cpu.memorymap.getHeapSegment(i);
		}
		return heapSegments;
	}

	public Segment[] getStackSegments() {
		int nofStackSegmentsBoard = board.memorymap.getNofStacks();
		int nofStackSegmentsCpu = board.cpu.memorymap.getNofStacks();
		Segment[] stackSegments = new Segment[nofStackSegmentsBoard + nofStackSegmentsCpu];
		int j = 0;
		for(int i = 0; i < nofStackSegmentsBoard; i++){
			stackSegments[j++] = board.memorymap.getStackSegment(i);
		}
		for(int i = 0; i < nofStackSegmentsCpu; i++){
			stackSegments[j++] = board.cpu.memorymap.getStackSegment(i);
		}
		return stackSegments;
	}
	
	public File getProjectDir() {
		return this.projectFile.getParentFile();
	}
	
	public int getValOfCompConstByName(String jname) {
		HString name = HString.getRegisteredHString(jname);
		int i = 0;
		ValueAssignment val = null;
		while(val == null && i < lib.length) {
			val = lib[i].getCompConstByName(name);
			i++;
		}		
		return val.getValue(); 
	}
	
	/* debug primitives */
	public void println(int indentLevel) {
		indent(indentLevel);
		StdStreams.vrb.println("project {");
		
		indent(indentLevel + 1);
		StdStreams.vrb.println("libpath = " + getLibPathAsSingleString());
		
		indent(indentLevel + 1);
		StdStreams.vrb.println("boardtype = " + board.getName() +" (" + board.getDescription() + ")");
		
		indent(indentLevel + 1);
		StdStreams.vrb.println("ostype = " + os.getName() +" (" + os.getDescription() + ")");
		
		indent(indentLevel + 1);
		StdStreams.vrb.println("programmertype = " + programmer.getName() +" (" + programmer.getDescription() + ")");
		
		// TODO print programmer options here
		
		indent(indentLevel + 1);
		for(int i = 0; i < rootClasses.length; i++) {
			StdStreams.vrb.print(rootClasses[i].toString());
			if(i < rootClasses.length - 1) StdStreams.vrb.print(", ");
		}
		StdStreams.vrb.println(";");
		
		indent(indentLevel + 1);
		StdStreams.vrb.print("tctfile = ");
		if(tctFile != null) StdStreams.vrb.print(tctFile.toString());
		else StdStreams.vrb.print("<not set>");
		StdStreams.vrb.println(";");
		
		indent(indentLevel);
		StdStreams.out.println("}");
	}
	
	private void indent(int indentLevel) {
		for (int i = indentLevel; i > 0; i--) {
			StdStreams.vrb.print("  ");
		}
	}
}
