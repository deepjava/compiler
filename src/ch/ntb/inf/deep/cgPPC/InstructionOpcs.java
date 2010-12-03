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
	final int CRF1 = 1;
	final int CRF2 = 3;
	final int CRF3 = 3;
	final int CRF4 = 4;
	final int CRF5 = 5;
	final int CRF6 = 6;
	final int CRF7 = 7;
	final int EQ = 1;
	final int GT = 2;
	final int LT = 3;
	final int LR = 8;
	final int SRR0 = 26;
	final int SRR1 = 27;
	
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
		ppcAnd = (0x1f << 26) | (0x1c << 1),
		ppcAndi = (0x1c << 26),
		ppcAndis = (0x1d << 26),
		ppcB = (0x12 << 26),
		ppcBl = (0x12 << 26) | 1,
		ppcBc = (0x10 << 26),
		ppcBclr = (0x13 << 26) | (0x10 << 1),
		ppcCmp = (0x1f << 26),
		ppcCmpi = (0x0b << 26),
		ppcExtsb = (0x1f << 26) | (0x3ba << 1),
		ppcExtsh = (0x1f << 26) | (0x39a << 1),
		ppcFadd = (0x3f << 26) | (0x15 << 1),
		ppcFadds = (0x3b << 26) | (0x15 << 1),
		ppcFcmpu = (0x3f << 26) | (0x00 << 1),
		ppcFctiw = (0x3f << 26) | (0x1e << 1),
		ppcFdiv = (0x3f << 26) | (0x12 << 1),
		ppcFdivs = (0x3b << 26) | (0x12 << 1),
		ppcFmr = (0x3f << 26) | (0x48 << 1),
		ppcFmul = (0x3f << 26) | (0x19 << 1),
		ppcFmuls = (0x3b << 26) | (0x19 << 1),
		ppcFneg = (0x3f << 26) | (0x28 << 1),
		ppcFrsp = (0x3f << 26) | (0x0c << 1),
		ppcFsub = (0x3f << 26) | (0x14 << 1),
		ppcFsubs = (0x3b << 26) | (0x14 << 1),
		ppcLbz = (0x22 << 26),
		ppcLbzx = (0x1f << 26) | (0x57 << 1),
		ppcLfd = (0x32 << 26),
		ppcLfs = (0x30 << 26),
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
		ppcMulhw = (0x1f << 26) | (0x4b << 1),
		ppcMulhwu = (0x1f << 26) | (0x0b << 1),
		ppcMulli = (0x7 << 26),
		ppcMullw = (0x1f << 26) | (0xeb << 1),
		ppcNeg = (0x1f << 26) | (0x68 << 1),
		ppcOr = (0x1f << 26) | (0x1bc << 1),
		ppcOri = (0x18 << 26),
		ppcOris = (0x19 << 26),
		ppcRfi = (0x13 << 26) | (0x32 << 1),
		ppcRlwinm = (0x15 << 26),
		ppcRlwimi = (0x14 << 26),
		ppcRlwnm = (0x17 << 26),
		ppcSlw = (0x1f << 26) | (0x18 << 1),
		ppcSraw = (0x1f << 26) | (0x318 << 1),
		ppcSrawi = (0x1f << 26) | (0x338 << 1),
		ppcSrw = (0x1f << 26) | (0x218 << 1),
		ppcStb = (0x26 << 26),
		ppcStbx = (0x1f << 26) | (0xd7 << 1),
		ppcSth = (0x2c << 26),
		ppcSthx = (0x1f << 26) | (0x197 << 1),
		ppcStfd = (0x36 << 26),
		ppcStfdx = (0x1f << 26) | (0x2d7 << 1),
		ppcStfs = (0x34 << 26),
		ppcStfsx = (0x1f << 26) | (0x297 << 1),
		ppcStmw = (0x2f << 26),
		ppcStw = (0x24 << 26),
		ppcStwu = (0x25 << 26),
		ppcStwux = (0x1f << 26) | (0xb7 << 1),
		ppcStwx = (0x1f << 26) | (0x97 << 1),
		ppcSubf = (0x1f << 26) | (0x28 << 1),
		ppcSubfc = (0x1f << 26) | (0x08 << 1),
		ppcSubfe = (0x1f << 26) | (0x88 << 1),
		ppcSubfic = (0x08 << 26),
		ppcSubfze = (0x1f << 26) | (0xc8 << 1),
		ppcTw = (0x1f << 26) | (0x04 << 1),
		ppcTwi = (0x03 << 26),
		ppcXor = (0x1f << 26) | (0x13c << 1),
		ppcXori = (0x1A << 26),
		ppcXoris = (0x1B << 26);
}
