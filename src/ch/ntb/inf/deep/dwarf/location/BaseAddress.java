package ch.ntb.inf.deep.dwarf.location;

import java.nio.ByteBuffer;

public class BaseAddress implements LocationListEntry {

	private final int address;

	public BaseAddress(int address) {
		this.address = address;
	}

	@Override
	public void serialize(ByteBuffer buf) {
		buf.putInt(0xFFFFFFFF);
		buf.putInt(address);
	}
}
