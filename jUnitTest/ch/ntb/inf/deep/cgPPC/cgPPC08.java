package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.strings.HString;

public class cgPPC08 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		Configuration.setActiveProject(project);
		project.setActiveTargetConfiguration("BootFromRam");
		HString[] rootClassNames = new HString[] { HString.getHString("ch/ntb/inf/deep/testClasses/T08Calls") };
		try {
			Class.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemPrimitives(), attributes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (Class.nofRootClasses > 0) {
			createCgPPC(Class.rootClasses[0]);
		}
	}

	@Test
	public void classMethod() {
		CodeGen code = getCode("classMethod");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 11, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 12, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(4), 1, 11, vol, false));
		for (int i = 5; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}


}
