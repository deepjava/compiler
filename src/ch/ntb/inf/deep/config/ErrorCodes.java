package ch.ntb.inf.deep.config;

public interface ErrorCodes {
	
	//Parser Error codes
	public static final short errDigitExp = 120, errRParenExp = 121,
	errRBraceExp = 122, errRBracketExp = 123,
	errQuotationMarkExp = 124, errIOExp = 125,
	errUnexpectetSymExp = 126, errLBraceExp = 127,
	errLBracketExp = 128, errSemicolonMissExp = 129,
	errAssignExp = 130;
	
	//Data tree error codes
	public static final short errNoSuchDevice = 140, errNoDevices = 141, errSyntax = 142, errInconsistentattributes = 143,
	errInvalidType = 144, errInvalideParameter = 145, errOverwriteProtectedConst = 146, errUndefinedConst = 147, errMaxNofReached = 148,
	errMissingTag = 149, errNoSuchRegister = 150;
	
	
}

