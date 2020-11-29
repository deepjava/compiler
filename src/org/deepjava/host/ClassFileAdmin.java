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

package org.deepjava.host;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.deepjava.strings.HString;

public class ClassFileAdmin {
	private static final boolean dbg = false;
	private static PrintStream vrb = StdStreams.vrb;
	
	public static final String classFileType =  ".class";
//	public static final String deepFileType =  ".deep";
	private static File[] parentDirs; // parent directories of class files
	
	public static void  clear(){
		parentDirs = null;
	}

	/**
	 * Register parent directories of class file libraries.
	 * <br>These parent directories hold disjoint sets of class files for the project being built.
	 * Parent directories may be set only once per system build.
	 * <br>During registering, each given directory is checked for existence. An error is reported if it does not exist or if it is not a directory.
	 * @param parentDirectories   parent directories
	 */
	public static void registerParentDirs(File[] parentDirectories) {
		if (dbg) vrb.println("Registering parent directories of class files:");
		if (ClassFileAdmin.parentDirs != null)
			ErrorReporter.reporter.error(303);
		else {
			int nofPaths = parentDirectories.length;
			ClassFileAdmin.parentDirs = new File[nofPaths];
			boolean error = false;
			for (int path = 0; path < nofPaths; path++){
				if (dbg) vrb.print("  Registering: "+ parentDirectories[path].getAbsolutePath() + '\t');
				if (!parentDirectories[path].exists() || (!parentDirectories[path].isDirectory() && !parentDirectories[path].getName().endsWith(".jar"))) {
					ErrorReporter.reporter.error(304, parentDirectories[path].getAbsolutePath());
					error = true;
				}
				ClassFileAdmin.parentDirs[path] = parentDirectories[path];
				if (dbg) vrb.println();
			}
			if (error) clear();
		}
	}

	/**
	 * Returns the file reference of the class file specified by its <code>className</code> or <code>null</code>
	 * if it is not found.
	 * @param className
	 * @return  File reference of the class file or <code>null</code>
	 */
	public static InputStream getClassFileInputStream(HString className) {
		if (parentDirs == null) return null;
		InputStream inStrm = null;
		String clsFileName = className.toString() + classFileType;
		int pd = 0;
		do {
			if (parentDirs[pd].isDirectory()) {
				if (dbg) vrb.println(className + ": search in dir");
				File clsFile;
				clsFile = new File(parentDirs[pd], clsFileName);
				if (clsFile.isFile()) {
					try {
						inStrm = new FileInputStream(clsFile);
					} catch (FileNotFoundException fnfE) {
						ErrorReporter.reporter.error(300, fnfE.getMessage());
						fnfE.getCause();
					}
				}
			} else {
				if (dbg) vrb.println(className + ": search in jar file");
				JarFile jar = null;
				ZipEntry entry = null;
				try {
					jar = new JarFile(parentDirs[pd]);
//					Enumeration<JarEntry> e = jar.entries();
//					while (e.hasMoreElements()) {
//					      JarEntry je = (JarEntry) e.nextElement();
//					      String name = je.getName();
//					      System.out.println(name);
//					}
					entry = jar.getEntry(clsFileName);
				} catch (IOException e1) { }
				try {
					if (entry != null) {
						inStrm = jar.getInputStream(entry);
					}
				} catch (IOException e) {
					ErrorReporter.reporter.error(300, e.getMessage());
					e.getCause();
				}
			}
			++pd;
		} while (inStrm == null && pd < parentDirs.length);
		return inStrm;
	}

	/**
	 * Returns the file reference of the class file specified by its <code>className</code> or <code>null</code>
	 * if it is not found.
	 * @param className
	 * @return  File reference of the class file or <code>null</code>
	 */
//	public static InputStream getClassFileInputStream(HString className) {
//		if (parentDirs == null) return null;
//		InputStream inStrm = null;
//		String clsFileName = className.toString() + classFileType;
//		int pd = 0;
//		do {
//			if (parentDirs[pd].isDirectory()) {
//				if (dbg) vrb.println(className + ": search in dir");
//				File clsFile;
//				clsFile = new File(parentDirs[pd], clsFileName);
//				if (clsFile.isFile()) {
//					try {
//						inStrm = new FileInputStream(clsFile);
//					} catch (FileNotFoundException fnfE) {
//						ErrorReporter.reporter.error(300, fnfE.getMessage());
//						fnfE.getCause();
//					}
//				}
//			} else {
//				if (dbg) vrb.println(className + ": search in jar file");
//				JarFile jar = null;
//				ZipEntry entry = null;
//				try {
//					jar = new JarFile(parentDirs[pd]);
//					entry = jar.getEntry(clsFileName);
//				} catch (IOException e1) { }
//				try {
//					if (entry != null) {
//						inStrm = jar.getInputStream(entry);
//					}
//				} catch (IOException e) {
//					ErrorReporter.reporter.error(300, e.getMessage());
//					e.getCause();
//				}
//			}
//			++pd;
//		} while (inStrm == null && pd < parentDirs.length);
//		return inStrm;
//	}

	public static void printParentDirs(){
		PrintStream out = StdStreams.vrb;
		out.println("Registered parent dirs of class files:");
		if (parentDirs != null) {
			int nofPaths = parentDirs.length;
			for (int pd = 0; pd < nofPaths; pd++){
				File parentDir = parentDirs[pd];
				try{
					out.printf("  Parent dir [%1$d] = %2$s, name = %3$s", pd, parentDir.getCanonicalPath(), parentDir.getName() );
				}catch(IOException e){
					e.printStackTrace();
				}
				if (!parentDir.exists() || ! parentDir.isDirectory()) out.print(", " +304);
				out.println();
			}
		}
	}
}
