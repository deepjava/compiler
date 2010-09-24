package ch.ntb.inf.deep.ssa;

public interface SSAValueType {
	public static int //Type of SSAValue
	tVoid = 0, tThis = 1, tPhiFunc = 2, tRef = 3, tBoolean = 4, tChar = 5,
	tFloat = 6, tDouble = 7, tByte = 8, tShort = 9,
	tInteger = 10, tLong = 11, tAref = 13, tAboolean =14,
	tAchar = 15, tAfloat = 16, tAdouble = 17, tAbyte = 18,
	tAshort = 19, tAinteger = 20, tAlong = 21;
	
	public static String[] svNames = {
		
		"Void",
		"This",
		"Phi-Function",
		"Ref",
		"Boolean",
		"Char",
		"Float",
		"Double",
		"Byte",
		"Short",
		"Integer",
		"Long",
		"",
		"Array-Ref",
		"Boolean-Array",
		"Char-Array",
		"Float-Array",
		"Double-Array",
		"Byte-Array",
		"Short-Array",
		"Integer-Array",
		"Long-Array"		
	};

}
