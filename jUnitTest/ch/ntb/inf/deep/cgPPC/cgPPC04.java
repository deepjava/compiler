package ch.ntb.inf.deep.cgPPC;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.config.Configuration;

public class cgPPC04 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		String workspace =System.getProperty("user.dir")+ "/bin";
		String[] rootClassNames = new String[] { "ch/ntb/inf/deep/testClasses/T04Loops" };
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
		if (Type.nofRootClasses > 0) {
			createCgPPC(Type.rootClasses[0]);
		}
	}

	//	@Ignore
	@Test 
	public void doWhile1() {
		createCgPPC1(Type.rootClasses[0],"doWhile1");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 8, vol, false));
		assertNull("wrong join", getJoin(3));
	}
	
//	@Ignore
	@Test
	public void doWhileIf1() {
		createCgPPC1(Type.rootClasses[0],"doWhileIf1");
		assertTrue("wrong join", checkJoin(getJoin(0), 11, 15, vol, true));
		assertTrue("wrong join", checkJoin(getJoin(0).next, 18, 22, vol, false));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 24, vol, true));
		assertTrue("wrong join", checkJoin(getJoin(2).next, 26, 29, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 1, 24, vol, false));
		for (int i = 4; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

//	@Ignore
	@Test
	public void while1() {
		createCgPPC1(Type.rootClasses[0],"while1");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 7, vol, false));
		for (int i = 3; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

//	@Ignore
	@Test
	public void whileTrue() {
		createCgPPC1(Type.rootClasses[0],"whileTrue");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 5, vol, false));
		for (int i = 3; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

//	@Ignore
	@Test
	public void whileTrueBreak() {
		createCgPPC1(Type.rootClasses[0],"whileTrueBreak");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

//	@Ignore
	@Test
	public void whileMultiCond() {
		createCgPPC1(Type.rootClasses[0],"whileMultiCond");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 8, vol, false));
		for (int i = 3; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

//	@Ignore
	@Test
	public void for1() {
		createCgPPC1(Type.rootClasses[0],"for1");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 10, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 1, 10, vol, false));
		for (int i = 4; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

//	@Ignore
	@Test
	public void forWhile() {
		createCgPPC1(Type.rootClasses[0],"forWhile");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 17, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(4), 0, 16, vol, false));
		for (int i = 5; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

//	@Ignore
	@Test
	public void forIfWhile() {
		createCgPPC1(Type.rootClasses[0],"forIfWhile");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 19, vol, false));
		for (int i = 4; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
	//	@Ignore
	@Test
	public void whileTrue2() {
		createCgPPC1(Type.rootClasses[0],"whileTrue2");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 4, 11, vol, false));
		assertNull("wrong join", getJoin(4));
	}

	//	@Ignore
	@Test
	public void phiFunctionTest1() {
		createCgPPC1(Type.rootClasses[0],"phiFunctionTest1");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
	//	@Ignore
	@Test
	public void phiFunctionTest2() {
		createCgPPC1(Type.rootClasses[0],"phiFunctionTest2");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
	//	@Ignore
	@Test
	public void phiFunctionTest3() {
		createCgPPC1(Type.rootClasses[0],"phiFunctionTest3");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 7, vol, false));
		assertNull("wrong join", getJoin(3));
	}
	
	//	@Ignore
	@Test
	public void phiFunctionTest4() {
		createCgPPC1(Type.rootClasses[0],"phiFunctionTest4");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 5, vol, false));
		assertNull("wrong join", getJoin(3));
	}
	
	//	@Ignore
	@Test
	public void phiFunctionTest5() {
		createCgPPC1(Type.rootClasses[0],"phiFunctionTest5");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 8, 18, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 7, vol, true));
		assertTrue("wrong join", checkJoin(getJoin(3).next, 9, 16, vol, false));
		assertNull("wrong join", getJoin(4));
	}

	//	@Ignore
	@Test
	public void phiFunctionTest6() {
		createCgPPC1(Type.rootClasses[0],"phiFunctionTest6");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 18, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 1, 8, vol, true));
		assertTrue("wrong join", checkJoin(getJoin(3).next, 9, 16, vol, false));
		assertNull("wrong join", getJoin(4));
	}

	//	@Ignore
	@Test
	public void phiFunctionTest7() {
		createCgPPC1(Type.rootClasses[0],"phiFunctionTest7");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 22, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 10, 22, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(4), 1, 9, vol, true));
		assertTrue("wrong join", checkJoin(getJoin(4).next, 11, 21, vol, false));
		assertNull("wrong join", getJoin(5));
	}

	//	@Ignore
	@Test
	public void phiFunctionTest8() {
		createCgPPC1(Type.rootClasses[0],"phiFunctionTest8");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 7, vol, false));
		assertNull("wrong join", getJoin(3));
	}

	//	@Ignore
	@Test
	public void phiFunctionTest9() {
		createCgPPC1(Type.rootClasses[0],"phiFunctionTest9");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 5, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 5, vol, false));
		assertNull("wrong join", getJoin(4));
	}


	//	@Ignore
	@Test 
	public void phiFunctionTest10() {
		createCgPPC1(Type.rootClasses[0],"phiFunctionTest10");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 11, 23, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 2, 11, vol, true));
		assertTrue("wrong join", checkJoin(getJoin(3).next, 13, 21, vol, false));
		assertNull("wrong join", getJoin(4));
	}
	
	//	@Ignore
	@Test 
	public void phiFunctionTest11() {
		createCgPPC1(Type.rootClasses[0],"phiFunctionTest11");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 31, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(4), 0, 33, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(5), 17, 31, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(6), 18, 31, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(7), 22, 31, vol, false));
		assertNull("wrong join", getJoin(8));
	}

	//	@Ignore
	@Test 
	public void phiFunctionTest12() {
		createCgPPC1(Type.rootClasses[0],"phiFunctionTest12");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 31, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(4), 0, 33, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(5), 17, 31, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(6), 18, 31, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(7), 22, 31, vol, false));
		assertNull("wrong join", getJoin(8));
	}

	//	@Ignore
	@Test 
	public void phiFunctionTest13() {
		createCgPPC1(Type.rootClasses[0],"phiFunctionTest13");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 31, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(4), 0, 33, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(5), 17, 31, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(6), 18, 31, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(7), 22, 31, vol, false));
		assertNull("wrong join", getJoin(8));
	}

}
