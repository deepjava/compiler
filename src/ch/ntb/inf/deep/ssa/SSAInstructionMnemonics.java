package ch.ntb.inf.deep.ssa;

public interface SSAInstructionMnemonics extends SSAInstructionOpcs {
	public static String[] scMnemonics = {
		"", //Empty String
		"sCloadConst",
		"sCadd",
		"sCsub",
		"sCmul",
		"sCdiv",
		"sCrem",
		"sCneg",
		"sCshl",
		"sCshr",
		"sCushr",
		"sCand",
		"sCor",
		"sCxor",
		"sCconvInt",
		"sCconvLong",
		"sCconvFloat",
		"sCconvDouble",
		"sCcmpl",
		"sCreturn",
		"sCcall",
		"sCnew",
		"sCinstanceof",
		"sCloadVar",
		"sCloadFromArray",
		"sCstoreToArray",
		"sCcmpg",
		"sCalength",
		"sCstoreToField",
		"sCthrow",
		"sCPhiFunc",
		"sCloadLocal",
		"sCRegMove",		
		"sCBranch"		
	};

}
