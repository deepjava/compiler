package org.deepjava.dwarf.die;

import org.deepjava.classItems.ICdescAndTypeConsts;
import org.deepjava.classItems.Type;

public abstract class TypeDIE extends DebugInformationEntry {

	protected TypeDIE(DebugInformationEntry parent, DwTagType tagType) {
		super(parent, tagType);
	}

	public static TypeDIE generateNewTypeDIE(Type type, DebugInformationEntry parent) {
		if (type.name.charAt(0) == 'V') {
			return null;
		} else if (type.name.charAt(0) == ICdescAndTypeConsts.tcRef) {
			return new RefTypeDIE(type, parent);
		} else if (type.name.charAt(0) == ICdescAndTypeConsts.tcArray){
			return new ArrayTypeDIE(type, parent);
		} else {
			return new BaseTypeDIE(type, parent);
		}
	}
}
