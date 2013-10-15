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

import java.io.File;

public class DeepProjectModel {

	private File lib;
	private String[] board;
	private String[] osName;
	private String[] programmerName;

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

	public String[] getOs() {
		return osName;
	}

	public void setOs(String[] os) {
		this.osName = os;
	}
	
}
