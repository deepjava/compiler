package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Type;
import ch.ntb.inf.deep.ssa.SSANode;

public class SSA00 extends TestSSA {

	@Before
	public void setUp() {
    	String[] rootClassNames = new String[]{"ch/ntb/inf/deep/testClasses/T00EmptyClass"};
		try {
			Class.buildSystem(rootClassNames, (1<<IClassFileConsts.atxCode)|(1<<IClassFileConsts.atxLocalVariableTable)|(1<<IClassFileConsts.atxLineNumberTable)|(1<<IClassFileConsts.atxExceptions));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(Type.nofRootClasses > 0){
			createSSA(Type.rootClasses[0]);
		}
	}

    @Test
	public void testConstructor() {
    	// constructor
		SSANode[] nodes = getAndTestNodes(0, 1);
//		SSAValue exitSet[] = new SSAValue[] 
//		testNode(nodes[0], 2, 0, new SSAValue[] {}, {});
	}
}
