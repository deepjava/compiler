package ch.ntb.inf.deep.cfg;

import java.io.IOException;

import org.junit.*;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Type;

public class CFG03 extends TestCFG {

	@BeforeClass
	static public void setUp() throws Exception {
    	String[] rootClassNames = new String[]{"ch/ntb/inf/deep/testClasses/T03Switch"};
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
	public void switchNear1() {
		CFGNode[] nodes = getAndTestNodes(1, 5);
		testNode(nodes[0], 0, 1, false, null, new int[] {}, new int[] { 28,30,32,34 });
		testNode(nodes[1], 28, 29, false, nodes[0], new int[] { 0 }, new int[] { });
		testNode(nodes[2], 30, 31, false, nodes[0], new int[] { 0 }, new int[] { });
		testNode(nodes[3], 32, 33, false, nodes[0], new int[] { 0 }, new int[] { });
		testNode(nodes[4], 34, 35, false, nodes[0], new int[] { 0 }, new int[] { });
	}

	@Test
	public void switchNear2() {
		CFGNode[] nodes = getAndTestNodes(2, 9);
		testNode(nodes[0], 0, 1, false, null, new int[] {}, new int[] {40,42,44,50,52,55,61});
		testNode(nodes[1], 40, 41, false, nodes[0], new int[] {0}, new int[] {});
		testNode(nodes[2], 42, 43, false, nodes[0], new int[] {0}, new int[] {});
		testNode(nodes[3], 44, 47, false, nodes[0], new int[] {0}, new int[] {63});
		testNode(nodes[4], 50, 51, false, nodes[0], new int[] {0}, new int[] {});
		testNode(nodes[5], 52, 52, false, nodes[0], new int[] {0}, new int[] {55});
		testNode(nodes[6], 55, 58, false, nodes[0], new int[] {0,52}, new int[] {63});
		testNode(nodes[7], 61, 62, false, nodes[0], new int[] {0}, new int[] {});
		testNode(nodes[8], 63, 66, false, nodes[0], new int[] {44,55}, new int[] {});
	}

	@Test
	public void switchFar1() {
		CFGNode[] nodes = getAndTestNodes(3, 5);
		testNode(nodes[0], 0, 1, false, null, new int[] {}, new int[] {36,39,41,44});
		testNode(nodes[1], 36, 38, false, nodes[0], new int[] {0}, new int[] {});
		testNode(nodes[2], 39, 40, false, nodes[0], new int[] {0}, new int[] {});
		testNode(nodes[3], 41, 43, false, nodes[0], new int[] {0}, new int[] {});
		testNode(nodes[4], 44, 45, false, nodes[0], new int[] {0}, new int[] {});
	}

	@Test
	public void switchFar2() {
		CFGNode[] nodes = getAndTestNodes(4, 10);
		testNode(nodes[0], 0, 1, false, null, new int[] {}, new int[] {36,39,89,139});
		testNode(nodes[1], 36, 38, false, nodes[0], new int[] {0}, new int[] {});
		testNode(nodes[2], 39, 40, false, nodes[0], new int[] {0}, new int[] {89,79,81,89,87,89});
		testNode(nodes[3], 79, 80, false, nodes[2], new int[] {39}, new int[] {});
		testNode(nodes[4], 81, 84, false, nodes[2], new int[] {39}, new int[] {89});
		testNode(nodes[5], 87, 88, false, nodes[2], new int[] {39}, new int[] {});
		testNode(nodes[6], 89, 90, false, nodes[0], new int[] {0,39,81}, new int[] {139,137,131,137,137,139,137});
		testNode(nodes[7], 131, 134, false, nodes[6], new int[] {89}, new int[] {139});
		testNode(nodes[8], 137, 138, false, nodes[6], new int[] {89}, new int[] {});
		testNode(nodes[9], 139, 140, false, nodes[0], new int[] {0,89,131}, new int[] {});
	}


}
