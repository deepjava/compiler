package ch.ntb.inf.deep.ssa;

import ch.ntb.inf.deep.cfg.CFG;


/**
 * @author  millischer
 */

public class SSA {
	protected CFG cfg;
	int nofLoopheaders;
	SSANode loopHeaders[];
	
	
	public SSA(CFG cfg) {
		this.cfg = cfg;
		loopHeaders= new SSANode[cfg.getNumberOfNodes()];
		nofLoopheaders = 0;
		
		
		determineStateArray();
	}
	
	public void determineStateArray(){
		//Array of exist SSANodes
		SSANode[] nodes = (SSANode[])cfg.getNodes();
		
		for(int i = 0;i < nodes.length;i++){
			//mark loopheaders for traverse a second time 
			if(nodes[i].isLoopHeader()){
				loopHeaders[nofLoopheaders]= nodes[i];
				nofLoopheaders++;
			}
			nodes[i].mergeAndDetermineStateArray(this);
		}
		
		for(int i = 0;i < nofLoopheaders;i++){
			loopHeaders[i].mergeAndDetermineStateArray(this);
		}		
	}
}
