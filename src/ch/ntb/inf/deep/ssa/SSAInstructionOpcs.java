package ch.ntb.inf.deep.ssa;

public interface SSAInstructionOpcs {
	short // operation codes of SSA instructions (SSA codes)
	sCloadConst = 0, sCloadLocal = 1, sCloadFromField = 2, sCloadFromArray = 3, 
	sCstoreToField = 4, sCstoreToArray = 5, sCadd = 6, sCsub = 7, 
	sCmul = 8, sCdiv = 9, sCrem = 10, sCneg = 11, 
	sCshl = 12, sCshr = 13, sCushr = 14, sCand = 15, 
	sCor = 16, sCxor = 17, sCconvInt = 18, sCconvLong = 19,
	sCconvFloat = 20, sCconvDouble = 21, sCcmpl = 22, sCcmpg = 23, 
	sCinstanceof = 24, sCalength = 25, sCcall = 26, sCnew = 27,
	sCreturn = 28, sCthrow = 29, sCbranch = 30, sCregMove = 31,
	sCPhiFunc = 32
	;

	byte  // attributes of SSA instructions
	ssaApBase = 8, 
	ssaApCall = ssaApBase+0,   ssaApPhi = ssaApBase+1,   
	ssaApLoadLocal = ssaApBase+2,	ssaApImmOpd = ssaApBase+3,
	ssaApTempStore = ssaApBase+4;

	int[] scAttrTab = {
			/*Format:	0xuuuN'aacc,
					u	unused
					N	Nof auxiliary registers for this instruction
					aa	attributes
					cc	operation code (opc)
			*/
			0x00070000 + sCloadConst,	//load 
			0x00000000 | (1<<ssaApLoadLocal) + sCloadLocal,
			0x00010000 + sCloadFromField,
			0x00020000 + sCloadFromArray,
			
			0x00010000 + sCstoreToField, //store
			0x00020000 + sCstoreToArray, 
			
			0x00000000 | (1<<ssaApImmOpd) + sCadd, 		//arithmetic 
			0x00000000 | (1<<ssaApImmOpd) + sCsub,
			0x00040000 | (1<<ssaApImmOpd) + sCmul,
			0x00090000 + sCdiv,
			0x00090000 + sCrem,
			0x00000000 + sCneg,		 	
			
			0x00050000 | (1<<ssaApImmOpd) + sCshl,		 	//shift and logical 
			0x00050000 | (1<<ssaApImmOpd) + sCshr,
			0x00050000 | (1<<ssaApImmOpd) + sCushr,
			0x00000000 | (1<<ssaApImmOpd) + sCand,
			0x00000000 | (1<<ssaApImmOpd) + sCor,
			0x00000000 | (1<<ssaApImmOpd) + sCxor,
			
			0x00060000 | (1<<ssaApTempStore) + sCconvInt, 	//convert from
			0x00080000 | (1<<ssaApTempStore) + sCconvLong,
			0x00040000 | (1<<ssaApTempStore) + sCconvFloat,
			0x00040000 | (1<<ssaApTempStore) + sCconvDouble,
			
			0x00000000 + sCcmpl, 		//compare
			0x00000000 + sCcmpg,
			0x00010000 + sCinstanceof,
			
			0x00000000 + sCalength, 	//arrayLength
			
			0x00010000 | (1<<ssaApCall) | (1<<ssaApImmOpd) + sCcall, 		//call
			0x00010000 | (1<<ssaApCall) + sCnew,
			0x00000000 + sCreturn, 		
			0x00010000 + sCthrow, 		
			0x00000000 | (1<<ssaApImmOpd) + sCbranch, 		
			
			0x00000000 + sCregMove, 		
			
			0x00000000 | (1<<ssaApPhi) + sCPhiFunc		
			};
			

}
