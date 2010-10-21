package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Type;

public class SSA03 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		String workspace =System.getProperty("user.dir");
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T03Switch" };
		try {
			Class.buildSystem(rootClassNames,workspace, (1 << IClassFileConsts.atxCode)
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
	public void testSwitchNear1(){
		SSANode[] nodes = getAndTestSSA(1, 5, 0);
		testNode(nodes[0], 2, 0, 2);
		testNode(nodes[1], 2, 0, 2);
		testNode(nodes[2], 2, 0, 2);
		testNode(nodes[3], 2, 0, 2);
		testNode(nodes[4], 2, 0, 2);
	}
	
	@Test
	public void testSwitchNear2(){
		SSANode[] nodes = getAndTestSSA(2, 9, 0);
		testNode(nodes[0], 2, 0, 3);
		testNode(nodes[1], 2, 0, 3);
		testNode(nodes[2], 2, 0, 3);
		testNode(nodes[3], 3, 0, 3);
		testNode(nodes[4], 2, 0, 3);
		testNode(nodes[5], 2, 0, 3);
		testNode(nodes[6], 3, 1, 3);
		testNode(nodes[7], 2, 0, 3);
		testNode(nodes[8], 3, 1, 3);
	}
	@Test
	public void testSwitchNear3(){
		SSANode[] nodes = getAndTestSSA(3, 4, 0);
		testNode(nodes[0], 4, 0, 6);
		testNode(nodes[1], 2, 0, 6);
		testNode(nodes[2], 1, 0, 6);
		testNode(nodes[3], 4, 0, 6);
	}
	
	@Test
	public void testSwitchFar1(){
		SSANode[] nodes = getAndTestSSA(4, 5, 0);
		testNode(nodes[0], 2, 0, 2);
		testNode(nodes[1], 2, 0, 2);
		testNode(nodes[2], 2, 0, 2);
		testNode(nodes[3], 2, 0, 2);
		testNode(nodes[4], 2, 0, 2);		
	}
	
	@Test
	public void testSwitchFar2(){
		SSANode[] nodes = getAndTestSSA(5, 10, 0);
		testNode(nodes[0], 2, 0, 2);
		testNode(nodes[1], 2, 0, 2);
		testNode(nodes[2], 1, 0, 2);
		testNode(nodes[3], 2, 0, 2);
		testNode(nodes[4], 3, 0, 2);
		testNode(nodes[5], 2, 0, 2);
		testNode(nodes[6], 1, 1, 2);
		testNode(nodes[7], 3, 0, 2);
		testNode(nodes[8], 2, 0, 2);
		testNode(nodes[9], 1, 1, 2);
	}

}
