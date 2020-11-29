package org.deepjava.dwarf;

import java.nio.ByteBuffer;

public class SymbolTableEntry {
	public final int name;
	public final int value;
	public final int size;
	public final SymbolType type;
	public final SymbolBind bind;
	public final Visibility visibility;
	public final short shndx;

	public SymbolTableEntry(ByteBuffer buf) {
		this.name = buf.getInt();
		this.value = buf.getInt();
		this.size = buf.getInt();

		int info = Byte.toUnsignedInt(buf.get());
		this.type = SymbolType.valueOf(info & 0xF);
		this.bind = SymbolBind.valueOf(info >> 4);
		int other = Byte.toUnsignedInt(buf.get());
		this.visibility = Visibility.valueOf(other & 0x3);
		this.shndx = buf.getShort();
	}

	public SymbolTableEntry() {
		this.name = 0;
		this.value = 0;
		this.size = 0;
		this.type = SymbolType.NOTYPE;
		this.bind = SymbolBind.LOCAL;
		this.visibility = Visibility.DEFAULT;
		this.shndx = 0;
	}

	public SymbolTableEntry(int name, int value, int size, SymbolType type, SymbolBind bind, Visibility visibility,
			short shndx) {
		this.name = name;
		this.value = value;
		this.size = size;
		this.type = type;
		this.bind = bind;
		this.visibility = visibility;
		this.shndx = shndx;
	}

	public void serialize(ByteBuffer buf) {
		buf.putInt(name);
		buf.putInt(value);
		buf.putInt(size);
		int info = ((bind.value) << 4) + ((type.value) & 0xf);
		buf.put((byte) info);
		buf.put((byte) visibility.value);
		buf.putShort(shndx);

	}
}
