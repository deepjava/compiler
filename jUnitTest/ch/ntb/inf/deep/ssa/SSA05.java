package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;

public class SSA05 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T05Returns" };
		Configuration.parseAndCreateConfig(config[0], config[1]);
		try {
			Class.buildSystem(rootClassNames,Configuration.getSearchPaths(),null, (1<<atxCode)|(1<<atxLocalVariableTable)|(1<<atxLineNumberTable)|(1<<atxExceptions));} catch (IOException e) {
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
	public void testMultipleReturns1Param() {
		SSANode[] nodes = getAndTestSSA("multipleReturns1", 3, 0);
		testNode(nodes[0], 3, 0, 3);
		testNode(nodes[1], 2, 0, 3);
		testNode(nodes[2], 2, 0, 3);
	}
	
	@Test
	public void testMultipleReturns1() {
		SSANode[] nodes = getAndTestSSA("multipleReturns2", 11, 0);
		testNode(nodes[0], 3, 0, 3);
		testNode(nodes[1], 2, 0, 3);
		testNode(nodes[2], 2, 0, 3);
		testNode(nodes[3], 2, 0, 3);
		testNode(nodes[4], 2, 0, 3);
		testNode(nodes[5], 2, 0, 3);
		testNode(nodes[6], 2, 0, 3);
		testNode(nodes[7], 2, 0, 3);
		testNode(nodes[8], 2, 0, 3);
		testNode(nodes[9], 2, 0, 3);
		testNode(nodes[10], 2, 0, 3);
	}
}
