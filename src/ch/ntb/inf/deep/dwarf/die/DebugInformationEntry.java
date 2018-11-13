package ch.ntb.inf.deep.dwarf.die;

import java.nio.ByteBuffer;

/**
 * Debugging Information Entry (DIE) as Described in DWARF
 * @author Martin
 *
 */
public interface DebugInformationEntry {
	public boolean hasChildern();
	public void serialize(ByteBuffer buf, int debugLinePosition);
	public void serializeAbbrev(ByteBuffer buf);
}
