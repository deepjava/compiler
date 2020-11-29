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

package org.deepjava.comp.hosttest.ssa;

import org.deepjava.classItems.CFR;
import org.deepjava.classItems.Class;
import org.deepjava.config.Configuration;
import org.deepjava.ssa.SSANode;
import org.deepjava.strings.HString;
import org.junit.BeforeClass;
import org.junit.Test;

public class SSA03 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		readConfig();
		HString[] rootClassNames = new HString[] { HString.getHString("org/deepjava/comp/hosttest/testClasses/T03Switch") };
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
	
//	@Ignore
	@Test
	public void testSwitchNear1(){
		SSANode[] nodes = getAndTestSSA("switchNear1", 5, 0);
		testNode(nodes[0], 2, 0, 2);
		testNode(nodes[1], 2, 0, 2);
		testNode(nodes[2], 2, 0, 2);
		testNode(nodes[3], 2, 0, 2);
		testNode(nodes[4], 2, 0, 2);
	}
	
//	@Ignore
	@Test
	public void testSwitchNear2(){
		SSANode[] nodes = getAndTestSSA("switchNear2", 9, 0);
		testNode(nodes[0], 2, 0, 3);
		testNode(nodes[1], 2, 0, 3);
		testNode(nodes[2], 2, 0, 3);
		testNode(nodes[3], 3, 0, 3);
		testNode(nodes[4], 2, 0, 3);
		testNode(nodes[5], 2, 0, 3);
		testNode(nodes[6], 3, 1, 3);
		testNode(nodes[7], 2, 0, 3);
		testNode(nodes[8], 3, 1, 3);
	}
	
//	@Ignore
	@Test
	public void testSwitchNear3(){
		SSANode[] nodes = getAndTestSSA("switchNear3", 4, 0);
		testNode(nodes[0], 4, 0, 6);
		testNode(nodes[1], 2, 0, 6);
		testNode(nodes[2], 1, 0, 6);
		testNode(nodes[3], 4, 0, 6);
	}
	
//	@Ignore
	@Test
	public void testSwitchFar1(){
		SSANode[] nodes = getAndTestSSA("switchFar1", 5, 0);
		testNode(nodes[0], 2, 0, 2);
		testNode(nodes[1], 2, 0, 2);
		testNode(nodes[2], 2, 0, 2);
		testNode(nodes[3], 2, 0, 2);
		testNode(nodes[4], 2, 0, 2);		
	}
	
//	@Ignore
	@Test
	public void testSwitchFar2(){
		SSANode[] nodes = getAndTestSSA("switchFar2", 12, 0);
		testNode(nodes[0], 2, 0, 2);
		testNode(nodes[1], 2, 0, 2);
		testNode(nodes[2], 1, 0, 2);
		testNode(nodes[3], 2, 0, 2);
		testNode(nodes[4], 2, 0, 2);
		testNode(nodes[5], 1, 0, 2);
		testNode(nodes[6], 2, 0, 2);
		testNode(nodes[7], 1, 1, 2);
		testNode(nodes[8], 2, 0, 2);
		testNode(nodes[9], 1, 0, 2);
		testNode(nodes[10], 2, 0, 2);
		testNode(nodes[11], 1, 1, 2);
	}

	@Test
	public void testSwitchWhile(){
		SSANode[] nodes = getAndTestSSA("switchWhile", 7, 1);
		testNode(nodes[0], 2, 0, 3);
		testNode(nodes[1], 2, 0, 3);
		testNode(nodes[2], 2, 0, 3);
		testNode(nodes[3], 2, 0, 3);
		testNode(nodes[4], 1, 0, 3);
		testNode(nodes[5], 1, 0, 3);
		testNode(nodes[6], 1, 1, 3);
	}
}
