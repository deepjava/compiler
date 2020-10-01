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

import static org.junit.Assert.assertEquals;
import ch.ntb.inf.deep.classItems.CFR;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.comp.hosttest.cfg.TestCFG;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.ssa.SSA;
import ch.ntb.inf.deep.ssa.SSANode;
import ch.ntb.inf.deep.strings.HString;

public class TestSSA implements ICclassFileConsts{
	static int attributes = (1 << atxCode) | (1 << atxLocalVariableTable) | (1 << atxExceptions) | (1 << atxLineNumberTable);
	static String workspace;

	public static void readConfig() {
		CFR.initBuildSystem();
		workspace = System.getProperty("user.dir");
		Configuration.readProjectFile(workspace + "/junitHostTest.deep");
	}

	static public SSA[] ssa;

    /**
	 * Creates SSA for all methods of a class
	 * 
	 * @param clazz
	 *            Java class object
	 */
	public static void createSSA(Class clazz) {
		TestCFG.createCFG(clazz);
		ssa = new SSA[TestCFG.cfg.length];
		for (int i = 0; i < TestCFG.cfg.length; i++){
			ssa[i] = new SSA(TestCFG.cfg[i]);
//			System.out.println();
//			System.out.println(ssa[i].toString());
//			ssa[i].printLineNumTab();
		}
	}

	/**
	 * Checks a SSANode
	 * 
	 * @param node
	 *            node to check
	 * @param nofSSAInstructions
	 *            expected number of ssa-instructions in this node
	 * @param nofPhiFunctions
	 * 			  expected number of phi functions in this node
	 * @param localsLength
	 * 			  expected length of the state array
	 */
	
	public static void testNode(SSANode node, int nofSSAInstructions, int nofPhiFunctions, int localsLength) {
		assertEquals("nof SSA instructions not as expected", nofSSAInstructions, node.nofInstr);
		assertEquals("nof phi functions not as expected", nofPhiFunctions, node.nofPhiFunc - node.nofDeletedPhiFunc);
		assertEquals("length of EntrySet not as expected", localsLength, node.entrySet.length);
		assertEquals("length of ExitySet not as expected", localsLength, node.exitSet.length);
	}

	/**
	 * Tests nof nodes and nof loopheaders in a ssa
	 *  
	 * @param ssaNo
	 *            number of the SSA to check
	 * @param nofNodes
	 *            expected number of nodes in this ssa
	 * @param nofLoopheaders
	 * 			  expected number of loop headers in this ssa
	 */
	public static SSANode[] getAndTestSSA(String methodName, int nofNodes, int nofLoopheaders) {
		int i = 0;
		while (i < ssa.length && ! ssa[i].cfg.method.name.equals(HString.getHString(methodName))) i++;
		assertEquals("nof nodes not as expected", nofNodes, ssa[i].getNofNodes());
		assertEquals("nof loopheaders not as expected",nofLoopheaders,ssa[i].nofLoopheaders);
		return ssa[i].getNodes();
	}


}
