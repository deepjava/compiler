package ch.ntb.inf.deep.dwarf.location;

import java.nio.ByteBuffer;

import ch.ntb.inf.deep.classItems.LocalVarRange;
import ch.ntb.inf.deep.dwarf.die.DwOpType;

public class LocationEntry implements LocationListEntry {

	private final int startOffset;
	private final int endOffset;
	private final byte operation;

	public LocationEntry(LocalVarRange range) {
		this.startOffset = range.ssaStart.machineCodeOffset * 4;
		this.endOffset = (range.ssaEnd.machineCodeOffset + 1) * 4;
		if (range.reg >= 0 && range.reg <= 31) {
			this.operation = (byte) (DwOpType.DW_OP_reg0.value() + range.reg);
		} else {
			// TODO: reinsert Exception if illegal Register!
//			throw new IllegalArgumentException("Registers only between 0 to 31 are supported!");
			this.operation = DwOpType.DW_OP_nop.value();

		}
	}

	@Override
	public void serialize(ByteBuffer buf) {
		buf.putInt(startOffset);
		buf.putInt(endOffset);
		buf.putShort((short) 1); // Expression length DWARF 4 Chapter 7.7.3
		buf.put(operation);
	}
}
