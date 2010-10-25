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
	ssaApCall = ssaApBase+0,   ssaApPhi = ssaApBase+1,   ssaApLoadLocal = ssaApBase+2;

	int[] scAttrTab = {
			/*Format:	0xuuuu'aacc,
					u	unused
					aa	attributes
					cc	operation code (opc)
			*/
			0x00000000 + sCloadConst,	//load 
			0x00000000 | (1<<ssaApLoadLocal) + sCloadLocal,
			0x00000000 + sCloadFromField,
			0x00000000 + sCloadFromArray,
			
			0x00000000 + sCstoreToField, //store
			0x00000000 + sCstoreToArray, 
			
			0x00000000 + sCadd, 		//arithmetic 
			0x00000000 + sCsub,
			0x00000000 + sCmul,
			0x00000000 + sCdiv,
			0x00000000 + sCrem,
			0x00000000 + sCneg,		 	
			
			0x00000000 + sCshl,		 	//shift and logical 
			0x00000000 + sCshr,
			0x00000000 + sCushr,
			0x00000000 + sCand,
			0x00000000 + sCor,
			0x00000000 + sCxor,
			
			0x00000000 + sCconvInt, 	//convert from
			0x00000000 + sCconvLong,
			0x00000000 + sCconvFloat,
			0x00000000 + sCconvDouble,
			
			0x00000000 + sCcmpl, 		//compare
			0x00000000 + sCcmpg,
			0x00000000 + sCinstanceof,
			
			0x00000000 + sCalength, 	//arrayLength
			
			0x00000000 | (1<<ssaApCall) + sCcall, 		//call
			0x00000000 | (1<<ssaApCall) + sCnew,
			0x00000000 + sCreturn, 		
			0x00000000 + sCthrow, 		
			0x00000000 + sCbranch, 		
			
			0x00000000 + sCregMove, 		
			
			0x00000000 | (1<<ssaApPhi) + sCPhiFunc		
			};
			

}
