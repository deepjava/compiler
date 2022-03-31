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
/**
changes:
<br>2022-03-25, OST/GRAU	error file must be found in all use cases
<br>11-09-12, NTB/MILR	error(int errNr, String additionalInfo): printing errNr, errMsg and additional informations, handling if class is into a jar
<br>09-04-22, NTB/ED	error(int errNr, String errMsg): printing errNr, errMsg
<br>09-03-23, NTB/ED	extension of error(int errNr, String errMsg)
<br>05-02-11, NTB/ED	creation
*/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;


/**
  * Does the common error reporting (implemented as a singleton)
 */
public class ErrorReporter {
	public static final  ErrorReporter  reporter;
	private static PrintStream vrb = StdStreams.vrb;
	private PrintStream  errPrStream;
	private String  errorMsgFilePath = "rsc/ErrorMsg.txt";
	private JarFile jar;

	public  int  maxNofErrors;
	public  int  nofErrors, firstError, lastError;

	public  int  lineNr, column; // error position in source file
	public  boolean  printErrorPos;

	static {//Class constructor
		reporter = new ErrorReporter();
	}

	private  ErrorReporter() {
		clear();
		errPrStream = StdStreams.err;
		String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		String url = null;
		try {
			url = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			vrb.println("Error message file cannot be loaded, URL syntax error ");
		}
//		errPrStream.println(url);
		while (url.contains("..")) {	// used when eclipse launched from within eclipse workspace
			int index = url.indexOf("..");
			char ch = url.charAt(index - 1);
			int index1 = url.lastIndexOf(ch, index - 2);
			String p1 = url.substring(0, index1);
			String p2 = url.substring(index + 2);
			url = p1 + p2;
		}
		if (url.endsWith("jar")) { // used when running as an eclipse plugin 
			try {
				jar = new JarFile(url);
			} catch (IOException e) {
				vrb.println("Error message file cannot be loaded");
			}
		} else {
			if (url.endsWith("bin/")) { // used when started directly in eclipse (e.g. with the Testlauncher)
				url = url.substring(0, url.length() - 4);
			}
			errorMsgFilePath = url + errorMsgFilePath;
		}						
		this.maxNofErrors = Integer.MAX_VALUE;
//		errPrStream.println(errorMsgFilePath);
	}

	public void setMaxNrOfErrors(int maxNofErrors) {
		this.maxNofErrors = maxNofErrors;
	}

	public void setErrorPos(int lineNr, int column) {
		this.lineNr = lineNr; this.column = column; 
		printErrorPos = true;
	}

	public void clear() {
		nofErrors = 0;  firstError = 0;  lastError = 0;
		printErrorPos = false;
	}

	public void error(int errNr) {
		error(errNr, null);
	}

	public void error(String errMsg) {
		report(0, errMsg);
	}
	
	public void error(int errNr, String additionalInfo) {
		String msg = "";
		BufferedReader br = null;
		boolean found = false;
		String[] elements = new String[0];
		if (jar != null) {
			ZipEntry entry = jar.getEntry(errorMsgFilePath);
			InputStreamReader isr = null;
			try {
				isr = new InputStreamReader(jar.getInputStream(entry));
				br = new BufferedReader(isr);
				br.readLine();//overread header
				while (!found && br.ready()){
					String line = br.readLine();
					elements = line.split(";");
					if(elements.length > 1 && Integer.decode(elements[0]) == errNr){
						found = true;
					}
				}
				br.close();
				isr.close();
			} catch (IOException e) {
				e.printStackTrace(errPrStream);
				try {
					br.close();
					isr.close();
				} catch (IOException e1) {
					e.printStackTrace(errPrStream);
				}
			}
		} else {
			File file = new File(errorMsgFilePath);
			if (file.exists()) {
				FileReader fr = null;
				try {
					//search error message in the message file
					fr = new FileReader(file);
					br = new BufferedReader(fr);
					br.readLine();//overread header
					while (!found && br.ready()){
						String line = br.readLine();
						elements = line.split(";");
						if(elements.length > 1 && Integer.decode(elements[0]) == errNr){
							found = true;
						}
					}
					br.close();
					fr.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace(errPrStream);
				} catch (IOException e) {
					e.printStackTrace(errPrStream);
					try {
						fr.close();
						br.close();
					} catch (IOException e1) {
						e.printStackTrace(errPrStream);
					}
				}
			}
		}

		if (found) {
			if (elements.length > 1) msg += elements[1];
			if (additionalInfo != null) msg += " (" + additionalInfo + ").";
			if (elements.length > 2) msg += " Possible solution: " + elements[2] + ".";
		} else {
			if(additionalInfo != null)
				msg = additionalInfo;
		}
		report(errNr, msg);
	}

	private void report(int errNr, String errMsg) {
		if (nofErrors == 0) firstError = errNr;
		lastError = errNr;
		nofErrors++;
		if (nofErrors < maxNofErrors && errPrStream != null) {
			if (printErrorPos) {
				errPrStream.print("line ");	errPrStream.print(lineNr);
				errPrStream.print(" col ");	errPrStream.print(column);
				printErrorPos = false;
			}
			errPrStream.print("[ERROR #");
			if (errNr != 0)   errPrStream.print(errNr);
			if (errMsg != null)  errPrStream.print("] " + errMsg);
			errPrStream.println();
		}
	}
	
}
