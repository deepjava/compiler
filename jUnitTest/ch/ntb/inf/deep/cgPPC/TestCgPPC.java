package ch.ntb.inf.deep.cgPPC;

import ch.ntb.inf.deep.cfg.TestCFG;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.ssa.TestSSA;

public class TestCgPPC {

	static MachineCode[] code;

    /**
	 * Creates code for all methods of a class
	 * 
	 * @param clazz
	 *            Java class object
	 */
	public static void createCgPPC(Class clazz) {
		TestSSA.createSSA(clazz);
		code = new MachineCode[TestCFG.cfg.length];
//		for (int i = 0; i < TestCFG.cfg.length; i++){
		int i = 2;
			RegAllocator.allocateRegisters(TestSSA.ssa[i]);
			TestSSA.ssa[i].print(0, i);
			
			code[i] = new MachineCode(TestSSA.ssa[i]);
			code[i].print();
			System.out.println();
//		}
	}

	public static int[] getCode(int methodNo) {
		int len = code[methodNo].iCount;
		int[] code1 = new int[len];
		for (int i = 0; i < len; i++) code1[i] = code[methodNo].instructions[i];
		return code1;
	}

}
