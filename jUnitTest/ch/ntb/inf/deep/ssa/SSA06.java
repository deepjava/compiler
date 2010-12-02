package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Type;

public class SSA06 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T06Operators" };
		try {
			Class.buildSystem(rootClassNames, (1 << atxCode)
					| (1 << atxLocalVariableTable)
					| (1 << atxLineNumberTable)
					| (1 << atxExceptions));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (Type.nofRootClasses > 0) {
			createSSA(Type.rootClasses[0]);
		}
	}

	@Test
	public void testConstructor() {
		SSANode[] nodes = getAndTestSSA(3, 1, 0);
		testNode(nodes[0], 3, 0, 2);
	}
	
	@Test
	public void testConditionalOperator1() {
		SSANode[] nodes = getAndTestSSA(0, 11, 0);
		testNode(nodes[0], 5, 0, 6);
		testNode(nodes[1], 1, 0, 6);
		testNode(nodes[2], 0, 0, 6);
		testNode(nodes[3], 2, 1, 6);
		testNode(nodes[4], 1, 0, 6);
		testNode(nodes[5], 1, 0, 6);
		testNode(nodes[6], 1, 0, 6);
		testNode(nodes[7], 1, 0, 6);
		testNode(nodes[8], 1, 0, 6);
		testNode(nodes[9], 0, 0, 6);
		testNode(nodes[10], 2, 1, 6);
	}
	
	@Test
	public void testConditionalOperator2() {
		SSANode[] nodes = getAndTestSSA(1, 14, 0);
		testNode(nodes[0], 9, 0, 11);
		testNode(nodes[1], 1, 0, 11);
		testNode(nodes[2], 0, 0, 11);
		testNode(nodes[3], 2, 1, 11);
		testNode(nodes[4], 1, 0, 11);
		testNode(nodes[5], 1, 0, 11);
		testNode(nodes[6], 1, 0, 11);
		testNode(nodes[7], 1, 0, 11);
		testNode(nodes[8], 1, 0, 11);
		testNode(nodes[9], 0, 0, 11);
		testNode(nodes[10], 2, 1, 11);
		testNode(nodes[11], 1, 0, 11);
		testNode(nodes[12], 2, 0, 11);
		testNode(nodes[13], 2, 0, 11);
	}
}
