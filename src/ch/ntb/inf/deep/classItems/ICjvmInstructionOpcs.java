/*
 * Copyright (c) 2011 NTB Interstate University of Applied Sciences of Technology Buchs.
 *
 * http://www.ntb.ch/inf
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Eclipse Public License for more details.
 * 
 * Contributors:
 *     NTB - initial implementation
 * 
 */

package ch.ntb.inf.deep.classItems;

public interface ICjvmInstructionOpcs {
	short // operation codes of JVM instructions (byte codes)
		bCnop = 0,   bCaconst_null = 1,   bCiconst_m1 = 2,   bCiconst_0 = 3,
		bCiconst_1 = 4,   bCiconst_2 = 5,   bCiconst_3 = 6,   bCiconst_4 = 7,
		bCiconst_5 = 8,   bClconst_0 = 9,   bClconst_1 = 10,   bCfconst_0 = 11,
		bCfconst_1 = 12,   bCfconst_2 = 13,   bCdconst_0 = 14,   bCdconst_1 = 15,
		bCbipush = 16,   bCsipush = 17,   bCldc = 18,   bCldc_w = 19,
		bCldc2_w = 20,   bCiload = 21,   bClload = 22,   bCfload = 23,
		bCdload = 24,   bCaload = 25,   bCiload_0 = 26,   bCiload_1 = 27,
		bCiload_2 = 28,   bCiload_3 = 29,   bClload_0 = 30,   bClload_1 = 31,
		bClload_2 = 32,   bClload_3 = 33,   bCfload_0 = 34,   bCfload_1 = 35,
		bCfload_2 = 36,   bCfload_3 = 37,   bCdload_0 = 38,   bCdload_1 = 39,
		bCdload_2 = 40,   bCdload_3 = 41,   bCaload_0 = 42,   bCaload_1 = 43,
		bCaload_2 = 44,   bCaload_3 = 45,   bCiaload = 46,   bClaload = 47,
		bCfaload = 48,   bCdaload = 49,   bCaaload = 50,   bCbaload = 51,
		bCcaload = 52,   bCsaload = 53,   bCistore = 54,   bClstore = 55,
		bCfstore = 56,   bCdstore = 57,   bCastore = 58,   bCistore_0 = 59,
		bCistore_1 = 60,   bCistore_2 = 61,   bCistore_3 = 62,   bClstore_0 = 63,
		bClstore_1 = 64,   bClstore_2 = 65,   bClstore_3 = 66,   bCfstore_0 = 67,
		bCfstore_1 = 68,   bCfstore_2 = 69,   bCfstore_3 = 70,   bCdstore_0 = 71,
		bCdstore_1 = 72,   bCdstore_2 = 73,   bCdstore_3 = 74,   bCastore_0 = 75,
		bCastore_1 = 76,   bCastore_2 = 77,   bCastore_3 = 78,   bCiastore = 79,
		bClastore = 80,   bCfastore = 81,   bCdastore = 82,   bCaastore = 83,
		bCbastore = 84,   bCcastore = 85,   bCsastore = 86,   bCpop = 87,
		bCpop2 = 88,   bCdup = 89,   bCdup_x1 = 90,   bCdup_x2 = 91,
		bCdup2 = 92,   bCdup2_x1 = 93,   bCdup2_x2 = 94,   bCswap = 95,
		bCiadd = 96,   bCladd = 97,   bCfadd = 98,   bCdadd = 99,
		bCisub = 100,   bClsub = 101,   bCfsub = 102,   bCdsub = 103,
		bCimul = 104,   bClmul = 105,   bCfmul = 106,   bCdmul = 107,
		bCidiv = 108,   bCldiv = 109,   bCfdiv = 110,   bCddiv = 111,
		bCirem = 112,   bClrem = 113,   bCfrem = 114,   bCdrem = 115,
		bCineg = 116,   bClneg = 117,   bCfneg = 118,   bCdneg = 119,
		bCishl = 120,   bClshl = 121,   bCishr = 122,   bClshr = 123,
		bCiushr = 124,   bClushr = 125,   bCiand = 126,   bCland = 127,
		bCior = 128,   bClor = 129,   bCixor = 130,   bClxor = 131,
		bCiinc = 132,   bCi2l = 133,   bCi2f = 134,   bCi2d = 135,
		bCl2i = 136,   bCl2f = 137,   bCl2d = 138,   bCf2i = 139,
		bCf2l = 140,   bCf2d = 141,   bCd2i = 142,   bCd2l = 143,
		bCd2f = 144,   bCi2b = 145,   bCi2c = 146,   bCi2s = 147,
		bClcmp = 148,   bCfcmpl = 149,   bCfcmpg = 150,   bCdcmpl = 151,
		bCdcmpg = 152,   bCifeq = 153,   bCifne = 154,   bCiflt = 155,
		bCifge = 156,   bCifgt = 157,   bCifle = 158,   bCif_icmpeq = 159,
		bCif_icmpne = 160,   bCif_icmplt = 161,   bCif_icmpge = 162,   bCif_icmpgt = 163,
		bCif_icmple = 164,   bCif_acmpeq = 165,   bCif_acmpne = 166,   bCgoto = 167,
		bCjsr = 168,   bCret = 169,   bCtableswitch = 170,   bClookupswitch = 171,
		bCireturn = 172,   bClreturn = 173,   bCfreturn = 174,   bCdreturn = 175,
		bCareturn = 176,   bCreturn = 177,   bCgetstatic = 178,   bCputstatic = 179,
		bCgetfield = 180,   bCputfield = 181,   bCinvokevirtual = 182,   bCinvokespecial = 183,
		bCinvokestatic = 184,   bCinvokeinterface = 185,   bCxxxunusedxxx = 186,   bCnew = 187,
		bCnewarray = 188,   bCanewarray = 189,   bCarraylength = 190,   bCathrow = 191,
		bCcheckcast = 192,   bCinstanceof = 193,   bCmonitorenter = 194,   bCmonitorexit = 195,
		bCwide = 196,   bCmultianewarray = 197,   bCifnull = 198,   bCifnonnull = 199,
		bCgoto_w = 200,   bCjsr_w = 201,   bCbreakpoint = 202,   bCunused203 = 203,
		bCunused204 = 204,   bCunused205 = 205,   bCunused206 = 206,   bCunused207 = 207,
		bCunused208 = 208,   bCunused209 = 209,   bCunused210 = 210,   bCunused211 = 211,
		bCunused212 = 212,   bCunused213 = 213,   bCunused214 = 214,   bCunused215 = 215,
		bCunused216 = 216,   bCunused217 = 217,   bCunused218 = 218,   bCunused219 = 219,
		bCunused220 = 220,   bCunused221 = 221,   bCunused222 = 222,   bCunused223 = 223,
		bCunused224 = 224,   bCunused225 = 225,   bCunused226 = 226,   bCunused227 = 227,
		bCunused228 = 228,   bCunused229 = 229,   bCunused230 = 230,   bCunused231 = 231,
		bCunused232 = 232,   bCunused233 = 233,   bCunused234 = 234,   bCunused235 = 235,
		bCunused236 = 236,   bCunused237 = 237,   bCunused238 = 238,   bCunused239 = 239,
		bCunused240 = 240,   bCunused241 = 241,   bCunused242 = 242,   bCunused243 = 243,
		bCunused244 = 244,   bCunused245 = 245,   bCunused246 = 246,   bCunused247 = 247,
		bCunused248 = 248,   bCunused249 = 249,   bCunused250 = 250,   bCunused251 = 251,
		bCunused252 = 252,   bCunused253 = 253,   bCimpdep1 = 254, bCimpdep2 = 255;

	byte  // attributes of JVM Instructions (byte code attribute position in look-up table)
		bcapBase = 16, // currently maximal 12 bits for this attributes (could be increased to 20 bits.
		bcapBranch = bcapBase+0,   bcapCondBranch = bcapBase+1,
		bcapUncondBranch = bcapBase+2,   bcapReturn = bcapBase+3,
		bcapSwitch = bcapBase+4,   bcapCall = bcapBase+5,
		bcapNew = bcapBase+6,   bcapCpRef = bcapBase+7,// references const pool item
		bcapUndef = bcapBase+8;

	int[] bcAttrTab = {
		/* old format A:	0xsFFo'mLcc,
		 * format B:	0xsFFF'owLcc, binary: ssss ' ffff | ffff'ffff | oo ww ' LLLL 	| cccc'cccc
				s	bit[31..28] (-8 <= o <= 7) 4 bit, change of operand stack pointer (in slots):		slotPointer := slotPointer + SignExtend(s)
					   S=-8: stack change depends on operand type
				F	bit[27..16] (0 <= o <= 0xFFF) 12 bit, Flags: {Branch, CondBranch, UncondBranch, Return, Switch, Call, New, } (see const declarations)
				o	bit[15..14] (0 <= o <= 2) 2 bit, number of operands (0 undefined for this instruction)
				w	bit[13..12] (0 <= w <= 2) 2 bit, number of additional bytes for wide instructions
				L	bit[11.. 8] (0 <= o <= 5) 4 bit, instruction length: number of bytes
				cc	bit[ 7.. 0] (0 <= cc <= 255) 8 bit, operation code (opc)
			*/
			/*Abbreviations:
				RCP	Runtime Constant Pool
				imm	immediate, e.g. slot number for local vars
			*/
			0x00000100 | bCnop, // no operation

			0x10000100 | bCaconst_null, // push null

			0x10000100 | bCiconst_m1, // push int const  -1
			0x10000100 | bCiconst_0, // push int const  0
			
			0x10000100 | bCiconst_1, // ..
			0x10000100 | bCiconst_2, // ..
			0x10000100 | bCiconst_3, // ..
			0x10000100 | bCiconst_4, // ..
			0x10000100 | bCiconst_5, // push int const  5

			0x20000100 | bClconst_0, // push long const  0
			0x20000100 | bClconst_1, // push long const  1

			0x10000100 | bCfconst_0, // push float const  0
			0x10000100 | bCfconst_1, // push float const  1
			0x10000100 | bCfconst_2, // push float const  2

			0x20000100 | bCdconst_0, // push double const  0
			0x20000100 | bCdconst_1, // push double const  1

			0x10000200 | bCbipush, // push immediate byte 
			0x10000300 | bCsipush, // push immediate short 

			0x10000200 | (1<<bcapCpRef) | bCldc, // push int or float from RCP (1 byte index)
			0x10000300 | (1<<bcapCpRef) | bCldc_w, // push int or float from RCP (2 bytes index)
			0x20000300 | (1<<bcapCpRef) | bCldc2_w, // push long or double from RCP (2 bytes index)

			0x10001200 | bCiload, //{wide} push int from local var[imm]
			0x20001200 | bClload, //{wide} push long from ..
			0x10001200 | bCfload, //{wide} push float from ..
			0x20001200 | bCdload, //{wide} push double from ..
			0x10001200 | bCaload, //{wide} push reference from ..

			0x10000100 | bCiload_0, // push int from locVar[0]
			0x10000100 | bCiload_1, // push int from locVar[1]
			0x10000100 | bCiload_2, // push int from locVar[2]
			0x10000100 | bCiload_3, // push int from locVar[3]

			0x20000100 | bClload_0, // push long from locVar[0,1]
			0x20000100 | bClload_1, // push long from locVar[1,2]
			0x20000100 | bClload_2, // push long from locVar[2,3]
			0x20000100 | bClload_3, // push long from locVar[3,4]

			0x10000100 | bCfload_0, // push float from locVar[0]
			0x10000100 | bCfload_1, // push float from locVar[1]
			0x10000100 | bCfload_2, // push float from locVar[2]
			0x10000100 | bCfload_3, // push float from locVar[3]

			0x20000100 | bCdload_0, // push double from locVar[0,1]
			0x20000100 | bCdload_1, // push double from locVar[1,2]
			0x20000100 | bCdload_2, // push double from locVar[2,3]
			0x20000100 | bCdload_3, // push double from locVar[3,4]

			0x10000100 | bCaload_0, // push reference from locVar[0]
			0x10000100 | bCaload_1, // push reference from locVar[1]
			0x10000100 | bCaload_2, // push reference from locVar[2]
			0x10000100 | bCaload_3, // push reference from locVar[3]

			0xF0000100 | bCiaload, // push int from array
			0x00000100 | bClaload, // push long from array
			0xF0000100 | bCfaload, // push float from array
			0x00000100 | bCdaload, // push double from array
			0xF0000100 | bCaaload, // push reference from array

			0xF0000100 | bCbaload, // push byte or boolean from array
			0xF0000100 | bCcaload, // push char from array
			0xF0000100 | bCsaload, // push short from array

			0xF0001200 | bCistore, //{wide} locVar[imm] = (int) top, pop
			0xE0001200 | bClstore, //{wide} locVar[imm,+1] = (long) top2, pop2
			0xF0001200 | bCfstore, //{wide} locVar[imm] = (float) top, pop
			0xE0001200 | bCdstore, //{wide} locVar[imm,+1] = (double) top2, pop2
			0xF0001200 | bCastore, //{wide} locVar[imm] = (reference) top, pop

			0xF0000100 | bCistore_0, // locVar[0] = (int) top, pop
			0xF0000100 | bCistore_1, // locVar[1] = (int) top, pop
			0xF0000100 | bCistore_2, // locVar[2] = (int) top, pop
			0xF0000100 | bCistore_3, // locVar[3] = (int) top, pop

			0xE0000100 | bClstore_0, // locVar[0,1] = (long) top2, pop2
			0xE0000100 | bClstore_1, // locVar[1,2] = (long) top2, pop2
			0xE0000100 | bClstore_2, // locVar[2,3] = (long) top2, pop2
			0xE0000100 | bClstore_3, // locVar[3,4] = (long) top2, pop2

			0xF0000100 | bCfstore_0, // locVar[0] = (float) top, pop
			0xF0000100 | bCfstore_1, // locVar[1] = (float) top, pop
			0xF0000100 | bCfstore_2, // locVar[2] = (float) top, pop
			0xF0000100 | bCfstore_3, // locVar[3] = (float) top, pop

			0xE0000100 | bCdstore_0, // locVar[0,1] = (double) top2, pop2
			0xE0000100 | bCdstore_1, // locVar[1,2] = (double) top2, pop2
			0xE0000100 | bCdstore_2, // locVar[2,3] = (double) top2, pop2
			0xE0000100 | bCdstore_3, // locVar[3,4] = (double) top2, pop2

			0xF0000100 | bCastore_0, // locVar[0] = (reference) top, pop
			0xF0000100 | bCastore_1, // locVar[1] = (reference) top, pop
			0xF0000100 | bCastore_2, // locVar[2] = (reference) top, pop
			0xF0000100 | bCastore_3, // locVar[3] = (reference) top, pop

			// ---- store top of stack into array
			0xD0000100 | bCiastore, // {top-2}[top-1] = (int)top, pop3
			0xC0000100 | bClastore, // {top-3}[top-2] = (long)top, pop4
			0xD0000100 | bCfastore, // {top-2}[top-1] = (float)top, pop3
			0xC0000100 | bCdastore, // {top-3}[top-2] = (double)top, pop4
			0xD0000100 | bCaastore, // {top-2}[top-1] = (reference)top, pop3
			0xD0000100 | bCbastore, // {top-2}[top-1] = (byte or boolean)top, pop3
			0xD0000100 | bCcastore, // {top-2}[top-1] = (char)top, pop3
			0xD0000100 | bCsastore, // {top-2}[top-1] = (short)top, pop3

			0xF0000100 | bCpop, // pop category 1 value
			0xE0000100 | bCpop2, // pop the top one or two operand stack values

			0x10000100 | bCdup, // Duplicate the top C1 operand stack value
			0x10000100 | bCdup_x1, // 
			0x10000100 | bCdup_x2, // 
			0x20000100 | bCdup2, // 
			0x20000100 | bCdup2_x1, // 
			0x20000100 | bCdup2_x2, // 

			0x00000100 | bCswap, // swap the top two slots

			0xF0000100 | bCiadd, // [top-1] = [top-1]+[top]
			0xE0000100 | bCladd, // [top-1] = [top-1]+[top,top-1]
			0xF0000100 | bCfadd,
			0xE0000100 | bCdadd,

			0xF0000100 | bCisub, // 
			0xE0000100 | bClsub, // 
			0xF0000100 | bCfsub, // 
			0xE0000100 | bCdsub, // 

			0xF0000100 | bCimul, // 
			0xE0000100 | bClmul, // 
			0xF0000100 | bCfmul, // 
			0xE0000100 | bCdmul, // 

			0xF0000100 | bCidiv, // 
			0xE0000100 | bCldiv, // 
			0xF0000100 | bCfdiv, // 
			0xE0000100 | bCddiv, // 

			0xF0000100 | bCirem, // 
			0xE0000100 | bClrem, // 
			0xF0000100 | bCfrem, // 
			0xE0000100 | bCdrem, // 

			0x00000100 | bCineg, // 
			0x00000100 | bClneg, // 
			0x00000100 | bCfneg, // 
			0x00000100 | bCdneg, // 

			0xF0000100 | bCishl, // 
			0xF0000100 | bClshl, // 
			0xF0000100 | bCishr, // 
			0xF0000100 | bClshr, // 
			0xF0000100 | bCiushr, // 
			0xF0000100 | bClushr, // 

			0xF0000100 | bCiand, // 
			0xE0000100 | bCland, // 
			0xF0000100 | bCior, // 
			0xE0000100 | bClor, // 
			0xF0000100 | bCixor, // 
			0xE0000100 | bClxor, // 

			0x00002300 | bCiinc, //{wide}  

			0x10000100 | bCi2l, // convert int to long
			0x00000100 | bCi2f, // convert int to float
			0x10000100 | bCi2d, // convert int to double

			0xF0000100 | bCl2i, // convert long to int
			0xF0000100 | bCl2f, // convert long to float
			0x00000100 | bCl2d, // convert long to double

			0x00000100 | bCf2i, // convert float to int
			0x10000100 | bCf2l, // convert float to long
			0x10000100 | bCf2d, // convert float to double

			0xF0000100 | bCd2i, // convert double to int
			0x00000100 | bCd2l, // convert double to long
			0xF0000100 | bCd2f, // convert double to float

			0x00000100 | bCi2b, // convert int to byte
			0x00000100 | bCi2c, // convert int to char
			0x00000100 | bCi2s, // convert int to short

			0xD0000100 | bClcmp, // 
			0xF0000100 | bCfcmpl, // 
			0xF0000100 | bCfcmpg, // 
			0xD0000100 | bCdcmpl, // 
			0xD0000100 | bCdcmpg, // 

			0xF0004300 | (1<<bcapCondBranch) | (1<<bcapBranch) | bCifeq, // branch if ([top] == 0)
			0xF0004300 | (1<<bcapCondBranch) | (1<<bcapBranch) | bCifne, // branch if ([top] != 0)
			0xF0004300 | (1<<bcapCondBranch) | (1<<bcapBranch) | bCiflt, // branch if ([top] < 0)
			0xF0004300 | (1<<bcapCondBranch) | (1<<bcapBranch) | bCifge, // branch if ([top] >= 0)
			0xF0004300 | (1<<bcapCondBranch) | (1<<bcapBranch) | bCifgt,  // branch if ([top] > 0)
			0xF0004300 | (1<<bcapCondBranch) | (1<<bcapBranch) | bCifle,  // branch if ([top] <= 0)

			0xE0008300 | (1<<bcapCondBranch) | (1<<bcapBranch) | bCif_icmpeq, // branch if ([top-1] == [top] )
			0xE0008300 | (1<<bcapCondBranch) | (1<<bcapBranch) | bCif_icmpne, // branch if ([top-1] != [top] )
			0xE0008300 | (1<<bcapCondBranch) | (1<<bcapBranch) | bCif_icmplt, // branch if ([top-1] < [top] )
			0xE0008300 | (1<<bcapCondBranch) | (1<<bcapBranch) | bCif_icmpge, // branch if ([top-1] >= [top] )
			0xE0008300 | (1<<bcapCondBranch) | (1<<bcapBranch) | bCif_icmpgt, // branch if ([top-1] > [top] )
			0xE0008300 | (1<<bcapCondBranch) | (1<<bcapBranch) | bCif_icmple, // branch if ([top-1] <= [top] )

			0xE0008300 | (1<<bcapCondBranch) | (1<<bcapBranch) | bCif_acmpeq, // branch if ([top-1] == [top] )
			0xE0008300 | (1<<bcapCondBranch) | (1<<bcapBranch) | bCif_acmpne, // branch if ([top-1] != [top] )
		
			0x00000300 | (1<<bcapUncondBranch) | (1<<bcapBranch) | bCgoto, // branch

			0x10000300 | (1<<bcapCall) | bCjsr, // jump to subroutine

			0x00001200 | (1<<bcapReturn) | (1<<bcapBranch) | bCret, //{wide} return ref		

			0xF0000000 | (1<<bcapSwitch) | (1<<bcapBranch) | bCtableswitch,
			0xF0000000 | (1<<bcapSwitch) | (1<<bcapBranch) | bClookupswitch,

			0xF0000100 | (1<<bcapReturn) | (1<<bcapBranch) | bCireturn, // return int
			0xE0000100 | (1<<bcapReturn) | (1<<bcapBranch) | bClreturn, // return long
			0xF0000100 | (1<<bcapReturn) | (1<<bcapBranch) | bCfreturn, // return float
			0xE0000100 | (1<<bcapReturn) | (1<<bcapBranch) | bCdreturn, // return double
			0xF0000100 | (1<<bcapReturn) | (1<<bcapBranch) | bCareturn, // return ref

			0x00000100 | (1<<bcapReturn) | (1<<bcapBranch) | bCreturn, // return

			0x80000300 | (1<<bcapCpRef) | bCgetstatic, //  (1 byte index)
			0x80000300 | (1<<bcapCpRef) | bCputstatic,
			0x80000300 | (1<<bcapCpRef) | bCgetfield,
			0x80000300 | (1<<bcapCpRef) | bCputfield,

			0x80000300 | (1<<bcapCall) | (1<<bcapCpRef) | bCinvokevirtual, // pop objRef & arguments, push result
			0x80000300 | (1<<bcapCall) | (1<<bcapCpRef) | bCinvokespecial, // pop objRef & arguments, push result
			0x80000300 | (1<<bcapCall) | (1<<bcapCpRef) | bCinvokestatic, // pop arguments, push result
			0x80000500 | (1<<bcapCall) | (1<<bcapCpRef) | bCinvokeinterface,

			0x80000500 | bCxxxunusedxxx,

			0x10000300 | (1<<bcapCall) | (1<<bcapNew) | (1<<bcapCpRef) | bCnew, // create new object
			0x00000200 | (1<<bcapCall) | (1<<bcapNew) | bCnewarray, // create new array of base type
			0x00000300 | (1<<bcapCall) | (1<<bcapNew) | (1<<bcapCpRef) | bCanewarray, // create new array of referenc

			0x00000100 | bCarraylength,
			
			0x80000100 | bCathrow, // stack is cleared and then the old top ref is pushed back

			0x00000300 | (1<<bcapCpRef) | bCcheckcast,
			0x00000300 | (1<<bcapCpRef) | bCinstanceof,

			0xF0000100 | bCmonitorenter,
			0xF0000100 | bCmonitorexit,

			0x00000100 | bCwide,

			0x80000400 | (1<<bcapCall) | (1<<bcapNew) | (1<<bcapCpRef) | bCmultianewarray, // create new multidimensional array

			0xF0004300 | (1<<bcapCondBranch) | (1<<bcapBranch) | bCifnull, // branch if ([top] == null)
			0xF0004300 | (1<<bcapCondBranch) | (1<<bcapBranch) | bCifnonnull, // branch if ([top] != null)

			0x00000500 | (1<<bcapUncondBranch) | (1<<bcapBranch) | bCgoto_w,
			0x10000500 | (1<<bcapCall) | bCjsr_w,

			0x00000100 | bCbreakpoint,

			bCunused203,
			bCunused204,   bCunused205,   bCunused206,   bCunused207,
			bCunused208,   bCunused209,   bCunused210,   bCunused211,
			bCunused212,   bCunused213,   bCunused214,   bCunused215,
			bCunused216,   bCunused217,   bCunused218,   bCunused219,
			bCunused220,   bCunused221,   bCunused222,   bCunused223,
			bCunused224,   bCunused225,   bCunused226,   bCunused227,
			bCunused228,   bCunused229,   bCunused230,   bCunused231,
			bCunused232,   bCunused233,   bCunused234,   bCunused235,
			bCunused236,   bCunused237,   bCunused238,   bCunused239,
			bCunused240,   bCunused241,   bCunused242,   bCunused243,
			bCunused244,   bCunused245,   bCunused246,   bCunused247,
			bCunused248,   bCunused249,   bCunused250,   bCunused251,
			bCunused252,   bCunused253,
			
			0x00000100 | bCimpdep1,
			0x00000100 | bCimpdep2
	};
}
