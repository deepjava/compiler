package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;

public class cgPPC01 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		String workspace =System.getProperty("user.dir")+ "/bin";
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T01SimpleMethods" };
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

	
	@Test
	public void assignment1() {
		CodeGen code = getCode("assignment1");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
	//	@Ignore
	@Test 
	public void assignment2() {
		CodeGen code = getCode("assignment2");
		assertTrue("wrong join", checkJoin(getJoin(0), 3, 7, vol, false));
		assertNull("wrong join", getJoin(1));
	}
	
	//	@Ignore
	@Test 
	public void assignment3() {
		CodeGen code = getCode("assignment3");
		assertTrue("wrong join", checkJoin(getJoin(0), 5, 9, vol, false));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 25, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 16, 23, vol, false));
		assertNull("wrong join", getJoin(4));
	}

	@Test
	public void simple1() {
		CodeGen code = getCode("simple1");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
	@Test
	public void simple2() {
		CodeGen code = getCode("simple2");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
	@Test
	public void simple3() {
		CodeGen code = getCode("simple3");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
	@Test
	public void simple4() {
		CodeGen code = getCode("simple4");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

//	@Ignore
	@Test
	public void simple5() {
		CodeGen code = getCode("simple5");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

//	@Ignore
	@Test
	public void simple6() {
		CodeGen code = getCode("simple6");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
}
