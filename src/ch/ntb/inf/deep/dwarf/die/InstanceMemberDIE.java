package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.Field;

public class InstanceMemberDIE extends MemberDIE {

	final int offset;
	
	public InstanceMemberDIE(Field field, DebugInformationEntry parent) {
		super(field, parent);
		this.offset = field.offset;
	}

}
