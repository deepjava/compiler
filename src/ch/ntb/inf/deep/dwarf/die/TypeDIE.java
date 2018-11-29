package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.ICdescAndTypeConsts;
import ch.ntb.inf.deep.classItems.Type;

public abstract class TypeDIE extends DebugInformationEntry {

	protected TypeDIE(DebugInformationEntry parent, DwTagType tagType) {
		super(parent, tagType); // Insert at First Position to be sure it is serialized before its depending DIE
	}

	public static TypeDIE generateNewTypeDIE(Type type, DebugInformationEntry parent) {
		if (type.name.charAt(0) == ICdescAndTypeConsts.tcRef) {
			// TODO: Handling Reference Types!
//			return new RefTypeDIE(type, parent);
			return new BaseTypeDIE(type, parent);
		} else {
			return new BaseTypeDIE(type, parent);
		}
	}
}
