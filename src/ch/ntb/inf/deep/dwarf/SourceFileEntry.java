package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;

public class SourceFileEntry {
	public final int No;
	public final int Dir;
	public final int Time;
	public final int Size;
	public final String filename;

	public SourceFileEntry(int No, String filename) {
		this.No = No;
		this.filename = filename;
		this.Dir = 0;
		this.Time = 0;
		this.Size = 0;
	}

	public SourceFileEntry(int No, ByteBuffer buf) {
		this.No = No;
		this.filename = Utils.parseString(buf);
		this.Dir = Utils.readUnsignedLeb128(buf);
		this.Time = Utils.readUnsignedLeb128(buf);
		this.Size = Utils.readUnsignedLeb128(buf);
	}

	public void serialize(ByteBuffer buf) {
		buf.put(Utils.serialize(filename));
		Utils.writeUnsignedLeb128(buf, Dir);
		Utils.writeUnsignedLeb128(buf, Time);
		Utils.writeUnsignedLeb128(buf, Size);
	}
}