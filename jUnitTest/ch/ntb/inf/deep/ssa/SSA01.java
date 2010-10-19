package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.*;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.ssa.SSANode;

public class SSA01 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		String workspace =System.getProperty("user.dir");
		String[] rootClassNames = new String[]{"ch/ntb/inf/deep/testClasses/T01SimpleMethods"};
		try {
			Class.buildSystem(rootClassNames,workspace, (1<<IClassFileConsts.atxCode)|(1<<IClassFileConsts.atxLocalVariableTable)|(1<<IClassFileConsts.atxLineNumberTable)|(1<<IClassFileConsts.atxExceptions));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(Type.nofRootClasses > 0){
			createSSA(Type.rootClasses[0]);
		}
	}

 	@Test
	public void emptyMethodStatic() {
		SSANode[] nodes = getAndTestSSA(1, 1, 0);
		testNode(nodes[0],1,0,0);
 	}
	
	@Test
	public void emptyMethod() {
		SSANode[] nodes = getAndTestSSA(2, 1, 0);
		testNode(nodes[0],1,0,1);
 	}

	@Test
	public void assignment1() {
		SSANode[] nodes = getAndTestSSA(3, 1,0);
		testNode(nodes[0],2,0,2);
 	}
	
	@Test
	public void simple1() {
		SSANode[] nodes = getAndTestSSA(4, 1,0);
		testNode(nodes[0],11,0,4);
 	}
	
	@Test
	public void simple2() {
		SSANode[] nodes = getAndTestSSA(5, 1,0);
		testNode(nodes[0],5,0,4);
 	}
	
	@Test
	public void simple3() {
		SSANode[] nodes = getAndTestSSA(6, 1,0);
		testNode(nodes[0],5,0,4);
 	}

}
