package org.deepjava.dwarf.die;

import org.deepjava.classItems.Field;

public class InstanceMemberDIE extends MemberDIE {

	private final int offset;

	public InstanceMemberDIE(Field field, DebugInformationEntry parent) {
		super(field, parent, DwTagType.DW_TAG_member);
		this.offset = field.offset;
	}

	@Override
	public void serializeDie(DWARF dwarf) {
		super.serializeDie(dwarf);
		dwarf.addInt(DwAtType.DW_AT_data_member_location, DwFormType.DW_FORM_data4, offset);
	}

}
