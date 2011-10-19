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
		if(System.getProperty("os.name").contains("Windows")){
			home = getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(6);//get jar name
			//getClass().getProtectionDomain().getCodeSource().getLocation().toString() returns file:/PATHTOTHEJAR
			//we need only PATHTOTHEJAR without file:/
		}else{
			home = getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(5);//get jar name
			//getClass().getProtectionDomain().getCodeSource().getLocation().toString() returns file:/PATHTOTHEJAR
			//we need /PATHTOTHEJAR without file:
		}
		if(home.endsWith("jar")){//used when launched as a builded Plugin 
				try {
					jar = new JarFile(home);
				} catch (IOException e) {
					e.printStackTrace(errPrStream);
				}
			
		}else{
			if(home.endsWith("bin/")){//used when launched with Testlauncher
				home = home.substring(0, home.length() - 4);
			}
			errorMsgFilePath = home + errorMsgFilePath; // used when launched from Plugin-Development environment 
		}
						
		this.maxNofErrors = Integer.MAX_VALUE;
		// printErrorPos = false;
	}

//	public void setPrintStream(PrintStream errStream) {
//		errPrStream = errStream;
//	}

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
				msg += elements[1] + ",";
			}
			if(additionalInfo != null){
				msg += " " + additionalInfo + ".";
			}
			if(elements.length > 2){
				msg += " possible solution: " + elements[2] + ".";
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
			errPrStream.print(" error: ");
			if (errNr != 0)   errPrStream.print(errNr);
			if (errMsg != null)  errPrStream.print(" \"" + errMsg + '\"');
			errPrStream.println();
		}
//		assert false;
	}
	
}
