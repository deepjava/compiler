package ch.ntb.inf.deep.config;

public interface ErrorCodes {
	
	//Parser Error codes
	public static final short errDigitExp = 220, errRParenExp = 221,
	errRBraceExp = 222, errRBracketExp = 223,
	errQuotationMarkExp = 224, errIOExp = 225,
	errUnexpectetSymExp = 226, errLBraceExp = 227,
	errLBracketExp = 228, errSemicolonMissExp = 229,
	errAssignExp = 230;
	
	//Data tree error codes
	public static final short errNoSuchDevice = 240, errNoDevices = 241, errSyntax = 242, errInconsistentattributes = 243,
	errInvalidType = 244, errInvalideParameter = 245, errOverwriteProtectedConst = 246, errUndefinedConst = 247, errMaxNofReached = 248,
	errMissingTag = 249, errNoSuchRegister = 250;
	
	
}

