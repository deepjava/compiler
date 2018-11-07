package ch.ntb.inf.deep.dwarf;

public enum SymbolBind {
	LOCAL(0, "LOCAL"), GLOBAL(1, "GLOBAL"), WEAK(2, "WEAK"), LOOS(10, "LOOS"), HIOS(12, "HIOS"),
	LOPROC(13, "LOPROC"), HIPROC(15, "HIPROC");

	int value;
	String desc;

	private SymbolBind(int value, String desc) {
		this.value = value;
		this.desc = desc;
	}

	static SymbolBind valueOf(int value) {
		for (SymbolBind type : values()) {
			if (type.value == value) {
				return type;
			}
		}
		throw new IllegalArgumentException("SymbolBind type: " + value);
	}

	@Override
	public String toString() {
		return desc;
	}
}