package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.Field;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.dwarf.Utils;

public abstract class MemberDIE extends DebugInformationEntry {
	final String name;
	final BaseTypeDIE type;
	final byte accessability;

	public MemberDIE(Field field, DebugInformationEntry parent) {
		super(parent, DwTagType.DW_TAG_member);
		this.name = field.name.toString();
		this.type =  ((CompilationUnitDIE)this.getRoot()).getBaseTypeDie((Type) field.type);
			
		if ((field.accAndPropFlags & (1 << ICclassFileConsts.apfPublic)) != 0) {
			this.accessability = 0x01;
		} else if ((field.accAndPropFlags & (1 << ICclassFileConsts.apfPrivate)) != 0) {
			this.accessability = 0x03;
		} else if ((field.accAndPropFlags & (1 << ICclassFileConsts.apfProtected)) != 0) {
			this.accessability = 0x02;
		} else if ((field.accAndPropFlags & (1 << ICclassFileConsts.dpfSysPrimitive)) != 0) {
			this.accessability = 0; // special system primitive
		} else {
			this.accessability = 0x2;
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
				
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwAtType.DW_AT_accessibility.value());
		Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwFormType.DW_FORM_data1.value());		
		serialize.debug_info.put(accessability);
		
		if (this instanceof ClassMemberDIE) {
			ClassMemberDIE classMemberDie = (ClassMemberDIE)this;
			
			Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwAtType.DW_AT_location.value());
			Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwFormType.DW_FORM_exprloc.value());
			classMemberDie.location.serialize(serialize.debug_info);
			
			Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwAtType.DW_AT_external.value());
			Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwFormType.DW_FORM_flag.value());
			serialize.debug_info.put((byte) 1);
		} else {
			InstanceMemberDIE instanceMemberDie = (InstanceMemberDIE)this;
			
			Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwAtType.DW_AT_data_member_location.value());
			Utils.writeUnsignedLeb128(serialize.debug_abbrev, DwFormType.DW_FORM_data4.value());
			serialize.debug_info.putInt(instanceMemberDie.offset);
			
		}
	}
}
