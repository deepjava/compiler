package ch.ntb.inf.deep.cfg;

import org.junit.Before;
import org.junit.Test;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.testClasses.T00EmptyClass;

/**
 * - create and test CFG<br>
 */
public class CFG00 extends TestCFG {

	@Before
	public void setUp() {
		createCFG(T00EmptyClass.class);
	}


    @Test
	public void testClassConstructor() {
    	// class constructor
		CFGNode[] nodes = getAndTestNodes(0, 1);
		testNode(nodes[0], 0, 4, false, null, new int[] {}, new int[] {});
	}
}
