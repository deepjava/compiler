package ch.ntb.inf.deep.dwarf;

import java.nio.ByteBuffer;

import ch.ntb.inf.deep.classItems.ICdescAndTypeConsts;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.dwarf.die.DebugInformationEntry;
import ch.ntb.inf.deep.dwarf.die.DwAtType;
import ch.ntb.inf.deep.dwarf.die.DwAteType;
import ch.ntb.inf.deep.dwarf.die.DwFormType;
import ch.ntb.inf.deep.dwarf.die.DwTagType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class BaseTypeDIE extends DebugInformationEntry {

	private static final int abbrev_code = 3;

	private final Type type;
	private final byte encoding;
	private final String name;

	public BaseTypeDIE(Type type) {
		this.type = type;
		switch (type.category) {
		case ICdescAndTypeConsts.tcPrimitive:
			switch (type.name.charAt(0)) {
			case 'V':
				this.encoding = DwAteType.DW_ATE_signed.value();				
				name = "void";
				break;
			case 'Z':
				this.encoding = DwAteType.DW_ATE_boolean.value();
				name = "boolean";
				break;
			case 'B':
				name = "byte";
				this.encoding = DwAteType.DW_ATE_signed_char.value();
				break;
			case 'S':
				this.encoding = DwAteType.DW_ATE_signed.value();
				name = "short";
				break;
			case 'C':
				this.encoding = DwAteType.DW_ATE_signed_char.value();
				name = "char";
				break;
			case 'I':
				this.encoding = DwAteType.DW_ATE_signed.value();
				name = "int";
				break;
			case 'J':
				this.encoding = DwAteType.DW_ATE_signed.value();
				name = "long";
				break;
			case 'F':
				this.encoding = DwAteType.DW_ATE_float.value();
				name = "float";
				break;
			case 'D':
				this.encoding = DwAteType.DW_ATE_float.value();
				name = "double";
				break;
			default:
				throw new RuntimeException("Unknown Base Type found");
			}
			break;
		case ICdescAndTypeConsts.tcRef:
			name = type.name.toString();
			this.encoding = DwAteType.DW_ATE_address.value();
			break;
		case ICdescAndTypeConsts.tcArray:
			throw new NotImplementedException();
			// break;
		default:
			throw new RuntimeException("Unknown Base Type found");
		}
	}

	@Override
	public void serialize(ByteBuffer buf, int debugLinePosition) {
		this.baseAddress = buf.position();
		Utils.writeUnsignedLeb128(buf, abbrev_code);
		buf.put((byte)type.getTypeSize());
		buf.put(encoding);
		buf.put(Utils.serialize(name));
	}

	public static void serializeAbbrev(ByteBuffer buf) {
		Utils.writeUnsignedLeb128(buf, abbrev_code); // abbrev_code ULEB128
		Utils.writeUnsignedLeb128(buf, DwTagType.DW_TAG_base_type.value());
		buf.put((byte) 0); // hasChilden (1 Byte) DW_CHILDREN_yes

		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_byte_size.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_data1.value());

		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_encoding.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_data1.value());

		Utils.writeUnsignedLeb128(buf, DwAtType.DW_AT_name.value());
		Utils.writeUnsignedLeb128(buf, DwFormType.DW_FORM_string.value());

		// Ending of Attribute List
		Utils.writeUnsignedLeb128(buf, 0);
		Utils.writeUnsignedLeb128(buf, 0);
	}
}
