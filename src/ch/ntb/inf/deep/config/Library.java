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
import java.io.FilenameFilter;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.strings.HString;

public class Library extends ConfigElement implements ErrorCodes {
	
	private File path;
	private Arch archs; // list with all CPU architectures in the library
	private CPU cpus; // list with all CPUs defined in this library
	private Board boards; // list with all boards defined in this library
	private OperatingSystem operatingsystems; // list with all operating systems defined in this library
	private Programmer programmers; // list with all programmers defined in this library
	private Constants compilerConstants = new Constants("compiler constants", true);
	private boolean isJarFile;
	
	public Library(HString path) {
		this.name = path;
		this.path = new File(path.toString());
		if(!this.path.isDirectory()) {
			this.isJarFile = true;
		}
	}
	
	public Library(String path) {
		this.name = HString.getRegisteredHString(path);
		this.path = new File(path);
		if(!this.path.isDirectory()) {
			this.isJarFile = true;
		}
	}
	
	public void readConfig() {
		if(!isJarFile) { // config files inside of jar files are not allowed
			File[] configDir = {
				new File(path.getPath() + "/config/base"),
				new File(path.getPath() + "/config/arch"),
				new File(path.getPath() + "/config/cpus"),
				new File(path.getPath() + "/config/boards"),
				new File(path.getPath() + "/config/operatingsystems"),
				new File(path.getPath() + "/config/programmers")};
			String[] configFileNames;
			File cfgFile = null;
			Parser cfgFileParser = null;
			FilenameFilter filter = new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			        return !name.startsWith(".") && name.endsWith(".deep");
			    }
			};
			for(int i = 0; i < configDir.length; i++) {
				if(Configuration.dbg) StdStreams.vrb.println("[CONF] Library: Reading config files from sub directory: " + configDir[i]);
				configFileNames = configDir[i].list(filter);
				if(configFileNames != null) {
					for(int j = 0; j < configFileNames.length; j++) {
						if(Configuration.dbg) StdStreams.vrb.println("[CONF] Library: Reading config file (" + (j + 1) + "/" + configFileNames.length + "): " + configFileNames[j]);
						cfgFile = new File(configDir[i] + "/" + configFileNames[j]);
						cfgFileParser = new Parser(cfgFile, this);
						cfgFileParser.start();
					}
				}
			}
		}
	}

	public Board addBoard(String jname) {
		if(Configuration.dbg) StdStreams.vrb.println("[CONF] Library: adding board " + jname);
		Board b;
		if(boards == null) {
			boards = new Board(jname);
			b = boards;
		}
		else {
			b = (Board)boards.getElementByName(jname);
			if(b == null) {
				b = new Board(jname);
				boards.append(b);
			}
		}
		return b;
	}

	public CPU addCpu(String jname) {
		if(Configuration.dbg) StdStreams.vrb.println("[CONF] Library: adding CPU " + jname);
		CPU c;
		if(cpus == null) {
			if(Configuration.dbg) StdStreams.vrb.println("  Adding first CPU");
			cpus = new CPU(jname);
			c = cpus;
		}
		else {
			if(Configuration.dbg) StdStreams.vrb.print("  Looking for CPU with name " + jname);
			c = (CPU)cpus.getElementByName(jname);
			if(c == null) {
				if(Configuration.dbg) StdStreams.vrb.println(" -> not found -> adding new CPU");
				c = new CPU(jname);
				cpus.append(c);
			}
			else {
				if(Configuration.dbg) StdStreams.vrb.println(" -> found -> nothing to do");
			}
		}
		return c;
	}
	
	public Arch addArch(String jname) {
		if(Configuration.dbg) StdStreams.vrb.println("[CONF] Library: adding archtecture " + jname);
		Arch a;
		if(archs == null) {
			if(Configuration.dbg) StdStreams.vrb.println("  Adding first architecture");
			archs = new Arch(jname);
			a = archs;
		}
		else {
			if(Configuration.dbg) StdStreams.vrb.print("  Looking for architecture with name " + jname);
			a = (Arch)archs.getElementByName(jname);
			if(a == null) {
				if(Configuration.dbg) StdStreams.vrb.println(" -> not found -> adding new architecture");
				a = new Arch(jname);
				archs.append(a);
			}
			else {
				if(Configuration.dbg) StdStreams.vrb.println(" -> found -> nothing to do");
			}
		}
		return a;
	}
	
	public Programmer addProgrammer(String jname) {
		if(Configuration.dbg) StdStreams.vrb.println("[CONF] Library: adding programmer " + jname);
		Programmer p;
		if(programmers == null) {
			if(Configuration.dbg) StdStreams.vrb.println("  Adding first programmer");
			programmers = new Programmer(jname);
			p = programmers;
		}
		else {
			if(Configuration.dbg) StdStreams.vrb.print("  Looking for programmer with name " + jname);
			p = (Programmer)programmers.getElementByName(jname);
			if(p == null) {
				if(Configuration.dbg) StdStreams.vrb.println(" -> not found -> adding new programmer");
				p = new Programmer(jname);
				programmers.append(p);
			}
			else {
				if(Configuration.dbg) StdStreams.vrb.println(" -> found -> nothing to do");
			}
		}
		return p;
	}
	
	public OperatingSystem addOperatingSystem(String jname) {
		if(Configuration.dbg) StdStreams.vrb.println("[CONF] Library: adding operating system " + jname);
		OperatingSystem os;
		if(operatingsystems == null) {
			if(Configuration.dbg) StdStreams.vrb.println("  Adding first operating system");
			operatingsystems = new OperatingSystem(jname);
			os = operatingsystems;
		}
		else {
			if(Configuration.dbg) StdStreams.vrb.print("  Looking for operating system with name " + jname);
			os = (OperatingSystem)operatingsystems.getElementByName(jname);
			if(os == null) {
				if(Configuration.dbg) StdStreams.vrb.println(" -> not found -> adding new operating system");
				os = new OperatingSystem(jname);
				operatingsystems.append(os);
			}
			else {
				if(Configuration.dbg) StdStreams.vrb.println(" -> found -> nothing to do");
			}
		}
		return os;
	}

	public Board getBoardByName(String jname) {
		if(boards != null) return (Board)boards.getElementByName(jname);
		return null;
	}
	
	public Board getBoardByName(HString name) {
		if(boards != null) return (Board)boards.getElementByName(name);
		return null;
	}
		
	public OperatingSystem getOperatingSystemByName(String jname) {
		if(operatingsystems != null) return (OperatingSystem)operatingsystems.getElementByName(jname);
		return null;
	}
	
	public OperatingSystem getOperatingSystemByName(HString name) {
		if(operatingsystems != null) return (OperatingSystem)operatingsystems.getElementByName(name);
		return null;
	}
	
	public Programmer getProgrammerByName(String jname) {
		if(programmers != null) return (Programmer)programmers.getElementByName(jname);
		return null;
	}
	
	public Programmer getProgrammerByName(HString name) {
		if(programmers != null) return (Programmer)programmers.getElementByName(name);
		return null;
	}
	
	public Board[] getBoards() {
		Board[] boards = new Board[this.boards.getNofElements()];
		Board b = (Board)this.boards.getHead();
		int count = 0;
		while(b != null && count < boards.length) {
			boards[count] = b;
			b = (Board)b.next;
			count++;
		}
		return boards;
	}

	public Arch[] getArchs() {
		Arch[] archs = new Arch[this.archs.getNofElements()];
		Arch a = (Arch)this.archs.getHead();
		int count = 0;
		while(a != null && count < archs.length) {
			archs[count] = a;
			a = (Arch)a.next;
			count++;
		}
		return archs;
	}
	
	public OperatingSystem[] getOperatingSystems() {
		OperatingSystem[] operatingsystems = new OperatingSystem[this.operatingsystems.getNofElements()];
		OperatingSystem os = (OperatingSystem)this.operatingsystems.getHead();
		int count = 0;
		while(os != null && count < operatingsystems.length) {
			operatingsystems[count] = os;
			os = (OperatingSystem)os.next;
			count++;
		}
		return operatingsystems;
	}
	
	public Programmer[] getProgrammers() {
		Programmer[] programmers = new Programmer[this.programmers.getNofElements()];
		Programmer p = (Programmer)this.programmers.getHead();
		int count = 0;
		while(p != null && count < programmers.length) {
			programmers[count] = p;
			p = (Programmer)p.next;
			count++;
		}
		return programmers;
	}
	
	public int getNofBoards() {
		if(this.boards != null) return this.boards.getNofElements();
		return 0;
	}
	
	public int getNofOperatingSystems() {
		if(this.operatingsystems != null) return this.operatingsystems.getNofElements();
		return 0;
	}
	
	public CPU getCpuByName(String jname) {
		return (CPU)cpus.getElementByName(jname);
	}
	
	public CPU getCpuByName(HString name) {
		return (CPU)cpus.getElementByName(name);
	}
	
	public Arch getArchByName(String jname) {
		return (Arch)archs.getElementByName(jname);
	}
	
	public Arch getArchByName(HString name) {
		return (Arch)archs.getElementByName(name);
	}
		
	public String getPathAsString() {
		return path.getPath();
	}
	
	public File getPath() {
		return path;
	}
	
	public ValueAssignment getCompConstByName(HString name) {
		return compilerConstants.getConstantByName(name);
	}
	
	public ValueAssignment getFirstCompConst() {
		return compilerConstants.getFirstConstant();
	}

	protected Constants getCompilerConstants() {
		return compilerConstants;
	}
}
