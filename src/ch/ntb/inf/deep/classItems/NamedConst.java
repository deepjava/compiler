package ch.ntb.inf.deep.classItems;

import ch.ntb.inf.deep.debug.Dbg;
import ch.ntb.inf.deep.strings.HString;

public class NamedConst extends DataItem {
	Constant constant;

	NamedConst(HString name, Type type, Item constant){
		super(name, type);
		this.constant = (Constant)constant;
	}

	//--- debug primitives
	public void print(int indentLevel){
		super.print(indentLevel);
		vrb.print("nconst "); constant.print(0);
	}

	public void printShort(int indentLevel){
		indent(indentLevel);
		vrb.printf("nconst %1$s %2$s, flags=", type.name, name);  Dbg.printAccAndPropertyFlags(accAndPropFlags, 'F');
		vrb.print(", ");  constant.printShort(0);
	}
	
	public Constant getConstantItem() {
		return constant;
	}
}
