package ch.ntb.inf.deep.dwarf.location;

import java.nio.ByteBuffer;

public interface DwarfExpression {

	public void serialize(ByteBuffer buf);
}
