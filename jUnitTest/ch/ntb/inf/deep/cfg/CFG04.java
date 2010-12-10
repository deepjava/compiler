package ch.ntb.inf.deep.cfg;


import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.Type;


public class CFG04 extends TestCFG implements ICclassFileConsts {

	@BeforeClass
	public static void setUp() {
		String workspace =System.getProperty("user.dir")+ "/bin";
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T04Loops" };
		try {
			Class.buildSystem(rootClassNames,new String[]{workspace, "../bsp/bin"},null, (1 << atxCode)
					| (1 << atxLocalVariableTable)
					| (1 << atxLineNumberTable)
					| (1 << atxExceptions));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (Type.nofRootClasses > 0) {
			createCFG(Type.rootClasses[0]);
		}
	}
 
    @Test
    public void doWhile1() {
        CFGNode[] nodes = getAndTestNodes("doWhile1", 3);
		testNode(nodes[0], 0, 1, false, null, new int[] {}, new int[] { 2 });
		testNode(nodes[1], 2, 8, true, nodes[0], new int[] { 0, 2 }, new int[] { 11, 2 });
		testNode(nodes[2], 11, 15, false, nodes[1], new int[] { 2 }, new int[] {});
    }

	@Test
	public void doWhileIf1() {
		CFGNode[] nodes = getAndTestNodes("doWhileIf1", 13);
		testNode(nodes[0], 0, 4, false, null, new int[] {}, new int[] { 5 });
		testNode(nodes[1], 5, 15, true, nodes[0], new int[] { 0, 39 }, new int[] { 22, 18 });
		testNode(nodes[2], 18, 19, false, nodes[1], new int[] { 5 }, new int[] { 23 });
		testNode(nodes[3], 22, 22, false, nodes[1], new int[] { 5 }, new int[] { 23 });
		testNode(nodes[4], 23, 26, false, nodes[1], new int[] { 22, 18 }, new int[] { 33, 29 });
		testNode(nodes[5], 29, 30, false, nodes[4], new int[] { 23 }, new int[] { 34 });
		testNode(nodes[6], 33, 33, false, nodes[4], new int[] { 23 }, new int[] { 34 });
		testNode(nodes[7], 34, 36, false, nodes[4], new int[] { 33, 29 }, new int[] { 39, 43 });
		testNode(nodes[8], 39, 40, false, nodes[7], new int[] { 34 }, new int[] { 5, 43 });
		testNode(nodes[9], 43, 44, false, nodes[7], new int[] { 34, 39 }, new int[] { 52, 47 });
		testNode(nodes[10], 47, 49, false, nodes[9], new int[] { 43 }, new int[] { 54 });
		testNode(nodes[11], 52, 53, false, nodes[9], new int[] { 43 }, new int[] { 54 });
		testNode(nodes[12], 54, 54, false, nodes[9], new int[] { 52, 47 }, new int[] {});
	}
	
	@Test
	public void while1() {
		CFGNode[] nodes = getAndTestNodes("while1", 4);
		testNode(nodes[0], 0, 2, false, null, new int[] {}, new int[] { 8 });
		testNode(nodes[1], 5, 5, false, nodes[2], new int[] { 8 }, new int[] { 8 });
		testNode(nodes[2], 8, 11, true, nodes[0], new int[] { 5, 0 }, new int[] { 14, 5 });
		testNode(nodes[3], 14, 15, false, nodes[2], new int[] { 8 }, new int[] {});
	}

    @Test
	public void whileTrue() {
		CFGNode[] nodes = getAndTestNodes("whileTrue", 2);	
		testNode(nodes[0], 0, 2, false, null, new int[] {}, new int[] {3});
		testNode(nodes[1], 3, 7, true, nodes[0], new int[] {0,3}, new int[] {3});
    }

    @Test
	public void whileTrueBreak() {
		CFGNode[] nodes = getAndTestNodes("whileTrueBreak", 1);	
		testNode(nodes[0], 0, 9, false, null, new int[] {}, new int[] {});
   }
    
	@Test
	public void whileMultiCond() {
		CFGNode[] nodes = getAndTestNodes("whileMultiCond", 5);
		testNode(nodes[0], 0, 2, false, null, new int[] {}, new int[] { 8 });
		testNode(nodes[1], 5, 5, false, nodes[3], new int[] { 14 }, new int[] { 8 });
		testNode(nodes[2], 8, 11, true, nodes[0], new int[] { 5, 0 }, new int[] { 14, 18 });
		testNode(nodes[3], 14, 15, false, nodes[2], new int[] { 8 }, new int[] { 5, 18 });
		testNode(nodes[4], 18, 19, false, nodes[2], new int[] { 14, 8 }, new int[] {});
	}

}
