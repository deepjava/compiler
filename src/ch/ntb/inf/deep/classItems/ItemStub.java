package ch.ntb.inf.deep.classItems;

import ch.ntb.inf.deep.debug.Dbg;
import ch.ntb.inf.deep.strings.HString;

public class ItemStub extends Item {
//	public Item next;
//	public HString name; // name of the field or method 
//	public Item type; // type of the field, null for method
//	public int accAndPropFlags; // 0
	public Class owner; // provisional owner of the field or method
	public HString descriptor; // descriptor of the method, null for fields

	ItemStub(Class owner, HString fieldName, Type fieldType){
		super(fieldName, fieldType);
		this.owner = owner;
	}

	ItemStub(Class owner, HString methName, HString methDescriptor){
		super(methName, null);
		this.owner = owner;
		this.descriptor = methDescriptor;
	}
	

	Item getReplacedStub(){
		Item item;
		if( type == null) item = owner.getMethod(name, descriptor);
		else  item = owner.getField(name);
		if(enAssertion) assert item != null;
		item.accAndPropFlags |= this.accAndPropFlags;
		return item;
	}

	//--- debug primitives
	public void print(int indentLevel){
		indent(indentLevel);
		vrb.print("stub of ");
		if( type == null) vrb.printf("method: name=%1$s, d=%2$s", name, descriptor);
		else vrb.printf("field: name=%1$s, t=%2$s", name, type.name);
		vrb.print(", dFlags:");  Dbg.printDeepAccAndPropertyFlags(this.accAndPropFlags);
	}

	public void printShort(int indentLevel){
		print(indentLevel);
	}
}
