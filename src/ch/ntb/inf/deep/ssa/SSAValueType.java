package ch.ntb.inf.deep.ssa;

public interface SSAValueType {
	public static int //Type of SSAValue
	tVoid = 0, tRef = 2, tObject = 3, tBoolean = 4, tChar = 5,
	tFloat = 6, tDouble = 7, tByte = 8, tShort = 9,
	tInteger = 10, tLong = 11,tAref = 12, tAobject= 13, tAboolean =14,
	tAchar = 15, tAfloat = 16, tAdouble = 17, tAbyte = 18,
	tAshort = 19, tAinteger = 20, tAlong = 21, tPhiFunc = 22 ;
	
	public static String[] svNames = {
		
		"Void",
		"",
		"Ref",
		"Object",
		"Boolean",
		"Char",
		"Float",
		"Double",
		"Byte",
		"Short",
		"Integer",
		"Long",
		"Array-Ref",
		"Object-Array",
		"Boolean-Array",
		"Char-Array",
		"Float-Array",
		"Double-Array",
		"Byte-Array",
		"Short-Array",
		"Integer-Array",
		"Long-Array",
		"Phi-Function"		
	};

}
