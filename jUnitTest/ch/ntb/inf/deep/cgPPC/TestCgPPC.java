package ch.ntb.inf.deep.cgPPC;

import ch.ntb.inf.deep.cfg.TestCFG;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.linker.Linker32;
import ch.ntb.inf.deep.ssa.TestSSA;
import ch.ntb.inf.deep.strings.HString;

public class TestCgPPC implements ICclassFileConsts {

	static MachineCode[] code;
//	static String[] config = new String[] {"C:/NTbcheckout/EUser/JCC/Deep/ExampleProject.deep","BootFromRam"};
	static String[] config = new String[] {"M:/EUser/JCC/Deep/ExampleProject.deep","BootFromRam"};

	public static void createCgPPC(Class clazz) {
		Linker32.prepareConstantBlock(clazz);
		TestSSA.createSSA(clazz);
		code = new MachineCode[TestCFG.cfg.length];
		for (int i = 0; i < TestCFG.cfg.length; i++){
			code[i] = new MachineCode(TestSSA.ssa[i]);
			TestSSA.ssa[i].print(0);
			code[i].print();
			System.out.println();
		}
	}

	public static int[] getCode(String name) {
		int i = 0;
		while (i < code.length && ! code[i].ssa.cfg.method.name.equals(HString.getHString(name))) i++;

		int len = code[i].iCount;
		int[] code1 = new int[len];
		for (int k = 0; k < len; k++) code1[k] = code[i].instructions[k];
		return code1;
	}

}
