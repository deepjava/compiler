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

import org.deepjava.cg.ppc.RegAllocatorPPC;
import org.deepjava.classItems.CFR;
import org.deepjava.classItems.Class;
import org.deepjava.config.Configuration;
import org.deepjava.strings.HString;
import org.junit.BeforeClass;
import org.junit.Test;

public class cgPPC06 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		readConfig();
		HString[] rootClassNames = new HString[] { HString.getHString("org/deepjava/comp/hosttest/testClasses/T06Operators") };
		CFR.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemClasses(), attributes);
		if (Class.nofRootClasses > 0) {
			createNodes(Class.rootClasses[0]);
		}
	}

//	@Ignore
	@Test
	public void conditionalOperator1() {
		getCode("conditionalOperator1");
		assertTrue("wrong join", checkJoin(getJoin(0), 5, 9, vol, true));
		assertTrue("wrong join", checkJoin(getJoin(0).next, 13, 22, vol, false));
		for (int i = 1; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
//	@Ignore
	@Test
	public void conditionalOperator2() {
		getCode("conditionalOperator2");
		assertTrue("wrong join", checkJoin(getJoin(0), 9, 13, vol, true));
		assertTrue("wrong join", checkJoin(getJoin(0).next, 17, 26, vol, false));
		for (int i = 1; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
//	@Ignore
	@Test
	public void conditionalOperator3() {
		getCode("conditionalOperator3");
		assertTrue("wrong join", checkJoin(getJoin(0), 5, 9, vol, false));
		for (int i = 1; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

//	@Ignore
	@Test
	public void conditionalOperator4() {
		getCode("conditionalOperator4");
		assertTrue("wrong join", checkJoin(getJoin(0), 4, 8, vol, false));
		for (int i = 1; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

}
