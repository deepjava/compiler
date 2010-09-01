package ch.ntb.inf.deep.classItems;

import ch.ntb.inf.deep.strings.HString;

public class StringLiteral extends DataItem {
	public HString string;
	
	StringLiteral(HString name, HString string){
		super(name, Type.wellKnownTypes[txString]);
		this.string = string;
	}

	//--- debug primitives
	public void printShort(int indentLevel){
		super.printShort(indentLevel);
		vrb.print('\"');  vrb.print(string);  vrb.print('\"');
	}

	public void print(int indentLevel){
		super.print(indentLevel);
		vrb.print(" value = \"" + string + '\"'); 
	}

	public void println(int indentLevel){
		print(indentLevel);  vrb.println();
	}
}
