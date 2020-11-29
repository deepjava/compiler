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

package org.deepjava.cfg;

/**
 * Node of the CFG-Tree with first and last bytecode address (bca).
 */
public class CFGNode {
	static final int nofLinks = 2;	// default number of successors and predecessors

	/**
	 * Used for finding loop headers.
	 */
	boolean visited, active;

	/**
	 * Used to identify nodes belonging to catch clauses.
	 */
	public boolean isCatch;

	/**
	 * Used for calculating dominator tree.
	 */
	int ref;
	boolean root = false;

	/**
	 * Bytecode address of the first bytecode instruction in this node.
	 */
	public int firstBCA;

	/**
	 * Bytecode address of the last bytecode instruction in this node.
	 */
	public int lastBCA;

	/**
	 * Number of backward branches, used for the detection of loop headers (if >
	 * 0 then loop header).
	 */
	int nofBackwardBranches;
	
	/**
	 * Points to immediate dominator of this node.
	 */
	public CFGNode idom;

	/**
	 * Points to the next CFGNode in the flat classfile representation (next can
	 * point to a CFGNode which is not a successor or predecessor in the
	 * instruction flow).
	 */
	public CFGNode next;

	/**
	 * successor and predecessor.
	 */
	public CFGNode[] successors, predecessors;

	/**
	 * counter for successors and predecessors.
	 */
	public int nofSuccessors, nofPredecessors;

	/**
	 * Constructor for a new, empty CFG-Node.
	 */
	public CFGNode() {
		this.active = false;
		this.visited = false;
		this.nofBackwardBranches = 0;
		this.successors = new CFGNode[nofLinks];
		this.predecessors = new CFGNode[nofLinks];
		this.nofSuccessors = 0;
		this.nofPredecessors = 0;
		this.idom = null;
	}

	/**
	 * Checks, if the current node is a loop header (other nodes have
	 * backward-branches to this node).
	 * 
	 * @return <true> if node is a loop header
	 */
	public final boolean isLoopHeader() {
		return nofBackwardBranches > 0;
	}

	/**
	 * getter for the predecessor which starts with the bytecode address
	 * firstBCA.
	 * 
	 * @param firstBCA
	 *            bytecode address of the first instruction of the predecessor
	 * @return the node with starting address firstBCA, null if no such node
	 *         exists
	 */
	public final CFGNode getPredecessor(final int firstBCA) {
		int i = 0;
		while (i < nofPredecessors) {
			if (predecessors[i].firstBCA == firstBCA) return predecessors[i];
			i++;
		}
		return null;
	}

	/**
	 * getter for the successor which start with the bytecode address firstBCA.
	 * 
	 * @param firstBCA
	 *            bytecode address of the first instruction of the successor
	 * @return the node with starting address firstBCA, null if no such node
	 *         exists
	 */
	public final CFGNode getSuccessor(final int firstBCA) {
		int i = 0;
		while (i < nofSuccessors) {
			if (successors[i].firstBCA == firstBCA) return successors[i];
			i++;
		}
		return null;
	}

	/**
	 * Adds a node to the array of successors.
	 * 
	 * @param node
	 *            node to add.
	 */
	public final void addSuccessor(CFGNode node) {
		int len = successors.length;
		if (nofSuccessors == len) {
			CFGNode[] newArray = new CFGNode[2 * len];
			for (int k = 0; k < len; k++)
				newArray[k] = successors[k];
			successors = newArray;
		}
		successors[nofSuccessors] = node;
		nofSuccessors++;
	}

	/**
	 * Adds a node to the array of predecessors.
	 * 
	 * @param node
	 *            node to add.
	 */
	public final void addPredecessor(CFGNode node) {
		if (getPredecessor(node.firstBCA) != null) return;	// node already in array
		int len = predecessors.length;
		if (nofPredecessors == len) {
			CFGNode[] newArray = new CFGNode[2 * len];
			for (int k = 0; k < len; k++)
				newArray[k] = predecessors[k];
			predecessors = newArray;
		}
		predecessors[nofPredecessors] = node;
		nofPredecessors++;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[" + firstBCA + ":" + lastBCA + "]");
		sb.append(isLoopHeader()? ", is loop header":"");
		sb.append(nofBackwardBranches > 0? ", bckwd branches=" + nofBackwardBranches:"");
		sb.append(idom != null? ", idom=[" + idom.firstBCA + ":" + idom.lastBCA + "]":", idom=null");
		sb.append(isCatch? ", is first node of catch":"");
		sb.append(", ref=" + ref);
		sb.append(", visited:" + visited);
		sb.append("\n");
		for (int n = 0; n < 6; n++) sb.append(" ");
		sb.append("predecessor: ");
		for (int k = 0; (k < predecessors.length) && (predecessors[k] != null); k++) {
			sb.append("[" + predecessors[k].firstBCA + ":" + predecessors[k].lastBCA + "]");
		}
		sb.append("\n");
		for (int n = 0; n < 6; n++) sb.append(" ");
		sb.append("successor: ");
		for (int k = 0; (k < successors.length)	&& (successors[k] != null); k++) {
			sb.append("[" + successors[k].firstBCA + ":" + successors[k].lastBCA + "]");
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public String toString(boolean cfg) {
		return toString(false);
	}

}
