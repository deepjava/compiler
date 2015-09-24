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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.ntb.inf.deep.cg.ppc.RegAllocator;
import ch.ntb.inf.deep.classItems.CFR;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.strings.HString;

public class cgPPC01 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		readConfig();
		HString[] rootClassNames = new HString[] { HString.getHString("ch/ntb/inf/deep/comp/hosttest/testClasses/T01SimpleMethods") };
		CFR.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemClasses(), attributes);
		if (Class.nofRootClasses > 0) {
			createNodes(Class.rootClasses[0]);
		}
	}
	
	@Test
	public void assignment1() {
		getCode("assignment1");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
	//	@Ignore
	@Test 
	public void assignment2() {
		getCode("assignment2");
		assertTrue("wrong join", checkJoin(getJoin(0), 3, 7, vol, false));
		assertNull("wrong join", getJoin(1));
	}
	
	//	@Ignore
	@Test 
	public void assignment3() {
		getCode("assignment3");
		assertTrue("wrong join", checkJoin(getJoin(0), 5, 9, vol, false));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 25, vol, false));
		assertTrue("wrong join", checkJoin(getJoin(3), 16, 23, vol, false));
		assertNull("wrong join", getJoin(4));
	}

	@Test
	public void simple1() {
		getCode("simple1");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
	@Test
	public void simple2() {
		getCode("simple2");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
	@Test
	public void simple3() {
		getCode("simple3");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
	@Test
	public void simple4() {
		getCode("simple4");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

//	@Ignore
	@Test
	public void simple5() {
		getCode("simple5");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

//	@Ignore
	@Test
	public void simple6() {
		getCode("simple6");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
}
