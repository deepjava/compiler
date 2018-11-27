package ch.ntb.inf.deep.dwarf.die;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DieSerializer{
	
	public final ByteBuffer debug_info;
	public final ByteBuffer debug_abbrev;
	public final ByteBuffer debug_line;

	public DieSerializer(ByteOrder byteOrder) {
		debug_info = ByteBuffer.allocate(0xFFFF);
		debug_info.order(byteOrder);
		debug_abbrev = ByteBuffer.allocate(0xFFFF);
		debug_abbrev.order(byteOrder);
		debug_line = ByteBuffer.allocate(0xFFFF);
		debug_line.order(byteOrder);
	}
}
