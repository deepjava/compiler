package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;

public class cgPPC05 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		String workspace =System.getProperty("user.dir")+ "/bin";
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T05Returns" };
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
	public void multipleReturns1() {
		CodeGen code = getCode("multipleReturns1");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
	@Test
	public void multipleReturns2() {
		CodeGen code = getCode("multipleReturns2");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
}
