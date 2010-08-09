package ch.ntb.inf.deep.debug;

import ch.ntb.inf.deep.classItems.IClassFileConsts;

public interface IClassFileConstsAndMnemonics extends  IClassFileConsts{
	
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
			"?", // 13, 0x2000
			"enum", // 14, 0x4000
			"?", // 15, 0x8000
			
			//--- deep properties
			"classLoaded", // 16
			"rootClass", // 17

			"readRef", // 18
			"writeRef", // 19
			"call", // 20
			"interfCall", // 21
			"declaration", // 22
			"new", // 23
			
			"const", // 24
			"deprecated", // 25
			"synthetic", // 26
			"command", // 27
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
