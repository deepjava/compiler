package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.ICdescAndTypeConsts;
import ch.ntb.inf.deep.classItems.StdConstant;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.strings.HString;

public class ConstantDIE extends DebugInformationEntry {

	private final String name;
	private final long value;
	private final TypeDIE typeDIE;

	protected ConstantDIE(StdConstant constant, HString name, DebugInformationEntry parent) {
		super(parent, DwTagType.DW_TAG_constant);
		this.name = name.toString();
		Type type = (Type) constant.type;
		typeDIE = TypeDIE.getType(type, parent);
		if (type.name.charAt(0) == ICdescAndTypeConsts.tdLong || type.name.charAt(0) == ICdescAndTypeConsts.tdDouble) {
			value = ((long)(constant.valueH)<<32) | (constant.valueL&0xFFFFFFFFL);
		} else {
			value = constant.valueH;
		}
	}

	@Override
	protected void serializeDie(DWARF dwarf) {
		dwarf.add(DwAtType.DW_AT_name, name);
		dwarf.addLong(DwAtType.DW_AT_const_value, DwFormType.DW_FORM_data8, value);
		dwarf.add(typeDIE);
	}
}
