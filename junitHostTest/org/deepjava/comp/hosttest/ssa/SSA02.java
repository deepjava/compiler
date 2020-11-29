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

public class SSA02 extends TestSSA {
	
	@BeforeClass
	public static void setUp() {
		readConfig();
		HString[] rootClassNames = new HString[] { HString.getHString("org/deepjava/comp/hosttest/testClasses/T02Branches") };
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
	public void testIf1(){
		SSANode[] nodes = getAndTestSSA("if1", 4, 0);
		testNode(nodes[0], 2, 0, 4);//1 Instruction because load parameter for first use
		testNode(nodes[1], 3, 0, 4);
		testNode(nodes[2], 2, 0, 4);
		testNode(nodes[3], 1, 1, 4);
	}
	
	@Test
	public void testIf2(){
		SSANode[] nodes = getAndTestSSA("if2", 4, 0);
		testNode(nodes[0], 3, 0, 4);
		testNode(nodes[1], 2, 0, 4);
		testNode(nodes[2], 1, 0, 4);
		testNode(nodes[3], 6, 1, 4);
	}

	@Test
	public void testIf3(){
		SSANode[] nodes = getAndTestSSA("if3", 6, 0);
		testNode(nodes[0], 3, 0, 7);
		testNode(nodes[1], 2, 0, 7);
		testNode(nodes[2], 4, 0, 7);
		testNode(nodes[3], 4, 0, 7);
		testNode(nodes[4], 5, 0, 7);
		testNode(nodes[5], 1, 2, 7);
	}
	
	@Test
	public void testIf4(){
		SSANode[] nodes = getAndTestSSA("if4", 4, 0);
		testNode(nodes[0], 4, 0, 5);
		testNode(nodes[1], 2, 0, 5);
		testNode(nodes[2], 1, 0, 5);
		testNode(nodes[3], 4, 1, 5);
	}
	
	@Test
	public void testIf5(){
		SSANode[] nodes = getAndTestSSA("if5", 4, 0);
		testNode(nodes[0], 2, 0, 5);
		testNode(nodes[1], 2, 0, 5);
		testNode(nodes[2], 1, 0, 5);
		testNode(nodes[3], 2, 2, 5);
	}
	
	@Test
	public void testIf6(){
		SSANode[] nodes = getAndTestSSA("if6", 14, 0);
		testNode(nodes[0], 4, 0, 9);
		testNode(nodes[1], 2, 0, 9);
		testNode(nodes[2], 1, 0, 9);
		testNode(nodes[3], 1, 1, 9);
		testNode(nodes[4], 2, 0, 9);
		testNode(nodes[5], 3, 0, 9);
		testNode(nodes[6], 3, 0, 9);
		testNode(nodes[7], 2, 0, 9);
		testNode(nodes[8], 3, 1, 9);
		testNode(nodes[9], 1, 0, 9);
		testNode(nodes[10], 3, 2, 9);
		testNode(nodes[11], 3, 0, 9);
		testNode(nodes[12], 2, 0, 9);
		testNode(nodes[13], 1, 2, 9);
	}
	
	
}
