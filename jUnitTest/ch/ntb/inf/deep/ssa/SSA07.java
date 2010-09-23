package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Type;

public class SSA07 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T07Arrays" };
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
	public void testEmptyIntArray() {
		SSANode[] nodes = getAndTestSSA(1, 1, 0);
		testNode(nodes[0], 3, 0, 2);
	}
	
	@Test
	public void testIntArrayParam() {
		SSANode[] nodes = getAndTestSSA(2, 4, 1);
		testNode(nodes[0], 5, 0, 7);
		testNode(nodes[1], 4, 0, 7);
		testNode(nodes[2], 2, 1, 7);
		testNode(nodes[3], 3, 0, 7);
	}
	
	@Test
	public void testStringArray() {
		SSANode[] nodes = getAndTestSSA(3, 1, 0);
		testNode(nodes[0], 20, 0, 5);
	}
	
	@Test
	public void testObjectArray() {
		SSANode[] nodes = getAndTestSSA(4, 1, 0);
		testNode(nodes[0], 7, 0, 5);
	}
	
	@Test
	public void testMultiArray(){
		SSANode[] nodes = getAndTestSSA(5, 1, 0);
		testNode(nodes[0], 26, 0, 10);
	}
}
