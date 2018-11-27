package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.dwarf.Utils;

public class BaseTypeDIE extends DebugInformationEntry {

	final byte sizeInBytes;
	final DwAteType encoding;
	final String name;

	public BaseTypeDIE(Type type, DebugInformationEntry parent) {
		super(parent, DwTagType.DW_TAG_base_type, true); // Insert at First Position to be sure it is serialized before its depending DIE
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
	public void serializeDie(DieSerializer serialize) {
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwAtType.DW_AT_byte_size.value());
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwFormType.DW_FORM_data1.value());
		serialize.debug_info.put(sizeInBytes);

		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwAtType.DW_AT_encoding.value());
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwFormType.DW_FORM_data1.value());
		serialize.debug_info.put(encoding.value());

		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwAtType.DW_AT_name.value());
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwFormType.DW_FORM_string.value());
		serialize.debug_info.put(Utils.serialize(name));
	}
}
