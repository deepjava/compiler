package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.LocalVar;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.dwarf.location.DwarfExpression;
import ch.ntb.inf.deep.dwarf.location.SimpleExpression;

public class ParameterDIE extends DebugInformationEntry {

	private final String name;
	private final TypeDIE type;
	private final DwarfExpression location;

	protected ParameterDIE(LocalVar localVar, DebugInformationEntry parent) {
		super(parent, DwTagType.DW_TAG_formal_parameter);
		this.name = localVar.name.toString();
		this.type = getType((Type) localVar.type, parent);
		this.location = new SimpleExpression(DwOpType.DW_OP_reg1);
	}

	@Override
	public void serializeDie(DWARF dwarf) {
		dwarf.add(DwAtType.DW_AT_name, name);
		dwarf.add(type);
		dwarf.addByte(DwAtType.DW_AT_artificial, DwFormType.DW_FORM_flag, (byte) 1);
		dwarf.add(DwAtType.DW_AT_location, location);
	}
}
