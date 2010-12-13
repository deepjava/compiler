package ch.ntb.inf.deep.ssa;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import ch.ntb.inf.deep.classItems.Class;
import ch.ntb.inf.deep.classItems.Type;

public class SSA00 extends TestSSA {

	@Before
	public void setUp() {
		String workspace =System.getProperty("user.dir")+ "/bin";
    	String[] rootClassNames = new String[]{"ch/ntb/inf/deep/testClasses/T00EmptyClass"};
		try {
			Class.buildSystem(rootClassNames,new String[]{workspace, "../bsp/bin"},null, (1<<atxCode)|(1<<atxLocalVariableTable)|(1<<atxLineNumberTable)|(1<<atxExceptions));
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
		SSANode[] node = getAndTestSSA("<init>",1,0);
		testNode(node[0], 3, 0, 2);
	}
}
