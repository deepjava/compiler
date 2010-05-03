package ch.ntb.inf.deep.cfg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import helpers.Helpers;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IMethodInfo;

import ch.ntb.inf.deep.cfg.CFG;
import ch.ntb.inf.deep.cfg.CFGNode;

public class TestCFG {
	/**
	 * Class file reader
	 */
	static public IClassFileReader cfr;

    /**
	 * Array of CFGs of all methods
	 */
	static public CFG[] cfg;


    /**
	 * Creates CFGs for a java class
	 * 
	 * @param clazz
	 *            Java class object
	 */
	static public void createCFG(Class<?> clazz) {
		// create CFG
		cfr = ToolFactory.createDefaultClassFileReader(Helpers.getFilenameFromClass(clazz), IClassFileReader.ALL);
		IMethodInfo[] mi = cfr.getMethodInfos();
		cfg = new CFG[mi.length];
		for (int i = 0; i < cfg.length; i++) {
			cfg[i] = new CFG(mi[i]);
		}
	}

	/**
	 * Tests nof nodes in a cfg and returns nodes
	 *  
	 * @param cfgNo
	 *            number of the CFG
	 * @param nofNodes
	 *            number of nodes
	 */
	static public CFGNode[] getAndTestNodes(int cfgNo, int nofNodes) {
		assertEquals("number of nodes not as expected", nofNodes, cfg[cfgNo].getNumberOfNodes());
		
		CFGNode[] nodes = new CFGNode[nofNodes];
		CFGNode node = cfg[cfgNo].rootNode;
		for (int i = 0; i < nofNodes; i++) { nodes[i] = node; node = node.next; }
		return nodes;
	}

    /**
	 * Tests the properties of a CFG node
	 * 
	 * @param node
	 *            node
	 * @param firstBCA
	 *            BCA of the first instruction
	 * @param lastBCA
	 *            BCA of the last instruction
	 * @param isLoopHeader
	 *            is node a loop header
	 * @param idom
	 *            immediate dominator of this node
	 * @param predecessors
	 *            array of BCAs of the last instruction in a predecessor
	 * @param successors
	 *            array of BCAs of the first instruction in a successor
	 */
	static public void testNode(CFGNode node, int firstBCA, int lastBCA, boolean isLoopHeader, CFGNode idom, int[] pred, int[] succ) {
		assertEquals("firstBCA not as expected", firstBCA, node.firstBCA);
		assertEquals("lastBCA not as expected", lastBCA, node.lastBCA);
		assertEquals("loop header error", isLoopHeader, node.isLoopHeader());
		assertEquals("idom error", idom, node.idom);

		assertEquals("nof predecessors not as expected", pred.length, node.nofPredecessors);
		for (int i = 0; i < pred.length; i++) {
			assertNotNull(("no predecessor with firstBCA = " + pred[i]), node.getPredecessor(pred[i]));
		}

		assertEquals("nof successors not as expected", succ.length, node.nofSuccessors);
		for (int i = 0; i < succ.length; i++) {
			assertNotNull(("no successor with firstBCA = " + succ[i]),node.getSuccessor(succ[i]));
		}
	}
}
