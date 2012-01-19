package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;

public class SSA06 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T06Operators" };
		Configuration.parseAndCreateConfig(config[0], config[1]);
		try {
			Class.buildSystem(rootClassNames,Configuration.getSearchPaths(),Configuration.getSystemPrimitives(), (1<<atxCode)|(1<<atxLocalVariableTable)|(1<<atxLineNumberTable)|(1<<atxExceptions));} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (Class.nofRootClasses > 0) {
			createSSA(Class.rootClasses[0]);
		}
	}

	@Test
	public void testConstructor() {
		SSANode[] nodes = getAndTestSSA("<init>", 1, 0);
		testNode(nodes[0], 3, 0, 2);
	}
	
	@Test
	public void testConditionalOperator1() {
		SSANode[] nodes = getAndTestSSA("conditionalOperator1", 11, 0);
		testNode(nodes[0], 5, 0, 6);
		testNode(nodes[1], 2, 0, 6);
		testNode(nodes[2], 1, 0, 6);
		testNode(nodes[3], 3, 1, 6);
		testNode(nodes[4], 1, 0, 6);
		testNode(nodes[5], 2, 0, 6);
		testNode(nodes[6], 2, 0, 6);
		testNode(nodes[7], 1, 0, 6);
		testNode(nodes[8], 2, 0, 6);
		testNode(nodes[9], 1, 0, 6);
		testNode(nodes[10], 3, 1, 6);
	}
	
	@Test
	public void testConditionalOperator2() {
		SSANode[] nodes = getAndTestSSA("conditionalOperator2", 14, 0);
		testNode(nodes[0], 9, 0, 11);
		testNode(nodes[1], 2, 0, 11);
		testNode(nodes[2], 1, 0, 11);
		testNode(nodes[3], 3, 1, 11);
		testNode(nodes[4], 1, 0, 11);
		testNode(nodes[5], 2, 0, 11);
		testNode(nodes[6], 2, 0, 11);
		testNode(nodes[7], 1, 0, 11);
		testNode(nodes[8], 2, 0, 11);
		testNode(nodes[9], 1, 0, 11);
		testNode(nodes[10], 3, 1, 11);
		testNode(nodes[11], 1, 0, 11);
		testNode(nodes[12], 2, 0, 11);
		testNode(nodes[13], 2, 0, 11);
	}
	
	@Test
	public void testConditionalOperator3() {
		SSANode[] nodes = getAndTestSSA("conditionalOperator3", 5, 0);
		testNode(nodes[0], 4, 0, 5);
		testNode(nodes[1], 1, 0, 5);
		testNode(nodes[2], 2, 0, 5);
		testNode(nodes[3], 1, 0, 5);
		testNode(nodes[4], 2, 1, 5);
	}
	
	@Test
	public void testConditionalOperator4() {
		SSANode[] nodes = getAndTestSSA("conditionalOperator4", 4, 0);
		testNode(nodes[0], 4, 0, 4);
		testNode(nodes[1], 2, 0, 4);
		testNode(nodes[2], 1, 0, 4);
		testNode(nodes[3], 1, 1, 4);
	}
}
