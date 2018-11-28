package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.Type;

public abstract class TypeDIE extends DebugInformationEntry {

	protected TypeDIE(DebugInformationEntry parent, DwTagType tagType) {
		super(parent, tagType, true); // Insert at First Position to be sure it is serialized before its depending DIE
	}

	public static TypeDIE generateNewTypeDIE(Type type, DebugInformationEntry parent) {
		if (type.category == 'L') {
			return new BaseTypeDIE(type, parent);
//			return new RefTypeDIE(type, parent);
		} else {
			return new BaseTypeDIE(type, parent);
		}
	}
}
