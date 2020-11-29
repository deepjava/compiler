package org.deepjava.dwarf;

public enum SymbolType {
	NOTYPE(0, "NOTYPE"), OBJECT(1, "OBJECT"), FUNC(2, "FUNC"), SECTION(3, "SECTION"), FILE(4, "FILE"),
	COMMON(5, "COMMON"), TLS(6, "TLS"), LOOS(10, "LOOS"), HIOS(12, "HIOS"), LOPROC(13, "LOPROC"),
	HIPROC(15, "HIPROC"), ASDF(18, "ASDF");

	int value;
	String desc;

	SymbolType(int value, String desc) {
		this.value = value;
		this.desc = desc;
	}

	static SymbolType valueOf(int value) {
		for (SymbolType type : values()) {
			if (type.value == value) {
				return type;
			}
		}
		throw new IllegalArgumentException("SymbolType type: " + value);
	}

	@Override
	public String toString() {
		return desc;
	}
}