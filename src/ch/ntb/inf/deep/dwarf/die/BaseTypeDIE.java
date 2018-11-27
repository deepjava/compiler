package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.Type;

public class BaseTypeDIE extends DebugInformationEntry {

	final byte sizeInBytes;
	final DwAteType encoding;
	final String name;

	public BaseTypeDIE(Type type, DebugInformationEntry parent) {
		super(parent, true); // Insert at First Position to be sure it is serialized before its depending DIE
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
	public void accept(DieVisitor visitor) {
		visitor.visit(this);
	}
}
