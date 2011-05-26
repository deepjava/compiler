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
	public boolean isParam[];
	public int paramType[];
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
		
		determineParam();
		sortNodes((SSANode)cfg.rootNode);
		
		if(dbg){
			StdStreams.vrb.print("Node order: ");
			for(int i = 0; i < nofSortedNodes - 1; i++){
				StdStreams.vrb.print("[" + sortedNodes[i].firstBCA + ":"+ sortedNodes[i].lastBCA + "], ");
			}
			StdStreams.vrb.println("[" + sortedNodes[nofSortedNodes-1].firstBCA + ":"+ sortedNodes[nofSortedNodes-1].lastBCA + "]");
			if(isParam.length > 0){
				StdStreams.vrb.println("IsParam");
				StdStreams.vrb.print("[ ");
				for(int i = 0; i < isParam.length - 1; i++){
					StdStreams.vrb.print(isParam[i] + ", ");
				}
				StdStreams.vrb.println(isParam[isParam.length - 1] + " ]");
			}
			
		}
		
		determineStateArray();
		
		//if the method have multiple return statements, so check if in the last node all required params are loaded
		if(returnCount > 1){
			int nofParams = cfg.method.nofParams;
			if(((cfg.method.accAndPropFlags & (1 << apfStatic)) == 0) && ((cfg.method.accAndPropFlags & (1 << dpfSysPrimitive)) == 0)){//method isn't static
				nofParams++;//implizit this parameter
			}
			if(nofParams > 0){
				//search last node
				SSANode last = null;
				for(int i = 0;  i < returnCount; i++){
					if(last == null){
						last = returnNodes[i];
					}else if(last.firstBCA < returnNodes[i].firstBCA){
						last = returnNodes[i];
					}					
				}
				for(int x = 0; x < nofParams; x++){
					boolean isNeeded = false;
					for(int i = 0; i < returnCount && !isNeeded; i++){
						if(returnNodes[i].exitSet[cfg.method.maxStackSlots + x] != null){
							isNeeded = true;
						}
					}
					if(isNeeded){	
						last.loadLocal(cfg.method.maxStackSlots + x, paramType[cfg.method.maxStackSlots + x]);
					}
								
				}
			}
		}
		
		
		renumberInstructions(cfg);
		if(dbg)print(0);
	}

	public void determineStateArray() {		
		// visit all
		for (int i = 0; i < nofSortedNodes; i++) {
			if(dbg)StdStreams.vrb.println(((CFGNode)sortedNodes[i]).toString());
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
		int flags = cfg.method.accAndPropFlags;
		String descriptor = cfg.method.methDescriptor.toString();
		int index = cfg.method.getMaxStckSlots();
		isParam = new boolean[cfg.method.getMaxStckSlots() + cfg.method.getMaxLocals()];
		paramType = new int[cfg.method.getMaxStckSlots() + cfg.method.getMaxLocals()];
		if(((flags & (1 << apfStatic)) == 0) && ((flags & (1 << dpfSysPrimitive)) == 0)){//method isn't static
			isParam[index] = true;
			paramType[index++] = SSAValue.tRef;
		}
		
		char ch = descriptor.charAt(1);
		for(int i = 1;ch != ')'; i++){//travers only between (....);
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
	 */
	public void print(int level) {
		int count = 0;
		SSANode node = (SSANode) this.cfg.rootNode;

		for (int i = 0; i < level; i++)
			StdStreams.vrb.print("\t");
		StdStreams.vrb.println("SSA for Method: " + cfg.method.owner.name + "." + cfg.method.name + cfg.method.methDescriptor);
		
		SSA.renumberInstructions(cfg);

		while (node != null) {
			node.print(level + 1, count);
			StdStreams.vrb.println("");
			node = (SSANode) node.next;
			count++;
		}
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
	
	

}
