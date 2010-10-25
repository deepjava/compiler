package ch.ntb.inf.deep.ssa;

public interface SSAInstructionMnemonics extends SSAInstructionOpcs {
	public static String[] scMnemonics = {
		"sCloadConst",
		"sCloadLocal",
		"sCloadFromField",
		"sCloadFromArray",
		"sCstoreToField",
		"sCstoreToArray",
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
		"sCcmpg",
		"sCinstanceof",
		"sCalength",
		"sCcall",
		"sCnew",
		"sCreturn",
		"sCthrow",
		"sCBranch",
		"sCRegMove",		
		"sCPhiFunc"
	};

}
