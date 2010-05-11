package ch.ntb.inf.deep.ssa;

public interface SSAInstructionOpcs {
	short // operation codes of SSA instructions (SSA codes)
	sCloadConst = 1,sCadd = 2, sCsub = 3, sCmul = 4, sCdiv = 5,
	sCrem = 6, sCneg = 7, sCshl = 8, sCshr = 9, sCushr = 10,
	sCand = 11, sCor = 12, sCxor = 13, sCconvInt = 14, sCconvLong = 15,
	sCconvFloat = 16, sCconvDouble = 17, sCcmpl = 18, sCreturn = 19, sCcall = 20,
	sCnew = 21, sCinstanceof = 22, sCloadVar = 23, sCloadFromArray = 24, sCstoreToArray =25,
	sCcmpg = 26, sCalength = 27, sCstoreToField = 28, sCthrow = 29, sCPhiFunc = 30,
	sCloadParam = 31, sCRegMove = 32;
	
	int[] scAttrTab = {
			/*Format:	0xuuuu'uucc,
					u	unused
					cc	operation code (opc)
			*/
			0x00000000 + sCloadConst,	//load 
			0x00000000 + sCloadVar,
			0x00000000 + sCloadFromArray,
			
			0x00000000 + sCadd, 		//arithmetic expression
			0x00000000 + sCsub,
			0x00000000 + sCmul,
			0x00000000 + sCdiv,
			0x00000000 + sCrem,
			
			0x00000000 + sCneg,		 	//logic expression
			0x00000000 + sCshl,
			0x00000000 + sCshr,
			0x00000000 + sCushr,
			0x00000000 + sCand,
			0x00000000 + sCor,
			0x00000000 + sCxor,
			
			0x00000000 + sCconvInt, 	//convert from
			0x00000000 + sCconvLong,
			0x00000000 + sCconvFloat,
			0x00000000 + sCconvDouble,
			
			0x00000000 + sCcmpl, 		//compare, the distinction cmpl and cmpg needs for NaN by float and double
			0x00000000 + sCcmpg,
			0x00000000 + sCinstanceof,
			
			0x00000000 + sCreturn, 		//return
			
			0x00000000 + sCstoreToArray, //store
			
			0x00000000 + sCalength, //arrayLength
			
			0x00000000 + sCcall, 		//new, call
			0x00000000 + sCnew
			};
			

}
