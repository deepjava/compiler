package ch.ntb.inf.deep.cfg;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import ch.ntb.inf.deep.cfg.CFGNode;
import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.IClassFileConsts;
import ch.ntb.inf.deep.classItems.Type;

/**
 * - create and test CFG<br>
 */
public class CFG00 extends TestCFG {

	@Before
	public void setUp() {
		String workspace =System.getProperty("user.dir");
		String[] rootClassNames = new String[]{"ch/ntb/inf/deep/testClasses/T00EmptyClass"};
		try {
			Class.buildSystem(rootClassNames,workspace,(1<<IClassFileConsts.atxCode)|(1<<IClassFileConsts.atxLocalVariableTable)|(1<<IClassFileConsts.atxLineNumberTable)|(1<<IClassFileConsts.atxExceptions));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(Type.nofRootClasses > 0){
			createCFG(Type.rootClasses[0]);
		}
	}


    @Test
	public void testConstructor() {
    	// constructor
		CFGNode[] nodes = getAndTestNodes(0, 1);
		testNode(nodes[0], 0, 4, false, null, new int[] {}, new int[] {});
	}
}
