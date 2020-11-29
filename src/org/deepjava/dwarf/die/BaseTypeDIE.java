package org.deepjava.dwarf.die;

import org.deepjava.classItems.Type;

public class BaseTypeDIE extends TypeDIE {

	private final byte sizeInBytes;
	private final DwAteType encoding;
	private final String name;

	protected BaseTypeDIE(Type type, DebugInformationEntry parent) {
		super(parent, DwTagType.DW_TAG_base_type);
		type.dwarfDIE = this;
		this.sizeInBytes = (byte) type.getTypeSize();
		String typeName = "";
		if (type.category == 'L') {
			typeName += type.category;
		}
		typeName += type.name;
		this.name = getTypeName(typeName);
		this.encoding = getTypeEncoding(typeName);
	}

	private DwAteType getTypeEncoding(String typeName) {
		switch (typeName.charAt(0)) {
		case 'V':
		case 'S':
		case 'J':
		case 'I':
			return DwAteType.DW_ATE_signed;
		case 'Z':
			return DwAteType.DW_ATE_boolean;
		case 'B':
		case 'C':
			return DwAteType.DW_ATE_signed_char;
		case 'F':
		case 'D':
			return DwAteType.DW_ATE_float;
		case 'L':
		case '[':
			return DwAteType.DW_ATE_address;
		default:
			throw new RuntimeException("Unknown Base Type found");
		}
	}

	private String getTypeName(String typeName) {
		switch (typeName.charAt(0)) {
		case 'V':
			return "void";
		case 'Z':
			return "boolean";
		case 'B':
			return "byte";
		case 'S':
			return "short";
		case 'C':
			return "char";
		case 'I':
			return "int";
		case 'J':
			return "long";
		case 'F':
			return "float";
		case 'D':
			return "double";
		case 'L':
			return typeName.substring(1).replace(";", "");
		case '[':
			return getTypeName(typeName.substring(1)) + "[]";
		default:
			throw new RuntimeException("Unknown Base Type found");
		}
	}

	@Override
	public void serializeDie(DWARF dwarf) {
		dwarf.addByte(DwAtType.DW_AT_byte_size, DwFormType.DW_FORM_data1, sizeInBytes);
		dwarf.addByte(DwAtType.DW_AT_encoding, DwFormType.DW_FORM_data1, encoding.value());
		dwarf.add(DwAtType.DW_AT_name, name);
	}
}
