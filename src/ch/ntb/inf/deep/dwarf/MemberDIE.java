package ch.ntb.inf.deep.dwarf;

import ch.ntb.inf.deep.classItems.Field;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.dwarf.die.DebugInformationEntry;

public class MemberDIE extends DebugInformationEntry {
	final String name;
	final BaseTypeDIE type;
	final int offset;

	public MemberDIE(Field field, DebugInformationEntry parent) {
		super(parent);
		this.name = field.name.toString();
		this.type =  ((CompilationUnitDIE)this.getRoot()).getBaseTypeDie((Type) field.type);
		this.offset = field.offset;
	}

	@Override
	public void accept(DieVisitor visitor) {
		visitor.visit(this);
	}

}
