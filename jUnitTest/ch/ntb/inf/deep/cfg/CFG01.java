package ch.ntb.inf.deep.cfg;


import java.io.IOException;

import org.junit.*;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Type;



/**
 * - create and test CFG<br>
 */
public class CFG01 extends TestCFG {

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
				createCFG(Type.rootClasses[0]);
			}
	}

	@Test
	public void emptyMethodStatic() {
		CFGNode[] nodes = getAndTestNodes(1, 1);
		testNode(nodes[0], 0, 0, false, null, new int[] {}, new int[] {});
 	}
	
	@Test
	public void emptyMethod() {
		CFGNode[] nodes = getAndTestNodes(2, 1);
		testNode(nodes[0], 0, 0, false, null, new int[] {}, new int[] {});
 	}

	@Test
	public void assignment1() {
		CFGNode[] nodes = getAndTestNodes(3, 1);
		testNode(nodes[0], 0, 2, false, null, new int[] {}, new int[] {});
 	}
	
	@Test
	public void simple1() {
		CFGNode[] nodes = getAndTestNodes(4, 1);
		testNode(nodes[0], 0, 15, false, null, new int[] {}, new int[] {});
 	}
}
