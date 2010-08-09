package ch.ntb.inf.deep.strings;

public interface IhStringConsts {
	//--- String Attributes
	byte
		sattrIs7bitChars = 0,
		sattrIs8bitChars = 1,
		sattrIs16bitChars = 2,
		
		sattrTargetRef = 3;
	
	String[] stringAttributes = {
			"is7bitChars",
			"is8bitChars",
			"is16bitChars",
			
			"targetRef"
	};
}
