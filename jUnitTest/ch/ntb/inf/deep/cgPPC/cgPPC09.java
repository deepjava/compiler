package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;

public class cgPPC09 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		String workspace =System.getProperty("user.dir")+ "/bin";
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T09Types" };
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
	public void m1() {
		CodeGen code = getCode("m1");
		assertTrue("wrong join", checkJoin(getJoin(0), 14, 18, vol, false));
		for (int i = 1; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
//	@Ignore
	@Test
	public void m2() {
		CodeGen code = getCode("m2");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
//	@Ignore
	@Test
	public void callm2() {
		CodeGen code = getCode("callm2");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
//	@Ignore
	@Test
	public void m3() {
		CodeGen code = getCode("m3");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertNull("wrong join", getJoin(3));
		assertTrue("wrong join", checkJoin(getJoin(4), 0, 6, vol, false));
		for (int i = 5; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
}
