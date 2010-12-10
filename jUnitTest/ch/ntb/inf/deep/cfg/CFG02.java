package ch.ntb.inf.deep.cfg;


import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.Type;

/**
 * - create and test CFG<br>
 */
public class CFG02 extends TestCFG implements ICclassFileConsts {

	@BeforeClass
	public static void setUp() {
		String workspace =System.getProperty("user.dir")+ "/bin";
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T02Branches" };
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
    public void if1() {
        CFGNode[] nodes = getAndTestNodes("if1", 4);
		testNode(nodes[0], 0, 1, false, null, new int[] {}, new int[] { 4, 11 });
		testNode(nodes[1], 4, 8, false, nodes[0], new int[] { 0 }, new int[] { 15 });
		testNode(nodes[2], 11, 14, false, nodes[0], new int[] { 0 }, new int[] { 15 });
		testNode(nodes[3], 15, 16, false, nodes[0], new int[] { 4, 11 }, new int[] {});
    }
}
