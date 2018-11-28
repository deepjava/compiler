package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.LocalVar;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.dwarf.DwarfExpression;
import ch.ntb.inf.deep.dwarf.SimpleExpression;

public class VariableDIE extends DebugInformationEntry {

	public final String name;
	public final TypeDIE type;
	public final DwarfExpression expression;

	protected VariableDIE(LocalVar localVar, DebugInformationEntry parent) {
		super(parent, DwTagType.DW_TAG_formal_parameter);
		this.name = localVar.name.toString();
		this.type = getType((Type) localVar.type, parent);
		this.expression = new SimpleExpression(DwOpType.DW_OP_reg0);
	}

	@Override
	public void serializeDie(DieSerializer serialize) {
		serialize.add(DwAtType.DW_AT_name, name);
		serialize.addInt(DwAtType.DW_AT_type, DwFormType.DW_FORM_ref4, type.baseAddress - getRoot().baseAddress);

		serialize.addByte(DwAtType.DW_AT_artificial, DwFormType.DW_FORM_flag, (byte) 1);
		serialize.add(DwAtType.DW_AT_location, DwFormType.DW_FORM_exprloc, expression);
	}
}
