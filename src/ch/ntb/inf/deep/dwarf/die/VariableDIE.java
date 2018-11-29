package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.LocalVar;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.dwarf.DwarfExpression;
import ch.ntb.inf.deep.dwarf.SimpleExpression;

public class VariableDIE extends DebugInformationEntry {

	private final String name;
	private final TypeDIE type;
	private final DwarfExpression location;

	protected VariableDIE(LocalVar localVar, DebugInformationEntry parent) {
		super(parent, DwTagType.DW_TAG_variable);
		this.name = localVar.name.toString();
		this.type = TypeDIE.getType((Type) localVar.type, parent.getRoot());
		this.location = new SimpleExpression(DwOpType.DW_OP_reg0);
	}

	@Override
	protected void serializeDie(DWARF dwarf) {
		dwarf.add(DwAtType.DW_AT_name, name);
		dwarf.add(type);
		dwarf.add(DwAtType.DW_AT_location, location);
	}
}
