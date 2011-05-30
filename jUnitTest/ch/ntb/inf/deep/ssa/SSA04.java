package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.config.Configuration;

public class SSA04 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T04Loops" };
		Configuration.parseAndCreateConfig(config[0], config[1]);
		try {
			Class.buildSystem(rootClassNames,Configuration.getSearchPaths(),null, (1<<atxCode)|(1<<atxLocalVariableTable)|(1<<atxLineNumberTable)|(1<<atxExceptions));} catch (IOException e) {
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
		testNode(nodes[4], 3, 1, 6);
		testNode(nodes[5], 2, 0, 6);
		testNode(nodes[6], 1, 0, 6);
		testNode(nodes[7], 2, 1, 6);
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
		testNode(nodes[0], 5, 0, 4);
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
		testNode(nodes[5], 2, 1, 4);
		testNode(nodes[6], 2, 1, 4);
		testNode(nodes[7], 1, 0, 4);
	}
	
	@Test
	public void whileTrue2() {
		SSANode[] nodes =getAndTestSSA("whileTrue2", 4, 2);
		testNode(nodes[0], 5, 0, 4);
		testNode(nodes[1], 2, 0, 4);
		testNode(nodes[2], 2, 1, 4);
		testNode(nodes[3], 1, 0, 4);
	}

	//	@Ignore
	@Test
	public void forIfFor() {
		SSANode[] nodes =getAndTestSSA("forIfFor", 16, 4);
		testNode(nodes[0], 4, 0, 7);
		testNode(nodes[1], 4, 0, 7);
		testNode(nodes[2], 4, 0, 7);
		testNode(nodes[3], 2, 2, 7);
		testNode(nodes[4], 2, 0, 7);
		testNode(nodes[5], 2, 0, 7);
		testNode(nodes[6], 3, 0, 7);
		testNode(nodes[7], 2, 0, 7);
		testNode(nodes[8], 4, 0, 7);
		testNode(nodes[9], 2, 2, 7);
		testNode(nodes[10], 2, 0, 7);
		testNode(nodes[11], 4, 0, 7);
		testNode(nodes[12], 2, 2, 7);
		testNode(nodes[13], 2, 1, 7);
		testNode(nodes[14], 2, 2, 7);
		testNode(nodes[15], 1, 2, 7);
	}

	//	@Ignore
	@Test
	public void phiFunctionTest1() {
		SSANode[] nodes =getAndTestSSA("phiFunctionTest1", 2, 1);
		testNode(nodes[0], 4, 0, 4);
		testNode(nodes[1], 1, 0, 4);
	}
	
	//	@Ignore
	@Test
	public void phiFunctionTest2() {
		SSANode[] nodes =getAndTestSSA("phiFunctionTest2", 3, 1);
		testNode(nodes[0], 1, 0, 4);
		testNode(nodes[1], 4, 0, 4);
		testNode(nodes[2], 1, 0, 4);
	}
	
	//	@Ignore
	@Test
	public void phiFunctionTest3() {
		SSANode[] nodes =getAndTestSSA("phiFunctionTest3", 3, 1);
		testNode(nodes[0], 1, 0, 4);
		testNode(nodes[1], 5, 1, 4);
		testNode(nodes[2], 1, 0, 4);
	}
	
	//	@Ignore
	@Test
	public void phiFunctionTest4() {
		SSANode[] nodes =getAndTestSSA("phiFunctionTest4", 3, 1);
		testNode(nodes[0], 1, 0, 4);
		testNode(nodes[1], 3, 0, 4);
		testNode(nodes[2], 1, 0, 4);
	}
	
	//	@Ignore
	@Test
	public void phiFunctionTest5() {
		SSANode[] nodes =getAndTestSSA("phiFunctionTest5", 3, 1);
		testNode(nodes[0], 1, 0, 4);
		testNode(nodes[1], 4, 1, 4);
		testNode(nodes[2], 1, 0, 4);
	}

	//	@Ignore
	@Test
	public void phiFunctionTest6() {
		SSANode[] nodes =getAndTestSSA("phiFunctionTest6", 7, 2);
		testNode(nodes[0], 2, 0, 4);
		testNode(nodes[1], 2, 0, 4);
		testNode(nodes[2], 2, 1, 4);
		testNode(nodes[3], 3, 0, 4);
		testNode(nodes[4], 2, 0, 4);
		testNode(nodes[5], 2, 1, 4);
		testNode(nodes[6], 3, 0, 4);
	}

	//	@Ignore
	@Test
	public void phiFunctionTest7() {
		SSANode[] nodes =getAndTestSSA("phiFunctionTest7", 7, 2);
		testNode(nodes[0], 3, 0, 4);
		testNode(nodes[1], 2, 0, 4);
		testNode(nodes[2], 2, 1, 4);
		testNode(nodes[3], 2, 0, 4);
		testNode(nodes[4], 2, 0, 4);
		testNode(nodes[5], 2, 1, 4);
		testNode(nodes[6], 3, 0, 4);
	}

	//	@Ignore
	@Test
	public void phiFunctionTest8() {
		SSANode[] nodes =getAndTestSSA("phiFunctionTest8", 7, 2);
		testNode(nodes[0], 3, 0, 5);
		testNode(nodes[1], 2, 0, 5);
		testNode(nodes[2], 2, 1, 5);
		testNode(nodes[3], 4, 0, 5);
		testNode(nodes[4], 2, 0, 5);
		testNode(nodes[5], 2, 1, 5);
		testNode(nodes[6], 3, 0, 5);
	}

	//	@Ignore
	@Test
	public void phiFunctionTest9() {
		SSANode[] nodes =getAndTestSSA("phiFunctionTest9", 7, 2);
		testNode(nodes[0], 3, 0, 6);
		testNode(nodes[1], 2, 0, 6);
		testNode(nodes[2], 3, 1, 6);
		testNode(nodes[3], 4, 0, 6);
		testNode(nodes[4], 2, 0, 6);
		testNode(nodes[5], 2, 1, 6);
		testNode(nodes[6], 3, 0, 6);
	}


	//	@Ignore
	@Test 
	public void phiFunctionTest10() {
		SSANode[] nodes =getAndTestSSA("phiFunctionTest10", 7, 2);
		testNode(nodes[0], 3, 0, 5);
		testNode(nodes[1], 2, 0, 5);
		testNode(nodes[2], 2, 1, 5);
		testNode(nodes[3], 3, 0, 5);
		testNode(nodes[4], 4, 0, 5);
		testNode(nodes[5], 2, 2, 5);
		testNode(nodes[6], 2, 0, 5);
	}
	
	//	@Ignore
	@Test 
	public void phiFunctionTest11() {
		SSANode[] nodes =getAndTestSSA("phiFunctionTest11", 2, 1);
		testNode(nodes[0], 6, 0, 4);
		testNode(nodes[1], 1, 0, 4);
	}

	//	@Ignore
	@Test 
	public void phiFunctionTest12() {
		SSANode[] nodes =getAndTestSSA("phiFunctionTest12", 3, 1);
		testNode(nodes[0], 2, 0, 4);
		testNode(nodes[1], 2, 1, 4);
		testNode(nodes[2], 1, 0, 4);
	}

	//	@Ignore
	@Test 
	public void phiFunctionTest13() {
		SSANode[] nodes =getAndTestSSA("phiFunctionTest13", 10, 2);
		testNode(nodes[0], 2, 0, 4);
		testNode(nodes[1], 2, 0, 4);
		testNode(nodes[2], 2, 0, 4);
		testNode(nodes[3], 3, 1, 4);
		testNode(nodes[4], 2, 0, 4);
		testNode(nodes[5], 2, 0, 4);
		testNode(nodes[6], 2, 0, 4);
		testNode(nodes[7], 2, 1, 4);
		testNode(nodes[8], 1, 0, 4);
		testNode(nodes[9], 1, 1, 4);
	}

	//	@Ignore
	@Test 
	public void phiFunctionTest14() {
		SSANode[] nodes =getAndTestSSA("phiFunctionTest14", 8, 2);
		testNode(nodes[0], 3, 0, 8);
		testNode(nodes[1], 4, 1, 8);
		testNode(nodes[2], 3, 0, 8);
		testNode(nodes[3], 5, 0, 8);
		testNode(nodes[4], 1, 0, 8);
		testNode(nodes[5], 3, 0, 8);
		testNode(nodes[6], 1, 1, 8);
		testNode(nodes[7], 3, 0, 8);
	}

	//	@Ignore
	@Test 
	public void phiFunctionTest15() {
		SSANode[] nodes =getAndTestSSA("phiFunctionTest15", 6, 2);
		testNode(nodes[0], 2, 0, 8);
		testNode(nodes[1], 4, 1, 8);
		testNode(nodes[2], 2, 0, 8);
		testNode(nodes[3], 7, 0, 8);
		testNode(nodes[4], 1, 1, 8);
		testNode(nodes[5], 3, 0, 8);
	}

	//	@Ignore
	@Test 
	public void phiFunctionTest16() {
		SSANode[] nodes =getAndTestSSA("phiFunctionTest16", 9, 2);
		testNode(nodes[0], 3, 0, 6);
		testNode(nodes[1], 2, 0, 6);
		testNode(nodes[2], 2, 1, 6);
		testNode(nodes[3], 2, 0, 6);
		testNode(nodes[4], 2, 0, 6);
		testNode(nodes[5], 2, 0, 6);
		testNode(nodes[6], 1, 0, 6);
		testNode(nodes[7], 2, 0, 6);
		testNode(nodes[8], 3, 1, 6);
	}
}
