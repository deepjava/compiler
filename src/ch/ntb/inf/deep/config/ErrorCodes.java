package ch.ntb.inf.deep.config;

public interface ErrorCodes {
	
	//Parser Error codes
	public static final short errDigitExp = 101, errRParenExp = 102,
	errRBraceExp = 103, errRBracketExp = 104,
	errQuotationMarkExp = 105, errIOExp = 106,
	errUnexpectetSymExp = 107, errLBraceExp = 108,
	errLBracketExp = 109, errSemicolonMissExp = 110,
	errAssignExp = 111;
	
	//Data tree error codes
	public static final short errNoSuchDevice = 201, errNoDevices = 202, errSyntax = 203, errInconsistentattributes = 204,
	errInvalidType = 205, errInvalideParameter = 206, errOverwriteProtectedConst = 207;
	
	
}

