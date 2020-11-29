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

public class cgPPC07 extends TestCgPPC {

	@BeforeClass
	public static void setUp() {
		readConfig();
		HString[] rootClassNames = new HString[] { HString.getHString("org/deepjava/comp/hosttest/testClasses/T07Arrays") };
		CFR.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemClasses(), attributes);
		if (Class.nofRootClasses > 0) {
			createNodes(Class.rootClasses[0]);
		}
	}

//	@Ignore
	@Test
	public void emptyIntArray() {
		getCode("emptyIntArray");
		for (int i = 0; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
	
//	@Ignore
	@Test
	public void intArray() {
		getCode("intArray");
		assertNull("wrong join", getJoin(0));
		assertNull("wrong join", getJoin(1));
		assertNull("wrong join", getJoin(2));
		assertNull("wrong join", getJoin(3));
		assertTrue("wrong join", checkJoin(getJoin(4), 0, 13, nonVol, false));
		assertTrue("wrong join", checkJoin(getJoin(5), 1, 15, nonVol, false));
		assertTrue("wrong join", checkJoin(getJoin(6), 2, 13, vol, false));
		for (int i = 7; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

//	@Ignore
	@Test
	public void clinit() {
		getCode("<clinit>");
		for (int i = 0; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}

//	@Ignore
	@Test
	public void init() {
		getCode("<init>");
		for (int i = 0; i < RegAllocatorPPC.maxNofJoins; i++)
			assertNull("wrong join", getJoin(i));
	}
}
