package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.Field;
import ch.ntb.inf.deep.dwarf.AddressExpression;
import ch.ntb.inf.deep.dwarf.DwarfExpression;

public class ClassMemberDIE extends MemberDIE {

	final DwarfExpression location;

	public ClassMemberDIE(Field field, DebugInformationEntry parent) {
		super(field, parent);
		location = new AddressExpression(field.address);
	}
	
	@Override
	public void serializeDie(DieSerializer serialize) {
		super.serializeDie(serialize);
		serialize.add(DwAtType.DW_AT_location, DwFormType.DW_FORM_exprloc, location);	
		serialize.addByte(DwAtType.DW_AT_external, DwFormType.DW_FORM_flag, (byte) 1);
	}

}
