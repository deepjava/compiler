package ch.ntb.inf.deep.dwarf.die;

import java.nio.ByteBuffer;

import ch.ntb.inf.deep.dwarf.DieVisitor;

public abstract class DebugInformationEntry {
	private static int abbrevCodeCount = 1;
	public final int abbrevCode;
	public int baseAddress;

	public boolean hasChildren;

	protected DebugInformationEntry(boolean hasChildren) {
		abbrevCode = abbrevCodeCount;
		abbrevCodeCount++;
		
		this.hasChildren = hasChildren;
	}
	
//	public abstract void serialize(ByteBuffer debug_info, ByteBuffer debug_abbrev, ByteBuffer debug_line);
	public abstract void accept(DieVisitor visitor);;
}
