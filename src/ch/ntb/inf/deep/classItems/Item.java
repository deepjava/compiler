package ch.ntb.inf.deep.classItems;

import java.io.PrintStream;

import ch.ntb.inf.deep.host.ErrorReporter;
import ch.ntb.inf.deep.strings.HString;

public abstract class Item   implements Cloneable, IClassFileConsts, IDescAndTypeConsts {
	static final boolean verbose = false, enAssertion = true;
	static PrintStream vrb = System.out;
	static PrintStream log = System.out;
	static ErrorReporter errRep = ErrorReporter.reporter;

	public static void indent(int indentLevel){
		indentLevel = indentLevel*3;
		while(indentLevel-- > 0) vrb.print(' ');
	}

	//--- instance fields
	public Item next;
	public HString name; // the key string for any item

	public Item type; // base type for objects of "Class" or "Type" or null
	public int accAndPropFlags; // access and property flags (see ClassFileConsts)
	public int offSet; // the offset in the data segment on the target, e.g. the offset of a constant in the constant pool or the offset of a method in the class descriptor

	//--- constructors
	Item(){
	}

	Item(HString name){
		this.name = name;
	}

	Item(HString name, Type type){
		this.name = name;
		this.type = type;
	}

	protected Item clone(){
		Item cln = null;
		try{
			cln = (Item)super.clone();
		}catch(CloneNotSupportedException e){
			e.printStackTrace();
		}
		return cln;
	}

	//--- instance methods


	//--- debug primitives
	public void printFields(int indentLevel){ vrb.println("no meth printFields for class "+ getClass().getName());	}
	public void printMethods(int indentLevel){ vrb.println("no meth printMethods for class "+ getClass().getName());	}


	public void printShort(int indentLevel){
		indent(indentLevel);
		if(type != null) vrb.print(type.name);
		vrb.printf(" %1$s ", name);
	}

	public void printTypeCategory(){
		if(type != null) type.printTypeCategory(); else  vrb.print("(-)");
	}

	public void printName(){
		vrb.print(name);
	}

	public void printOwner(){
		vrb.print('?');
	}
	
	public void printName(int indentLevel){
		indent(indentLevel);
		vrb.print(name);
	}

	public void printTypeName(int indentLevel){
		indent(indentLevel);
		vrb.print(type.name);
	}

	public void print(int indentLevel){
		printName(indentLevel);
	}

	public void println(int indentLevel){
		print(indentLevel);  vrb.println();
	}
	
	public void printOffset(int indentLevel){
		indent(indentLevel);
		printOffset();
	}
	
	public void printOffset(){
		vrb.print("offset = " + offSet);
	}
}
