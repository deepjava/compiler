﻿/*
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

package ch.ntb.inf.deep.eclipse.ui.properties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ClasspathFileChanger {
	private BufferedReader configFile;
	private BufferedWriter out;
	private StringBuffer fileContent;
	//private int count;
	
	public ClasspathFileChanger(String file) {
		try {
			//count = 0;
			fileContent = new StringBuffer();
			configFile = new BufferedReader(new FileReader(file));
			
			int ch  = configFile.read();
			while(ch  != -1){
				fileContent.append((char)ch);
				ch = configFile.read();
				//count++;
			}
			configFile.close();
			out = new BufferedWriter(new FileWriter(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void changeLibPath(String oldPath, String newPath) throws IOException{
		String key = "<classpathentry kind=\"lib\" path=\"";
		int indexOfKey = fileContent.indexOf(key + oldPath);
		if(indexOfKey > -1){
			int indexOfEndtoken = fileContent.indexOf("/>", indexOfKey);
			File srcFolder = new File(newPath + "/src");
			if(srcFolder.exists()){
				fileContent.replace(indexOfKey, indexOfEndtoken, key + newPath + "/bin\" sourcepath=\"" + newPath + "/src\"");
			}else{				
				fileContent.replace(indexOfKey, indexOfEndtoken, key + newPath + "/bin\"");
			}
		}else{
			throw new IOException();
		}
	}
	
	public void save(){
		try {
			out.write(fileContent.toString());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void close(){
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
