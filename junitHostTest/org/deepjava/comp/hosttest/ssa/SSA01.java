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

public class SSA01 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		readConfig();
		HString[] rootClassNames = new HString[] { HString.getHString("org/deepjava/comp/hosttest/testClasses/T01SimpleMethods") };
		CFR.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemClasses(), attributes);
		if(Class.nofRootClasses > 0){
			createSSA(Class.rootClasses[0]);
		}
	}

 	@Test
	public void emptyMethodStatic() {
		SSANode[] nodes = getAndTestSSA("emptyMethodStatic", 1, 0);
		testNode(nodes[0],1,0,0);
 	}
	
	@Test
	public void emptyMethod() {
		SSANode[] nodes = getAndTestSSA("emptyMethod", 1, 0);
		testNode(nodes[0],1,0,1);
 	}

	@Test
	public void assignment1() {
		SSANode[] nodes = getAndTestSSA("assignment1", 1,0);
		testNode(nodes[0],2,0,2);
 	}
	
	@Test
	public void simple1() {
		SSANode[] nodes = getAndTestSSA("simple1", 1,0);
		testNode(nodes[0],11,0,4);
 	}
	
	@Test
	public void simple2() {
		SSANode[] nodes = getAndTestSSA("simple2", 1,0);
		testNode(nodes[0],8,0,4);
 	}
	
	@Test
	public void simple3() {
		SSANode[] nodes = getAndTestSSA("simple3", 1,0);
		testNode(nodes[0],5,0,4);
 	}

	@Test
	public void simple4() {
		SSANode[] nodes = getAndTestSSA("simple4", 1,0);
		testNode(nodes[0],19,0,2);
	}

	@Test
	public void simple5() {
		SSANode[] nodes = getAndTestSSA("simple5", 1,0);
		testNode(nodes[0],5,0,4);
	}

	@Test
	public void simple6() {
		SSANode[] nodes = getAndTestSSA("simple6", 1,0);
		testNode(nodes[0],7,0,7);
	}

}
