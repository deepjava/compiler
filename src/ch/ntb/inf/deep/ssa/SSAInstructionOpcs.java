package ch.ntb.inf.deep.ssa;

interface SSAInstructionOpcs {
	short // operation codes of SSA instructions (SSA codes)
	sCload_const = 1,sCadd = 2, sCsub = 3, sCmul = 4, sCdiv = 5,
	sCrem = 6, sCneg = 7, sCshl = 8, sCshr = 9, sCushr = 10,
	sCand = 11, sCor = 12, sCxor = 13, sCconv_int = 14, sCconv_long = 15,
	sCconv_float = 16, sCconv_double = 17, sCcmpl = 18, sCreturn = 19, sCcall = 20,
	sCnew = 21, sCinstanceof = 22, sCload_var = 23, sCload_fromArray = 24, sCstore_toArray =25,
	sCcmpg = 26, sCalength = 27;
	
	int[] scAttrTab = {
			/*Format:	0xuuuu'uucc,
					u	unused
					cc	operation code (opc)
			*/
			0x00000000 + sCload_const,	//load 
			0x00000000 + sCload_var,
			0x00000000 + sCload_fromArray,
			
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
			
			0x00000000 + sCconv_int, 	//convert from
			0x00000000 + sCconv_long,
			0x00000000 + sCconv_float,
			0x00000000 + sCconv_double,
			
			0x00000000 + sCcmpl, 		//compare, the distinction cmpl and cmpg needs for NaN by float and double
			0x00000000 + sCcmpg,
			0x00000000 + sCinstanceof,
			
			0x00000000 + sCreturn, 		//return
			
			0x00000000 + sCstore_toArray, //store
			
			0x00000000 + sCalength, //arrayLength
			
			0x00000000 + sCcall, 		//new, call
			0x00000000 + sCnew
			};
			

}
