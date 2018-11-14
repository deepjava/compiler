package ch.ntb.inf.deep.dwarf.die;

import java.nio.ByteBuffer;

/**
 * Debugging Information Entry (DIE) as Described in DWARF
 * @author Martin
 *
 */
public abstract class DebugInformationEntry {
	public int baseAddress;
	
	public abstract void serialize(ByteBuffer buf, int debugLinePosition);
}
