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

package ch.ntb.inf.deep.comp.hosttest.ssa;

import org.junit.BeforeClass;
import org.junit.Test;
import ch.ntb.inf.deep.classItems.CFR;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.ssa.SSANode;
import ch.ntb.inf.deep.strings.HString;

public class SSA09 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		readConfig();
		HString[] rootClassNames = new HString[] { HString.getHString("ch/ntb/inf/deep/comp/hosttest/testClasses/T09Types") };
		CFR.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemClasses(), attributes);
		if(Class.nofRootClasses > 0){
			createSSA(Class.rootClasses[0]);
		}
	}

	@Test
	public void testConstructor() {
		SSANode[] nodes = getAndTestSSA("<init>", 1, 0);
		testNode(nodes[0], 3, 0, 2);
	}
	
	@Test
	public void testM1(){
		SSANode[] nodes = getAndTestSSA("m1", 7, 0);
		testNode(nodes[0], 4, 0, 11);
		testNode(nodes[1], 3, 0, 11);
		testNode(nodes[2], 1, 0, 11);
		testNode(nodes[3], 6, 0, 11);
		testNode(nodes[4], 2, 0, 11);
		testNode(nodes[5], 1, 0, 11);
		testNode(nodes[6], 6, 1, 11);
	}
	
	@Test
	public void m2(){
		SSANode[] nodes = getAndTestSSA("m2", 1, 0);
		testNode(nodes[0], 23, 0, 16);
		
	}
	
	@Test
	public void callm2(){
		SSANode[] nodes = getAndTestSSA("callm2", 1, 0);
		testNode(nodes[0], 11, 0, 21);
	}
	
	@Test
	public void m3(){
		SSANode[] nodes = getAndTestSSA("m3", 4, 0);
		testNode(nodes[0], 2, 0, 7);
		testNode(nodes[1], 3, 0, 7);
		testNode(nodes[2], 1, 0, 7);
		testNode(nodes[3], 1, 1, 7);
	}
	
	}
