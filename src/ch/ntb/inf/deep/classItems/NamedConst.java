package ch.ntb.inf.deep.classItems;

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
		vrb.print(" const: "); constant.print(0);
	}

}
