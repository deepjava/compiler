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

interface InstructionOpcs {
	final int condEQ = 0;
	final int condNOTEQ = 1;
	final int condCS = 2;
	final int condAlways = 14;
	final int TOifequal = 0x04;
	final int TOifless = 0x10;
	final int TOifgreater = 0x08;
	final int TOifgeU = 0x05;
	final int TOifnequal = 0x18;
	final int TOalways = 0x1f;
	final int CRF0 = 0;
	final int CRF1 = 1;
	final int CRF2 = 2;
	final int CRF3 = 3;
	final int CRF4 = 4;
	final int CRF5 = 5;
	final int CRF6 = 6;
	final int CRF7 = 7;
	final int LT = 0;
	final int GT = 1;
	final int EQ = 2;
	final int XER = 1;
	final int LR = 8;
	final int CTR = 9;
	final int SRR0 = 26;
	final int SRR1 = 27;
	final int EIE = 80;
	final int EID = 81;
	final int NRI = 82;
	
	final int CRF0SO = 3;
	final int CRF0EQ = 2;
	final int CRF0GT = 1;
	final int CRF0LT = 0;
	final int CRF1SO = 7;
	final int CRF1EQ = 6;
	final int CRF1GT = 5;
	final int CRF1LT = 4;
	final int CRF2SO = 11;
	final int CRF2EQ = 10;
	final int CRF2GT = 9;
	final int CRF2LT = 8;
	
	final int noShift = 0;
	final int LSL = 0;
	final int LRS = 1;
	final int ASR = 1;
	final int ROR = 3;
	final int RRX = 3;

	public static String[] condString = {
		"if equal",
		"if not equal",
		"if carry set",
		"if carry clear",
		"if negative",
		"if positive",
		"if overflow",
		"if no overflow",
		"if unsigned higher",
		"if unsigned lower",
		"if greater or equal",
		"if less",
		"if greater",
		"if less or equal",
		"always",
		"reserved"
	};

	final int // ARM Instructions
		armAdc = (0x0 << 26) | (0x5 << 21),
		armAnd = (0x0 << 26) | (0x0 << 21),
		armAdd = (0x8 << 20),
		armAdds = (0x9 << 20),
		armB = (0x12 << 26),
		armBl = (0x12 << 26) | 1,
		armBc = (0x10 << 26),
		armRsb = (0x26 << 20),
		armRsbs = (0x27 << 20),
		armSub = (0x24 << 20),
		armSubs = (0x25 << 20);
}
