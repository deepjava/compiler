package ch.ntb.inf.deep.ssa;

import static org.junit.Assert.assertEquals;
import ch.ntb.inf.deep.cfg.TestCFG;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;

public class TestSSA implements ICclassFileConsts{

	static public SSA[] ssa;

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
			ssa[i].print(0);
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
		assertEquals("nof phi functions not as expected", nofPhiFunctions, node.nofPhiFunc - node.nofDeletedPhiFunc);
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

}
