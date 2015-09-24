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

public class cgPPC09 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		readConfig();
		HString[] rootClassNames = new HString[] { HString.getHString("ch/ntb/inf/deep/comp/hosttest/testClasses/T09Types") };
		CFR.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemClasses(), attributes);
		if (Class.nofRootClasses > 0) {
			createNodes(Class.rootClasses[0]);
		}
	}

	@Test
	public void m1() {
		getCode("m1");
		assertTrue("wrong join", checkJoin(getJoin(0), 14, 18, vol, false));
		for (int i = 1; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
	@Test
	public void m2() {
		getCode("m2");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
	@Test
	public void callm2() {
		getCode("callm2");
		for (int i = 0; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
	@Test
	public void m3() {
		getCode("m3");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertNull("wrong join", getJoin(3));
		assertTrue("wrong join", checkJoin(getJoin(4), 0, 6, vol, false));
		for (int i = 5; i < RegAllocator.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
}
