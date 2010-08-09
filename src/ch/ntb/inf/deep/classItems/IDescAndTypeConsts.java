package ch.ntb.inf.deep.classItems;

public interface IDescAndTypeConsts {
	char // primitive type descriptors
		tdVoid = 'V', // void

		tdBoolean = 'Z',	 // boolean
		tdByte = 'B', // byte
		tdShort = 'S', // short
		tdChar = 'C', // char
		tdInt = 'I', // int
		tdLong = 'J', // long
		tdFloat = 'F', // float
		tdDouble = 'D', // double
	
		// type categories
		tcPrimitive = 'P', // any primitive type { 'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z',   'V' }
		tcRef = 'L', // must be first char of a reference type descriptor
		tcArray = '['; // must be first char of an array type descriptor

	byte // well known type indices (indices for types in the wellKnownTypeTab)
		txVoid = 0, // void
		
		txBoolean = 1,	 // boolean
		txByte = 2, // byte
		txShort = 3, // short
		txChar = 4, // char
		txInt = 5, // int
		txLong = 6, // long
		txFloat = 7, // float
		txDouble = 8, // double
		txMaxOfPrimtiveTypes = txDouble,
	
		txString = 9,	 // java/lang/String
		txMaxOfStringOrPrimtiveTypes = txString,
		
		txObject = 10,
		nofWellKnownTypes = 11;
//	byte // base type name indices
//		btnxVoid = 1,
//		//--- 1 slot types (4 bytes)
//		btnxBoolean = 2,
//		btnxByte = 3,
//		btnxShort = 4,
//		btnxChar = 5,
//		btnxInt = 6,
//		btnxFloat = 7,
//		
//		btnxRef = 0,
//		btnxArray = 0,
//		
//		//--- 2 slot types (8 bytes)
//		btnxLong = 8,
//		btnxDouble = 9;
}
// B, .. , Z, []