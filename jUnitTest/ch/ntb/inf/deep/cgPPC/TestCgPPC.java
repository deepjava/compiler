package ch.ntb.inf.deep.cgPPC;

import ch.ntb.inf.deep.cfg.TestCFG;
import ch.ntb.inf.deep.cgPPC.CodeGen;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Project;
import ch.ntb.inf.deep.ssa.SSA;
import ch.ntb.inf.deep.ssa.SSANode;
import ch.ntb.inf.deep.ssa.SSAValue;
import ch.ntb.inf.deep.ssa.instruction.PhiFunction;
import ch.ntb.inf.deep.strings.HString;

public class TestCgPPC implements ICclassFileConsts {
	static final boolean vol = false;
	static final boolean nonVol = true;
	
	static SSA[] ssa;
	static CodeGen[] code;
	static int attributes = (1 << atxCode) | (1 << atxLocalVariableTable) | (1 << atxExceptions) | (1 << atxLineNumberTable);
	static String workspace = System.getProperty("user.dir");
	static Project project = Configuration.addProject(workspace + "/junitTest.deep");

	public static void createCgPPC(Class clazz) {
		TestCFG.createCFG(clazz);
		int nofMethods = TestCFG.cfg.length;
		ssa = new SSA[nofMethods];
		code = new CodeGen[nofMethods];
		for (int i = 0; i < nofMethods; i++){
			ssa[i] = new SSA(TestCFG.cfg[i]);
		}
	}


	public static CodeGen getCode(String name) {
		int i = 0;
		while (i < TestCFG.cfg.length && !TestCFG.cfg[i].method.name.equals(HString.getHString(name))) i++;
		code[i] = new CodeGen(ssa[i]);
//		ssa[i].cfg.printToLog();
//		code[i].ssa.print(0);
//		System.out.print(code[i].toString());
//		System.out.println();
		return code[i];
	}

	public static SSAValue getJoin(int index) {
		return RegAllocator.joins[index];
	}
	
	public static boolean checkJoin(SSAValue join, int start, int end, boolean nonVol, boolean next) {
		assert join != null;
		return join.start == start && join.end == end && (join.nonVol == nonVol) && (next? join.next!= null: join.next==null);
	}
	
	public static boolean checkPhiFunction(SSA ssa, int ssaInstrNr, boolean deleted, boolean used) {
		SSANode node = (SSANode) ssa.cfg.rootNode;
		PhiFunction phi = null;
		while (node != null) {
			for (int i = 0; i < node.nofPhiFunc; i++) {
				if (node.phiFunctions[i].result.n == ssaInstrNr) {
					phi = (PhiFunction)node.phiFunctions[i];
					node = null; 
					break;
				}
			}
			if (node != null) node = (SSANode) node.next;
		}
		return (phi.deleted == deleted && phi.used == used);
	}
}
