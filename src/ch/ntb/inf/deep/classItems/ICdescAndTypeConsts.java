package ch.ntb.inf.deep.classItems;

public interface ICdescAndTypeConsts {
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

	byte// well known type indices (indices for types in the wellKnownTypeTab)
	// The indices of {txBoolean, txChar, txFloat, txDouble, txByte, txShort, txInt, txLong}
	// must be equal to the coding of these types in byte code  instruction newarray.
		txNull = 0, //?
		txString = 1,  // java/lang/String
		txObject = 2,

		txVoid = 3, // void, 
		txBoolean = 4,	 // boolean
		txChar = 5, // char
		txFloat = 6, // float
		txDouble = 7, // double
		txByte = 8, // byte
		txShort = 9, // short
		txInt = 10, // int
		txLong = 11, // long
		nofWellKnownTypes = 12;

	byte// field list indices
		flxBits = 0, // < 8 Bit, e.g. boolean ('Z')
		flxByte = 1,  //  ('B')
		flxShortChar = 2, // short ('S'), char ('C')
		flxInt = 4, //  ('I')
		flxRef = 5,	 // any reference type ('L') (incl. array ('['))
		flxFloat = 6, // float ('F')
		flxDouble = 7, // double ('D')
		flxLong = 8; // long ('J')

//		txMaxOfPrimtiveTypes = txDouble,
//		txMaxOfStringOrPrimtiveTypes = txString,

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