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

package ch.ntb.inf.deep.comp.hosttest.cfg;

import org.junit.BeforeClass;
import org.junit.Test;
import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.classItems.CFR;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.strings.HString;

/**
 * - create and test CFG<br>
 */
public class CFG02 extends TestCFG implements ICclassFileConsts {

	@BeforeClass
	public static void setUp() {
		readConfig();
		HString[] rootClassNames = new HString[] { HString.getHString("ch/ntb/inf/deep/comp/hosttest/testClasses/T02Branches") };
		CFR.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemClasses(), attributes);
		if (Class.nofRootClasses > 0) {
			createCFG(Class.rootClasses[0]);
		}
	}

    @Test
    public void if1() {
        CFGNode[] nodes = getAndTestNodes("if1", 4);
		testNode(nodes[0], 0, 1, false, null, new int[] {}, new int[] { 4, 11 });
		testNode(nodes[1], 4, 8, false, nodes[0], new int[] { 0 }, new int[] { 15 });
		testNode(nodes[2], 11, 14, false, nodes[0], new int[] { 0 }, new int[] { 15 });
		testNode(nodes[3], 15, 16, false, nodes[0], new int[] { 4, 11 }, new int[] {});
    }
}
