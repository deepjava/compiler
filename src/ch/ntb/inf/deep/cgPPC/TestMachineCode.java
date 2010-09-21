package ch.ntb.inf.deep.cgPPC;

import java.io.IOException;


import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.cfg.CFG;
import ch.ntb.inf.deep.ssa.SSA;


public class TestMachineCode {
	
	static CFG[] cfg;
	static SSA[] ssa;
	static MachineCode[] code;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		String[] rootClassNames = new String[]{"ch/ntb/inf/deep/testClasses/T01SimpleMethods"};
//		String[] rootClassNames = new String[]{"ch/ntb/inf/deep/testClasses/T02Branches"};
//		String[] rootClassNames = new String[]{"ch/ntb/inf/deep/testClasses/T03Switch"};
		String[] rootClassNames = new String[]{"ch/ntb/inf/deep/testClasses/T04Loops"};
		try {
			Class.buildSystem(rootClassNames, (1<<IClassFileConsts.atxCode)|(1<<IClassFileConsts.atxLocalVariableTable)|(1<<IClassFileConsts.atxLineNumberTable)|(1<<IClassFileConsts.atxExceptions));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(Type.nofRootClasses > 0){
			Method m1,m2;
			
			int count = 0;	
			m1 =(Method) Type.rootClasses[0].methods;
			m2 = m1;
			while (m2 != null){
				count++;
				m2 = (Method)m2.next;
			}
			cfg = new CFG[count];
			for (int i = 0; i < count; i++) {
				cfg[i] = new CFG(m1);
				m1 = (Method)m1.next;
			}
			ssa = new SSA[cfg.length];
			code = new MachineCode[cfg.length];
//			for (int i = 0; i < cfg.length; i++){
			int i = 4; 
				ssa[i] = new SSA(cfg[i]);
				
				RegAllocator.allocateRegisters(ssa[i]);
				
				System.out.println();
				ssa[i].print(0, i);
				code[i] = new MachineCode(ssa[i]);
				code[i].print();
//			}
		}
	}

}
