package ch.ntb.inf.deep.config;

public interface IAttributes {
	byte //--- access and content attributes
	atrRead = 0,		// 0x0001
	atrWrite = 1,		// 0x0002
	atrWconst = 4,		// 0x0010
	atrCode = 5,		// 0x0020
	atrVar = 6,			// 0x0020
	atrHeap = 7,    	// 0x0080
	atrStack = 8,		// 0x0100
	AtrSysconst = 9;	// 0x0200
}
