package ch.ntb.inf.deep.cgPPC;

interface InstructionOpcs {
	int // PowerPC Instructions
		ppcAdd = (0x1f << 26) | (0x10a << 1),
		ppcAddi = (0x0e << 26),
		ppcAddis = (0x0f << 26),
		ppcFadd = (0x3f << 26) | (0x15 << 1),
		ppcFadds = (0x3b << 26) | (0x15 << 1),
		ppcStwu = (0x25 << 26);
}
