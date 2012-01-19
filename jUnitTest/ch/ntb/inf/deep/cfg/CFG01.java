package ch.ntb.inf.deep.cfg;


import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.config.Configuration;



/**
 * - create and test CFG<br>
 */
public class CFG01 extends TestCFG implements ICclassFileConsts {

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
			createCFG(Class.rootClasses[0]);
		}
	}

	@Test
	public void emptyMethodStatic() {
		CFGNode[] nodes = getAndTestNodes("emptyMethodStatic", 1);
		testNode(nodes[0], 0, 0, false, null, new int[] {}, new int[] {});
 	}
	
	@Test
	public void emptyMethod() {
		CFGNode[] nodes = getAndTestNodes("emptyMethod", 1);
		testNode(nodes[0], 0, 0, false, null, new int[] {}, new int[] {});
 	}

	@Test
	public void assignment1() {
		CFGNode[] nodes = getAndTestNodes("assignment1", 1);
		testNode(nodes[0], 0, 2, false, null, new int[] {}, new int[] {});
 	}
	
	@Test
	public void simple1() {
		CFGNode[] nodes = getAndTestNodes("simple1", 1);
		testNode(nodes[0], 0, 15, false, null, new int[] {}, new int[] {});
 	}
}
