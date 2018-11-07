package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;

import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.dwarf.die.DebugInformationEntry;
import ch.ntb.inf.deep.dwarf.die.DwAtType;
import ch.ntb.inf.deep.dwarf.die.DwFormType;
import ch.ntb.inf.deep.dwarf.die.DwTagType;

public class SubProgramDIE implements DebugInformationEntry {

	private static final int abbrev_code = 2;

	private final String name;
	private final int startAddress;
	private final int endAddress;

	public SubProgramDIE(Method method) {
		this.name = method.name.toString();
		this.startAddress = method.address;

		// TODO: find end Address
		if (method.next != null) {
			this.endAddress = method.next.address; // - this.startAddress;
		} else {
			this.endAddress = 0;
		}
	}

	@Override
	public boolean hasChildern() {
		return false;
	}

	@Override
	public void serialize(ByteBuffer buf, int debugLinePosition) {
		Utils.writeUnsignedLeb128(buf, abbrev_code);
		buf.put(Utils.serialize(name));
		buf.putInt(startAddress);
		buf.putInt(endAddress);
	}

	@Override
	public void serializeAbbrev(ByteBuffer buf) {
		Utils.writeUnsignedLeb128(buf, abbrev_code); // abbrev_code ULEB128
		Utils.writeUnsignedLeb128(buf, DwTagType.DW_TAG_subprogram.value());
		buf.put((byte) (hasChildern() ? 1 : 0)); // hasChilden (1 Byte) DW_CHILDREN_yes

		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_name.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_string.value());

		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_low_pc.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_addr.value());

		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_high_pc.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_addr.value());

		// Ending of Attribute List
		Utils.writeUnsignedLeb128(buf, 0);
		Utils.writeUnsignedLeb128(buf, 0);
	}

}
