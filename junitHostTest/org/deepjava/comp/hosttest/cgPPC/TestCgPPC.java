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

package org.deepjava.comp.hosttest.cgPPC;

import org.deepjava.cg.Code32;
import org.deepjava.cg.CodeGen;
import org.deepjava.cg.InstructionDecoder;
import org.deepjava.cg.ppc.CodeGenPPC;
import org.deepjava.cg.ppc.InstructionDecoderPPC;
import org.deepjava.cg.ppc.RegAllocatorPPC;
import org.deepjava.classItems.CFR;
import org.deepjava.classItems.Class;
import org.deepjava.classItems.ICclassFileConsts;
import org.deepjava.classItems.Method;
import org.deepjava.comp.hosttest.cfg.TestCFG;
import org.deepjava.config.Configuration;
import org.deepjava.ssa.SSA;
import org.deepjava.ssa.SSANode;
import org.deepjava.ssa.SSAValue;
import org.deepjava.ssa.instruction.PhiFunction;
import org.deepjava.strings.HString;

public class TestCgPPC implements ICclassFileConsts {
	static final boolean vol = false;
	static final boolean nonVol = true;
	
	static SSA[] ssa;
	static CodeGen cg;
	static int attributes = (1 << atxCode) | (1 << atxLocalVariableTable) | (1 << atxExceptions) | (1 << atxLineNumberTable);
	static String workspace;

	public static void readConfig() {
		CFR.initBuildSystem();
		workspace = System.getProperty("user.dir");
		Configuration.readProjectFile(workspace + "/junitHostTest.deep");
		if (Configuration.getBoard().cpu.arch.name.equals(HString.getHString("ppc32"))) cg = new CodeGenPPC();
		Code32.arch = Configuration.getBoard().cpu.arch;
		InstructionDecoder.dec = new InstructionDecoderPPC();
		cg.init();
	}

	public static void createNodes(Class clazz) {
		TestCFG.createCFG(clazz);
		int nofMethods = TestCFG.cfg.length;
		ssa = new SSA[nofMethods];
		for (int i = 0; i < nofMethods; i++) {
			ssa[i] = new SSA(TestCFG.cfg[i]);
		}
	}


	public static Code32 getCode(String name) {
		int i = 0;
		while (i < TestCFG.cfg.length && !TestCFG.cfg[i].method.name.equals(HString.getHString(name))) i++;
		Method m = TestCFG.cfg[i].method;
		m.ssa = ssa[i];
		m.cfg = TestCFG.cfg[i];
		m.machineCode = new Code32(m.ssa);
		cg.translateMethod(m);
//		System.out.println(m.ssa.toString());
//		System.out.println(RegAllocatorPPC.joinsToString());
//		System.out.println(m.machineCode.toString());
		return m.machineCode;
	}

	public static SSAValue getJoin(int index) {
		return RegAllocatorPPC.joins[index];
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
