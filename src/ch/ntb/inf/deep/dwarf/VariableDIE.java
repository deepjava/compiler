package ch.ntb.inf.deep.dwarf;

import ch.ntb.inf.deep.classItems.Field;
import ch.ntb.inf.deep.classItems.LocalVar;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.dwarf.die.DebugInformationEntry;
import ch.ntb.inf.deep.dwarf.die.DwOpType;

public class VariableDIE extends DebugInformationEntry {

	public final String name;
	public final BaseTypeDIE type;
	public final DwarfExpression expression;

	protected VariableDIE(LocalVar localVar, DebugInformationEntry parent) {
		super(parent);
		this.name = localVar.name.toString();
		this.type = ((CompilationUnitDIE) parent.getParent().getParent()).getBaseTypeDie((Type) localVar.type);
		this.expression = new SimpleExpression(DwOpType.DW_OP_reg0);
	}

	protected VariableDIE(Field field, CompilationUnitDIE parent) {
		super(parent);
		this.name = field.name.toString();
		this.type = parent.getBaseTypeDie((Type) field.type);
		if (field.address == -1) {
			// Instance Field
			System.out.println("\tInstance Variable: " + field.name + " offset: " + field.offset);
			this.expression = new AddressExpression(field.address);
		} else {
			// Class Field
			System.out.println("\tClass Variable: " + field.name + " offset: " + field.offset);
			this.expression = new AddressExpression(field.address);
		}
	}

	@Override
	public void accept(DieVisitor visitor) {
		visitor.visit(this);
	}

}
