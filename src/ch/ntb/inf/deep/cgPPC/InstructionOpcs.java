package ch.ntb.inf.deep.cgPPC;

interface InstructionOpcs {
	final int BOtrue = 0x0c;
	final int BOfalse = 0x04;
	final int BOalways = 0x14;
	final int CRF0 = 0;
	final int EQ = 1;
	final int GT = 2;
	final int LT = 3;
	final int LR = 0x100;
	
	final int iffalse = 4;
	final int iftrue = 0xc;
	final int always = 0x14;
	final int CRF0SO = 28;
	final int CRF0EQ = 29;
	final int CRF0GT = 30;
	final int CRF0LT = 31;

	public static String[] BOstring = {
		"",
		"",
		"",
		"",
		"iffalse",
		"iffalse",
		"",
		"",
		"",
		"",
		"",
		"",
		"iftrue",
		"iftrue",
		"",
		"",
		"",
		"",
		"",
		"",
		"always",
		"",
		"",
		"",
		"",
		"",
		"",
		"",
		"",
		"",
		"",
		""
	};

	public static String[] BIstring = {
		"CRF7[SO]",
		"CRF7[EQ]",
		"CRF7[GT]",
		"CRF7[LT]",
		"CRF6[SO]",
		"CRF6[EQ]",
		"CRF6[GT]",
		"CRF6[LT]",
		"CRF5[SO]",
		"CRF5[EQ]",
		"CRF5[GT]",
		"CRF5[LT]",
		"CRF4[SO]",
		"CRF4[EQ]",
		"CRF4[GT]",
		"CRF4[LT]",
		"CRF3[SO]",
		"CRF3[EQ]",
		"CRF3[GT]",
		"CRF3[LT]",
		"CRF2[SO]",
		"CRF2[EQ]",
		"CRF2[GT]",
		"CRF2[LT]",
		"CRF1[SO]",
		"CRF1[EQ]",
		"CRF1[GT]",
		"CRF1[LT]",
		"CRF0[SO]",
		"CRF0[EQ]",
		"CRF0[GT]",
		"CRF0[LT]"
	};

	final int // PowerPC Instructions
		ppcAdd = (0x1f << 26) | (0x10a << 1),
		ppcAddi = (0x0e << 26),
		ppcAddis = (0x0f << 26),
		ppcB = (0x12 << 26),
		ppcBl = (0x12 << 26) | 1,
		ppcBc = (0x10 << 26),
		ppcBclr = (0x13 << 26) | (0x10 << 1),
		ppcCmp = (0x1f << 26),
		ppcCmpi = (0x0b << 26),
		ppcFadd = (0x3f << 26) | (0x15 << 1),
		ppcFadds = (0x3b << 26) | (0x15 << 1),
		ppcFsub = (0x3f << 26) | (0x14 << 1),
		ppcFsubs = (0x3b << 26) | (0x14 << 1),
		ppcLmw = (0x2e << 26),
		ppcLwz = (0x20 << 26),
		ppcMfspr = (0x1f << 26) | (0x153 << 1),
		ppcMtspr = (0x1f << 26) | (0x1d3 << 1),
		ppcOr = (0x1f << 26) | (0x1bc << 1),
		ppcStmw = (0x2f << 26),
		ppcStw = (0x24 << 26),
		ppcStwu = (0x25 << 26),
		ppcSubf = (0x1f << 26) | (0x28 << 1),
		ppcSubfic = (0x08 << 26);
}
