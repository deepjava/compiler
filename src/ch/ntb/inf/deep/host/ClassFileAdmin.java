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

package ch.ntb.inf.deep.host;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import ch.ntb.inf.deep.strings.HString;

public class ClassFileAdmin {
	public static final String classFileType =  ".class";
	public static int errChangingParentDir = 303, errMsgIllegalParentDir = 304;

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
	public static void registerParentDirs(String[] parentDirectories){
		PrintStream log = StdStreams.log;
		log.println("registering parent dirs of class files:");
		if(ClassFileAdmin.parentDirs != null)
			ErrorReporter.reporter.error(errChangingParentDir);
		else{
			int nofPaths = parentDirectories.length;
			ClassFileAdmin.parentDirs = new File[nofPaths];
			boolean error = false;
			for(int path = 0; path < nofPaths; path++){
				String parentPath = parentDirectories[path];
				log.print("  registering: "+ parentPath + '\t');
				File parentDir = new File( parentPath );
				if( ! parentDir.exists() || (! parentDir.isDirectory() &&  !parentDir.getName().endsWith(".jar") )){
					log.println("Errorcode " + errMsgIllegalParentDir);
					ErrorReporter.reporter.error(errMsgIllegalParentDir);
					error = true;
				}
				ClassFileAdmin.parentDirs[path] = parentDir;
				log.println();
			}
			if(error) clear();
		}
	}

	/**
	 * Returns the file reference of the class file specified by its <code>className</code> or <code>null</code>
	 * if it is not found.
	 * @param className
	 * @return  File reference of the class file or <code>null</code>
	 */
	public static InputStream getClassFileInputStream(HString className){
		if( parentDirs == null ) return null;
		InputStream inStrm = null;
		String clsFileName = className.toString() + classFileType;
		int pd = 0;
		do{
			if(parentDirs[pd].isDirectory()){
				File clsFile;
				clsFile = new File(parentDirs[pd], clsFileName);
				if(clsFile.isFile()){
					try {
						inStrm = new FileInputStream(clsFile);
					} catch (FileNotFoundException fnfE) {
						ErrorReporter.reporter.error(300, fnfE.getMessage());
						fnfE.getCause();
					}
				}
			}else{
				JarFile jar = null;
				ZipEntry entry = null;
				try {
					jar = new JarFile(parentDirs[pd]);
					entry = jar.getEntry(clsFileName);
				}catch(IOException e1){
				}
				try{
					if(entry != null){
						inStrm = jar.getInputStream(entry);
					}
				} catch (IOException e) {
					ErrorReporter.reporter.error(300, e.getMessage());
					e.getCause();
				}
			}
			++pd;
		}while( inStrm == null && pd < parentDirs.length);
		return inStrm;
	}

	public static void printParentDirs(){
		PrintStream out = StdStreams.out;
		out.println("registered parent dirs of class files:");
		if( parentDirs != null ){
			int nofPaths = parentDirs.length;
			for(int pd = 0; pd < nofPaths; pd++){
				File parentDir = parentDirs[pd];
				try{
					out.printf("  parent dir [%1$d] = %2$s, name = %3$s", pd, parentDir.getCanonicalPath(), parentDir.getName() );
				}catch(IOException e){
					e.printStackTrace();
				}
				if( ! parentDir.exists() || ! parentDir.isDirectory() ) out.print(", " +errMsgIllegalParentDir);
				out.println();
			}
		}
	}
}
