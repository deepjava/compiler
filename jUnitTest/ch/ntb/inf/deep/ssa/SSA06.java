package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Type;

public class SSA06 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T06Operators" };
		try {
			Class.buildSystem(rootClassNames, (1 << IClassFileConsts.atxCode)
					| (1 << IClassFileConsts.atxLocalVariableTable)
					| (1 << IClassFileConsts.atxLineNumberTable)
					| (1 << IClassFileConsts.atxExceptions));
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
		SSANode[] nodes = getAndTestSSA(0, 1, 0);
		testNode(nodes[0], 2, 0, 2);
	}
	
	@Test
	public void testConditionalOperator1() {
		SSANode[] nodes = getAndTestSSA(1, 11, 0);
		testNode(nodes[0], 4, 0, 6);
		testNode(nodes[1], 0, 0, 6);
		testNode(nodes[2], 0, 0, 6);
		testNode(nodes[3], 1, 1, 6);
		testNode(nodes[4], 0, 0, 6);
		testNode(nodes[5], 0, 0, 6);
		testNode(nodes[6], 0, 0, 6);
		testNode(nodes[7], 0, 0, 6);
		testNode(nodes[8], 0, 0, 6);
		testNode(nodes[9], 0, 0, 6);
		testNode(nodes[10], 1, 1, 6);
	}
	
	@Test
	public void testConditionalOperator2() {
		SSANode[] nodes = getAndTestSSA(2, 14, 0);
		testNode(nodes[0], 8, 0, 11);
		testNode(nodes[1], 0, 0, 11);
		testNode(nodes[2], 0, 0, 11);
		testNode(nodes[3], 1, 1, 11);
		testNode(nodes[4], 0, 0, 11);
		testNode(nodes[5], 0, 0, 11);
		testNode(nodes[6], 0, 0, 11);
		testNode(nodes[7], 0, 0, 11);
		testNode(nodes[8], 0, 0, 11);
		testNode(nodes[9], 0, 0, 11);
		testNode(nodes[10], 1, 1, 11);
		testNode(nodes[11], 0, 0, 11);
		testNode(nodes[12], 1, 0, 11);
		testNode(nodes[13], 1, 0, 11);
	}
}
