package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Type;

public class SSA04 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		String workspace =System.getProperty("user.dir")+ "/bin";
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T04Loops" };
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
	public void testDoWhile1(){
		SSANode[] nodes = getAndTestSSA("doWhile1", 3, 1);
		testNode(nodes[0], 1, 0, 4);
		testNode(nodes[1], 4, 1, 4);
		testNode(nodes[2], 3, 0, 4);
	}
	
	@Test
	public void testDoWhileIf1(){
		SSANode[] nodes = getAndTestSSA("doWhileIf1", 13, 1);
		testNode(nodes[0], 2, 0, 6);
		testNode(nodes[1], 5, 2, 6);
		testNode(nodes[2], 2, 0, 6);
		testNode(nodes[3], 1, 0, 6);
		testNode(nodes[4], 2, 1, 6);
		testNode(nodes[5], 2, 0, 6);
		testNode(nodes[6], 1, 0, 6);
		testNode(nodes[7], 1, 1, 6);
		testNode(nodes[8], 1, 0, 6);
		testNode(nodes[9], 1, 0, 6);
		testNode(nodes[10], 2, 0, 6);
		testNode(nodes[11], 1, 0, 6);
		testNode(nodes[12], 1, 1, 6);
	}
	
	@Test
	public void testWhile1(){
		SSANode[] nodes = getAndTestSSA("while1", 4, 1);
		testNode(nodes[0], 2, 0, 3);
		testNode(nodes[1], 2, 0, 3);
		testNode(nodes[2], 2, 1, 3);
		testNode(nodes[3], 1, 0, 3);
	}
	
	@Test
	public void testWhileTrue(){
		SSANode[] nodes = getAndTestSSA("whileTrue", 2, 1);
		testNode(nodes[0], 1, 0, 4);
		testNode(nodes[1], 3, 0, 4);
	}
	
	@Test
	public void testWhileTrueBreak(){
		SSANode[] nodes = getAndTestSSA("whileTrueBreak", 1, 0);
		testNode(nodes[0], 4, 0, 4);
	}
	
	@Test
	public void testWhileMultiCond(){
		SSANode[] nodes = getAndTestSSA("whileMultiCond", 5, 1);
		testNode(nodes[0], 2, 0, 3);
		testNode(nodes[1], 2, 0, 3);
		testNode(nodes[2], 2, 1, 3);
		testNode(nodes[3], 1, 0, 3);
		testNode(nodes[4], 1, 0, 3);
	}
	
	@Test
	public void testFor1(){
		SSANode[] nodes = getAndTestSSA("for1", 4, 1);
		testNode(nodes[0], 3, 0, 4);
		testNode(nodes[1], 4, 0, 4);
		testNode(nodes[2], 2, 2, 4);
		testNode(nodes[3], 1, 0, 4);
	}
	
	@Test
	public void testForWhile(){
		SSANode[] nodes = getAndTestSSA("forWhile", 6, 2);
		testNode(nodes[0], 4, 0, 5);
		testNode(nodes[1], 2, 0, 5);
		testNode(nodes[2], 2, 1, 5);
		testNode(nodes[3], 2, 0, 5);
		testNode(nodes[4], 1, 2, 5);
		testNode(nodes[5], 1, 0, 5);
	}
	
	@Test
	public void testForIfWhile(){
		SSANode[] nodes =getAndTestSSA("forIfWhile", 8, 2);
		testNode(nodes[0], 3, 0, 4);
		testNode(nodes[1], 2, 0, 4);
		testNode(nodes[2], 1, 0, 4);
		testNode(nodes[3], 2, 0, 4);
		testNode(nodes[4], 2, 1, 4);
		testNode(nodes[5], 2, 2, 4);
		testNode(nodes[6], 2, 2, 4);
		testNode(nodes[7], 1, 0, 4);
	}
}
