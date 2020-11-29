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

package org.deepjava.cg.ppc;

import org.deepjava.cg.InstructionDecoder;

public class InstructionDecoderPPC extends InstructionDecoder implements InstructionOpcs {

	/**
	 * Encode the assembler mnemonic into the machine instruction. Does not check
	 * if the parameters are correct.
	 * 
	 * @author NTB\millischer 07.12.2009, 
	 * @author NTB\Urs Graf 11.9.10 
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

		// Format for the Mnemonic shout be:
		// add *spaces* rD,rA,rB
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

			res = ppcAdd | (param1 << 21) | (param2 << 16) | (param3 << 11);

		} else if (parts[0].equals("add.")) {

			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.parseInt(param[1].substring(1)) & r;
			param3 = Integer.parseInt(param[2].substring(1)) & r;

			res = ppcAdd | (param1 << 21) | (param2 << 16) | (param3 << 11) | 1;
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

			res = ppcAddi | (param1 << 21) | (param2 << 16) | param3;
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

			res = ppcAddi | (param1 << 21) | param2;
		} else if (parts[0].equals("lis")) {
			String[] param = parts[1].split(",");

			param1 = Integer.parseInt(param[0].substring(1)) & r;
			param2 = Integer.decode(param[1]) & SIMM;

			res = ppcAddis | (param1 << 21) | param2;
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

			res = ppcOr | (param2 << 21) | (param1 << 16) | (param2 << 11);
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
		if (param.equals("iffalse")) return BOfalse;
		else if (param.equals("iftrue")) return BOtrue;
		else if (param.equals("always")) return BOalways;
		else return 0;
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

	private static String getBIString(int BI) {
		if (BI == CRF0SO) return "CRF0[SO]";
		else if (BI == CRF0EQ) return "CRF0[EQ]";
		else if (BI == CRF0GT) return "CRF0[GT]";
		else if (BI == CRF0LT) return "CRF0[LT]";
		else if (BI == CRF1SO) return "CRF1[SO]";
		else if (BI == CRF1EQ) return "CRF1[EQ]";
		else if (BI == CRF1GT) return "CRF1[GT]";
		else if (BI == CRF1LT) return "CRF1[LT]";
		else return "";
	}

	private static int decodeTO(String param) {
		if (param.equals("ifequal")) return TOifequal;
		else if (param.equals("ifless")) return TOifless;
		else if (param.equals("ifgreater")) return TOifgreater;
		else if (param.equals("ifgeu")) return TOifgeU;
		else if (param.equals("always")) return TOalways;
		else return 0;
	}

	public String getMnemonic(Integer machineInstr) {
		int opcode = (machineInstr & 0xFC000000) >>> (31 - 5);

			int S = (machineInstr & 0x3E00000) >>> (31 - 10);
			int D = (machineInstr & 0x3E00000) >>> (31 - 10);
			int A = (machineInstr & 0x1F0000) >>> (31 - 15);
			int B = (machineInstr & 0xF800) >>> (31 - 20);
			int C = (machineInstr & 0x7C0) >>> (31 - 25);

			int IMM = (machineInstr & 0xF000) >>> (31 - 19);

			int SH = (machineInstr & 0xF800) >>> (31 - 20);
			int MB = (machineInstr & 0x7C0) >>> (31 - 25);
			int ME = (machineInstr & 0x3E) >>> (31 - 30);

			int NB = (machineInstr & 0xF800) >>> (31 - 20);
			int FM = (machineInstr & 0x1FE0000) >>> (31 - 14);
			int CRM = (machineInstr & 0xFF000) >>> (31 - 19);
			int UIMM = (machineInstr & 0xFFFF);
			short simm = (short) (machineInstr & 0xFFFF);
			int SIMM = simm;
			int spr = (machineInstr & 0x1FF800) >>> (31 - 20);
			int SPR = (spr >> 5) + ((spr & 0x1f) << 5);
			int aa = (machineInstr & 0x02) >>> (31 - 30);
			int lk = (machineInstr & 0x01);
			int Rc = lk;
			int d = (short) (machineInstr & 0xFFFF);
			int li = (machineInstr & 0x3FFFFFC) << 6 >> 6; 
			int OE = (machineInstr & 0x400) >>> (31 - 21);
			int TO = (machineInstr & 0x3E00000) >>> (31 - 10);
			int BO = (machineInstr & 0x3E00000) >>> (31 - 10);
			int BI = (machineInstr & 0x1F0000) >>> (31 - 15);
			int BD = (short)(machineInstr & 0xFFFC);
			int crfD = (machineInstr & 0x3800000) >>> (31 - 8);
			int crfS = (machineInstr & 0x1C0000) >>> (31 - 13);
			int L = (machineInstr & 0x200000) >>> (31 - 10);

			int XO21to30 = (machineInstr & 0x7FE) >>> 1;
			int XO22to30 = (machineInstr & 0x3FE) >>> 1;
			int XO26to30 = (machineInstr & 0x3E) >>> 1;

			switch (opcode) {
			case 0x03:
				return "twi  " + TOstring[TO] + ", r" + A + ", " + SIMM;
			case 0x07:
				return "mulli  r" + D + ", r" + A + ", " + SIMM;
			case 0x08:
				return "subfic  r" + D + ", r" + A + ", " + SIMM;
			case 0x0A:
				return "cmpli  crf" + crfD + ", " + L + ", r" + A + ", " + SIMM;
			case 0x0B:
				return "cmpi  crf" + crfD + ", " + L + ", r" + A + ", " + SIMM;
			case 0x0C:
				return "addic  r" + D + ", r" + A + ", " + SIMM;
			case 0x0D:
				return "addic.  r" + D + ", r" + A + ", " + SIMM;
			case 0x0E:
				if (A == 0) {
					return "li  r" + D + ", " + SIMM;
				} else {
					return "addi  r" + D + ", r" + A + ", " + SIMM;
				}
			case 0x0F:
				if (A == 0) {
					return "lis  r" + D + ", " + SIMM;
				} else {
					return "addis  r" + D + ", r" + A + ", " + SIMM;
				}
			case 0x10:
				if (aa == 0 && lk == 0) {
					return "bc  " + BOstring[BO] + ", " + BIstring[BI] + ", " + BD;
				} else if (aa == 1 && lk == 0) {
					return "bca  " + BOstring[BO] + ", " + BIstring[BI] + ", " + BD;
				} else if (aa == 0 && lk == 1) {
					return "bcl  " + BOstring[BO] + ", " + BIstring[BI] + ", " + BD;
				} else if (aa == 1 && lk == 1) {
					return "bcla  " + BOstring[BO] + ", " + BIstring[BI] + ", " + BD;
				}
				break;
			case 0x11:
				return "sc";
			case 0x12: // bx
				if (aa == 0 && lk == 0) {
					return "b  " + li;
				} else if (aa == 1 && lk == 0) {
					return "ba  " + li;
				} else if (aa == 0 && lk == 1) {
					return "bl  " + li;
				} else if (aa == 1 && lk == 1) {
					return "bla  " + li;
				}
				break;
			case 0x13:
				switch (XO21to30) {
				case 0x00:
					return "mcrf  crf" + crfD + ", crf" + crfS;
				case 0x10:
					if (lk == 0) {
						return "bclr " + BOstring[BO] + ", " + BIstring[BI];
					} else {
						return "bclrl " + BOstring[BO] + ", " + BIstring[BI];
					}
				case 0x21:
					return "crnor  " + getBIString(D) + ", " + getBIString(A) + ", " + getBIString(B);
				case 0x32:
					return "rfi";
				case 0x81:
					return "crandc  " + getBIString(D) + ", " + getBIString(A) + ", " + getBIString(B);
				case 0x96:
					return "isync";
				case 0xC1:
					return "crxor  " + getBIString(D) + ", " + getBIString(A) + ", " + getBIString(B);
				case 0xE1:
					return "crnand  " + getBIString(D) + ", " + getBIString(A) + ", " + getBIString(B);
				case 0x90:
					return "isync";
				case 0x101:
					return "crand  " + getBIString(D) + ", " + getBIString(A) + ", " + getBIString(B);
				case 0x121:
					return "creqv  " + getBIString(D) + ", " + getBIString(A) + ", " + getBIString(B);
				case 0x1A1:
					return "crorc  " + getBIString(D) + ", " + getBIString(A) + ", " + getBIString(B);
				case 0x1C1:
					return "cror  " + getBIString(D) + ", " + getBIString(A) + ", " + getBIString(B);
				case 0x210:
					if (lk == 0) {
						return "bcctr " + BOstring[BO] + ", " + BIstring[BI];
					} else {
						return "bcctrl " + BOstring[BO] + ", " + BIstring[BI];
					}
				}
				break;
			case 0x14:
				if (Rc == 0) {
					return "rlwimi  r" + A + ", r" + S + ", " + SH + ", " + MB + ", " + ME;
				} else {
					return "rlwimi.  r" + A + ", r" + S + ", " + SH + ", " + MB + ", " + ME;
				}
			case 0x15:
				if (Rc == 0) {
					return "rlwinm  r" + A + ", r" + S + ", " + SH + ", " + MB + ", " + ME;
				} else {
					return "rlwinm.  r" + A + ", r" + S + ", " + SH + ", " + MB + ", " + ME;
				}
			case 0x17:
				if (Rc == 0) {
					return "rlwnm  r" + A + ", r" + S + ", r" + B + ", " + MB + ", " + ME;
				} else {
					return "rlwnm.  r" + A + ", r" + S + ", r" + B + ", " + MB + ", " + ME;
				}
			case 0x18:
				if (A == 0 && S == 0 && UIMM == 0) {
					return "nop";
				}
				return "ori  r" + A + ", r" + S + ", 0x" + Integer.toHexString(UIMM);
			case 0x19:
				return "oris  r" + A + ", r" + S + ", 0x" + Integer.toHexString(UIMM);
			case 0x1A:
				return "xori  r" + A + ", r" + S + ", 0x" + Integer.toHexString(UIMM);
			case 0x1B:
				return "xoris  r" + A + ", r" + S + ", 0x" + Integer.toHexString(UIMM);
			case 0x1C:
				return "andi. r" + A + ", r" + S + ", 0x" + Integer.toHexString(UIMM);
			case 0x1D:
				return "andis.  r" + A + ", r" + S + ", 0x" + Integer.toHexString(UIMM);
			case 0x1F:
				switch (XO21to30) {
				case 0:
					return "cmp crf" + crfD + ", " + L + ", r" + A + ", r" + B;
				case 0x04:
					return "tw  " + TOstring[TO] + ", r" + A + ", r" + B;
				case 0x13:
					return "mfcr  r" + D;
				case 0x14:
					return "lwarx  r" + D + ", r" + A + ", r" + B;
				case 0x17:
					return "lwzx  r" + D + ", r" + A + ", r" + B;
				case 0x18:
					if (Rc == 0) {
						return "slw  r" + A + ", r" + S + ", r" + B;
					} else {
						return "slw.  r" + A + ", r" + S + ", r" + B;
					}
				case 0x1A:
					if (Rc == 0) {
						return "cntlzw  r" + A + ", r" + S;
					} else {
						return "cntlzw.  r" + A + ", r" + S;
					}
				case 0x1C:
					if (Rc == 0) {
						return "and  r" + A + ", r" + S + ", r" + B;
					} else {
						return "and.  r" + A + ", r" + S + ", r" + B;
					}
				case 0x20:
					return "cmpl  crf" + crfD + ", " + L + ", r" + A + ", r" + B;
				case 0x37:
					return "lwzux  r" + D + ", r" + A + ", r" + B;
				case 0x3C:
					if (Rc == 0) {
						return "andc  r" + A + ", r" + S + ", r" + B;
					} else {
						return "andc.  r" + A + ", r" + S + ", r" + B;
					}
				case 0x53:
					return "mfmsr  r" + D;
				case 0x57:
					return "lbzx  r" + D + ", r" + A + ", r" + B;
				case 0x77:
					return "lbzux  r" + D + ", r" + A + ", r" + B;
				case 0x7C:
					if (Rc == 0) {
						return "nor  r" + A + ", r" + S + ", r" + B;
					} else {
						return "nor.  r" + A + ", r" + S + ", r" + B;
					}
				case 0x92:
					return "mtmsr  r" + S;
				case 0x96:
					return "stwcx. r" + S + ", r" + A + ", r" + B;
				case 0x97:
					return "stwx  r" + S + ", r" + A + ", r" + B;
				case 0xB7:
					return "stwux  r" + S + ", r" + A + ", r" + B;
				case 0x90:
					return "mtcrf  " + CRM + ", r" + S;
				case 0x117:
					return "lhzx  r" + D + ", r" + A + ", r" + B;
				case 0x11C:
					if (Rc == 0) {
						return "eqv  r" + A + ", r" + S + ", r" + B;
					} else {
						return "eqv.  r" + A + ", r" + S + ", r" + B;
					}
				case 0x137:
					return "lhzux  r" + D + ", r" + A + ", r" + B;
				case 0x13C:
					return "xor  r" + A + ", r" + S + ", r" + B;
				case 0x153:
					return "mfspr  r" + D + ", " + SPRname(SPR);
				case 0x157:
					return "lhax  r" + D + ", r" + A + ", r" + B;
				case 0x173:
					return "mftb  r" + D + ", " + SPRname(SPR);
				case 0x177:
					return "lhaux  r" + D + ", r" + A + ", r" + B;
				case 0x197:
					return "sthx  r" + S + ", r" + A + ", r" + B;
				case 0x19C:
					if (Rc == 0) {
						return "orc  r" + A + ", r" + S + ", r" + B;
					} else {
						return "orc.  r" + A + ", r" + S + ", r" + B;
					}
				case 0x1B7:
					return "sthux  r" + S + ", r" + A + ", r" + B;
				case 0x1BC:
					if (Rc == 0) {
						if (S == B)
							return "lr  r" + A + ", r" + S;
						else
							return "or  r" + A + ", r" + S + ", r" + B;
					} else {
						if (S == B)
							return "lr  r" + A + ", r" + S;
						else
							return "or.  r" + A + ", r" + S + ", r" + B;
					}
				case 0x1D3:
					return "mtspr  " + SPRname(SPR) + ", r" + S;
				case 0x1DC:
					if (Rc == 0) {
						return "nand  r" + A + ", r" + S + ", r" + B;
					} else {
						return "nand.  r" + A + ", r" + S + ", r" + B;
					}
				case 0x200:
					return "mcrxr  crf" + crfD;
				case 0x205:
					return "stswi  r" + S + ", r" + A + ", " + NB;
				case 0x215:
					return "lswx  r" + D + ", r" + A + ", r" + B;
				case 0x216:
					return "lwbrx  r" + D + ", r" + A + ", r" + B;
				case 0x217:
					return "lfsx  fr" + D + ", r" + A + ", r" + B;
				case 0x218:
					if (Rc == 0) {
						return "srw  r" + A + ", r" + S + ", r" + B;
					} else {
						return "srw.  r" + A + ", r" + S + ", r" + B;
					}
				case 0x237:
					return "lfsux  fr" + D + ", r" + A + ", r" + B;
				case 0x255:
					return "lswi  r" + D + ", r" + A + " , " + NB;
				case 0x256:
					return "sync";
				case 0x257:
					return "lfdx  fr" + D + ", r" + A + ", r" + B;
				case 0x277:
					return "lfdux  fr" + D + ", r" + A + ", r" + B;
				case 0x2D7:
					return "stfdx  fr" + S + ", r" + A + ", r" + B;
				case 0x2F7:
					return "stfdux  fr" + S + ", r" + A + ", r" + B;
				case 0x295:
					return "stswx  r" + S + ", r" + A + ", r" + B;
				case 0x296:
					return "stwbrx  r" + S + ", r" + A + ", r" + B;
				case 0x297:
					return "stfsx  fr" + S + ", r" + A + ", r" + B;
				case 0x2B7:
					return "stfsux  fr" + S + ", r" + A + ", r" + B;
				case 0x316:
					return "lhbrx  r" + D + ", r" + A + ", r" + B;
				case 0x318:
					if (Rc == 0) {
						return "sraw  r" + A + ", r" + S + ", r" + B;
					} else {
						return "sraw.  r" + A + ", r" + S + ", r" + B;
					}
				case 0x338:
					if (Rc == 0) {
						return "srawi  r" + A + ", r" + S + ", " + SH;
					} else {
						return "srawi.  r" + A + ", r" + S + ", " + SH;
					}
				case 0x356:
					return "eieio";
				case 0x396:
					return "sthbrx  r" + S + ", r" + A + ", r" + B;
				case 0x3BA:
					if (Rc == 0) {
						return "extsb  r" + A + ", r" + S;
					} else {
						return "extsb.  r" + A + ", r" + S;
					}
				case 0x3D6:
					return "icbi  r" + A + ", r" + B;
				case 0x3D7:
					return "stfiwx  fr" + S + ", r" + A + ", r" + B;

				case 0x39A:
					if (Rc == 0) {
						return "extsh  r" + A + ", r" + S;
					} else {
						return "extsh.  r" + A + ", r" + S;
					}
				}
				switch (XO22to30) {
				case 0x08:
					if (OE == 0 && Rc == 0) {
						return "subfc  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 0 && Rc == 1) {
						return "subfc.  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 1 && Rc == 0) {
						return "subfco  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 1 && Rc == 1) {
						return "subfco.  r" + D + ", r" + A + ", r" + B;
					}
					break;
				case 0x0A:
					if (OE == 0 && Rc == 0) {
						return "addc  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 0 && Rc == 1) {
						return "addc.  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 1 && Rc == 0) {
						return "addco  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 1 && Rc == 1) {
						return "addco.  r" + D + ", r" + A + ", r" + B;
					}
					break;
				case 0x0B:
					if (Rc == 0) {
						return "mulhwu  r" + D + ", r" + A + ", r" + B;
					} else {
						return "mulhwu.  r" + D + ", r" + A + ", r" + B;
					}
				case 0x28:
					if (OE == 0 && Rc == 0) {
						return "subf  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 0 && Rc == 1) {
						return "subf.  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 1 && Rc == 0) {
						return "subfo  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 1 && Rc == 1) {
						return "subfo.  r" + D + ", r" + A + ", r" + B;
					}
					break;
				case 0x4B:
					if (Rc == 0) {
						return "mulhw  r" + D + ", r" + A + ", r" + B;
					} else {
						return "mulhw.  r" + D + ", r" + A + ", r" + B;
					}
				case 0x68:
					if (OE == 0 && Rc == 0) {
						return "neg  r" + D + ", r" + A;
					} else if (OE == 0 && Rc == 1) {
						return "neg.  r" + D + ", r" + A;
					} else if (OE == 1 && Rc == 0) {
						return "nego  r" + D + ", r" + A;
					} else if (OE == 1 && Rc == 1) {
						return "nego.  r" + D + ", r" + A;
					}
					break;
				case 0x88:
					if (OE == 0 && Rc == 0) {
						return "subfe  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 0 && Rc == 1) {
						return "subfe.  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 1 && Rc == 0) {
						return "subfeo  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 1 && Rc == 1) {
						return "subfeo.  r" + D + ", r" + A + ", r" + B;
					}
					break;
				case 0x8A:
					if (OE == 0 && Rc == 0) {
						return "adde  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 0 && Rc == 1) {
						return "adde.  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 1 && Rc == 0) {
						return "addeo  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 1 && Rc == 1) {
						return "addeo.  r" + D + ", r" + A + ", r" + B;
					}
					break;
				case 0xC8:
					if (OE == 0 && Rc == 0) {
						return "subfze  r" + D + ", r" + A;
					} else if (OE == 0 && Rc == 1) {
						return "subfze.  r" + D + ", r" + A;
					} else if (OE == 1 && Rc == 0) {
						return "subfzeo  r" + D + ", r" + A;
					} else if (OE == 1 && Rc == 1) {
						return "subfzeo.  r" + D + ", r" + A;
					}
					break;
				case 0xCA:
					if (OE == 0 && Rc == 0) {
						return "addze  r" + D + ", r" + A;
					} else if (OE == 0 && Rc == 1) {
						return "addze.  r" + D + ", r" + A;
					} else if (OE == 1 && Rc == 0) {
						return "addzeo  r" + D + ", r" + A;
					} else if (OE == 1 && Rc == 1) {
						return "addzeo.  r" + D + ", r" + A;
					}
					break;
				case 0xD7:
					return "stbx  r" + S + ", r" + A + ", r" + B;
				case 0xE8:
					if (OE == 0 && Rc == 0) {
						return "subfme  r" + D + ", r" + A;
					} else if (OE == 0 && Rc == 1) {
						return "subfme.  r" + D + ", r" + A;
					} else if (OE == 1 && Rc == 0) {
						return "subfmeo  r" + D + ", r" + A;
					} else if (OE == 1 && Rc == 1) {
						return "subfmeo.  r" + D + ", r" + A;
					}
					break;
				case 0xEA:
					if (OE == 0 && Rc == 0) {
						return "addme  r" + D + ", r" + A;
					} else if (OE == 0 && Rc == 1) {
						return "addme.  r" + D + ", r" + A;
					} else if (OE == 1 && Rc == 0) {
						return "addmeo  r" + D + ", r" + A;
					} else if (OE == 1 && Rc == 1) {
						return "addmeo.  r" + D + ", r" + A;
					}
					break;
				case 0xEB:
					if (OE == 0 && Rc == 0) {
						return "mullw  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 0 && Rc == 1) {
						return "mullw.  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 1 && Rc == 0) {
						return "mullwo  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 1 && Rc == 1) {
						return "mullwo.  r" + D + ", r" + A + ", r" + B;
					}
					break;

				case 0xF7:
					return "stbux  r" + S + ", r" + A + ", r" + B;
				case 0x10A:
					if (OE == 0 && Rc == 0) {
						return "add  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 0 && Rc == 1) {
						return "add.  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 1 && Rc == 0) {
						return "addo  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 1 && Rc == 1) {
						return "addo.  r" + D + ", r" + A + ", r" + B;
					}
				case 0x1CB:
					if (OE == 0 && Rc == 0) {
						return "divwu  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 0 && Rc == 1) {
						return "divwu.  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 1 && Rc == 0) {
						return "divwuo  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 1 && Rc == 1) {
						return "divwuo.  r" + D + ", r" + A + ", r" + B;
					}
					break;
				case 0x1EB:
					if (OE == 0 && Rc == 0) {
						return "divw  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 0 && Rc == 1) {
						return "divw.  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 1 && Rc == 0) {
						return "divwo  r" + D + ", r" + A + ", r" + B;
					} else if (OE == 1 && Rc == 1) {
						return "divwo.  r" + D + ", r" + A + ", r" + B;
					}
					break;
				}
				break;
			case 0x20:
				return "lwz  r" + D + ", " + d + "(r" + A + ")";
			case 0x21:
				return "lwzu  r" + D + ", " + d + "(r" + A + ")";
			case 0x22:
				return "lbz  r" + D + ", " + d + "(r" + A + ")";
			case 0x23:
				return "lbzu  r" + D + ", " + d + "(r" + A + ")";
			case 0x24:
				return "stw  r" + S + ", " + d + "(r" + A + ")";
			case 0x25:
				return "stwu  r" + S + ", " + d + "(r" + A + ")";
			case 0x26:
				return "stb  r" + S + ", " + d + "(r" + A + ")";
			case 0x27:
				return "stbu  r" + S + ", " + d + "(r" + A + ")";
			case 0x28:
				return "lhz  r" + D + ", " + d + "(r" + A + ")";
			case 0x29:
				return "lhzu  r" + D + ", " + d + "(r" + A + ")";
			case 0x2A:
				return "lha  r" + D + ", " + d + "(r" + A + ")";
			case 0x2B:
				return "lhau  r" + D + ", " + d + "(r" + A + ")";
			case 0x2C:
				return "sth  r" + S + ", " + d + "(r" + A + ")";
			case 0x2D:
				return "sthu  r" + S + ", " + d + "(r" + A + ")";
			case 0x2E:
				return "lmw  r" + D + ", " + d + "(r" + A + ")";
			case 0x2F:
				return "stmw  r" + S + ", " + d + "(r" + A + ")";
			case 0x30:
				return "lfs  fr" + D + ", " + d + "(r" + A + ")";
			case 0x31:
				return "lfsu  fr" + D + ", " + d + "(r" + A + ")";
			case 0x32:
				return "lfd  fr" + D + ", " + d + "(r" + A + ")";
			case 0x33:
				return "lfdu  fr" + D + ", " + d + "(r" + A + ")";
			case 0x34:
				return "stfs  fr" + S + ", " + d + "(r" + A + ")";
			case 0x35:
				return "stfsu  fr" + S + ", " + d + "(r" + A + ")";
			case 0x36:
				return "stfd  fr" + S + ", " + d + "(r" + A + ")";
			case 0x37:
				return "stfdu  fr" + S + ", " + d + "(r" + A + ")";

			case 0x3B:
				switch (XO26to30) {
				case 0x12:
					if (Rc == 0) {
						return "fdivs  fr" + D + ", fr" + A + ", fr" + B;
					} else {
						return "fdivs.  fr" + D + ", fr" + A + ", fr" + B;
					}
				case 0x14:
					if (Rc == 0) {
						return "fsubs  fr" + D + ", fr" + A + ", fr" + B;
					} else {
						return "fsubs.  fr" + D + ", fr" + A + ", fr" + B;
					}
				case 0x15:
					if (Rc == 0) {
						return "fadds  fr" + D + ", fr" + A + ", fr" + B;
					} else {
						return "fadds.  fr" + D + ", fr" + A + ", fr" + B;
					}
				case 0x19:
					if (Rc == 0) {
						return "fmuls  fr" + D + ", fr" + A + " fr" + C;
					} else {
						return "fmuls.  fr" + D + ", fr" + A + " fr" + C;
					}
				case 0x1C:
					if (Rc == 0) {
						return "fmsubs  fr" + D + ", fr" + A + " fr" + C + ", fr" + B;
					} else {
						return "fmsubs.  fr" + D + ", fr" + A + " fr" + C + ", fr" + B;
					}
				case 0x1D:
					if (Rc == 0) {
						return "fmadds  fr" + D + ", fr" + A + " fr" + C + ", fr" + B;
					} else {
						return "fmadds.  fr" + D + ", fr" + A + " fr" + C + ", fr" + B;
					}
				case 0x1E:
					if (Rc == 0) {
						return "fnmsubs  fr" + D + ", fr" + A + ", fr" + C + ", fr" + B;
					} else {
						return "fnmsubs.  fr" + D + ", fr" + A + ", fr" + C + ", fr" + B;
					}
				case 0x1F:
					if (Rc == 0) {
						return "fnmadds  fr" + D + ", fr" + A + ", fr" + C + ", fr" + B;
					} else {
						return "fnmadds.  fr" + D + ", fr" + A + ", fr" + C + ", fr" + B;
					}
				}
			case 0x3F:
				switch (XO26to30) {
				case 0x12:
					if (Rc == 0) {
						return "fdiv  fr" + D + ", fr" + A + ", fr" + B;
					} else {
						return "fdiv.  fr" + D + ", fr" + A + ", fr" + B;
					}
				case 0x14:
					if (Rc == 0) {
						return "fsub  fr" + D + ", fr" + A + ", fr" + B;
					} else {
						return "fsub.  fr" + D + ", fr" + A + ", fr" + B;
					}

				case 0x15:
					if (Rc == 0) {
						return "fadd  fr" + D + ", fr" + A + ", fr" + B;
					} else {
						return "fadd.  fr" + D + ", fr" + A + ", fr" + B;
					}
				case 0x19:
					if (Rc == 0) {
						return "fmul  fr" + D + ", fr" + A + " fr" + C;
					} else {
						return "fmul.  fr" + D + ", fr" + A + " fr" + C;
					}
				case 0x1C:
					if (Rc == 0) {
						return "fmsub  fr" + D + ", fr" + A + " fr" + C + ", fr" + B;
					} else {
						return "fmsub.  fr" + D + ", fr" + A + " fr" + C + ", fr" + B;
					}
				case 0x1D:
					if (Rc == 0) {
						return "fmadd  fr" + D + ", fr" + A + " fr" + C + ", fr" + B;
					} else {
						return "fmadd.  fr" + D + ", fr" + A + " fr" + C + ", fr" + B;
					}
				case 0x1E:
					if (Rc == 0) {
						return "fnmsub  fr" + D + ", fr" + A + ", fr" + C + ", fr" + B;
					} else {
						return "fnmsub.  fr" + D + ", fr" + A + ", fr" + C + ", fr" + B;
					}
				case 0x1F:
					if (Rc == 0) {
						return "fnmadd  fr" + D + ", fr" + A + ", fr" + C + ", fr" + B;
					} else {
						return "fnmadd.  fr" + D + ", fr" + A + ", fr" + C + ", fr" + B;
					}
				}
				switch (XO21to30) {
				case 0x00:
					return "fcmpu  crf" + (D >> 2) + ", fr" + A + ", fr" + B;
				case 0x0C:
					if (Rc == 0) {
						return "frsp  fr" + D + ", fr" + B;
					} else {
						return "frsp.  fr" + D + ", fr" + B;
					}
				case 0x0E:
					if (Rc == 0) {
						return "fctiw  fr" + D + ", fr" + B;
					} else {
						return "fctiw.  fr" + D + ", fr" + B;
					}
				case 0x0F:
					if (Rc == 0) {
						return "fctiwz  fr" + D + ", fr" + B;
					} else {
						return "fctiwz.  fr" + D + ", fr" + B;
					}
				case 0x20:
					return "fcmpo  crf" + crfD + ", fr" + A + ", fr" + B;
				case 0x26:
					if (Rc == 0) {
						return "mtfsb1  crb" + D;
					} else {
						return "mtfsb1.  crb" + D;
					}
				case 0x28:
					if (Rc == 0) {
						return "fneg  fr" + D + ", fr" + B;
					} else {
						return "fneg.  fr" + D + ", fr" + B;
					}
				case 0x40:
					return "mcrfs  crf" + crfD + ", crf" + crfS;
				case 0x46:
					if (Rc == 0) {
						return "mtfsb0  crb" + D;
					} else {
						return "mtfsb0.  crb" + D;
					}
				case 0x48:
					if (Rc == 0) {
						return "fmr  fr" + D + ", fr" + B;
					} else {
						return "fmr.  fr" + D + ", fr" + B;
					}
				case 0x86:
					if (Rc == 0) {
						return "mtfsfi  crf" + crfD + ", " + IMM;
					} else {
						return "mtfsfi.  crf" + crfD + ", " + IMM;
					}
				case 0x88:
					if (Rc == 0) {
						return "fnabs  fr" + D + ", fr" + B;
					} else {
						return "fnabs.  fr" + D + ", fr" + B;
					}
				case 0x108:
					if (Rc == 0) {
						return "fabs  fr" + D + ", fr" + B;
					} else {
						return "fabs.  fr" + D + ", fr" + B;
					}
				case 0x247:
					if (Rc == 0) {
						return "mffs  fr" + D;
					} else {
						return "mffs.  fr" + D;
					}
				case 0x2C7:
					if (Rc == 0) {
						return "mtfsf  " + FM + ", fr" + B;
					} else {
						return "mtfsf.  " + FM + ", fr" + B;
					}
				} 
			}
			return machineInstr + "  (0x" + Integer.toHexString(machineInstr) + ")";
	}

	private static String SPRname(int SPR) {
		if (SPR == 1) return "XER";
		if (SPR == 8) return "LR";
		if (SPR == 9) return "CTR";
		if (SPR == 18) return "DSISR";
		if (SPR == 19) return "DAR";
		if (SPR == 22) return "DEC";
		if (SPR == 26) return "SRR0";
		if (SPR == 27) return "SRR1";
		if (SPR == 80) return "EIE";
		if (SPR == 81) return "EID";
		if (SPR == 82) return "NRE";
		if (SPR == 144) return "CMPA";
		if (SPR == 145) return "CMPB";
		if (SPR == 146) return "CMPC";
		if (SPR == 147) return "CMPD";
		if (SPR == 148) return "ECR";
		if (SPR == 149) return "DER";
		if (SPR == 150) return "COUNTA";
		if (SPR == 151) return "COUNTB";
		if (SPR == 152) return "CMPE";
		if (SPR == 153) return "CMPF";
		if (SPR == 154) return "CMPG";
		if (SPR == 155) return "CMPH";
		if (SPR == 156) return "LCTRL1";
		if (SPR == 157) return "LCTRL2";
		if (SPR == 158) return "ICTRL";
		if (SPR == 159) return "BAR";
		if (SPR == 268) return "TBL";
		if (SPR == 269) return "TBU";
		if (SPR == 272) return "SPRG0";
		if (SPR == 273) return "SPRG1";
		if (SPR == 274) return "SPRG2";
		if (SPR == 275) return "SPRG3";
		if (SPR == 284) return "TBL";
		if (SPR == 285) return "TBU";
		if (SPR == 287) return "PVR";
		if (SPR == 560) return "ICCSR";
		if (SPR == 561) return "ICADR";
		if (SPR == 562) return "ICDAT";
		if (SPR == 630) return "DPDR";
		if (SPR == 638) return "IMMR";
		if (SPR == 1022) return "FPECR";
		assert false : "wrong SPR number";
		return null;
	}

	static {
		//int code = InstructionDecoder.getCode("rlwinm  r3, r29, 2, 0, 29");
		//		System.out.println(InstructionDecoder.getMnemonic(code);
	}
}
