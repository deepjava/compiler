package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.strings.HString;

public class cgPPC03 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		Configuration.setActiveProject(project);
		project.setActiveTargetConfiguration("BootFromRam");
		HString[] rootClassNames = new HString[] { HString.getHString("ch/ntb/inf/deep/testClasses/T03Switch") };
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
	public void switchNear1() {
		CodeGen code = getCode("switchNear1");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
//	@Ignore
	@Test
	public void switchNear2() {
		CodeGen code = getCode("switchNear2");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 21, vol, false));
		for (int i = 3; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
//	@Ignore
	@Test
	public void switchNear3() {
		CodeGen code = getCode("switchNear3");
		assertNull("wrong join", getJoin(0));
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
//	@Ignore
	@Test
	public void switchFar1() {
		CodeGen code = getCode("switchFar1");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

//	@Ignore
	@Test
	public void switchFar2() {
		CodeGen code = getCode("switchFar2");
		assertNull("wrong join", getJoin(0));
		assertTrue("wrong join", checkJoin(getJoin(1), 0, 20, vol, false));
		for (int i = 2; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

	@Test
	public void switchWhile() {
		CodeGen code = getCode("switchWhile");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 10, vol, false));
		for (int i = 3; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

}
