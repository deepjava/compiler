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

package ch.ntb.inf.deep.cfg;

/**
 * Node in the CFG-Tree with first and last bytecode address (bca).
 * 
 * 
 * @author buesser, graf
 */
public class CFGNode {
	static final int nofLinks = 2;

	/**
	 * used for finding loop headers.
	 */
	boolean visited, active;

	/**
	 * used for calculating dominator tree.
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
//		if (getSuccessor(node.firstBCA) != null) return;	// node already in array
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
		return "CFG-Node [" + firstBCA + ":" + lastBCA + "]";
	}

}
