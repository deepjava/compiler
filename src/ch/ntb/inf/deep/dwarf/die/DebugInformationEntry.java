package ch.ntb.inf.deep.dwarf.die;

import java.util.ArrayList;
import java.util.List;

import ch.ntb.inf.deep.dwarf.Utils;
import ch.ntb.inf.deep.classItems.Type;

public abstract class DebugInformationEntry {
	private static int abbrevCodeCount = 1;

	private List<DebugInformationEntry> children;
	private DebugInformationEntry parent;

	protected final DwTagType type;
	public final int abbrevCode;
	public int baseAddress = -1;

	protected DebugInformationEntry(DebugInformationEntry parent, DwTagType type) {
		this(parent, type, false);
	}

	protected DebugInformationEntry(DebugInformationEntry parent, DwTagType type, boolean insertAtBeginn) {
		abbrevCode = abbrevCodeCount;
		abbrevCodeCount++;
		this.parent = parent;
		this.type = type;
		children = new ArrayList<>();
		if (parent != null) {
			int insertIndex = insertAtBeginn ? 0 : parent.children.size();
			parent.children.add(insertIndex, this);
		}
	}

	protected static TypeDIE getType(Type type, DebugInformationEntry parent) {
		if (type.dwarfDIE == null || parent.getRoot() != type.dwarfDIE.getRoot()) {
			TypeDIE.generateNewTypeDIE(type, parent);
		}
		return type.dwarfDIE;
	}

	public void serialize(DieSerializer serializer) {
		baseAddress = serializer.debug_info.position();
		Utils.writeUnsignedLeb128(serializer.debug_info, abbrevCode);
		Utils.writeUnsignedLeb128(serializer.debug_abbrev, abbrevCode); // abbrev_code ULEB128
		Utils.writeUnsignedLeb128(serializer.debug_abbrev, type.value());
		serializer.debug_abbrev.put((byte) (hasChildren() ? 1 : 0)); // hasChilden (1 Byte) DW_CHILDREN_yes

		serializeDie(serializer);

		// Ending of Attribute List
		Utils.writeUnsignedLeb128(serializer.debug_abbrev, 0);
		Utils.writeUnsignedLeb128(serializer.debug_abbrev, 0);

		if (hasChildren()) {
			for (DebugInformationEntry child : getChildren()) {
				child.serialize(serializer);
			}
			// Last sibling terminated by a null entry
			Utils.writeUnsignedLeb128(serializer.debug_info, 0);
		}
	}

	protected abstract void serializeDie(DieSerializer serializer);

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
