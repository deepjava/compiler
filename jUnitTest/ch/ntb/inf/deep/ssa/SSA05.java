package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Type;

public class SSA05 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T05Returns" };
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
	public void testMultipleReturns1Param() {
		SSANode[] nodes = getAndTestSSA(1, 3, 0);
		testNode(nodes[0], 2, 0, 3);
		testNode(nodes[1], 1, 0, 3);
		testNode(nodes[2], 1, 0, 3);
	}
	
	@Test
	public void testMultipleReturns1() {
		SSANode[] nodes = getAndTestSSA(2, 11, 0);
		testNode(nodes[0], 2, 0, 3);
		testNode(nodes[1], 1, 0, 3);
		testNode(nodes[2], 1, 0, 3);
		testNode(nodes[3], 1, 0, 3);
		testNode(nodes[4], 1, 0, 3);
		testNode(nodes[5], 1, 0, 3);
		testNode(nodes[6], 1, 0, 3);
		testNode(nodes[7], 1, 0, 3);
		testNode(nodes[8], 1, 0, 3);
		testNode(nodes[9], 1, 0, 3);
		testNode(nodes[10], 1, 0, 3);
	}
}
