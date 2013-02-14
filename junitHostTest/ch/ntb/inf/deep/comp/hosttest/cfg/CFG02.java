package ch.ntb.inf.deep.comp.hosttest.cfg;

import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.classItems.CFR;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.strings.HString;

/**
 * - create and test CFG<br>
 */
public class CFG02 extends TestCFG implements ICclassFileConsts {

	@BeforeClass
	public static void setUp() {
		Configuration.setActiveProject(project);
		project.setActiveTargetConfiguration("BootFromRam");
		HString[] rootClassNames = new HString[] { HString.getHString("ch/ntb/inf/deep/testClasses/T02Branches") };
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
    public void if1() {
        CFGNode[] nodes = getAndTestNodes("if1", 4);
		testNode(nodes[0], 0, 1, false, null, new int[] {}, new int[] { 4, 11 });
		testNode(nodes[1], 4, 8, false, nodes[0], new int[] { 0 }, new int[] { 15 });
		testNode(nodes[2], 11, 14, false, nodes[0], new int[] { 0 }, new int[] { 15 });
		testNode(nodes[3], 15, 16, false, nodes[0], new int[] { 4, 11 }, new int[] {});
    }
}
