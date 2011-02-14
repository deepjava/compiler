package ch.ntb.inf.deep.classItems;

import ch.ntb.inf.deep.debug.Dbg;
import ch.ntb.inf.deep.strings.HString;

public class DataItem extends ClassMember {

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
		vrb.printf("field %1$s %2$s, flags=", type.name, name);  Dbg.printAccAndPropertyFlags(accAndPropFlags, 'F');
	}

	public void print(int indentLevel){
		indent(indentLevel);
		Dbg.printJavaAccAndPropertyFlags(this.accAndPropFlags, 'F');
		type.printTypeCategory(); type.printName();
		vrb.printf(" %1$s; offset=%2$d, ", name.toString(), offset); type.printSize();
		vrb.print(", dFlags:");  Dbg.printDeepAccAndPropertyFlags(this.accAndPropFlags, 'F');
	}

	public void println(int indentLevel){
		print(indentLevel);  vrb.println();
	}
}
