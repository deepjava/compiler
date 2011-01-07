package ch.ntb.inf.deep.classItems;

import ch.ntb.inf.deep.debug.Dbg;
import ch.ntb.inf.deep.strings.HString;

public class StringLiteral extends Constant {
	public HString string;
	
	StringLiteral(HString name, HString string){
		super(name, Type.wellKnownTypes[txString]);
		this.string = string;
		this.accAndPropFlags |= (1<<apfFinal);
	}

	//--- debug primitives
	public void printShort(int indentLevel){
		vrb.printf("string \"%1$s\", flags=", string);  Dbg.printAccAndPropertyFlags(accAndPropFlags, 'F');
	}

	public void print(int indentLevel){
		super.print(indentLevel);
		vrb.print(" value = \"" + string + '\"'); 
	}

	public void println(int indentLevel){
		print(indentLevel);  vrb.println();
	}
}
