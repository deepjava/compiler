package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Type;

public class SSA08 extends TestSSA {
	@BeforeClass
	public static void setUp() {
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T08Calls" };
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
		testNode(nodes[0], 3, 0, 2);
	}
	
	@Test
	public void testClassMethCall(){
		SSANode[] nodes = getAndTestSSA(1, 1, 0);
		testNode(nodes[0], 6, 0, 4);
	}
	
	@Test
	public void testObjectMethCall(){
		SSANode[] nodes = getAndTestSSA(2, 1, 0);
		testNode(nodes[0], 5, 0, 3);
	}
	
	@Test
	public void testCallToAnotherClass(){
		SSANode[] nodes = getAndTestSSA(3, 1, 0);
		testNode(nodes[0], 2, 0, 0);
	}

}
