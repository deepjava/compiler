package ch.ntb.inf.deep.ssa;

import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.strings.HString;

public class SSA00 extends TestSSA {

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
		if(Class.nofRootClasses > 0){
			createSSA(Class.rootClasses[0]);
		}
	}

    @Test
	public void testConstructor() {
    	// constructor
		SSANode[] node = getAndTestSSA("<init>",1,0);
		testNode(node[0], 3, 0, 2);
	}
}
