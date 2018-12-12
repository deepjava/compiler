package ch.ntb.inf.deep.dwarf.location;

import java.nio.ByteBuffer;

import ch.ntb.inf.deep.classItems.LocalVarRange;
import ch.ntb.inf.deep.dwarf.Utils;
import ch.ntb.inf.deep.dwarf.die.DwOpType;

public class LocationEntry implements LocationListEntry {

	private final int startOffset;
	private final int endOffset;
	private final int reg;
	private final int localVarOffset;

	public LocationEntry(LocalVarRange range, int localVarOffset) {
		this.startOffset = range.ssaStart.machineCodeOffset * 4;
		this.endOffset = (range.ssaEnd.machineCodeOffset + 1) * 4;
		this.localVarOffset = localVarOffset;
		if ((range.reg >= 0 && range.reg <= 31) || (range.reg >= 256)) {
			this.reg = range.reg;
		} else {
			throw new IllegalArgumentException(
					"Location can be between 0 and 31 for Registers or higher as 256 for Stack Slot but was "
							+ range.reg);
		}
	}

	@Override
	public void serialize(ByteBuffer buf) {
		buf.putInt(startOffset);
		buf.putInt(endOffset);
		if (reg <= 31) {
			buf.putShort((short) 1); // Expression length DWARF 4 Chapter 7.7.3
			buf.put((byte) (DwOpType.DW_OP_reg0.value() + reg));
		} else if (reg >= 256) {
			int lengthPosition = buf.position();
			buf.putShort((short) 0); // dummy Length. Write it later
			buf.put(DwOpType.DW_OP_breg13.value());
			Utils.writeSignedLeb128(buf, (reg - 256) * 4 + localVarOffset); // Stack Position Number
			buf.putShort(lengthPosition, (short) (buf.position() - lengthPosition - 2));
		}
	}
}
