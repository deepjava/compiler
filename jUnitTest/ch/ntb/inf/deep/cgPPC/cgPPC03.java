package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;

public class cgPPC03 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		String workspace =System.getProperty("user.dir")+ "/bin";
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T03Switch" };
		Configuration.parseAndCreateConfig(config[0], config[1]);
		try {
			Class.buildSystem(rootClassNames,new String[]{workspace, "../bsp/bin"},Configuration.getSystemPrimitives(), (1 << atxCode)
					| (1 << atxLocalVariableTable)
					| (1 << atxLineNumberTable)
					| (1 << atxExceptions));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (Class.nofRootClasses > 0) {
			createCgPPC(Class.rootClasses[0]);
		}
	}

//	@Ignore
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
