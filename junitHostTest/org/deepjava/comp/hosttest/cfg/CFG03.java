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

package org.deepjava.comp.hosttest.cfg;

import org.deepjava.cfg.CFGNode;
import org.deepjava.classItems.CFR;
import org.deepjava.classItems.Class;
import org.deepjava.classItems.ICclassFileConsts;
import org.deepjava.config.Configuration;
import org.deepjava.strings.HString;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * - create and test CFG<br>
 */
public class CFG03 extends TestCFG implements ICclassFileConsts {

	@BeforeClass
	public static void setUp() {
		readConfig();
		HString[] rootClassNames = new HString[] { HString.getHString("org/deepjava/comp/hosttest/testClasses/T03Switch") };
		CFR.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemClasses(), attributes);
		if (Class.nofRootClasses > 0) {
			createCFG(Class.rootClasses[0]);
		}
	}

	@Test
	public void switchNear1() {
		CFGNode[] nodes = getAndTestNodes("switchNear1", 5);
		testNode(nodes[0], 0, 1, false, null, new int[] {}, new int[] { 28,30,32,34 });
		testNode(nodes[1], 28, 29, false, nodes[0], new int[] { 0 }, new int[] { });
		testNode(nodes[2], 30, 31, false, nodes[0], new int[] { 0 }, new int[] { });
		testNode(nodes[3], 32, 33, false, nodes[0], new int[] { 0 }, new int[] { });
		testNode(nodes[4], 34, 35, false, nodes[0], new int[] { 0 }, new int[] { });
	}

	@Test
	public void switchNear2() {
		CFGNode[] nodes = getAndTestNodes("switchNear2", 9);
		testNode(nodes[0], 0, 1, false, null, new int[] {}, new int[] {40,42,44,50,52,55,61});
		testNode(nodes[1], 40, 41, false, nodes[0], new int[] {0}, new int[] {});
		testNode(nodes[2], 42, 43, false, nodes[0], new int[] {0}, new int[] {});
		testNode(nodes[3], 44, 47, false, nodes[0], new int[] {0}, new int[] {63});
		testNode(nodes[4], 50, 51, false, nodes[0], new int[] {0}, new int[] {});
		testNode(nodes[5], 52, 52, false, nodes[0], new int[] {0}, new int[] {55});
		testNode(nodes[6], 55, 58, false, nodes[0], new int[] {0,52}, new int[] {63});
		testNode(nodes[7], 61, 62, false, nodes[0], new int[] {0}, new int[] {});
		testNode(nodes[8], 63, 66, false, nodes[0], new int[] {44,55}, new int[] {});
	}

	@Test
	public void switchFar1() {
		CFGNode[] nodes = getAndTestNodes("switchFar1", 5);
		testNode(nodes[0], 0, 1, false, null, new int[] {}, new int[] {36,39,41,44});
		testNode(nodes[1], 36, 38, false, nodes[0], new int[] {0}, new int[] {});
		testNode(nodes[2], 39, 40, false, nodes[0], new int[] {0}, new int[] {});
		testNode(nodes[3], 41, 43, false, nodes[0], new int[] {0}, new int[] {});
		testNode(nodes[4], 44, 45, false, nodes[0], new int[] {0}, new int[] {});
	}

	@Test
	public void switchFar2() {
		CFGNode[] nodes = getAndTestNodes("switchFar2", 12);
		testNode(nodes[0], 0, 1, false, null, new int[] {}, new int[] {36,39,89,139});
		testNode(nodes[1], 36, 38, false, nodes[0], new int[] {0}, new int[] {});
		testNode(nodes[2], 39, 40, false, nodes[0], new int[] {0}, new int[] {89,79,81,89,87,89});
		testNode(nodes[3], 79, 80, false, nodes[2], new int[] {39}, new int[] {});
		testNode(nodes[4], 81, 81, false, nodes[2], new int[] {39}, new int[] {84});
		testNode(nodes[5], 84, 84, false, nodes[4], new int[] {81}, new int[] {89});
		testNode(nodes[6], 87, 88, false, nodes[2], new int[] {39}, new int[] {});
		testNode(nodes[7], 89, 90, false, nodes[0], new int[] {0,39,84}, new int[] {139,137,131,137,137,139,137});
		testNode(nodes[8], 131, 131, false, nodes[7], new int[] {89}, new int[] {134});
		testNode(nodes[9], 134, 134, false, nodes[8], new int[] {131}, new int[] {139});
		testNode(nodes[10], 137, 138, false, nodes[7], new int[] {89}, new int[] {});
		testNode(nodes[11], 139, 140, false, nodes[0], new int[] {0,89,134}, new int[] {});
	}

	@Test
	public void testSwitchWhile(){
		CFGNode[] nodes = getAndTestNodes("switchWhile", 6);
		testNode(nodes[0], 0, 1, false, null, new int[] {}, new int[] {20,38});
		testNode(nodes[1], 20, 20, false, nodes[0], new int[] {0}, new int[] {28});
		testNode(nodes[2], 23, 25, false, nodes[3], new int[] {28}, new int[] {28});
		testNode(nodes[3], 28, 32, true, nodes[1], new int[] {20,23}, new int[] {23,35});
		testNode(nodes[4], 35, 35, false, nodes[3], new int[] {28}, new int[] {38});
		testNode(nodes[5], 38, 38, false, nodes[0], new int[] {0,35}, new int[] {});
	}

}
