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

public class SSA07 extends TestSSA {

	@BeforeClass
	public static void setUp() {
		readConfig();
		HString[] rootClassNames = new HString[] { HString.getHString("ch/ntb/inf/deep/comp/hosttest/testClasses/T07Arrays") };
		CFR.buildSystem(rootClassNames, Configuration.getSearchPaths(), Configuration.getSystemClasses(), attributes);
		if(Class.nofRootClasses > 0){
			createSSA(Class.rootClasses[0]);
		}
	}

	@Test
	public void testConstructor() {
		SSANode[] nodes = getAndTestSSA("<init>", 1, 0);
		testNode(nodes[0], 10, 0, 4);
	}
	
	@Test
	public void testEmptyIntArray() {
		SSANode[] nodes = getAndTestSSA("emptyIntArray", 1, 0);
		testNode(nodes[0], 3, 0, 2);
	}
	
	@Test
	public void testIntArrayParam() {
		SSANode[] nodes = getAndTestSSA("intArray", 4, 1);
		testNode(nodes[0], 5, 0, 7);
		testNode(nodes[1], 4, 0, 7);
		testNode(nodes[2], 2, 1, 7);
		testNode(nodes[3], 3, 0, 7);
	}
	
	@Test
	public void testStringArray() {
		SSANode[] nodes = getAndTestSSA("stringArray", 1, 0);
		testNode(nodes[0], 20, 0, 5);
	}
	
	@Test
	public void testObjectArray() {
		SSANode[] nodes = getAndTestSSA("objectArray", 1, 0);
		testNode(nodes[0], 7, 0, 5);
	}
	
	@Test
	public void testMultiArray(){
		SSANode[] nodes = getAndTestSSA("multiArray", 1, 0);
		testNode(nodes[0], 39, 0, 12);
	}
	
	@Test
	public void testMultiObjectArray(){
		SSANode[] nodes = getAndTestSSA("multiObjectArray", 1, 0);
		testNode(nodes[0], 28, 0, 10);
	}
}
