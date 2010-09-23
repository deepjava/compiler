package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Type;

public class SSA02 extends TestSSA {
	
	@BeforeClass
	public static void setUp() {
		String[] rootClassNames = new String[]{"ch/ntb/inf/deep/testClasses/T02Branches"};
		try {
			Class.buildSystem(rootClassNames, (1<<IClassFileConsts.atxCode)|(1<<IClassFileConsts.atxLocalVariableTable)|(1<<IClassFileConsts.atxLineNumberTable)|(1<<IClassFileConsts.atxExceptions));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(Type.nofRootClasses > 0){
			createSSA(Type.rootClasses[0]);
		}
	}
	
	@Test
	public void testConstructor() {
		SSANode[] nodes = getAndTestSSA(0, 1, 0);
		testNode(nodes[0], 3, 0, 2);
	}
	
	@Test
	public void testIf1(){
		SSANode[] nodes = getAndTestSSA(1, 4, 0);
		testNode(nodes[0], 2, 0, 4);//1 Instruction because load parameter for first use
		testNode(nodes[1], 3, 0, 4);
		testNode(nodes[2], 2, 0, 4);
		testNode(nodes[3], 1, 1, 4);
	}
	
	@Test
	public void testIf2(){
		SSANode[] nodes = getAndTestSSA(2, 4, 0);
		testNode(nodes[0], 3, 0, 4);
		testNode(nodes[1], 2, 0, 4);
		testNode(nodes[2], 1, 0, 4);
		testNode(nodes[3], 3, 1, 4);
	}

	@Test
	public void testIf3(){
		SSANode[] nodes = getAndTestSSA(3, 6, 0);
		testNode(nodes[0], 3, 0, 7);
		testNode(nodes[1], 2, 0, 7);
		testNode(nodes[2], 4, 0, 7);
		testNode(nodes[3], 4, 0, 7);
		testNode(nodes[4], 5, 0, 7);
		testNode(nodes[5], 1, 2, 7);
	}
	
	@Test
	public void testIf4(){
		SSANode[] nodes = getAndTestSSA(4, 4, 0);
		testNode(nodes[0], 4, 0, 5);
		testNode(nodes[1], 2, 0, 5);
		testNode(nodes[2], 1, 0, 5);
		testNode(nodes[3], 3, 1, 5);
	}
	
	@Test
	public void testIf5(){
		SSANode[] nodes = getAndTestSSA(5, 4, 0);
		testNode(nodes[0], 2, 0, 5);
		testNode(nodes[1], 2, 0, 5);
		testNode(nodes[2], 1, 0, 5);
		testNode(nodes[3], 2, 2, 5);
	}
	
	
}
