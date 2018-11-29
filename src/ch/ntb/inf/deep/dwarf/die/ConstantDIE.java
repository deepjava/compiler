package ch.ntb.inf.deep.dwarf.die;

import ch.ntb.inf.deep.classItems.ConstField;

public class ConstantDIE extends DebugInformationEntry {

	protected ConstantDIE(ConstField constant, DebugInformationEntry parent) {
		super(parent, DwTagType.DW_TAG_constant);			
	}

	@Override
	protected void serializeDie(DWARF dwarf) {
		// TODO Auto-generated method stub

	}
}
