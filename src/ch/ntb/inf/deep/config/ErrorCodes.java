package ch.ntb.inf.deep.config;

public interface ErrorCodes {
	
	//Parser Error codes
	public static final int errDigitExp = 200, errRParenExp = 201,
	errRBraceExp = 202, errRBracketExp = 203,
	errQuotationMarkExp = 204, errIOExp = 205,
	errUnexpectetSymExp = 206, errLBraceExp = 207,
	errLBracketExp = 208, errSemicolonMissExp = 209,
	errAssignExp = 210;
	
	//Data tree error codes
	public static final int errNoSuchDevice = 220, errNoDevices = 221, errSyntax = 222, errInconsistentattributes = 223,
	errInvalidType = 224, errInvalideParameter = 225, errOverwriteProtectedConst = 226, errUndefinedConst = 227, errMaxNofReached = 228,
	errMissingTag = 229, errNoSuchRegister = 230, errNoDefaultSegmentDef = 231, errNoSysTabSegmentDef = 232, errInitNotSupported = 233,
	errFixMethAddrNotSupported = 234, errNoSuchSegment = 235;
	
	
}

