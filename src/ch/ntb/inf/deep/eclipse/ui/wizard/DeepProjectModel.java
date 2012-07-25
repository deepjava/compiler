package ch.ntb.inf.deep.eclipse.ui.wizard;

import ch.ntb.inf.deep.config.Board;
import ch.ntb.inf.deep.config.Library;
import ch.ntb.inf.deep.config.OperatingSystem;
import ch.ntb.inf.deep.config.Programmer;

public class DeepProjectModel {

	private Library lib;
	private Board board;
	private Programmer programmer;
	private OperatingSystem os;

	public void setLibrary(Library lib) {
		this.lib = lib;
	}
	
	public Library getLibrary() {
		return this.lib;
	}

	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public Programmer getProgrammer() {
		return programmer;
	}

	public void setProgrammer(Programmer programmer) {
		this.programmer = programmer;
	}

	public OperatingSystem getOs() {
		return os;
	}

	public void setOs(OperatingSystem os) {
		this.os = os;
	}
	
}
