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

package ch.ntb.inf.deep.ssa;

public interface SSAInstructionOpcs {
	short // operation codes of SSA instructions (SSA codes)
	sCloadConst = 0, sCloadLocal = 1, sCloadFromField = 2, sCloadFromArray = 3, 
	sCstoreToField = 4, sCstoreToArray = 5, sCadd = 6, sCsub = 7, 
	sCmul = 8, sCdiv = 9, sCrem = 10, sCneg = 11, 
	sCshl = 12, sCshr = 13, sCushr = 14, sCand = 15, 
	sCor = 16, sCxor = 17, sCconvInt = 18, sCconvLong = 19,
	sCconvFloat = 20, sCconvDouble = 21, sCcmpl = 22, sCcmpg = 23, 
	sCinstanceof = 24, sCcheckcast = 25, sCthrow = 26, sCalength = 27, 
	sCcall = 28, sCnew = 29, sCreturn = 30, sCbranch = 31, sCswitch = 32,
	sCregMove = 33, sCPhiFunc = 34;

	byte  // attributes of SSA instructions
	ssaApBase = 8, 
	ssaApCall = ssaApBase+0,   ssaApPhi = ssaApBase+1,   
	ssaApLoadLocal = ssaApBase+2,	ssaApImmOpd = ssaApBase+3,
	ssaApTempStore = ssaApBase+4;

	int[] scAttrTab = {
			/*Format:	0xuuMN'aacc,
					u	unused
					M	nof auxiliary registers for this instruction (ARM)
					N	nof auxiliary registers for this instruction (PPC)
					aa	attributes
					cc	operation code (opc)
			*/
			0x00660000 + sCloadConst,	//load 
			0x00000000 | (1<<ssaApLoadLocal) + sCloadLocal,
			0x00010000 + sCloadFromField,
			0x00020000 + sCloadFromArray,
			
			0x00010000 + sCstoreToField, //store
			0x00020000 + sCstoreToArray, 
			
			0x00000000 | (1<<ssaApImmOpd) + sCadd, 		//arithmetic 
			0x00000000 | (1<<ssaApImmOpd) + sCsub,
			0x00440000 | (1<<ssaApImmOpd) + sCmul,
			0x00770000 | (1<<ssaApImmOpd) + sCdiv,
			0x00880000 | (1<<ssaApImmOpd) + sCrem,
			0x00000000 + sCneg,		 	
			
			0x00550000 | (1<<ssaApImmOpd) + sCshl,		//shift and logical 
			0x00550000 | (1<<ssaApImmOpd) + sCshr,
			0x00550000 | (1<<ssaApImmOpd) + sCushr,
			0x00000000 | (1<<ssaApImmOpd) + sCand,
			0x00000000 | (1<<ssaApImmOpd) + sCor,
			0x00000000 | (1<<ssaApImmOpd) + sCxor,
			
			0x00660000 | (1<<ssaApTempStore) + sCconvInt, 	//convert from
			0x00000000 | (1<<ssaApTempStore) + sCconvLong,
			0x00000000 | (1<<ssaApTempStore) + sCconvFloat,
			0x00000000 | (1<<ssaApTempStore) + sCconvDouble,
			
			0x00000000 + sCcmpl, 		//compare
			0x00000000 + sCcmpg,
			
			0x00010000 + sCinstanceof,	//checking
			0x00010000 + sCcheckcast,
			0x00010000 + sCthrow, 		
			
			0x00000000 + sCalength, 	//arrayLength
			
			0x00010000 | (1<<ssaApCall) | (1<<ssaApImmOpd) + sCcall, 		//call
			0x00010000 | (1<<ssaApCall) + sCnew,
			0x00000000 + sCreturn, 		
			0x00000000 | (1<<ssaApImmOpd) + sCbranch, 		
			0x00000000 + sCswitch, 		
			
			0x00000000 + sCregMove, 		
			
			0x00000000 | (1<<ssaApPhi) + sCPhiFunc		
		};
			

}
