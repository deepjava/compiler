package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Type;

public class SSA09 extends TestSSA {
	@BeforeClass
	public static void setUp() {
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T09Types" };
		try {
			Class.buildSystem(rootClassNames, (1 << atxCode)
					| (1 << atxLocalVariableTable)
					| (1 << atxLineNumberTable)
					| (1 << atxExceptions));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (Type.nofRootClasses > 0) {
			createSSA(Type.rootClasses[0]);
		}
	}

	@Test
	public void testConstructor() {
		SSANode[] nodes = getAndTestSSA(1, 1, 0);
		testNode(nodes[0], 3, 0, 2);
	}
	
	@Test
	public void testM1(){
		SSANode[] nodes = getAndTestSSA(0, 7, 0);
		testNode(nodes[0], 4, 0, 11);
		testNode(nodes[1], 3, 0, 11);
		testNode(nodes[2], 1, 0, 11);
		testNode(nodes[3], 6, 0, 11);
		testNode(nodes[4], 2, 0, 11);
		testNode(nodes[5], 1, 0, 11);
		testNode(nodes[6], 5, 1, 11);
	}
	
	}
