package ch.ntb.inf.deep.dwarf.die;

import java.nio.ByteBuffer;
import ch.ntb.inf.deep.dwarf.Utils;

public class AbbrevEntry {
	public DwAtType at;
	public DwFormType form;

	public void parse(ByteBuffer buffer) {
		at = DwAtType.byValue((int) Utils.readUnsignedLeb128(buffer));
		form = DwFormType.byValue((int) Utils.readUnsignedLeb128(buffer));
	}

	public boolean isNull() {
		return (at == null || at.value() == 0) && (form == null || form.value() == 0);
	}
}