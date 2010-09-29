package ch.ntb.inf.deep.cgPPC;

interface InstructionOpcs {
	final int BOtrue = 0x0c;
	final int BOfalse = 0x04;
	final int BOalways = 0x14;
	final int TOifequal = 0x04;
	final int TOifless = 0x10;
	final int TOifgreater = 0x08;
	final int TOifgeU = 0x05;
	final int CRF0 = 0;
	final int EQ = 1;
	final int GT = 2;
	final int LT = 3;
	final int LR = 0x100;
	
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

	public static String[] TOstring = {
		"",
		"",
		"",
		"",
		"ifequal",
		"ifgeU",
		"",
		"",
		"ifgreater",
		"",
		"",
		"",
		"",
		"",
		"",
		"",
		"ifless",
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
		"",
		"",
		"",
		"",
		""
	};

	final int // PowerPC Instructions
		ppcAdd = (0x1f << 26) | (0x10a << 1),
		ppcAddc = (0x1f << 26) | (0xa << 1),
		ppcAdde = (0x1f << 26) | (0x8a << 1),
		ppcAddi = (0x0e << 26),
		ppcAddis = (0x0f << 26),
		ppcB = (0x12 << 26),
		ppcBl = (0x12 << 26) | 1,
		ppcBc = (0x10 << 26),
		ppcBclr = (0x13 << 26) | (0x10 << 1),
		ppcCmp = (0x1f << 26),
		ppcCmpi = (0x0b << 26),
		ppcExtsb = (0x1f << 26) | (0x3ba << 1),
		ppcFadd = (0x3f << 26) | (0x15 << 1),
		ppcFadds = (0x3b << 26) | (0x15 << 1),
		ppcFdiv = (0x3f << 26) | (0x12 << 1),
		ppcFdivs = (0x3b << 26) | (0x12 << 1),
		ppcFmr = (0x3f << 26) | (0x48 << 1),
		ppcFmul = (0x3f << 26) | (0x19 << 1),
		ppcFmuls = (0x3b << 26) | (0x19 << 1),
		ppcFsub = (0x3f << 26) | (0x14 << 1),
		ppcFsubs = (0x3b << 26) | (0x14 << 1),
		ppcLbzx = (0x1f << 26) | (0x57 << 1),
		ppcLfdx = (0x1f << 26) | (0x257 << 1),
		ppcLfsx = (0x1f << 26) | (0x217 << 1),
		ppcLha = (0x2a << 26),
		ppcLhax = (0x1f << 26) | (0x157 << 1),
		ppcLmw = (0x2e << 26),
		ppcLwz = (0x20 << 26),
		ppcLwzu = (0x21 << 26),
		ppcLwzux = (0x1f << 26) | (0x37 << 1),
		ppcLwzx = (0x1f << 26) | (0x17 << 1),
		ppcMfspr = (0x1f << 26) | (0x153 << 1),
		ppcMtspr = (0x1f << 26) | (0x1d3 << 1),
		ppcMulli = (0x7 << 26),
		ppcMullw = (0x1f << 26) | (0xeb << 1),
		ppcOr = (0x1f << 26) | (0x1bc << 1),
		ppcRlwinm = (0x15 << 26),
		ppcStbx = (0x1f << 26) | (0xd7 << 1),
		ppcSthx = (0x1f << 26) | (0x197 << 1),
		ppcStfdx = (0x1f << 26) | (0x2d7 << 1),
		ppcStfsx = (0x1f << 26) | (0x297 << 1),
		ppcStmw = (0x2f << 26),
		ppcStw = (0x24 << 26),
		ppcStwu = (0x25 << 26),
		ppcStwux = (0x1f << 26) | (0xb7 << 1),
		ppcStwx = (0x1f << 26) | (0x97 << 1),
		ppcSubf = (0x1f << 26) | (0x28 << 1),
		ppcSubfic = (0x08 << 26),
		ppcTw = (0x1f << 26) | (0x04 << 1),
		ppcTwi = (0x03 << 26);
}
