package ch.ntb.inf.deep.ssa;

import org.junit.*;
import org.junit.Test;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.ssa.SSANode;
import ch.ntb.inf.deep.testClasses.T01SimpleMethods;

public class SSA01 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		createSSA(T01SimpleMethods.class);
	}

 	@Test
	public void emptyMethodStatic() {
		SSANode[] nodes = getAndTestNodes(1, 1);
//		testNode(nodes[0], 0, 0, false, null, new int[] {}, new int[] {});
 	}
	
	@Test
	public void emptyMethod() {
		SSANode[] nodes = getAndTestNodes(2, 1);
//		testNode(nodes[0], 0, 0, false, null, new int[] {}, new int[] {});
 	}

	@Test
	public void assignment1() {
		SSANode[] nodes = getAndTestNodes(3, 1);
//		testNode(nodes[0], 0, 2, false, null, new int[] {}, new int[] {});
 	}
	
	@Test
	public void simple1() {
		SSANode[] nodes = getAndTestNodes(4, 1);
//		testNode(nodes[0], 0, 15, false, null, new int[] {}, new int[] {});
 	}

}
