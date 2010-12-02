package ch.ntb.inf.deep.debug;

import ch.ntb.inf.deep.classItems.ICclassFileConsts;

public interface ICclassFileConstsAndMnemonics extends  ICclassFileConsts{
	
	//--- debug strings for: constant pool tags (cpt)
	String[] cptIdents = {
		"ExtSlot",
		"Utf8",
		"?",
		"Integer", "Float", "Long", "Double",
		"Class", "String", "FieldRef", "MethRef",
		"IntfMethRef", "NameAndType"
	};

	
	//--- debug strings for: access and property flags (apf) for class, field, method
	String[] apfIdents = {
			"public", // 0, 0x001
			"private", // 1, 0x002
			"protected", // 2, 0x004
			"static", // 3, 0x008
			"final", // 4, 0x010
			"(super|synch)", // 5, 0x020 // "super" for class objects, "synchronized" for methods
			"volatile", // 6, 0x040
			"transient", // 7, 0x080
			"native", // 8, 0x100
			"interface", // 9, 0x200
			"abstract", // 10, 0x400
			"strict", // 11, 0x800
			"enumArray", // 12, 0x1000
			"deprecated", // 13, 0x2000
			"enum", // 14, 0x4000
			"synthetic", // 15, 0x8000
					
			//---- deep properties
			//-- class flags:
			"classLoaded", // 16 class loaded
			"rootClass", // 17 this class is a root class (loaded by loadRootClass(..) )
			"declaration", // 18 class is used for declarations of static fields, instance fields, local variables
			"instances", // 19 there might be objects of this class in the running system
			"typeTest", // 20 there are type tests with this type (instructions: checkcast, instanceof)

			//--- field flags:
			"const", // 21 constant field
			"readAccess", // 22 one or more read accesses to this item
			"writeAccess", // 23 one or more write accesses to this item

			//--- method flags:
			"command", //24 method is a command, i.e. this method is invoked by an outside client
			"SysPrimitive", //25 method is a system primitive
			"isExcHnd", //25 method is an exception handler, i.e. this method is invoked by hardware
			"call",	//27 method gets called by the bc instructions invokestatic or invokevirtual
			"interfCall",	//28 method gets invoked by the bc instruction invokeinterface
			"excHndCall",	//29 method gets invoked directly or indirectly by an exception handler method
			"new",	//30 method gets invoked by the bc instructions: {new,  newarray,  anewarray, multianewarray}
			"unsave"	//30 method gets invoked by the bc instructions: {new,  newarray,  anewarray, multianewarray}
	};

	String[] attributes = {// attributes: 
			"ConstantValue", // 4.7.2, p119, items: fields
			"Deprecated", // 4.7.10, p132, items: fields, methods
			"Synthetic", // 4.7.6, p127, items: fields, methods
			"SourceFile", // 4.7.7, p128
			
			"Code", // 4.7.3, p120, items: methods
			"LocalVariableTable", // 4.7.9, p130
			"LineNumberTable", // 4.7.8, p129
			"Exceptions", // 4.7.4, p123

			"InnerClasses", // 4.7.5, p125
		};
}
