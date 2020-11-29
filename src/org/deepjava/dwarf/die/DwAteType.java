package org.deepjava.dwarf.die;

public enum DwAteType {
	DW_ATE_address(1),
	DW_ATE_boolean(2),
	DW_ATE_complex_float(3),
	DW_ATE_float(4),
	DW_ATE_signed(5),
	DW_ATE_signed_char(6),
	DW_ATE_unsigned(7),
	DW_ATE_unsigned_char(8);
	
	private byte value;

	DwAteType(int value) {
		this.value = (byte)value;
	}
	
	public static DwAteType byValue(int value) {
		for (DwAteType a : DwAteType.values()) {
			if (a.value == value)
				return a;
		}

		return null;
	}
	
	public byte value() {
		return this.value;
	}
}
