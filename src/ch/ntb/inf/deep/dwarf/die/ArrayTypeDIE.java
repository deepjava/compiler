package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.Type;

public class ArrayTypeDIE extends TypeDIE {

	private final TypeDIE baseTypeDIE;
	
	protected ArrayTypeDIE(Type type, DebugInformationEntry parent) {
		super(parent, DwTagType.DW_TAG_array_type);
		Type baseType = Type.getTypeByDescriptor(type.name.substring(1));
		baseTypeDIE = getType((Type) baseType, parent.getRoot());	
		type.dwarfDIE = new RefTypeDIE(type, parent, this);
	}

	@Override
	protected void serializeDie(DWARF dwarf) {
		dwarf.add(baseTypeDIE);
		dwarf.addByte(DwAtType.DW_AT_lower_bound, DwFormType.DW_FORM_data1, (byte)0);
		dwarf.addByte(DwAtType.DW_AT_upper_bound, DwFormType.DW_FORM_data1, (byte)5);
	}
}
