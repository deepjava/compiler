package org.deepjava.dwarf.location;

import java.nio.ByteBuffer;

import org.deepjava.dwarf.die.DwOpType;

public class AddressLocation {

	private final int address;

	public AddressLocation(int address) {
		this.address = address;
	}

	public void serialize(ByteBuffer buf) {
		buf.put((byte) 5); // Length Address Operation + 4 Bytes Describing Address
		buf.put(DwOpType.DW_OP_addr.value());
		buf.putInt(address);
	}
}
