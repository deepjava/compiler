package ch.ntb.inf.deep.ssa;

import ch.ntb.inf.deep.cfg.CFG;

/**
 * @author millischer
 */

public class SSA {
	public CFG cfg;
	public int nofLoopheaders;
	public boolean isParam[];
	public int paramType[];
	private SSANode loopHeaders[];
	private SSANode sortedNodes[];
	private int nofSortedNodes;
	public int nofGPR, nofFPR;

	public SSA(CFG cfg) {
		this.cfg = cfg;
		loopHeaders = new SSANode[cfg.getNumberOfNodes()];
		sortedNodes = new SSANode[cfg.getNumberOfNodes()];
		nofLoopheaders = 0;
		nofSortedNodes = 0;
		
		determineParam();
		sortNodes((SSANode)cfg.rootNode);
		determineStateArray();
		renumberInstructions(cfg);
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

	}

	private void sortNodes(SSANode rootNode) {
		if (rootNode.traversed) {// if its already processed
			return;
		}
		rootNode.traversed = true;
		if (rootNode.nofPredecessors > 0) {
			if (rootNode.isLoopHeader()) {
				// mark loop headers for traverse a second time
				loopHeaders[nofLoopheaders++] = rootNode;
				sortedNodes[nofSortedNodes++] = rootNode;
			} else {
				for (int i = 0; i < rootNode.nofPredecessors; i++) {
					if (!((SSANode) rootNode.predecessors[i]).traversed) {
						sortNodes((SSANode) rootNode.predecessors[i]);
					}
				}
				sortedNodes[nofSortedNodes++] = rootNode;
			}
		} else {
			sortedNodes[nofSortedNodes++] = rootNode;
		}
		for (int i = 0; i < rootNode.nofSuccessors; i++) {
			sortNodes((SSANode) rootNode.successors[i]);
		}
	}
	public SSANode[] getNodes(){
		SSANode current = (SSANode) this.cfg.rootNode;
		SSANode[] nodes = new SSANode[this.getNofNodes()];
		for(int i = 0; i < nodes.length; i++){
			nodes[i] = current;
			current = (SSANode)current.next;
		}
		return nodes;		
	}
	
	public int getNofNodes(){
		int count = 0;
		SSANode current = (SSANode)this.cfg.rootNode;
		while(current != null){
			count++;
			current = (SSANode)current.next;
		}
		return count;
	}

	/**
	 * Renumber all the instructions in the SSA before computing live intervals
	 */
	public static void renumberInstructions(CFG cfg) {
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
	}
	
	
	private void determineParam(){
		int flags = cfg.method.accAndPorpFlags;
		String descriptor = cfg.method.methDescriptor.toString();
		int index = cfg.method.getMaxStckSlots();
		isParam = new boolean[cfg.method.getMaxStckSlots() + cfg.method.getMaxLocals()];
		paramType = new int[cfg.method.getMaxStckSlots() + cfg.method.getMaxLocals()];
		
		if((flags & 0x0008) == 0){//method isn't static
			isParam[index] = true;
			paramType[index++] = SSAValue.tThis;
		}
		
		char ch = descriptor.charAt(1);
		for(int i = 1;ch != ')'; i++){//travers only between (....);
			isParam[index] = true;
			if(ch == '['){
				while(ch == '['){
					i++;
					ch = descriptor.charAt(i);
				}
				paramType[index++] = decodeFieldType(ch)+10;//+10 is for Arrays
				
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
			type = SSAValue.tByte;
			break;
		case 'C':
			type = SSAValue.tChar;
			break;
		case 'D':
			type = SSAValue.tDouble;
			break;
		case 'F':
			type = SSAValue.tFloat;
			break;
		case 'I':
			type = SSAValue.tInteger;
			break;
		case 'J':
			type = SSAValue.tLong;
			break;
		case 'L':
			type = SSAValue.tObject;
			break;
		case 'S':
			type = SSAValue.tShort;
			break;
		case 'Z':
			type = SSAValue.tBoolean;
			break;
		default:
			type = SSAValue.tVoid;
			break;
		}
		return type;
	}

	/**
	 * Prints out the SSA readable.
	 * <p>
	 * <b>Example:</b>
	 * <p>
	 * 
	 * <pre>
	 * SSA 4:
	 *     SSANode0:
	 *       EntrySet {[ , ], [ ,  ]}
	 *          NoOpnd[sCloadConst]
	 *          Dyadic[sCadd] ( Integer, Integer )
	 *          Dyadic[sCadd] ( Integer, Integer )
	 *          Dyadic[sCadd] ( Integer, Integer )
	 *          Monadic[sCloadVar] ( Void )
	 *          NoOpnd[sCloadConst]
	 *          Dyadic[sCadd] ( Integer, Integer )
	 *       ExitSet {[ , ], [ Integer (null), Integer (null) ]}
	 * </pre>
	 * 
	 * @param level
	 *            defines how much to indent
	 * @param SSANr
	 *            the Number of the SSA in this class
	 */
	public void print(int level, int SSANr) {
		int count = 0;
		SSANode node = (SSANode) this.cfg.rootNode;

		for (int i = 0; i < level; i++)
			System.out.print("\t");
		System.out.println("SSA for Method: " + cfg.method.name);
		
		SSA.renumberInstructions(cfg);

		while (node != null) {
			node.print(level + 1, count);
			System.out.println("");
			node = (SSANode) node.next;
			count++;
		}

	}

}
