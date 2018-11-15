package ch.ntb.inf.deep.dwarf.die;

import java.util.ArrayList;
import java.util.List;

import ch.ntb.inf.deep.dwarf.DieVisitor;

public abstract class DebugInformationEntry {
	private static int abbrevCodeCount = 1;

	private List<DebugInformationEntry> children;
	private DebugInformationEntry parent;
	public final int abbrevCode;
	public int baseAddress;

	protected DebugInformationEntry(DebugInformationEntry parent) {
		this(parent, false);
	}

	protected DebugInformationEntry(DebugInformationEntry parent, boolean insertAtBeginn) {
		abbrevCode = abbrevCodeCount;
		abbrevCodeCount++;
		this.parent = parent;
		children = new ArrayList<>();
		if (parent != null) {
			int insertIndex = insertAtBeginn ? 0 : parent.children.size();
			parent.children.add(insertIndex, this);
		}
	}

	public abstract void accept(DieVisitor visitor);

	public DebugInformationEntry getParent() {
		return parent;
	}

	public List<DebugInformationEntry> getChildren() {
		return children;
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}
}
