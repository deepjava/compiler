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
		String[] rootClassNames = new String[]{"ch/ntb/inf/deep/testClasses/T01SimpleMethods"};
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
	public void emptyMethodStatic() {
		SSANode[] nodes = getAndTestNodes(1, 1);
//		testNode(nodes[0], 0, 0, false, null, new int[] {}, new int[] {});
 	}
	
	@Test
	public void emptyMethod() {
		SSANode[] nodes = getAndTestNodes(2, 1);
//		testNode(nodes[0], 0, 0, false, null, new int[] {}, new int[] {});
 	}

	@Test
	public void assignment1() {
		SSANode[] nodes = getAndTestNodes(3, 1);
//		testNode(nodes[0], 0, 2, false, null, new int[] {}, new int[] {});
 	}
	
	@Test
	public void simple1() {
		SSANode[] nodes = getAndTestNodes(4, 1);
//		testNode(nodes[0], 0, 15, false, null, new int[] {}, new int[] {});
 	}

}
