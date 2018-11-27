package ch.ntb.inf.deep.dwarf;

import ch.ntb.inf.deep.classItems.Field;
import ch.ntb.inf.deep.dwarf.die.DebugInformationEntry;

public class ClassMemberDIE extends MemberDIE {

	final DwarfExpression location;

	public ClassMemberDIE(Field field, DebugInformationEntry parent) {
		super(field, parent);
		location = new AddressExpression(field.address);
	}

}
