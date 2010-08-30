package ch.ntb.inf.deep.ssa;

import static org.junit.Assert.*;

import java.util.List;

import ch.ntb.inf.deep.cfg.CFG;
import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.cfg.TestCFG;
import ch.ntb.inf.deep.classItems.Class;

public class TestSSA {

    /**
	 * Array of SSAs of all methods
	 */
	static public SSA[] ssa;

	/**
	 * Current IR instruction list
	 */
//	private List<SSAInstruction> ssaInstructionList;

	/**
	 * Max stack slots
	 */
	private int maxStack;

	/**
	 * Max local variables slots
	 */
	private int maxLocals;

	/**
	 * Exit set in the control flow graph node
	 */
//	private SSAVar[] exitSet;

	/**
	 * Entry set in the control flow graph node
	 */
//	private SSAVar[] entrySet;

    /**
	 * Creates SSA for all methods of a class
	 * 
	 * @param clazz
	 *            Java class object
	 */
	public static void createSSA(Class clazz) {
		TestCFG.createCFG(clazz);
		ssa = new SSA[TestCFG.cfg.length];
		for (int i = 0; i < TestCFG.cfg.length; i++){
			ssa[i] = new SSA(TestCFG.cfg[i]);
			System.out.println();
			ssa[i].print(0, i);
		}
	}

	/**
	 * Checks a SSANode
	 * 
	 * @param node
	 *            node to check
	 * @param nofSSAInstructions
	 *            expected number of ssa-instructions in this node
	 * @param nofPhiFunctions
	 * 			  expected number of phi functions in this node
	 * @param localsLength
	 * 			  expected length of the state array
	 */
	
	public static void testNode(SSANode node, int nofSSAInstructions, int nofPhiFunctions, int localsLength) {
		assertEquals("nof SSA instructions not as expected", nofSSAInstructions, node.nofInstr);
		assertEquals("nof phi functions not as expected", nofPhiFunctions, node.nofPhiFunc);
		assertEquals("length of EntrySet not as expected", localsLength, node.entrySet.length);
		assertEquals("length of ExitySet not as expected", localsLength, node.exitSet.length);
	}

	/**
	 * Tests nof nodes and nof loopheaders in a ssa
	 *  
	 * @param ssaNo
	 *            number of the SSA to check
	 * @param nofNodes
	 *            expected number of nodes in this ssa
	 * @param nofLoopheaders
	 * 			  expected number of loop headers in this ssa
	 */
	public static SSANode[] getAndTestSSA(int ssaNo, int nofNodes, int nofLoopheaders) {
		assertEquals("number of nodes not as expected", nofNodes, ssa[ssaNo].getNofNodes());
		assertEquals("number of loopheaders not as expected",nofLoopheaders,ssa[ssaNo].nofLoopheaders);
		return ssa[ssaNo].getNodes();
	}

	/**
	 * Checked the correctness of a phi function
	 * 
	 * @param bca
	 *            byte code address
	 * @param vars
	 *            Array of IR variable number
	 */
	// public void phiFunctions(int bca, Integer[][] vars) {
	// List<IRInstruction> phi = ssaInstructionList.getPhiFunctions();
	// assertEquals(bca, ssaInstructionList.get(bca).getBca());
	// for(int i = 0; i < vars.length; i++) {
	// assertEquals(vars[i][2], phi.get(i).getResult().getId());
	// // Parameters of the phi function begin at index 1, because the slot
	// // number is stored in parameter with index 0
	// Iterator iter = phi.get(i).getParams().iterator();
	// iter.next();
	// int j = 1;
	// while(iter.hasNext()) {
	// IRVar irvpara = (IRVar) iter.next();
	// assertEquals(vars[i][j], irvpara.getId());
	// j--;
	// }
	// }
	// }

	/**
	 * Checks the local variables in the entry set
	 * 
	 * @param var
	 *            Array of IR variable number
	 */
	// public void entrySetLocals(Integer[] var) {
	// for(int i = maxStack; i < (maxLocals + maxStack); i++) {
	// assertEquals(entrySet[i].getId(), var[i - maxStack]);
	// }
	// }

	/**
	 * Checks the local variables in the exit set
	 * 
	 * @param var
	 *            Array of IR variable number
	 */
	// public void exitSetLocals(Integer[] var) {
	// for(int i = maxStack; i < (maxLocals + maxStack); i++) {
	// assertEquals(exitSet[i].getId(), var[i - maxStack]);
	// }
	// }

}
