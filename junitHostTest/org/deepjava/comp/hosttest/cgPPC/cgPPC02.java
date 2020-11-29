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

package org.deepjava.comp.hosttest.cgPPC;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.deepjava.cg.Code32;
import org.deepjava.cg.ppc.RegAllocatorPPC;
import org.deepjava.classItems.CFR;
import org.deepjava.classItems.Class;
import org.deepjava.config.Configuration;
import org.deepjava.strings.HString;
import org.junit.BeforeClass;
import org.junit.Test;

public class cgPPC02 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		readConfig();
		HString[] rootClassNames = new HString[] { HString.getHString("org/deepjava/comp/hosttest/testClasses/T02Branches") };
		CFR.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemClasses(), attributes);
		if (Class.nofRootClasses > 0) {
			createNodes(Class.rootClasses[0]);
		}
	}

	@Test
	public void if1() {
		Code32 code = getCode("if1");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 3, 8, vol, false));
		for (int i = 4; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 7, false, false));
	}
	
	@Test
	public void if2() {
		Code32 code = getCode("if2");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 3, 9, vol, false));
		for (int i = 3; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 6, false, false));
	}
	
	@Test
	public void if3() {
		Code32 code = getCode("if3");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 18, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 1, 19, vol, false));
		for (int i = 4; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 18, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 19, false, false));
	}
	
	@Test
	public void if4() {
		Code32 code = getCode("if4");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertNull("wrong join", getJoin(3));
		assertTrue("wrong join", checkJoin(getJoin(4), 4, 8, vol, false));
		for (int i = 5; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 7, false, false));
	}
	
	@Test
	public void if5() {
		Code32 code = getCode("if5");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 7, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(4), 0, 7, vol, false));
		for (int i = 5; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 5, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 6, false, false));
	}
	
	@Test
	public void if6() {
		Code32 code = getCode("if6");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertNull("wrong join", getJoin(3));
		assertNull("wrong join", getJoin(4));
		assertTrue("wrong join", checkJoin(getJoin(5), 0, 34, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(7), 0, 36, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(8), 1, 25, vol, false));
		for (int i = 9; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 7, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 19, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 24, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 25, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 34, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 35, false, false));
	}
	
//@Ignore
	@Test
	public void if7() {
		Code32 code = getCode("if7");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertTrue("wrong join", checkJoin(getJoin(3), 0, 25, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(4), 8, 24, vol, false));
		for (int i = 5; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 6, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 17, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 20, true, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 21, false, false));
		assertTrue("wrong phi function", checkPhiFunction(code.ssa, 22, false, false));
	}
}
