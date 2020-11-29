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

import org.deepjava.classItems.*;
import org.deepjava.host.ErrorReporter;
import org.deepjava.host.ICjvmInstructionMnemonics;
import org.deepjava.host.StdStreams;
import org.deepjava.ssa.SSANode;


/**
 * Builder for the CFG of a java method. Reads the bytecode instructions and
 * builds the CFG.
 */
public class CFG implements ICjvmInstructionMnemonics {
	private static final boolean dbg = false;

	/**
	 * Start-node of the CFG.
	 */
	public final CFGNode rootNode;
	
	/**
	 * Method for which the CFG is build. Used in the toString-Methods, SSA and
	 * Linker.
	 */
	public final Method method;

	/**
	 * Contains the code
	 */
	public final byte[] code;

	/**
	 * get the number of CFG-Nodes.
	 * 
	 * @return number of nodes in the cfg
	 */
	public final int getNumberOfNodes() {
		if (rootNode == null) {
			return 0;
		} else {
			int counter = 0;
			CFGNode node = rootNode;
			while (node != null) {
				counter++;
				node = node.next;
			}
			return counter;
		}
	}

	/**
	 * Returns the node that contains the instruction addressed by bca.
	 * 
	 * @param bca
	 *            bytecode address of the node
	 * @return cfg-node at the corresponding bytecode address, null if no such
	 *         node exists.
	 */
	private final CFGNode getNode(final int bca) {
		CFGNode node = rootNode;
		if (bca < 0)
			return null;
		while (node != null) {
			if ((bca >= node.firstBCA && bca <= node.lastBCA) || (bca > node.lastBCA && bca < node.next.firstBCA)) {
//			if ((bca >= node.firstBCA && bca <= node.lastBCA)) {
				return node;
			} else {
				node = node.next;
			}
		}
		assert false;
		return null; // node not found!!
	}

	/**
	 * Creates a CFG of a method.
	 * 
	 * @param m
	 *            Method for which the CFG is built
	 */
	public CFG(final Method m) {
		this.method = m;

		CFGNode startNode = new SSANode();
		rootNode = startNode;
		rootNode.root = true;
		code = m.code;

		int len = code.length;
		startNode.firstBCA = 0;
		startNode.lastBCA = findLastBcaInNode(startNode, len);
		int bca = 0;
		if (dbg) StdStreams.vrb.println("build cfg for " + m.owner.name + "." + m.name);
		if (dbg) m.printExceptionTable(1);
		
		while (bca < len) {
			int bci = code[bca] & 0xff;
			if (dbg) StdStreams.vrb.println("   " + bca + " " + bcMnemonics[bci]);
			int entry = bcAttrTab[bci];
			int instrLen = (entry >> 8) & 0xF;
			if (instrLen == 0) {	// bCtableswitch or bClookupswitch
				int addr = bca + 1;
				addr = (addr + 3) & -4; // round to the next multiple of 4
				int defaultOffset = getInt(code, addr);
				addr += 4; // skip default offset
				if (bci == bCtableswitch) {	// bCtableswitch
					int low = getInt(code, addr);
					int high = getInt(code, addr + 4);
					int nofCases = high - low + 1;
					for (int i = 0; i < nofCases; i++) {
						int branchOffset = getInt(code, addr + 8 + i * 4);
						if (dbg) StdStreams.vrb.println("\tbranchOffset = " + branchOffset);
						split(bca, bca + branchOffset);
					}
					if (dbg) StdStreams.vrb.println("\tdefaultOffset = " + defaultOffset);
					split(bca, bca + defaultOffset);
					instrLen = ((high-low) + 1) * 4 + addr + 8 - bca;
				} else {	// bClookupswitch
					int nofPairs = getInt(code, addr);
					for (int i = 0; i < nofPairs; i++) {
						int branchOffset = getInt(code, addr + 8 + i * 8);
						if (dbg) StdStreams.vrb.println("\tbranchOffset = " + branchOffset);
						split(bca, bca + branchOffset);
					}
					if (dbg) StdStreams.vrb.println("\tdefaultOffset = " + defaultOffset);
					split(bca, bca + defaultOffset);
					instrLen = nofPairs * 8 + 4 + addr - bca;
				}
				eliminateGoto(bca, 0);
			} else if (bci == bCwide) {	// wide instruction
				entry = bcAttrTab[code[bca+1] & 0xff];
				instrLen = ((entry >> 8) & 0xF) + ((entry >> 12) & 0x3) + 1;	// add wide to len
			} else if ((entry & (1 << bcapBranch)) != 0  && (entry & (1 << bcapReturn)) == 0) {  // is branch but no return
				int branchOffset = (short)(((code[bca + 1]&0xff) << 8) | (code[bca + 2]&0xff));
				split(bca, bca + branchOffset);
			} else if (bci == bCathrow) {
				split(bca, -1); 
			}
			
			bca += instrLen;
		}
		if (bca != len) {
			ErrorReporter.reporter.error(401);
			return;
		}
		
		if (dbg) StdStreams.vrb.println("   get catch blocks");
		if (m.exceptionTab != null) enterCatchBlocks(m.exceptionTab);
		
		if (dbg) StdStreams.vrb.println("   marking loop headers");
		markLoopHeaders(rootNode);
		
		if (dbg) StdStreams.vrb.println("   eliminating dead nodes");
		eliminateDeadNodes();
		
		if (dbg) StdStreams.vrb.println("   enter predecessors");
		enterPredecessors();
		
		if (dbg) StdStreams.vrb.println("   build dom");
		findDominators();
		
		if (dbg) StdStreams.vrb.println(toString());
	}

	/**
	 * Split the CFG node after the instruction with bytecode address bca and
	 * before the branch address.
	 * 
	 * @param bca
	 *			address to split before.
	 * @param branchAddress
	 * 			if -1 -> no branch address, hence no splitting
	 */
	private void split(int bca, int branchAddr) {
		if (dbg) StdStreams.vrb.println("\tsplit node after address " + bca + " and before branch target at " + branchAddr);
		int entry;
		CFGNode srcNode = getNode(bca);
		if (srcNode == null) {
			ErrorReporter.reporter.error(400);
			return;
		}
		if (bca != srcNode.lastBCA) { // if last instruction, no splitting
			// split after branch
			CFGNode newNode = new SSANode();
			newNode.lastBCA = srcNode.lastBCA;
			int bci = code[bca] & 0xff;
			entry = bcAttrTab[bci];
			int instrLen = (entry >> 8) & 0xF;
			if (instrLen == 0) {
				if (bci == bCtableswitch) 
					instrLen = tableSwitchLen(bca);
				else // lookupswitch
					instrLen = lookupSwitchLen(bca);
			} else if (bci == bCwide) {	// wide instruction
				instrLen = ((entry >> 8) & 0xF) + ((entry >> 12) & 0x3);
			}
			newNode.firstBCA = bca + instrLen;
			srcNode.lastBCA = bca;
			newNode.next = srcNode.next;
			srcNode.next = newNode;
			newNode.successors = srcNode.successors;
			newNode.nofSuccessors = srcNode.nofSuccessors;
			srcNode.successors = new SSANode[CFGNode.nofLinks];
			srcNode.nofSuccessors = 0;
			entry = bcAttrTab[code[bca] & 0xff];
			if ((entry & (1 << bcapCondBranch)) != 0) { // no link if unconditional branch
				srcNode.addSuccessor(newNode);
			}
			if (dbg) StdStreams.vrb.println("\t\tnew node: from bca=" + newNode.firstBCA + " to bca=" + newNode.lastBCA);
		}
		
		if (branchAddr == -1) return;	// no branch target in case of athrow
		CFGNode targNode = getNode(branchAddr);
		if (targNode == null) {
			ErrorReporter.reporter.error(400);
			return;
		}
		if (branchAddr != targNode.firstBCA) { // not first instruction
			// split before target address
			CFGNode newTargNode = new SSANode();
			newTargNode.lastBCA = targNode.lastBCA;
			newTargNode.firstBCA = branchAddr;
			targNode.lastBCA = findLastBcaInNode(targNode, branchAddr);
			newTargNode.next = targNode.next;
			targNode.next = newTargNode;
			newTargNode.successors = targNode.successors;
			newTargNode.nofSuccessors = targNode.nofSuccessors;
			targNode.successors = new SSANode[CFGNode.nofLinks];
			targNode.nofSuccessors = 0;
			entry = bcAttrTab[code[targNode.lastBCA] & 0xff];
			if ((entry & ((1 << bcapUncondBranch) | (1 << bcapReturn) | (1 << bcapSwitch))) == 0) {
				// no link if last bci was jump or return
				targNode.addSuccessor(newTargNode);
			}
			srcNode = getNode(bca);
			if(srcNode == null) {
				ErrorReporter.reporter.error(400);
				return;
			}
			srcNode.addSuccessor(newTargNode);
		} else {	// if first instruction, no splitting
			srcNode = getNode(bca);
			if(srcNode == null)	{
				ErrorReporter.reporter.error(400);
				return;
			}
			srcNode.addSuccessor(targNode);
		}
	}

	/**
	 * Eliminates unnecessary goto blocks in switch statements.
	 * 
	 * @param bca
	 *			address to split before.
	 * @param branchAddress
	 * 			if -1 -> no branch address, hence no splitting
	 */
	private void eliminateGoto(int bca, int branchAddr) {
		if (dbg) StdStreams.vrb.println("\teliminates unnecessary goto blocks in switch statement at bca=" + bca);
		CFGNode switchNode = getNode(bca);
		for (int i = 0; i < switchNode.nofSuccessors; i++) {
			CFGNode node = switchNode.successors[i];
			int addr = node.firstBCA;
			int entry = bcAttrTab[code[addr] & 0xff];
			if ((entry & (1 << bcapUncondBranch)) != 0) {	// node is a goto
				if (dbg) StdStreams.vrb.println("\t\teliminate goto node at bca = " + addr);
				int offset = (short) (((code[addr + 1]&0xff) << 8) | (code[addr + 2]&0xff));
				CFGNode dest = getNode(addr+offset);
				assert dest != null;
				switchNode.successors[i] = dest;
			}
		}
	}

	/**
	 * Searches a node for the last byte code address of a instruction which
	 * comes before a certain address.
	 * 
	 * @param node
	 *            node to search in.
	 * @param addr
	 *            the address, up to which to search
	 * @return address of the last byte code instruction.
	 */
	private final int findLastBcaInNode(CFGNode node, int addr) {
		//		if (dbg) StdStreams.vrb.println("find last bca in node [" + node.firstBCA + ":" + node.lastBCA + "]");
		int instrLen = 0;
		int bca = node.firstBCA;
		while (bca < addr) {
			int entry = bcAttrTab[code[bca] & 0xff];
			instrLen = (entry >> 8) & 0xF;
			int bci = (entry & 0xff);
			if (instrLen == 0) {
				if (bci == bCtableswitch) {	// bCtableswitch
					instrLen = tableSwitchLen(bca);
				} else { // bClookupswitch) {
					instrLen = lookupSwitchLen(bca);
				}
				if (dbg) StdStreams.vrb.println("\tinstruction at " + bca + " with len undef, len = " + instrLen);
			} else if (bci == bCwide) {	// wide instruction
				entry = bcAttrTab[code[bca+1] & 0xff];
				instrLen = ((entry >> 8) & 0xF) + ((entry >> 12) & 0x3) + 1;	// add wide to len
				if (dbg) StdStreams.vrb.println("\tinstruction at " + bca + " with len undef, len = " + instrLen);
			}
			bca += instrLen;
		}
		return bca - instrLen;
	}

	/**
	 * Returns the length of a tableswitch instruction.
	 * 
	 * @param bca
	 *            bytecode address of instruction
	 * @return len in nof bytes.
	 */
	private final int tableSwitchLen(int bca) {
		int addr = bca + 1;
		addr = (addr + 3) & -4; // round to the next multiple of 4
		int low = getInt(code, addr + 4);
		int high = getInt(code, addr + 8);
		return (addr + 12 + (high - low + 1) * 4) - bca;
	}

	/**
	 * Returns the length of a lookupswitch instruction.
	 * 
	 * @param bca
	 *            bytecode address of instruction
	 * @return len in nof bytes.
	 */
	private final int lookupSwitchLen(int bca) {
		int addr = bca + 1;
		addr = (addr + 3) & -4; // round to the next multiple of 4
		int npairs = getInt(code, addr + 4);
		return addr + 8 + 8 * npairs - bca;
	}

	/**
	 * Adds all catch blocks of a method as successors to its try block
	 * and marks them as catch blocks
	 */
	private final void enterCatchBlocks(ExceptionTabEntry[] entries) {
		for (ExceptionTabEntry e : entries) {
			CFGNode catchNode = getNode(e.handlerPc);
			assert catchNode != null;
			if (catchNode.firstBCA != e.handlerPc) {	// method could be a single node containing a return before the catch
				split(e.handlerPc-1, -1);				// such a node has to be split here
				catchNode = getNode(e.handlerPc);
				assert catchNode != null;
			}
			catchNode.isCatch = true;
			CFGNode tryNode = getNode(e.endPc-1);	// [startPc...[endPc
			assert tryNode != null;
			tryNode.addSuccessor(catchNode);
		}
	}

	/**
	 * Marks target nodes of backward branches (headers of loops like
	 * while...do, do...while and for...).
	 */
	private final void markLoopHeaders(CFGNode b) {
		if (!(b.visited)) {
			b.visited = true;
			b.active = true;
			for (int i = 0; i < b.nofSuccessors; i++)
				markLoopHeaders(b.successors[i]);
			b.active = false;
		} else if (b.active) {
			b.nofBackwardBranches++;
		}
	}

	/**
	 * Eliminates all dead nodes (goto)
	 */
	private final void eliminateDeadNodes() {
		CFGNode current = rootNode;
		while (current != null) {
			CFGNode next = current.next;
			if (next != null && !next.visited) // dead node
				current.next = next.next;
			current = next;
		}
	}
	
	/**
	 * Enters all the predecessors for all nodes of a cfg
	 */
	private final void enterPredecessors() {
		CFGNode node = rootNode;
		while (node != null) {
			for (int i = 0; i < node.nofSuccessors; i++) {
				CFGNode b = node.successors[i];
				b.addPredecessor(node);
				b.ref = b.nofPredecessors;
			}
			node = node.next;
		}
	}

	/**
	 * Find the dominators of all nodes of a cfg
	 */
	private final void findDominators() {
		CFGNode node = rootNode;	
		while (node != null) {
			node.visited = false;
			node = node.next;
		}
		node = rootNode;
		node.idom = null;
		for (int i = 0; i < node.nofSuccessors; i++)
			visitDom(node.successors[i], node);
	}

	private void visitDom(CFGNode b, CFGNode predecessor) {
		if (b.root) return; 
		b.ref--;
		if (b.idom == null)
			b.idom = predecessor;
		else
			b.idom = commonDom(b.idom, predecessor);
		if (b.ref == b.nofBackwardBranches) {
			for (int i = 0; i < b.nofSuccessors; i++) {
				visitDom(b.successors[i], b);
			}
		}
	}

	private final CFGNode commonDom(CFGNode a, CFGNode b) {
		CFGNode aa = a; 
		CFGNode bb = b;
		do {
			a.visited = true;
			a = a.idom;
		} while (a != null);
		do {
			if (b.visited) break;
			b.visited = true;
			b = b.idom;			
		} while (b != null);
		while (aa != null) {
			aa.visited = false;
			aa = aa.idom;
		}
		while (bb != null) {
			bb.visited = false;
			bb = bb.idom;
		}
		return b;
	}

	private static int getInt(byte[] bytes, int index){
		return ((bytes[index]&0xff)<<24) | (bytes[index+1]&0xff)<<16 | (bytes[index+2]&0xff)<<8 | (bytes[index+3]&0xff);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CFG of method " + method.owner.name + "." + method.name + "\n");
		int i = 0;
		CFGNode node = this.rootNode;
		while (node != null) {
			for (int n = 0; n < 3; n++) sb.append(" ");
			sb.append("CFGNode nr:" + i + " ");
			sb.append(node.toString(true) + "\n");
			node = node.next;
			i++;
		}
		return sb.toString();
	}

}
