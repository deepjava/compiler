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

package org.deepjava.eclipse.ui.wizard;

import java.io.File;

public class DeepProjectModel {

	private File lib;
	private String[] board;
	private String[] osName;
	private String[] programmerName;
	private String programmerOptions;
	private File imgPath, plFilePath;
	private String imgFormat; 
	private boolean createImgFile, loadPlFile;

	public void setLibrary(File lib) {
		this.lib = lib;
	}
	
	public File getLibrary() {
		return this.lib;
	}

	public String[] getBoard() {
		return board;
	}

	public void setBoard(String[] b) {
		board = b;
	}

	public String[] getProgrammer() {
		return programmerName;
	}

	public void setProgrammer(String[] prog) {
		this.programmerName = prog;
	}

	public String getProgrammerOptions() {
		return programmerOptions;
	}

	public void setProgrammerOptions(String opt) {
		this.programmerOptions = opt;
	}

	public String[] getOs() {
		return osName;
	}

	public void setOs(String[] os) {
		this.osName = os;
	}
	
	public void setImgPath(File imgPath){
		this.imgPath = imgPath;
	}
	
	public File getImgPath(){
		return imgPath;
	}
	
	public void setImgFormat(String imgFormat){
		this.imgFormat = imgFormat;
	}
	
	public String getImgFormat(){
		return imgFormat;
	}
	
	public void setCreateImgFile(boolean createImgFile){
		this.createImgFile = createImgFile;
	}
	
	public boolean createImgFile(){
		return createImgFile;
	}

	public void setLoadPlFile(boolean load){
		this.loadPlFile = load;
	}
	
	public boolean getLoadPlFile(){
		return loadPlFile;
	}
	
	public void setPlFilePath(File file){
		this.plFilePath = file;
	}
	
	public File getPlFilePath(){
		return plFilePath;
	}

}
