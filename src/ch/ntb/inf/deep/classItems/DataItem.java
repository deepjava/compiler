package ch.ntb.inf.deep.classItems;

import ch.ntb.inf.deep.debug.Dbg;
import ch.ntb.inf.deep.strings.HString;

public class DataItem extends Item {

	//--- constructors
	DataItem(){
	}

	DataItem(HString name, Type type){
		super(name, type);
	}

	//--- instance methods

	
	//--- debug primitives
	public void printShort(int indentLevel){
		indent(indentLevel);
		vrb.print("field ");
		super.printShort(0);
	}

	public void print(int indentLevel){
		indent(indentLevel);
		Dbg.printJavaAccAndPropertyFlags(this.accAndPropFlags);
		type.printTypeCategory(); type.printName();
		vrb.print(' '); printName();
		vrb.print(";//dFlags:");  Dbg.printDeepAccAndPropertyFlags(this.accAndPropFlags);
	}

	public void println(int indentLevel){
		print(indentLevel);  vrb.println();
	}
}
