package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.Field;
import ch.ntb.inf.deep.dwarf.location.AddressLocation;

public class ClassMemberDIE extends MemberDIE {

	public ClassMemberDIE(Field field, DebugInformationEntry parent) {
		this(field, field.address, parent);
	}

	public ClassMemberDIE(Field field, int address, DebugInformationEntry parent) {
		super(field, parent, DwTagType.DW_TAG_member);
		new ClassMemberImplementationDIE(field, this, parent.getParent());
	}

	@Override
	public void serializeDie(DWARF dwarf) {
		super.serializeDie(dwarf);
		dwarf.addFlag(DwAtType.DW_AT_external);
		dwarf.addFlag(DwAtType.DW_AT_declaration);
	}
}

class ClassMemberImplementationDIE extends DebugInformationEntry {

	private final AddressLocation location;
	private final ClassMemberDIE declerationDIE;

	public ClassMemberImplementationDIE(Field field, ClassMemberDIE declerationDIE, DebugInformationEntry parent) {
		super(parent, DwTagType.DW_TAG_variable);
		this.declerationDIE = declerationDIE;
		location = new AddressLocation(field.address);
	}

	@Override
	protected void serializeDie(DWARF dwarf) {
		// DwAtType.DW_AT_specification;
		// dwarf.add
		dwarf.addReference(DwAtType.DW_AT_specification, declerationDIE);
		dwarf.add(DwAtType.DW_AT_location, location);
	}
}