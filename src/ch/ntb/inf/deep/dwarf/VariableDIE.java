package ch.ntb.inf.deep.dwarf;

import ch.ntb.inf.deep.classItems.LocalVar;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.dwarf.die.DebugInformationEntry;

public class VariableDIE extends DebugInformationEntry {
	
	public final String name;
	public final BaseTypeDIE type;

	protected VariableDIE(DebugInformationEntry parent, LocalVar localVar) {
		super(parent);
		System.out.println("\t\tVariable: " + localVar.name);
		this.name = localVar.name.toString();
		this.type = ((CompilationUnitDIE)parent.getParent()).getBaseTypeDie((Type) localVar.type);
	}

	@Override
	public void accept(DieVisitor visitor) {
		visitor.visit(this);
	}

}
