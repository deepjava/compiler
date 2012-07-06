package ch.ntb.inf.deep.cfg;

import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.strings.HString;

/**
 * - create and test CFG<br>
 */
public class CFG00 extends TestCFG implements ICclassFileConsts {

	@BeforeClass
	public static void setUp() {
		Configuration.setActiveProject(project);
		project.setActiveTargetConfiguration("BootFromRam");
		HString[] rootClassNames = new HString[] { HString.getHString("ch/ntb/inf/deep/testClasses/T00EmptyClass") };
		try {
			Class.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemPrimitives(), attributes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (Class.nofRootClasses > 0) {
			createCFG(Class.rootClasses[0]);
		}
	}

    @Test
	public void testConstructor() {
    	// constructor
		CFGNode[] nodes = getAndTestNodes("<init>", 1);
		testNode(nodes[0], 0, 4, false, null, new int[] {}, new int[] {});
	}
}
