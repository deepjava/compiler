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
		
		
		populateStateArray();
	}
	
	public void populateStateArray(){
		
		
		
		SSANode a = new SSANode();
		a.mergeAndPopulateStateArray(this);
		/*
		for (SSANode ssaNode : todo) {

			// add Loop-Header to be processed twice
			if (ssaNode.isLoopHeader() && !ssaNode.visited) {
				todo.add(ssaNode);
			}

			// First: merge State-Arrays of Parent Nodes
			ssaNode.mergeStateArrays(maxStack, maxLocals);

			// Second: process block and update state-array
			if (!(ssaNode.visited)) {
				ssaNode.visited = true;
				ssaNode.populateStateArray(maxStack, maxLocals);
			}
		}
				
		*/
		
		
	}

}
