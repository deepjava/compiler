/*
 * Copyright 2011 - 2013 NTB University of Applied Sciences in Technology
 * Buchs, Switzerland, http://www.ntb.ch/inf
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package ch.ntb.inf.deep.comp.hosttest.cgPPC;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.cg.ppc.CodeGenPPC;
import ch.ntb.inf.deep.cg.ppc.RegAllocator;
import ch.ntb.inf.deep.classItems.CFR;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.strings.HString;

public class cgPPC04 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		readConfig();
		HString[] rootClassNames = new HString[] { HString.getHString("ch/ntb/inf/deep/comp/hosttest/testClasses/T04Loops") };
		CFR.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemClasses(), attributes);
		if (Class.nofRootClasses > 0) {
			createCgPPC(Class.rootClasses[0]);
		}
	}

	@Test 
	public void doWhile1() {
		CodeGenPPC code = getCode("doWhile1");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 8, vol, false));
		for (int i = 3; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 1, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 2, true, false));
	}
	
//	@Ignore
	@Test
	public void doWhileIf1() {
		CodeGenPPC code = getCode("doWhileIf1");
		assertTrue("wrong join", checkJoin(getJoin(0), 11, 15, vol, true));
		assertTrue("wrong join", checkJoin(getJoin(0).next, 18, 22, vol, false));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 24, vol, true));
		assertTrue("wrong join", checkJoin(getJoin(2).next, 26, 29, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 1, 24, vol, false));
		for (int i = 4; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 2, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 3, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 4, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 5, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 14, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 21, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 29, false, false));
	}

//	@Ignore
	@Test
	public void while1() {
		CodeGenPPC code = getCode("while1");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 7, vol, false));
		for (int i = 3; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 4, false, false));
	}

//	@Ignore
	@Test
	public void whileTrue() {
		CodeGenPPC code = getCode("whileTrue");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 5, vol, false));
		for (int i = 3; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 1, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 2, true, false));
	}

//	@Ignore
	@Test
	public void whileTrueBreak() {
		getCode("whileTrueBreak");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

//	@Ignore
	@Test
	public void whileMultiCond() {
		CodeGenPPC code = getCode("whileMultiCond");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 8, vol, false));
		for (int i = 3; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 4, false, false));
	}

//	@Ignore
	@Test
	public void for1() {
		CodeGenPPC code = getCode("for1");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 10, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 1, 10, vol, false));
		for (int i = 4; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 7, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 8, false, false));
	}

//	@Ignore
	@Test
	public void forWhile() {
		CodeGenPPC code = getCode("forWhile");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 17, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(4), 0, 16, vol, false));
		for (int i = 5; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 6, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 7, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 8, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 13, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 14, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 15, false, false));
	}

//	@Ignore
	@Test
	public void forIfWhile() {
		CodeGenPPC code = getCode("forIfWhile");
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 8, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 9, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 12, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 13, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 16, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 17, false, false));
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
		getCode("whileTrue2");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 4, 11, vol, false));
		assertNull("wrong join", getJoin(4));
	}

	//	@Ignore
	@Test
	public void forIfFor() {
		CodeGenPPC code = getCode("forIfFor");
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 12, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 13, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 14, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 15, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 16, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 32, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 33, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 34, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 35, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 36, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 45, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 46, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 47, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 48, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 49, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 52, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 53, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 54, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 55, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 58, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 59, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 60, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 61, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 62, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 65, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 66, false, false));
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 67, vol, false));	// offset
		assertTrue("wrong join", checkJoin(getJoin(3), 26, 38, vol, true));	// k
		assertTrue("wrong join", checkJoin(getJoin(3).next, 39, 51, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(4), 1, 66, vol, false));	// val
		assertTrue("wrong join", checkJoin(getJoin(5), 6, 18, vol, true));	// i
		assertTrue("wrong join", checkJoin(getJoin(5).next, 19, 64, vol, false));	// i
												// index 6, valid, keine joins 
		for (int i = 6; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

	//	@Ignore
	@Test
	public void phiFunctionTest1() {
		CodeGenPPC code = getCode("phiFunctionTest1");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 0, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 1, true, false));
	}
	
	//	@Ignore
	@Test
	public void phiFunctionTest2() {
		CodeGenPPC code = getCode("phiFunctionTest2");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 1, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 2, true, false));
	}
	
	//	@Ignore
	@Test
	public void phiFunctionTest3() {
		CodeGenPPC code = getCode("phiFunctionTest3");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 7, vol, false));
		for (int i = 3; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 1, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 2, true, false));
	}
	
	//	@Ignore
	@Test
	public void phiFunctionTest4() {
		CodeGenPPC code = getCode("phiFunctionTest4");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 5, vol, false));
		for (int i = 3; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 1, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 2, true, false));
	}
	
	//	@Ignore
	@Test
	public void phiFunctionTest5() {
		CodeGenPPC code = getCode("phiFunctionTest5");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 6, vol, false));
		for (int i = 3; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 1, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 2, true, false));
	}

	//	@Ignore
	@Test
	public void phiFunctionTest6() {
		CodeGenPPC code = getCode("phiFunctionTest6");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 8, 18, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 7, vol, true));
		assertTrue("wrong join", checkJoin(getJoin(3).next, 9, 16, vol, false));
		for (int i = 4; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 4, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 5, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 13, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 14, false, false));
	}

	//	@Ignore
	@Test
	public void phiFunctionTest7() {
		CodeGenPPC code = getCode("phiFunctionTest7");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 18, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 1, 8, vol, true));
		assertTrue("wrong join", checkJoin(getJoin(3).next, 9, 16, vol, false));
		for (int i = 4; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 5, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 6, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 13, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 14, false, false));
	}

	//	@Ignore
	@Test
	public void phiFunctionTest8() {
		CodeGenPPC code = getCode("phiFunctionTest8");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 22, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 1, 9, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(4), 12, 20, vol, false));
		for (int i = 5; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 5, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 6, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 7, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 16, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 17, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 18, false, false));
	}

	//	@Ignore
	@Test
	public void phiFunctionTest9() {
		CodeGenPPC code = getCode("phiFunctionTest9");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 23, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(4), 1, 10, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(5), 13, 21, vol, false));
		for (int i = 6; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 5, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 6, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 7, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 17, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 18, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 19, false, false));
	}


	//	@Ignore
	@Test 
	public void phiFunctionTest10() {
		CodeGenPPC code = getCode("phiFunctionTest10");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 22, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 10, 22, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(4), 1, 9, vol, true));
		assertTrue("wrong join", checkJoin(getJoin(4).next, 11, 21, vol, false));
		for (int i = 5; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 5, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 6, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 7, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 17, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 18, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 19, false, false));
	}
	
	//	@Ignore
	@Test 
	public void phiFunctionTest11() {
		CodeGenPPC code = getCode("phiFunctionTest11");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 7, vol, false));
		for (int i = 3; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 0, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 1, true, false));
	}

	//	@Ignore
	@Test 
	public void phiFunctionTest12() {
		CodeGenPPC code = getCode("phiFunctionTest12");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 5, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 5, vol, false));
		for (int i = 4; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 2, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 3, false, false));
	}

	//	@Ignore
	@Test 
	public void phiFunctionTest13() {
		CodeGenPPC code = getCode("phiFunctionTest13");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 11, 23, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 2, 11, vol, true));
		assertTrue("wrong join", checkJoin(getJoin(3).next, 13, 21, vol, false));
		for (int i = 4; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 6, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 7, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 17, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 18, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 22, false, false));
	}

	//	@Ignore
	@Test 
	public void phiFunctionTest14() {
		CodeGenPPC code = getCode("phiFunctionTest14");
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 3, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 4, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 5, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 6, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 7, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 8, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 25, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 26, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 27, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 28, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 29, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 30, true, false));
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 31, vol, false));	// a
		assertTrue("wrong join", checkJoin(getJoin(4), 0, 14, vol, true));	// b
		assertTrue("wrong join", checkJoin(getJoin(4).next, 14, 33, vol, false));
		for (int i = 5; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

	//	@Ignore
	@Test 
	public void phiFunctionTest15() {
		CodeGenPPC code = getCode("phiFunctionTest15");
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 2, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 3, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 4, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 5, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 6, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 7, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 21, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 22, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 23, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 24, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 25, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 26, true, false));
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 27, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(4), 12, 29, vol, false));
		for (int i = 5; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

	//	@Ignore
	@Test 
	public void phiFunctionTest16() {
		CodeGenPPC code = getCode("phiFunctionTest16");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 26, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(4), 11, 21, vol, false));
		for (int i = 5; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 5, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 6, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 7, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 8, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 15, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 16, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 17, true, true));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 18, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 24, false, false));
	}

	//	@Ignore
	@Test 
	public void phiFunctionTest17() {
		getCode("phiFunctionTest17");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 121, nonVol, false));	// ref	
		assertNull("wrong join", getJoin(4));	// nofDim					
		assertTrue("wrong join", checkJoin(getJoin(5), 0, 116, nonVol, false));	// dim0	
		assertTrue("wrong join", checkJoin(getJoin(6), 0, 117, nonVol, false));	// dim1
		assertTrue("wrong join", checkJoin(getJoin(7), 0, 118, nonVol, false));	// dim2
		assertTrue("wrong join", checkJoin(getJoin(8), 0, 119, nonVol, false));	// dim3
		assertTrue("wrong join", checkJoin(getJoin(9), 0, 120, nonVol, false));	// dim4
		assertNull("wrong join", getJoin(10));	// elemSize
		assertTrue("wrong join", checkJoin(getJoin(11), 20, 98, nonVol, false));	// dim1Size
		assertTrue("wrong join", checkJoin(getJoin(12), 27, 101, nonVol, false));	// size
		assertTrue("wrong join", checkJoin(getJoin(13), 28, 52, nonVol, true));	// addr
		assertTrue("wrong join", checkJoin(getJoin(13).next, 64, 98, nonVol, false));	// addr
		assertTrue("wrong join", checkJoin(getJoin(14), 65, 98, nonVol, false));	// i
		assertNull("wrong join", getJoin(16));	// elemAddr
		for (int i = 17; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
	//	@Ignore
	@Test 
	public void phiFunctionTest18() {
		getCode("phiFunctionTest18");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertNull("wrong join", getJoin(3));
		assertNull("wrong join", getJoin(4));					
		assertTrue("wrong join", checkJoin(getJoin(5), 0, 74, nonVol, false));	// code
		assertTrue("wrong join", checkJoin(getJoin(6), 0, 24, nonVol, false));	// message
		assertTrue("wrong join", checkJoin(getJoin(7), 0, 53, nonVol, false));	// expected
		assertTrue("wrong join", checkJoin(getJoin(8), 0, 64, nonVol, false));	// actual
		assertTrue("wrong join", checkJoin(getJoin(9), 0, 38, nonVol, true));	// len
		assertTrue("wrong join", checkJoin(getJoin(9).next, 38, 109, vol, false));	// len
		assertTrue("wrong join", checkJoin(getJoin(10), 79, 105, vol, false));	// checkByte
		assertTrue("wrong join", checkJoin(getJoin(11), 1, 101, nonVol, false));	// m
		assertTrue("wrong join", checkJoin(getJoin(12), 6, 24, nonVol, true));	// i
		assertTrue("wrong join", checkJoin(getJoin(12).next, 80, 101, vol, false));	// i
		for (int i = 13; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

	//	@Ignore
	@Test 
	public void phiFunctionTest19() {
		CodeGenPPC code = getCode("phiFunctionTest19");
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 10, true, true));	// a
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 11, true, true));	// i
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 12, true, true));	// b
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 13, false, false));	// k
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 18, true, true));	// a
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 19, false, false));	// i
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 20, true, false));	// b
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 21, true, false));	// k
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 28, true, false));	// a
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 29, false, false));	// n
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 30, true, false));	// b
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 31, true, false));	// k
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 1, 23, nonVol, false));	// a
		assertTrue("wrong join", checkJoin(getJoin(4), 2, 23, vol, true));	// i
		assertTrue("wrong join", checkJoin(getJoin(4).next, 24, 33, vol, false));	// n
		assertTrue("wrong join", checkJoin(getJoin(5), 4, 15, vol, false));	// b
		assertTrue("wrong join", checkJoin(getJoin(6), 5, 15, vol, false));	// k
		for (int i = 7; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
}
