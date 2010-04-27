package ch.ntb.inf.deep.ssa;

import org.junit.Before;
import org.junit.Test;

import ch.ntb.inf.deep.ssa.SSANode;
import ch.ntb.inf.deep.testClasses.T00EmptyClass;

public class SSA00 extends TestSSA {

	@Before
	public void setUp() {
		createSSA(T00EmptyClass.class);
	}

    @Test
	public void testConstructor() {
    	// constructor
		SSANode[] nodes = getAndTestNodes(0, 1);
//		SSAValue exitSet[] = new SSAValue[] 
//		testNode(nodes[0], 2, 0, new SSAValue[] {}, {});
	}
}
