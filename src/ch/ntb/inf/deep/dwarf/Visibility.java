package ch.ntb.inf.deep.dwarf;

public enum Visibility {
	DEFAULT(0, "DEFAULT"), INTERNAL(1, "INTERNAL"), HIDDEN(2, "HIDDEN"), PROTECTED(3, "PROTECTED"),
	EXPORTED(4, "EXPORTED"), SINGLETON(5, "SINGLETON"), ELIMINATED(6, "ELIMINATED");

	int value;
	String desc;

	private Visibility(int value, String desc) {
		this.value = value;
		this.desc = desc;
	}

	static Visibility valueOf(int value) {
		for (Visibility type : values()) {
			if (type.value == value) {
				return type;
			}
		}
		throw new IllegalArgumentException("Visibilit type: " + value);
	}

	@Override
	public String toString() {
		return desc;
	}
}