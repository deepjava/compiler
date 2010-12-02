package ch.ntb.inf.deep.cfg;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.ICclassFileConsts;
import ch.ntb.inf.deep.classItems.Type;

/**
 * - create and test CFG<br>
 */
public class CFG00 extends TestCFG implements ICclassFileConsts {

	@Before
	public void setUp() {
		String workspace =System.getProperty("user.dir");
		String[] rootClassNames = new String[]{"ch/ntb/inf/deep/testClasses/T00EmptyClass"};
		try {
			Class.buildSystem(rootClassNames, new String[]{workspace + "/bin"}, null, (1<<atxCode)|(1<<atxLocalVariableTable)|(1<<atxLineNumberTable)|(1<<atxExceptions));
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
