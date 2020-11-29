package org.deepjava.dwarf.die;

import org.deepjava.classItems.Type;

public class ArrayTypeDIE extends TypeDIE {

	private final TypeDIE baseTypeDIE;
	
	protected ArrayTypeDIE(Type type, DebugInformationEntry parent) {
		super(parent, DwTagType.DW_TAG_array_type);
		Type baseType = Type.getTypeByDescriptor(type.name.substring(1));
		baseTypeDIE = getType((Type) baseType, parent.getRoot());	
		type.dwarfDIE = new RefTypeDIE(type, parent, this);
		new ArraySubRangeType(this, baseTypeDIE);
	}

	@Override
	protected void serializeDie(DWARF dwarf) {
		dwarf.add(baseTypeDIE);
	}
}
