package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.Field;
import ch.ntb.inf.deep.classItems.LocalVar;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.dwarf.AddressExpression;
import ch.ntb.inf.deep.dwarf.DwarfExpression;
import ch.ntb.inf.deep.dwarf.SimpleExpression;
import ch.ntb.inf.deep.dwarf.Utils;

public class VariableDIE extends DebugInformationEntry {

	public final String name;
	public final BaseTypeDIE type;
	public final DwarfExpression expression;

	protected VariableDIE(LocalVar localVar, DebugInformationEntry parent) {
		super(parent, DwTagType.DW_TAG_formal_parameter);
		this.name = localVar.name.toString();
		this.type = ((CompilationUnitDIE) parent.getParent().getParent()).getBaseTypeDie((Type) localVar.type);
		this.expression = new SimpleExpression(DwOpType.DW_OP_reg0);
	}

	protected VariableDIE(Field field, CompilationUnitDIE parent) {
		super(parent, DwTagType.DW_TAG_formal_parameter);
		this.name = field.name.toString();
		this.type = parent.getBaseTypeDie((Type) field.type);
		if (field.address == -1) {
			// Instance Field
			System.out.println("\tInstance Variable: " + field.name + " offset: " + field.offset);
			this.expression = new AddressExpression(field.address);
		} else {
			// Class Field
			System.out.println("\tClass Variable: " + field.name + " offset: " + field.offset);
			this.expression = new AddressExpression(field.address);
		}
	}

	@Override
	public void serializeDie(DieSerializer serialize) {
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwAtType.DW_AT_name.value());
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwFormType.DW_FORM_string.value());
		serialize.debug_info.put(Utils.serialize(name));

		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwAtType.DW_AT_type.value());
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwFormType.DW_FORM_ref4.value());
		serialize.debug_info.putInt(type.baseAddress - getRoot().baseAddress);

		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwAtType.DW_AT_location.value());
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwFormType.DW_FORM_exprloc.value());
		expression.serialize(serialize.debug_info);
	}
}
