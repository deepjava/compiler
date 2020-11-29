package org.deepjava.dwarf.location;

import java.nio.ByteBuffer;

public interface LocationListEntry {
	
	public void serialize(ByteBuffer buf);
}
