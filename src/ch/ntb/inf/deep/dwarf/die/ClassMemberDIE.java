package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.Field;
import ch.ntb.inf.deep.dwarf.location.AddressExpression;
import ch.ntb.inf.deep.dwarf.location.DwarfExpression;

public class ClassMemberDIE extends MemberDIE {

	private final DwarfExpression location;

	public ClassMemberDIE(Field field, DebugInformationEntry parent) {
		this(field, field.address, parent);
	}

	public ClassMemberDIE(Field field, int address, DebugInformationEntry parent) {
		super(field, parent, DwTagType.DW_TAG_variable);
		location = new AddressExpression(address);
	}

	@Override
	public void serializeDie(DWARF dwarf) {
		super.serializeDie(dwarf);
		dwarf.add(DwAtType.DW_AT_location, location);
		dwarf.addByte(DwAtType.DW_AT_external, DwFormType.DW_FORM_flag, (byte) 1);
	}

}
