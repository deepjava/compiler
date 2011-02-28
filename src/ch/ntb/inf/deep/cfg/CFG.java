package ch.ntb.inf.deep.cfg;

import ch.ntb.inf.deep.classItems.*;
import ch.ntb.inf.deep.debug.ICjvmInstructionOpcsAndMnemonics;
import ch.ntb.inf.deep.host.StdStreams;
import ch.ntb.inf.deep.ssa.SSANode;


/**
 * Builder for the CFG of a java method. Reads the bytecode instructions and
 * builds the CFG.
 * 
 * 
 * @author buesser 23.2.2010
 * Urs Graf 27.2.2011, wide corrected
 */
public class CFG implements ICjvmInstructionOpcsAndMnemonics {
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
			if (node.firstBCA <= bca && node.lastBCA >= bca) {
				return node;
			} else {
				node = node.next;
			}
		}
		return null; // node not found!!
	}

	/**
	 * Creates a CFG of a method.
	 * 
	 * @param method
	 *            Method for which the CFG is build
	 */
	public CFG(final Method method) {
		this.method = method;

		CFGNode startNode = new SSANode();
		rootNode = startNode;
		rootNode.root = true;
		code = method.code;

		int len = code.length;
		startNode.firstBCA = 0;
		startNode.lastBCA = findLastBcaInNode(this, startNode, len);
		int bca = 0;
		if (dbg) StdStreams.out.println("build cfg");
		while (bca < len) {
			int bci = code[bca] & 0xff;
			if (dbg) StdStreams.out.println("\t" + bca + " " + bcMnemonics[bci]);
			int entry = bcAttrTab[bci];
			int instrLen = (entry >> 8) & 0xF;
			if (instrLen == 0) {
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
						split(this, bca, bca + branchOffset);
					}
					split(this, bca, bca + defaultOffset);
					instrLen = ((high-low) + 1) * 4 + addr + 8 - bca;
				} else {	// bClookupswitch
					int nofPairs = getInt(code, addr);
					for (int i = 0; i < nofPairs; i++) {
						int branchOffset = getInt(code, addr + 8 + i * 8);
						split(this, bca, bca + branchOffset);
					}
					split(this, bca, bca + defaultOffset);
					instrLen = nofPairs * 8 + 4 + addr - bca;
				}
			} else if (bci == bCwide) {	// wide instruction
				entry = bcAttrTab[code[bca+1] & 0xff];
				instrLen = ((entry >> 8) & 0xF) + ((entry >> 12) & 0x3) + 1;	// add wide to len
			} else if ((entry & (1 << bcapBranch)) != 0  && (entry & (1 << bcapReturn)) == 0) {
				int branchOffset = (short)(((code[bca + 1]&0xff) << 8) | (code[bca + 2]&0xff));
				split(this, bca, bca + branchOffset);
			}
			
			bca += instrLen;
		}
		assert (bca == len) : "last instruction not at end of method";
		if (dbg) StdStreams.out.println("marking loop headers");
		markLoopHeaders(rootNode);
		if (dbg) StdStreams.out.println("eliminating dead nodes");
		eliminateDeadNodes(this);
		if (dbg) StdStreams.out.println("enter predecessors");
		enterPredecessors(this);
		CFGNode current = this.rootNode;	// prepare to find dominators
		while (current != null) {
			current.visited = false;
			current = current.next;
		}
		rootNode.idom = null;
		if (dbg) StdStreams.out.println("build dom");
		for (int i = 0; i < rootNode.nofSuccessors; i++)
			visitDom(rootNode.successors[i], rootNode);
		if (dbg) printToLog();
	}

	/**
	 * Split the CFG node after the instruction with bytecode address bca and
	 * before the branch address.
	 * 
	 * @param cfg
	 *            cfg with the node to split.
	 * @param bca
	 *            address to split before.
	 * @param branchAddress
	 */
	private static void split(CFG cfg, int bca, int branchAddr) {
		byte[] code = cfg.code;
		int entry;
		CFGNode srcNode = cfg.getNode(bca);
		if (bca != srcNode.lastBCA) { // if last instruction, no splitting
			// split after branch
			CFGNode newNode = new SSANode();
			newNode.lastBCA = srcNode.lastBCA;
			int bci = code[bca] & 0xff;
			entry = bcAttrTab[bci];
			int instrLen = (entry >> 8) & 0xF;
			if (instrLen == 0) {
				if (bci == bCtableswitch) 
					instrLen = tableSwitchLen(cfg, bca);
				else // lookupswitch
					instrLen = lookupSwitchLen(cfg, bca);
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
			if ((entry & (1 << bcapCondBranch)) != 0) { // no link if
				// unconditional branch
				srcNode.addSuccessor(newNode);
			}
		}
		entry = bcAttrTab[code[branchAddr] & 0xff];
		if ((entry & (1 << bcapUncondBranch)) != 0) {
			if (dbg)
				StdStreams.out.println("eliminate goto node at bca = " + branchAddr);
			// branch target is a goto, jump to new target node
			branchAddr += (short) (code[branchAddr + 1] & 0xff << 8 | code[branchAddr + 2]);
			if (dbg) StdStreams.out.println("new branch address = " + branchAddr);
		}
		CFGNode targNode = cfg.getNode(branchAddr);
		if (branchAddr != targNode.firstBCA) { // if first instruction, no splitting
			// split before target address
			CFGNode newTargNode = new SSANode();
			newTargNode.lastBCA = targNode.lastBCA;
			newTargNode.firstBCA = branchAddr;
			targNode.lastBCA = findLastBcaInNode(cfg, targNode, branchAddr);
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
			srcNode = cfg.getNode(bca);
			srcNode.addSuccessor(newTargNode);
		} else {
			srcNode = cfg.getNode(bca);
			srcNode.addSuccessor(targNode);
		}
	}

	/**
	 * Searches a node for the last byte code address of a instruction which
	 * comes before a certain address.
	 * 
	 * @param cfg
	 *            cfg with the node to search.
	 * @param node
	 *            node to search in.
	 * @param addr
	 *            the address, up to which to search
	 * @return address of the last byte code instruction.
	 */
	private static int findLastBcaInNode(CFG cfg, CFGNode node, int addr) {
		if (dbg) StdStreams.out.println("find last bca in node [" + node.firstBCA + ":" + node.lastBCA + "]");
		byte[] code = cfg.code;
		int instrLen = 0;
		int bca = node.firstBCA;
		while (bca < addr) {
			int entry = bcAttrTab[code[bca] & 0xff];
			instrLen = (entry >> 8) & 0xF;
			int bci = (entry & 0xff);
			if (instrLen == 0) {
				if (bci == bCtableswitch) {	// bCtableswitch
					instrLen = tableSwitchLen(cfg, bca);
				} else { // bClookupswitch) {
					instrLen = lookupSwitchLen(cfg, bca);
				}
				if (dbg) StdStreams.out.println("\tinstruction at " + bca + " with len undef, len = " + instrLen);
			} else if (bci == bCwide) {	// wide instruction
				entry = bcAttrTab[code[bca+1] & 0xff];
				instrLen = ((entry >> 8) & 0xF) + ((entry >> 12) & 0x3) + 1;	// add wide to len
				if (dbg) StdStreams.out.println("\tinstruction at " + bca + " with len undef, len = " + instrLen);
			}
			bca += instrLen;
		}
		return bca - instrLen;
	}

	/**
	 * Returns the length of a tableswitch instruction.
	 * 
	 * @param cfg
	 *            cfg with the node to read.
	 * @param bca
	 *            bytecode address of instruction
	 * @return len in nof bytes.
	 */
	private static int tableSwitchLen(CFG cfg, int bca) {
		byte[] code = cfg.code;
		int addr = bca + 1;
		addr = (addr + 3) & -4; // round to the next multiple of 4
		int low = getInt(code, addr + 4);
		int high = getInt(code, addr + 8);
		return (addr + 12 + (high - low + 1) * 4) - bca;
	}

	/**
	 * Returns the length of a lookupswitch instruction.
	 * 
	 * @param cfg
	 *            cfg with the node to read.
	 * @param bca
	 *            bytecode address of instruction
	 * @return len in nof bytes.
	 */
	private static int lookupSwitchLen(CFG cfg, int bca) {
		byte[] code = cfg.code;
		int addr = bca + 1;
		addr = (addr + 3) & -4; // round to the next multiple of 4
		int npairs = getInt(code, addr + 4);
		return addr + 8 + 8 * npairs - bca;
	}

	/**
	 * Marks target nodes of backward branches (headers of loops like
	 * while...do, do...while and for...).
	 */
	private static void markLoopHeaders(CFGNode b) {
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
	 * Enters all the predecessors for all nodes of a cfg
	 */
	private static void enterPredecessors(CFG cfg) {
		CFGNode node = cfg.rootNode;
		while (node != null) {
			for (int i = 0; i < node.nofSuccessors; i++) {
				CFGNode b = node.successors[i];
				b.addPredecessor(node);
				b.ref = b.nofPredecessors;
			}
			node = node.next;
		}
	}

	private static void visitDom(CFGNode b, CFGNode predecessor) {
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

	private static CFGNode commonDom(CFGNode a, CFGNode b) {
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

	/**
	 * Eliminates all dead nodes (goto)
	 */
	private static void eliminateDeadNodes(CFG cfg) {
		CFGNode current = cfg.rootNode;
		while (current != null) {
			CFGNode next = current.next;
			if (next != null && !next.visited) // dead node
				current.next = next.next;
			current = next;
		}
	}

	private static int getInt(byte[] bytes, int index){
		return ((bytes[index]&0xff)<<24) | (bytes[index+1]&0xff)<<16 | (bytes[index+2]&0xff)<<8 | (bytes[index+3]&0xff);
	}

	private String cfgToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CFG of method " + method.owner.name + "." + method.name + "\n");
		int i = 0;
		CFGNode node = this.rootNode;
		while (node != null) {
			sb.append("\tnodeNr:" + i + " from " + node.firstBCA + " to "
					+ node.lastBCA + "\t");
			if (node.isLoopHeader())
				sb.append("is loop header");
			if (node.nofBackwardBranches > 0)
				sb.append(", bckwd branches=" + node.nofBackwardBranches);
			sb.append(", ref="+node.ref);
			sb.append(", visited:"+node.visited);
			sb.append("\n\t\tpredecessor: ");
			for (int k = 0; (k < node.predecessors.length)
					&& (node.predecessors[k] != null); k++) {
				sb.append(node.predecessors[k].toString());
				sb.append("\t");
			}
			sb.append("\n");
			sb.append("\t\tsuccessor: ");
			for (int k = 0; (k < node.successors.length)
					&& (node.successors[k] != null); k++) {
				sb.append(node.successors[k].toString());
				sb.append("\t");
			}
			sb.append("\n");
			node = node.next;
			i++;
		}
		return sb.toString();
	}

	/**
	 * Prints all nodes of a CFG for debugging purposes.
	 */
	public void printToLog() {
		StdStreams.out.println(this.cfgToString());
	}
}