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

package ch.ntb.inf.deep.comp.hosttest.cgPPC;

import ch.ntb.inf.deep.cg.ppc.CodeGenPPC;
import ch.ntb.inf.deep.cg.ppc.RegAllocator;
import ch.ntb.inf.deep.classItems.CFR;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.comp.hosttest.cfg.TestCFG;
import ch.ntb.inf.deep.config.Configuration;
import ch.ntb.inf.deep.config.Project;
import ch.ntb.inf.deep.ssa.SSA;
import ch.ntb.inf.deep.ssa.SSANode;
import ch.ntb.inf.deep.ssa.SSAValue;
import ch.ntb.inf.deep.ssa.instruction.PhiFunction;
import ch.ntb.inf.deep.strings.HString;

public class TestCgPPC implements ICclassFileConsts {
	static final boolean vol = false;
	static final boolean nonVol = true;
	
	static SSA[] ssa;
	static CodeGenPPC[] code;
	static int attributes = (1 << atxCode) | (1 << atxLocalVariableTable) | (1 << atxExceptions) | (1 << atxLineNumberTable);
	static String workspace;
	static Project project;

	public static void readConfig() {
		CFR.initBuildSystem();
		workspace = System.getProperty("user.dir");
		project = Configuration.readProjectFile(workspace + "/junitHostTest.deep");
	}

	public static void createCgPPC(Class clazz) {
		TestCFG.createCFG(clazz);
		int nofMethods = TestCFG.cfg.length;
		ssa = new SSA[nofMethods];
		code = new CodeGenPPC[nofMethods];
		for (int i = 0; i < nofMethods; i++){
			ssa[i] = new SSA(TestCFG.cfg[i]);
		}
	}


	public static CodeGenPPC getCode(String name) {
		CodeGenPPC.init();
		int i = 0;
		while (i < TestCFG.cfg.length && !TestCFG.cfg[i].method.name.equals(HString.getHString(name))) i++;
		code[i] = new CodeGenPPC(ssa[i]);
		return code[i];
	}

	public static SSAValue getJoin(int index) {
		return RegAllocator.joins[index];
	}
	
	public static boolean checkJoin(SSAValue join, int start, int end, boolean nonVol, boolean next) {
		assert join != null;
		return join.start == start && join.end == end && (join.nonVol == nonVol) && (next? join.next!= null: join.next==null);
	}
	
	public static boolean checkPhiFunction(SSA ssa, int ssaInstrNr, boolean deleted, boolean used) {
		SSANode node = (SSANode) ssa.cfg.rootNode;
		PhiFunction phi = null;
		while (node != null) {
			for (int i = 0; i < node.nofPhiFunc; i++) {
				if (node.phiFunctions[i].result.n == ssaInstrNr) {
					phi = (PhiFunction)node.phiFunctions[i];
					node = null; 
					break;
				}
			}
			if (node != null) node = (SSANode) node.next;
		}
		return (phi.deleted == deleted && phi.used == used);
	}
}
