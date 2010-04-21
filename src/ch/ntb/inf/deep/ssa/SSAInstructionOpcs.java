package ch.ntb.inf.deep.ssa;

interface SSAInstructionOpcs {
	short // operation codes of SSA instructions (SSA codes)
	sCload_const = 1,sCadd = 2, sCsub = 3, sCmul = 4, sCdiv = 5,
	sCrem = 6, sCneg = 7, sCshl = 8, sCshr = 9, sCushr = 10,
	sCand = 11, sCor = 12, sCxor = 13, sCintCast = 14, sClongCast = 15,
	sCfloatCast = 16, sCdoubleCast = 17, sCcmp = 18, sCreturn = 19, sCcall = 20,
	sCnew = 21, sCinstanceof = 22, sCload_var = 23;
	
	int[] scAttrTab = {
			/*Format:	0xuuuu'uucc,
					u	unused
					cc	operation code (opc)
			*/
			0x00000000 + sCload_const,	//load 
			0x00000000 + sCload_var,
			
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
			
			0x00000000 + sCintCast, 	//cast
			0x00000000 + sClongCast,
			0x00000000 + sCfloatCast,
			0x00000000 + sCdoubleCast,
			
			0x00000000 + sCcmp, 		//compare
			0x00000000 + sCinstanceof,
			
			0x00000000 + sCreturn, 		//return
			
			0x00000000 + sCcall, 		//new, call
			0x00000000 + sCnew
			};
			

}
