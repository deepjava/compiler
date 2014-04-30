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

package ch.ntb.inf.deep.ssa;

import ch.ntb.inf.deep.cfg.CFG;
import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.LocalVar;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.ssa.instruction.SSAInstruction;

public class SSA implements ICclassFileConsts, SSAInstructionOpcs {
	private static final boolean dbg = false;
	public CFG cfg;
	public int nofLoopheaders;
	public boolean isParam[]; // indicates which locals are passed as parameters
	public int paramType[]; // types of those parameters
	public LineNrSSAInstrPair[] lineNumTab; // length equals line number table length in class file, entries are sorted by bca
	public int highestLineNr;
	public int lowestLineNr;	
	private int returnCount;
	private SSANode returnNodes[];
	private SSANode loopHeaders[];
	private SSANode sortedNodes[];
	private int nofSortedNodes;

	public SSA(CFG cfg) {
		this.cfg = cfg;
		int nofNodes = cfg.getNumberOfNodes();
		loopHeaders = new SSANode[nofNodes];
		sortedNodes = new SSANode[nofNodes];
		returnNodes = new SSANode[4];
		
		if (dbg) StdStreams.vrb.println("generate ssa for " + cfg.method.owner.name + "." + cfg.method.name);
//		cfg.method.printLocalVars(1);
		
		findParameters();	// fills parameter array
		sortNodes((SSANode)cfg.rootNode);
		
		if (dbg) {
			StdStreams.vrb.print("Node order: ");
			for (int i = 0; i < nofSortedNodes - 1; i++){
				StdStreams.vrb.print("[" + sortedNodes[i].firstBCA + ":"+ sortedNodes[i].lastBCA + "], ");
			}
			StdStreams.vrb.println("[" + sortedNodes[nofSortedNodes-1].firstBCA + ":"+ sortedNodes[nofSortedNodes-1].lastBCA + "]");
			if (isParam.length > 0){
				StdStreams.vrb.println("IsParam");
				StdStreams.vrb.print("[ ");
				for (int i = 0; i < isParam.length - 1; i++) {
					StdStreams.vrb.print(isParam[i] + ", ");
				}
				StdStreams.vrb.println(isParam[isParam.length - 1] + " ]");
			}
			
		}
		
		// visit all
		for (int i = 0; i < nofSortedNodes; i++) {
			SSANode node = sortedNodes[i];
			node.owner = this;
			node.traversed = false;	// reset traversed
			node.mergeAndDetermineStateArray();
			node.traversCode();	// translate bytecode into ssa instructions
			node.traversed = true;
		}

		// visit loop headers again
		for (int i = 0; i < nofLoopheaders; i++) {
			SSANode node = loopHeaders[i];
			node.mergeAndDetermineStateArray();
		}
		
		// clean up phi-functions
		for (int i = 0; i < nofSortedNodes; i++) {
			SSANode node = sortedNodes[i];
			if (!node.isLoopHeader()){
				node.eliminateRedundantPhiFunc();
			}
		}
		
		// clean up phi-functions in loop headers
		for (int i = 0; i < nofLoopheaders; i++) {
			loopHeaders[i].eliminateRedundantPhiFunc();
		}
		
		// if the method has multiple return statements, check if all required parameters are loaded in the last node
		if (returnCount > 1) {
			int nofParams = cfg.method.nofParams;
			if ((cfg.method.accAndPropFlags & (1<<apfStatic)) == 0) {	// instance method
				nofParams++;	// add parameter "this"
			}
			if (nofParams > 0){
				//search last node
				SSANode last = null;
				for (int i = 0;  i < returnCount; i++) {
					if (last == null) {
						last = returnNodes[i];
					} else if (last.firstBCA < returnNodes[i].firstBCA) {
						last = returnNodes[i];
					}					
				}
				for (int x = 0; x < nofParams; x++) {
					boolean isNeeded = false;
					for (int i = 0; i < returnCount && !isNeeded; i++) {
						if (returnNodes[i].exitSet[cfg.method.maxStackSlots + x] != null) {
							isNeeded = true;
						}
					}
					if (isNeeded){	
						last.loadParameter(cfg.method.maxStackSlots + x, paramType[cfg.method.maxStackSlots + x]);
					}
								
				}
			}
		}
		renumberInstructions(cfg);
		createLineNrSSATable();
//		if (true) StdStreams.vrb.println(cfg.toString());
//		if (true) StdStreams.vrb.println(toString());
	}

	private void sortNodes(SSANode node) {
		if (node.traversed) return;	// already processed
		if (node.nofPredecessors > 0) {
			if (node.isLoopHeader()) {
				if(node.idom != null) sortNodes((SSANode) node.idom);
				if(!node.traversed){
					loopHeaders[nofLoopheaders++] = node;	// put into loop header list
					sortedNodes[nofSortedNodes++] = node;					
				}
			} else {
				for (int i = 0; i < node.nofPredecessors; i++) {
					sortNodes((SSANode) node.predecessors[i]);
				}
				if (!node.traversed) sortedNodes[nofSortedNodes++] = node;
			}
		} else {
			if (!node.traversed) sortedNodes[nofSortedNodes++] = node;
		}
		node.traversed = true;
		for (int i = 0; i < node.nofSuccessors; i++) {
			sortNodes((SSANode) node.successors[i]);
		}
	}
	
	/**
	 * @return array with all SSA nodes of this ssa 
	 */
	public SSANode[] getNodes() {
		SSANode current = (SSANode) this.cfg.rootNode;
		SSANode[] nodes = new SSANode[this.getNofNodes()];
		for (int i = 0; i < nodes.length; i++){
			nodes[i] = current;
			current = (SSANode)current.next;
		}
		return nodes;		
	}
	
	/**
	 * @return nof SSA nodes in this ssa
	 */
	public int getNofNodes() {
		int count = 0;
		CFGNode current = this.cfg.rootNode;
		while(current != null){
			count++;
			current = current.next;
		}
		return count;
	}

	/**
	 * Renumber all the instructions in the SSA before computing live intervals
	 * @return nof SSA instructions in method
	 */
	public static int renumberInstructions(CFG cfg) {
		int counter = 0;
		SSANode b = (SSANode) cfg.rootNode;
		while (b != null) {
			for (int i = 0; i < b.nofPhiFunc; i++) {
				b.phiFunctions[i].result.n = counter++;
			}
			for (int i = 0; i < b.nofInstr; i++) {
				b.instructions[i].result.n = counter++;	
			}
			b = (SSANode) b.next;
		}
		return counter;
	}
	
	/**
	 * Determines which entries in the state array are parameters (isParam[]) 
	 * together with their types (paramType[]).
	 */
	private void findParameters() {
		String descriptor = cfg.method.methDescriptor.toString();
		int index = cfg.method.maxStackSlots;
		isParam = new boolean[cfg.method.maxStackSlots + cfg.method.maxLocals];
		paramType = new int[cfg.method.maxStackSlots + cfg.method.maxLocals];
		int flags = cfg.method.accAndPropFlags;
		if ((flags & (1<<apfStatic)) == 0) {	// instance method, add parameter "this"
			isParam[index] = true;
			paramType[index++] = SSAValue.tRef;
		}
		
		char ch = descriptor.charAt(1);
		for (int i = 1;ch != ')'; i++) {//traverse only between (....);
			isParam[index] = true;
			if (ch == '[') {
				while(ch == '[') {
					i++;
					ch = descriptor.charAt(i);
				}
				paramType[index++] = (decodeFieldType(ch) & 0x7fffffff) + 10;//+10 is for Arrays
			} else {				
				paramType[index] = decodeFieldType(ch);
				if (paramType[index]== SSAValue.tLong || paramType[index] == SSAValue.tDouble) index +=2;
				else index++;
			}
			if (ch == 'L') {
				while (ch != ';') {
					i++;
					ch = descriptor.charAt(i);
				}
			}
			ch = descriptor.charAt(i+1);
		}		
	}
	
	public int decodeFieldType(char character){
		int type;;
		switch(character){
		case 'B':
			type = SSAValue.tByte | (1 <<SSAValue.ssaTaFitIntoInt);
			break;
		case 'C':
			type = SSAValue.tChar | (1 <<SSAValue.ssaTaFitIntoInt);
			break;
		case 'D':
			type = SSAValue.tDouble;
			break;
		case 'F':
			type = SSAValue.tFloat;
			break;
		case 'I':
			type = SSAValue.tInteger | (1 <<SSAValue.ssaTaFitIntoInt);
			break;
		case 'J':
			type = SSAValue.tLong;
			break;
		case 'L':
			type = SSAValue.tRef;
			break;
		case 'S':
			type = SSAValue.tShort | (1 <<SSAValue.ssaTaFitIntoInt);
			break;
		case 'Z':
			type = SSAValue.tBoolean | (1 <<SSAValue.ssaTaFitIntoInt);
			break;
		default:
			type = SSAValue.tVoid;
			break;
		}
		return type;
	}

	public void countAndMarkReturns(SSANode node){
		if(returnCount >= returnNodes.length){
			SSANode[]temp = new SSANode[2*returnNodes.length];
			for(int i = 0; i < returnCount; i++){
				temp[i] = returnNodes[i];
			}
			returnNodes = temp;
		}
		returnNodes[returnCount++] = node; 
	}


	private void createLineNrSSATable() {
		int[] origTab = cfg.method.lineNrTab;
		if (origTab != null) { 
			lineNumTab = new LineNrSSAInstrPair[origTab.length];
			SSANode node = (SSANode) cfg.rootNode;
			for (int n = 0; n < origTab.length; n++) {
				int pc = (origTab[n] >> 16) & 0xFFFF;
				if (pc == 0) {
					lineNumTab[n] = new LineNrSSAInstrPair(pc, origTab[n] & 0xFFFF, node.instructions[0]);
					setHighestLowestLineNr(origTab[n] & 0xFFFF);
				} else {
					while (node != null) {
						int i = 0;
						while (i < node.nofInstr && pc > node.instructions[i].bca) i++;
						if (i < node.nofInstr) {
							if (pc == node.instructions[i].bca)
								lineNumTab[n] = new LineNrSSAInstrPair(pc, origTab[n] & 0xFFFF, node.instructions[i]);
							else if (i > 0 && pc == node.instructions[i-1].bca)
								lineNumTab[n] = new LineNrSSAInstrPair(pc, origTab[n] & 0xFFFF, node.instructions[i-1]);
							else 
								lineNumTab[n] = new LineNrSSAInstrPair(pc, origTab[n] & 0xFFFF, node.instructions[i]);
							setHighestLowestLineNr(origTab[n] & 0xFFFF);
							break;

						}
						node = (SSANode) node.next;
					}
				}
			}
		}
	}

	public LineNrSSAInstrPair[] getLineNrTable() {
		return lineNumTab;
	}

	/**
	 * returns the SSA instruction which was generated by a certain Bytecode instruction with address bca
	 */
	public SSAInstruction searchBca(int bca) {
		SSANode node = (SSANode)cfg.rootNode;
		while (node != null) {
			SSAInstruction instr = node.searchBca(bca);
			if (instr != null) return instr;
			node = (SSANode) node.next;
		}
		return null;
	}
	
	private void setHighestLowestLineNr(int lineNr) {
		if (highestLineNr < 1) { // no lineNr is set before
			highestLineNr = lineNr;
			lowestLineNr = lineNr;
		}
		if (highestLineNr < lineNr)	highestLineNr = lineNr;
		if (lineNr < lowestLineNr) lowestLineNr = lineNr;	
	}

	public void printLineNumTab() {
		StdStreams.vrb.println("lineNumTab.length=" + lineNumTab.length);
		for (int i = 0; i < lineNumTab.length && lineNumTab[i] != null; i++) StdStreams.vrb.println(lineNumTab[i].toString());
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		SSANode node = (SSANode) this.cfg.rootNode;
		
		sb.append("SSA for Method: " + cfg.method.owner.name + "." + cfg.method.name + cfg.method.methDescriptor + "\n");
		SSA.renumberInstructions(cfg);
		
		while (node != null) {
			sb.append(node.toString() + "\n");
			node = (SSANode) node.next;
		}
		return sb.toString();
	}

}
