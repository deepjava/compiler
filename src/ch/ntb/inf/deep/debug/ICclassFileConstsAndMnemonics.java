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
			"public", // 0
			"private", // 1
			"protected", // 2
			"static", // 3
			"final", // 4
			"|Csuper|Msynch", // 5, "super" for class objects, "synchronized" for methods
			"volatile", // 6
			"transient", // 7
			"native", // 8
			"interface", // 9
			"abstract", // 10
			"strict", // 11
			"enumArray", // 12
			"deprecated", // 13, dpfDeprecated
			"enum", // 14
			
			//---- deep properties
			//-- class flags:
			"classLoaded", // 15 class loaded
			"rootClass", // 16 this class is a root class (loaded by loadRootClass(..) )
			"declaration", // 17 class is used for declarations of static fields, instance fields, local variables
			"instances", // 18 there might be objects of this class in the running system
			"typeTest", // 19 there are type tests with this type (instructions: checkcast, instanceof)

			//--- field flags:
			"|Cmark|Fconst", // 20 constant field, marked class
			"readAccess", // 21 one or more read accesses to this item
			"writeAccess", // 22 one or more write accesses to this item
			//"Synthetic", //31 synthetic field or method (items not in source text

			//--- method flags:
			"command", //23 method is a command, i.e. this method is invoked by an outside client
			"call",	//24 method gets called by the bc instructions invokestatic or invokevirtual
			"interfCall",	//25 method gets invoked by the bc instruction invokeinterface
			"excHndCall",	//26 method gets invoked directly or indirectly by an exception handler method
			"excHnd", //27 method is an exception handler, i.e. this method is invoked by hardware

			"new",	//28 method gets invoked by the bc instructions: {new,  newarray,  anewarray, multianewarray}
			"unsave",	//29 method gets invoked by the bc instructions: {new,  newarray,  anewarray, multianewarray}
			"SysPrimitive", //30 method is a system primitive
			"Synthetic", //31 synthetic field or method (items not in source text, deep in-line methods: code not loaded)
	};

	String[] attributes = {// class file attributes: 
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
