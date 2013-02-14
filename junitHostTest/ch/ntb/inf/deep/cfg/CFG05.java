package ch.ntb.inf.deep.cfg;

import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.CFR;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.strings.HString;


/**
 * - create and test CFG<br>
 */
public class CFG05 extends TestCFG implements ICclassFileConsts {

	@BeforeClass
	public static void setUp() {
		Configuration.setActiveProject(project);
		project.setActiveTargetConfiguration("BootFromRam");
		HString[] rootClassNames = new HString[] { HString.getHString("ch/ntb/inf/deep/testClasses/T05Returns") };
		try {
			CFR.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemClasses(), attributes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (Class.nofRootClasses > 0) {
			createCFG(Class.rootClasses[0]);
		}
	}

	@Test
	public void multipleReturns1() {
		CFGNode[] nodes = getAndTestNodes("multipleReturns1", 3);
		testNode(nodes[0], 0, 3, false, null, new int[] {}, new int[] { 10, 6 });
		testNode(nodes[1], 6, 9, false, nodes[0], new int[] { 0 }, new int[] {});
		testNode(nodes[2], 10, 14, false, nodes[0], new int[] { 0 }, new int[] {});
	}
	
	@Test
	public void multipleReturns2() {
		CFGNode[] nodes = getAndTestNodes("multipleReturns2", 11);
		testNode(nodes[0], 0, 5, false, null, new int[] {}, new int[] { 8, 10 });
		testNode(nodes[1], 8, 9, false, nodes[0], new int[] { 0 }, new int[] {});
		testNode(nodes[2], 10, 12, false, nodes[0], new int[] { 0 }, new int[] { 17, 15 });
		testNode(nodes[3], 15, 16, false, nodes[2], new int[] { 10 }, new int[] {});
		testNode(nodes[4], 17, 19, false, nodes[2], new int[] { 10 }, new int[] { 22, 24 });
		testNode(nodes[5], 22, 23, false, nodes[4], new int[] { 17 }, new int[] {});
		testNode(nodes[6], 24, 26, false, nodes[4], new int[] { 17 }, new int[] { 31, 29 });
		testNode(nodes[7], 29, 30, false, nodes[6], new int[] { 24 }, new int[] {});
		testNode(nodes[8], 31, 33, false, nodes[6], new int[] { 24 }, new int[] { 38, 36 });
		testNode(nodes[9], 36, 37, false, nodes[8], new int[] { 31 }, new int[] {});
		testNode(nodes[10], 38, 40, false, nodes[8], new int[] { 31 }, new int[] {});
	}
}
