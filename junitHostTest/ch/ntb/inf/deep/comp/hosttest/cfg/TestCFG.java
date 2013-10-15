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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.BeforeClass;

import ch.ntb.inf.deep.cfg.CFG;
import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.classItems.CFR;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Project;
import ch.ntb.inf.deep.strings.HString;

public class TestCFG implements ICclassFileConsts {
	static int attributes = (1 << atxCode) | (1 << atxLocalVariableTable) | (1 << atxExceptions) | (1 << atxLineNumberTable);
	static String workspace;
	static Project project;

	public static void readConfig() {
		CFR.initBuildSystem();
		workspace = System.getProperty("user.dir");
		project = Configuration.readProjectFile(workspace + "/junitHostTest.deep");
	}

 
	public static CFG[] cfg;


    /**
	 * Creates CFGs for a java class
	 * 
	 * @param clazz
	 *            Java class object
	 */
	public static void createCFG(Class clazz) {
		
		// create CFG
		Method m1,m2;
		
		int count = 0;	
		m1 =(Method) clazz.methods;
		m2 = m1;
		while (m2 != null){
			count++;
			m2 = (Method)m2.next;
		}
		cfg = new CFG[count];
		for (int i = 0; i < count; i++) {
			cfg[i] = new CFG(m1);
			m1 = (Method)m1.next;
//			cfg[i].printToLog();
		}
	}

	/**
	 * Tests nof nodes in a cfg and returns nodes
	 *  
	 * @param cfgNo
	 *            number of the CFG
	 * @param nofNodes
	 *            number of nodes
	 */
	static public CFGNode[] getAndTestNodes(String name, int nofNodes) {
		int i = 0;
		while (i < cfg.length && !cfg[i].method.name.equals(HString.getHString(name))) i++;

		assertEquals("number of nodes not as expected", nofNodes, cfg[i].getNumberOfNodes());
		
		CFGNode[] nodes = new CFGNode[nofNodes];
		CFGNode node = cfg[i].rootNode;
		for (int k = 0; k < nofNodes; k++) { nodes[k] = node; node = node.next; }
		return nodes;
	}

    /**
	 * Tests the properties of a CFG node
	 * 
	 * @param node
	 *            node
	 * @param firstBCA
	 *            BCA of the first instruction
	 * @param lastBCA
	 *            BCA of the last instruction
	 * @param isLoopHeader
	 *            is node a loop header
	 * @param idom
	 *            immediate dominator of this node
	 * @param predecessors
	 *            array of BCAs of the last instruction in a predecessor
	 * @param successors
	 *            array of BCAs of the first instruction in a successor
	 */
	static public void testNode(CFGNode node, int firstBCA, int lastBCA, boolean isLoopHeader, CFGNode idom, int[] pred, int[] succ) {
		assertEquals("firstBCA not as expected", firstBCA, node.firstBCA);
		assertEquals("lastBCA not as expected", lastBCA, node.lastBCA);
		assertEquals("loop header error", isLoopHeader, node.isLoopHeader());
		assertEquals("idom error", idom, node.idom);

		assertEquals("nof predecessors not as expected", pred.length, node.nofPredecessors);
		for (int i = 0; i < pred.length; i++) {
			assertNotNull(("no predecessor with firstBCA = " + pred[i]), node.getPredecessor(pred[i]));
		}

		assertEquals("nof successors not as expected", succ.length, node.nofSuccessors);
		for (int i = 0; i < succ.length; i++) {
			assertNotNull(("no successor with firstBCA = " + succ[i]),node.getSuccessor(succ[i]));
		}
	}
}
