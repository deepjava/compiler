package ch.ntb.inf.deep.classItems;

import ch.ntb.inf.deep.debug.Dbg;
import ch.ntb.inf.deep.strings.HString;

public class Array extends Type {

	public Type componentType;
	public byte dimension; // array dimension

	
	Array(HString regName){
		super(regName, wktObject); // base type is java/lang/Object
		category = tcArray;
		sizeInBits = 32;

		dimension = 1;
		while(regName.charAt(dimension) == tcArray) dimension++;
		HString sname = regName.substring(dimension);
		sname = stab.insertCondAndGetEntry(sname);
		componentType = getTypeByDescriptor(sname);
	}

	protected void selectAndMoveInitClasses(){
	}

	//--- debug primitives
	
	public void printShort(int indentLevel){
		indent(indentLevel);
		vrb.printf("array %1$s, flags=", name);  Dbg.printAccAndPropertyFlags(accAndPropFlags, 'C');
	}
	
	public void printFields(int indentLevel){
		indent(indentLevel);
		vrb.printf("array: (dim=%1$d, compType=%2$s, baseType=%3$s)\n", dimension, componentType.name, type.name);
	}

	public void printMethods(int indentLevel){
		indent(indentLevel);
		vrb.println("methods: none");
	}
}
