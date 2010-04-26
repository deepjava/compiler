package ch.ntb.inf.deep.ssa;

public interface SSAValueType {
	int //Type of SSAValue
	tVoid = 0, tObject = 3, tBoolean = 4, tChar = 5,
	tFloat = 6, tDouble = 7, tByte = 8, tShort = 9,
	tInteger = 10, tLong = 11,tAobject= 13, tAboolean =14,
	tAchar = 15, tAfloat = 16, tAdouble = 17, tAbyte = 18,
	tAshort = 19, tAinteger = 20, tAlong = 21;

}
