package ch.ntb.inf.deep.host;
/**
changes:
<br>09-04-22, NTB/ED	error(int errNr, String errMsg): printing errNr, errMsg
<br>09-03-23, NTB/ED	extension of error(int errNr, String errMsg)
<br>05-02-11, NTB/ED	creation
*/
import java.io.PrintStream;

/**
  * Does the common error reporting (implemented as a singleton)
 */
public class ErrorReporter {
	public static final  ErrorReporter  reporter;
	private  PrintStream  errPrStream;

	public  int  maxNofErrors;
	public  int  nofErrors, firstError, lastError;

	public  int  lineNr, column; // error position in source file
	public  boolean  printErrorPos;

	static {//Class constructor
		reporter = new ErrorReporter();
	}

	private  ErrorReporter() {
		clear();
		errPrStream = System.out;
		this.maxNofErrors = Integer.MAX_VALUE;
		// printErrorPos = false;
	}

	public void setPrintStream(PrintStream errStream) {
		errPrStream = errStream;
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
		error(0, errMsg);
	}

	public void error(int errNr, String errMsg) {
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
//			errPrStream.println();
		}
//		assert false;
	}
	
	public void print(String string){
		errPrStream.print(string);
	}
	public void println(){
		errPrStream.println();
	}
}
