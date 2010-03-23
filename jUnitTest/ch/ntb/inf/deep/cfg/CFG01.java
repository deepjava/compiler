package ch.ntb.inf.deep.cfg;


import org.junit.Before;
import org.junit.Test;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.testClasses.T01SimpleMethods;


/**
 * - create and test CFG<br>
 */
public class CFG01 extends TestCFG {

	@Before
	public void setUp() {
		createCFG(T01SimpleMethods.class);
	}

	@Test
	public void emptyMethodStatic() {
		CFGNode[] nodes = getAndTestNodes(1, 1);
		testNode(nodes[0], 0, 0, false, null, new int[] {}, new int[] {});
 	}
	
	@Test
	public void emptyMethod() {
		CFGNode[] nodes = getAndTestNodes(2, 1);
		testNode(nodes[0], 0, 0, false, null, new int[] {}, new int[] {});
 	}

	@Test
	public void assignment1() {
		CFGNode[] nodes = getAndTestNodes(3, 1);
		testNode(nodes[0], 0, 2, false, null, new int[] {}, new int[] {});
 	}
	
	@Test
	public void simple1() {
		CFGNode[] nodes = getAndTestNodes(4, 1);
		testNode(nodes[0], 0, 15, false, null, new int[] {}, new int[] {});
 	}
}
