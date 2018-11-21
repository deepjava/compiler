package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;

public interface DwarfExpression {

	public void serialize(ByteBuffer buf);
}
