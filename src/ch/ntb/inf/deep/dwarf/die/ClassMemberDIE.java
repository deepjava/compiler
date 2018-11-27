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

}
