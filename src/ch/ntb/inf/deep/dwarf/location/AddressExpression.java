package ch.ntb.inf.deep.dwarf.location;

import java.nio.ByteBuffer;

import ch.ntb.inf.deep.dwarf.die.DwOpType;

public class AddressExpression implements DwarfExpression {

	private final int address;

	public AddressExpression(int address) {
		this.address = address;
	}

	@Override
	public void serialize(ByteBuffer buf) {
		buf.put((byte) 5); // Length Address Operation + 4 Bytes Describing Address
		buf.put(DwOpType.DW_OP_addr.value());
		buf.putInt(address);
	}
}
