package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Type;

public class SSA07 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		String workspace =System.getProperty("user.dir")+ "/bin";
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T07Arrays" };
		try {
			Class.buildSystem(rootClassNames,new String[]{workspace, "../bsp/bin"},null, (1 << atxCode)
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
		SSANode[] nodes = getAndTestSSA("<init>", 1, 0);
		testNode(nodes[0], 3, 0, 2);
	}
	
	@Test
	public void testEmptyIntArray() {
		SSANode[] nodes = getAndTestSSA("emptyIntArray", 1, 0);
		testNode(nodes[0], 3, 0, 2);
	}
	
	@Test
	public void testIntArrayParam() {
		SSANode[] nodes = getAndTestSSA("intArray", 4, 1);
		testNode(nodes[0], 5, 0, 7);
		testNode(nodes[1], 4, 0, 7);
		testNode(nodes[2], 2, 1, 7);
		testNode(nodes[3], 3, 0, 7);
	}
	
	@Test
	public void testStringArray() {
		SSANode[] nodes = getAndTestSSA("stringArray", 1, 0);
		testNode(nodes[0], 20, 0, 5);
	}
	
	@Test
	public void testObjectArray() {
		SSANode[] nodes = getAndTestSSA("objectArray", 1, 0);
		testNode(nodes[0], 7, 0, 5);
	}
	
	@Test
	public void testMultiArray(){
		SSANode[] nodes = getAndTestSSA("multiArray", 1, 0);
		testNode(nodes[0], 26, 0, 10);
	}
}
