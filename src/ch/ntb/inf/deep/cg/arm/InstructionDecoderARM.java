/*
 * Copyright 2011 - 2013 NTB University of Applied Sciences in Technology
 * Buchs, Switzerland, http://www.ntb.ch/inf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package ch.ntb.inf.deep.cg.arm;

import ch.ntb.inf.deep.cg.InstructionDecoder;

public class InstructionDecoderARM extends InstructionDecoder implements InstructionOpcs {

	/**
	 * Encode the assembler mnemonic into the machine instruction. Does not check
	 * if the parameters are correct.
	 * 
	 * @param mnemonic
	 *            String
	 * @return machine instruction
	 */
	@SuppressWarnings("unused")
	public int getCode(String mnemonic) {
		// masks
		int crb = 0x1F;
		int BD = 0xFFFC;
		int crf = 0x7;
		int BI = 0x1F;
		int BO = 0x1F;
		int CRM = 0xFF;
		int d = 0xFFFF;
		int FM = 0xFF;
		int fr = 0x1F;
		int IMM = 0xF;
		int L = 0x1;
		int LI = 0x3FFFFFC;
		int MB = 0x1F;
		int ME = 0x1F;
		int NB = 0x1F;
		int r = 0x1F;
		int SH = 0x1F;
		int SIMM = 0xFFFF;
		int SPR = 0x3FF;
		int TO = 0x1F;
		int UIMM = 0xFFFF;


		int res = 0;
		int firstIndex = mnemonic.indexOf(" ");
		int param1, param2, param3, param4, param5;
		String[] parts = new String[2];
		mnemonic = mnemonic.toLowerCase();

		// Format for the mnemonic should be "add *spaces* rD,rA,rB"
		if (firstIndex > 0) {
			parts[0] = mnemonic.substring(0, firstIndex);
			parts[1] = mnemonic.substring(firstIndex + 1, mnemonic.length());
			parts[1] = parts[1].trim().replace(" ", "");
		} else {
			parts[0] = mnemonic;
		}

		if (parts[0].equals("add")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

//			res = ppcAdd | (param1 << 21) | (param2 << 16) | (param3 << 11);

		} else if (parts[0].equals("adds")) {

			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

//			res = ppcAdd | (param1 << 21) | (param2 << 16) | (param3 << 11) | 1;
		} else if (parts[0].equals("addo")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x614;
		} else if (parts[0].equals("addo.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;
			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x615;
		} else if (parts[0].equals("addc")) {

			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x014;
		} else if (parts[0].equals("addc.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;
			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x015;
		} else if (parts[0].equals("addco")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x414;
		} else if (parts[0].equals("addco.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x415;
		} else if (parts[0].equals("adde")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x114;
		} else if (parts[0].equals("adde.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x115;
		} else if (parts[0].equals("addeo")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x514;
		} else if (parts[0].equals("addeo.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x515;
		} else if (parts[0].equals("addi")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & SIMM;

//			res = ppcAddi | (param1 << 21) | (param2 << 16) | param3;
		} else if (parts[0].equals("addic")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & SIMM;

			res = (0x0C << 26) | (param1 << 21) | (param2 << 16) | param3;
		} else if (parts[0].equals("addic.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & SIMM;

			res = (0x0D << 26) | (param1 << 21) | (param2 << 16) | param3;
		} else if (parts[0].equals("addis")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & SIMM;

			res = (0x0F << 26) | (param1 << 21) | (param2 << 16) | param3;
		} else if (parts[0].equals("addme")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | 0x01D4;
		} else if (parts[0].equals("addme.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | 0x01D5;
		} else if (parts[0].equals("addmeo")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | 0x05D4;
		} else if (parts[0].equals("addmeo.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | 0x05D5;
		} else if (parts[0].equals("addze")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | 0x0194;
		} else if (parts[0].equals("addze.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | 0x0195;
		} else if (parts[0].equals("addzeo")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | 0x0594;
		} else if (parts[0].equals("addzeo.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | 0x0595;
		} else if (parts[0].equals("and")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | 0x38;
		} else if (parts[0].equals("and.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | 0x39;
		} else if (parts[0].equals("andc")) {
			String[] param = parts[1].split(",");
			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;
			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | 0x78;
		} else if (parts[0].equals("andc.")) {
			String[] param = parts[1].split(",");
			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;
			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | 0x79;
		} else if (parts[0].equals("andi.")) {
			String[] param = parts[1].split(",");
			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & UIMM;
			res = (0x1C << 26) | (param2 << 21) | (param1 << 16) | param3;
		} else if (parts[0].equals("andis.")) {
			String[] param = parts[1].split(",");
			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & UIMM;
			res = (0x1D << 26) | (param2 << 21) | (param1 << 16) | param3;
		} else if (parts[0].equals("b")) {
			param1 = Integer.decode(parts[1]) & LI;
			res = (0x12 << 26) | param1 | 0x0;
		} else if (parts[0].equals("ba")) {
			param1 = Integer.decode(parts[1]) & LI;
			res = (0x12 << 26) | param1 | 0x2;
		} else if (parts[0].equals("bl")) {
			param1 = Integer.decode(parts[1]) & LI;
			res = (0x12 << 26) | param1 | 0x1;
		} else if (parts[0].equals("bla")) {
			param1 = Integer.decode(parts[1]) & LI;
			res = (0x12 << 26) | param1 | 0x3;
		} else if (parts[0].equals("bc")) {
			String[] param = parts[1].split(",");
			param1 = decodeBO(param[0]);
			assert param1 != 0 : "wrong BO field";
			param2 = decodeBI(param[1]);
			assert param2 != -1 : "wrong BI field";
			param3 = Integer.decode(param[2]) & BD;
			res = (0x10 << 26) | (param1 << 21) | (param2 << 16) | param3 | 0x0;
		} else if (parts[0].equals("bca")) {
			String[] param = parts[1].split(",");
			param1 = decodeBO(param[0]);
			assert param1 != 0 : "wrong BO field";
			param2 = decodeBI(param[1]);
			assert param2 != -1 : "wrong BI field";
			param3 = Integer.decode(param[2]) & BD;
			res = (0x10 << 26) | (param1 << 21) | (param2 << 16) | param3 | 0x2;
		} else if (parts[0].equals("bcl")) {
			String[] param = parts[1].split(",");
			param1 = decodeBO(param[0]);
			assert param1 != 0 : "wrong BO field";
			param2 = decodeBI(param[1]);
			assert param2 != -1 : "wrong BI field";
			param3 = Integer.decode(param[2]) & BD;
			res = (0x10 << 26) | (param1 << 21) | (param2 << 16) | param3 | 0x1;
		} else if (parts[0].equals("bcla")) {
			String[] param = parts[1].split(",");
			param1 = decodeBO(param[0]);
			assert param1 != 0 : "wrong BO field";
			param2 = decodeBI(param[1]);
			assert param2 != -1 : "wrong BI field";
			param3 = Integer.decode(param[2]) & BD;
			res = (0x10 << 26) | (param1 << 21) | (param2 << 16) | param3 | 0x3;
		} else if (parts[0].equals("bcctr")) {
			String[] param = parts[1].split(",");
			param1 = decodeBO(param[0]);
			assert param1 != 0 : "wrong BO field";
			param2 = decodeBI(param[1]);
			assert param2 != -1 : "wrong BI field";
			res = (0x13 << 26) | (param1 << 21) | (param2 << 16) | 0x0420;
		} else if (parts[0].equals("bcctrl")) {
			String[] param = parts[1].split(",");
			param1 = decodeBO(param[0]);
			assert param1 != 0 : "wrong BO field";
			param2 = decodeBI(param[1]);
			assert param2 != -1 : "wrong BI field";
			res = (0x13 << 26) | (param1 << 21) | (param2 << 16) | 0x0421;
		} else if (parts[0].equals("bclr")) {
			String[] param = parts[1].split(",");
			param1 = decodeBO(param[0]);
			assert param1 != 0 : "wrong BO field";
			param2 = decodeBI(param[1]);
			assert param2 != -1 : "wrong BI field";
			res = (0x13 << 26) | (param1 << 21) | (param2 << 16) | 0x0020;
		} else if (parts[0].equals("bclrl")) {
			String[] param = parts[1].split(",");
			param1 = decodeBO(param[0]);
			assert param1 != 0 : "wrong BO field";
			param2 = decodeBI(param[1]);
			assert param2 != -1 : "wrong BI field";
			res = (0x13 << 26) | (param1 << 21) | (param2 << 16) | 0x0021;
		} else if (parts[0].equals("cmp")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(3)) & crf;
			param2 = Integer.decode(param[1]) & L;
			param3 = Integer.parseInt(param[2].substring(1)) & r;
			param4 = Integer.parseInt(param[3].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 23) | (param2 << 21) | (param3 << 16) | (param4 << 11);
		} else if (parts[0].equals("cmpi")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(3)) & crf;
			param2 = Integer.decode(param[1]) & L;
			param3 = Integer.parseInt(param[2].substring(1)) & r;
			param4 = Integer.decode(param[3]) & SIMM;

			res = (0x0B << 26) | (param1 << 23) | (param2 << 21) | (param3 << 16) | param4;
		} else if (parts[0].equals("cmpl")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(3)) & crf;
			param2 = Integer.decode(param[1]) & L;
			param3 = Integer.parseInt(param[2].substring(1)) & r;
			param4 = Integer.parseInt(param[3].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 23) | (param2 << 21) | (param3 << 16) | (param4 << 11) | 0x40;
		} else if (parts[0].equals("cmpli")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(3)) & crf;
			param2 = Integer.decode(param[1]) & L;
			param3 = Integer.parseInt(param[2].substring(1)) & r;
			param4 = Integer.decode(param[3]) & UIMM;

			res = (0x0A << 26) | (param1 << 23) | (param2 << 21) | (param3 << 16) | param4;
		} else if (parts[0].equals("cntlzw")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | 0x34;
		} else if (parts[0].equals("cntlzw.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | 0x35;
		} else if (parts[0].equals("crand")) {
			String[] param = parts[1].split(",");

			param1 = decodeBI(param[0]);
			param2 = decodeBI(param[1]);
			param3 = decodeBI(param[2]);

			res = (0x13 << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x202;
		} else if (parts[0].equals("crandc")) {
			String[] param = parts[1].split(",");

			param1 = decodeBI(param[0]);
			param2 = decodeBI(param[1]);
			param3 = decodeBI(param[2]);

			res = (0x13 << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x102;
		} else if (parts[0].equals("creqv")) {
			String[] param = parts[1].split(",");

			param1 = decodeBI(param[0]);
			param2 = decodeBI(param[1]);
			param3 = decodeBI(param[2]);

			res = (0x13 << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x242;
		} else if (parts[0].equals("crnand")) {
			String[] param = parts[1].split(",");

			param1 = decodeBI(param[0]);
			param2 = decodeBI(param[1]);
			param3 = decodeBI(param[2]);

			res = (0x13 << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x1C2;
		} else if (parts[0].equals("crnor")) {
			String[] param = parts[1].split(",");

			param1 = decodeBI(param[0]);
			param2 = decodeBI(param[1]);
			param3 = decodeBI(param[2]);

			res = (0x13 << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x42;
		} else if (parts[0].equals("cror")) {
			String[] param = parts[1].split(",");

			param1 = decodeBI(param[0]);
			param2 = decodeBI(param[1]);
			param3 = decodeBI(param[2]);

			res = (0x13 << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x382;
		} else if (parts[0].equals("crorc")) {
			String[] param = parts[1].split(",");

			param1 = decodeBI(param[0]);
			param2 = decodeBI(param[1]);
			param3 = decodeBI(param[2]);

			res = (0x13 << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x342;
		} else if (parts[0].equals("crxor")) {
			String[] param = parts[1].split(",");

			param1 = decodeBI(param[0]);
			param2 = decodeBI(param[1]);
			param3 = decodeBI(param[2]);

			res = (0x13 << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x182;
		} else if (parts[0].equals("divw")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x3D6;
		} else if (parts[0].equals("divw.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x3D7;
		} else if (parts[0].equals("divwo")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x7D6;
		} else if (parts[0].equals("divwo.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x7D7;
		} else if (parts[0].equals("divwu")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x396;
		} else if (parts[0].equals("divwu.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x397;
		} else if (parts[0].equals("divwuo")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x796;
		} else if (parts[0].equals("divwuo.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | 0x797;
		} else if (parts[0].equals("eieio")) {

			res = (0x1F << 26) | 0x6AC;
		} else if (parts[0].equals("eqv")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | 0x238;
		} else if (parts[0].equals("eqv.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | 0x239;
		} else if (parts[0].equals("extsb")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | 0x774;
		} else if (parts[0].equals("extsb.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | 0x775;
		} else if (parts[0].equals("extsh")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | 0x734;
		} else if (parts[0].equals("extsh.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | 0x735;
		} else if (parts[0].equals("fabs")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 11) | (0x108 << 1);
		} else if (parts[0].equals("fabs.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 11) | (0x108 << 1) | 0x1;
		} else if (parts[0].equals("fadd")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x15 << 1);
		} else if (parts[0].equals("fadd.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x15 << 1) | 0x1;
		} else if (parts[0].equals("fadds")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;

			res = (0x3B << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x15 << 1);
		} else if (parts[0].equals("fadds.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;

			res = (0x3B << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x15 << 1) | 0x1;
		} else if (parts[0].equals("fcmpo")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(3)) & crf;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 23) | (param2 << 16) | (param3 << 11) | (0x20 << 1);
		} else if (parts[0].equals("fcmpu")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(3)) & crf;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 23) | (param2 << 16) | (param3 << 11);
		} else if (parts[0].equals("fctiw")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 11) | (0x0E << 1);
		} else if (parts[0].equals("fctiw.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 11) | (0x0E << 1) | 0x1;
		} else if (parts[0].equals("fctiwz")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 11) | (0x0F << 1);
		} else if (parts[0].equals("fctiwz.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 11) | (0x0F << 1) | 0x1;
		} else if (parts[0].equals("fdiv")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x12 << 1);
		} else if (parts[0].equals("fdiv.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x12 << 1) | 0x1;
		} else if (parts[0].equals("fdivs")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;

			res = (0x3B << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x12 << 1);
		} else if (parts[0].equals("fdivs.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;

			res = (0x3B << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x12 << 1) | 0x1;
		} else if (parts[0].equals("fmadd")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;
			param4 = Integer.parseInt(param[3].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 16) | (param4 << 11) | (param3 << 6) | (0x1D << 1);
		} else if (parts[0].equals("fmadd.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;
			param4 = Integer.parseInt(param[3].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 16) | (param4 << 11) | (param3 << 6) | (0x1D << 1) | 0x1;
		} else if (parts[0].equals("fmadds")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;
			param4 = Integer.parseInt(param[3].substring(2)) & fr;

			res = (0x3B << 26) | (param1 << 21) | (param2 << 16) | (param4 << 11) | (param3 << 6) | (0x1D << 1);
		} else if (parts[0].equals("fmadds.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;
			param4 = Integer.parseInt(param[3].substring(2)) & fr;

			res = (0x3B << 26) | (param1 << 21) | (param2 << 16) | (param4 << 11) | (param3 << 6) | (0x1D << 1) | 0x1;
		} else if (parts[0].equals("fmr")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 11) | (0x48 << 1);
		} else if (parts[0].equals("fmr.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 11) | (0x48 << 1) | 0x1;
		} else if (parts[0].equals("fmsub")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;
			param4 = Integer.parseInt(param[3].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 16) | (param4 << 11) | (param3 << 6) | (0x1C << 1);
		} else if (parts[0].equals("fmsub.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;
			param4 = Integer.parseInt(param[3].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 16) | (param4 << 11) | (param3 << 6) | (0x1C << 1) | 0x1;
		} else if (parts[0].equals("fmsubs")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;
			param4 = Integer.parseInt(param[3].substring(2)) & fr;

			res = (0x3B << 26) | (param1 << 21) | (param2 << 16) | (param4 << 11) | (param3 << 6) | (0x1C << 1);
		} else if (parts[0].equals("fmsubs.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;
			param4 = Integer.parseInt(param[3].substring(2)) & fr;

			res = (0x3B << 26) | (param1 << 21) | (param2 << 16) | (param4 << 11) | (param3 << 6) | (0x1C << 1) | 0x1;
		} else if (parts[0].equals("fmul")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 6) | (0x19 << 1);
		} else if (parts[0].equals("fmul.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 6) | (0x19 << 1) | 0x1;
		} else if (parts[0].equals("fmuls")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;

			res = (0x3B << 26) | (param1 << 21) | (param2 << 16) | (param3 << 6) | (0x19 << 1);
		} else if (parts[0].equals("fmuls.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;

			res = (0x3B << 26) | (param1 << 21) | (param2 << 16) | (param3 << 6) | (0x19 << 1) | 0x1;
		} else if (parts[0].equals("fnabs")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 11) | (0x88 << 1);
		} else if (parts[0].equals("fnabs.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 11) | (0x88 << 1) | 0x1;
		} else if (parts[0].equals("fneg")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 11) | (0x28 << 1);
		} else if (parts[0].equals("fneg.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 11) | (0x28 << 1) | 0x1;
		} else if (parts[0].equals("fnmadd")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;
			param4 = Integer.parseInt(param[3].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 16) | (param4 << 11) | (param3 << 6) | (0x1F << 1);
		} else if (parts[0].equals("fnmadd.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;
			param4 = Integer.parseInt(param[3].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 16) | (param4 << 11) | (param3 << 6) | (0x1F << 1) | 0x1;
		} else if (parts[0].equals("fnmadds")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;
			param4 = Integer.parseInt(param[3].substring(2)) & fr;

			res = (0x3B << 26) | (param1 << 21) | (param2 << 16) | (param4 << 11) | (param3 << 6) | (0x1F << 1);
		} else if (parts[0].equals("fnmadds.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;
			param4 = Integer.parseInt(param[3].substring(2)) & fr;

			res = (0x3B << 26) | (param1 << 21) | (param2 << 16) | (param4 << 11) | (param3 << 6) | (0x1F << 1) | 0x1;
		} else if (parts[0].equals("fnmsub")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;
			param4 = Integer.parseInt(param[3].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 16) | (param4 << 11) | (param3 << 6) | (0x1E << 1);
		} else if (parts[0].equals("fnmsub.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;
			param4 = Integer.parseInt(param[3].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 16) | (param4 << 11) | (param3 << 6) | (0x1E << 1) | 0x1;
		} else if (parts[0].equals("fnmsubs")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;
			param4 = Integer.parseInt(param[3].substring(2)) & fr;

			res = (0x3B << 26) | (param1 << 21) | (param2 << 16) | (param4 << 11) | (param3 << 6) | (0x1E << 1);
		} else if (parts[0].equals("fnmsubs.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;
			param4 = Integer.parseInt(param[3].substring(2)) & fr;

			res = (0x3B << 26) | (param1 << 21) | (param2 << 16) | (param4 << 11) | (param3 << 6) | (0x1E << 1) | 0x1;
		} else if (parts[0].equals("frsp")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 11) | (0x0C << 1);
		} else if (parts[0].equals("frsp.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 11) | (0x0C << 1) | 0x1;
		} else if (parts[0].equals("fsub")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x14 << 1);
		} else if (parts[0].equals("fsub.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x14 << 1) | 0x1;
		} else if (parts[0].equals("fsubs")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;

			res = (0x3B << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x14 << 1);
		} else if (parts[0].equals("fsubs.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;
			param3 = Integer.parseInt(param[2].substring(2)) & fr;

			res = (0x3B << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x14 << 1) | 0x1;
		} else if (parts[0].equals("icbi")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 16) | (param2 << 11) | (0x3D6 << 1);
		} else if (parts[0].equals("isync")) {

			res = (0x13 << 26) | (0x90 << 1);
		} else if (parts[0].equals("lbz")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x22 << 26) | (param1 << 21) | (param3 << 16) | param2;

		} else if (parts[0].equals("lbzu")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x23 << 26) | (param1 << 21) | (param3 << 16) | param2;

		} else if (parts[0].equals("lbzux")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x77 << 1);
		} else if (parts[0].equals("lbzx")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x57 << 1);
		} else if (parts[0].equals("lfd")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x32 << 26) | (param1 << 21) | (param3 << 16) | param2;

		} else if (parts[0].equals("lfdu")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x33 << 26) | (param1 << 21) | (param3 << 16) | param2;

		} else if (parts[0].equals("lfdux")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x277 << 1);
		} else if (parts[0].equals("lfdx")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x257 << 1);
		} else if (parts[0].equals("lfs")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x30 << 26) | (param1 << 21) | (param3 << 16) | param2;

		} else if (parts[0].equals("lfsu")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x31 << 26) | (param1 << 21) | (param3 << 16) | param2;

		} else if (parts[0].equals("lfsux")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x237 << 1);
		} else if (parts[0].equals("lfsx")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x217 << 1);
		} else if (parts[0].equals("lha")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x2A << 26) | (param1 << 21) | (param3 << 16) | param2;
		} else if (parts[0].equals("lhau")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x2B << 26) | (param1 << 21) | (param3 << 16) | param2;
		} else if (parts[0].equals("lhaux")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x177 << 1);
		} else if (parts[0].equals("lhax")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x157 << 1);
		} else if (parts[0].equals("lhbrx")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x316 << 1);
		} else if (parts[0].equals("lhz")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x28 << 26) | (param1 << 21) | (param3 << 16) | param2;
		} else if (parts[0].equals("lhzu")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x29 << 26) | (param1 << 21) | (param3 << 16) | param2;
		} else if (parts[0].equals("lhzux")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x137 << 1);
		} else if (parts[0].equals("lhzx")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x117 << 1);
		} else if (parts[0].equals("li")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(param[1]) & SIMM;

//			res = ppcAddi | (param1 << 21) | param2;
		} else if (parts[0].equals("lis")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(param[1]) & SIMM;

//			res = ppcAddis | (param1 << 21) | param2;
		} else if (parts[0].equals("lmw")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x2E << 26) | (param1 << 21) | (param3 << 16) | param2;
		} else if (parts[0].equals("lr")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

//			res = ppcOr | (param2 << 21) | (param1 << 16) | (param2 << 11);
		} else if (parts[0].equals("lswi")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & NB;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x255 << 1);
		} else if (parts[0].equals("lswx")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x215 << 1);
		} else if (parts[0].equals("lwarx")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x14 << 1);
		} else if (parts[0].equals("lwbrx")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x216 << 1);
		} else if (parts[0].equals("lwz")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x20 << 26) | (param1 << 21) | (param3 << 16) | param2;
		} else if (parts[0].equals("lwzu")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x21 << 26) | (param1 << 21) | (param3 << 16) | param2;
		} else if (parts[0].equals("lwzux")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x37 << 1);
		} else if (parts[0].equals("lwzx")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x17 << 1);
		} else if (parts[0].equals("mcrf")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(3)) & crf;
			param2 = Integer.parseInt(param[1].substring(3)) & crf;

			res = (0x13 << 26) | (param1 << 23) | (param2 << 18);
		} else if (parts[0].equals("mcrfs")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(3)) & crf;
			param2 = Integer.parseInt(param[1].substring(3)) & crf;

			res = (0x3F << 26) | (param1 << 23) | (param2 << 18) | (0x40 << 1);
		} else if (parts[0].equals("mcrxr")) {

			param1 = Integer.parseInt(parts[1].substring(3)) & crf;

			res = (0x1F << 26) | (param1 << 23) | (0x200 << 1);
		} else if (parts[0].equals("mfcr")) {

			param1 = Integer.parseInt(parts[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (0x13 << 1);
		} else if (parts[0].equals("mffs")) {

			param1 = Integer.parseInt(parts[1].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (0x247 << 1);
		} else if (parts[0].equals("mffs.")) {

			param1 = Integer.parseInt(parts[1].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 21) | (0x247 << 1) | 0x1;
		} else if (parts[0].equals("mfmsr")) {

			param1 = Integer.parseInt(parts[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (0x53 << 1);
		} else if (parts[0].equals("mfspr")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			try {
				int tempInt = Integer.decode(param[1]) & SPR;
				param2 = ((tempInt & 0x1F) << 5) | ((tempInt & 0x3E0) >> 5);
			} catch (NumberFormatException e) {
				if (param[1].equals("xer")) {
					param2 = 32;
				} else if (param[1].equals("lr")) {
					param2 = 256;
				} else if (param[1].equals("ctr")) {
					param2 = 288;
				} else if (param[1].equals("tbl_r")) {
					param2 = 392;
				} else if (param[1].equals("tbu_r")) {
					param2 = 424;
				} else if (param[1].equals("dsisr")) {
					param2 = 576;
				} else if (param[1].equals("dar")) {
					param2 = 608;
				} else if (param[1].equals("dec")) {
					param2 = 704;
				} else if (param[1].equals("srr0")) {
					param2 = 832;
				} else if (param[1].equals("srr1")) {
					param2 = 864;
				} else if (param[1].equals("eie")) {
					param2 = 514;
				} else if (param[1].equals("eid")) {
					param2 = 546;
				} else if (param[1].equals("nri")) {
					param2 = 578;
				} else if (param[1].equals("sprg0")) {
					param2 = 520;
				} else if (param[1].equals("sprg1")) {
					param2 = 552;
				} else if (param[1].equals("sprg2")) {
					param2 = 584;
				} else if (param[1].equals("sprg3")) {
					param2 = 616;
				} else if (param[1].equals("tbl_w")) {
					param2 = 904;
				} else if (param[1].equals("tbu_w")) {
					param2 = 936;
				} else if (param[1].equals("pvr")) {
					param2 = 1000;
				} else if (param[1].equals("iccst")) {
					param2 = 529;
				} else if (param[1].equals("icadr")) {
					param2 = 561;
				} else if (param[1].equals("icdat")) {
					param2 = 583;
				} else if (param[1].equals("fpecr")) {
					param2 = 991;
				} else if (param[1].equals("cmpa")) {
					param2 = 516;
				} else if (param[1].equals("cmpb")) {
					param2 = 548;
				} else if (param[1].equals("cmpc")) {
					param2 = 580;
				} else if (param[1].equals("cmpd")) {
					param2 = 612;
				} else if (param[1].equals("ecr")) {
					param2 = 644;
				} else if (param[1].equals("der")) {
					param2 = 677;
				} else if (param[1].equals("counta")) {
					param2 = 708;
				} else if (param[1].equals("countb")) {
					param2 = 740;
				} else if (param[1].equals("cmpe")) {
					param2 = 772;
				} else if (param[1].equals("cmpf")) {
					param2 = 804;
				} else if (param[1].equals("cmpg")) {
					param2 = 836;
				} else if (param[1].equals("cmph")) {
					param2 = 868;
				} else if (param[1].equals("lctrl1")) {
					param2 = 900;
				} else if (param[1].equals("lctrl2")) {
					param2 = 932;
				} else if (param[1].equals("ictrl")) {
					param2 = 964;
				} else if (param[1].equals("bar")) {
					param2 = 996;
				} else if (param[1].equals("dpdr")) {
					param2 = 723;
				} else {
					param2 = 0;
					assert false : "wrong SPR number";
				}
			}

			res = (0x1F << 26) | (param1 << 21) | (param2 << 11) | (0x153 << 1);
		} else if (parts[0].equals("mftb")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(param[1]) & SPR;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 11) | (0x173 << 1);
		} else if (parts[0].equals("mtcrf")) {
			String[] param = parts[1].split(",");

			param1 = Integer.decode(param[0]) & CRM;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 12) | (0x90 << 1);
		} else if (parts[0].equals("mtfsb0")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(3)) & crb;

			res = (0x3F << 26) | (param1 << 21) | (0x46 << 1);
		} else if (parts[0].equals("mtfsb0.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(3)) & crb;

			res = (0x3F << 26) | (param1 << 21) | (0x46 << 1) | 0x1;
		} else if (parts[0].equals("mtfsb1")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(3)) & crb;

			res = (0x3F << 26) | (param1 << 21) | (0x26 << 1);
		} else if (parts[0].equals("mtfsb1.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(3)) & crb;

			res = (0x3F << 26) | (param1 << 21) | (0x26 << 1) | 0x1;
		} else if (parts[0].equals("mtfsf")) {
			String[] param = parts[1].split(",");

			param1 = Integer.decode(param[0]) & FM;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 17) | (param2 << 11) | (0x2C7 << 1);
		} else if (parts[0].equals("mtfsf.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.decode(param[0]) & FM;
			param2 = Integer.parseInt(param[1].substring(2)) & fr;

			res = (0x3F << 26) | (param1 << 17) | (param2 << 11) | (0x2C7 << 1) | 0x1;
		} else if (parts[0].equals("mtfsfi")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(3)) & crf;
			param2 = Integer.decode(param[1]) & IMM;

			res = (0x3F << 26) | (param1 << 23) | (param2 << 12) | (0x86 << 1);
		} else if (parts[0].equals("mtfsfi.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(3)) & crf;
			param2 = Integer.decode(param[1]) & IMM;

			res = (0x3F << 26) | (param1 << 23) | (param2 << 12) | (0x86 << 1) | 0x1;
		} else if (parts[0].equals("mtmsr")) {

			param1 = Integer.parseInt(parts[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (0x92 << 1);
		} else if (parts[0].equals("mtspr")) {
			String[] param = parts[1].split(",");
			try {
				int tempInt = Integer.decode(param[0]) & SPR;
				param1 = ((tempInt & 0x1F) << 5) | ((tempInt & 0x3E0) >> 5);
			} catch (NumberFormatException e) {
				if (param[0].equals("xer")) {
					param1 = 32;
				} else if (param[0].equals("lr")) {
					param1 = 256;
				} else if (param[0].equals("ctr")) {
					param1 = 288;
				} else if (param[0].equals("tbl_r")) {
					param1 = 392;
				} else if (param[0].equals("tbu_r")) {
					param1 = 424;
				} else if (param[0].equals("dsisr")) {
					param1 = 576;
				} else if (param[0].equals("dar")) {
					param1 = 608;
				} else if (param[0].equals("dec")) {
					param1 = 704;
				} else if (param[0].equals("srr0")) {
					param1 = 832;
				} else if (param[0].equals("srr1")) {
					param1 = 864;
				} else if (param[0].equals("eie")) {
					param1 = 514;
				} else if (param[0].equals("eid")) {
					param1 = 546;
				} else if (param[0].equals("nri")) {
					param1 = 578;
				} else if (param[0].equals("sprg0")) {
					param1 = 520;
				} else if (param[0].equals("sprg1")) {
					param1 = 552;
				} else if (param[0].equals("sprg2")) {
					param1 = 584;
				} else if (param[0].equals("sprg3")) {
					param1 = 616;
				} else if (param[0].equals("tbl_w")) {
					param1 = 904;
				} else if (param[0].equals("tbu_w")) {
					param1 = 936;
				} else if (param[0].equals("pvr")) {
					param1 = 1000;
				} else if (param[0].equals("iccst")) {
					param1 = 529;
				} else if (param[0].equals("icadr")) {
					param1 = 561;
				} else if (param[0].equals("icdat")) {
					param1 = 583;
				} else if (param[0].equals("fpecr")) {
					param1 = 991;
				} else if (param[0].equals("cmpa")) {
					param1 = 516;
				} else if (param[0].equals("cmpb")) {
					param1 = 548;
				} else if (param[0].equals("cmpc")) {
					param1 = 580;
				} else if (param[0].equals("cmpd")) {
					param1 = 612;
				} else if (param[0].equals("ecr")) {
					param1 = 644;
				} else if (param[0].equals("der")) {
					param1 = 677;
				} else if (param[0].equals("counta")) {
					param1 = 708;
				} else if (param[0].equals("countb")) {
					param1 = 740;
				} else if (param[0].equals("cmpe")) {
					param1 = 772;
				} else if (param[0].equals("cmpf")) {
					param1 = 804;
				} else if (param[0].equals("cmpg")) {
					param1 = 836;
				} else if (param[0].equals("cmph")) {
					param1 = 868;
				} else if (param[0].equals("lctrl1")) {
					param1 = 900;
				} else if (param[0].equals("lctrl2")) {
					param1 = 932;
				} else if (param[0].equals("ictrl")) {
					param1 = 964;
				} else if (param[0].equals("bar")) {
					param1 = 996;
				} else if (param[0].equals("dpdr")) {
					param1 = 723;
				} else {
					param1 = 0;
					assert false : "wrong SPR number";
				}
			}
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 11) | (0x1D3 << 1);
		} else if (parts[0].equals("mulhw")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x4B << 1);
		} else if (parts[0].equals("mulhw.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x4B << 1) | 0x1;
		} else if (parts[0].equals("mulhwu")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x0B << 1);
		} else if (parts[0].equals("mulhwu.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x0B << 1) | 0x1;
		} else if (parts[0].equals("mulli")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & SIMM;

			res = (0x07 << 26) | (param1 << 21) | (param2 << 16) | param3;
		} else if (parts[0].equals("mullw")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0xEB << 1);
		} else if (parts[0].equals("mullw.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0xEB << 1) | 0x1;
		} else if (parts[0].equals("mullwo")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x1 << 10) | (0xEB << 1);
		} else if (parts[0].equals("mullwo.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x1 << 10) | (0xEB << 1) | 0x1;
		} else if (parts[0].equals("nand")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (0x1DC << 1);
		} else if (parts[0].equals("nand.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (0x1DC << 1) | 0x1;
		} else if (parts[0].equals("neg")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (0x68 << 1);
		} else if (parts[0].equals("neg.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (0x68 << 1) | 0x1;
		} else if (parts[0].equals("nego")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (0x1 << 10) | (0x68 << 1);
		} else if (parts[0].equals("nego.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (0x1 << 10) | (0x68 << 1) | 0x1;
		} else if (parts[0].equals("nor")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (0x7C << 1);
		} else if (parts[0].equals("nor.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (0x7C << 1) | 0x1;
		} else if (parts[0].equals("or")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (0x1BC << 1);
		} else if (parts[0].equals("or.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (0x1BC << 1) | 0x1;
		} else if (parts[0].equals("orc")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (0x19C << 1);
		} else if (parts[0].equals("orc.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (0x19C << 1) | 0x1;
		} else if (parts[0].equals("ori")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & UIMM;

			res = (0x18 << 26) | (param2 << 21) | (param1 << 16) | param3;
		} else if (parts[0].equals("oris")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & UIMM;

			res = (0x19 << 26) | (param2 << 21) | (param1 << 16) | param3;
		} else if (parts[0].equals("rfi")) {

			res = (0x13 << 26) | (0x32 << 1);
		} else if (parts[0].equals("rlwimi")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & SH;
			param4 = Integer.decode(param[3]) & MB;
			param5 = Integer.decode(param[4]) & ME;

			res = (0x14 << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (param4 << 6) | (param5 << 1);
		} else if (parts[0].equals("rlwimi.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & SH;
			param4 = Integer.decode(param[3]) & MB;
			param5 = Integer.decode(param[4]) & ME;

			res = (0x14 << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (param4 << 6) | (param5 << 1) | 0x1;
		} else if (parts[0].equals("rlwinm")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & SH;
			param4 = Integer.decode(param[3]) & MB;
			param5 = Integer.decode(param[4]) & ME;

			res = (0x15 << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (param4 << 6) | (param5 << 1);
		} else if (parts[0].equals("rlwinm.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & SH;
			param4 = Integer.decode(param[3]) & MB;
			param5 = Integer.decode(param[4]) & ME;

			res = (0x15 << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (param4 << 6) | (param5 << 1) | 0x1;
		} else if (parts[0].equals("rlwnm")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & SH;
			param4 = Integer.decode(param[3]) & MB;
			param5 = Integer.decode(param[4]) & ME;

			res = (0x17 << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (param4 << 6) | (param5 << 1);
		} else if (parts[0].equals("rlwnm.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & SH;
			param4 = Integer.decode(param[3]) & MB;
			param5 = Integer.decode(param[4]) & ME;

			res = (0x17 << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (param4 << 6) | (param5 << 1) | 0x1;
		} else if (parts[0].equals("sc")) {

			res = (0x11 << 26) | (0x1 << 1);
		} else if (parts[0].equals("slw")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (0x18 << 1);
		} else if (parts[0].equals("slw.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (0x18 << 1) | 0x1;
		} else if (parts[0].equals("sraw")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (0x318 << 1);
		} else if (parts[0].equals("sraw.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (0x318 << 1) | 0x1;
		} else if (parts[0].equals("srawi")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & SH;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (0x338 << 1);
		} else if (parts[0].equals("srawi.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & SH;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (0x338 << 1) | 0x1;
		} else if (parts[0].equals("srw")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (0x218 << 1);
		} else if (parts[0].equals("srw.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (0x218 << 1) | 0x1;
		} else if (parts[0].equals("stb")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x26 << 26) | (param1 << 21) | (param3 << 16) | param2;
		} else if (parts[0].equals("stbu")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x27 << 26) | (param1 << 21) | (param3 << 16) | param2;
		} else if (parts[0].equals("stbux")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0xF7 << 1);
		} else if (parts[0].equals("stbx")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0xD7 << 1);
		} else if (parts[0].equals("stfd")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x36 << 26) | (param1 << 21) | (param3 << 16) | param2;
		} else if (parts[0].equals("stfdu")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x37 << 26) | (param1 << 21) | (param3 << 16) | param2;
		} else if (parts[0].equals("stfdux")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x2F7 << 1);
		} else if (parts[0].equals("stfdx")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x2D7 << 1);
		} else if (parts[0].equals("stfiwx")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x3D7 << 1);
		} else if (parts[0].equals("stfs")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x34 << 26) | (param1 << 21) | (param3 << 16) | param2;
		} else if (parts[0].equals("stfsu")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x35 << 26) | (param1 << 21) | (param3 << 16) | param2;
		} else if (parts[0].equals("stfsux")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x2B7 << 1);
		} else if (parts[0].equals("stfsx")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(2)) & fr;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x297 << 1);
		} else if (parts[0].equals("sth")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x2C << 26) | (param1 << 21) | (param3 << 16) | param2;
		} else if (parts[0].equals("sthbrx")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x396 << 1);
		} else if (parts[0].equals("sthu")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x2D << 26) | (param1 << 21) | (param3 << 16) | param2;
		} else if (parts[0].equals("sthux")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x1B7 << 1);
		} else if (parts[0].equals("sthx")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x197 << 1);
		} else if (parts[0].equals("stmw")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x2F << 26) | (param1 << 21) | (param3 << 16) | param2;
		} else if (parts[0].equals("stswi")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & NB;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x205 << 1);
		} else if (parts[0].equals("stswx")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x295 << 1);
		} else if (parts[0].equals("stw")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x24 << 26) | (param1 << 21) | (param3 << 16) | param2;
		} else if (parts[0].equals("stwbrx")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x296 << 1);
		} else if (parts[0].equals("stwcx.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x96 << 1) | 0x1;
		} else if (parts[0].equals("stwu")) {
			String[] param = parts[1].split(",");
			String[] SpParam = param[1].split("\\x28");// 0x28=='('
			SpParam[1] = SpParam[1].substring(0, SpParam[1].length() - 1);

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(SpParam[0]) & d;
			param3 = Integer.parseInt(SpParam[1].substring(1)) & r;

			res = (0x25 << 26) | (param1 << 21) | (param3 << 16) | param2;
		} else if (parts[0].equals("stwux")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0xB7 << 1);
		} else if (parts[0].equals("stwx")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x97 << 1);
		} else if (parts[0].equals("subf")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x28 << 1);
		} else if (parts[0].equals("subf.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x28 << 1) | 0x1;
		} else if (parts[0].equals("subfo")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x1 << 10) | (0x28 << 1);
		} else if (parts[0].equals("subfo.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x1 << 10) | (0x28 << 1) | 0x1;
		} else if (parts[0].equals("subfc")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x08 << 1);
		} else if (parts[0].equals("subfc.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x08 << 1) | 0x1;
		} else if (parts[0].equals("subfco")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x1 << 10) | (0x08 << 1);
		} else if (parts[0].equals("subfco.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x1 << 10) | (0x08 << 1) | 0x1;
		} else if (parts[0].equals("subfe")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x88 << 1);
		} else if (parts[0].equals("subfe.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x88 << 1) | 0x1;
		} else if (parts[0].equals("subfeo")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x1 << 10) | (0x88 << 1);
		} else if (parts[0].equals("subfeo.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x1 << 10) | (0x88 << 1) | 0x1;
		} else if (parts[0].equals("subfic")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & SIMM;

			res = (0x08 << 26) | (param1 << 21) | (param2 << 16) | param3;
		}
		else if (parts[0].equals("subfme")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (0xE8 << 1);
		} else if (parts[0].equals("subfme.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (0xE8 << 1) | 0x1;
		} else if (parts[0].equals("subfmeo")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (0x1 << 10) | (0xE8 << 1);
		} else if (parts[0].equals("subfmeo.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (0x1 << 10) | (0xE8 << 1) | 0x1;
		} else if (parts[0].equals("subfze")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (0xC8 << 1);
		} else if (parts[0].equals("subfze.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (0xC8 << 1) | 0x1;
		} else if (parts[0].equals("subfzeo")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (0x1 << 10) | (0xC8 << 1);
		} else if (parts[0].equals("subfzeo.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (0x1 << 10) | (0xC8 << 1) | 0x1;
		} else if (parts[0].equals("sync")) {

			res = (0x1f << 26) | (0x256 << 1);
		} else if (parts[0].equals("tw")) {
			String[] param = parts[1].split(",");

			param1 = decodeTO(param[0]);
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param1 << 21) | (param2 << 16) | (param3 << 11) | (0x04 << 1);
		} else if (parts[0].equals("twi")) {
			String[] param = parts[1].split(",");

			param1 = decodeTO(param[0]);
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & SIMM;

			res = (0x03 << 26) | (param1 << 21) | (param2 << 16) | param3;
		} else if (parts[0].equals("xor")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (0x13C << 1);
		} else if (parts[0].equals("xor.")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = (0x1F << 26) | (param2 << 21) | (param1 << 16) | (param3 << 11) | (0x13C << 1) | 0x1;
		} else if (parts[0].equals("xori")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & UIMM;

			res = (0x1A << 26) | (param2 << 21) | (param1 << 16) | param3;
		} else if (parts[0].equals("xoris")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.decode(param[2]) & UIMM;

			res = (0x1B << 26) | (param2 << 21) | (param1 << 16) | param3;
		} else {
			assert false : "no such instruction";
		}

		//		System.out.println(Integer.toBinaryString(res));
		return res;
	}

	private static int decodeBO(String param) {
//		if (param.equals("iffalse")) return BOfalse;
//		else if (param.equals("iftrue")) return BOtrue;
//		else if (param.equals("always")) return BOalways;
//		else return 0;
		return 0;
	}

	private static int decodeBI(String param) {
		if (param.equals("0")) return 0;
		else if (param.equals("crf0[so]")) return CRF0SO;
		else if (param.equals("crf0[eq]")) return CRF0EQ;
		else if (param.equals("crf0[gt]")) return CRF0GT;
		else if (param.equals("crf0[lt]")) return CRF0LT;
		else if (param.equals("crf1[so]")) return CRF1SO;
		else if (param.equals("crf1[eq]")) return CRF1EQ;
		else if (param.equals("crf1[gt]")) return CRF1GT;
		else if (param.equals("crf1[lt]")) return CRF1LT;
		else return -1;
	}

	private static int decodeTO(String param) {
		if (param.equals("ifequal")) return TOifequal;
		else if (param.equals("ifless")) return TOifless;
		else if (param.equals("ifgreater")) return TOifgreater;
		else if (param.equals("ifgeu")) return TOifgeU;
		else if (param.equals("always")) return TOalways;
		else return 0;
	}

	public String getMnemonic(Integer instr) {
		int cond = (instr >>> 28) & 0xf;
		int op = (instr >>> 25) & 0x7;
		if (cond != 0xf)  {	// conditional instructions 
			switch (op) {
			case 0: {	// data processing and miscellaneous
				int op1 = (instr >>> 20) & 0x1f;
				int op2 = (instr >>> 4) & 0xf;
				if ((op1 & 0x19) != 0x10) {
					if ((op2 & 1) == 0) {	// data processing (register)
						int n = (instr >>> 16) & 0xf;
						int d = (instr >>> 12) & 0xf;
						int m = instr & 0xf;
						switch (op1) {
						case 8: return "add " + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;
						case 9:	return "adds " + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", R" + m;
						default: break;
						}
					}
				}
				break;
			}	
			case 1: {	// data processing and miscellaneous (immediate)
				int op1 = (instr >>> 20) & 0x1f;
				if ((op1 & 0x19) != 0x10) { // data processing (immediate)
					int n = (instr >>> 16) & 0xf;
					int d = (instr >>> 12) & 0xf;
					int imm12 = instr & 0xfff;
					switch (op1) {
					case 4: return "sub " + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + imm12;
					case 5: return "subs " + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + imm12;
					case 6: return "rsb " + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + imm12;
					case 7: return "rsbs " + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + imm12;
					case 8: return "add " + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + imm12;
					case 9:	return "adds " + (cond!=condAlways?condString[cond]:"") + " R" + d + ", R" + n + ", #" + imm12;
					default: break;
					}					
				}
				break;
			}	
			case 2: {	// load store word and unsigned byte
				break;
			}	
			default: break;
			}
			
		} else {	// unconditional instructions 
			
		}
		return "undefined";
	}

	static {
	}
}
