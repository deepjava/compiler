package ch.ntb.inf.deep.dwarf;

import ch.ntb.inf.deep.classItems.Field;
import ch.ntb.inf.deep.dwarf.die.DebugInformationEntry;

public class InstanceMemberDIE extends MemberDIE {

	final int offset;
	
	public InstanceMemberDIE(Field field, DebugInformationEntry parent) {
		super(field, parent);
		this.offset = field.offset;
	}

}
