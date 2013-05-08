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
