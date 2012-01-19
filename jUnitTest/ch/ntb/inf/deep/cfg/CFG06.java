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
public class CFG06 extends TestCFG implements ICclassFileConsts {

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
			createCFG(Class.rootClasses[0]);
		}
	}

	@Test
	public void conditionalOperator1() {
		CFGNode[] nodes = getAndTestNodes("conditionalOperator1", 11);
		testNode(nodes[0], 0, 11, false, null, new int[] {}, new int[] {14,18});
		testNode(nodes[1], 14, 15, false, nodes[0], new int[] {0}, new int[] {20});
		testNode(nodes[2], 18, 18, false, nodes[0], new int[] {0}, new int[] {20});
		testNode(nodes[3], 20, 25, false, nodes[0], new int[] {14,18}, new int[] {28,41});
		testNode(nodes[4], 28, 29, false, nodes[3], new int[] {20}, new int[] {32,37});
		testNode(nodes[5], 32, 34, false, nodes[4], new int[] {28}, new int[] {51});
		testNode(nodes[6], 37, 38, false, nodes[4], new int[] {28}, new int[] {51});
		testNode(nodes[7], 41, 43, false, nodes[3], new int[] {20}, new int[] {46,50});
		testNode(nodes[8], 46, 47, false, nodes[7], new int[] {41}, new int[] {51});
		testNode(nodes[9], 50, 50, false, nodes[7], new int[] {41}, new int[] {51});
		testNode(nodes[10], 51, 56, false, nodes[3], new int[] {32,37,46,50}, new int[] {});
	}

	@Test
	public void conditionalOperator2() {
		CFGNode[] nodes = getAndTestNodes("conditionalOperator2", 14);
		testNode(nodes[0], 0, 25, false, null, new int[] {}, new int[] {28,33});
		testNode(nodes[1], 28, 30, false, nodes[0], new int[] {0}, new int[] {35});
		testNode(nodes[2], 33, 33, false, nodes[0], new int[] {0}, new int[] {35});
		testNode(nodes[3], 35, 44, false, nodes[0], new int[] {28,33}, new int[] {47,61});
		testNode(nodes[4], 47, 48, false, nodes[3], new int[] {35}, new int[] {51,56});
		testNode(nodes[5], 51, 53, false, nodes[4], new int[] {47}, new int[] {74});
		testNode(nodes[6], 56, 58, false, nodes[4], new int[] {47}, new int[] {74});
		testNode(nodes[7], 61, 64, false, nodes[3], new int[] {35}, new int[] {67,72});
		testNode(nodes[8], 67, 69, false, nodes[7], new int[] {61}, new int[] {74});
		testNode(nodes[9], 72, 72, false, nodes[7], new int[] {61}, new int[] {74});
		testNode(nodes[10], 74, 82, false, nodes[3], new int[] {51,56,67,72}, new int[] {85,91});
		testNode(nodes[11], 85, 86, false, nodes[10], new int[] {74}, new int[] {89,91});
		testNode(nodes[12], 89, 90, false, nodes[11], new int[] {85}, new int[] {});
		testNode(nodes[13], 91, 92, false, nodes[10], new int[] {74,85}, new int[] {});
	}

}
