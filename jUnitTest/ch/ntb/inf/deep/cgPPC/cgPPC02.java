package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.junit.BeforeClass;
import org.junit.Test;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.strings.HString;

public class cgPPC02 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		Configuration.setActiveProject(project);
		project.setActiveTargetConfiguration("BootFromRam");
		HString[] rootClassNames = new HString[] { HString.getHString("ch/ntb/inf/deep/testClasses/T02Branches") };
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
	public void if1() {
		CodeGen code = getCode("if1");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 3, 8, vol, false));
		for (int i = 4; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 7, false, false));
	}
	
	@Test
	public void if2() {
		CodeGen code = getCode("if2");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 3, 9, vol, false));
		for (int i = 3; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 6, false, false));
	}
	
	@Test
	public void if3() {
		CodeGen code = getCode("if3");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 18, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 1, 19, vol, false));
		for (int i = 4; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 18, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 19, false, false));
	}
	
	@Test
	public void if4() {
		CodeGen code = getCode("if4");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertNull("wrong join", getJoin(3));
		assertTrue("wrong join", checkJoin(getJoin(4), 4, 8, vol, false));
		for (int i = 5; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 7, false, false));
	}
	
	@Test
	public void if5() {
		CodeGen code = getCode("if5");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 7, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(4), 0, 7, vol, false));
		for (int i = 5; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 5, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 6, false, false));
	}
	
	@Test
	public void if6() {
		CodeGen code = getCode("if6");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertNull("wrong join", getJoin(3));
		assertNull("wrong join", getJoin(4));
		assertTrue("wrong join", checkJoin(getJoin(5), 0, 34, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(7), 0, 36, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(8), 1, 25, vol, false));
		for (int i = 9; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 7, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 19, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 24, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 25, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 34, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 35, false, false));
	}
	
//@Ignore
	@Test
	public void if7() {
		CodeGen code = getCode("if7");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 25, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(4), 8, 24, vol, false));
		for (int i = 5; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 6, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 17, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 20, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 21, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 22, false, false));
	}
}
