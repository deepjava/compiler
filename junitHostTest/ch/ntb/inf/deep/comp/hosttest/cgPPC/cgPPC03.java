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

import ch.ntb.inf.deep.cg.ppc.RegAllocatorPPC;
import ch.ntb.inf.deep.classItems.CFR;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.strings.HString;

public class cgPPC03 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		readConfig();
		HString[] rootClassNames = new HString[] { HString.getHString("ch/ntb/inf/deep/comp/hosttest/testClasses/T03Switch") };
		CFR.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemClasses(), attributes);
		if (Class.nofRootClasses > 0) {
			createNodes(Class.rootClasses[0]);
		}
	}

	@Test
	public void switchNear1() {
		getCode("switchNear1");
		for (int i = 0; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
//	@Ignore
	@Test
	public void switchNear2() {
		getCode("switchNear2");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 21, vol, false));
		for (int i = 3; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
//	@Ignore
	@Test
	public void switchNear3() {
		getCode("switchNear3");
		assertNull("wrong join", getJoin(0));
		for (int i = 0; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
//	@Ignore
	@Test
	public void switchFar1() {
		getCode("switchFar1");
		for (int i = 0; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

//	@Ignore
	@Test
	public void switchFar2() {
		getCode("switchFar2");
		assertNull("wrong join", getJoin(0));
		assertTrue("wrong join", checkJoin(getJoin(1), 0, 20, vol, false));
		for (int i = 2; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

	@Test
	public void switchWhile() {
		getCode("switchWhile");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertTrue("wrong join", checkJoin(getJoin(2), 0, 11, vol, false));
		for (int i = 3; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

}
