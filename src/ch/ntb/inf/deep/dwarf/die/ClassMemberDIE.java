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
		// Static Class Members need to be added to Compilation Unit. Otherwise dereferencing not work!
		super(field, parent.getParent(), DwTagType.DW_TAG_variable);
		location = new AddressExpression(address);
	}

	@Override
	public void serializeDie(DWARF dwarf) {
		super.serializeDie(dwarf);
		dwarf.addFlag(DwAtType.DW_AT_external);
		dwarf.add(DwAtType.DW_AT_location, location);
	}
}