package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.Field;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.Type;

public abstract class MemberDIE extends DebugInformationEntry {
	final String name;
	final TypeDIE type;
	final byte accessability;

	public MemberDIE(Field field, DebugInformationEntry parent) {
		super(parent, DwTagType.DW_TAG_member);
		this.name = field.name.toString();
		this.type = getType((Type) field.type, parent.getRoot());

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
		serialize.add(DwAtType.DW_AT_name, name);
		serialize.addTypeDIE(type);
		serialize.addByte(DwAtType.DW_AT_accessibility, DwFormType.DW_FORM_data1, accessability);
	}
}
