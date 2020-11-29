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

public class SSA08 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		readConfig();
		HString[] rootClassNames = new HString[] { HString.getHString("org/deepjava/comp/hosttest/testClasses/T08Calls") };
		CFR.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemClasses(), attributes);
		if(Class.nofRootClasses > 0){
			createSSA(Class.rootClasses[0]);
		}
	}
	
	@Test
	public void testClassConstructor(){
		SSANode[] nodes = getAndTestSSA("<clinit>", 1, 0);
		testNode(nodes[0], 5, 0, 1);
	}

	@Test
	public void testConstructor() {
		SSANode[] nodes = getAndTestSSA("<init>", 1, 0);
		testNode(nodes[0], 5, 0, 3);
	}
	
	@Test
	public void testClassMethCall(){
		SSANode[] nodes = getAndTestSSA("classMethCall", 1, 0);
		testNode(nodes[0], 10, 0, 4);
	}
	
	@Test
	public void testObjectMethCall(){
		SSANode[] nodes = getAndTestSSA("objectMethCall", 1, 0);
		testNode(nodes[0], 5, 0, 3);
	}
	
	@Test
	public void testCallToAnotherClass(){
		SSANode[] nodes = getAndTestSSA("callToAnotherClass", 1, 0);
		testNode(nodes[0], 2, 0, 0);
	}

}
