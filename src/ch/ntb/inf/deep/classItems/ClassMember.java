package ch.ntb.inf.deep.classItems;

import ch.ntb.inf.deep.strings.HString;

public class ClassMember extends Item {

	//--- instance fields
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
