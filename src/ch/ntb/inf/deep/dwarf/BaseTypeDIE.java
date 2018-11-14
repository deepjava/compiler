package ch.ntb.inf.deep.dwarf;

import ch.ntb.inf.deep.classItems.ICdescAndTypeConsts;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.dwarf.die.DebugInformationEntry;
import ch.ntb.inf.deep.dwarf.die.DwAteType;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class BaseTypeDIE extends DebugInformationEntry {

	final Type type;
	final byte encoding;
	final String name;

	public BaseTypeDIE(Type type) {
		super(false);
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
			name = "[" + type.type.name + "]";
			this.encoding = DwAteType.DW_ATE_address.value();
			break;
		default:
			throw new RuntimeException("Unknown Base Type found");
		}
	}

	@Override
	public void accept(DieVisitor visitor) {
		visitor.visit(this);
	}
}
