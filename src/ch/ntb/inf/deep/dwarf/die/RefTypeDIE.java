package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.Type;

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
	protected void serializeDie(DieSerializer serializer) {
		serializer.addByte(DwAtType.DW_AT_byte_size, DwFormType.DW_FORM_data1, (byte) 4); // 32 Bit Pointer
		serializer.addInt(DwAtType.DW_AT_type, DwFormType.DW_FORM_ref4, baseType.baseAddress - getRoot().baseAddress);
	}
}
