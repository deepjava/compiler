package org.deepjava.dwarf.die;

public class ArraySubRangeType extends DebugInformationEntry {

	private final TypeDIE baseTypeDIE;
	
	protected ArraySubRangeType(ArrayTypeDIE parent, TypeDIE baseTypeDIE) {
		super(parent, DwTagType.DW_TAG_subrange_type);
		this.baseTypeDIE = baseTypeDIE;
	}

	@Override
	protected void serializeDie(DWARF dwarf) {
		dwarf.add(baseTypeDIE);
		dwarf.addByte(DwAtType.DW_AT_byte_size, DwFormType.DW_FORM_data1, (byte)1);		// Fixed length.
		dwarf.addByte(DwAtType.DW_AT_lower_bound, DwFormType.DW_FORM_data1, (byte)0);

		// Get the Array upper bound. Each Array has a 2 Byte Size Field on offset -6
		// To Read this Field we load the object address, subtract 8 and read on this address the last only 2 Bytes
		// To get the last Element we just need to subtract 1 from the Size!
		dwarf.addByte(DwAtType.DW_AT_upper_bound, DwFormType.DW_FORM_exprloc, (byte)7);
		dwarf.debug_info.put(DwOpType.DW_OP_push_object_address.value());
		dwarf.debug_info.put(DwOpType.DW_OP_lit8.value());
		dwarf.debug_info.put(DwOpType.DW_OP_minus.value());
		dwarf.debug_info.put(DwOpType.DW_OP_deref_size.value());
		dwarf.debug_info.put((byte)2);
		dwarf.debug_info.put(DwOpType.DW_OP_lit1.value());
		dwarf.debug_info.put(DwOpType.DW_OP_minus.value());

	}
}
