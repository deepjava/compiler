package ch.ntb.inf.deep.classItems;

import ch.ntb.inf.deep.strings.HString;

public class ClassMember extends Item {

	//--- instance fields
//	public int offset = -1; // relative offset in bytes from the // relative offset (depends on the type of the item, e.g. the offset of a constant in the constant pool or the offset of a method from the code base of a class)
//	public int address = -1; // the absolute address of this item on the target
//	public int index = -1; // the index or offset in a special context, for methodes it is the offset in byte in the class descriptor

	public Class owner;
	
	//--- constructors
	ClassMember(){
	}
	
	ClassMember(HString name, Type type){
		super(name, type);
	}
	

	//--- instance methods
	public Class getOwner(){
		return owner;
	}


	public void printOwner(){
		owner.printName();
	}
}
