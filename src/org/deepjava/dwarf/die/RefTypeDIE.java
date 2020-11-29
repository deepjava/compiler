package org.deepjava.dwarf.die;

import org.deepjava.classItems.Type;

public class RefTypeDIE extends TypeDIE {

	private final TypeDIE baseType;

	protected RefTypeDIE(Type type, DebugInformationEntry parent, TypeDIE baseType) {
		super(parent, DwTagType.DW_TAG_pointer_type);
		type.dwarfDIE = this;
		this.baseType = baseType;
	}

	protected RefTypeDIE(Type type, DebugInformationEntry parent) {
		super(parent, DwTagType.DW_TAG_pointer_type);
		type.dwarfDIE = this;
		this.baseType = null;
	}

	@Override
	protected void serializeDie(DWARF dwarf) {
		dwarf.addByte(DwAtType.DW_AT_byte_size, DwFormType.DW_FORM_data1, (byte) 4); // 32 Bit Pointer
		dwarf.add(baseType);
	}

	public TypeDIE getBaseType() {
		return baseType;
	}
}
