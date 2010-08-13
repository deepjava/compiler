package ch.ntb.inf.deep.ssa;

import ch.ntb.inf.deep.cfg.CFG;


/**
 * @author  millischer
 */

public class SSA {
	public CFG cfg;
	int nofLoopheaders;
	SSANode loopHeaders[];
	
	
	public SSA(CFG cfg) {
		this.cfg = cfg;
		loopHeaders= new SSANode[cfg.getNumberOfNodes()];
		nofLoopheaders = 0;
		
		
		determineStateArray();
	}
	

	public void determineStateArray(){
		SSANode node = (SSANode)this.cfg.rootNode;
		while (node != null) {
			//mark loopheaders for traverse a second time 
			if(node.isLoopHeader()){
				loopHeaders[nofLoopheaders]= node;
				nofLoopheaders++;
			}
			node.mergeAndDetermineStateArray(this);
			node = (SSANode)node.next;
		}
		
		for(int i = 0;i < nofLoopheaders;i++){
			loopHeaders[i].mergeAndDetermineStateArray(this);
		}		
	}
	public void print(int level, int SSANr){
		int count = 0;
		SSANode node = (SSANode)this.cfg.rootNode;
		
		for (int i = 0; i < level; i++)System.out.print("\t");
		System.out.println("SSA"+ SSANr +":");
		
		while(node != null){
			node.print(level+1, count);
			System.out.println("");
			node = (SSANode)node.next;
			count++;
		}
		
		
	}
}
