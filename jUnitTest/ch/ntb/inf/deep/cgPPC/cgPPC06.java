package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;

public class cgPPC06 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		String workspace =System.getProperty("user.dir")+ "/bin";
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T06Operators" };
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
	public void conditionalOperator1() {
		CodeGen code = getCode("conditionalOperator1");
		assertTrue("wrong join", checkJoin(getJoin(0), 5, 9, vol, true));
		assertTrue("wrong join", checkJoin(getJoin(0).next, 13, 22, vol, false));
		for (int i = 1; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
//	@Ignore
	@Test
	public void conditionalOperator2() {
		CodeGen code = getCode("conditionalOperator2");
		assertTrue("wrong join", checkJoin(getJoin(0), 9, 13, vol, true));
		assertTrue("wrong join", checkJoin(getJoin(0).next, 17, 26, vol, false));
		for (int i = 1; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
//	@Ignore
	@Test
	public void conditionalOperator3() {
		CodeGen code = getCode("conditionalOperator3");
		assertTrue("wrong join", checkJoin(getJoin(0), 5, 9, vol, false));
		for (int i = 1; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

//	@Ignore
	@Test
	public void conditionalOperator4() {
		CodeGen code = getCode("conditionalOperator4");
		assertTrue("wrong join", checkJoin(getJoin(0), 4, 8, vol, false));
		for (int i = 1; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

}
