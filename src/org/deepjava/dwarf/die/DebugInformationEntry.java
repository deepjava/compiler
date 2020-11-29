package org.deepjava.dwarf.die;

import java.util.ArrayList;
import java.util.List;

import org.deepjava.classItems.Type;
import org.deepjava.dwarf.Utils;

public abstract class DebugInformationEntry {
	private static int abbrevCodeCount = 1;

	private List<DebugInformationEntry> children;
	private DebugInformationEntry parent;

	protected final DwTagType tagType;
	protected final int abbrevCode;
	protected int baseAddress = -1;

	protected DebugInformationEntry(DebugInformationEntry parent, DwTagType type) {
		abbrevCode = abbrevCodeCount;
		abbrevCodeCount++;
		this.parent = parent;
		this.tagType = type;
		children = new ArrayList<>();
		if (parent != null) {
			parent.children.add(this);
		}
	}

	protected static TypeDIE getType(Type type, DebugInformationEntry parent) {
		if (type.dwarfDIE == null) {
			TypeDIE.generateNewTypeDIE(type, parent);
		}
		return type.dwarfDIE;
	}

	public void serialize(DWARF dwarf) {
		baseAddress = dwarf.debug_info.position();
		Utils.writeUnsignedLeb128(dwarf.debug_info, abbrevCode);
		Utils.writeUnsignedLeb128(dwarf.debug_abbrev, abbrevCode); // abbrev_code ULEB128
		Utils.writeUnsignedLeb128(dwarf.debug_abbrev, tagType.value());
		dwarf.debug_abbrev.put((byte) (hasChildren() ? 1 : 0)); // hasChilden (1 Byte) DW_CHILDREN_yes

		serializeDie(dwarf);

		dwarf.addDieEnd();

		addChildren(dwarf);
	}

	protected void addChildren(DWARF dwarf) {
		if (hasChildren()) {
			for (DebugInformationEntry child : getChildren()) {
				child.serialize(dwarf);
			}
			// Last sibling terminated by a null entry
			Utils.writeUnsignedLeb128(dwarf.debug_info, 0);
		}
	}

	protected abstract void serializeDie(DWARF dwarf);

	public DebugInformationEntry getParent() {
		return parent;
	}

	public List<DebugInformationEntry> getChildren() {
		return children;
	}

	public DebugInformationEntry getRoot() {
		if (getParent() == null) {
			return this;
		} else {
			return getParent().getRoot();
		}
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}
}
