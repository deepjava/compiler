package ch.ntb.inf.deep.cfg;

import org.eclipse.jdt.core.util.ICodeAttribute;
import org.eclipse.jdt.core.util.IMethodInfo;

import ch.ntb.inf.deep.classItems.Method;
import ch.ntb.inf.deep.ssa.SSANode;


/**
 * Builder for the CFG of a java method. Reads the bytecode instructions and
 * builds the CFG.
 * 
 * 
 * @author buesser 23.2.2010, graf
 */
public class CFG implements JvmInstructionMnemonics {
	private static final boolean debug = false;

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
		code = method.code;

		int len = code.length;
//		if (codeAttr != null) {
//			len = (int) codeAttr.getCodeLength();
//		} else {
//			len = 0;
//		}
		if (debug) {
			System.out.println("code of method "+method.name);			
			for (int i = 0; i < len; i++) {
				System.out.print((code[i] & 0xff) + "  ");
				if ((i % 8) == 7)
					System.out.println();
			}
			System.out.println();
		}
		startNode.firstBCA = 0;
		startNode.lastBCA = findLastBcaInNode(this, startNode, len);
		int bca = 0;
		while (bca < len) {
			int entry = bcAttrTab[code[bca] & 0xff];
			assert ((entry & (1 << bcapCFGnotImpl)) == 0) : "bytecode instruction not implemented";
			if ((entry & bcmpOpcLen) == 0) {
				if ((code[bca] & 0xff) == bCtableswitch) {
					int tAddr = (bca + 4) / 4 * 4;
					int def = code[tAddr] & 0xff << 24 | code[tAddr + 1]
							& 0xff << 16 | code[tAddr + 2] & 0xff << 8
							| code[tAddr + 3] & 0xff;
					tAddr += 4;
					int low = code[tAddr] & 0xff << 24 | code[tAddr + 1]
							& 0xff << 16 | code[tAddr + 2] & 0xff << 8
							| code[tAddr + 3] & 0xff;
					tAddr += 4;
					int high = code[tAddr] & 0xff << 24 | code[tAddr + 1]
							& 0xff << 16 | code[tAddr + 2] & 0xff << 8
							| code[tAddr + 3] & 0xff;
					tAddr += 4;
					int nofCases = high - low + 1;
					split(this, bca, bca + def);
					for (int i = 0; i < nofCases; i++) {
						int branchOffset = code[tAddr] & 0xff << 24
								| code[tAddr + 1] & 0xff << 16
								| code[tAddr + 2] & 0xff << 8 | code[tAddr + 3]
								& 0xff;
						split(this, bca, bca + branchOffset);
						tAddr += 4;
					}
					int tableLen = tableSwitchLen(this, bca);
					bca += tableLen;
				} else {
					if ((code[bca] & 0xff) == bClookupswitch) {
						int tAddr = (bca + 4) / 4 * 4;
						int def = code[tAddr] & 0xff << 24 | code[tAddr + 1]
								& 0xff << 16 | code[tAddr + 2] & 0xff << 8
								| code[tAddr + 3] & 0xff;
						tAddr += 4;
						int npairs = code[tAddr] & 0xff << 24 | code[tAddr + 1]
								& 0xff << 16 | code[tAddr + 2] & 0xff << 8
								| code[tAddr + 3] & 0xff;
						tAddr += 8;
						split(this, bca, bca + def);
						for (int i = 0; i < npairs; i++) {
							int branchOffset = code[tAddr] & 0xff << 24
									| code[tAddr + 1] & 0xff << 16
									| code[tAddr + 2] & 0xff << 8
									| code[tAddr + 3] & 0xff;
							split(this, bca, bca + branchOffset);
							tAddr += 8;
						}
						int tableLen = lookupSwitchLen(this, bca);
						bca += tableLen;
					} else { // wide
						if ((code[bca + 1] & 0xff) == bCiinc)
							bca += 6;
						else
							bca += 4;
					}
				}
			} else {
				if ((entry & (1 << bcapBranch)) != 0) {
					int branchOffset = (short) (code[bca + 1] & 0xff << 8 | code[bca + 2]);
					split(this, bca, bca + branchOffset);
				}
				bca += (entry & bcmpOpcLen) >> 8;
			}
		}
		assert (bca == len) : "last instruction not at end of method";
		markLoopHeaders(rootNode);
		eliminateDeadNodes(this);
		enterPredecessors(this);
		CFGNode current = this.rootNode;	// prepare for finding dominators
		while (current != null) {
			current.visited = false;
			current = current.next;
		}
		rootNode.idom = null;
		for (int i = 0; i < rootNode.nofSuccessors; i++)
			visitDom(rootNode.successors[i], rootNode);
		if (debug)
			printToLog();
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
			int len;

			entry = bcAttrTab[code[bca] & 0xff];
			if ((entry & bcmpOpcLen) == 0) {
				if ((code[bca] & 0xff) == bCtableswitch) {
					len = tableSwitchLen(cfg, bca);
				} else {
					if ((code[bca] & 0xff) == bClookupswitch) {
						len = lookupSwitchLen(cfg, bca);
					} else { // wide instruction
						if ((code[bca + 1] & 0xff) == bCiinc)
							len = 6;
						else
							len = 4;
					}
				}
			} else {
				len = (entry & bcmpOpcLen) >> 8;
			}
			newNode.firstBCA = bca + len;
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
			if (debug)
				System.out
						.println("eliminate goto node at bca = " + branchAddr);
			// branch target is a goto, jump to new target node
			branchAddr += (short) (code[branchAddr + 1] & 0xff << 8 | code[branchAddr + 2]);
			if (debug)
				System.out.println("new branch address = " + branchAddr);
		}
		CFGNode targNode = cfg.getNode(branchAddr);
		if (branchAddr != targNode.firstBCA) { // if first instruction, no
			// splitting
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
		byte[] code = cfg.code;
		int bca = node.firstBCA;
		int len = 0;
		while (bca < addr) {
			int entry = bcAttrTab[code[bca] & 0xff];
			if ((entry & bcmpOpcLen) == 0) {
				if (debug)
					System.out.println("instruction with len undef");
				if ((code[bca] & 0xff) == bCtableswitch) {
					len = tableSwitchLen(cfg, bca);
				} else {
					if ((code[bca] & 0xff) == bClookupswitch) {
						len = lookupSwitchLen(cfg, bca);
					} else { // wide instruction
						if ((code[bca + 1] & 0xff) == bCiinc)
							len = 6;
						else
							len = 4;
					}
				}
				if (debug)
					System.out.println("len = " + len);
			} else {
				len = (entry & bcmpOpcLen) >> 8;
			}
			bca += len;
		}
		return bca - len;
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
		int tAddr = (bca + 4) / 4 * 4;
		tAddr += 4;
		int low = code[tAddr] & 0xff << 24 | code[tAddr + 1] & 0xff << 16
				| code[tAddr + 2] & 0xff << 8 | code[tAddr + 3] & 0xff;
		tAddr += 4;
		int high = code[tAddr] & 0xff << 24 | code[tAddr + 1] & 0xff << 16
				| code[tAddr + 2] & 0xff << 8 | code[tAddr + 3] & 0xff;
		return (tAddr + 4 + (high - low + 1) * 4) - bca;
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
		int tAddr = (bca + 4) / 4 * 4;
		tAddr += 4;
		int npairs = code[tAddr] & 0xff << 24 | code[tAddr + 1] & 0xff << 16
				| code[tAddr + 2] & 0xff << 8 | code[tAddr + 3] & 0xff;
		return tAddr + 4 + 8 * npairs - bca;
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
		b.ref--;
		if (b.idom == null)
			b.idom = predecessor;
		else
			b.idom = commonDom(b.idom, predecessor);
		if (b.ref == b.nofBackwardBranches)
			for (int i = 0; i < b.nofSuccessors; i++)
				visitDom(b.successors[i], b);
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

	private String cfgToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CFG of method " + method.name + "\n");
		int i = 0;
		CFGNode node = this.rootNode;
		while (node != null) {
			sb.append("\tnodeNr:" + i + " from " + node.firstBCA + " to "
					+ node.lastBCA + "\t");
			if (node.isLoopHeader())
				sb.append("is loop header");
			sb.append(node.visited);
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
	 * Prints all nodes of a CFG to System.out for debugging purposes.
	 */
	public void printToLog() {
		// System.out.println(CFGPrinter.getCFGString(this));
		System.out.println(this.cfgToString());
	}
}