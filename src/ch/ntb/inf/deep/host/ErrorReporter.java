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
/**
changes:
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
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;


/**
  * Does the common error reporting (implemented as a singleton)
 */
public class ErrorReporter {
	public static final  ErrorReporter  reporter;
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
		String home = "";
		
		if(System.getProperty("os.name").contains("Windows")){ // Running on Microsoft Windows
			home = getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(6); // get jar name
			// we have to remove the first 6 characters of the returned _absolute_ path to the JAR file,
			// because the string starts with "file:/" which is not a valid file name!
			// Example: file:/I:\eclipse\..
		} else { // Running on Linux, Mac OS X or another UNIX system
			home = getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(5); // get jar name
			// we have to remove the first 5 characters of the returned _absolute_ path to the JAR file,
			// because the string starts with "file:" which is not a valid file name!
			// Example: file:/opt/eclipse/..
		}
		
		if(home.endsWith("jar")) { // used when running as an eclipse plugin 
				try {
					jar = new JarFile(home);
				} catch (IOException e) {
					e.printStackTrace(errPrStream);
				}
			
		} else {
			if(home.endsWith("bin/")) { // used when started directly in eclipse (e.g. with the Testlauncher)
				home = home.substring(0, home.length() - 4);
			}
			errorMsgFilePath = home + errorMsgFilePath;
		}
						
		this.maxNofErrors = Integer.MAX_VALUE;
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
		if(jar != null){
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
		}else{
			File file = new File(errorMsgFilePath);
			if(file.exists()){
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

		if(found){
			if(elements.length > 1){
				msg += elements[1];
			}
			if(additionalInfo != null){
				msg += " (" + additionalInfo + ").";
			}
			if(elements.length > 2){
				msg += " Possible solution: " + elements[2] + ".";
			}
			
		}else{
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
