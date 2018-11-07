package ch.ntb.inf.deep.dwarf.die;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ch.ntb.inf.deep.dwarf.Utils;

public class Abbrev {
	public int number;
	public int offset;
	public DwTagType tag;
	public boolean has_children;

	public List<AbbrevEntry> entries;

	public Abbrev() {
		entries = new ArrayList<>();
	}

	public void parse(ByteBuffer buffer) {
		offset = buffer.position();
		number = (int) Utils.readUnsignedLeb128(buffer);
		if (number == 0)
			return;

		tag = DwTagType.byValue((int) Utils.readUnsignedLeb128(buffer));
		has_children = !(buffer.get() == 0);

		while (true) {
			AbbrevEntry entry = new AbbrevEntry();
			entry.parse(buffer);

			if (entry.isNull())
				break;

			entries.add(entry);
		}
	}

	public List<AbbrevEntry> getEntries() {
		return entries;
	}
}
