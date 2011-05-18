package ch.ntb.inf.deep.cgPPC;

import ch.ntb.inf.deep.cfg.TestCFG;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.linker.Linker32;
import ch.ntb.inf.deep.ssa.SSAValue;
import ch.ntb.inf.deep.ssa.TestSSA;
import ch.ntb.inf.deep.strings.HString;

public class TestCgPPC implements ICclassFileConsts {
	static final boolean vol = false;
	static final boolean nonVol = true;
	
	static CodeGen[] code1;
	static String[] config = new String[] {"C:/NTbcheckout/EUser/JCC/Deep/ExampleProject.deep","BootFromRam"};
//	static String[] config = new String[] {"D:/work/Crosssystem/deep/ExampleProject.deep","BootFromRam"};

	public static void createCgPPC(Class clazz) {
		Linker32.prepareConstantBlock(clazz);
		TestSSA.createSSA(clazz);
		code1 = new CodeGen[TestCFG.cfg.length];
		for (int i = 0; i < TestCFG.cfg.length; i++){
			code1[i] = new CodeGen(TestSSA.ssa[i]);
			TestSSA.ssa[i].print(0);
			code1[i].print();
			System.out.println();
		}
	}

	public static int[] getCode(String name) {
		int i = 0;
		while (i < code1.length && !code1[i].ssa.cfg.method.name.equals(HString.getHString(name))) i++;

		int len = code1[i].iCount;
		int[] code = new int[len];
		for (int k = 0; k < len; k++) code[k] = code1[i].instructions[k];
		return code;
	}

	public static void createCgPPC1(Class clazz, String name) {
//		Linker32.prepareConstantBlock(clazz);
//		TestSSA.createSSA(clazz);
		int i = 0;
		while (i < TestSSA.ssa.length && !TestSSA.ssa[i].cfg.method.name.equals(HString.getHString(name))) i++;
		CodeGen code = new CodeGen(TestSSA.ssa[i]);
		TestSSA.ssa[i].cfg.printToLog();
		code.print();
//		System.out.println();
	}

	public static SSAValue getJoin(int index) {
		return RegAllocator.joins[index];
	}
	
	public static boolean checkJoin(SSAValue join, int start, int end, boolean nonVol, boolean next) {
		return join.start == start && join.end == end && (join.nonVol == nonVol) && (next? join.next!= null: join.next==null);
	}
}
