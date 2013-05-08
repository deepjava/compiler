/*
 * Copyright (c) 2011 NTB Interstate University of Applied Sciences of Technology Buchs.
 *
 * http://www.ntb.ch/inf
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Eclipse Public License for more details.
 * 
 * Contributors:
 *     NTB - initial implementation
 * 
 */

package ch.ntb.inf.deep.ssa;

import ch.ntb.inf.deep.cfg.CFG;
import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.host.StdStreams;

/**
 * @author millischer
 */

public class SSA implements ICclassFileConsts, SSAInstructionOpcs {
	private static boolean dbg = false;
	public CFG cfg;
	public int nofLoopheaders;
	public boolean isParam[]; // indicates which locals are passed as parameters
	public int paramType[]; // types of those parameters
	private LineNrSSAInstrPair[] lineNumTab; // length equals line number table length in class file, entries are sorted by bca
	public int highestLineNr;
	public int lowestLineNr;	
	private int returnCount;
	private SSANode returnNodes[];
	private SSANode loopHeaders[];
	private SSANode sortedNodes[];
	private int nofSortedNodes;

	public SSA(CFG cfg) {
		this.cfg = cfg;
		loopHeaders = new SSANode[cfg.getNumberOfNodes()];
		sortedNodes = new SSANode[cfg.getNumberOfNodes()];
		returnCount = 0;
		returnNodes = new SSANode[4];
		nofLoopheaders = 0;
		nofSortedNodes = 0;	
		highestLineNr = 0;
		lowestLineNr = 0;
		
		determineParam();	// fills parameter array
		sortNodes((SSANode)cfg.rootNode);
		
		if(dbg) {
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
		
		determineStateArray();
		
		//if the method have multiple return statements, so check if in the last node all required parameters are loaded
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
						last.loadLocal(cfg.method.maxStackSlots + x, paramType[cfg.method.maxStackSlots + x]);
					}
								
				}
			}
		}
		renumberInstructions(cfg);
		createLineNrSSATable();
//		if (true) StdStreams.vrb.println(cfg.toString());
//		if (true) StdStreams.vrb.println(toString());
	}

	public void determineStateArray() {		
		// visit all
		for (int i = 0; i < nofSortedNodes; i++) {
			//reset traversed for next use
			sortedNodes[i].traversed = false;
			sortedNodes[i].mergeAndDetermineStateArray(this);
		}

		// second visit of loop headers
		for (int i = 0; i < nofLoopheaders; i++) {
			loopHeaders[i].mergeAndDetermineStateArray(this);
		}
		//clean up PhiFunctions
		for (int i = 0; i < nofSortedNodes; i++) {
			if(!sortedNodes[i].isLoopHeader()){
				sortedNodes[i].eliminateRedundantPhiFunc();
			}
		}
		// clean up PhiFunctins in loop headers
		for (int i = 0; i < nofLoopheaders; i++) {
			loopHeaders[i].eliminateRedundantPhiFunc();
		}

	}

	private void sortNodes(SSANode rootNode) {
		if (rootNode.traversed) {// if its already processed
			return;
		}
		if (rootNode.nofPredecessors > 0) {
			if (rootNode.isLoopHeader()) {

				if(rootNode.idom != null) sortNodes((SSANode)rootNode.idom);
				if(!rootNode.traversed){
					// mark loop headers for traverse a second time
					loopHeaders[nofLoopheaders++] = rootNode;
					sortedNodes[nofSortedNodes++] = rootNode;					
				}
			} else {
				for (int i = 0; i < rootNode.nofPredecessors; i++) {
					sortNodes((SSANode) rootNode.predecessors[i]);
					
				}
				if(!rootNode.traversed)sortedNodes[nofSortedNodes++] = rootNode;
			}
		} else {
			if(!rootNode.traversed)sortedNodes[nofSortedNodes++] = rootNode;
		}
		rootNode.traversed = true;
		for (int i = 0; i < rootNode.nofSuccessors; i++) {
			sortNodes((SSANode) rootNode.successors[i]);
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
	
	
	private void determineParam(){
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
		for (int i = 1;ch != ')'; i++){//traverse only between (....);
			isParam[index] = true;
			if(ch == '['){
				while(ch == '['){
					i++;
					ch = descriptor.charAt(i);
				}
				paramType[index++] = (decodeFieldType(ch) & 0x7fffffff) + 10;//+10 is for Arrays
				
			}else{				
				paramType[index] = decodeFieldType(ch);
				if(paramType[index]== SSAValue.tLong || paramType[index] == SSAValue.tDouble){
					index +=2;
				}else{
					index++;
				}
			}
			if(ch == 'L'){
				while(ch != ';'){
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
		if (origTab != null) lineNumTab = new LineNrSSAInstrPair[origTab.length];
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

	public LineNrSSAInstrPair[] getLineNrTable() {
		return lineNumTab;
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
