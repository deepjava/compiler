package org.deepjava.dwarf.location;

import java.nio.ByteBuffer;

public class EndOfListEntry implements LocationListEntry {

	@Override
	public void serialize(ByteBuffer buf) {
		buf.putInt(0);
		buf.putInt(0);
	}
}
